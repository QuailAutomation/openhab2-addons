/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.config;

import org.openhab.binding.zoneminder.ZoneMinderConstants;

/**
 * Specific configuration class for Monitor COnfig.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class ZoneMinderThingMonitorConfig extends ZoneMinderThingConfig {

    // Parameters
    private Integer monitorId;

    private Integer max_image_size;

    private Boolean enable_image_updates;

    private String video_encoding;

    private Integer video_framerate;

    @Override
    public String getConfigId() {
        return ZoneMinderConstants.THING_ZONEMINDER_MONITOR;
    }

    public String getId() {
        return monitorId.toString();
    }

    @Override
    public String getZoneMinderId() {
        return monitorId.toString();
    }

    public Integer getMaxImageSize() {
        return max_image_size;
    }

    public void setMaxImageSize(Integer size) {
        this.max_image_size = size;
    }

    public Boolean getEnablImageUpdates() {
        return enable_image_updates;
    }

    public void setEnableImageUpdates(Boolean enable_image_updates) {
        this.enable_image_updates = enable_image_updates;
    }

    public String getVideoEncoding() {
        return video_encoding;
    }

    public void setVideoEncoding(String video_encoding) {
        this.video_encoding = video_encoding;
    }

    public Integer getVideoFramerate() {
        return video_framerate;
    }

    public void setVideoFramerate(Integer video_framerate) {
        this.video_framerate = video_framerate;
    }

}
