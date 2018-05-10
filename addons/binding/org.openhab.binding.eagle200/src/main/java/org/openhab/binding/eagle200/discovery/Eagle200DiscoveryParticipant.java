/**
 *
 */
package org.openhab.binding.eagle200.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.eagle200.Eagle200BindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author thomashentschel
 *
 */
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
public class Eagle200DiscoveryParticipant implements MDNSDiscoveryParticipant, ExtendedDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(Eagle200DiscoveryParticipant.class);

    @SuppressWarnings("unused")
    private DiscoveryServiceCallback serviceCallback;

    /**
     *
     */
    public Eagle200DiscoveryParticipant() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant#getSupportedThingTypeUIDs()
     */
    @Override
    public @NonNull Set<@NonNull ThingTypeUID> getSupportedThingTypeUIDs() {
        Set<ThingTypeUID> result = new HashSet<ThingTypeUID>();
        result.add(Eagle200BindingConstants.THING_TYPE_EAGLE200_BRIDGE);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant#getServiceType()
     */
    @Override
    public @NonNull String getServiceType() {
        return "_http._tcp.local.";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant#createResult(javax.jmdns.ServiceInfo)
     */
    @Override
    public @Nullable DiscoveryResult createResult(@NonNull ServiceInfo info) {
        if (info.getName().contains("eagle-")) {
            String id = info.getName();
            id = id.substring(id.lastIndexOf("eagle-"));
            logger.debug("eagle200 id found: " + id + " with type: " + info.getType());
            ThingUID uid = this.getThingUID(info);
            if (uid == null) {
                return null;
            }

            String cloudID = id.split("-")[1];
            String hostName = info.getHostAddresses().length > 0 ? info.getHostAddresses()[0] : "";
            Map<String, Object> properties = new HashMap<>(0);
            properties.put(Eagle200BindingConstants.THING_BRIDGECONFIG_CLOUDID, cloudID);
            properties.put(Eagle200BindingConstants.THING_BRIDGECONFIG_HOSTNAME, hostName);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(cloudID).withLabel("Eagle 200 Bridge " + id).build();
            return discoveryResult;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant#getThingUID(javax.jmdns.ServiceInfo)
     */
    @Override
    public @Nullable ThingUID getThingUID(@NonNull ServiceInfo info) {
        logger.debug("ServiceInfo: {}", info);
        logger.debug("ServiceInfo addr: {}",
                info.getHostAddresses().length > 0 ? info.getHostAddresses()[0] : "<none>");
        if (info.getType() != null) {
            if (info.getType().equals(getServiceType())) {
                if (info.getName().contains("eagle-")) {
                    String id = info.getName();
                    id = id.substring(id.lastIndexOf("eagle-"));
                    logger.warn("Discovered a Eagle 200 thing with id '{}'", id);
                    return new ThingUID(Eagle200BindingConstants.THING_TYPE_EAGLE200_BRIDGE, id);
                }
            }
        }
        return null;
    }

    @Override
    public void setDiscoveryServiceCallback(@NonNull DiscoveryServiceCallback callback) {
        this.serviceCallback = callback;
    }
}
