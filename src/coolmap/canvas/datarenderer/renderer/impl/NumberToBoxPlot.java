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
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
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
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

/**
 *
 * @author sugang
 */
public class NumberToBoxPlot extends ViewRenderer<Double> {

    private JTextField minValueField = new JTextField();
    private JTextField maxValueField = new JTextField();
    private JTextField disectField = new JTextField();

    private Color normalBG = Color.WHITE;
    private Color errorBG = UI.colorRedWarning;
    private JComboBox presetRangeComboBox;

    public NumberToBoxPlot() {
        setName("Number to BoxPlot");
        setDescription("Use bar height to represent numeric values");

        //initialize UI
        configUI.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 5;
        c.ipady = 5;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridwidth = 1;

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        JButton button = new JButton("Apply");
        configUI.add(button, c);
        button.setToolTipText("Apply preset data ranges");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    MinMaxItem item = (MinMaxItem) (presetRangeComboBox.getSelectedItem());
                    minValueField.setText(item.getMinMax().lowerEndpoint().toString());
                    maxValueField.setText(item.getMinMax().upperEndpoint().toString());
                } catch (Exception ex) {
                    minValueField.setText("-1");
                    maxValueField.setText("1");
                }

                updateRenderer();
            }
        });

        c.gridx = 1;
        c.gridwidth = 1;
        presetRangeComboBox = new JComboBox();
        configUI.add(presetRangeComboBox, c);
        presetRangeComboBox.addItem(new DataMinMaxItem());
        presetRangeComboBox.addItem(new DefinedMinMaxItem(-1, 1));
        presetRangeComboBox.addItem(new DefinedMinMaxItem(0, 1));
        presetRangeComboBox.addItem(new DefinedMinMaxItem(-1, 0));
        presetRangeComboBox.addItem(new DefinedMinMaxItem(0, 100));

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
        configUI.add(new JLabel("Max valuee: "), c);
        c.gridx = 1;
        configUI.add(maxValueField, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        configUI.add(new JLabel("Disect boundary: "), c);
        c.gridx = 1;
        configUI.add(disectField, c);
        disectField.setText("0");

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;

        button = new JButton("Update", UI.getImageIcon("refresh"));
        configUI.add(button, c);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //hit button, redraw!

                updateRenderer();
            }
        });

    }

    private double disectBound = 0;

    @Override
    protected void updateRendererChanges() {

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

        try {
            disectBound = Double.parseDouble(disectField.getText());
            disectField.setBackground(normalBG);
        } catch (Exception e) {
            disectField.setBackground(errorBG);
        }

        updateLegend();

    }

    private void updateLegend() {

        int width = DEFAULT_LEGEND_WIDTH;
        int height = DEFAULT_LEGENT_HEIGHT;
        legend = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        Graphics2D g = (Graphics2D) legend.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setPaint(UI.colorBlack2);
        g.fillRoundRect(0, 0, width, height - 12, 5, 5);

        g.setColor(barColorBelow);
        int boxNum = 10;
        g.setStroke(UI.stroke1_5);

        double value;
        for (int i = 0; i < boxNum; i++) {
            value = _minValue + (_maxValue - _minValue) / boxNum * i;

            if (value > disectBound) {
                g.setColor(barColorNormal);
            }

            int h = (height - 12) / boxNum * i;
            g.drawLine(i * width / boxNum, height - 12 - h, (i + 1) * width / boxNum, height - 12 - h);

        }

        g.setColor(Color.BLACK);
        g.setFont(UI.fontMono.deriveFont(10f));
        DecimalFormat format = new DecimalFormat("#.##");
        g.drawString(format.format(_minValue), 2, 23);

        g.setColor(Color.BLACK);
        String maxString = format.format(_maxValue);
        int swidth = g.getFontMetrics().stringWidth(maxString);
        g.drawString(maxString, width - 2 - swidth, 23);
        g.dispose();
    }

    private BufferedImage legend;

    @Override
    public Image getLegend() {
        return legend;
    }

    private double _minValue = 0;
    private double _maxValue = 1;
    private Color[] _gradientColors = null;

    @Override
    protected void initialize() {
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
    protected void _preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    protected void _prepareGraphics(Graphics2D g2D) {
//        g2D.setFont(UI.fontMono.deriveFont(12f));
//        g2D.setColor(UI.colorLightYellow);
    }

    @Override
    protected void _renderCellLD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            try {
                g2D.setColor(UI.colorBlack2);
                g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
                g2D.setColor(barColorNormal);
                g2D.setStroke(UI.stroke1_5);

                //This is the 
                //int height = (int)Math.round(cellHeight * (v - _minValue)/(_maxValue - _minValue));
                //g2D.fillRect(Math.round(anchorX), Math.round(anchorY + cellHeight - height), Math.round(cellWidth), Math.round(cellHeight));
                if (rowNode.isSingleNode() && columnNode.isSingleNode()) {

                    double value = (v - _minValue) / (_maxValue - _minValue);
                    
                    if (v >= disectBound) {
                        g2D.setColor(barColorNormal);
                    } else {
                        g2D.setColor(barColorBelow);
                    }

                    g2D.drawLine((int) (anchorX + 1), (int) (anchorY + cellHeight - cellHeight * value), (int) (anchorX + cellWidth - 1), (int) (anchorY + cellHeight - cellHeight * value));
                } else {

//                    double min = percentile.evaluate(valueArray, 0);
//                    double max = percentile.evaluate(valueArray, 100)
                    double fiveVal[] = boxPlotValues(getCoolMapObject(), rowNode, columnNode);
                    if (fiveVal == null) {
                        g2D.setColor(UI.colorBlack1);
                        g2D.drawRect(Math.round(anchorX), Math.round(anchorY), Math.round(cellWidth), Math.round(cellHeight));
                    }

                    double range = _maxValue - _minValue;
                    double minP = (fiveVal[0] - _minValue) / range;
                    double maxP = (fiveVal[4] - _minValue) / range;
                    double medianP = (fiveVal[2] - _minValue) / range;
                    double q1P = (fiveVal[1] - _minValue) / range;
                    double q3P = (fiveVal[3] - _minValue) / range;

                    try {
//                        if (cellWidth >= 2 && cellHeight >= 2) {
                            g2D.drawLine((int) (anchorX + cellWidth / 2), (int) (anchorY + cellHeight - cellHeight * maxP), (int) (anchorX + cellWidth / 2), (int) (anchorY + cellHeight - cellHeight * minP));

                            if (fiveVal[2] >= disectBound) {
                                g2D.setColor(UI.colorLightGreen4);
                            } else {
                                g2D.setColor(UI.colorOrange2);
                            }

                            g2D.fillRect((int) (anchorX), (int) (anchorY + cellHeight - cellHeight * q3P), (int) (cellWidth), (int) (cellHeight * (q3P - q1P)));

                            if (fiveVal[2] >= disectBound) {
                                g2D.setColor(barColorNormal);
                            } else {
                                g2D.setColor(barColorBelow);
                            }

//                        g2D.setColor(barColorNormal);
                            //g2D.drawRect((int) (anchorX), (int) (anchorY + cellHeight - cellHeight * q3P), (int) (cellWidth), (int) (cellHeight * (q3P - q1P)));
                            g2D.drawLine((int) (anchorX), (int) (anchorY + cellHeight - cellHeight * medianP), (int) (anchorX + cellWidth), (int) (anchorY + cellHeight - cellHeight * medianP));
//                        } else {
//
//                            if (fiveVal[2] >= medianP) {
//                                g2D.setColor(barColorNormal);
//                            } else {
//                                g2D.setColor(barColorBelow);
//                            }
//
////                            System.out.println("painted rect");
////                            System.out.println((int) cellWidth + " " + ((int) cellHeight));
//                            g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
//                        }

                    } catch (Exception e) {
                        System.err.println("Boxplot render exception");
                    }
                }

//                if(cellWidth>=4 && cellHeight >=){
//                    g2D.setColor(UI.colorBlack1);
//                    g2D.drawRect(Math.round(anchorX), Math.round(anchorY), Math.round(cellWidth), Math.round(cellHeight));
//                }
            } catch (Exception e) {
                System.out.println("Null pointer exception:" + v + "," + _minValue + "," + _maxValue + "," + _gradientColors);
                e.printStackTrace();
            }
        }
    }

    @Override
    public Image getSubTip(CoolMapObject object, VNode rowNode, VNode columnNode, float percentX, float PercentY, int cellWidth, int cellHeight) {

        try {
            if (rowNode == lastRowNode && columnNode == lastColumnNode) {
                return subTip; //Still in the same cell
            } else {
                updateSubTip(object, rowNode, columnNode);
            }
        } catch (Exception e) {
            updateSubTip(object, rowNode, columnNode);
        }

        return subTip;
    }

    DecimalFormat df = new DecimalFormat("#.##");

    private void updateSubTip(CoolMapObject object, VNode rowNode, VNode columnNode) {
        //must get all the values
        double[] fiveNum = boxPlotValues(object, rowNode, columnNode);
        if (fiveNum == null) {
            subTip = null;
            return;
        }

        String tip = "Five Numbers:\n";
        tip += "[" + df.format(fiveNum[0]) + "," + df.format(fiveNum[1]) + "," + df.format(fiveNum[2]) + "," + df.format(fiveNum[3]) + "," + df.format(fiveNum[4]) + "]";

        subTip = ViewRenderer.createToolTipFromString(tip, UI.fontMono.deriveFont(12f).deriveFont(Font.BOLD));

    }

//    private MatrixCell lastActivreCell = null;
    private VNode lastRowNode = null;
    private VNode lastColumnNode = null;

    private BufferedImage subTip = null;

    private Color barColorNormal = UI.colorLightGreen0;
    private Color barColorBelow = UI.colorOrange0;

    //need to extract the function to find the 5 points
    private double[] boxPlotValues(CoolMapObject object, VNode rowNode, VNode columnNode) {
        if (rowNode == null || columnNode == null || rowNode.isSingleNode() && columnNode.isSingleNode()) {
            return null;
        } else {

            Integer[] rowIndices;
            Integer[] colIndices;
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

            //A box plot across all matrices
            List<CMatrix> matrices = object.getBaseCMatrices();
            Double value;
            ArrayList<Double> values = new ArrayList<Double>();

            //add values
            for (Integer i : rowIndices) {
                if (i == null || i < 0) {
                    continue;
                }

                for (Integer j : colIndices) {

                    if (j == null || j < 0) {
                        continue;
                    }
                    //Double value = (Double) getCoolMapObject().getViewValue(i, j);
                    //This is wrong. it should eb the base matrix value, not the view values
                    for (CMatrix<Double> matrix : matrices) {

                        value = matrix.getValue(i, j);

                        if (value == null || value.isNaN()) {
                            continue;
                        } else {
                            //System.out.println(i + " " + j + " " + v);
                            values.add(value);
                        }
                    }

                }
            }

            if (values.isEmpty()) {
                return null;
            }

            Collections.sort(values);
            int size = values.size();
            double min = values.get(0);
            double max = values.get(values.size() - 1);

            double median;
            if (size % 2 == 0) {
                median = (values.get(size / 2) + values.get(size / 2 - 1)) / 2;
            } else {
                median = (values.get(size / 2));
            }
            double[] valueArray = new double[values.size()];
            int c = 0;
            for (Double d : values) {
                valueArray[c++] = d.doubleValue();
            }

            Arrays.sort(valueArray);

            Percentile percentile = new Percentile();
            double q1 = percentile.evaluate(valueArray, 25);
            double q3 = percentile.evaluate(valueArray, 75);
//            double median = percentile.evaluate(valueArray, 50);
            return new double[]{min, q1, median, q3, max};

        }
    }

    @Override
    protected void _renderCellSD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            try {
                g2D.setColor(UI.colorBlack2);
                g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
                g2D.setColor(barColorNormal);

//                g2D.setStroke(null);
                g2D.setStroke(UI.stroke1_5);

                //This is the 
                //int height = (int)Math.round(cellHeight * (v - _minValue)/(_maxValue - _minValue));
                //g2D.fillRect(Math.round(anchorX), Math.round(anchorY + cellHeight - height), Math.round(cellWidth), Math.round(cellHeight));
                if (rowNode.isSingleNode() && columnNode.isSingleNode()) {

                    double medianP = (v - _minValue) / (_maxValue - _minValue);
                    if (v >= disectBound) {
                        g2D.setColor(barColorNormal);
                    } else {
                        g2D.setColor(barColorBelow);

                    }

                    g2D.drawLine((int) (anchorX + 1), (int) (anchorY + cellHeight - cellHeight * medianP), (int) (anchorX + cellWidth - 1), (int) (anchorY + cellHeight - cellHeight * medianP));
                } else {

//                    double min = percentile.evaluate(valueArray, 0);
//                    double max = percentile.evaluate(valueArray, 100)
                    double fiveVal[] = boxPlotValues(getCoolMapObject(), rowNode, columnNode);
                    if (fiveVal == null) {
                        g2D.setColor(UI.colorBlack1);
                        g2D.drawRect(Math.round(anchorX), Math.round(anchorY), Math.round(cellWidth), Math.round(cellHeight));
                    }

                    double range = _maxValue - _minValue;
                    double minP = (fiveVal[0] - _minValue) / range;
                    double maxP = (fiveVal[4] - _minValue) / range;
                    double medianP = (fiveVal[2] - _minValue) / range;
                    double q1P = (fiveVal[1] - _minValue) / range;
                    double q3P = (fiveVal[3] - _minValue) / range;

                    try {
                        g2D.drawLine((int) (anchorX + cellWidth / 2), (int) (anchorY + cellHeight - cellHeight * maxP), (int) (anchorX + cellWidth / 2), (int) (anchorY + cellHeight - cellHeight * minP));

                        if (fiveVal[2] >= disectBound) {
                            g2D.setColor(UI.colorLightGreen4);
                        } else {
                            g2D.setColor(UI.colorOrange2);
                        }

                        g2D.fillRect((int) (anchorX + cellWidth / 4), (int) (anchorY + cellHeight - cellHeight * q3P), (int) (cellWidth / 2), (int) (cellHeight * (q3P - q1P)));

                        if (fiveVal[2] >= disectBound) {
                            g2D.setColor(barColorNormal);
                        } else {
                            g2D.setColor(barColorBelow);
                        }

//                        g2D.setColor(barColorNormal);
                        g2D.drawRect((int) (anchorX + cellWidth / 4), (int) (anchorY + cellHeight - cellHeight * q3P), (int) (cellWidth / 2), (int) (cellHeight * (q3P - q1P)));

                        g2D.drawLine((int) (anchorX + 1), (int) (anchorY + cellHeight - cellHeight * medianP), (int) (anchorX + cellWidth - 1), (int) (anchorY + cellHeight - cellHeight * medianP));
                    } catch (Exception e) {
                        System.err.println("Boxplot render exception");
                    }
                }

                g2D.setColor(UI.colorBlack1);
                g2D.drawRect(Math.round(anchorX), Math.round(anchorY), Math.round(cellWidth), Math.round(cellHeight));
            } catch (Exception e) {
                System.out.println("Null pointer exception:" + v + "," + _minValue + "," + _maxValue + "," + _gradientColors);
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void _renderCellHD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        _renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);

//        g2D.setColor(Color.BLACK);
//        g2D.drawString(df.format(v), anchorX, anchorY + cellHeight);
    }

//    DecimalFormat df = new DecimalFormat("#.##");
    @Override
    protected void _postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
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

        public GradientItem(Color[] c, float[] pos, String name) {
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

        }

        public Image getPreview() {
            return preview;
        }

        @Override
        public String toString() {
            return name;//To change body of generated methods, choose Tools | Templates.
        }

        public Color[] getColors() {
            return c;
        }

        public float[] getPositions() {
            return pos;

        }

    }

    private class GradientComboItemRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            l.setIcon(new ImageIcon(((GradientItem) value).getPreview()));
            l.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            return l;
        }

    }
}
