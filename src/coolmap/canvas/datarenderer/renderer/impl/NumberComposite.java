/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl;

import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.Config;
import coolmap.utils.graphics.UI;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author sugang
 */
public class NumberComposite extends ViewRenderer<Double> {

    private JComboBox singleComboBox, rowComboBox, columnComboBox, rowColumnComboBox;
    private ViewRenderer<Double> rowGroupRenderer;
    private ViewRenderer<Double> columnGroupRenderer;
    private ViewRenderer<Double> rowColumnGroupRenderer;
    private ViewRenderer<Double> singleRenderer;

    public NumberComposite() {

        setName("What the fuck man");
        System.out.println("Created a new NumberComposite");
        setDescription("A renderer that can be used to assign renderers to different aggregations");

        configUI.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 5;
        c.ipady = 5;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridwidth = 1;

        //This combo box will need to be able to add registered
        singleComboBox = new JComboBox<ViewRenderer<Double>>();
        rowComboBox = new JComboBox<ViewRenderer<Double>>();
        columnComboBox = new JComboBox<ViewRenderer<Double>>();
        rowColumnComboBox = new JComboBox<ViewRenderer<Double>>();

        //Add them
        c.gridx = 0;
        c.gridy++;
        configUI.add(new JLabel("Default:"), c);
        c.gridx = 1;
        configUI.add(singleComboBox, c);

        c.gridx = 0;
        c.gridy++;
        configUI.add(new JLabel("Row Group:"), c);
        c.gridx = 1;
        configUI.add(rowComboBox, c);

        c.gridx = 0;
        c.gridy++;
        configUI.add(new JLabel("Column Group"), c);
        c.gridx = 1;
        configUI.add(columnComboBox, c);

        c.gridx = 0;
        c.gridy++;
        configUI.add(new JLabel("Row & Column Group"), c);
        c.gridx = 1;
        configUI.add(rowColumnComboBox, c);

        singleComboBox.setRenderer(new ComboRenderer());
        rowComboBox.setRenderer(new ComboRenderer());
        columnComboBox.setRenderer(new ComboRenderer());
        rowColumnComboBox.setRenderer(new ComboRenderer());

        _updateLists();

    }

    private class ComboRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
            JLabel label = (JLabel) super.getListCellRendererComponent(jlist, o, i, bln, bln1);
            if (o == null) {
                return label;
            }
            if (getCoolMapObject() != null) {
                CoolMapObject obj = getCoolMapObject();
                ViewRenderer renderer = (ViewRenderer) o;
                if (renderer != null && obj != null && renderer.canRender(obj.getViewClass())) {
                    label.setEnabled(true);
                    label.setFocusable(true);
                } else {
                    label.setEnabled(false);
                    label.setFocusable(false);
                    label.setBackground(UI.colorRedWarning);
                }
            }
            try {
                String displayName = ((ViewRenderer) o).getName();
                label.setText(displayName);
            } catch (Exception e) {
                label.setText(o.getClass().getSimpleName());
            }

            return label;

        }

    }
    
    private void _updateLists() {
        if (Config.isInitialized()) {
            try {

                JSONArray rendererToLoad = Config.getJSONConfig().getJSONObject("widget").getJSONObject("config").getJSONObject("coolmap.application.widget.impl.WidgetViewRenderer").getJSONArray("load");
                for (int i = 0; i < rendererToLoad.length(); i++) {
                    try {
                        String rendererClass = rendererToLoad.getString(i);
//                        System.err.println(rendererClass);
//                        registerViewRenderer(rendererClass);
                        Class cls = Class.forName(rendererClass);
                        if (cls == this.getClass()) {
                            continue;
                        } else {
                            singleComboBox.addItem(cls.newInstance());
                            rowColumnComboBox.addItem(cls.newInstance());
                            columnComboBox.addItem(cls.newInstance());
                            rowComboBox.addItem(cls.newInstance());
                        }

                    } catch (JSONException exception) {
                        System.out.println("parsing error");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            
            singleComboBox.setSelectedIndex(1);
            rowComboBox.setSelectedIndex(2);
            
            //problem is at column??
            columnComboBox.setSelectedIndex(3);
            rowColumnComboBox.setSelectedIndex(0);
        }
    }
    private JPanel configUI = new JPanel();

    @Override
    public JComponent getConfigUI() {
        return configUI;
    }

    @Override
    public void updateRendererChanges() {
        //it could be slow as it needs to update multiple times?
        if (rowGroupRenderer != null) {
            rowGroupRenderer.updateRendererChanges();
        }

        if (columnGroupRenderer != null) {
//            System.out.println("=====column group updated=====");
            columnGroupRenderer.updateRendererChanges();
        }

        if (rowColumnGroupRenderer != null) {
//            System.out.println("=====row column group updated=====");
            rowColumnGroupRenderer.updateRendererChanges();
        }

        if (singleRenderer != null) {
            singleRenderer.updateRendererChanges();
        }
    }

    @Override
    protected void initialize() {
        CoolMapObject obj = getCoolMapObject();
        System.err.println("The coolMap object is:" + obj);
//        why this is initlaized twice?
//        System.out.println("===initialize composite renderer===");
        if (!canRender(obj.getViewClass())) {
            return;
        }

        try {
            singleRenderer = (ViewRenderer<Double>) singleComboBox.getSelectedItem().getClass().newInstance();
            singleRenderer.setCoolMapObject(getCoolMapObject(), true); //also set parent object and initialize
//            singleRenderer.setName("Single ++");

        } catch (Exception e) {
            System.err.println("single renderer fail");
        }

        try {
            rowGroupRenderer = (ViewRenderer<Double>) rowComboBox.getSelectedItem().getClass().newInstance();
            rowGroupRenderer.setCoolMapObject(getCoolMapObject(), true); //also set parent object and initialize
//            rowGroupRenderer.setName("Row group ++");

        } catch (Exception e) {
            System.err.println("row group renderer fail");
        }

        try {
            
            columnGroupRenderer = (ViewRenderer<Double>) columnComboBox.getSelectedItem().getClass().newInstance();
            columnGroupRenderer.setCoolMapObject(getCoolMapObject(), true); //also set parent object and initialize
//            columnGroupRenderer.setName("Col group ++");

        } catch (Exception e) {
            System.err.println("column group renderer fail");
        }

        try {
            rowColumnGroupRenderer = (ViewRenderer<Double>) rowColumnComboBox.getSelectedItem().getClass().newInstance();
            rowColumnGroupRenderer.setCoolMapObject(getCoolMapObject(), true); //also set parent object and initialize
//            rowColumnGroupRenderer.setName("Row + Column group");

        } catch (Exception e) {
            System.err.println("row + column group renderer fail");
        }

        updateRenderer();
    }

    @Override
    public boolean canRender(Class<?> viewClass) {
        try {
            return Double.class.isAssignableFrom(viewClass);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    protected void prepareGraphics(Graphics2D g2D) {
    }

    @Override
    public void renderCellLD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
    }

    //
    @Override
    public void renderCellSD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || rowNode == null || columnNode == null) {
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            if (rowNode.isSingleNode() && columnNode.isSingleNode()) {
                //single | single
                try {
                    singleRenderer.renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                } catch (Exception e) {

                }
            } else if (rowNode.isSingleNode() && columnNode.isGroupNode()) {
                //single | group
                try {
                    columnGroupRenderer.renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                } catch (Exception e) {

                }
            } else if (rowNode.isGroupNode() && columnNode.isSingleNode()) {
                //group | single
                try {
                    rowGroupRenderer.renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                } catch (Exception e) {

                }
            } else {
                //both group
                try {
                    rowColumnGroupRenderer.renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                } catch (Exception e) {

                }
            }

        }
    }//

    @Override
    public void renderCellHD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
    }

    @Override
    protected void postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

}
