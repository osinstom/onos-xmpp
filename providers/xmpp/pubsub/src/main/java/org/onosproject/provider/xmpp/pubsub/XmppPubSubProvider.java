package org.onosproject.provider.xmpp.pubsub;

import org.apache.felix.scr.annotations.*;
import org.onosproject.net.DeviceId;
import org.onosproject.net.driver.DriverService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.pubsub.api.*;
import org.onosproject.xmpp.XmppController;
import org.onosproject.xmpp.XmppDevice;
import org.onosproject.xmpp.XmppDeviceId;
import org.onosproject.xmpp.XmppIqListener;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.*;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
public class XmppPubSubProvider extends AbstractProvider implements PubSubProvider {

    private final Logger logger = getLogger(getClass());

    private static final String PROVIDER = "org.onosproject.provider.xmpp.pubsub";
    private static final String XMPP = "xmpp";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DriverService driverService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppController controller;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PubSubProviderRegistry providerRegistry;


    protected PubSubProviderService providerService;

    private XmppIqListener iqListener = new InternalXmppIqListener();

    /**
     * Creates a XmppPubSubProvider with the supplied identifier.
     *
     */
    public XmppPubSubProvider() {
        super(new ProviderId(XMPP, PROVIDER));
    }

    @Activate
    public void activate(ComponentContext context) {
        providerService = providerRegistry.register(this);
        controller.addXmppIqListener(iqListener);
        PubSubConstructorFactory.getInstance().init(driverService);
        logger.info("Started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        controller.removeXmppIqListener(iqListener);
        providerRegistry.unregister(this);
        providerService = null;
        logger.info("Stopped");
    }

    @Override
    public void sendNotification(List<DeviceId> devices, Object message) {
        try {
            logger.info("Sending notifications...");
            notifyDevices(devices, message);
        } catch(IllegalArgumentException e) {
            throw e;
        }
    }

    private void notifyDevices(List<DeviceId> devices, Object info) {
        for(DeviceId device : devices) {
            sendNotification(device, info);
        }
    }

    @Override
    public void sendNotification(DeviceId device, Object message) {
        XmppDeviceId xmppDeviceId = XmppPubSubUtils.getXmppDeviceId(device);
        if(message instanceof PubSubError) {
            sendErrorNotification(xmppDeviceId,(PubSubError)message);
        } else {
            Packet notification = XmppPubSubUtils.constructXmppNotification(xmppDeviceId, message);
            sendXmppPacketToDevice(xmppDeviceId, notification);
        }
    }

    private void sendErrorNotification(XmppDeviceId xmppDeviceId, PubSubError error) {
        XmppDevice xmppDevice = getXmppDevice(xmppDeviceId);
        PacketError.Condition condition = XmppPubSubUtils.getConditionForPubSubError(error);
        if(condition!=null)
            xmppDevice.sendError(new PacketError(condition));
    }

    private void sendErrorNotification(XmppDeviceId xmppDeviceId, PacketError packet) {
        XmppDevice xmppDevice = getXmppDevice(xmppDeviceId);
        xmppDevice.sendError(packet);
    }

    private void sendXmppPacketToDevice(XmppDeviceId xmppDeviceId, Packet packet) {
        XmppDevice xmppDevice = getXmppDevice(xmppDeviceId);
        xmppDevice.sendPacket(packet);
    }

    private XmppDevice getXmppDevice(XmppDeviceId xmppDeviceId) {
        XmppDevice xmppDevice = controller.getDevice(xmppDeviceId);
        return xmppDevice;
    }

    private void handlePubSubOperation(XmppPubSubUtils.Method method, IQ iq) {
        switch(method) {
            case SUBSCRIBE:
                handleSubscribe(iq);
                break;
            case UNSUBSCRIBE:
                handleUnsubscribe(iq);
                break;
            case PUBLISH:
                handlePublish(iq);
                break;
            case RETRACT:
                handleRetract(iq);
                break;
        }
    }

    private void handleSubscribe(IQ iq) {
        SubscriptionInfo subscriptionInfo = XmppPubSubUtils.parseSubscription(iq);
        notifyNewSubscriptionToCore(subscriptionInfo);
    }

    private void notifyNewSubscriptionToCore(SubscriptionInfo subscriptionInfo) {
        providerService.subscribe(subscriptionInfo);
    }

    private void handleUnsubscribe(IQ iq) {
        SubscriptionInfo subscriptionInfo = XmppPubSubUtils.parseSubscription(iq);
        notifyUnsubscribeToCore(subscriptionInfo);
    }

    private void notifyUnsubscribeToCore(SubscriptionInfo subscriptionInfo) {
        providerService.unsubscribe(subscriptionInfo);
    }

    private void handlePublish(IQ iq) {
        try {
            PublishInfo publishInfo = XmppPubSubUtils.parsePublish(iq);
            notifyPublishInfoToCore(publishInfo);
        } catch (Exception e) {
            DeviceId deviceId = DeviceId.deviceId(XmppDeviceId.uri(iq.getFrom()));
            XmppDeviceId xmppDeviceId = XmppPubSubUtils.getXmppDeviceId(deviceId);
            sendErrorNotification(xmppDeviceId, new PubSubError(PubSubError.ErrorType.INVALID_PAYLOAD));
        }
    }

    private void notifyPublishInfoToCore(PublishInfo publishInfo) {
        providerService.publish(publishInfo);
    }

    private void handleRetract(IQ iq) {
        try {
            Retract retractInfo = XmppPubSubUtils.parseRetract(iq);
            notifyRetractInfoToCore(retractInfo);
        } catch(PubSubValidationException e) {
            logger.info("Error while parsing Retract message.");
            DeviceId deviceId = DeviceId.deviceId(XmppDeviceId.uri(iq.getFrom()));
            XmppDeviceId xmppDeviceId = XmppPubSubUtils.getXmppDeviceId(deviceId);
            sendErrorNotification(xmppDeviceId, e.asPacketError());
        }
    }

    private void notifyRetractInfoToCore(Retract retract) {
        providerService.retract(retract);
    }

    private class InternalXmppIqListener implements XmppIqListener {

        @Override
        public void handleIqStanza(IQ iqEvent) {
            if(XmppPubSubUtils.isPubSub(iqEvent)) {
                XmppPubSubUtils.Method method = XmppPubSubUtils.getMethod(iqEvent);
                handlePubSubOperation(method, iqEvent);
            }
        }

    }





}
