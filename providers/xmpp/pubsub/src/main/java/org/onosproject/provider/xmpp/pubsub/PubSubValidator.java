package org.onosproject.provider.xmpp.pubsub;

import org.dom4j.Element;
import org.onosproject.pubsub.api.PubSubError;
import org.xmpp.packet.PacketError;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validates incoming PubSub XMPP messages.
 */
public class PubSubValidator {

    public static String checkNotEmpty(String value) {
        if(value.isEmpty())
            throw new IllegalArgumentException();
        else
            return value;
    }

    public static Element validateRetract(Element retract) throws PubSubValidationException {
        try {
            checkNotEmpty(retract.attribute("node").getValue());
        } catch (Exception e) {
            throw new PubSubValidationException(PacketError.Condition.bad_request, "nodeid-required");
        }
        try {
            checkNotEmpty(retract.element("item").attribute("id").getValue());
        } catch (Exception e) {
            throw new PubSubValidationException(PacketError.Condition.bad_request, "item-required");
        }
        return retract;
    }

    public static Element validatePublish(Element publish) throws PubSubValidationException {
        try {
            checkNotEmpty(publish.attribute("node").getValue());
        } catch (Exception e) {
            throw new PubSubValidationException(PacketError.Condition.bad_request, "nodeid-required");
        }
        try {
            checkNotNull(publish.element("item").element("entry"));
        } catch (Exception e) {
            throw new PubSubValidationException(PacketError.Condition.bad_request, "payload-required");
        }
        return publish;
    }

}
