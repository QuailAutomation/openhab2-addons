/**
 *
 */
package org.openhab.binding.eagle200.discovery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.eagle200.Eagle200BindingConstants;
import org.openhab.binding.eagle200.handler.Eagle200BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author thomashentschel
 *
 */

public class EagleDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {

    private static final int TIMEOUT = 20;
    private static final int REFRESH = 15;
    private Eagle200BridgeHandler bridgeHandler;
    private ScheduledFuture<?> discoveryJob;
    private Runnable discoveryRunnable;
    private DiscoveryServiceCallback discoveryServiceCallback;
    private boolean scanInProgress = false;
    private int scancount = 0;
    private static final Logger logger = LoggerFactory.getLogger(EagleDiscoveryService.class);

    /**
     * @param timeout
     * @throws IllegalArgumentException
     */
    public EagleDiscoveryService(Eagle200BridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(Collections.singleton(Eagle200BindingConstants.THING_TYPE_EAGLE200_METER), TIMEOUT, true);
        this.bridgeHandler = bridgeHandler;
        this.discoveryRunnable = new Runnable() {

            @Override
            public void run() {
                EagleDiscoveryService.this.startScan();
            }
        };
    }

    @Override
    public void setDiscoveryServiceCallback(@NonNull DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    public void activate() {
        bridgeHandler.registerDiscoveryService(this);
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        bridgeHandler.unregisterDiscoveryService();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start eagle200 device background discovery");
        if (this.discoveryJob == null || this.discoveryJob.isCancelled()) {
            this.discoveryJob = scheduler.scheduleWithFixedDelay(this.discoveryRunnable, 0, REFRESH, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop eagle200 device background discovery");
        if (this.discoveryJob != null && !this.discoveryJob.isCancelled()) {
            this.discoveryJob.cancel(true);
            this.discoveryJob = null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#startScan()
     */
    @Override
    protected void startScan() {
        if (this.scanInProgress) {
            return;
        }
        this.scanInProgress = true;
        try {
            if (this.bridgeHandler.getConfiguration().isComplete()) {
                logger.trace("Scanning for energy meters");
                this.scanForEnergyMeterThings();
                // TODO: eagle200 supports other devices, scan for them next...
            } else {
                logger.debug("connection to Eagle failed, leaving device scan. Fix configuration");
                return;
            }
        } finally {
            this.scanInProgress = false;
        }
    }

    private void scanForEnergyMeterThings() {
        List<String> meterAddresses;
        try {
            meterAddresses = bridgeHandler.getConnection().getMeterHWAddress();
        } catch (IOException | IllegalStateException e) {
            logger.warn("connection to Eagle failed, stopping device scan. Fix network or configuration: {}",
                    e.getMessage());
            return;
        }

        ThingUID bridgeID = this.bridgeHandler.getThing().getUID();
        // for each address add a device
        for (String meterAddress : meterAddresses) {
            ThingTypeUID theThingTypeUid = Eagle200BindingConstants.THING_TYPE_EAGLE200_METER;
            String thingID = meterAddress;
            // ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeID, thingID);
            ThingUID thingUID = new ThingUID(theThingTypeUid, thingID);
            if (this.discoveryServiceCallback.getExistingDiscoveryResult(thingUID) != null) {
                logger.trace("Thing " + thingUID.toString() + " was already discovered");
                return;
            }
            if (this.discoveryServiceCallback.getExistingThing(thingUID) != null) {
                logger.trace("Thing " + thingUID.toString() + " already exists");
                return;
            }

            Map<String, Object> properties = new HashMap<>(0);
            properties.put(Eagle200BindingConstants.THING_CONFIG_HWADDRESS, meterAddress);
            properties.put(Eagle200BindingConstants.THING_CONFIG_SCANFREQUENCY, 60);
            this.scancount++;
            logger.trace("Bridge {} for device {} is in state {}, scan count {}", bridgeID, thingUID,
                    this.bridgeHandler.getThing().getStatus(), this.scancount);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeID)
                    .withProperties(properties).withRepresentationProperty(meterAddress).withBridge(bridgeID)
                    .withLabel("Electricity Meter " + meterAddress).build();
            thingDiscovered(discoveryResult);
        }
    }
}
