package cn.mythicland.mythiclobby.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManagedNpc {
    private StoredNpc storedNpc;
    private final boolean dynamicDisplay;
    private Object runtimeNpc;
    private List<String> renderedDisplayLines;

    public ManagedNpc(StoredNpc storedNpc, Object runtimeNpc, List<String> renderedDisplayLines, boolean dynamicDisplay) {
        this.storedNpc = storedNpc;
        this.runtimeNpc = runtimeNpc;
        this.renderedDisplayLines = new ArrayList<>(renderedDisplayLines);
        this.dynamicDisplay = dynamicDisplay;
    }

    public StoredNpc getStoredNpc() {
        return storedNpc;
    }

    public void setStoredNpc(StoredNpc storedNpc) {
        this.storedNpc = storedNpc;
    }

    public Object getRuntimeNpc() {
        return runtimeNpc;
    }

    public void setRuntimeNpc(Object runtimeNpc) {
        this.runtimeNpc = runtimeNpc;
    }

    public List<String> getRenderedDisplayLines() {
        return Collections.unmodifiableList(renderedDisplayLines);
    }

    public void setRenderedDisplayLines(List<String> renderedDisplayLines) {
        this.renderedDisplayLines = new ArrayList<>(renderedDisplayLines);
    }

    public boolean hasDynamicDisplay() {
        return dynamicDisplay;
    }
}
