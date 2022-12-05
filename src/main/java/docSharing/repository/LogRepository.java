package docSharing.repository;

import docSharing.entity.Document;
import docSharing.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface LogRepository extends JpaRepository<Log, Long> {
    @Transactional
    @Modifying
    @Query("DELETE Log l WHERE l.document = ?1")
    int deleteByDocument(Document document);
}
