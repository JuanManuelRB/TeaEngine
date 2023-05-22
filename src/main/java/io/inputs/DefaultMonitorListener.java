package io.inputs;

public class DefaultMonitorListener extends MonitorListener {
    private static DefaultMonitorListener instance;

    private DefaultMonitorListener() {}

    public static DefaultMonitorListener get() {
        if (instance == null)
            instance = new DefaultMonitorListener();

        return instance;
    }

    @Override
    public long monitor(int monitor) {
        return monitors.get(monitor);
    }

    @Override
    public void monitorCallback(long monitorID, int monitor) {
        monitors.put(monitor, monitorID);
    }
}
