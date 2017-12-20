package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.Provider;
import org.onosproject.pubsub.impl.PubSubManager;

import java.util.List;

public interface PubSubProvider extends Provider {

    /**
     * Sending PubSub notification to devices. By default, the message should be in format of PublishInfo.
     * However, it may be customized and any kind of object may be sent to network. Custom notification
     * should be handled by PubSubInfoConstructor driver.
     * @param devices
     * @param message
     */
    void sendNotification(List<DeviceId> devices, Object message);

    /**
     * Sending PubSub notification to particular device.
     * @param device
     * @param message
     */
    void sendNotification(DeviceId device, Object message);



}
