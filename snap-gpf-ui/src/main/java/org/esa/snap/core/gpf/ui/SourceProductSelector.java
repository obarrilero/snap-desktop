/*
 * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package org.esa.snap.core.gpf.ui;

import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.selection.SelectionChangeListener;
import com.bc.ceres.swing.selection.support.ComboBoxSelectionContext;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.dataio.ProductIOPlugInManager;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductFilter;
import org.esa.snap.core.datamodel.ProductManager;
import org.esa.snap.core.datamodel.ProductNode;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.esa.snap.rcp.actions.file.OpenProductAction;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.SnapFileChooser;
import org.openide.util.Utilities;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

/**
 * WARNING: This class belongs to a preliminary API and may change in future releases.
 * todo - add capability to select/add/remove multiple soures (nf - 27.11.2007)
 * todo - add capability to specify optional sources
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
public class SourceProductSelector {

    private AppContext appContext;
    private ProductFilter productFilter;
    private Product extraProduct;
    private File currentDirectory;
    private DefaultComboBoxModel<Object> productListModel;
    private JLabel productNameLabel;
    private JButton productFileChooserButton;
    private JComboBox<Object> productNameComboBox;
    private final ProductManager.Listener productManagerListener;
    private ComboBoxSelectionContext selectionContext;
    private boolean enableEmptySelection;

    public SourceProductSelector(AppContext appContext) {
        this(appContext, false);
    }

    public SourceProductSelector(AppContext appContext, boolean enableEmptySelection) {
        this(appContext, "Name:", enableEmptySelection);
    }

    public SourceProductSelector(AppContext appContext, String labelText) {
        this(appContext, labelText, false);
    }

    public SourceProductSelector(AppContext appContext, String labelText, boolean enableEmptySelection) {
        this.appContext = appContext;
        this.enableEmptySelection = enableEmptySelection;

        productListModel = new DefaultComboBoxModel<>();

        productNameLabel = new JLabel(labelText);
        productFileChooserButton = new JButton(new ProductFileChooserAction());
        final Dimension size = new Dimension(26, 16);
        productFileChooserButton.setPreferredSize(size);
        productFileChooserButton.setMinimumSize(size);

        productNameComboBox = new JComboBox<>(productListModel);
        productNameComboBox.setPrototypeDisplayValue("[1] 123456789 123456789 12345");
        productNameComboBox.setRenderer(new ProductListCellRenderer());
        productNameComboBox.addPopupMenuListener(new ProductPopupMenuListener());
        productNameComboBox.addActionListener(e -> {
            final Object selected = productNameComboBox.getSelectedItem();
            if (selected != null && selected instanceof Product) {
                Product product = (Product) selected;
                if (product.getFileLocation() != null) {
                    productNameComboBox.setToolTipText(product.getFileLocation().getPath());
                } else {
                    productNameComboBox.setToolTipText(product.getDisplayName());
                }
            } else {
                productNameComboBox.setToolTipText("Select a source product.");
            }
        });

        productFilter = ProductFilter.ALL;
        selectionContext = new ComboBoxSelectionContext(productNameComboBox);

        productManagerListener = new ProductManager.Listener() {
            @Override
            public void productAdded(ProductManager.Event event) {
                addProduct(event.getProduct());
            }

            @Override
            public void productRemoved(ProductManager.Event event) {
                Product product = event.getProduct();
                if (productListModel.getSelectedItem() == product) {
                    productListModel.setSelectedItem(null);
                }
                productListModel.removeElement(product);
            }
        };
    }

    /**
     * @return the product filter, default is a filter which accepts all products
     */
    public ProductFilter getProductFilter() {
        return productFilter;
    }

    /**
     * @param productFilter the product filter
     */
    public void setProductFilter(ProductFilter productFilter) {
        this.productFilter = productFilter;
    }

    public synchronized void initProducts() {
        productListModel.removeAllElements();
        if (enableEmptySelection) {
            productListModel.addElement(null);
        }
        for (Product product : appContext.getProductManager().getProducts()) {
            addProduct(product);
        }

        Product selectedProduct = appContext.getSelectedProduct();
        final ProductNode productNode = Utilities.actionsGlobalContext().lookup(ProductNode.class);
        if (productNode != null) {
            // user would want to apply operation to the selected productNode rather than the productSceneView
            selectedProduct = productNode.getProduct();
        }

        if (selectedProduct != null && productFilter.accept(selectedProduct)) {
            productListModel.setSelectedItem(selectedProduct);
        }
        appContext.getProductManager().addListener(productManagerListener);
    }

    public int getProductCount() {
        if (enableEmptySelection) {
            return productListModel.getSize() - 1;
        } else {
            return productListModel.getSize();
        }
    }

    public void setSelectedIndex(int index) {
        productListModel.setSelectedItem(productListModel.getElementAt(index));
    }

    public Product getSelectedProduct() {
        return (Product) productListModel.getSelectedItem();
    }

    public void setCurrentDirectory(File directory) {
        if (directory != null && directory.isDirectory()) {
            currentDirectory = directory;
        }
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setSelectedProduct(Product product) {
        if (product == null) {
            productListModel.setSelectedItem(null);
            return;
        }
        if (productListModelContains(product)) {
            productListModel.setSelectedItem(product);
        } else {
            if (productFilter.accept(product)) {
                if (extraProduct != null) {
                    productListModel.removeElement(extraProduct);
                    extraProduct.dispose();
                }
                productListModel.addElement(product);
                productListModel.setSelectedItem(product);
                extraProduct = product;
            }
        }
    }

    public synchronized void releaseProducts() {
        appContext.getProductManager().removeListener(productManagerListener);
        if (extraProduct != null && getSelectedProduct() != extraProduct) {
            extraProduct.dispose();
        }
        extraProduct = null;
        productListModel.removeAllElements();
    }

    public void addSelectionChangeListener(SelectionChangeListener listener) {
        selectionContext.addSelectionChangeListener(listener);
    }

    public void removeSelectionChangeListener(SelectionChangeListener listener) {
        selectionContext.removeSelectionChangeListener(listener);
    }

    private void addProduct(Product product) {
        if (productFilter.accept(product)) {
            productListModel.addElement(product);
        }
    }

    // UI Components

    /////////////////////////////////////

    public JComboBox<Object> getProductNameComboBox() {
        return productNameComboBox;
    }

    public JLabel getProductNameLabel() {
        return productNameLabel;
    }

    public JButton getProductFileChooserButton() {
        return productFileChooserButton;
    }

    private boolean productListModelContains(Product product) {
        for (int i = 0; i < productListModel.getSize(); i++) {
            Object listProduct = productListModel.getElementAt(i);
            if (listProduct == null) {
                continue;
            }
            if (listProduct.equals(product)) {
                return true;
            }
        }
        return false;
    }

    public JPanel createDefaultPanel() {
        return createDefaultPanel("Source Product");
    }

    public JPanel createDefaultPanel(String borderTitle) {
        final JPanel subPanel = new JPanel(new BorderLayout(3, 3));
        subPanel.add(getProductNameComboBox(), BorderLayout.CENTER);
        subPanel.add(getProductFileChooserButton(), BorderLayout.EAST);

        final TableLayout tableLayout = new TableLayout(1);
        tableLayout.setTableAnchor(TableLayout.Anchor.WEST);
        tableLayout.setTableWeightX(1.0);
        tableLayout.setRowFill(0, TableLayout.Fill.HORIZONTAL);
        tableLayout.setRowFill(1, TableLayout.Fill.HORIZONTAL);
        tableLayout.setTablePadding(3, 3);
        JPanel panel = new JPanel(tableLayout);
        panel.add(getProductNameLabel());
        panel.add(subPanel);
        if (StringUtils.isNotNullAndNotEmpty(borderTitle)) {
            panel.setBorder(BorderFactory.createTitledBorder(borderTitle));
            panel.add(tableLayout.createVerticalSpacer());
        }
        return panel;
    }

    private class ProductFileChooserAction extends AbstractAction {

        private String APPROVE_BUTTON_TEXT = "Select";
        private JFileChooser chooser;

        private ProductFileChooserAction() {
            super("...");
            chooser = new SnapFileChooser();
            chooser.setDialogTitle("Select Source Product");
            final Iterator<ProductReaderPlugIn> iterator = ProductIOPlugInManager.getInstance().getAllReaderPlugIns();
            List<SnapFileFilter> sortedFileFilters = SnapFileFilter.getSortedFileFilters(iterator);
            for (SnapFileFilter fileFilter : sortedFileFilters) {
                chooser.addChoosableFileFilter(fileFilter);
            }
            chooser.setAcceptAllFileFilterUsed(true);
            chooser.setFileFilter(chooser.getAcceptAllFileFilter());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            final Window window = SwingUtilities.getWindowAncestor((JComponent) event.getSource());

            String homeDirPath = SystemUtils.getUserHomeDir().getPath();
            String openDir = appContext.getPreferences().getPropertyString(OpenProductAction.PREFERENCES_KEY_LAST_PRODUCT_DIR,
                                                                           homeDirPath);
            currentDirectory = new File(openDir);
            chooser.setCurrentDirectory(currentDirectory);

            if (chooser.showDialog(window, APPROVE_BUTTON_TEXT) == JFileChooser.APPROVE_OPTION) {
                final File file = chooser.getSelectedFile();

                Product product = null;
                try {
                    product = ProductIO.readProduct(file);
                    if (product == null) {
                        throw new IOException(MessageFormat.format("File ''{0}'' could not be read.", file.getPath()));
                    }

                    if (productFilter.accept(product)) {
                        setSelectedProduct(product);
                    } else {
                        final String message = String.format("Product [%s] is not a valid source.",
                                                             product.getFileLocation().getCanonicalPath());
                        handleError(window, message);
                        product.dispose();
                    }
                } catch (IOException e) {
                    handleError(window, e.getMessage());
                } catch (Exception e) {
                    if (product != null) {
                        product.dispose();
                    }
                    handleError(window, e.getMessage());
                    e.printStackTrace();
                }
                currentDirectory = chooser.getCurrentDirectory();
                appContext.getPreferences().setPropertyString(OpenProductAction.PREFERENCES_KEY_LAST_PRODUCT_DIR,
                                                              currentDirectory.getAbsolutePath());
            }
        }

        private void handleError(final Component component, final String message) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(component, message, "Error",
                                                                           JOptionPane.ERROR_MESSAGE));
        }
    }

    private static class ProductListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            final Component cellRendererComponent =
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (cellRendererComponent instanceof JLabel) {
                final JLabel label = (JLabel) cellRendererComponent;
                if (value instanceof Product) {
                    final Product product = (Product) value;
                    label.setText(product.getDisplayName());
                } else {
                    label.setText(" ");
                }
            }

            return cellRendererComponent;
        }
    }

    /**
     * To let the popup menu be wider than the closed combobox.
     * Adapted an idea from http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6257236
     */
    private static class ProductPopupMenuListener implements PopupMenuListener {

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            JComboBox box = (JComboBox) e.getSource();
            Object comp = box.getUI().getAccessibleChild(box, 0);
            if (!(comp instanceof JPopupMenu)) {
                return;
            }
            JComponent scrollPane = (JComponent) ((JPopupMenu) comp).getComponent(0);
            Dimension size = new Dimension();
            size.width = scrollPane.getPreferredSize().width;
            final int boxItemCount = box.getModel().getSize();
            for (int i = 0; i < boxItemCount; i++) {
                final JLabel label = new JLabel();
                Object elementAt = box.getModel().getElementAt(i);
                if (elementAt != null && elementAt instanceof Product) {
                    label.setText(((Product) elementAt).getDisplayName());
                }
                size.width = Math.max(label.getPreferredSize().width, size.width);
            }
            size.height = scrollPane.getPreferredSize().height;
            scrollPane.setPreferredSize(size);
            scrollPane.setMaximumSize(size);
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }
    }
}
