package docSharing.entity;

import docSharing.utils.debounce.Callback;

import java.util.List;
import java.util.Map;

public class SendLogsToDatabase implements Callback {

    private Map<Long, Log> logsMap;

    public SendLogsToDatabase(Map<Long, Log> logsMap) {
        this.logsMap = logsMap;
    }

    @Override
    public void call(Object o) {
        Log log = logsMap.get(o);
        System.out.println("TIME IS UP for userId:" + o + ", stored data: " + log);

        // if the log's data is not empty or null, store it in the database
        if (log.getData() != null || log.getData().length() > 0)
            System.out.println("This log is not empty! " + log.getData());

        // finally, remove the log from the map and clear its cache
        logsMap.remove(o);
    }
}
