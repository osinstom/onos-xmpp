package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;

public class PubSubInfo {

    protected DeviceId fromDevice;
    protected String nodeId;

    public PubSubInfo(DeviceId fromDevice, String nodeId) {
        this.fromDevice = fromDevice;
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public DeviceId getFromDevice() {
        return fromDevice;
    }

    @Override
    public String toString() {
        return "PubSubInfo{" +
                "fromDevice=" + fromDevice +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }
}
