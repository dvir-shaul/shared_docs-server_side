package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.GeneralItem;
import docSharing.entity.Folder;
import docSharing.entity.User;
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

    public Long create(GeneralItem generalItem) {

        if (generalItem.getParentFolder() != null) {
            Optional<Folder> folder = folderRepository.findById(generalItem.getParentFolder().getId());
            if (!folder.isPresent())
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + generalItem.getParentFolder().getId());
        }
        Folder parentFolder=generalItem.getParentFolder();
        User user = generalItem.getUser();
        Folder folder=(Folder) generalItem;
        if(parentFolder!=null) {
            parentFolder.addFolder(folder);
        }
        user.addFolder(folder);
        return folderRepository.save(folder).getId();

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
     * @param newParentFolder - the folder that folder is located.
     * @param id - document id.
     * @return rows affected in mysql.
     */
    public int relocate(Folder newParentFolder, Long id) {
        if (!folderRepository.findById(newParentFolder.getId()).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
        }
        if (!folderRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DOCUMENT_DOES_NOT_EXISTS.toString());
        }
        Folder folder= folderRepository.findById(id).get();
        Folder oldParentFolder=folder.getParentFolder();
        oldParentFolder.removeFolder(folder);
        newParentFolder.addFolder(folder);
        return documentRepository.updateParentFolderId(newParentFolder, id);
    }
    /**
     * delete folder by getting the document id, and deleting all it's content by recursively
     * going through the folder files and folders in it.
     * @param id - gets folder id to start delete the content.
     */
    public void delete(Long id) {
//        // make sure the user doesn't try to delete the root folder.
//        // if he really wants to remove the root folder -> user deleteAll function.
//        if (id == null) return;
//        // make sure this folder exists!
//        // CONSULT: is this even necessary?
//        Optional<Folder> folderFound = folderRepository.findById(id);
//        if (!folderFound.isPresent()) return;
//        //remove all content from a folder
//        folderFound.get().removeAllContent();
//        // RECURSIVELY CALL A FUNCTION TO REMOVE ALL FOLDERS AND DOCUMENTS FROM A SPECIFIC GIVEN FOLDER ID
//        // get all folders inside this specific folder
//        List<Folder> foldersList = folderRepository.findAllByParentFolderId(id);
//        // stop condition -> if list is empty. otherwise continue digging.
//        if (foldersList.size() == 0) {
//            // find all the documents inside this folder and delete them!
//            List<Document> documentList = documentRepository.findAllByParentFolderId(id);
//            // check if this list is empty of documents
//            if (documentList.size() > 0) {
//                // remove every document inside this folder
//                documentList.forEach(document -> {
//                    documentRepository.deleteById(document.getId());
//                });
//            }
//            // eventually, remove this specific folder
//            folderRepository.deleteById(id);
//            return;
//        }
//        foldersList.forEach(folder -> delete(folder.getId()));
        folderRepository.deleteById(id);
    }

}
