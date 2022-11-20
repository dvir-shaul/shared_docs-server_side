package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.utils.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FolderService implements ServiceInterface {

    @Autowired
    FolderRepository folderRepository;
    @Autowired
    DocumentRepository documentRepository;

    /**
     *  create new folder.
     * @param userId - the user that creates the file, the admin.
     * @param name - name of document.
     * @param parentFolderId -what folder to create the folder into.
     * @return - id of the folder that was created.
     */
    public Long create(Long userId, String name, Long parentFolderId) {
        if (parentFolderId != null) {
            System.out.println(parentFolderId + " is not null?");
            Optional<Folder> doc = folderRepository.findById(parentFolderId);
            if (!doc.isPresent())
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + parentFolderId);
        }
        return folderRepository.save(Folder.createFolder(name, parentFolderId, userId)).getId();
    }

    /**
     * rename function gets an id of folder and new name to change the folder's name.
     * @param id - document id.
     * @param name - new name of the document.
     * @return rows affected in mysql.
     */
    public int rename(Long id, String name) {
        if(folderRepository.findById(id).isPresent()){
            return folderRepository.updateName(name, id);
        }
        throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
    }

    /**
     * relocate is to change the document's location.
     * @param parentFolderId - the folder that folder is located.
     * @param id - document id.
     * @return rows affected in mysql.
     */
    public int relocate(Long parentFolderId, Long id) {
        boolean a = folderRepository.findById(id).isPresent();
        boolean b = folderRepository.findById(id).isPresent();
        if(!a || !b) {throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());}
        return folderRepository.updateParentFolderId(parentFolderId, id);
    }
    /**
     * delete folder by getting the document id, and deleting all it's content by recursively
     * going through the folder files and folders in it.
     * @param id - gets folder id to start delete the content.
     */
    public void delete(Long id) {
        // make sure the user doesn't try to delete the root folder.
        // if he really wants to remove the root folder -> user deleteAll function.
        if (id == null) return;
        // make sure this folder exists!
        // CONSULT: is this even necessary?
        Optional<Folder> folderFound = folderRepository.findById(id);
        if (!folderFound.isPresent()) return;
        // RECURSIVELY CALL A FUNCTION TO REMOVE ALL FOLDERS AND DOCUMENTS FROM A SPECIFIC GIVEN FOLDER ID
        // get all folders inside this specific folder
        List<Folder> foldersList = folderRepository.findAllByParentFolderId(id);
        // stop condition -> if list is empty. otherwise continue digging.
        if (foldersList.size() == 0) {
            // find all the documents inside this folder and delete them!
            List<Document> documentList = documentRepository.findAllByParentFolderId(id);
            // check if this list is empty of documents
            if (documentList.size() > 0) {
                // remove every document inside this folder
                documentList.forEach(document -> {
                    documentRepository.deleteById(document.getId());
                });
            }
            // eventually, remove this specific folder
            folderRepository.deleteById(id);
            return;
        }
        foldersList.forEach(folder -> delete(folder.getId()));
        folderRepository.deleteById(id);
    }
}
