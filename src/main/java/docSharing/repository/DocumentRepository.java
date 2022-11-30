package docSharing.repository;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends CrudRepository<Document, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE Document d SET d.name = ?1 WHERE d.id = ?2")
    int updateName(String name, Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Document d SET d.content = ?1 WHERE d.id = ?2")
    int updateContent(String content, Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Document SET parentFolder = ?1 WHERE id = ?2")
    int updateParentFolderId(Folder parentFolder, Long id);

    @Query("SELECT d FROM Document d WHERE d.parentFolder IS NULL and d.user=?1")
    List<Document> findAllByParentFolderIsNull(User user);


    @Query("SELECT d FROM Document d WHERE d.parentFolder=?1 and d.user=?2")
    List<Document> findAllByUserIdAndParentFolderId(Folder parentFolder, User user);

    @Query("SELECT d.content FROM Document d WHERE d.id=?1")
    String getContentFromDocument(Long documentId);
}