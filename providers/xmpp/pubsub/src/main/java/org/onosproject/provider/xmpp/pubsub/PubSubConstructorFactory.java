package org.onosproject.provider.xmpp.pubsub;

import org.onlab.util.ItemNotFoundException;
import org.onosproject.net.DeviceId;
import org.onosproject.net.driver.DefaultDriverData;
import org.onosproject.net.driver.DefaultDriverHandler;
import org.onosproject.net.driver.Driver;
import org.onosproject.net.driver.DriverService;
import org.onosproject.pubsub.api.PubSubInfoConstructor;
import org.onosproject.xmpp.XmppDeviceId;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

public class PubSubConstructorFactory {

    private final Logger logger = getLogger(getClass());

    private static final String HARDWARE_VERSION = "XMPP Device";
    private static final String SOFTWARE_VERSION = "1.0";

    private DriverService driverService;

    public static PubSubConstructorFactory instance;

    public static PubSubConstructorFactory getInstance() {
        if(instance==null) {
            instance = new PubSubConstructorFactory();
        }
        return instance;
    }

    public void init(DriverService driverService) {
        this.driverService = driverService;
    }

    public PubSubInfoConstructor getPubSubInfoConstructor(String domain) {

        Driver driver = getDriverByJidDomain(domain);
        checkNotNull(driver);

        DeviceId deviceId = DeviceId.deviceId(XmppDeviceId.uri(domain));
        logger.info("Driver {} assigned to device {}", driver.name(), deviceId);

        DefaultDriverHandler handler =
                new DefaultDriverHandler(new DefaultDriverData(driver, deviceId));

        PubSubInfoConstructor pubSubInfoConstructor = driver.createBehaviour(handler, PubSubInfoConstructor.class);
        return pubSubInfoConstructor;
    }

    private Driver getDriverByJidDomain(String domain) {
        Driver driver;
        try {
            driver = driverService.getDriver(domain, HARDWARE_VERSION, SOFTWARE_VERSION);
        } catch (ItemNotFoundException e) {
            throw e;
        }

        if (driver == null) {
            logger.error("No XMPP driver for domain: {}", domain);
            return null;
        }

        if (!driver.hasBehaviour(PubSubInfoConstructor.class)) {
            logger.error("Driver {} does not support PubSubInfoConstructor behaviour", driver.name());
            return null;
        }
        return driver;
    }


}
