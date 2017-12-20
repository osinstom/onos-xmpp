package org.onosproject.drivers.xmpp;


import org.apache.felix.scr.annotations.Component;
import org.onosproject.net.driver.AbstractDriverLoader;

/**
 * Loader for OpenContrail XMPP device drivers.
 */
@Component(immediate = true)
public class XmppDriversLoader extends AbstractDriverLoader {

    /**
     * Creates a new loader for resource with the specified path.
     */
    public XmppDriversLoader() {
        super("/xmpp-drivers.xml");
    }

}
