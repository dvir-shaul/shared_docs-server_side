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
    Optional<Document> findById(Long id);

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




}