package docSharing.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import docSharing.repository.DocumentRepository;
import docSharing.repository.UserRepository;
import docSharing.utils.ExceptionMessage;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "Log")
@Table(name = "logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime creationDate = LocalDateTime.now();
    @Column(name = "edited_on", nullable = false, updatable = false)
    private LocalDateTime lastEditDate;
    private Integer offset;
    private String action;
    private String data;
    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;



    public Log(User user, Document document, int offset, String data, String action) throws AccountNotFoundException {
        this.user = user;
        this.document = document;
        this.offset = offset;
        this.data = data;
        this.action = action;
    }

    public static Log copy(Log log) {
        Log tempLog = new Log();
        //tempLog.setUserId(log.getUserId());
        tempLog.setUser(log.getUser());
        tempLog.setOffset(log.getOffset());
        tempLog.setCreationDate(log.getCreationDate());
        //tempLog.setDocumentId(log.getDocumentId());
        tempLog.setDocument(log.getDocument());
        tempLog.setData(log.getData());
        tempLog.setAction(log.getAction());
        return tempLog;
    }
}
