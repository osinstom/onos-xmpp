package org.onosproject.provider.xmpp.device.impl;

import com.google.common.base.Preconditions;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.ChassisId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.*;
import org.onosproject.net.device.*;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.xmpp.XmppController;
import org.onosproject.xmpp.XmppDeviceId;
import org.onosproject.xmpp.XmppDeviceListener;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.xmpp.packet.JID;

import java.net.URI;
import java.net.URISyntaxException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * XMPP Device provider.
 */
@Component(immediate = true)
public class XmppDeviceProvider extends AbstractProvider implements DeviceProvider {

    private final Logger logger = getLogger(getClass());


    private static final String PROVIDER = "org.onosproject.provider.xmpp.device";
    private static final String APP_NAME = "org.onosproject.xmpp";
    private static final String XMPP = "xmpp";
    private static final String ADDRESS = "address";

    private static final String HARDWARE_VERSION = "XMPP Device";
    private static final String SOFTWARE_VERSION = "1.0";
    private static final String SERIAL_NUMBER = "unknown";
    private static final String IS_NULL_MSG = "XMPP device info is null";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected XmppController controller;

    protected DeviceProviderService providerService;

    protected ApplicationId appId;

    private XmppDeviceListener deviceListener = new InternalXmppDeviceListener();

    public XmppDeviceProvider() {
        super(new ProviderId(XMPP, PROVIDER));
    }

    @Activate
    public void activate(ComponentContext context) {
        providerService = providerRegistry.register(this);
        appId = coreService.registerApplication(APP_NAME);
        controller.addXmppDeviceListener(deviceListener);
        logger.info("Started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        controller.removeXmppDeviceListener(deviceListener);
        providerRegistry.unregister(this);
        providerService = null;
    }

    @Override
    public void triggerProbe(DeviceId deviceId) {

    }

    @Override
    public void roleChanged(DeviceId deviceId, MastershipRole newRole) {

    }

    @Override
    public boolean isReachable(DeviceId deviceId) {
        String id = deviceId.uri().getSchemeSpecificPart();
        JID jid = new JID(id);
        XmppDeviceId xmppDeviceId = new XmppDeviceId(jid);
        if(controller.getDevice(xmppDeviceId)==null) {
            return false;
        } else
            return true;
    }

    @Override
    public void changePortState(DeviceId deviceId, PortNumber portNumber, boolean enable) {

    }

    private void connectDevice(XmppDeviceId xmppDeviceId) {
        DeviceId deviceId = DeviceId.deviceId(xmppDeviceId.id());

        // Assumption: manufacturer is uniquely identified by domain part of JID
        String manufacturer = xmppDeviceId.getJid().getDomain();

        ChassisId cid = new ChassisId();

        SparseAnnotations annotations = DefaultAnnotations.builder()
                .set(AnnotationKeys.PROTOCOL, XMPP.toUpperCase())
                .build();
        DeviceDescription deviceDescription = new DefaultDeviceDescription(
                deviceId.uri(),
                Device.Type.OTHER,
                manufacturer, HARDWARE_VERSION,
                SOFTWARE_VERSION, SERIAL_NUMBER,
                cid, true,
                annotations);

        if (deviceService.getDevice(deviceId) == null) {
            providerService.deviceConnected(deviceId, deviceDescription);
        }
    }

    private void disconnectDevice(XmppDeviceId xmppDeviceId) {
        Preconditions.checkNotNull(xmppDeviceId, IS_NULL_MSG);

        DeviceId deviceId = DeviceId.deviceId(xmppDeviceId.id());
        if (deviceService.getDevice(deviceId) != null) {
            providerService.deviceDisconnected(deviceId);
            logger.info("XMPP device {} removed from XMPP controller", deviceId);
        } else {
            logger.warn("XMPP device {} does not exist in the store, " +
                    "or it may already have been removed", deviceId);
        }
    }

    private class InternalXmppDeviceListener implements XmppDeviceListener {

        @Override
        public void deviceConnected(XmppDeviceId deviceId) {
            logger.info("NOTIFICATION: device {} connected", deviceId);
            connectDevice(deviceId);
        }

        @Override
        public void deviceDisconnected(XmppDeviceId deviceId) {
            logger.info("NOTIFICATION: device {} disconnected", deviceId);
            disconnectDevice(deviceId);
        }
    }


}
