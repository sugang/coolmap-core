/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl;

import com.google.common.collect.Range;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
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
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
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

/**
 *
 * @author sugang this is a base one for all.
 */
public class NumberToBar extends ViewRenderer<Double> {

    private JTextField minValueField = new JTextField();
    private JTextField maxValueField = new JTextField();

    private Color normalBG = Color.WHITE;
    private Color errorBG = UI.colorRedWarning;
    private JComboBox presetRangeComboBox;

    public NumberToBar() {
        setName("Number to Bar");
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

        updateLegend();

    }

    protected void updateLegend() {

        int width = DEFAULT_LEGEND_WIDTH;
        int height = DEFAULT_LEGENT_HEIGHT;
        legend = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        Graphics2D g = (Graphics2D) legend.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setPaint(UI.colorBlack2);
        g.fillRoundRect(0, 0, width, height - 12, 5, 5);

        g.setColor(UI.colorLightYellow);
        int boxNum = 10;
        for (int i = 0; i < boxNum; i++) {
            int h = (height - 12) / boxNum * i;
            g.fillRect(i * width / boxNum, height - 12 - h, width / boxNum, h);
        }

        g.setColor(UI.colorBlack2);
        g.setFont(UI.fontMono.deriveFont(10f));
        DecimalFormat format = new DecimalFormat("#.##");
        g.drawString(format.format(_minValue), 2, 23);

        String maxString = format.format(_maxValue);
        int swidth = g.getFontMetrics().stringWidth(maxString);
        g.drawString(maxString, width - 2 - swidth, 23);
        g.dispose();
    }

    protected BufferedImage legend;

    @Override
    public Image getLegend() {
        return legend;
    }

    protected double _minValue = 0;
    protected double _maxValue = 1;
    private Color[] _gradientColors = null;

    @Override
    protected void initialize() {
        System.out.println("Number to Bar initalized");
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
//        g2D.setFont(UI.fontMono.deriveFont(12f));
//        g2D.setColor(UI.colorLightYellow);
    }

    @Override
    public void renderCellLD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            try {
                g2D.setStroke(UI.stroke1_5);
                g2D.setColor(UI.colorBlack3);
                g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
                int height = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));
                g2D.setColor(UI.colorLightYellow);
                g2D.fillRect(Math.round(anchorX), Math.round(anchorY + cellHeight - height), Math.round(cellWidth), Math.round(cellHeight));
//                g2D.setColor(UI.colorBlack2);
//                g2D.drawRect(Math.round(anchorX), Math.round(anchorY), Math.round(cellWidth), Math.round(cellHeight));
            } catch (Exception e) {
                System.out.println("Null pointer exception:" + v + "," + _minValue + "," + _maxValue + "," + _gradientColors);
                e.printStackTrace();
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
                g2D.setStroke(UI.stroke1_5);
                g2D.setColor(UI.colorBlack3);
                g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
                int height = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));
                g2D.setColor(UI.colorLightYellow);
                g2D.fillRect(Math.round(anchorX), Math.round(anchorY + cellHeight - height), Math.round(cellWidth), Math.round(cellHeight));
                g2D.setColor(UI.colorBlack2);
                g2D.drawRect(Math.round(anchorX), Math.round(anchorY), Math.round(cellWidth), Math.round(cellHeight));
            } catch (Exception e) {
                System.out.println("Null pointer exception:" + v + "," + _minValue + "," + _maxValue + "," + _gradientColors);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void renderCellHD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            try {
                g2D.setStroke(UI.stroke2);
                g2D.setColor(UI.colorBlack3);
                g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
                int height = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));

                LinearGradientPaint paint = new LinearGradientPaint(anchorX, anchorY, anchorX, anchorY + cellHeight, new float[]{0f, 1f}, new Color[]{UI.colorLightYellow, UI.colorOrange0});

                g2D.setPaint(paint);
                g2D.fillRect(Math.round(anchorX), Math.round(anchorY + cellHeight - height), Math.round(cellWidth), Math.round(cellHeight));

                g2D.setColor(UI.colorBlack2);
                g2D.drawRect(Math.round(anchorX), Math.round(anchorY), Math.round(cellWidth), Math.round(cellHeight));
            } catch (Exception e) {
                System.out.println("Null pointer exception:" + v + "," + _minValue + "," + _maxValue + "," + _gradientColors);
                e.printStackTrace();
            }
        }
    }

    DecimalFormat df = new DecimalFormat("#.##");

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
