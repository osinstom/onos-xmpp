package org.onosproject.contrail;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
@Command(scope = "onos", name = "l3vpn-subs",
        description = "List all the subscriptions for L3Vpns")
public class L3VpnSubscriptionsCommand extends AbstractShellCommand {

    @Override
    protected void execute() {
        L3VpnController l3VpnController = AbstractShellCommand.get(L3VpnController.class);
        ConcurrentMap<String, List<DeviceId>> subscriptions = l3VpnController.getSubscriptions();
        System.out.println("-----------------------------------------------------------------------------------------");
        for(String vpn : subscriptions.keySet()) {
            System.out.println("SUBSCRIPTIONS \t\t\t\t\t\t NETWORK: " + vpn);
            for(DeviceId deviceId : subscriptions.get(vpn)) {
                System.out.println(deviceId.uri().toString());
            }
        }
    }
}
