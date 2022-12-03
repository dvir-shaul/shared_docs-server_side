package docSharing.controller;

import docSharing.entity.Document;
import docSharing.entity.GeneralItem;
import docSharing.entity.Folder;
import docSharing.entity.Permission;
import docSharing.requests.Type;
import docSharing.response.ExportDoc;
import docSharing.response.FileRes;
import docSharing.response.Response;
import docSharing.service.*;
import docSharing.utils.Regex;
import docSharing.utils.Validations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Component
public class FacadeController {

    @Autowired
    DocumentService documentService;
    @Autowired
    FolderService folderService;

    public ResponseEntity<Response> getAll(Long parentFolderId, Long userId) {
        try {
            List<Folder> folders;
            List<Document> documents;
            if (parentFolderId != null) {
                folders = folderService.get(parentFolderId, userId);
                documents = documentService.get(parentFolderId, userId);
            } else {
                folders = folderService.getAllWhereParentFolderIsNull(userId);
                documents = documentService.getAllWhereParentFolderIsNull(userId);
            }
            return ResponseEntity.ok().body(new Response.Builder()
                    .status(HttpStatus.OK)
                    .data(convertToFileRes(folders, documents))
                    .message("getAll function worked")
                    .build());

        } catch (AccountNotFoundException e) {
            // TODO: we need to throw more exceptions so we know what status to retrieve!
            return ResponseEntity.badRequest().body(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .build());
        }
    }

//    public ResponseEntity<Response> get(Long id, Class c) {
//        if (id == null)
//            return ResponseEntity.badRequest().body(new Response.Builder()
//                    .status(HttpStatus.BAD_REQUEST)
//                    .message("In order to relocate, an ID must be provided!")
//                    .build());
//
//        return ResponseEntity.ok().body(convertFromClassToService(c).get(id));
//    }

    public ResponseEntity<Response> create(GeneralItem item, Class c) {
        // make sure we got all the data from the client
        try {
            Validations.validate(Regex.FILE_NAME.getRegex(), item.getName());
//            Validations.validate(Regex.ID.getRegex(), item.getParentFolderId().toString());
        } catch (NullPointerException |IllegalArgumentException e) {
            return ResponseEntity.ok().body(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .data("")
                    .message(e.getMessage())
                    .build());
        }

        return ResponseEntity.ok().body(new Response.Builder()
                .status(HttpStatus.OK)
                .data(convertFromClassToService(c).create(item))
                .message("item created successfully")
                .build());
    }

    public ResponseEntity<Response> getPath(GeneralItem item, Class c){
        return ResponseEntity.ok().body(new Response.Builder()
                .status(HttpStatus.OK)
                .message("")
                .data(convertFromClassToService(c).getPath(item))
                .build());
    }

    public ResponseEntity<Response> rename(Long id, String name, Class c) {
        // FIXME: need to validate name using Validations.validate!
        //  it also checks if null and returns an exception so we need to catch it here.
        if (name == null)
            return ResponseEntity.badRequest().body(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("You must include all and exact parameters for such an action: name")
                    .build());

        return ResponseEntity.ok().body(new Response.Builder()
                .status(HttpStatus.OK)
                .message("Successfully renamed to: " + convertFromClassToService(c).rename(id, name))
                .build());
    }

    public ResponseEntity<Response> delete(Long id, Class c) {
        if (id == null)
            return ResponseEntity.badRequest().body(new Response.Builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("I'm sorry. In order for me to delete a document, you need to be more specific about its id... So what is it's id?")
                    .build());
        try {
            convertFromClassToService(c).delete(id);
            return ResponseEntity.ok().body(new Response.Builder()
                    .status(HttpStatus.OK)
                    .message("An item answering to the id:" + id + " has been successfully erased from the database!")
                    .build());
        } catch (FileNotFoundException e) {
            return ResponseEntity.badRequest().body(new Response.Builder().status(HttpStatus.NOT_FOUND).message("You must include all and exact parameters for such an action: name").build());
        }
    }

    public ResponseEntity<Response> relocate(Long newParentId, Long id, Class c) {
        try {
            if (id == null)
                return ResponseEntity.badRequest().body(new Response.Builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("In order to relocate, an ID must be provided!")
                        .build());

            Folder parentFolder = null;
            if (newParentId != null) {
                parentFolder = folderService.findById(newParentId);
            }
            return ResponseEntity.ok().body(new Response.Builder()
                    .message("Affected number of lines: " + convertFromClassToService(c).relocate(parentFolder, id))
                    .status(HttpStatus.OK)
                    .build());

        } catch (FileNotFoundException e) {
            return ResponseEntity.badRequest().body(new Response.Builder()
                    .message(e.getMessage())
                    .status(HttpStatus.NOT_FOUND)
                    .build());
        }
    }

    public ResponseEntity<Response> export(Long documentId) {
        try {
            Document document = documentService.findById(documentId);
            ExportDoc exportDoc = new ExportDoc(document.getName(), document.getContent());
            return ResponseEntity.ok().body(new Response.Builder()
                    .data(exportDoc)
                    .status(HttpStatus.OK)
                    .message("Export performed successfully")
                    .build());

        } catch (FileNotFoundException e) {
            return ResponseEntity.badRequest().body(new Response.Builder()
                    .message(e.getMessage())
                    .status(HttpStatus.NOT_FOUND)
                    .build());
        }
    }

    public ResponseEntity<Response> doesExist(Long id, Class c) {
        if (id == null)
            return ResponseEntity.badRequest().body(new Response.Builder()
                    .message("File of type " + c.getSimpleName() + " with an id of: " + id + " does not exist")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());

        return ResponseEntity.ok().body(new Response.Builder()
                .status(HttpStatus.OK)
                .message("File exists")
                .data(convertFromClassToService(c).doesExist(id))
                .build());
    }


    /**
     * This function gets an item as a parameter and extracts its class in order to return the correct service.
     *
     * @param c - class of folder/document
     * @return the service we need to use according to what file it is.
     */
    private ServiceInterface convertFromClassToService(Class c) {
        if (c.equals(Document.class)) return documentService;
        if (c.equals(Folder.class)) return folderService;
        return null;
    }

    private List<FileRes> convertToFileRes(List<Folder> folders, List<Document> documents) {
        List<FileRes> fileResList = new ArrayList<>();

        for (Folder folder : folders)
            fileResList.add(new FileRes(folder.getName(), folder.getId(), Type.FOLDER, Permission.ADMIN, folder.getUser().getEmail()));

        for (Document document : documents)
            fileResList.add(new FileRes(document.getName(), document.getId(), Type.DOCUMENT, Permission.ADMIN, document.getUser().getEmail()));

        return fileResList;
    }
}