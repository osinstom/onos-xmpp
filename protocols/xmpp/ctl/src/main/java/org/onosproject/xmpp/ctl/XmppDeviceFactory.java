package org.onosproject.xmpp.ctl;

import org.onlab.util.ItemNotFoundException;
import org.onosproject.net.DeviceId;
import org.onosproject.net.driver.DefaultDriverData;
import org.onosproject.net.driver.DefaultDriverHandler;
import org.onosproject.net.driver.Driver;
import org.onosproject.net.driver.DriverService;
import org.onosproject.xmpp.XmppDevice;
import org.onosproject.xmpp.XmppDeviceId;
import org.onosproject.xmpp.driver.AbstractXmppDevice;
import org.onosproject.xmpp.driver.DefaultXmppDevice;
import org.onosproject.xmpp.driver.XmppDeviceDriver;
import org.onosproject.xmpp.driver.XmppDeviceManager;
import org.slf4j.Logger;
import org.xmpp.packet.JID;

import java.net.InetSocketAddress;

import static org.onosproject.xmpp.XmppDeviceId.uri;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by autonet on 01.09.17.
 */
public class XmppDeviceFactory {

    private final Logger logger = getLogger(getClass());

    private static XmppDeviceFactory INSTANCE = null;

    private XmppDeviceManager manager;
    private DriverService driverService;

    private XmppDeviceFactory() {}

    public static  XmppDeviceFactory getInstance() {
        if(INSTANCE == null)
            INSTANCE = new XmppDeviceFactory();
        return INSTANCE;
    }

    public void init(XmppDeviceManager manager, DriverService driverService) {
        setManager(manager);
        setDriverService(driverService);
    }

    /**
     * Configures XMPP device manager only if it is not initialized.
     *
     * @param manager reference object of XMPP device manager
     */
    private void setManager(XmppDeviceManager manager) {
        synchronized (manager) {
            if (this.manager == null) {
                this.manager = manager;
            } else {
                logger.warn("XMPP device manager has already been set.");
            }
        }
    }

    public void cleanManager() {
        synchronized (manager) {
            if(this.manager!=null)
                this.manager = null;
            else
                logger.warn("Manager for XMPP device is not configured");
        }
    }

    private void setDriverService(DriverService driverService) {
        synchronized (driverService) {
            if (this.driverService == null) {
                this.driverService = driverService;
            } else {
                logger.warn("Driver Service for DeviceFactory has already been set.");
            }
        }
    }

    public XmppDevice getXmppDeviceInstanceByJid(JID jid) {
        XmppDeviceId xmppDeviceId = new XmppDeviceId(jid);

        return getXmppDevice(xmppDeviceId);
    }


    public XmppDevice getXmppDeviceInstanceBySocketAddress(InetSocketAddress address) {

        XmppDeviceId xmppDeviceId = this.manager.getXmppDeviceIdBySocketAddress(address);

        return getXmppDevice(xmppDeviceId);
    }

    private XmppDevice getXmppDevice(XmppDeviceId xmppDeviceId) {
        XmppDevice device = manager.getDevice(xmppDeviceId);
        if(device!=null) {
            return device;
        } else {
            // temporary solution, TODO: getDriver for device
            XmppDevice newDevice = createXmppDriverInstance(xmppDeviceId);
            newDevice.setManager(this.manager);
            return newDevice;
        }
    }

    private XmppDevice createXmppDriverInstance(XmppDeviceId xmppDeviceId) {
        XmppDeviceDriver xmppDriver = null;
        try {
            xmppDriver = getXmppDeviceDriverInstance(xmppDeviceId);
        } catch(ItemNotFoundException ex) {
            xmppDriver = new DefaultXmppDevice();
        }

        xmppDriver.init(xmppDeviceId);
        xmppDriver.setManager(this.manager);
        return xmppDriver;
    }

    private XmppDeviceDriver getXmppDeviceDriverInstance(XmppDeviceId xmppDeviceId) throws ItemNotFoundException{
        Driver driver;
        try {
            // TODO: temp solution, need to provide universal solution
            driver = driverService.getDriver("XEP0060");
        } catch (ItemNotFoundException e) {
            throw e;
        }

        if (driver == null) {
            logger.error("No XMPP driver for {} : {}", xmppDeviceId);
            return null;
        }

        logger.info("Driver {} assigned to device {}", driver.name(), xmppDeviceId);

        if (!driver.hasBehaviour(XmppDeviceDriver.class)) {
            logger.error("Driver {} does not support XmppDeviceDriver behaviour", driver.name());
            return null;
        }

        DefaultDriverHandler handler =
                new DefaultDriverHandler(new DefaultDriverData(driver, DeviceId.deviceId(uri(xmppDeviceId.toString()))));
        XmppDeviceDriver xmppDriver = driver.createBehaviour(handler, XmppDeviceDriver.class);

        return xmppDriver;
    }


}
