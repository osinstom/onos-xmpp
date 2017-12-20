package org.onosproject.pubsub.api;

import org.onosproject.event.AbstractEvent;

public class PubSubEvent extends AbstractEvent<PubSubEvent.Type, Object> {

    public enum Type {
        NEW_SUBSCRIPTION,
        DELETE_SUBSCRIPTION,
        PUBLISH,
        RETRACT
    }

    public PubSubEvent(Type type, Object object) {
        super(type, object);
    }
}