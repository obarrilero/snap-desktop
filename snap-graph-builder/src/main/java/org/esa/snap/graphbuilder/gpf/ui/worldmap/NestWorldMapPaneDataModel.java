/*
 * Copyright (C) 2014 by Array Systems Computing Inc. http://www.array.ca
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
package org.esa.snap.graphbuilder.gpf.ui.worldmap;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerContext;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerTypeRegistry;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.Product;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;

public class NestWorldMapPaneDataModel {

    public static final String PROPERTY_LAYER = "layer";
    public static final String PROPERTY_SELECTED_PRODUCT = "selectedProduct";
    public static final String PROPERTY_PRODUCTS = "products";
    public static final String PROPERTY_ADDITIONAL_GEO_BOUNDARIES = "additionalGeoBoundaries";
    public static final String PROPERTY_SELECTED_GEO_BOUNDARIES = "selectedGeoBoundaries";
    public static final String PROPERTY_AUTO_ZOOM_ENABLED = "autoZoomEnabled";

    private PropertyChangeSupport changeSupport;
    private static final LayerType layerType = LayerTypeRegistry.getLayerType("org.esa.snap.worldmap.BlueMarbleLayerType");
    private Layer worldMapLayer;
    private Product selectedProduct;
    private boolean autoZoomEnabled;
    private ArrayList<Product> productList;
    private final ArrayList<GeoPos[]> additionalGeoBoundaryList;
    private final ArrayList<GeoPos[]> selectedGeoBoundaryList;

    private final GeoPos selectionBoxStart = new GeoPos();
    private final GeoPos selectionBoxEnd = new GeoPos();

    public NestWorldMapPaneDataModel() {
        productList = new ArrayList<>();
        additionalGeoBoundaryList = new ArrayList<>();
        selectedGeoBoundaryList = new ArrayList<>();
        autoZoomEnabled = false;
    }

    public Layer getWorldMapLayer(LayerContext context) {
        if (worldMapLayer == null) {
            worldMapLayer = layerType.createLayer(context, new PropertyContainer());
        }
        return worldMapLayer;
    }

    public Product getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(Product product) {
        Product oldSelectedProduct = selectedProduct;
        if (oldSelectedProduct != product) {
            selectedProduct = product;
            firePropertyChange(PROPERTY_SELECTED_PRODUCT, oldSelectedProduct, selectedProduct);
        }
    }

    public void setSelectionBoxStart(final float lat, final float lon) {
        selectionBoxStart.setLocation(lat, lon);
    }

    public void setSelectionBoxEnd(final float lat, final float lon) {
        selectionBoxEnd.setLocation(lat, lon);
    }

    public GeoPos[] getSelectionBox() {
        final GeoPos[] selectionBox = new GeoPos[4];
        selectionBox[0] = selectionBoxStart;
        selectionBox[1] = new GeoPos(selectionBoxStart.getLat(), selectionBoxEnd.getLon());
        selectionBox[2] = selectionBoxEnd;
        selectionBox[3] = new GeoPos(selectionBoxEnd.getLat(), selectionBoxStart.getLon());
        return selectionBox;
    }

    public Product[] getProducts() {
        return productList.toArray(new Product[productList.size()]);
    }

    public void setProducts(Product[] products) {
        final Product[] oldProducts = getProducts();
        productList.clear();
        if (products != null) {
            productList.addAll(Arrays.asList(products));
        }
        firePropertyChange(PROPERTY_PRODUCTS, oldProducts, getProducts());
    }

    public GeoPos[][] getAdditionalGeoBoundaries() {
        return additionalGeoBoundaryList.toArray(new GeoPos[additionalGeoBoundaryList.size()][]);
    }

    public void setAdditionalGeoBoundaries(GeoPos[][] geoBoundarys) {
        final GeoPos[][] oldGeoBoundarys = getAdditionalGeoBoundaries();
        additionalGeoBoundaryList.clear();
        if (geoBoundarys != null) {
            additionalGeoBoundaryList.addAll(Arrays.asList(geoBoundarys));
        }
        firePropertyChange(PROPERTY_ADDITIONAL_GEO_BOUNDARIES, oldGeoBoundarys, additionalGeoBoundaryList);
    }

    public GeoPos[][] getSelectedGeoBoundaries() {
        return selectedGeoBoundaryList.toArray(new GeoPos[selectedGeoBoundaryList.size()][]);
    }

    public void setSelectedGeoBoundaries(GeoPos[][] geoBoundarys) {
        final GeoPos[][] oldGeoBoundarys = getSelectedGeoBoundaries();
        selectedGeoBoundaryList.clear();
        if (geoBoundarys != null) {
            selectedGeoBoundaryList.addAll(Arrays.asList(geoBoundarys));
        }
        firePropertyChange(PROPERTY_SELECTED_GEO_BOUNDARIES, oldGeoBoundarys, selectedGeoBoundaryList);
    }

    public void addModelChangeListener(PropertyChangeListener listener) {
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removeModelChangeListener(PropertyChangeListener listener) {
        if (changeSupport != null) {
            changeSupport.removePropertyChangeListener(listener);
        }
    }

    public void addProduct(Product product) {
        if (!productList.contains(product)) {
            final Product[] oldProducts = getProducts();
            if (productList.add(product)) {
                firePropertyChange(PROPERTY_PRODUCTS, oldProducts, getProducts());
            }
        }
    }

    public void removeProduct(Product product) {
        if (productList.contains(product)) {
            final Product[] oldProducts = getProducts();
            if (productList.remove(product)) {
                firePropertyChange(PROPERTY_PRODUCTS, oldProducts, getProducts());
            }
        }
    }

    public boolean isAutoZommEnabled() {
        return autoZoomEnabled;
    }

    public void setAutoZoomEnabled(boolean autoZoomEnabled) {
        final boolean oldAutoZommEnabled = isAutoZommEnabled();
        if (oldAutoZommEnabled != autoZoomEnabled) {
            this.autoZoomEnabled = autoZoomEnabled;
            firePropertyChange(PROPERTY_AUTO_ZOOM_ENABLED, oldAutoZommEnabled, autoZoomEnabled);
        }
    }

    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (changeSupport != null) {
            changeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
}
