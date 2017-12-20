package org.onosproject.contrail;

import com.google.common.collect.Maps;
import org.apache.felix.scr.annotations.*;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.pubsub.api.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of BGP-signaled End System L3VPN management for OpenContrail vRouters.
 */
@Component(immediate = true)
@Service(value = L3VpnController.class)
public class L3VpnController {

    private final Logger logger = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PubSubService pubSubService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    private PubSubListener listener = new InternalPubSubListener();
    private DeviceListener deviceListener = new InternalDeviceListener();

    private ConcurrentMap<String, List<DeviceId>> vpnDevicesMap = Maps.newConcurrentMap();
    private ConcurrentMap<PublishInfo, DeviceId> bgpInfoMap = Maps.newConcurrentMap();
    private ConcurrentMap<PublishInfo, DeviceId> alreadyNotified = Maps.newConcurrentMap();

    private String[] vpns = { "net0", "net1", "net2", "net3", "net4", "net5", "net6", "net7", "net8", "net9" };

    @Activate
    public void activate() {
        logger.info("Started");
        pubSubService.addListener(listener);
        deviceService.addListener(deviceListener);
        initializeVpns();
    }

    @Deactivate
    public void deactivate() {
        logger.info("Stopped");
        pubSubService.removeListener(listener);
        deviceService.removeListener(deviceListener);
    }

    public ConcurrentMap<String, List<DeviceId>> getSubscriptions() {
        return vpnDevicesMap;
    }

    public ConcurrentMap<PublishInfo, DeviceId> getBgpInfoMap() {
        return bgpInfoMap;
    }

    private void initializeVpns() {
        for(String vpn : vpns) {
            List<DeviceId> devices = new ArrayList<DeviceId>();
            vpnDevicesMap.put(vpn, devices);
        }
    }


    private void handlePublish(PublishInfo publishInfo) {
        String vpnInstance = publishInfo.getNodeId();
        if(vpnDevicesMap.containsKey(vpnInstance)) {
            DeviceId publisher = publishInfo.getFromDevice();
            storeBgpInfo(publisher, publishInfo);
            notifyVpnMembers(publisher, publishInfo);
            notifyPublisherAboutVpnMembers(publisher, publishInfo);
            logger.info("Status of the VPN Store after Publish: " + bgpInfoMap.toString());
        } else {
            pubSubService.sendEventNotification(publishInfo.getFromDevice(), new PubSubError(PubSubError.ErrorType.ITEM_NOT_FOUND));
        }
    }

    private void storeBgpInfo(DeviceId publisher, PublishInfo publishInfo) {
        bgpInfoMap.putIfAbsent(publishInfo, publisher);
    }

    /**
     * When Publish is received, the VPN members must be notifier about reachability of NEW member
     * @param publisher
     * @param publishInfo
     */
    private void notifyVpnMembers(DeviceId publisher, PublishInfo publishInfo) {
        List<DeviceId> devicesToNotify = getVpnMembersExceptPublisher(publishInfo.getNodeId(), publisher);
        pubSubService.sendEventNotification(devicesToNotify, publishInfo);
    }

    /**
     * When Publish is received, the Publisher must be notified about reachability of all VPN members
     * @param publisher
     * @param publishInfo
     */
    private void notifyPublisherAboutVpnMembers(DeviceId publisher, PublishInfo publishInfo) {
        String vpnInstance = publishInfo.getNodeId();
        List<DeviceId> vpnMembers = getVpnMembersExceptPublisher(vpnInstance, publisher);

        for(DeviceId member : vpnMembers) {
            for (PublishInfo info : bgpInfoMap.keySet()) {
                if (bgpInfoMap.get(info).equals(member) && !isAlreadyNotified(publisher, info)) {
                    logger.info("Sending Event Notification: " + info.toString());
                    pubSubService.sendEventNotification(publisher, info);
                    alreadyNotified.putIfAbsent(info, publisher);
                }
            }
        }
    }

    private boolean isAlreadyNotified(DeviceId publisher, PublishInfo info) {
        for(PublishInfo notifiedInfo : alreadyNotified.keySet()) {
            if(notifiedInfo.equals(info) && alreadyNotified.get(notifiedInfo).equals(publisher)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method should return all VPN members except publisher
     * @return
     */
    private List<DeviceId> getVpnMembersExceptPublisher(String vpnInstance, DeviceId publisher) {
        List<DeviceId> vpnDevices = vpnDevicesMap.get(vpnInstance);
        List<DeviceId> devicesToNotify = new ArrayList<DeviceId>();

        for(DeviceId device : vpnDevices)
            if(!device.equals(publisher))
                devicesToNotify.add(device);
        return devicesToNotify;
    }

    private void handleNewSubscription(SubscriptionInfo info) {
        String vpnInstanceName = info.getNodeId();
        List<DeviceId> vpnDevices = vpnDevicesMap.get(vpnInstanceName);
        DeviceId device = info.getFromDevice();
        if(vpnDevices!=null) {
            if(!vpnDevices.contains(device))
                vpnDevices.add(device);
        } else {
            pubSubService.sendEventNotification(info.getFromDevice(), new PubSubError(PubSubError.ErrorType.ITEM_NOT_FOUND));
        }
        logger.info("NEW_SUBSCRIPTION handled. Status of subscrptions: /n {}", vpnDevicesMap.toString());
    }

    private void handleDeleteSubscription(SubscriptionInfo info) {
        String vpnInstanceName = info.getNodeId();
        DeviceId device = info.getFromDevice();
        removeFromVpnIfDeviceExists(vpnInstanceName, device);
    }

    private void handleRetract(Retract retractMsg) {
        logger.info(retractMsg.toString());
        String vpnInstance = retractMsg.getNodeId();
        String itemId = retractMsg.getItemId();

        logger.info("VPN ID = " + vpnInstance);
        // TODO: Need to handle retract action, check a proper behaviour in specification

        if(vpnDevicesMap.containsKey(vpnInstance)) {
            DeviceId publisher = retractMsg.getFromDevice();
            removeBgpInfoFromVpnStore(publisher, itemId);
            List<DeviceId> devicesToNotify = getVpnMembersExceptPublisher(vpnInstance, publisher);
            pubSubService.sendEventNotification(devicesToNotify, retractMsg);
            logger.info("Status of the VPN Store after Retract: " + bgpInfoMap.toString());
        } else {
            pubSubService.sendEventNotification(retractMsg.getFromDevice(), new PubSubError(PubSubError.ErrorType.ITEM_NOT_FOUND));
        }
    }

    private void removeBgpInfoFromVpnStore(DeviceId publisher, String itemId) {
        for(PublishInfo info : bgpInfoMap.keySet()) {
            if(itemId.equals(info.getItemId())) {
                boolean isRemoved = bgpInfoMap.remove(info, publisher);
                logger.info("BGP entry has been removed");
            }
        }
    }

    private void removeFromVpnIfDeviceExists(String vpnInstanceName, DeviceId deviceId) {
        List<DeviceId> vpnDevices = vpnDevicesMap.get(vpnInstanceName);
        if(vpnDevices==null) {
            pubSubService.sendEventNotification(deviceId, new PubSubError(PubSubError.ErrorType.ITEM_NOT_FOUND));
            return;
        }
        if(vpnDevices.contains(deviceId)) {
            vpnDevices.remove(deviceId);
            logger.info("Device '{}' has been removed from VPN '{}'. Status of the VPN store: {}",
                    deviceId, vpnInstanceName, vpnDevicesMap.toString());
        } else {
            pubSubService.sendEventNotification(deviceId, new PubSubError(PubSubError.ErrorType.NOT_SUBSCRIBED));
        }
    }

    private void removeFromStoreIfDeviceExists(DeviceId deviceId) {
        for(String vpn : vpnDevicesMap.keySet()) {
            List<DeviceId> vpnDevices = vpnDevicesMap.get(vpn);
            checkNotNull(vpnDevices);
            if(vpnDevices.contains(deviceId)) {
                vpnDevices.remove(deviceId);
                logger.info("Device '{}' has been removed from VPN '{}'. Status of the VPN store: {}",
                        deviceId, vpn, vpnDevicesMap.toString());
            }
        }
    }

    private class InternalPubSubListener implements PubSubListener {

        @Override
        public void event(PubSubEvent event) {
            PubSubEvent.Type type = event.type();
            switch(type) {
                case NEW_SUBSCRIPTION:
                    SubscriptionInfo info = (SubscriptionInfo) event.subject();
                    handleNewSubscription(info);
                    break;
                case DELETE_SUBSCRIPTION:
                    SubscriptionInfo unsubscriptionInfo = (SubscriptionInfo) event.subject();
                    handleDeleteSubscription(unsubscriptionInfo);
                    break;
                case PUBLISH:
                    PublishInfo publishInfo = (PublishInfo) event.subject();
                    handlePublish(publishInfo);
                    break;
                case RETRACT:
                    Retract retractMessage = (Retract) event.subject();
                    handleRetract(retractMessage);
                    break;
            }
        }
    }


    private class InternalDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {
            switch (event.type()) {
                case DEVICE_UPDATED:
                case DEVICE_SUSPENDED:
                case DEVICE_AVAILABILITY_CHANGED:
                case DEVICE_REMOVED:
                    DeviceId device = event.subject().id();
                    if(!deviceService.isAvailable(device)) {
                        removeFromStoreIfDeviceExists(device);
                    }
                    break;
            }
        }

    }
}
