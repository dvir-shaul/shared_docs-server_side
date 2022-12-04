package docSharing.service;

import docSharing.entity.*;
import docSharing.repository.*;
import docSharing.requests.Method;
import docSharing.requests.Type;
import docSharing.response.FileRes;
import docSharing.response.UserStatus;
import docSharing.response.UsersInDocRes;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentService implements ServiceInterface {
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
    public Set<User> getActiveUsers(Long userId, Long documentId, Method method) {
        onlineUsersPerDoc.putIfAbsent(documentId, new HashSet<>());
        if (method != Method.GET) {
            if (!userRepository.findById(userId).isPresent()) {
                throw new IllegalArgumentException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
            }
            User user = userRepository.findById(userId).get();
            switch (method) {
                case ADD:
                    onlineUsersPerDoc.get(documentId).add(user);
                    break;
                case REMOVE:
                    onlineUsersPerDoc.get(documentId).remove(user);
                    break;
                default:
                    break;

            }
        }
        return onlineUsersPerDoc.get(documentId);
    }

    public List<FileRes> getPath(Long documentId) {
        try {
            Document document = findById(documentId);
            List<FileRes> path = new ArrayList<>();
            Folder parentFolder = document.getParentFolder();
            while (parentFolder != null) {
                path.add(0, new FileRes(parentFolder.getName(), parentFolder.getId(), Type.FOLDER, Permission.ADMIN, document.getUser().getEmail()));
                parentFolder = parentFolder.getParentFolder();
            }
            return path;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
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
    public Document findById(Long id) throws FileNotFoundException {
        Optional<Document> document = documentRepository.findById(id);
        if (!document.isPresent())
            throw new FileNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());

        return document.get();
    }

    /**
     * addToMap used when a new document was created and needed to add to the cached maps of logs of documents.
     *
     * @param id - of document
     */
    void addToMap(long id) {
        documentsContentLiveChanges.put(id, "");
        databaseDocumentsCurrentContent.put(id, "");
    }

    /**
     * @param id - document id
     * @return - Document entity
     * @throws AccountNotFoundException - Could not locate this document in the database.
     */
    public Document getDocById(Long id) throws AccountNotFoundException {
        if (!documentRepository.findById(id).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_DOCUMENT_IN_DATABASE.toString());
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
        if (!folderRepository.findById(parentFolderId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_FOLDER_IN_DATABASE.toString());
        if (!userRepository.findById(userId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
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
     * @param parentFolder - parent folder of the document
     * @param user         - the owner of the document
     * @param name         - name of document
     * @param content      - the content of the document
     * @return id of document.
     */
    public Long create(Folder parentFolder, User user, String name, String content) {
        if (parentFolder == null) {
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
     * @param docId - document id in database
     * @param name  - new document name to change to.
     * @return - rows that was affected in database (1).
     */
    public int rename(Long docId, String name) {
        if (documentRepository.findById(docId).isPresent()) {
            return documentRepository.updateName(name, docId);
        }
        throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
    }


    /**
     * this function goal is to show the live content of a document to the client.
     *
     * @param documentId - document id
     * @return content in documentsContentLiveChanges
     */
    public String getContent(Long documentId) {
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
    public int relocate(Folder newParentFolder, Long id) throws FileNotFoundException {
        if (newParentFolder != null && !folderRepository.findById(newParentFolder.getId()).isPresent()) {
            throw new FileNotFoundException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }

        Optional<Document> document = documentRepository.findById(id);
        if (!document.isPresent()) {
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
     */
    public void delete(Long docId) throws FileNotFoundException {
        databaseDocumentsCurrentContent.remove(docId);
        documentsContentLiveChanges.remove(docId);

        Optional<Document> document = documentRepository.findById(docId);
        if (!document.isPresent())
            throw new FileNotFoundException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());

        userDocumentRepository.deleteDocument(document.get());
        documentRepository.deleteById(docId);
        for (Log log : document.get().getLogs()) {
            logRepository.delete(log);
        }
        // CONSULT: can we get the number of lines affected to return to the user?
    }

    public Boolean doesExist(Long id) {
        return documentRepository.findById(id).isPresent();
    }


    public List<Document> getAllWhereParentFolderIsNull(Long userId) throws AccountNotFoundException {
        if (!userRepository.findById(userId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        User user = userRepository.findById(userId).get();
        return documentRepository.findAllByParentFolderIsNull(user);
    }

    public List<UsersInDocRes> getAllUsersInDocument(Long userId, Long documentId, Method method) throws AccountNotFoundException {
        if (!documentRepository.findById(documentId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        Document document = documentRepository.findById(documentId).get();
        Set<Long> onlineUsers = getActiveUsers(userId, documentId, method).stream().map(u -> u.getId()).collect(Collectors.toSet());
        return userDocumentRepository.findAllUsersInDocument(document)
                .stream()
                .map(u -> new UsersInDocRes(u.getUser().getId(), u.getUser().getName(), u.getUser().getEmail(), u.getPermission(), onlineUsers.contains(u.getUser().getId()) ? UserStatus.ONLINE : UserStatus.OFFLINE))
                .collect(Collectors.toList());
    }

    public Permission getUserPermissionInDocument(Long userId, Long documentId) throws AccountNotFoundException {
        if (!documentRepository.findById(documentId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        if (!userRepository.findById(userId).isPresent())
            throw new AccountNotFoundException(ExceptionMessage.NO_USER_IN_DATABASE.toString());
        User user = userRepository.findById(userId).get();
        Document document = documentRepository.findById(documentId).get();
        Optional<UserDocument> userDocument = userDocumentRepository.find(document, user);
        if (!userDocument.isPresent()) {
            return Permission.UNAUTHORIZED;
        }
        return userDocument.get().getPermission();
    }

    public UserDocument saveUserInDocument(UserDocument userDocument) {
        return userDocumentRepository.save(userDocument);
    }
}
