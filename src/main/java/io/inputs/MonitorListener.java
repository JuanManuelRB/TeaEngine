package io.inputs;

import java.util.TreeMap;

public abstract class MonitorListener {
    TreeMap<Integer, Long> monitors;
    public final long primaryMonitor() {
        return monitor(1);
    }
    public abstract long monitor(int monitor);

    public abstract void monitorCallback(long monitorID, int monitor);
}
