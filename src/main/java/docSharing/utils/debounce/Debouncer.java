package docSharing.utils.debounce;

import docSharing.entity.Log;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Debouncer<T> {
    private final ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<Long, TimerTask> delayedMap = new ConcurrentHashMap<>();
    private final Callback callback;
    private final int interval;

    public Debouncer(Callback c, int interval) {
        this.callback = c;
        this.interval = interval;
    }

    public void call(Log log) {
        TimerTask task = new TimerTask(log);

        TimerTask prev;
        do {
            prev = delayedMap.putIfAbsent(log.getUser().getId(), task);
            if (prev == null)
                schedule.schedule(task, interval, TimeUnit.MILLISECONDS);
        } while (prev != null && !prev.extend()); // Exit only if new task was added to map, or existing task was extended successfully
    }

    // The task that wakes up when the wait time elapses
    private class TimerTask implements Runnable {
        private final Log log;
        private long dueTime;
        private final Object lock = new Object();

        public TimerTask(Log log) {
            this.log = log;
            extend();
        }

        public boolean extend() {
            synchronized (lock) {
                if (dueTime < 0) // Task has been shutdown
                    return false;
                dueTime = System.currentTimeMillis() + interval;
                return true;
            }
        }

        public void run() {
            synchronized (lock) {
                long remaining = dueTime - System.currentTimeMillis();
                if (remaining > 0) { // Re-schedule task
                    schedule.schedule(this, remaining, TimeUnit.MILLISECONDS);
                } else { // Mark as terminated and invoke callback
                    dueTime = -1;
                    try {
                        callback.call(log);
                    } finally {
                        delayedMap.remove(log.getUser().getId());
                    }
                }
            }
        }
    }
}
