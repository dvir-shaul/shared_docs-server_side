package docSharing.service;

import docSharing.entity.Document;
import docSharing.entity.Folder;
import docSharing.entity.Log;
import docSharing.entity.User;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.LogRepository;
import docSharing.repository.UserRepository;
import docSharing.utils.logAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LogServiceTest {
    @InjectMocks
    LogService logService;
    @Mock
    LogRepository logRepository;
    @Mock
    private FolderRepository folderRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private UserRepository userRepository;
    private Log log;
    private Folder folder;
    private User user;
    private Document document;

    @BeforeEach
    public void setup() throws AccountNotFoundException {
        user = User.createUser("test@test.com", "abed123", "test user");
        user.setId(10L);
        userRepository.save(user);
        folder = Folder.createFolder("test", null, user);
        folder.setId(22L);
        folderRepository.save(folder);
        document = Document.createDocument(user,"newDoc",folder,"");
        document.setId(4L);
        documentRepository.save(document);
        log = new Log(user,document,0,"a", logAction.INSERT, LocalDateTime.now());
        log.setId(20L);
    }

    @Test
    public void givenLogInDB_whenInsertNewLog_thenOk() {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        logService.updateLogs(log);
        assertEquals(LogService.chainedLogs.get(document.getId()).size(),1);
    }
    @Test
    public void givenLogInDB_newLogActionNull_assertThrow() {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        logService.updateLogs(log);
        log.setAction(null);
        assertThrows(NullPointerException.class,
                ()->logService.updateLogs(log));
    }
    @Test
    public void givenLogInDB_newLogEditDatNull_assertDoesNotThrow() {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        logService.updateLogs(log);
        log.setLastEditDate(null);
        assertDoesNotThrow(()->logService.updateLogs(log));
    }
    @Test
    public void givenLogInDB_newLogOffsetNull_assertThrow() {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        logService.updateLogs(log);
        log.setOffset(null);
        assertThrows(NullPointerException.class,
                ()->logService.updateLogs(log));
    }
    @Test
    public void givenLogInDB_newLogDocumentNull_assertThrow() {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        logService.updateLogs(log);
        log.setDocument(null);
        assertThrows(NullPointerException.class,
                ()->logService.updateLogs(log));
    }
    @Test
    public void givenLogInDB_newLogUserNull_assertThrow() {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        logService.updateLogs(log);
        log.setUser(null);
        assertThrows(NullPointerException.class,
                ()->logService.updateLogs(log));
    }


    @Test
    public void givenLogInDB_whenInsertNewLog_updateOffset() {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Log newLog = Log.copy(log);
        newLog.setAction(logAction.DELETE);
        newLog.setOffset(1);
        logService.updateLogs(newLog);
        assertEquals(LogService.chainedLogs.get(document.getId()).size(),1);

    }
    @Test
    public void givenLogInDB_whenInsertNewLog_concatenateLogs() {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        Log newLog = Log.copy(log);
        newLog.setOffset(1);
        logService.updateLogs(newLog);
        logService.updateLogs(newLog);
        assertEquals(LogService.chainedLogs.get(document.getId()).size(),1);

    }
    @Test
    public void givenLogInDB_whenInsertNewLog_truncateLogs() throws InterruptedException {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        Log newLog = Log.copy(log);
        newLog.setOffset(1);
        logService.updateLogs(newLog);
        newLog.setAction(logAction.DELETE);
        logService.updateLogs(newLog);
        assertEquals(LogService.chainedLogs.size(),1);
    }
    @Test
    public void givenLogInDB_whenInsertNewLog_chainNoSequel() throws InterruptedException {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        Log newLog = Log.copy(log);
        logService.updateLogs(newLog);
        newLog.setOffset(10);
        logService.updateLogs(newLog);
        assertEquals(LogService.chainedLogs.get(document.getId()).size(),1);
    }
    @Test
    public void givenLogInDB_whenInsertNewLog_logInMiddle() throws InterruptedException {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        Log newLog = Log.copy(log);
        logService.updateLogs(newLog);
        newLog.setOffset(1);;
        logService.updateLogs(newLog);
        newLog.setOffset(0);
        newLog.setData("b");
        logService.updateLogs(newLog);
        newLog.setOffset(1);
        logService.updateLogs(newLog);
    }
    @Test
    public void givenLogInDB_whenInsertNewLog_differentUser() throws InterruptedException {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        Log newLog = Log.copy(log);
        logService.updateLogs(newLog);
        newLog.setOffset(1);
        user.setId(2L);
        newLog.setUser(user);
        logService.updateLogs(newLog);
        newLog.setOffset(0);
        newLog.setData("b");
        logService.updateLogs(newLog);
        newLog.setOffset(1);
        logService.updateLogs(newLog);
        assertEquals(2,LogService.chainedLogs.get(document.getId()).size());
    }

    @Test
    public void givenLogInDB_whenInsertNewLog_deleteFromDifferentUserThenInsert() throws InterruptedException {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        Log newLog = Log.copy(log);
        newLog.setAction(logAction.DELETE);
        logService.updateLogs(newLog);
        logService.updateLogs(log);
        assertEquals(1,LogService.chainedLogs.get(document.getId()).size());

    }

    @Test
    public void givenLogInDB_whenInsertNewLog_checkMap() throws InterruptedException {
        given(folderRepository.findById(folder.getId())).willReturn(null);
        Folder folder2 = Folder.createFolder("test", folder, user);
        given(folderRepository.save(folder2)).willReturn(folder);
        Map<Long, Log> documentLogs = new HashMap<>();
        documentLogs.put(1L,log);
        Log newLog = Log.copy(log);
        newLog.setAction(logAction.DELETE);
        logService.updateLogs(newLog);
        logService.updateLogs(log);
        assertEquals(1,LogService.chainedLogs.get(document.getId()).size());
    }
}