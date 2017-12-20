package org.onosproject.drivers.xmpp.opencontrail;

import org.onosproject.net.DeviceId;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.pubsub.api.PubSubInfoConstructor;
import org.onosproject.pubsub.api.PublishInfo;
import org.dom4j.*;
import org.slf4j.Logger;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import static org.slf4j.LoggerFactory.getLogger;

public class OpenContrailPubSubInfoConstructor extends AbstractHandlerBehaviour implements PubSubInfoConstructor {

    private final Logger logger = getLogger(getClass());

    private static final String BGPVPN_NAMESPACE = "urn:ietf:params:xml:ns:bgp:l3vpn:unicast";

    @Override
    public PublishInfo parsePublishInfo(DeviceId deviceId, Object payload) {
        Element pubsubPayload = (Element) payload;

        String vpnInstanceName = getVpnInstanceName(pubsubPayload);
        String itemId = getItemId(pubsubPayload);

        PublishInfo info = new PublishInfo(deviceId, vpnInstanceName, itemId);
        Element item = ((Element) pubsubPayload.elements().get(0)).createCopy();
        BgpVpnPubSubEntry entry = getBgpVpnPubSubEntry(item);

        info.setPayload(entry);

        return info;
    }

    private String getItemId(Element element) {
        return element.element("item").attribute("id").getValue();
    }

    public Element constructPayload(PublishInfo info) {

        DocumentFactory df = DocumentFactory.getInstance();

        // items element
        Element items = df.createElement("items");
        items.add(Namespace.NO_NAMESPACE);
        items.addAttribute("node", info.getNodeId());

        BgpVpnPubSubEntry bgpEntry = (BgpVpnPubSubEntry) info.getPayload();
        // entry element
        Element entry = df.createElement("entry", BGPVPN_NAMESPACE);
        // <nlri>
        Element nlri = df.createElement("nlri");
        nlri.add(Namespace.NO_NAMESPACE);
        nlri.addAttribute("af", Integer.toString(bgpEntry.getNrliAf()));
        nlri.addText(bgpEntry.getNrliIpAddress());

        // <next-hop>
        Element nextHop = df.createElement("next-hop");
        nextHop.add(Namespace.NO_NAMESPACE);
        nextHop.addAttribute("af", Integer.toString(bgpEntry.getNextHopAf()));
        nextHop.addText(bgpEntry.getNextHopAddress());

        //<label>
        Element label = df.createElement("label");
        label.add(Namespace.NO_NAMESPACE);
        label.addText(Integer.toString(bgpEntry.getLabel()));

        entry.elements().add(nlri);
        entry.elements().add(nextHop);
        entry.elements().add(label);
        Element item = df.createElement("item");
        item.addAttribute("id", bgpEntry.getNrliIpAddress() + ":1:" + bgpEntry.getNextHopAddress());
        item.add(entry);
        items.add(item);

        return items;
    }

    @Override
    public Object constructNotification(Object message) throws UnsupportedOperationException {
        logger.info(message.toString());
        if(message instanceof Element) {
            Packet config = createConfigXmppPacket(message);
            return config;
        } else if (message instanceof PublishInfo) {
            Element notification = constructPayload((PublishInfo) message);
            return notification;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Packet createConfigXmppPacket(Object message) {
        Element config = (Element) message;
        IQ iq = new IQ(IQ.Type.set);
        iq.setChildElement(config);
        return iq;
    }

    private String getVpnInstanceName(Element element) {
        return element.attribute("node").getValue();
    }

    private BgpVpnPubSubEntry getBgpVpnPubSubEntry(Element item) {
        Element entry = ((Element) item.elements().get(0)).createCopy();

        Element nlri = entry.element("nlri");
        String nlriAf = nlri.attributeValue("af");
        String nlriAddress = nlri.attributeValue("address") != null ? nlri.attributeValue("address") : nlri.getText();
        Element nextHop = entry.element("next-hop");
        String nextHopAf = nextHop.attributeValue("af");
        String nextHopAddress = nextHop.attributeValue("address") != null ? nextHop.attributeValue("address") : nextHop.getText();
        Element version = entry.element("version");
        String versionId = version.attributeValue("id");
        Element label = entry.element("label");
        String labelId = label.getText();
        BgpVpnPubSubEntry bgpVpnEntry = new BgpVpnPubSubEntry(Integer.parseInt(labelId),Integer.parseInt(nlriAf), nlriAddress, Integer.parseInt(nextHopAf),
                nextHopAddress, Integer.parseInt(versionId));
        return bgpVpnEntry;
    }

}
