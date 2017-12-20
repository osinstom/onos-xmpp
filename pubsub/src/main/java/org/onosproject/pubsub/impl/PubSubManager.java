package org.onosproject.pubsub.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.AbstractListenerProviderRegistry;
import org.onosproject.net.provider.AbstractProviderService;
import org.onosproject.pubsub.api.*;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
@Service
public class PubSubManager extends AbstractListenerProviderRegistry<PubSubEvent, PubSubListener, PubSubProvider, PubSubProviderService>
        implements PubSubService, PubSubAdminService, PubSubProviderRegistry{

    private final Logger logger = getLogger(getClass());

    @Activate
    public void activate() {
        eventDispatcher.addSink(PubSubEvent.class, listenerRegistry);
    }

    @Deactivate
    public void deactivate() {
        eventDispatcher.removeSink(PubSubEvent.class);
    }

    @Override
    protected PubSubProviderService createProviderService(PubSubProvider provider) {
        return new InternalPubSubProviderService(provider);
    }

    @Override
    public void sendEventNotification(List<DeviceId> devices, Object notificationInfo) {
        for(DeviceId deviceId : devices) {
            sendEventNotification(deviceId, notificationInfo);
        }
    }

    @Override
    public void sendEventNotification(DeviceId deviceId, Object info) {
        logger.info("Provider for deviceId {}", deviceId);
        PubSubProvider provider = getProvider(deviceId);
        provider.sendNotification(deviceId, info);
    }


    private class InternalPubSubProviderService
            extends AbstractProviderService<PubSubProvider>
            implements PubSubProviderService {

        public InternalPubSubProviderService(PubSubProvider provider) {
            super(provider);
        }

        @Override
        public void subscribe(SubscriptionInfo subscriptionInfo) {
            final PubSubEvent event = new PubSubEvent(PubSubEvent.Type.NEW_SUBSCRIPTION, subscriptionInfo);
            post(event);
        }

        @Override
        public void unsubscribe(SubscriptionInfo subscriptionInfo) {
            final PubSubEvent event = new PubSubEvent(PubSubEvent.Type.DELETE_SUBSCRIPTION, subscriptionInfo);
            post(event);
        }

        @Override
        public void publish(PublishInfo publishInfo) {
            final PubSubEvent event = new PubSubEvent(PubSubEvent.Type.PUBLISH, publishInfo);
            post(event);
        }

        @Override
        public void retract(Retract retract) {
            final PubSubEvent event = new PubSubEvent(PubSubEvent.Type.RETRACT, retract);
            post(event);
        }
    }

}
