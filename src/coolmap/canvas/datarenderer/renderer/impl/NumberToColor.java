/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl;

import com.google.common.collect.Range;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.utils.CImageGradient;
import coolmap.utils.Tools;
import coolmap.utils.graphics.GradientEditorPanel;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class NumberToColor extends ViewRenderer<Double> {

    private GradientEditorPanel editor;

    private JTextField minValueField = new JTextField();
    private JTextField maxValueField = new JTextField();

    private Color normalBG = Color.WHITE;
    private Color errorBG = UI.colorRedWarning;
    private JComboBox presetRangeComboBox;
    private JComboBox presetColorComboBox;
    private JCheckBox drawSubMap;

    public static String ATTR_COLORS = "colors";
    public static String ATTR_POS = "pos";
    public static String ATTR_LOW = "low";
    public static String ATTR_HIGH = "high";

    @Override
    public JSONObject getCurrentState(){
        try {
            JSONObject state = new JSONObject();
            state.put(ATTR_LOW, _minValue);
            state.put(ATTR_HIGH, _maxValue);
            
            int numPts = editor.getNumPoints();
            float[] pos = new float[numPts];
            int[] colors = new int[numPts];
            
            for(int i=0; i<editor.getNumPoints(); i++){
                pos[i] = editor.getColorPositionAt(i);
                colors[i] = editor.getColorAt(i).getRGB();
            }
            
            state.put(ATTR_COLORS, colors);
            state.put(ATTR_POS, pos);
            
            return state;
        } catch (Exception e) {
            //log error
            System.err.println("View renderer save state exception");
            return null;
        }
    }

    @Override
    public boolean restoreState(JSONObject savedState){

        return true;
    }

    public NumberToColor() {
        setName("Number to Color");
        setDescription("Use color to represent numeric values");

        //initialize UI
        configUI.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 5;
        c.ipady = 5;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridwidth = 2;
//        c.weightx = 0.8;

        editor = new GradientEditorPanel();
        configUI.add(editor, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
//        JButton button = new JButton("Apply");
//        configUI.add(button, c);
//        button.setToolTipText("Apply preset gradient");
//
//        button.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//                try {
//                    GradientItem item = (GradientItem) presetColorComboBox.getSelectedItem();
//
//                    editor.clearColors();
//
//                    Color c[] = item.getColors();
//                    float p[] = item.getPositions();
//
//                    editor.setStart(c[0]);
//                    editor.setEnd(c[c.length - 1]);
//
//                    if (c.length > 2) {
//                        for (int i = 1; i < c.length - 1; i++) {
//                            editor.addColor(c[i], p[i]);
//                        }
//                    }
//
//                } catch (Exception ex) {
//                    editor.clearColors();
//                    editor.setStart(DEFAULT_MIN_COLOR);
//                    editor.setEnd(DEFAULT_MAX_COLOR);
//                }
//
//                updateRenderer();
//            }
//        });
        configUI.add(new JLabel("Preset palette:"), c);

        c.gridx = 1;
        c.gridwidth = 1;
        presetColorComboBox = new JComboBox();

        presetColorComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {

                try {
                    GradientItem item = (GradientItem) presetColorComboBox.getSelectedItem();
                    presetColorComboBox.setToolTipText(item.getToolTip());

                    editor.clearColors();

                    Color c[] = item.getColors();
                    float p[] = item.getPositions();

                    editor.setStart(c[0]);
                    editor.setEnd(c[c.length - 1]);

                    if (c.length > 2) {
                        for (int i = 1; i < c.length - 1; i++) {
                            editor.addColor(c[i], p[i]);
                        }
                    }

                    try {
                        Double[] ranges = item.getRanges();
                        if (ranges != null) {
                            minValueField.setText(ranges[0].toString());
                            maxValueField.setText(ranges[1].toString());
                            editor.setMinValue(ranges[0].floatValue());
                            editor.setMaxValue(ranges[1].floatValue());
                        }
                    } catch (Exception ex) {
                        minValueField.setText("-1");
                        maxValueField.setText("1");
                        editor.setMinValue(-1);
                        editor.setMaxValue(1);
                    }

                } catch (Exception ex) {
                    editor.clearColors();
                    editor.setStart(DEFAULT_MIN_COLOR);
                    editor.setEnd(DEFAULT_MAX_COLOR);
                }
            }
        });

//       
        configUI.add(presetColorComboBox, c);
        presetColorComboBox.setRenderer(new GradientComboItemRenderer());

        presetColorComboBox.addItem(
                new GradientItem(
                        new Color[]{DEFAULT_MIN_COLOR, Color.BLACK, DEFAULT_MAX_COLOR},
                        new float[]{0f, 0.5f, 1f},
                        "TBP", "Teal - Black - Pink"));

        presetColorComboBox.addItem(
                new GradientItem(
                        new Color[]{Color.GREEN, Color.RED},
                        new float[]{0f, 1f},
                        "GR", "Green - Red"));

        presetColorComboBox.addItem(
                new GradientItem(
                        new Color[]{Color.GREEN, Color.BLACK, Color.RED},
                        new float[]{0f, 0.5f, 1f},
                        "RBG", "Red - Black - Green"));

        presetColorComboBox.addItem(
                new GradientItem(
                        new Color[]{Color.ORANGE, Color.BLUE},
                        new float[]{0f, 1f},
                        "OB", "Orange - Blue"));

        presetColorComboBox.addItem(
                new GradientItem(
                        new Color[]{Color.ORANGE, Color.BLACK, Color.BLUE},
                        new float[]{0f, 0.5f, 1f},
                        "OBB", "Orange - Black - Blue"));

        presetColorComboBox.addItem(
                new GradientItem(
                        new Color[]{Color.BLACK, Color.GREEN},
                        new float[]{0f, 1f},
                        "BG", "Black - Green"));

        presetColorComboBox.addItem(
                new GradientItem(
                        new Color[]{Color.RED, UI.colorAKABENI, Color.BLACK, Color.BLACK},
                        new float[]{0f, 0.05f, 0.051f, 1f},
                        "P.05", 0d, 1d, "P-value @ 0.05, Red to Black"));

        presetColorComboBox.setToolTipText(((GradientItem) presetColorComboBox.getSelectedItem()).getToolTip());

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
//        button = new JButton("Apply");
//        
//        button.setToolTipText("Apply preset data ranges");
//        button.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    MinMaxItem item = (MinMaxItem) (presetRangeComboBox.getSelectedItem());
//                    minValueField.setText(item.getMinMax().lowerEndpoint().toString());
//                    maxValueField.setText(item.getMinMax().upperEndpoint().toString());
//                } catch (Exception ex) {
//                    minValueField.setText("-1");
//                    maxValueField.setText("1");
//                }
//
//                updateRenderer();
//            }
//        });
//        configUI.add(button, c);
        configUI.add(new JLabel("Preset range:"), c);

        c.gridx = 1;
        c.gridwidth = 1;
        presetRangeComboBox = new JComboBox();
        configUI.add(presetRangeComboBox, c);
        presetRangeComboBox.addItem(new DataMinMaxItem());
        presetRangeComboBox.addItem(new DefinedMinMaxItem(-1, 1));
        presetRangeComboBox.addItem(new DefinedMinMaxItem(0, 1));
        presetRangeComboBox.addItem(new DefinedMinMaxItem(-1, 0));
        presetRangeComboBox.addItem(new DefinedMinMaxItem(0, 100));

        presetRangeComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                try {
                    MinMaxItem item = (MinMaxItem) (presetRangeComboBox.getSelectedItem());
                    minValueField.setText(item.getMinMax().lowerEndpoint().toString());
                    maxValueField.setText(item.getMinMax().upperEndpoint().toString());
                } catch (Exception ex) {
                    minValueField.setText("-1");
                    maxValueField.setText("1");
                }
            }
        });

////////////////////////////////////////////////////////////////////////////////
//        c.weightx = 0.2;
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        configUI.add(new JLabel("Min value: "), c);
        c.gridx = 1;
//        c.weightx = 0.3;
        configUI.add(minValueField, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        configUI.add(new JLabel("Max value: "), c);
        c.gridx = 1;
        configUI.add(maxValueField, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        JLabel label = Tools.createJLabel("Draw Sub-heatmap:", null, "Draw sub-heatmaps for collapsed ontology nodes", null);
        configUI.add(label, c);

        c.gridx = 1;
        drawSubMap = new JCheckBox();
        configUI.add(drawSubMap, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;

        JButton button = new JButton("Update", UI.getImageIcon("refresh"));
        configUI.add(button, c);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //hit button, redraw!

                updateRenderer();
            }
        });

//        editor.applyButton.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                updateRenderer();
//            }
//        });
        toolTipLabel = new JLabel();
        toolTipLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        toolTipLabel.setBackground(UI.colorGrey3);
        toolTipLabel.setOpaque(true);
        toolTipLabel.setForeground(UI.colorBlack2);
        toolTipLabel.setFont(UI.fontPlain.deriveFont(12f));
    }

    @Override
    public void updateRendererChanges() {

//        System.err.println("Renderer changes updated" + getCoolMapObject());
//        System.out.println("===Update renderer changes called===");
        if (getCoolMapObject() == null) {
            return;
        }

        //update min max
        try {
            _minValue = Double.parseDouble(minValueField.getText());
            minValueField.setBackground(normalBG);
        } catch (Exception e) {

            minValueField.setBackground(errorBG);
        }

        try {
            _maxValue = Double.parseDouble(maxValueField.getText());
            maxValueField.setBackground(normalBG);
        } catch (Exception e) {

            maxValueField.setBackground(errorBG);
        }

        editor.setMinValue(new Float(_minValue));
        editor.setMaxValue(new Float(_maxValue));

        updateGradient();

    }

    private void updateGradient() {
//        System.out.println("Gradient updated..");
        _gradient.reset();
        for (int i = 0; i < editor.getNumPoints(); i++) {
            Color c = editor.getColorAt(i);
            float p = editor.getColorPositionAt(i);

            if (c == null || p < 0 || p > 1) {
                continue;
            }

            _gradient.addColor(c, p);
        }

        _gradientColors = _gradient.generateGradient(CImageGradient.InterType.Linear);

        int width = DEFAULT_LEGEND_WIDTH;
        int height = DEFAULT_LEGENT_HEIGHT;
        legend = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        Graphics2D g = (Graphics2D) legend.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        LinearGradientPaint paint = editor.getLinearGradientPaint(0, 0, width, 0);

        g.setPaint(paint);
        g.fillRoundRect(0, 0, width, height - 12, 5, 5);

        g.setColor(UI.colorBlack2);
        g.setFont(UI.fontMono.deriveFont(10f));
        DecimalFormat format = new DecimalFormat("#.##");
        g.drawString(format.format(_minValue), 2, 23);

        String maxString = format.format(_maxValue);
        int swidth = g.getFontMetrics().stringWidth(maxString);
        g.drawString(maxString, width - 2 - swidth, 23);
        g.dispose();

//        System.out.println("===Gradient updated===" + _gradientColors + " " + this);
    }

    private BufferedImage legend;

    @Override
    public Image getLegend() {
        return legend;
    }

    private double _minValue = 0;
    private double _maxValue = 1;
    private static Color DEFAULT_MIN_COLOR = new Color(127, 205, 187);
    private static Color DEFAULT_MAX_COLOR = new Color(252, 146, 114);
    private CImageGradient _gradient = new CImageGradient(10000);
    private Color[] _gradientColors = null;

    @Override
    protected void initialize() {
//        System.out.println("===Number to color initialized===");

        CoolMapObject obj = getCoolMapObject();
        if (!canRender(obj.getViewClass())) {
            return;
        }

        double minValue = Double.MAX_VALUE;
        double maxValue = -Double.MAX_VALUE;

        for (int i = 0; i < obj.getViewNumRows(); i++) {
            for (int j = 0; j < obj.getViewNumColumns(); j++) {
                try {
                    Double v = (Double) obj.getViewValue(i, j);
                    if (v == null || v.isNaN()) {
                        continue;
                    } else {
                        if (v < minValue) {
                            minValue = v;
                        }
                        if (v > maxValue) {
                            maxValue = v;
                        }
                    }
                } catch (Exception e) {

                }
            }
        }

        minValueField.setText(minValue + "");
        maxValueField.setText(maxValue + "");

        editor.clearColors();

        editor.setStart(DEFAULT_MIN_COLOR);
        editor.addColor(Color.BLACK, 0.5f);
        editor.setEnd(DEFAULT_MAX_COLOR);

//        System.err.println("Number to color initialized");
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
    public void preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    public void prepareGraphics(Graphics2D g2D) {
        g2D.setFont(UI.fontMono.deriveFont(12f));
    }

    @Override
    public void renderCellLD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
//        renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            try {
                int index = (int) ((v - _minValue) / (_maxValue - _minValue) * _gradientColors.length);
                if (index >= _gradientColors.length) {
                    index = _gradientColors.length - 1;
                }
                if (index < 0) {
                    index = 0;
                }
                Color c = _gradientColors[index];
                //System.out.println(c);
                g2D.setColor(c);
//                System.out.println((int) cellWidth + " " + ((int) cellHeight)) ;
                g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
            } catch (Exception e) {

            }
        }

    }

    @Override
    public void renderCellSD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            try {
                int index = (int) ((v - _minValue) / (_maxValue - _minValue) * _gradientColors.length);
                if (index >= _gradientColors.length) {
                    index = _gradientColors.length - 1;
                }
                if (index < 0) {
                    index = 0;
                }
                Color c = _gradientColors[index];
                //System.out.println(c);
                g2D.setColor(c);
//                System.out.println((int) cellWidth + " " + ((int) cellHeight)) ;
                g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
//                System.out.println("WH:" + cellWidth + " " + cellHeight);

                if (drawSubMap.isSelected()) {
                    //paint the sub heatmap block

//                    System.out.println("draw submap");
                    if (rowNode == null || columnNode == null || rowNode.isSingleNode() && columnNode.isSingleNode()) {
                        return;
                    } else {

                        //need to determine the number of cMatrices
                        CoolMapObject object = getCoolMapObject();
                        List<CMatrix> matrices = object.getBaseCMatrices();
                        Double value;

                        //row and column indices
                        Integer[] rowIndices;
                        Integer[] colIndices;

                        //whether they are group node or node
                        if (rowNode.isGroupNode()) {
                            rowIndices = rowNode.getBaseIndicesFromCOntology((CMatrix) object.getBaseCMatrices().get(0), COntology.ROW);
                        } else {
                            rowIndices = new Integer[]{((CMatrix) object.getBaseCMatrices().get(0)).getIndexOfRowName(rowNode.getName())};
                        }
                        if (columnNode.isGroupNode()) {
                            colIndices = columnNode.getBaseIndicesFromCOntology((CMatrix) object.getBaseCMatrices().get(0), COntology.COLUMN);
                        } else {
                            colIndices = new Integer[]{((CMatrix) object.getBaseCMatrices().get(0)).getIndexOfColName(columnNode.getName())};
                        }

                        //then determine the width
                        int subMatrixWidth = Math.round(cellWidth * 1.0f / matrices.size());

                        int subAnchorX = anchorX;

                        int subCellHeight = Math.round(cellHeight * 1.0f / rowIndices.length);
                        int subCellWidth = Math.round(subMatrixWidth * 1.0f / colIndices.length);

//                        System.out.println("CW:" + cellWidth + " " + colIndices.length + " " + subCellWidth);
//                        System.out.println("CH:" + cellHeight + " " + rowIndices.length + " " + subCellHeight);
                        if (subCellHeight < 1) {
                            subCellHeight = 1;
                        }
                        if (subCellWidth < 1) {
                            subCellWidth = 1;
                        }

                        for (CMatrix matrix : matrices) {

                            int rowIndex = 0;
                            for (Integer i : rowIndices) {
                                if (i == null || i < 0) {
                                    continue;
                                }
                                int columnIndex = 0;
                                for (Integer j : colIndices) {
                                    if (j == null || j < 0) {
                                        continue;
                                    }
                                    try {

                                        value = (Double) matrix.getValue(i, j);
                                        index = (int) ((value - _minValue) / (_maxValue - _minValue) * _gradientColors.length);
                                        if (index >= _gradientColors.length) {
                                            index = _gradientColors.length - 1;
                                        }
                                        if (index < 0) {
                                            index = 0;
                                        }
                                        c = _gradientColors[index];

                                        g2D.setColor(c);

                                        int subCellX = Math.round(subMatrixWidth * 1.0f * columnIndex / colIndices.length) + subAnchorX;
                                        int subCellY = Math.round(cellHeight * 1.0f * rowIndex / rowIndices.length) + anchorY;

//                                        System.out.println("WTF:" + columnIndex + " " + rowIndex + "----" + subCellX + " " + subCellY + " " + subCellWidth + " " + subCellHeight);
                                        g2D.fillRect(subCellX, subCellY, subCellWidth, subCellHeight);

                                    } catch (Exception e) {
                                        //draw it here
                                        e.printStackTrace();
                                    }

                                    columnIndex++;
                                }//end of column loop

                                rowIndex++;
                            }//end of row loop

                            subAnchorX += subMatrixWidth;
                        }//end of matrix loop

                        g2D.setStroke(UI.stroke1);
                        g2D.setColor(Color.BLACK);
                        g2D.drawRect(anchorX, anchorY, cellWidth, cellHeight);
                    }
                }

            } catch (Exception e) {
//                System.out.println("Null pointer exception:" + v + "," + _minValue + "," + _maxValue + "," + _gradientColors + " " + getName() + "" + this);
                //e.printStackTrace();
            }
        }
    }

    @Override
    public Image getSubTip(CoolMapObject object, VNode rowNode, VNode columnNode, float percentX, float PercentY, int cellWidth, int cellHeight) {

        try {
            if (!drawSubMap.isSelected()) {
                return null;
            }

            if (rowNode == null || columnNode == null || rowNode.isSingleNode() && columnNode.isSingleNode()) {
                return null;
            }

            List<CMatrix> matrices = object.getBaseCMatrices();

            //row and column indices
            Integer[] rowIndices;
            Integer[] colIndices;

            //whether they are group node or node
            if (rowNode.isGroupNode()) {
                rowIndices = rowNode.getBaseIndicesFromCOntology((CMatrix) object.getBaseCMatrices().get(0), COntology.ROW);
            } else {
                rowIndices = new Integer[]{((CMatrix) object.getBaseCMatrices().get(0)).getIndexOfRowName(rowNode.getName())};
            }

            if (columnNode.isGroupNode()) {
                colIndices = columnNode.getBaseIndicesFromCOntology((CMatrix) object.getBaseCMatrices().get(0), COntology.COLUMN);
            } else {
                colIndices = new Integer[]{((CMatrix) object.getBaseCMatrices().get(0)).getIndexOfColName(columnNode.getName())};
            }

            //determine where it is in the map
            int matrixIndex = (int) (percentX * matrices.size());

            int rowIndex = (int) (PercentY * rowIndices.length);
            int colIndex = (int) ((cellWidth * percentX - matrixIndex * 1.0 / matrices.size() * cellWidth) / (cellWidth / matrices.size()) * colIndices.length);

//        System.out.println(rowIndex + " " + colIndex + " " + percentX + " " + matrixIndex + " " + colIndices.length);
            if (rowIndex < 0) {
                rowIndex = 0;
            }
            if (colIndex < 0) {
                colIndex = 0;
            }

            if (rowIndex >= rowIndices.length) {
                rowIndex = rowIndices.length - 1;
            }

            if (colIndex >= colIndices.length) {
                colIndex = colIndices.length - 1;
            }

            int rowBaseIndex = rowIndices[rowIndex];
            int colBaseIndex = colIndices[colIndex];

//        if(rowBaseIndex < 0 )rowBaseIndex = 0;
//        if(colBaseIndex < 0 )colBaseIndex = 0;
//        
//        if(rowBaseIndex >= rowIndices.length) rowBaseIndex = rowIndices.length - 1;
//        if(colBaseIndex >= colIndices.length) colBaseIndex = colIndices.length - 1;
            CMatrix matrix = matrices.get(matrixIndex);
            Double value = (Double) matrix.getValue(rowBaseIndex, colBaseIndex);

            String rowLabel = matrix.getRowLabel(rowBaseIndex);
            String colLabel = matrix.getColLabel(colBaseIndex);

//        System.out.println(matrixIndex + " " + rowBaseIndex + " " + colBaseIndex + " " + rowLabel + " -- " + colLabel + " -- " + value);
            toolTipLabel.setText("<html><table cellspacing='1' border='0' cellpadding='1'>"
                    + ((matrices.size() > 1) ? "<tr><td><strong>Data: </strong></td><td>" + matrices.get(matrixIndex).getName() + "</td></tr>" : "")
                    + "<tr><td><strong>Row: </strong></td><td>" + rowLabel + "</td></tr><tr><td><strong>Column: </strong></td><td>" + colLabel + "</td></tr><tr><td><strong>Value: </strong></td><td><span style='color:#020202;font-weight:bold;'>" + df.format(value) + "</span></td></tr></table></html>");

            return createToolTipFromJLabel(toolTipLabel);
        } catch (Exception e) {
            return null;
        }

    }

    private DecimalFormat df = new DecimalFormat("#.###");
    private JLabel toolTipLabel;

    @Override
    public void renderCellHD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            try {
                int index = (int) ((v - _minValue) / (_maxValue - _minValue) * _gradientColors.length);
                if (index >= _gradientColors.length) {
                    index = _gradientColors.length - 1;
                }
                if (index < 0) {
                    index = 0;
                }
                Color c = _gradientColors[index];
                //System.out.println(c);
                g2D.setColor(c);
//                System.out.println((int) cellWidth + " " + ((int) cellHeight)) ;
                g2D.fillRoundRect((int) anchorX + 1, (int) anchorY + 1, (int) cellWidth - 2, (int) cellHeight - 2, 4, 4);

                if (drawSubMap.isSelected()) {
                    //paint the sub heatmap block

//                    System.out.println("draw submap");
                    if (rowNode == null || columnNode == null || rowNode.isSingleNode() && columnNode.isSingleNode()) {
                        return;
                    } else {

                        g2D.setStroke(UI.strokeDash1_5);
                        //need to determine the number of cMatrices
                        CoolMapObject object = getCoolMapObject();
                        List<CMatrix> matrices = object.getBaseCMatrices();
                        Double value;

                        //row and column indices
                        Integer[] rowIndices;
                        Integer[] colIndices;

                        //whether they are group node or node
                        if (rowNode.isGroupNode()) {
                            rowIndices = rowNode.getBaseIndicesFromCOntology((CMatrix) object.getBaseCMatrices().get(0), COntology.ROW);
                        } else {
                            rowIndices = new Integer[]{((CMatrix) object.getBaseCMatrices().get(0)).getIndexOfRowName(rowNode.getName())};
                        }
                        if (columnNode.isGroupNode()) {
                            colIndices = columnNode.getBaseIndicesFromCOntology((CMatrix) object.getBaseCMatrices().get(0), COntology.COLUMN);
                        } else {
                            colIndices = new Integer[]{((CMatrix) object.getBaseCMatrices().get(0)).getIndexOfColName(columnNode.getName())};
                        }

                        //then determine the width
                        int subMatrixWidth = Math.round(cellWidth * 1.0f / matrices.size());

                        int subAnchorX = anchorX;

                        int subCellHeight = Math.round(cellHeight * 1.0f / rowIndices.length);
                        int subCellWidth = Math.round(subMatrixWidth * 1.0f / colIndices.length);

//                        System.out.println("CW:" + cellWidth + " " + colIndices.length + " " + subCellWidth);
//                        System.out.println("CH:" + cellHeight + " " + rowIndices.length + " " + subCellHeight);
                        if (subCellHeight < 1) {
                            subCellHeight = 1;
                        }
                        if (subCellWidth < 1) {
                            subCellWidth = 1;
                        }

                        int matrixIndex = 0;
                        for (CMatrix matrix : matrices) {

                            int rowIndex = 0;
                            for (Integer i : rowIndices) {
                                if (i == null || i < 0) {
                                    continue;
                                }
                                int columnIndex = 0;
                                for (Integer j : colIndices) {
                                    if (j == null || j < 0) {
                                        continue;
                                    }
                                    try {

                                        value = (Double) matrix.getValue(i, j);
                                        index = (int) ((value - _minValue) / (_maxValue - _minValue) * _gradientColors.length);
                                        if (index >= _gradientColors.length) {
                                            index = _gradientColors.length - 1;
                                        }
                                        if (index < 0) {
                                            index = 0;
                                        }
                                        c = _gradientColors[index];

                                        g2D.setColor(c);

                                        int subCellX = Math.round(subMatrixWidth * 1.0f * columnIndex / colIndices.length) + subAnchorX;
                                        int subCellY = Math.round(cellHeight * 1.0f * rowIndex / rowIndices.length) + anchorY;

//                                        System.out.println("WTF:" + columnIndex + " " + rowIndex + "----" + subCellX + " " + subCellY + " " + subCellWidth + " " + subCellHeight);
                                        g2D.fillRect(subCellX, subCellY, subCellWidth, subCellHeight);

                                    } catch (Exception e) {
                                        //draw it here
                                        e.printStackTrace();
                                    }

                                    columnIndex++;
                                }//end of column loop

                                rowIndex++;
                            }//end of row loop

                            g2D.setColor(UI.colorBlack1);
                            g2D.drawRect(subAnchorX, anchorY, subMatrixWidth, cellHeight);

                            //
//                            subAnchorX += subMatrixWidth;
                            matrixIndex++;
                            subAnchorX = anchorX + Math.round(cellWidth * 1.0f * matrixIndex / matrices.size());
                        }//end of matrix loop

                        g2D.setStroke(UI.stroke2);
                        g2D.setColor(Color.BLACK);
                        g2D.drawRect(anchorX, anchorY, cellWidth, cellHeight);
                    }
                }

            } catch (Exception e) {
//                System.out.println("Null pointer exception:" + v + "," + _minValue + "," + _maxValue + "," + _gradientColors + " " + getName() + "" + this);
                //e.printStackTrace();

            }
        }
    }

//    DecimalFormat df = new DecimalFormat("#.##");
    @Override
    public void postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    private JPanel configUI = new JPanel();

    @Override
    public JComponent getConfigUI() {
        return configUI;
    }

    private class DataMinMaxItem extends MinMaxItem {

        @Override
        public Range<Double> getMinMax() {

            CoolMapObject obj = getCoolMapObject();
            if (!canRender(obj.getViewClass())) {
                return null;
            }

            double minValue = Double.MAX_VALUE;
            double maxValue = -Double.MAX_VALUE;

            try {
                for (int i = 0; i < obj.getViewNumRows(); i++) {
                    for (int j = 0; j < obj.getViewNumColumns(); j++) {
                        try {
                            Double v = (Double) obj.getViewValue(i, j);
                            if (v == null || v.isNaN()) {
                                continue;
                            } else {
                                if (v < minValue) {
                                    minValue = v;
                                }
                                if (v > maxValue) {
                                    maxValue = v;
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                }
                return Range.closed(minValue, maxValue);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return "View min - max";
        }

    }

    private class DefinedMinMaxItem extends MinMaxItem {

        private double min;
        private double max;

        public DefinedMinMaxItem(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public Range<Double> getMinMax() {
            return Range.closed(min, max);
        }

    }

    private abstract class MinMaxItem {

        public abstract Range<Double> getMinMax();

        @Override
        public String toString() {
            Range<Double> range = getMinMax();
            return range.lowerEndpoint() + " - " + range.upperEndpoint();
        }
    }

    private class GradientItem {

        private final Color[] c;
        private final float[] pos;
        private final BufferedImage preview;
        private final String name;
        private final String toolTip;

//        @Override
//        public String toString(){
//            return getToolTip();
//        }
        public String getToolTip() {
            return toolTip;
        }

        public GradientItem(Color[] c, float[] pos, String name, Double low, Double high, String toolTip) {
            this.c = c;
            this.pos = pos;

            //update preview
            preview = new BufferedImage(100, 16, BufferedImage.TYPE_INT_ARGB);
            this.name = name;

            LinearGradientPaint paint = new LinearGradientPaint(0, 0, 100, 0, pos, c);
            Graphics2D g2D = preview.createGraphics();
            g2D.setPaint(paint);
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2D.fillRoundRect(2, 2, 90, 12, 4, 4);

            g2D.dispose();

            this.low = low;
            this.high = high;
            this.toolTip = toolTip;
        }

        public GradientItem(Color[] c, float[] pos, String name, String toolTip) {
            this(c, pos, name, null, null, toolTip);
        }

        public Image getPreview() {
            return preview;
        }

        @Override
        public String toString() {
            return name + getToolTip();//To change body of generated methods, choose Tools | Templates.
        }

        public Color[] getColors() {
            return c;
        }

        public float[] getPositions() {
            return pos;

        }

        public Double[] getRanges() {
            if (low == null || high == null) {
                return null;
            }
            return new Double[]{low, high};
        }

        private Double low = null;
        private Double high = null;

    }

    private class GradientComboItemRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            l.setIcon(new ImageIcon(((GradientItem) value).getPreview()));
            l.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            l.setText(((GradientItem) value).name);
            return l;
        }

    }

}
