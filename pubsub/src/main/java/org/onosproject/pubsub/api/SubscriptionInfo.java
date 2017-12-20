package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;

public class SubscriptionInfo extends PubSubInfo {

    public SubscriptionInfo(DeviceId fromDevice, String nodeId) {
        super(fromDevice, nodeId);
    }

    @Override
    public String toString() {
        return "SubscriptionInfo{" +
                "fromDevice=" + fromDevice +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }
}
