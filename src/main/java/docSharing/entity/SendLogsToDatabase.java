package docSharing.entity;

import docSharing.utils.debounce.Callback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SendLogsToDatabase implements Callback {

    private Map<Long, Map<Long, Log>> logsMap;
    static List<Log> unsavedLogs;


    public SendLogsToDatabase(Map<Long, Map<Long, Log>> logsMap, List<Log> unsavedLogs) {
        this.logsMap = logsMap;
        this.unsavedLogs = unsavedLogs;
    }

    @Override
    public void call(Log log) {
        Log currentLog = logsMap.get(log.getDocument().getId()).get(log.getUser().getId());
        currentLog.setLastEditDate(LocalDateTime.now().minusSeconds(3));
        // if the log's data is not empty or null, store it in the database
        if (currentLog.getData() != null || currentLog.getData().length() > 0) {
            // store the current log in a list that pushes all its data to the database one in a few seconds
            unsavedLogs.add(currentLog);
            // finally, remove the log from the map and clear its cache
            logsMap.get(currentLog.getDocument().getId()).remove(currentLog.getUser().getId());
        }
    }
}
