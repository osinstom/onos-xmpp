package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;

public class Retract extends PubSubInfo {

    private String itemId;

    public Retract(DeviceId fromDevice, String nodeId, String itemId) {
        super(fromDevice, nodeId);
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }

    @Override
    public String toString() {
        return "Retract{" +
                "fromDevice=" + fromDevice +
                ", itemId='" + itemId + '\'' +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }
}
