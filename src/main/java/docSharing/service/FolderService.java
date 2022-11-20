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

    public Long create(Long userId, String name, Long parentFolderId) {
        if (parentFolderId != null) {
            System.out.println(parentFolderId + " is not null?");
            Optional<Folder> doc = folderRepository.findById(parentFolderId);
            if (!doc.isPresent())
                throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString() + parentFolderId);
        }
        return folderRepository.save(Folder.createFolder(name, parentFolderId, userId)).getId();
    }

    public int rename(Long id, String name) {
        // TODO: make sure this folder exists in the db
        System.out.println("I am renaming this folder! " + id);
        if(folderRepository.findById(id).isPresent()){
            return folderRepository.updateName(name, id);
        }
        throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());
    }

    public int relocate(Long parentFolderId, Long id) {
        boolean a = folderRepository.findById(id).isPresent();
        boolean b = folderRepository.findById(id).isPresent();
        if(!a || !b) {throw new IllegalArgumentException(ExceptionMessage.FOLDER_DOES_NOT_EXISTS.toString());}
        // TODO: make sure both folders exist in the db
        return folderRepository.updateParentFolderId(parentFolderId, id);
    }

    public void delete(Long id) {
        // make sure the user doesn't try to delete the root folder.
        // if he really wants to remove the root folder -> user deleteAll function.
        if (id == null) return;
        // make sure this folder exists!
        // CONSULT: is this even necessary?
        Optional<Folder> folderFound = folderRepository.findById(id);
        if (!folderFound.isPresent()) return;

        // RECURSIVELY CALL A FUNCTION TO REMOVE ALL FOLDERS AND DOCUMENTS FROM A SPECIFIC GIVEN FOLDER ID

        System.out.println("Inside folder " + id + ", going deeper!");
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
                    System.out.println("Removing document. id" + document.getId());
                    documentRepository.deleteById(document.getId());
                });
            }
            // eventually, remove this specific folder
            System.out.println("Removing folder. id:" + id);
            folderRepository.deleteById(id);
            return;
        }

        foldersList.forEach(folder -> delete(folder.getId()));
        System.out.println("Now, deleting the source folder. id:" + id);
        folderRepository.deleteById(id);
        System.out.println("This is the end of the recursive function!");
    }
}
