package docSharing.utils.debounce;

import docSharing.entity.Log;
import docSharing.repository.LogRepository;

public interface Callback {
    public void call(Log log);
}