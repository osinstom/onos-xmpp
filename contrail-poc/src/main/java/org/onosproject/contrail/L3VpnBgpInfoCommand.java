package org.onosproject.contrail;

import org.dom4j.Element;
import org.onosproject.cli.AbstractShellCommand;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Argument;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.pubsub.api.PublishInfo;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
@Command(scope = "onos", name = "l3vpn-bgp-info",
        description = "List all the BGP informations for L3Vpns")
public class L3VpnBgpInfoCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "vpn", description = "VPN name",
            required = true, multiValued = false)
    private String vpnInstance = null;

    @Override
    protected void execute() {
        L3VpnController l3VpnController = AbstractShellCommand.get(L3VpnController.class);
        ConcurrentMap<String, List<DeviceId>> subscriptions = l3VpnController.getSubscriptions();
        ConcurrentMap<PublishInfo, DeviceId> bgpInfoMap = l3VpnController.getBgpInfoMap();
        List<DeviceId> devices = subscriptions.get(vpnInstance);
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("BGP informations \t\t\t\t\t\t NETWORK: " + vpnInstance);
        for(DeviceId deviceId : devices) {
            for (PublishInfo info : bgpInfoMap.keySet()) {
                if (bgpInfoMap.get(info).equals(deviceId)) {
                    Object payload = info.getPayload();
                    System.out.println("DEVICE=" + deviceId.toString() + "\nID=" + info.getItemId() + "\n" + payload.toString());
                }
            }
        }
    }

}
