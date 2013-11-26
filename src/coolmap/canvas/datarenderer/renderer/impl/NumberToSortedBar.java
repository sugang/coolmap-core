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
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author sugang
 */
public class NumberToSortedBar extends ViewRenderer<Double> {

    private JTextField minValueField = new JTextField();
    private JTextField maxValueField = new JTextField();

    private Color normalBG = Color.WHITE;
    private Color errorBG = UI.colorRedWarning;
    private JComboBox presetRangeComboBox;

    private double _minValue = 0;
    private double _maxValue = 1;

    private JPanel configUI = new JPanel();

    private JLabel lowColorLabel, highColorLabel;

    private Color lowColor = UI.colorTOKIWA;
    private Color highColor = UI.colorKARAKURENAI;

    private class ColorLabel extends JLabel {

        public ColorLabel(Color col) {
            super("     ");
            setBackground(col);
            //setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }

        @Override
        protected void paintComponent(Graphics g) {
            //To change body of generated methods, choose Tools | Templates.
            Graphics2D g2D = (Graphics2D) g;
            g2D.setColor(getBackground());
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2D.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
            super.paintComponent(g);
//            g2D.setColor(UI.colorBlack4);
//            g2D.setStroke(UI.stroke1_5);
//            g2D.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 5, 5);
        }

    }

    private final JLabel toolTipLabel;

    public NumberToSortedBar() {

        toolTipLabel = new JLabel();
        toolTipLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        toolTipLabel.setBackground(UI.colorGrey3);
        toolTipLabel.setOpaque(true);
        toolTipLabel.setForeground(UI.colorBlack2);
        toolTipLabel.setFont(UI.fontPlain.deriveFont(12f));

        setName("Number to Sorted Bar");
        setDescription("Use bar height to represent numeric values, with all member pairs sorted by value");

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
        c.gridx = 2;
        lowColorLabel = new ColorLabel(lowColor);
        configUI.add(lowColorLabel, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        configUI.add(new JLabel("Max value: "), c);
        c.gridx = 1;
        configUI.add(maxValueField, c);
        c.gridx = 2;
        highColorLabel = new ColorLabel(highColor);
        configUI.add(highColorLabel, c);

//        c.gridx = 0;
//        c.gridy++;
//        c.gridwidth = 1;
//        configUI.add(new JLabel("Disect boundary: "), c);
//        c.gridx = 1;
//        configUI.add(disectField, c);
//        disectField.setText("0");
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

    @Override
    public JComponent getConfigUI() {
        return configUI;
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

    @Override
    public void updateRendererChanges() {
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

//        try {
//            disectBound = Double.parseDouble(disectField.getText());
//            disectField.setBackground(normalBG);
//        } catch (Exception e) {
//            disectField.setBackground(errorBG);
//        }
        lastRowNode = null;
        lastColumnNode = null;
        updateLegend();
    }

    @Override
    public Image getLegend() {
        return legend;
    }

    private BufferedImage legend;

    private CImageGradient _gradient = new CImageGradient(10000);
    private Color[] _gradientColors = null;

    private void updateLegend() {

        _gradient.reset();
        _gradient.addColor(lowColorLabel.getBackground(), 0f);
        _gradient.addColor(highColorLabel.getBackground(), 1f);

        _gradientColors = _gradient.generateGradient(CImageGradient.InterType.Linear);

        int width = DEFAULT_LEGEND_WIDTH;
        int height = DEFAULT_LEGENT_HEIGHT;
        legend = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        Graphics2D g = (Graphics2D) legend.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setPaint(UI.colorBlack2);
        g.fillRoundRect(0, 0, width, height - 12, 5, 5);

        int boxNum = 10;
        g.setStroke(UI.stroke1_5);

        double value;
        int cIndex;
        for (int i = 0; i < boxNum; i++) {
//            value = _minValue + (_maxValue - _minValue) / boxNum * i;

//            if (value > disectBound) {
//                g.setColor(legendColor);
//            }
            cIndex = (int) (1.0f * _gradientColors.length * i / boxNum);
            if (cIndex < 0) {
                cIndex = 0;
            }
            if (cIndex >= _gradientColors.length) {
                cIndex = _gradientColors.length - 1;
            }

            g.setColor(_gradientColors[cIndex]);
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
    public void preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    public void prepareGraphics(Graphics2D g2D) {
    }

    @Override
    public void renderCellLD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //When in low definition, render in color & bar
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
//            if (rowNode.isSingleNode() && columnNode.isSingleNode()) {
            //single nodes
            try {
                g2D.setStroke(UI.stroke1_5);
                g2D.setColor(UI.colorBlack3);
                g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);

                double percentage = (v - _minValue) / (_maxValue - _minValue);
                int height = (int) Math.round(cellHeight * percentage);
                int cIndex = (int) (_gradientColors.length * percentage);
                if (cIndex < 0) {
                    cIndex = 0;
                }
                if (cIndex >= _gradientColors.length) {
                    cIndex = _gradientColors.length - 1;
                }
                Color c = _gradientColors[(int) (cIndex)];

//                g2D.setStroke(UI.stroke1_5);
                g2D.setColor(c);
                g2D.fillRect(anchorX, anchorY + cellHeight - height, cellWidth, height);

            } catch (Exception e) {

            }
//            }
        }
    }

    @Override
    public void renderCellSD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {

            if (rowNode.isSingleNode() && columnNode.isSingleNode()) {
                //single nodes
                try {
                    g2D.setStroke(UI.stroke1_5);
                    g2D.setColor(UI.colorBlack3);
                    g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);

                    double percentage = (v - _minValue) / (_maxValue - _minValue);
                    int height = (int) Math.round(cellHeight * percentage);
                    int cIndex = (int) (_gradientColors.length * percentage);
                    if (cIndex < 0) {
                        cIndex = 0;
                    }
                    if (cIndex >= _gradientColors.length) {
                        cIndex = _gradientColors.length - 1;
                    }
                    Color c = _gradientColors[(int) (cIndex)];

//                g2D.setStroke(UI.stroke1_5);
                    g2D.setColor(c);
                    g2D.fillRect(anchorX, anchorY + cellHeight - height, cellWidth, height);

                } catch (Exception e) {
                }
            } else {
                //group nodes
                //also for multiple datasets, it need to separate
                try {
                    //update orders
                    //need to consider what if the cMatrix was added or removed? need to 
                    if (lastRowNode != rowNode || lastColumnNode != columnNode) {
                        updateNodeOrders(getCoolMapObject(), rowNode, columnNode);
                    }

                    //then go on
                    CoolMapObject obj = getCoolMapObject();
                    List<CMatrix> matrices = obj.getBaseCMatrices();

                    int subMatrixWidth = Math.round(cellWidth / matrices.size());
                    for (int mIndex = 0; mIndex < matrices.size(); mIndex++) {
                        int xAnchor = anchorX + Math.round(cellWidth * mIndex / matrices.size());
                        int yAnchor = anchorY;
                        int sWidth = subMatrixWidth;
                        int sHeight = cellHeight;
                        CMatrix mx = matrices.get(mIndex);

                        ArrayList<NodePair> pairsSorted = nodePairHash.get(mx.getID());
                        if (pairsSorted == null) {
                            return;
                        }

//                        int sCellWidth = subMatrixWidth / pairsSorted.size();
//                        if(sCellWidth < 1) sCellWidth = 1;
                        int sCellWidth = 1;
                        //now I have all the nodePairs, then start from the first one
                        NodePair pr;
                        Double value;
                        int subX1, subX2, subY;
                        for (int i = 0; i < pairsSorted.size(); i++) {
                            pr = pairsSorted.get(i);
                            value = pr.getValue();
                            double percentage = (value - _minValue) / (_maxValue - _minValue);
                            subY = anchorY + cellHeight - (int) Math.round(cellHeight * percentage);
                            subX1 = Math.round(xAnchor + subMatrixWidth * 1.0f * i / pairsSorted.size());

                            if (i < pairsSorted.size() - 1) {

                                subX2 = Math.round(subX1 + subMatrixWidth * 1.0f / pairsSorted.size());
                                sCellWidth = subX2 - subX1;
                                if (sCellWidth < 1) {
                                    sCellWidth = 1;
                                }
                            } else {
                                sCellWidth = Math.round(subMatrixWidth * 1.0f / pairsSorted.size());
                            }
                            int cIndex = (int) (_gradientColors.length * percentage);
                            if (cIndex < 0) {
                                cIndex = 0;
                            }
                            if (cIndex >= _gradientColors.length) {
                                cIndex = _gradientColors.length - 1;
                            }
                            Color c = _gradientColors[cIndex];
                            g2D.setColor(c);

                            sCellWidth++;
                            g2D.fillRect(subX1, subY, sCellWidth, (int) Math.round(cellHeight * percentage));
                        }

                    }

                } catch (Exception e) {
                }

            }
            g2D.setColor(UI.colorBlack2);
            //g2D.drawRect(Math.round(anchorX), Math.round(anchorY), Math.round(cellWidth), Math.round(cellHeight));
            g2D.drawLine(anchorX, anchorY + cellHeight, anchorX + cellWidth, anchorY + cellHeight);

        }
    }

    @Override
    public Image getSubTip(CoolMapObject object, VNode rowNode, VNode columnNode, float percentX, float PercentY, int cellWidth, int cellHeight) {
        try {
            List<CMatrix> matrices = object.getBaseCMatrices();
            int matIndex = (int) (percentX * matrices.size());

            CMatrix currentMatrix = matrices.get(matIndex);

            if (lastRowNode != rowNode || lastColumnNode != columnNode) {
                updateNodeOrders(getCoolMapObject(), rowNode, columnNode);
            }

            ArrayList<NodePair> pairs = nodePairHash.get(currentMatrix.getID());
            if (pairs == null) {
                return null;
            }

            //now get the column percentage
            float subPercent = (percentX - 1.0f * matIndex / matrices.size()) * matrices.size();
            if (subPercent < 0) {
                subPercent = 0;
            } else if (subPercent > 1) {
                subPercent = 1;
            }

            int index = Math.round(pairs.size() * subPercent);
            if (index < 0) {
                index = 0;
            } else if (index >= pairs.size()) {
                index = pairs.size() - 1;
            }

            NodePair pair = pairs.get(index);

            String htmlLabel = pair.getHTMLLabel(matrices, matIndex);

            toolTipLabel.setText(htmlLabel);
            
            return createToolTipFromJLabel(toolTipLabel);
        } catch (Exception e) {
            return null;
        }
    }

    private VNode lastRowNode = null;
    private VNode lastColumnNode = null;
    private HashMap<String, ArrayList<NodePair>> nodePairHash = new HashMap<String, ArrayList<NodePair>>();

    private void updateNodeOrders(CoolMapObject object, VNode rowNode, VNode columnNode) {
        try {
            lastRowNode = rowNode;
            lastColumnNode = columnNode;
            nodePairHash.clear();

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

            List<CMatrix> matrices = object.getBaseCMatrices();
            Double value;
            for (CMatrix mx : matrices) {
                ArrayList<NodePair> pairs = new ArrayList<NodePair>();

                for (Integer i : rowIndices) {
                    if (i == null || i < 0) {
                        continue;
                    }

                    for (Integer j : colIndices) {

                        if (j == null || j < 0) {
                            continue;
                        }
                        for (CMatrix<Double> matrix : matrices) {

                            value = matrix.getValue(i, j);

                            if (value == null || value.isNaN()) {
                                continue;
                            } else {
                                //System.out.println(i + " " + j + " " + v);
                                pairs.add(new NodePair(mx, mx.getRowLabel(i), mx.getColLabel(j), value));
                            }
                        }

                    }
                }

                Collections.sort(pairs);

                nodePairHash.put(mx.getID(), pairs);
            }

        } catch (Exception e) {
            lastRowNode = null;
            lastColumnNode = null;
            nodePairHash.clear();
        }
    }

    private class NodePair implements Comparable<NodePair> {

        private final Double value;
        private final String rowLabel;
        private final String columnLabel;

        public NodePair(CMatrix matrix, String rowLabel, String columnLabel, Double v) {
            value = v;
            this.rowLabel = rowLabel;
            this.columnLabel = columnLabel;
        }

        @Override
        public int compareTo(NodePair o) {
            try {
                return value.compareTo(o.value);
            } catch (Exception e) {
                return -1;
            }
        }

        public String getLabel() {
            return rowLabel + "," + columnLabel;
        }

        public Double getValue() {
            return value;
        }

        @Override
        public String toString() {
            return rowLabel + "," + columnLabel + "," + value;
        }

        public String getHTMLLabel(List<CMatrix> matrices, int index) {
            return "<html><table cellspacing='1' border='0' cellpadding='1'>"
                    + ((matrices.size() > 1) ? "<tr><td><strong>Data: </strong></td><td>" + matrices.get(index).getName() + "</td></tr>" : "")
                    + "<tr><td><strong>Row: </strong></td><td>" + rowLabel + "</td></tr><tr><td><strong>Column: </strong></td><td>" + columnLabel + "</td></tr><tr><td><strong>Value: </strong></td><td><span style='color:#020202;font-weight:bold;'>" + df.format(value) + "</span></td></tr></table></html>";
        }

        private DecimalFormat df = new DecimalFormat("#.###");

    }

    @Override
    public void renderCellHD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
    }

    @Override
    public void postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {

    }

}
