package docSharing.repository;

import docSharing.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface LogRepository extends JpaRepository<Log, Long> {
}
