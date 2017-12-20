package org.onosproject.xmpp.ctl;

import com.google.common.collect.Maps;
import org.apache.felix.scr.annotations.*;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.CoreService;
import org.onosproject.net.driver.DriverService;
import org.onosproject.xmpp.*;
import org.onosproject.xmpp.driver.XmppDeviceManager;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.*;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The main class (bundle) of XMPP protocol.
 * Responsible for:
 * 1. Initialization and starting XMPP server.
 * 2. Handling XMPP packets from clients and writing to clients.
 * 3. Configuration parameters initialization.
 * 4. Notifing listeners about XMPP events/packets.
 */
@Component(immediate = true)
@Service
public class XmppControllerImpl implements XmppController {

    private static final String APP_ID = "org.onosproject.xmpp";
    private static final String XMPP_PORT = "5269";

    private static final Logger logger =
            LoggerFactory.getLogger(XmppControllerImpl.class);

    // core services declaration
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DriverService driverService;

    // configuration properties definition
    @Property(name = "xmppPort", value = XMPP_PORT,
            label = "Port number used by XMPP protocol; default is 5269")
    private String xmppPort = XMPP_PORT;


    // listener declaration
    protected Set<XmppDeviceListener> xmppDeviceListeners = new CopyOnWriteArraySet<XmppDeviceListener>();

    protected Set<XmppIqListener> xmppIqListeners = new CopyOnWriteArraySet<XmppIqListener>();
    protected Set<XmppMessageListener> xmppMessageListeners = new CopyOnWriteArraySet<XmppMessageListener>();
    protected Set<XmppPresenceListener> xmppPresenceListeners = new CopyOnWriteArraySet<XmppPresenceListener>();

    protected XmppDeviceManager manager = new DefaultXmppDeviceManager();

    private final XmppServer xmppServer = new XmppServer();
    private XmppDeviceFactory deviceFactory = XmppDeviceFactory.getInstance();

    ConcurrentMap<XmppDeviceId, XmppDevice> connectedDevices = Maps.newConcurrentMap();
    ConcurrentMap<InetSocketAddress, XmppDeviceId> addressDeviceIdMap = Maps.newConcurrentMap();

    @Activate
    public void activate(ComponentContext context) {
        logger.info("XmppControllerImpl started.");
        coreService.registerApplication(APP_ID, this::cleanup);
        cfgService.registerProperties(getClass());
        deviceFactory.init(manager, driverService);
        xmppServer.setConfiguration(context.getProperties());
        xmppServer.start();
    }

    @Deactivate
    public void deactivate() {
        cleanup();
        cfgService.unregisterProperties(getClass(), false);
        logger.info("Stopped");
    }

    private void cleanup() {
        xmppServer.stop();
        deviceFactory.cleanManager();
        connectedDevices.values().forEach(XmppDevice::disconnectDevice);
        connectedDevices.clear();
    }

    @Override
    public XmppDevice getDevice(XmppDeviceId xmppDeviceId) {
        return connectedDevices.get(xmppDeviceId);
    }

    @Override
    public void addXmppDeviceListener(XmppDeviceListener deviceListener) {
        xmppDeviceListeners.add(deviceListener);
    }

    @Override
    public void removeXmppDeviceListener(XmppDeviceListener deviceListener) {
        xmppDeviceListeners.remove(deviceListener);
    }

    @Override
    public void addXmppIqListener(XmppIqListener iqListener) {
        xmppIqListeners.add(iqListener);
    }

    @Override
    public void removeXmppIqListener(XmppIqListener iqListener) {
        xmppIqListeners.remove(iqListener);
    }

    @Override
    public void addXmppMessageListener(XmppMessageListener messageListener) {
        xmppMessageListeners.add(messageListener);
    }

    @Override
    public void removeXmppMessageListener(XmppMessageListener messageListener) {
        xmppMessageListeners.remove(messageListener);
    }

    @Override
    public void addXmppPresenceListener(XmppPresenceListener presenceListener) {
        xmppPresenceListeners.add(presenceListener);
    }

    @Override
    public void removeXmppPresenceListener(XmppPresenceListener presenceListener) {
        xmppPresenceListeners.remove(presenceListener);
    }


    private class DefaultXmppDeviceManager implements XmppDeviceManager {

        private final Logger logger = getLogger(DefaultXmppDeviceManager.class);

        @Override
        public boolean addConnectedDevice(XmppDeviceId deviceId, XmppDevice device) {
            if (connectedDevices.get(deviceId) != null) {
                logger.warn("Trying to add Xmpp Device but found a previous " +
                        "value for XMPP deviceId: {}", deviceId);
                return false;
            } else {
                logger.info("Added XMPP device: {}", deviceId);
                connectedDevices.put(deviceId, device);
                for(XmppDeviceListener listener : xmppDeviceListeners)
                    listener.deviceConnected(deviceId);
                return true;
            }
        }

        @Override
        public void removeConnectedDevice(XmppDeviceId deviceId) {
            connectedDevices.remove(deviceId);
            for(XmppDeviceListener listener : xmppDeviceListeners)
                listener.deviceDisconnected(deviceId);
        }

        @Override
        public XmppDevice getDevice(XmppDeviceId deviceId) {
            return connectedDevices.get(deviceId);
        }

        @Override
        public void processUpstreamEvent(XmppDeviceId deviceId, Packet packet) {
            if(packet instanceof IQ)
                for(XmppIqListener iqListener: xmppIqListeners)
                    iqListener.handleIqStanza((IQ)packet);
            if(packet instanceof Message)
                for(XmppMessageListener messageListener : xmppMessageListeners)
                    messageListener.handleMessageStanza((Message) packet);
            if(packet instanceof Presence)
                for(XmppPresenceListener presenceListener : xmppPresenceListeners)
                    presenceListener.handlePresenceStanza((Presence) packet);
        }

        @Override
        public XmppDeviceId getXmppDeviceIdBySocketAddress(InetSocketAddress address) {
            Iterator iterator = connectedDevices.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry pair = (Map.Entry)iterator.next();
                XmppDevice device = (XmppDevice) pair.getValue();
                InetSocketAddress deviceAddress = (InetSocketAddress) device.getChannel().remoteAddress();
                if(deviceAddress.equals(address)) {
                    return (XmppDeviceId)pair.getKey();
                }
            }
            return null;
        }

    }


}
