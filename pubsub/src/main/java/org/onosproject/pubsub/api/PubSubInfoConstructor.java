package org.onosproject.pubsub.api;

import org.onosproject.net.DeviceId;
import org.onosproject.net.driver.HandlerBehaviour;


public interface PubSubInfoConstructor extends HandlerBehaviour {

    PublishInfo parsePublishInfo(DeviceId device, Object payload);

    Object constructNotification(Object message) throws UnsupportedOperationException;

}
