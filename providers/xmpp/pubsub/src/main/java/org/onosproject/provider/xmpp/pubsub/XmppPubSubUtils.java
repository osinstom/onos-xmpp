package org.onosproject.provider.xmpp.pubsub;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.util.UserDataElement;
import org.onosproject.net.DeviceId;
import org.onosproject.pubsub.api.*;
import org.onosproject.xmpp.XmppDevice;
import org.onosproject.xmpp.XmppDeviceId;
import org.slf4j.Logger;
import org.xmpp.packet.*;

import javax.naming.Name;
import java.util.IllegalFormatException;
import java.util.List;

import static org.onosproject.provider.xmpp.pubsub.PubSubValidator.validateRetract;
import static org.slf4j.LoggerFactory.getLogger;

public class XmppPubSubUtils {

    private static final Logger logger = getLogger(XmppPubSubUtils.class);

    private static final String PUBSUB_NS = "http://jabber.org/protocol/pubsub";
    private static final String PUBSUB_EVENT_NS = "http://jabber.org/protocol/pubsub#event";
    private static final String PUBSUB_ELEMENT = "pubsub";


    public static XmppDeviceId getXmppDeviceId(DeviceId device) {
        String strJid = device.uri().getSchemeSpecificPart();
        JID jid = new JID(strJid);
        XmppDeviceId xmppDeviceId = new XmppDeviceId(jid);
        return xmppDeviceId;
    }

    enum Method {
        SUBSCRIBE,
        UNSUBSCRIBE,
        PUBLISH,
        RETRACT
    }

    public static SubscriptionInfo parseSubscription(IQ iq) {
        String nodeId = XmppPubSubUtils.getChildElement(iq.getChildElement())
                .attribute("node").getValue();

        SubscriptionInfo subscriptionInfo = new SubscriptionInfo(DeviceId.deviceId(XmppDeviceId.uri(iq.getFrom())), nodeId);
        return subscriptionInfo;
    }

    public static PublishInfo parsePublish(IQ iq) {
        String domain = iq.getFrom().getDomain();
        PubSubInfoConstructor pubSubInfoConstructor = PubSubConstructorFactory.getInstance().getPubSubInfoConstructor(domain);

        Element publish = getElementBody(iq);
        DeviceId deviceId = DeviceId.deviceId(XmppDeviceId.uri(iq.getFrom()));
        PublishInfo publishInfo = pubSubInfoConstructor.parsePublishInfo(deviceId, publish);

        return publishInfo;
    }

    public static Retract parseRetract(IQ iq) throws PubSubValidationException{
        Element body = getElementBody(iq);
        validateRetract(body);
        DeviceId deviceId = DeviceId.deviceId(XmppDeviceId.uri(iq.getFrom()));

        String nodeId = body.attribute("node").getValue();
        String itemId = body.element("item").attribute("id").getValue();

        Retract retract = new Retract(deviceId, nodeId, itemId);
        return retract;
    }

    private static Element getElementBody(IQ iq) {
        return (Element) iq.getChildElement().elements().get(0);
    }

    public static Packet constructXmppNotification(XmppDeviceId xmppDeviceId, Object message)
            throws IllegalFormatException {
        Packet packet = null;
        if(message instanceof Retract) {
            packet = constructRetractNotification( (Retract) message);
        } else {
            packet = constructNotification(xmppDeviceId, message);
        }

        return packet;
    }

    private static Packet constructRetractNotification(Retract retractInfo) {
        Message message = new Message();
        DocumentFactory df = DocumentFactory.getInstance();
        Element event = df.createElement("event", PUBSUB_EVENT_NS );
        // items element
        Element items = df.createElement("items");
        items.addAttribute("node", retractInfo.getNodeId());
        Element retract = df.createElement("retract");
        retract.addAttribute("id", retractInfo.getItemId());
        items.elements().add(retract);
        event.elements().add(items);

        // event element
        message.getElement().elements().add(event);
        return message;
    }

    public static PacketError.Condition getConditionForPubSubError(PubSubError error) {
        if(error.getErrorType().equals(PubSubError.ErrorType.ITEM_NOT_FOUND)) {
            return PacketError.Condition.item_not_found;
        } else if (error.getErrorType().equals(PubSubError.ErrorType.NOT_SUBSCRIBED)) {
            return PacketError.Condition.unexpected_request;
        } else if (error.getErrorType().equals(PubSubError.ErrorType.INVALID_PAYLOAD)) {
            return PacketError.Condition.bad_request;
        }
        return null;
    }

    private static Packet constructNotification(XmppDeviceId xmppDeviceId, Object message) {
        JID jid = xmppDeviceId.getJid();
        String domain = jid.getDomain();

        PubSubInfoConstructor constructor = PubSubConstructorFactory.getInstance().getPubSubInfoConstructor(domain);
        Packet packet = null;
        try {
            Object payload = constructor.constructNotification(message);
            if(payload instanceof Element) {
                packet = constructXmppEventNotificationMessage((Element) payload);
            } else if (payload instanceof Packet) {
                packet = (Packet) payload;
            } else {
                packet = constructXmppEventNotificationMessage((PublishInfo) message);
            }
        } catch(UnsupportedOperationException e) {
            // if UnsupportedOperationException is received, use default construction method
            packet = constructXmppEventNotificationMessage((PublishInfo) message);
        }
        return packet;
    }

    public static Packet constructXmppEventNotificationMessage(Element payload) {
        Message message = new Message();
        DocumentFactory df = DocumentFactory.getInstance();
        Element event = df.createElement("event", PUBSUB_EVENT_NS );
        event.elements().add(payload);
        // event element
        message.getElement().elements().add(event);

        return message;
    }


    public static Packet constructXmppEventNotificationMessage(PublishInfo publishInfo) {

        DocumentFactory df = DocumentFactory.getInstance();
        // items element
        Element items = df.createElement("items");
        items.addAttribute("node", publishInfo.getNodeId());

        Element item = ((Element) publishInfo.getPayload()).createCopy();
        item.setQName(QName.get(item.getName(), Namespace.NO_NAMESPACE, item.getQualifiedName()));

        items.elements().add(item);

        Packet message = constructXmppEventNotificationMessage(items);

        return message;
    }

    public static boolean isPubSub(IQ iqEvent) {
        Element element = iqEvent.getElement().element(PUBSUB_ELEMENT);
        return element != null && element.getNamespace() != null && element.getNamespace().getURI().equals(PUBSUB_NS);
    }

    public static Method getMethod(IQ iq) {
        Element pubsubElement = iq.getChildElement();
        Element methodElement = getChildElement(pubsubElement);
        String name = methodElement.getName();
        switch(name) {
            case "subscribe":
                return Method.SUBSCRIBE;
            case "unsubscribe":
                return Method.UNSUBSCRIBE;
            case "publish":
                return Method.PUBLISH;
            case "retract":
                return Method.RETRACT;
        }

        return null;
    }

    public static Element getChildElement(Element element) {
        Element child = (Element) element.elements().get(0); // the first element is related to pubsub operation
        return child;
    }
}
