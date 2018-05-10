package org.openhab.binding.isy.handler;

public interface IsyThingHandler {

    public void handleUpdate(String control, String action, String node);
}
