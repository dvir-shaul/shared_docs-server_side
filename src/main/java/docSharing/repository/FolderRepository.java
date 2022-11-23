package docSharing.repository;

import docSharing.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    Optional<Folder> findById(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Folder f " + "SET f.name = ?1 WHERE f.id = ?2")
    int updateName(String name, Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Folder SET parentFolder = ?1 WHERE id = ?2")
    int updateParentFolderId(Folder parentFolder, Long id);

    List<Folder> findAllByParentFolderId(Long parentFolderId);
}
