package docSharing.repository;

import docSharing.entity.Document;
import docSharing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE User u " + "SET u.isActivated = ?1 WHERE u.id = ?2")
    int updateIsActivated(Boolean bool, Long id);
}
