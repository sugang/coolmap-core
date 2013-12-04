/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.Config;
import coolmap.utils.graphics.UI;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
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

    private JLabel singleLegend;
    private JLabel rowLegend;
    private JLabel columnLegend;
    private JLabel rowColumnLegend;

    private BufferedImage legend = null;

    @Override
    public Image getLegend() {
        //To change body of generated methods, choose Tools | Templates.
        return legend;
    }

    private void updateLegend() {
        try {
            ArrayList<Image> legends = new ArrayList<Image>(4);
            if (singleRenderer != null && singleRenderer.getLegend() != null) {
                legends.add(singleRenderer.getLegend());
            }
            if (rowGroupRenderer != null && rowGroupRenderer.getLegend() != null) {
                legends.add(rowGroupRenderer.getLegend());
            }
            if (columnGroupRenderer != null && columnGroupRenderer.getLegend() != null) {
                legends.add(columnGroupRenderer.getLegend());
            }
            if (rowColumnGroupRenderer != null && rowColumnGroupRenderer.getLegend() != null) {
                legends.add(rowColumnGroupRenderer.getLegend());
            }

            if (!legends.isEmpty()) {
                int margin = 5;
                int imageWidth = 0;
                int imageHeight = 0;
                for (Image l : legends) {
                    imageHeight += margin * 2 + l.getHeight(null);
                    if (imageWidth < l.getWidth(null)) {
                        imageWidth = l.getWidth(null);
                    }
                }

                imageWidth += margin * 2;

                if (imageWidth > 0 && imageHeight > 0) {
                    legend = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(imageWidth, imageHeight, Transparency.TRANSLUCENT);
                    Graphics2D g2D = legend.createGraphics();
                    g2D.translate(margin, 0);
                    for (Image l : legends) {
                        g2D.translate(0, margin);
                        g2D.drawImage(l, 0, 0, null);
                        g2D.translate(0, margin + l.getHeight(null));
                    }
                    g2D.dispose();
                }

            }
        } catch (Exception e) {

        }
    }

    public NumberComposite() {

        setName("Number to Composite");
//        System.out.println("Created a new NumberComposite");
        setDescription("A renderer that can be used to assign renderers to different aggregations");

        configUI.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 5;
        c.ipady = 5;
        c.insets = new Insets(2, 2, 2, 2);
        c.gridwidth = 1;

        //This combo box will need to be able to add registered
        singleComboBox = new JComboBox();
        rowComboBox = new JComboBox();
        columnComboBox = new JComboBox();
        rowColumnComboBox = new JComboBox();

        singleLegend = new JLabel();
        rowLegend = new JLabel();
        columnLegend = new JLabel();
        rowColumnLegend = new JLabel();

        singleLegend.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        rowLegend.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        columnLegend.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        rowColumnLegend.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        //Add them
        JLabel label = new JLabel("Default:");
        label.setToolTipText("Default renderer");
        c.gridx = 0;
        c.gridy++;
        configUI.add(label, c);
        c.gridx = 1;
        configUI.add(singleComboBox, c);
        singleComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateRenderer();
                }
            }
        });

        c.gridx = 2;
        JButton config = new JButton(UI.getImageIcon("gear"));
        configUI.add(config, c);
        config.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (singleRenderer == null) {
                    return;
                }
//                JOptionPane.showmess
                int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), singleRenderer.getConfigUI(), "Default Renderer Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                if (returnVal == JOptionPane.OK_OPTION) {
                    updateRenderer();
                }
            }
        });

        c.gridx = 1;
        c.gridy++;
        c.gridwidth = 1;
        configUI.add(singleLegend, c);

//////////////////////////////////////////////////////////////
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        label = new JLabel("Row Group:");
        label.setToolTipText("Renderer for row ontology nodes");
        configUI.add(label, c);
        c.gridx = 1;
        configUI.add(rowComboBox, c);
        c.gridx++;
        config = new JButton(UI.getImageIcon("gear"));
        configUI.add(config, c);
        config.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (rowGroupRenderer == null) {
                    return;
                }
//                JOptionPane.showmess
                int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), rowGroupRenderer.getConfigUI(), "Row Group Renderer Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                if (returnVal == JOptionPane.OK_OPTION) {
                    updateRenderer();
                }

            }
        });
        rowComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateRenderer();
                }
            }
        });

        c.gridx = 1;
        c.gridy++;
        c.gridwidth = 1;
        configUI.add(rowLegend, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        label = new JLabel("Column Group:");
        label.setToolTipText("Renderer for column ontology nodes");
        configUI.add(label, c);
        c.gridx = 1;
        configUI.add(columnComboBox, c);
        c.gridx++;
        config = new JButton(UI.getImageIcon("gear"));
        configUI.add(config, c);
        config.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (columnGroupRenderer == null) {
                    return;
                }
//                JOptionPane.showmess
                int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), columnGroupRenderer.getConfigUI(), "Column Group Renderer Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                if (returnVal == JOptionPane.OK_OPTION) {
                    updateRenderer();
                }
            }
        });

        c.gridx = 1;
        c.gridy++;
        c.gridwidth = 1;
        configUI.add(columnLegend, c);
        columnComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateRenderer();
                }
            }
        });

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        label = new JLabel("Row & Column Group:");
        label.setToolTipText("Renderer for row and column ontology nodes");
        configUI.add(label, c);
        c.gridx = 1;
        configUI.add(rowColumnComboBox, c);
        c.gridx++;
        config = new JButton(UI.getImageIcon("gear"));
        configUI.add(config, c);
        config.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (rowColumnGroupRenderer == null) {
                    return;
                }
//                JOptionPane.showmess
                int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), rowColumnGroupRenderer.getConfigUI(), "Row + Column Group Renderer Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                if (returnVal == JOptionPane.OK_OPTION) {
                    updateRenderer();
                }
            }
        });
        rowColumnComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateRenderer();
                }
            }
        });

        c.gridx = 1;
        c.gridy++;
        c.gridwidth = 1;
        configUI.add(rowColumnLegend, c);

        JButton button = new JButton("Apply Changes", UI.getImageIcon("refresh"));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateRenderer();
            }
        });

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        configUI.add(button, c);

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
            if (o == null || !(o instanceof ViewRenderer)) {
//                label.setForeground(UI.colorLightRed);
                return label;
            }
            if (getCoolMapObject() != null) {
//                label.setForeground(UI.colorBlack1);
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

        rowComboBox.addItem("No renderer");
        columnComboBox.addItem("No renderer");
        rowColumnComboBox.addItem("No renderer");
//        rowComboBox.addItem(new Object());

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
//                        System.out.println("parsing error");
                        CMConsole.logError("Error: failed to load built in renderers to composite color renderer.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//            singleComboBox.setSelectedIndex(1);
//            rowComboBox.setSelectedIndex(2);
//
//            //problem is at column??
//            columnComboBox.setSelectedIndex(3);
//            rowColumnComboBox.setSelectedIndex(0);
        }
    }
    private JPanel configUI = new JPanel();

    @Override
    public JComponent getConfigUI() {
//        return configUI;
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

        updateLegend();
    }

    @Override
    protected void initialize() {
        CoolMapObject obj = getCoolMapObject();
//        why this is initlaized twice?
//        System.out.println("===initialize composite renderer===");
        if (!canRender(obj.getViewClass())) {
            return;
        }

        updateRenderer();
    }

    @Override
    public void updateRenderer() {

        try {

            if (singleRenderer == null || singleRenderer.getClass() != singleComboBox.getSelectedItem().getClass()) {
                singleRenderer = (ViewRenderer<Double>) singleComboBox.getSelectedItem().getClass().newInstance();
                singleRenderer.setCoolMapObject(getCoolMapObject(), true); //also set parent object and initialize
//            singleRenderer.setName("Single ++");
            }

        } catch (Exception e) {
            singleRenderer = null;
        }

        try {
            if (rowGroupRenderer == null || rowGroupRenderer.getClass() != rowComboBox.getSelectedItem().getClass()) {
                rowGroupRenderer = (ViewRenderer<Double>) rowComboBox.getSelectedItem().getClass().newInstance();
                rowGroupRenderer.setCoolMapObject(getCoolMapObject(), true); //also set parent object and initialize
//            rowGroupRenderer.setName("Row group ++");
            }
        } catch (Exception e) {
            rowGroupRenderer = null;
        }

        try {
            if (columnGroupRenderer == null || columnGroupRenderer.getClass() != columnComboBox.getSelectedItem().getClass()) {
                columnGroupRenderer = (ViewRenderer<Double>) columnComboBox.getSelectedItem().getClass().newInstance();
                columnGroupRenderer.setCoolMapObject(getCoolMapObject(), true); //also set parent object and initialize
//            columnGroupRenderer.setName("Col group ++");
            }
        } catch (Exception e) {
            columnGroupRenderer = null;
        }

        try {
            if (rowColumnGroupRenderer == null || rowColumnGroupRenderer.getClass() != rowColumnComboBox.getSelectedItem().getClass()) {
                rowColumnGroupRenderer = (ViewRenderer<Double>) rowColumnComboBox.getSelectedItem().getClass().newInstance();
                rowColumnGroupRenderer.setCoolMapObject(getCoolMapObject(), true); //also set parent object and initialize
            }
//            rowColumnGroupRenderer.setName("Row + Column group");

        } catch (Exception e) {
            rowColumnGroupRenderer = null;
        }

        super.updateRenderer(); //To change body of generated methods, choose Tools | Templates.

        //update legend
        if (singleRenderer != null) {
            singleLegend.setIcon(new ImageIcon(singleRenderer.getLegend()));
        } else {
            singleLegend.setIcon(null);
        }

        if (rowGroupRenderer != null) {
            rowLegend.setIcon(new ImageIcon(this.rowGroupRenderer.getLegend()));
        } else {
            rowLegend.setIcon(null);
        }

        if (columnGroupRenderer != null) {
            columnLegend.setIcon(new ImageIcon(this.columnGroupRenderer.getLegend()));
        } else {
            columnLegend.setIcon(null);
        }

        if (rowColumnGroupRenderer != null) {
            rowColumnLegend.setIcon(new ImageIcon(this.rowColumnGroupRenderer.getLegend()));
        } else {
            rowColumnLegend.setIcon(null);
        }

        getConfigUI().repaint();
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
    public void preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
        if (singleRenderer != null) {
            singleRenderer.preRender(fromRow, toRow, fromCol, toCol, zoomX, zoomY);
        }

        if (rowGroupRenderer != null) {
            rowGroupRenderer.preRender(fromRow, toRow, fromCol, toCol, zoomX, zoomY);
        }

        if (columnGroupRenderer != null) {
            columnGroupRenderer.preRender(fromRow, toRow, fromCol, toCol, zoomX, zoomY);
        }

        if (rowColumnGroupRenderer != null) {
            rowColumnGroupRenderer.preRender(fromRow, toRow, fromCol, toCol, zoomX, zoomY);
        }
    }

    @Override
    public void prepareGraphics(Graphics2D g2D) {
        if (singleRenderer != null) {
            singleRenderer.prepareGraphics(g2D);
        }

        if (rowGroupRenderer != null) {
            rowGroupRenderer.prepareGraphics(g2D);
        }

        if (columnGroupRenderer != null) {
            columnGroupRenderer.prepareGraphics(g2D);
        }

        if (rowColumnGroupRenderer != null) {
            rowColumnGroupRenderer.prepareGraphics(g2D);
        }
    }

    @Override
    public void renderCellLD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || rowNode == null || columnNode == null) {
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
//            if (rowNode.isSingleNode() && columnNode.isSingleNode()) {
//                //single | single
//                try {
//                    singleRenderer.renderCellLD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
//                } catch (Exception e) {
//
//                }
//            } else if (rowNode.isSingleNode() && columnNode.isGroupNode()) {
//                //single | group
//                try {
//                    columnGroupRenderer.renderCellLD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
//                } catch (Exception e) {
//
//                }
//            } else if (rowNode.isGroupNode() && columnNode.isSingleNode()) {
//                //group | single
//                try {
//                    rowGroupRenderer.renderCellLD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
//                } catch (Exception e) {
//
//                }
//            } else {
//                //both group
//                try {
//                    rowColumnGroupRenderer.renderCellLD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
//                } catch (Exception e) {
//
//                }
//            }
            ViewRenderer<Double> renderer = assignRenderer(rowNode, columnNode);
            try {
                renderer.renderCellLD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
            } catch (Exception e) {

            }

        }
    }

    private ViewRenderer<Double> assignRenderer(VNode rowNode, VNode columnNode) {
        ViewRenderer<Double> currentRenderer = singleRenderer;
        if (rowNode.isGroupNode() && columnNode.isGroupNode()) {
            if (rowColumnGroupRenderer != null) {
                currentRenderer = rowColumnGroupRenderer;
            } else if (columnGroupRenderer != null) {
                currentRenderer = columnGroupRenderer;
            } else if (rowGroupRenderer != null) {
                currentRenderer = rowGroupRenderer;
            }
        } else if (rowNode.isGroupNode() && columnNode.isSingleNode()) {
            if (rowGroupRenderer != null) {
                currentRenderer = rowGroupRenderer;
            }
        } else if (columnNode.isGroupNode() && rowNode.isSingleNode()) {
            if (columnGroupRenderer != null) {
                currentRenderer = columnGroupRenderer;
            }
        }
        return currentRenderer;
    }

    @Override
    public Image getSubTip(CoolMapObject object, VNode rowNode, VNode colNode, float percentX, float PercentY, int cellWidth, int cellHeight) {
//        return super.getSubTip(object, rowNode, colNode, percentX, PercentY, cellWidth, cellHeight); //To change body of generated methods, choose Tools | Templates.
        try {
            ViewRenderer renderer = assignRenderer(rowNode, colNode);
            return renderer.getSubTip(object, rowNode, colNode, percentX, PercentY, cellWidth, cellHeight);
        } catch (Exception e) {
            return null;
        }
    }

    //
    @Override
    public void renderCellSD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || rowNode == null || columnNode == null) {
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {

            ViewRenderer<Double> renderer = assignRenderer(rowNode, columnNode);
            try {
                renderer.renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
            } catch (Exception e) {

            }

//            if (rowNode.isSingleNode() && columnNode.isSingleNode()) {
//                //single | single
//                try {
//                    singleRenderer.renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
//                } catch (Exception e) {
//
//                }
//            } else if (rowNode.isSingleNode() && columnNode.isGroupNode()) {
//                //single | group
//                try {
//                    columnGroupRenderer.renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
//                } catch (Exception e) {
//
//                }
//            } else if (rowNode.isGroupNode() && columnNode.isSingleNode()) {
//                //group | single
//                try {
//                    rowGroupRenderer.renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
//                } catch (Exception e) {
//
//                }
//            } else {
//                //both group
//                try {
//                    rowColumnGroupRenderer.renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
//                } catch (Exception e) {
//
//                }
//            }
        }
    }//

    @Override
    public void renderCellHD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || rowNode == null || columnNode == null) {
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            ViewRenderer<Double> renderer = assignRenderer(rowNode, columnNode);
            try {
                renderer.renderCellHD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
        if (singleRenderer != null) {
            singleRenderer.postRender(fromRow, toRow, fromCol, toCol, zoomX, zoomY);
        }

        if (rowGroupRenderer != null) {
            rowGroupRenderer.postRender(fromRow, toRow, fromCol, toCol, zoomX, zoomY);
        }

        if (columnGroupRenderer != null) {
            columnGroupRenderer.postRender(fromRow, toRow, fromCol, toCol, zoomX, zoomY);
        }

        if (rowColumnGroupRenderer != null) {
            rowColumnGroupRenderer.postRender(fromRow, toRow, fromCol, toCol, zoomX, zoomY);
        }
    }

}
