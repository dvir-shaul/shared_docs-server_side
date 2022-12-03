package docSharing.service;

import docSharing.entity.*;
import docSharing.repository.*;
import docSharing.requests.Method;
import docSharing.utils.ExceptionMessage;
import docSharing.utils.debounce.Debouncer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.*;

@Service
public class DocumentService implements ServiceInterface {
    private static Logger logger = LogManager.getLogger(DocumentService.class.getName());

    static Map<Long, String> documentsContentLiveChanges = new HashMap<>(); // current content in cache
    static Map<Long, String> databaseDocumentsCurrentContent = new HashMap<>(); // current content in database
    //<docId,<userId, log>>
    static Map<Long, Set<User>> onlineUsersPerDoc = new HashMap<>();
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    FolderRepository folderRepository;
    @Autowired
    UserDocumentRepository userDocumentRepository;
    @Autowired
    UserRepository userRepository;

    /**
     * updateDatabaseWithNewContent is a function that's get called every 10 seconds to update the database with the live
     * changes that made onto every document we have.
     */
    @Scheduled(fixedDelay = 10 * 1000)
    public void updateDatabaseWithNewContent() {
        for (Map.Entry<Long, String> entry : documentsContentLiveChanges.entrySet()) {
            if (!entry.getValue().equals(databaseDocumentsCurrentContent.get(entry.getKey()))) {
                documentRepository.updateContent(entry.getValue(), entry.getKey());
                databaseDocumentsCurrentContent.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * the goal of this function is to present all live users that are using a specific document.
     * this function gets called when we want to add a new user to a document or delete the user from document,
     *
     * @param userId     - user's id in database.
     * @param documentId - document's id in database.
     * @param method     - ADD/ REMOVE
     * @return set of current users that viewing the document.
     */
    public Set<User> addUserToDocActiveUsers(Long userId, Long documentId, Method method) {
        logger.info("in DocumentService -> addUserToDocActiveUsers");

        onlineUsersPerDoc.putIfAbsent(documentId, new HashSet<>());
        if (!userRepository.findById(userId).isPresent()) {
            logger.error("in DocumentService -> addUserToDocActiveUsers -> " + ExceptionMessage.NO_USER_IN_DATABASE.toString());
            throw new IllegalArgumentException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        User user = userRepository.findById(userId).get();
        switch (method) {
            case ADD:
                onlineUsersPerDoc.get(documentId).add(user);
                break;
            case REMOVE:
                onlineUsersPerDoc.get(documentId).remove(user);
        }
        return onlineUsersPerDoc.get(documentId);
    }

    /**
     * getActiveUsersPerDoc called when we need to update the current users that watch and use a specific document
     * to the client.
     * @param documentId - document id in database.
     * @return - set of users that use this document.
     */
    // FIXME: Can't this function be included in "addUserToDocActiveUsers"? They both do basically the same thing
    // FIXME: but with a different action...
    public Set<User> getActiveUsersPerDoc(Long documentId) {
        logger.info("in DocumentService -> getActiveUsersPerDoc");
        return onlineUsersPerDoc.get(documentId);
    }

    /**
     * main function that deals with new logs,
     * the goal is to make order in all the data that was entered to the document,
     * and to save / chain logs accordingly to who it was changed from.
     * send all data to inner functions to deal with new data and build the logs appropriately.
     *
     * @param log - log with new data.
     */
    public String updateContent(Log log) {
        logger.info("in DocumentService -> updateContent");

        if (!documentRepository.findById(log.getDocument().getId()).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString() + log.getDocument().getId());
        }
        if (!documentsContentLiveChanges.containsKey(log.getDocument().getId())) {
            Document doc = documentRepository.findById(log.getDocument().getId()).get();

            if (doc.getContent() == null) doc.setContent("");
            documentsContentLiveChanges.put(log.getDocument().getId(), doc.getContent());

        }
        // update document content string
        return updateCurrentContentCache(log);

    }

    /**
     * the goal of this function is to update the cached documentsContentLiveChanges map with new changes.
     * the new log is sent to inner functions called concatenateStrings/truncateString according to if it was insert/delete.
     *
     * @param log - log with new data.
     */
    private String updateCurrentContentCache(Log log) {
        logger.info("in DocumentService -> updateCurrentContentCache");
        logger.debug(log.getAction());
        switch (log.getAction()) {
            case "delete":
                log.setData(documentsContentLiveChanges.get(log.getDocument().getId()).substring(log.getOffset(), log.getOffset() + Integer.valueOf(log.getData())));
                documentsContentLiveChanges.put(log.getDocument().getId(), truncateString(documentsContentLiveChanges.get(log.getDocument().getId()), log));

                break;
            case "insert":
                documentsContentLiveChanges.put(log.getDocument().getId(), concatenateStrings(documentsContentLiveChanges.get(log.getDocument().getId()), log));
                break;
        }

        return documentsContentLiveChanges.get(log.getDocument().getId());
    }


    /**
     * @param id - document id.
     * @return entity of Document from database
     * @throws AccountNotFoundException - no document in database with given id.
     */
    public Document findById(Long id) throws AccountNotFoundException {
        logger.info("in DocumentService -> findById");

        if (!documentRepository.findById(id).isPresent()) {
            logger.error("in DocumentService -> findById -> " + ExceptionMessage.NO_DOCUMENT_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        }
        return documentRepository.findById(id).get();
    }

    /**
     * addToMap used when a new document was created and needed to add to the cached maps of logs of documents.
     *
     * @param id - of document
     */
    void addToMap(long id) {
        logger.info("in DocumentService -> addToMap");
        documentsContentLiveChanges.put(id, "");
        databaseDocumentsCurrentContent.put(id, "");
    }

    /**
     * @param id - document id
     * @return - Document entity
     * @throws AccountNotFoundException - Could not locate this document in the database.
     */
    public Document getDocById(Long id) throws AccountNotFoundException {
        logger.info("in DocumentService -> getDocById");

        if (!documentRepository.findById(id).isPresent()) {
            logger.error("in DocumentService -> getDocById -> " + ExceptionMessage.NO_DOCUMENT_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        }
        return documentRepository.findById(id).get();
    }

    /**
     * this function gets called when we want to show to te client all the documents that in a specific folder.
     *
     * @param parentFolderId - parent folder
     * @param userId         - current user
     * @return list with all the document entities.
     */
    public List<Document> get(Long parentFolderId, Long userId) throws AccountNotFoundException {
        logger.info("in DocumentService -> get");

        if (!folderRepository.findById(parentFolderId).isPresent()) {
            logger.error("in DocumentService -> get -> " + ExceptionMessage.NO_FOLDER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());
        }
        if (!userRepository.findById(userId).isPresent()) {
            logger.error("in DocumentService -> get -> " + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        logger.debug("userId: " + userId + ", parentFolderId: " + parentFolderId);
        User user = userRepository.findById(userId).get();
        Folder parentFolder = folderRepository.findById(parentFolderId).get();
        return documentRepository.findAllByUserIdAndParentFolderId(parentFolder, user);
    }

    /**
     * function get an item of kind document and uses the logics to create and save a new folder to database.
     * first check if we have a folder to create the document into.
     * then create the document and add to the folder the new document in the database,
     * same as the user is needed to be assigned to the new document.
     * set Permission of the creator as an MODERATOR.
     *
     * @param generalItem - document.
     * @return id of document.
     */
    public Long create(GeneralItem generalItem) {
        logger.info("in DocumentService -> create");

        if (generalItem.getParentFolder() != null) {
            Optional<Folder> folder = folderRepository.findById(generalItem.getParentFolder().getId());
            if (!folder.isPresent()) {
                logger.error("in DocumentService -> create -> " + ExceptionMessage.FOLDER_DOES_NOT_EXISTS);
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + generalItem.getParentFolder().getId());
            }
        }
        Document savedDoc = documentRepository.save((Document) generalItem);
        addToMap(savedDoc.getId());
        if (savedDoc.getParentFolder() != null) {
            savedDoc.getParentFolder().addDocument(savedDoc);
        }
        savedDoc.getUser().addDocument(savedDoc);
        UserDocument userDocument = new UserDocument();
        userDocument.setId(new UserDocumentPk());
        userDocument.setDocument(savedDoc);
        userDocument.setUser(savedDoc.getUser());
        userDocument.setPermission(Permission.ADMIN);
        userDocumentRepository.save(userDocument);
        return savedDoc.getId();
    }

    /**
     * this function goal is to change name of a document to a new one.
     *
     * @param docId - document id in database
     * @param name  - new document name to change to.
     * @return - rows that was affected in database (1).
     */
    public int rename(Long docId, String name) {
        logger.info("in DocumentService -> rename");

        if (documentRepository.findById(docId).isPresent()) {
            return documentRepository.updateName(name, docId);
        }
        logger.error("in DocumentService -> rename -> " + ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS);
        throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
    }

    /**
     * this function goal is to show the live content of a document to the client.
     *
     * @param documentId - document id
     * @return content in documentsContentLiveChanges
     */
    public String getContent(Long documentId) {
        logger.info("in DocumentService -> getContent");

        String content = documentsContentLiveChanges.get(documentId);
        if (content == null || content.length() == 0) {
            String databaseContent = documentRepository.getContentFromDocument(documentId);
            documentsContentLiveChanges.put(documentId, databaseContent);
            databaseDocumentsCurrentContent.put(documentId, databaseContent);
        }
        return documentsContentLiveChanges.get(documentId);
    }

    /**
     * this function gets called from updateCurrentContentCache when the document content was changed.
     * the goal is to put the log in the correct offset we have and update the string accordingly.
     *
     * @param text - document content
     * @param log  - log with new data
     * @return updated content
     */
    public static String concatenateStrings(String text, Log log) {
        logger.info("in DocumentService -> concatenateStrings");

        String beforeCut = text.substring(0, log.getOffset());
        String afterCut = text.substring(log.getOffset());
        return beforeCut + log.getData() + afterCut;
    }

    /**
     * this function gets called from updateCurrentContentCache when the document content was changed.
     * the goal is to delete the data that is in the log and to apply it on the correct offset in the content at the doc.
     *
     * @param text - document content.
     * @param log  - log with new data.
     * @return updated content.
     */
    public static String truncateString(String text, Log log) {
        logger.info("in DocumentService -> truncateString");

        String beforeCut = text.substring(0, log.getOffset());
        String afterCut = text.substring(log.getOffset() + log.getData().length());
        return beforeCut.concat(afterCut);
    }

    /**
     * relocate is to change the document's location.
     *
     * @param newParentFolder - the folder that document is located.
     * @param id              - document id.
     * @return rows affected in mysql.
     */
    public int relocate(Folder newParentFolder, Long id) {
        logger.info("in DocumentService -> relocate");

        if (newParentFolder != null && !folderRepository.findById(newParentFolder.getId()).isPresent()) {
            logger.error("in DocumentService -> relocate -> " + ExceptionMessage.FOLDER_DOES_NOT_EXISTS);
            throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }
        if (!documentRepository.findById(id).isPresent()) {
            logger.error("in DocumentService -> relocate -> " + ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS);
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        Document doc = documentRepository.findById(id).get();
        Folder oldParentFolder = doc.getParentFolder();
        doc.setParentFolder(newParentFolder);
        oldParentFolder.removeDocument(doc);
        if (newParentFolder != null) {
            newParentFolder.addDocument(doc);
        }
        return documentRepository.updateParentFolderId(newParentFolder, id);
    }

    /**
     * delete file by getting the document id,
     * also remove from the maps of content we have on service.
     *
     * @param docId - gets document id .
     */
    public void delete(Long docId) {
        logger.info("in DocumentService -> delete");

        databaseDocumentsCurrentContent.remove(docId);
        documentsContentLiveChanges.remove(docId);
        if (!documentRepository.findById(docId).isPresent()) {
            logger.error("in DocumentService -> delete -> " + ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS);
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        userDocumentRepository.deleteDocument(documentRepository.findById(docId).get());
        documentRepository.deleteById(docId);
    }

    /**
     * get called by AbstartController in getAll function, thats need to return all the data files, when
     * the parent folder is null.
     * @param userId - user's id that files are belonged to.
     * @return - list of documents.
     * @throws AccountNotFoundException-
     */
    public List<Document> getAllWhereParentFolderIsNull(Long userId) throws AccountNotFoundException {
        logger.info("in DocumentService -> getAllWhereParentFolderIsNull");
        logger.debug("userId: " + userId);

        if (!userRepository.findById(userId).isPresent()) {
            logger.error("in DocumentService -> getAllWhereParentFolderIsNull -> " + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        User user = userRepository.findById(userId).get();
        return documentRepository.findAllByParentFolderIsNull(user);
    }

    /**
     * getAllUsersInDocument is a function to retrive all users that's are linked to a specific document id.
     * @param documentId - document id we search on.
     * @return - list of UserDocument entity that contain all users and their permissions on that document.
     * @throws AccountNotFoundException -
     */
    public List<UserDocument> getAllUsersInDocument(Long documentId) throws AccountNotFoundException {
        logger.info("in DocumentService -> getAllUsersInDocument");

        if (!documentRepository.findById(documentId).isPresent()) {
            logger.error("in DocumentService -> getAllUsersInDocument -> " + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        Document document = documentRepository.findById(documentId).get();
        return userDocumentRepository.findAllUsersInDocument(document);
    }

    /**
     * @param userId     - user's id in database.
     * @param documentId - document id in data base.
     * @return - the permission that the specific user's id have in the document id.
     * @throws AccountNotFoundException -
     */
    public Permission getUserPermissionInDocument(Long userId, Long documentId) throws AccountNotFoundException {
        logger.info("in DocumentService -> getUserPermissionInDocument");
        logger.debug("userId: " + userId + ", documentId: " + documentId);
        if (!documentRepository.findById(documentId).isPresent()) {
            logger.error("in DocumentService -> getAllUsersInDocument -> " + ExceptionMessage.NO_DOCUMENT_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        }
        if (!userRepository.findById(userId).isPresent()) {
            logger.error("in DocumentService -> getAllUsersInDocument -> " + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        User user = userRepository.findById(userId).get();
        Document document = documentRepository.findById(documentId).get();
        Optional<UserDocument> userDocument = userDocumentRepository.find(document, user);
        if (!userDocument.isPresent()) {
            return Permission.UNAUTHORIZED;
        }
        return userDocument.get().getPermission();
    }

    /**
     * save entity of UserDocument in database.
     * @param userDocument - user id, doc id and permission.
     * @return - UserDocument.
     */
    public UserDocument saveUserInDocument(UserDocument userDocument) {
        logger.info("in DocumentService -> saveUserInDocument");
        return userDocumentRepository.save(userDocument);
    }
}
