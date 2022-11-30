package docSharing.repository;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE Folder f " + "SET f.name = ?1 WHERE f.id = ?2")
    int updateName(String name, Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Folder SET parentFolder = ?1 WHERE id = ?2")
    int updateParentFolderId(Folder parentFolder, Long id);

//    @Query("SELECT f FROM Folder f WHERE f.parentFolder IS NULL")
    @Query("SELECT f FROM Folder f WHERE f.parentFolder IS NULL and f.user=?1")
    List<Folder> findAllByParentFolderIsNull(User user);

    @Query("SELECT f FROM Folder f WHERE f.parentFolder=?1 and f.user=?2")
    List<Folder> findAllByParentFolderIdAndUserId(Folder parentFolder, User user);


    List<Folder> findAllByParentFolderId(Long parentFolderId);
}
