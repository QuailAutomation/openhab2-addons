/**
 *
 */
package org.openhab.binding.enphaseenvoy.protocol;

/**
 * @author thomashentschel
 *
 */
public class InverterProduction {
    public String serialNumber = "";
    public long lastReportDate = 0;
    public int devType = 0;
    public int lastReportWatts = 0;
    public int maxReportWatts = 0;
}
