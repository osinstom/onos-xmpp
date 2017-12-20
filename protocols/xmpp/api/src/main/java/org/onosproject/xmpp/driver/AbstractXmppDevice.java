package org.onosproject.xmpp.driver;


import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import org.dom4j.Document;
import org.dom4j.Element;
import org.onosproject.xmpp.XmppConstants;
import org.onosproject.xmpp.XmppDeviceId;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.xmpp.XmppDeviceListener;
import org.onosproject.xmpp.stream.*;
import org.onosproject.xmpp.stream.StreamError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.*;


/**
 * Abstraction of XMPP client.
 */
public abstract class AbstractXmppDevice extends AbstractHandlerBehaviour implements XmppDeviceDriver  {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected Channel channel;
    protected XmppDeviceId deviceId;
    protected XmppDeviceManager manager;

    @Override
    public void init(XmppDeviceId xmppDeviceId) {
        this.deviceId = xmppDeviceId;
    }

    @Override
    public void setChannel(io.netty.channel.Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public void setManager(XmppDeviceManager manager) {
        if(this.manager==null) {
            this.manager = manager;
        }
    }

    @Override
    public void connectDevice() {
        this.manager.addConnectedDevice(deviceId, this);
    }

    @Override
    public void disconnectDevice() {
        this.channel.close();
        this.manager.removeConnectedDevice(deviceId);
    }

    @Override
    public void writeRawXml(Document document) {
        Element root = document.getRootElement();
        Packet packet = null;
        if(root.getName().equals("iq")) {
            packet = new IQ(root);
        } else if (root.getName().equals("message")) {
            packet = new Message(root);
        } else if (root.getName().equals("presence")) {
            packet = new Presence(root);
        }
        sendPacket(packet);
    }

    @Override
    public void sendPacket(Packet packet) {
        packet.setTo(this.deviceId.getJid());
        packet.setFrom(new JID(XmppConstants.SERVER_JID));
        Preconditions.checkNotNull(packet);
        if(this.channel.isActive()) {
            writeToChannel(packet);
        } else {
            logger.warn("Dropping XMPP packets for switch {} because channel is not connected: {}",
                    this.deviceId, packet);
        }
    }


    @Override
    public void handlePacket(Packet packet) {
        logger.info("HANDLING PACKET from " + deviceId);
        this.manager.processUpstreamEvent(deviceId, packet);
        packet.toString();
    }

    @Override
    public void closeStream() {
        this.writeCloseStream();
    }

    private void writeCloseStream() {
        writeToChannel(new StreamClose());
    }

    @Override
    public void openStream(StreamOpen streamOpen) {
        logger.info(streamOpen.toXML());

        // 1. respond with StreamOpen
        writeStreamOpen(streamOpen);
    }

    @Override
    public void handleStreamError(StreamError streamError) {
        // TODO: Implement error handling
    }

    @Override
    public void sendStreamError(StreamError.Condition condition) {
        logger.info("Sending error");
        StreamError error = new StreamError(condition);
        this.writeToChannel(error);
    }

    @Override
    public void sendError(PacketError packetError) {
        Packet packet = new IQ();
        packet.setTo(this.deviceId.getJid());
        packet.setFrom(new JID(XmppConstants.SERVER_JID));
        packet.setError(packetError);
        this.writeToChannel(packet);
    }

    private void writeStreamOpen(StreamOpen streamOpenFromDevice) {

        Element element = createReverseStreamOpenXmlElement(streamOpenFromDevice);
        StreamOpen streamOpenToDevice = new StreamOpen(element);
        writeToChannel(streamOpenToDevice);
    }

    private void writeToChannel(Object packet) {
        this.channel.writeAndFlush(packet);
    }


    private Element createReverseStreamOpenXmlElement(StreamOpen original) {
        Element element = original.getElement().createCopy();
        JID from = original.getFromJID();
        JID to = original.getToJID();
        element.addAttribute("from", to.toString());
        element.addAttribute("to", from.toString());
        element.addAttribute("id", this.channel.id().asShortText()); // use Netty Channel ID as XMPP stream ID
        return element;
    }


}
