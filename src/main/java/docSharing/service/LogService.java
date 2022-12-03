package docSharing.service;

import docSharing.entity.Log;
import docSharing.entity.SendLogsToDatabase;
import docSharing.repository.LogRepository;
import docSharing.utils.debounce.Debouncer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogService {

    private static Logger logger = LogManager.getLogger(LogService.class.getName());

    @Autowired
    LogRepository logRepository;

    static Map<Long, Map<Long, Log>> chainedLogs = new HashMap<>(); // logs history until storing to database
    static List<Log> unsavedLogs = new ArrayList<>();

    Debouncer debouncer = new Debouncer<Log>(new SendLogsToDatabase(chainedLogs, unsavedLogs), 3000);

    @Scheduled(fixedDelay = 5 * 1000)
    public void updateDatabaseWithNewContent() {
        // keep it in a while loop because this list can be edited in any second, so need to check if still contains anything
        while (!unsavedLogs.isEmpty()) {
            Log log = unsavedLogs.remove(0);
            logRepository.save(log);  // TODO: save to the database instead of console logging it.
            System.out.println("New log has been saved in the database: " + log);
        }
    }
    /**
     * This function called every time we get a new log,
     * checks if a new data that was written to document was written before
     * the logs that are online in chainedLogs map, if it ws before we will update the offsets accordingly.
     *
     * @param log - changes from
     */
    public void updateLogs(Log log) {
        logger.info("in LogService -> updateLogs");

        debouncer.call(log);
        Map<Long, Log> documentLogs = chainedLogs.get(log.getDocument().getId());

        if (documentLogs == null) {
            documentLogs = new HashMap<>();
            chainedLogs.put(log.getDocument().getId(), documentLogs);
        }
        // update logs
        chainLogs(documentLogs, log);
        updateLogsOffset(documentLogs, log);
    }

    /**
     * this function goal is to connect 2 logs to 1 log according to the user that was written the new data
     * and the offset that the data was entered.
     * checking order is:
     * 1. if such a log doesn't exist in the cache, create a new entry for it in the map
     * 2. if the new log is not a sequel to current log, store the current one in the db and start a new one instead.
     * 3. if the current log was attempting to delete and how we want to insert,push the deleted one and create a new log
     * 4. if the new log is in the middle of the current log, it must be concatenated.
     * saves the concatenated logs to chainedLogs map.
     *
     * @param newLog - new data that needed to chain to old log.
     */
    private void chainLogs(Map<Long, Log> documentLogs, Log newLog) {
        logger.info("in LogService -> chainLogs");

        // if such a log doesn't exist in the cache, create a new entry for it in the map
        if (!documentLogs.containsKey(newLog.getUser().getId())) {
            documentLogs.put(newLog.getUser().getId(), newLog);
            return;
        }

        Log currentLog = documentLogs.get(newLog.getUser().getId());
        // if the new log is not a sequel to current log, store the current one in the db and start a new one instead.
        if (currentLog.getOffset() - 1 > newLog.getOffset() || (currentLog.getOffset() + currentLog.getData().length() + 1) < newLog.getOffset()) {
            currentLog.getUser().addLog(currentLog);
            currentLog.getDocument().addLog(currentLog);
            logRepository.save(currentLog);
            documentLogs.put(currentLog.getUser().getId(), newLog);
        }

        // if the new log is in the middle of the current log, it must be concatenated.
        else {
            if (currentLog.getAction().equals("insert") && newLog.getAction().equals("delete")) {
                currentLog.setData(truncateLogs(currentLog, newLog));
                // if the current log was attempting to delete, and now we want to insert, push the deleted log and create a new log
            } else if (currentLog.getAction().equals("delete") && newLog.getAction().equals("insert")) {
                currentLog.getUser().addLog(currentLog);
                currentLog.getDocument().addLog(currentLog);
                currentLog.setLastEditDate(newLog.getCreationDate());
                logRepository.save(currentLog);
                documentLogs.put(currentLog.getUser().getId(), newLog);
                return;

            } else if (newLog.getAction().equals(currentLog.getAction())) {
                currentLog.setData(concatenateLogs(currentLog, newLog));
            }

            documentLogs.put(currentLog.getUser().getId(), currentLog);
        }
    }

    /**
     * This function called every time we get a new log,
     * checks if a new data that was written to document was written before
     * the logs that are online in chainedLogs map, if it ws before we will update the offsets accordingly.
     *
     * @param log - changes from
     */
    private void updateLogsOffset(Map<Long, Log> documentLogs, Log log) {
        logger.info("in LogService -> updateLogsOffset");

        documentLogs.replaceAll((userId, _log) -> {
            // create a copy of the log in case we need to modify it
            Log tempLog = Log.copy(_log);
            // if the offset is before other logs' offset, decrease its offset by the length of the log
            if (log.getAction().equals("delete") && log.getOffset() <= _log.getOffset()) {
                tempLog.setOffset(_log.getOffset() - log.getData().length());
            }

            // make sure not to change the current user's log
            if (!log.getUser().getId().equals(userId)) {


                // if the offset is before other logs' offset, increase its offset by the length of the log
                if (log.getAction().equals("insert") && log.getOffset() <= _log.getOffset()) {
                    tempLog.setOffset(_log.getOffset() + log.getData().length());
                }

                // if the offset is in the middle of the logs' offset, split it to two, commit the first one and store only the second part
                else if (log.getOffset() > _log.getOffset() && log.getOffset() < _log.getOffset() + _log.getData().length()) {
                    // cut the _log to half
                    Log firstPartOfLog = Log.copy(_log);
                    firstPartOfLog.setData(_log.getData().substring(0, log.getOffset()));
                    firstPartOfLog.setLastEditDate(_log.getCreationDate());
                    firstPartOfLog.getUser().addLog(firstPartOfLog);
                    firstPartOfLog.getDocument().addLog(firstPartOfLog);
                    // store the first half in the database. for now just print it
                    logRepository.save(firstPartOfLog);

                    // keep the second half in the cache
                    // there's not a real need to store it in a different log, but for simplicity...
                    Log secondPartOfLog = Log.copy(_log);
                    secondPartOfLog.setOffset(log.getOffset() + 1);
                    secondPartOfLog.setData(_log.getData().substring(log.getOffset()));
                    secondPartOfLog.setLastEditDate(_log.getCreationDate());

                    // firstPartLog.send to DB!!!!
                    tempLog = secondPartOfLog;
                }

                // if the offset is after the log's data, skip it because it doesn't matter.
            }
            return tempLog;
        });
    }

    /**
     * this function gets called from chainLogs when the logs are needed to truncate.
     * change the newLog offset to put him according to the current log.
     * it comes from that idea that we want to make the currentLog offset as our absolute zero.
     *
     * @param currentLog - log that is in the cached map of logs.
     * @param newLog     - log with changes from the client.
     * @return - updated content that was truncated from the 2 logs we have.
     */
    private String truncateLogs(Log currentLog, Log newLog) {
        logger.info("in LogService -> truncateLogs");

        newLog.setOffset(newLog.getOffset() - (currentLog.getOffset()));
        if (currentLog.getData() == null) currentLog.setData("");
        return DocumentService.truncateString(currentLog.getData(), newLog);
    }

    /**
     * this function gets called from chainLogs when the logs are needed to concatenate.
     * change the newLog offset to put him according to the current log.
     * it comes from that idea that we want to make the currentLog offset as our absolute zero.
     *
     * @param currentLog - log that is in the cached map of logs.
     * @param newLog     - log with changes from the client.
     * @return - updated content that was concatenated from the 2 logs we have.
     */
    private String concatenateLogs(Log currentLog, Log newLog) {
        logger.info("in LogService -> concatenateLogs");

        int diff = Math.max(newLog.getOffset() - currentLog.getOffset(), 0);
        newLog.setOffset(diff);
        if (currentLog.getData() == null) currentLog.setData(""); // CONSULT: shouldn't happen, so why did I write it?
        return DocumentService.concatenateStrings(currentLog.getData(), newLog);
    }

}


