package docSharing.entity;

import docSharing.repository.LogRepository;
import docSharing.utils.debounce.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

//@Component
public class SendLogsToDatabase implements Callback {

    private Map<Long, Map<Long, Log>> logsMap;

    public SendLogsToDatabase(Map<Long, Map<Long, Log>> logsMap) {
        this.logsMap = logsMap;
    }

//    @Autowired
//    LogRepository logRepository;

    @Override
    public void call(Log log,LogRepository logRepository) {

        logsMap.get(log.getDocument().getId()).get(log.getUser().getId()).setLastEditDate(log.getLastEditDate());
       logRepository.save(logsMap.get(log.getDocument().getId()).get(log.getUser().getId()));
        // if the log's data is not empty or null, store it in the database
        System.out.println("data: "+logsMap.get(log.getDocument().getId()).get(log.getUser().getId()).getData());
        if (logsMap.get(log.getDocument().getId()).get(log.getUser().getId()).getData() != null || logsMap.get(log.getDocument().getId()).get(log.getUser().getId()).getData().length() > 0)
            System.out.println("logsMap: "+logsMap);
            // finally, remove the log from the map and clear its cache
            //  logsMap.get(log.getDocument().getId()).remove(log.getUser().getId());

    }
}
