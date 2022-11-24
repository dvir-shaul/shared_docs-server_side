package docSharing.entity;

import docSharing.utils.debounce.Callback;

import java.util.List;
import java.util.Map;

public class SendLogsToDatabase implements Callback {

    private Map<Long, Log> changesMap;

    public SendLogsToDatabase(Map<Long, Log> changesMap) {
        this.changesMap = changesMap;
    }

    @Override
    public void call(Object o) {
        System.out.println("TIME IS UP for userId:" + o);
        System.out.println(changesMap.get(o));
        changesMap.remove(o);

        // once time is up:
            //
            //
            //
            //
    }
}
