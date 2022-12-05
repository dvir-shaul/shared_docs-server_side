package docSharing.service;

import docSharing.entity.*;
import docSharing.repository.*;
import docSharing.requests.Method;
import docSharing.requests.Type;
import docSharing.response.FileRes;
import docSharing.response.UserStatus;
import docSharing.response.UsersInDocRes;
import docSharing.utils.ExceptionMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    LogRepository logRepository;

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
    public Set<User> getActiveUsers(long userId, long documentId, Method method) throws AccountNotFoundException {
        logger.info("in DocumentService -> getActiveUsers");
        onlineUsersPerDoc.putIfAbsent(documentId, new HashSet<>());
        if (method != Method.GET) {
            Optional<User> user = userRepository.findById(userId);
            if (!user.isPresent()) {
                logger.error("in DocumentService -> addUserToDocActiveUsers -> " + ExceptionMessage.NO_USER_IN_DATABASE.toString());
                throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
            }
            switch (method) {
                case ADD:
                    onlineUsersPerDoc.get(documentId).add(user.get());
                    break;
                case REMOVE:
                    onlineUsersPerDoc.get(documentId).remove(user.get());
                    break;
                default:
                    break;

            }
        }
        return onlineUsersPerDoc.get(documentId);
    }


    /**
     * getPath called from FacadeFileController when we enter a folder or document inside the client side,
     * and want to present the client the new path he has done so far.
     *
     * @param documentId - document id in the database.
     * @return - List of FileRes
     */
    public List<FileRes> getPath(long documentId) throws FileNotFoundException {
        logger.info("in DocumentService -> getPath");
        Document document = findById(documentId);
        List<FileRes> path = new ArrayList<>();
        Folder parentFolder = document.getParentFolder();
        while (parentFolder != null) {
            path.add(0, new FileRes(parentFolder.getName(), parentFolder.getId(), Type.FOLDER, Permission.ADMIN, document.getUser().getEmail()));
            parentFolder = parentFolder.getParentFolder();
        }
        return path;

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
        if (log == null) {
            throw new NullPointerException(ExceptionMessage.NULL_INPUT.toString());
        }
        Optional<Document> document = documentRepository.findById(log.getDocument().getId());
        if (!document.isPresent()) {
            logger.error("in DocumentService -> DocumentService -> " + ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS);
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString() + log.getDocument().getId());
        }
        if (!documentsContentLiveChanges.containsKey(log.getDocument().getId())) {
            Document doc = document.get();
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
        logger.info("in DocumentService -> updateCurrentContentCache, action is:" + log.getAction());
        switch (log.getAction()) {
            case DELETE:
                log.setData(documentsContentLiveChanges.get(log.getDocument().getId()).substring(log.getOffset(), log.getOffset() + Integer.valueOf(log.getData())));
                documentsContentLiveChanges.put(log.getDocument().getId(), truncateString(documentsContentLiveChanges.get(log.getDocument().getId()), log));
                break;
            case INSERT:
                documentsContentLiveChanges.put(log.getDocument().getId(), concatenateStrings(documentsContentLiveChanges.get(log.getDocument().getId()), log));
                break;
        }
        return documentsContentLiveChanges.get(log.getDocument().getId());
    }


    /**
     * @param id - document id in database.
     * @return entity of Document from database
     * @throws FileNotFoundException - no document in database with given id.
     */
    public Document findById(Long id) throws FileNotFoundException {
        logger.info("in DocumentService -> findById");
        Optional<Document> document = documentRepository.findById(id);
        if (!document.isPresent()) {
            logger.error("in DocumentService -> findById -> " + ExceptionMessage.NO_DOCUMENT_IN_DATABASE);
            throw new FileNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        }
        return document.get();
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
     * getDocById called to send back the document entity.
     *
     * @param id - document id
     * @return - Document entity
     * @throws AccountNotFoundException - Could not locate this document in the database.
     */
    public Document getDocById(Long id) throws AccountNotFoundException {
        logger.info("in DocumentService -> getDocById with docId:" + id);
        Optional<Document> optDocument = documentRepository.findById(id);
        if (!optDocument.isPresent()) {
            logger.error("in DocumentService -> getDocById -> " + ExceptionMessage.NO_DOCUMENT_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        }
        return optDocument.get();
    }

    /**
     * this function gets called when we want to show to te client all the documents that in a specific folder.
     *
     * @param parentFolderId - parent folder
     * @param userId         - current user
     * @return list with all the document entities.
     */
    public List<Document> get(Long parentFolderId, Long userId) throws AccountNotFoundException {
        logger.info("in DocumentService -> get with userId: " + userId + ", parentFolderId: " + parentFolderId);
        Optional<Folder> optFolder = folderRepository.findById(parentFolderId);
        if (!optFolder.isPresent()) {
            logger.error("in DocumentService -> get -> " + ExceptionMessage.NO_FOLDER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());
        }

        Optional<User> optUser = userRepository.findById(userId);
        if (!optUser.isPresent()) {
            logger.error("in DocumentService -> get -> " + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        User user = optUser.get();
        Folder parentFolder = optFolder.get();
        return documentRepository.findAllByUserIdAndParentFolderId(parentFolder, user);
    }

    /**
     * function get an item of kind document and uses the logics to create and save a new folder to database.
     * first check if we have a folder to create the document into.
     * then create the document and add to the folder the new document in the database,
     * same as the user is needed to be assigned to the new document.
     * set Permission of the creator as an MODERATOR.
     *
     * @param parentFolder - parent folder of the document
     * @param user         - the owner of the document
     * @param name         - name of document
     * @param content      - the content of the document
     * @return id of document.
     */
    public Long create(Folder parentFolder, User user, String name, String content) {
        logger.info("in DocumentService -> create, item :" + name);
        if (parentFolder == null) {
            logger.error("in DocumentService -> create -> " + ExceptionMessage.FOLDER_DOES_NOT_EXISTS);
            throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + parentFolder.getId());
        }
        Document doc = Document.createDocument(user, name, parentFolder, content != null ? content : "");
        Document savedDoc = documentRepository.save(doc);
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
     * @param documentId - document id in database
     * @param name       - new document name to change to.
     * @return - rows that was affected in database (1).
     */
    public int rename(long documentId, String name) {
        logger.info("in DocumentService -> rename, documentId:" + documentId + " name:" + name);

        if (documentRepository.findById(documentId).isPresent()) {
            return documentRepository.updateName(name, documentId);
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
    public String getContent(Long documentId) throws FileNotFoundException {
        logger.info("in DocumentService -> getContent, docId:" + documentId);
        if (documentRepository.findById(documentId).isPresent()) {
            String content = documentsContentLiveChanges.get(documentId);
            if (content == null || content.length() == 0) {
                String databaseContent = documentRepository.getContentFromDocument(documentId);
                documentsContentLiveChanges.put(documentId, databaseContent);
                databaseDocumentsCurrentContent.put(documentId, databaseContent);
            }
            return documentsContentLiveChanges.get(documentId);
        } else {
            throw new FileNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        }
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
    public int relocate(Folder newParentFolder, long id) throws FileNotFoundException {
        logger.info("in DocumentService -> relocate");

        if (newParentFolder != null && !folderRepository.findById(newParentFolder.getId()).isPresent()) {
            logger.error("in DocumentService -> relocate -> " + ExceptionMessage.FOLDER_DOES_NOT_EXISTS);
            throw new FileNotFoundException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }

        Optional<Document> document = documentRepository.findById(id);
        if (!document.isPresent()) {
            logger.error("in DocumentService -> relocate -> " + ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS);
            throw new FileNotFoundException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }

        Folder oldParentFolder = document.get().getParentFolder();
        document.get().setParentFolder(newParentFolder);
        oldParentFolder.removeDocument(document.get());

        if (newParentFolder != null) {
            newParentFolder.addDocument(document.get());
        }
        return documentRepository.updateParentFolderId(newParentFolder, id);
    }

    /**
     * delete file by getting the document id,
     * also remove from the maps of content we have on service.
     *
     * @param docId - gets document id .
     * @return
     */
    public int delete(long docId) throws FileNotFoundException {
        logger.info("in DocumentService -> delete");

        databaseDocumentsCurrentContent.remove(docId);
        documentsContentLiveChanges.remove(docId);

        Optional<Document> document = documentRepository.findById(docId);
        if (!document.isPresent()) {
            logger.error("in DocumentService -> delete -> " + ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS);
            throw new FileNotFoundException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        userDocumentRepository.deleteDocument(document.get());
        logRepository.deleteByDocument(document.get());
        documentRepository.deleteById(docId);
        return 1;
    }

    /**
     * checks if document id is existed in database.
     *
     * @param id - of document
     * @return - true if documentRepository.findById(id).isPresent()
     */
    public Boolean doesExist(long id) {
        logger.info("in DocumentService -> doesExist, current file id: " + id);
        return documentRepository.findById(id).isPresent();
    }


    /**
     * get called by FacadeFileController in getAll function, that's need to return all the data files, when
     * the parent folder is null.
     *
     * @param userId - user's id that files are belonged to.
     * @return - list of documents.
     * @throws IllegalArgumentException - no userId in database
     */
    public List<Document> getAllWhereParentFolderIsNull(Long userId) throws IllegalArgumentException {
        logger.info("in DocumentService -> getAllWhereParentFolderIsNull, current userId: " + userId);

        Optional<User> optUser = userRepository.findById(userId);
        if (!optUser.isPresent()) {
            logger.error("in DocumentService -> getAllWhereParentFolderIsNull -> " + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new IllegalArgumentException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        User user = optUser.get();
        return documentRepository.findAllByParentFolderIsNull(user);
    }

    /**
     * getAllUsersInDocument is a function to retrieve all users that's are linked to a specific document id.
     *
     * @param documentId - document id we search on.
     * @return - list of UserDocument entity that contain all users and their permissions on that document.
     * @throws IllegalArgumentException - no user in database
     */
    public List<UsersInDocRes> getAllUsersInDocument(Long userId, long documentId, Method method) throws IllegalArgumentException, AccountNotFoundException {
        logger.info("in DocumentService -> getAllUsersInDocument");

        Optional<Document> optDocument = documentRepository.findById(documentId);
        if (!optDocument.isPresent()) {
            logger.error("in DocumentService -> getAllUsersInDocument -> " + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new IllegalArgumentException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        Optional<User> optUser = userRepository.findById(userId);
        if (!optUser.isPresent()) {
            logger.warn("in DocumentService -> getAllUsersInDocument -> " + ExceptionMessage.NO_USER_IN_DATABASE);

        }
        Document document = optDocument.get();
        Set<Long> onlineUsers = getActiveUsers(userId, documentId, method).stream().map(u -> u.getId()).collect(Collectors.toSet());
        return userDocumentRepository.findAllUsersInDocument(document)
                .stream()
                .map(u -> new UsersInDocRes(u.getUser().getId(), u.getUser().getName(), u.getUser().getEmail(), u.getPermission(), onlineUsers.contains(u.getUser().getId()) ? UserStatus.ONLINE : UserStatus.OFFLINE))
                .collect(Collectors.toList());
    }

    /**
     * getUserPermissionInDocument sends back to the client the permission the user have.
     * checks first if the document is in the database.
     * then checks if the user is in the database.
     * finally checks if the userDocumentRepository database has the specific user with document record, if not then
     * user will be assigned as UNAUTHORIZED.
     *
     * @param userId     - user's id in database.
     * @param documentId - document id in data base.
     * @return - the permission that the specific user's id have in the document id.
     * @throws AccountNotFoundException -
     */
    public Permission getUserPermissionInDocument(long userId, long documentId) throws FileNotFoundException, AccountNotFoundException {
        logger.info("in DocumentService -> getUserPermissionInDocument, current userId: " + userId + ", documentId: " + documentId);
        Optional<Document> optDocument = documentRepository.findById(documentId);
        if (!optDocument.isPresent()) {
            logger.error("in DocumentService -> getAllUsersInDocument -> " + ExceptionMessage.NO_DOCUMENT_IN_DATABASE);
            throw new FileNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
        }
        Optional<User> optUser = userRepository.findById(userId);
        if (!optUser.isPresent()) {
            logger.error("in DocumentService -> getAllUsersInDocument -> " + ExceptionMessage.NO_USER_IN_DATABASE);
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        }
        User user = optUser.get();
        Document document = optDocument.get();
        Optional<UserDocument> userDocument = userDocumentRepository.find(document, user);
        if (!userDocument.isPresent()) {
            return Permission.UNAUTHORIZED;
        }
        return userDocument.get().getPermission();
    }

    /**
     * save entity of UserDocument in database.
     *
     * @param userDocument - user id, doc id and permission.
     * @return - UserDocument.
     */
    public UserDocument saveUserInDocument(UserDocument userDocument) {
        logger.info("in DocumentService -> saveUserInDocument");

        if (userDocument == null) {
            throw new NullPointerException(ExceptionMessage.NULL_INPUT.toString());
        }
        if(userDocumentRepository.find(userDocument.getDocument(), userDocument.getUser()).isPresent()){
            throw new IllegalArgumentException(ExceptionMessage.MULTIPLE_PRIMARY_KEY.toString());
        }
        return userDocumentRepository.save(userDocument);
    }
}
