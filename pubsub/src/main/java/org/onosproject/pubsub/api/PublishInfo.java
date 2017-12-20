package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;

public class PublishInfo extends PubSubInfo {

    private Object payload;
    private String itemId;

    public PublishInfo(DeviceId fromDevice, String nodeId, String itemId) {
        super(fromDevice, nodeId);
        this.itemId = itemId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getItemId() {
        return itemId;
    }

    @Override
    public String toString() {
        return "PublishInfo{" +
                "fromDevice=" + fromDevice +
                ", payload=" + payload +
                ", nodeId='" + nodeId + '\'' +
                ", itemId='" + itemId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PublishInfo that = (PublishInfo) o;

        return itemId.equals(that.itemId);
    }

    @Override
    public int hashCode() {
        return itemId.hashCode();
    }
}
