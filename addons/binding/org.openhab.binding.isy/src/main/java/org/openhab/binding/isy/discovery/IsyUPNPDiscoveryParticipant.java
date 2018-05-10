package org.openhab.binding.isy.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.isy.IsyBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = UpnpDiscoveryParticipant.class, immediate = true)
public class IsyUPNPDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private static final Logger logger = LoggerFactory.getLogger(IsyUPNPDiscoveryParticipant.class);

    public IsyUPNPDiscoveryParticipant() {
    }

    @Override
    public @NonNull Set<@NonNull ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(IsyBindingConstants.THING_TYPE_ISYBRIDGE);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(2);
            properties.put(IsyBindingConstants.BRIDGE_CONFIG_IPADDRESS, device.getDetails().getBaseURL().getHost());
            properties.put(IsyBindingConstants.BRIDGE_CONFIG_SERIALNUMBER, getUDNKeyAsString(device));

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(device.getDetails().getModelDetails().getModelName())
                    .withRepresentationProperty(IsyBindingConstants.BRIDGE_CONFIG_SERIALNUMBER).build();
            logger.debug("UPNP discovery creating device " + result);
            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details != null) {
            ModelDetails modelDetails = details.getModelDetails();
            // ManufacturerDetails mfd = details.getManufacturerDetails();
            if (modelDetails != null) {
                String modelName = modelDetails.getModelName();
                if (modelName != null) {
                    if (modelName.toLowerCase().startsWith(IsyBindingConstants.UPNP_DISCOVERY_KEY)) {
                        String id = getUDNKeyAsString(device);
                        logger.debug("UPNP discovered ISY model {} with id {}", modelName, id);
                        return new ThingUID(IsyBindingConstants.THING_TYPE_ISYBRIDGE, id);
                    }
                }
            }
        }
        return null;
    }

    private static String getUDNKeyAsString(RemoteDevice device) {
        return device.getIdentity().getUdn().getIdentifierString().replaceAll(":", "-");
    }
}
