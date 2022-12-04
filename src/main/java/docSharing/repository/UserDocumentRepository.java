package docSharing.repository;

import docSharing.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {
    @Query("SELECT u FROM UserDocument u WHERE (u.document = ?1 and u.user = ?2) ")
    Optional<UserDocument> find(Document doc, User user);


    @Query("SELECT u FROM UserDocument u WHERE u.user = ?1")
    List<UserDocument> findByUser(User user);

    @Query("SELECT u FROM UserDocument u WHERE u.document=?1")
    List<UserDocument> findAllUsersInDocument(Document document);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserDocument urd SET urd.permission = ?1 WHERE (urd.document = ?2 and urd.user = ?3)")
    int updatePermission(Permission permission, Document document, User user);

    @Transactional
    @Modifying
    @Query("DELETE UserDocument urd WHERE urd.document = ?1")
    int deleteDocument(Document document);



    @Transactional
    @Modifying
    @Query("DELETE UserDocument urd WHERE urd.user = ?1 AND urd.document = ?2")
    int deleteUserFromDocument(User user, Document document);


}
