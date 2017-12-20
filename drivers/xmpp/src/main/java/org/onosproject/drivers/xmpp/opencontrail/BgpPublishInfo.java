package org.onosproject.drivers.xmpp.opencontrail;

import org.onosproject.net.DeviceId;
import org.onosproject.pubsub.api.PublishInfo;

import java.util.ArrayList;
import java.util.List;

public class BgpPublishInfo extends PublishInfo {

    private List<BgpVpnPubSubEntry> entries = new ArrayList<BgpVpnPubSubEntry>();

    public BgpPublishInfo(DeviceId deviceId, String vpnInstanceName, String itemId) {
        super(deviceId, vpnInstanceName, itemId);
    }

    public void addEntry(BgpVpnPubSubEntry entry) {
        entries.add(entry);
    }

    public List<BgpVpnPubSubEntry> getEntries() {
        return entries;
    }


    @Override
    public String toString() {
        return "BgpPublishInfo{" +
                "vpnInstanceName='" + this.nodeId + '\'' +
                ", entries=" + entries +
                '}';
    }
}
