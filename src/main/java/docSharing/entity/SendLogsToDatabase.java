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
        System.out.println("TIME IS UP for userId:" + o + ", stored data: " + logsMap.get(o));
        logsMap.remove(o);

        // once time is up:
        //
        //
        //
        //
    }
}
