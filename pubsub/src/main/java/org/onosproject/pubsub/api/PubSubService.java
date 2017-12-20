package org.onosproject.pubsub.api;

import org.onosproject.event.ListenerService;
import org.onosproject.net.DeviceId;

import java.util.List;

public interface PubSubService extends ListenerService<PubSubEvent, PubSubListener> {

    /**
     * Notifying Provider about PubSub event notification for a list of devices.
     * @param devices
     * @param notificationInfo
     * @throws IllegalArgumentException
     */
    void sendEventNotification(List<DeviceId> devices, Object notificationInfo) throws IllegalArgumentException;


    /**
     * Notifying Provider about PubSub event notification for a particular device.
     * @param deviceId
     * @param notificationInfo
     * @throws IllegalArgumentException
     */
    void sendEventNotification(DeviceId deviceId, Object notificationInfo) throws IllegalArgumentException;

}
