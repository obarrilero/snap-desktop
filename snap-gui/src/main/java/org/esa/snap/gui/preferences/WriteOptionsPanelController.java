/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.snap.gui.preferences;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;

/**
 * Panel for write options.
 *
 * @author thomas
 */
@org.openide.util.NbBundle.Messages({
        "Options_DisplayName_WriteOptions=Write Options",
        "Options_Keywords_WriteOptions=write, writing, save, header, MPH, SPH, history, annotation, incremental"
})
@OptionsPanelController.SubRegistration(location = "Advanced",
        displayName = "#Options_DisplayName_WriteOptions",
        keywords = "#Options_Keywords_WriteOptions",
        keywordsCategory = "Write Options",
        id = "WriteOptions")
public final class WriteOptionsPanelController extends DefaultConfigController {

    /**
     * Preferences key for save product headers (MPH, SPH) or not
     */
    public static final String PROPERTY_KEY_SAVE_PRODUCT_HEADERS = "save.product.headers";
    /**
     * Preferences key for save product history or not
     */
    public static final String PROPERTY_KEY_SAVE_PRODUCT_HISTORY = "save.product.history";
    /**
     * Preferences key for save product annotations (ADS) or not
     */
    public static final String PROPERTY_KEY_SAVE_PRODUCT_ANNOTATIONS = "save.product.annotations";
    /**
     * Preferences key for incremental mode at save
     */
    public static final String PROPERTY_KEY_SAVE_INCREMENTAL = "save.incremental";

    /**
     * default value for preference save product headers (MPH, SPH) or not
     */
    public static final boolean DEFAULT_VALUE_SAVE_PRODUCT_HEADERS = true;
    /**
     * default value for preference save product history (History) or not
     */
    public static final boolean DEFAULT_VALUE_SAVE_PRODUCT_HISTORY = true;
    /**
     * default value for preference save product annotations (ADS) or not
     */
    public static final boolean DEFAULT_VALUE_SAVE_PRODUCT_ANNOTATIONS = false;
    /**
     * default value for preference incremental mode at save
     */
    public static final boolean DEFAULT_VALUE_SAVE_INCREMENTAL = true;

    protected Object createBean() {
        return new WriteOptionsBean();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("write-options");
    }

    @SuppressWarnings("UnusedDeclaration")
    static class WriteOptionsBean {

        @ConfigProperty(label = "Save product header (MPH, SPH, Global_Attributes)", key = PROPERTY_KEY_SAVE_PRODUCT_HEADERS)
        boolean saveProductHeaders = DEFAULT_VALUE_SAVE_PRODUCT_HEADERS;

        @ConfigProperty(label = "Save product history (History)", key = PROPERTY_KEY_SAVE_PRODUCT_HISTORY)
        boolean saveProductHistory = DEFAULT_VALUE_SAVE_PRODUCT_HISTORY;

        @ConfigProperty(label = "Save product annotation datasets (ADS)", key = PROPERTY_KEY_SAVE_PRODUCT_ANNOTATIONS)
        boolean saveProductAds = DEFAULT_VALUE_SAVE_PRODUCT_ANNOTATIONS;

        @ConfigProperty(label = "Use incremental save (only save modified items)", key = PROPERTY_KEY_SAVE_INCREMENTAL)
        boolean saveIncremental = DEFAULT_VALUE_SAVE_INCREMENTAL;
    }

}
