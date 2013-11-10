/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl.obsolete;

import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.CImageGradient;
import coolmap.utils.graphics.UI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import javax.swing.*;

/**
 *
 * @author gangsu
 */
public class Politics extends ViewRenderer<Double> {

    private double _minValue;
    private double _maxValue;
    private Color _minColor = new Color(127, 205, 187);
    private Color _mediumColor = new Color(25, 25, 25);
    private Color _maxColor = new Color(252, 146, 114);
    private Color[] _colors = null;
    private CImageGradient _gradient = new CImageGradient(10000);

    public Politics() {
        setName("Politics");
        setDescription("Use color to represent numeric values");
    }

//    public DoubleToColor(CoolMapObject object) {
//        super(object);
//        
//    }
    @Override
    public void initialize() {

        /////////////////////////////
        _minValue = Double.MAX_VALUE;
        _maxValue = -Double.MAX_VALUE;

        //
        CoolMapObject<?, Double> object = getCoolMapObject();
        if (object == null) {
            _minValue = 0;
            _maxValue = 0;
        } else {
            for (int i = 0; i < object.getViewNumRows(); i++) {
                for (int j = 0; j < object.getViewNumColumns(); j++) {
                    Double v = object.getViewValue(i, j);
                    if (v == null || v.isNaN()) {
                        continue;
                    } else {
                        if (v < _minValue) {
                            _minValue = v;
                        }
                        if (v > _maxValue) {
                            _maxValue = v;
                        }
                    }
                }
            }
        }

        //System.out.println("Min/Max:" + _minValue + ":" + _maxValue);
        _updateGradient();
    }

    private void _updateGradient() {
        //This is just for the dataset
//        _minValue = 0;
//        _maxValue = 500;





        _gradient.reset();
        _gradient.addColor(_minColor, 0.0);
        _gradient.addColor(_mediumColor, 0.5);
        _gradient.addColor(_maxColor, 1.0);

//        _gradient.addColor(new Color(44, 127, 184), 0.0);
//        _gradient.addColor(new Color(127, 205, 187), 0.14);
//        _gradient.addColor(new Color(189, 189, 189), 0.141);
//        _gradient.addColor(new Color(189, 189, 189), 0.239);
//        _gradient.addColor(new Color(252, 146, 114), 0.24);
//        _gradient.addColor(new Color(251, 106, 74), 0.5);
//        _gradient.addColor(new Color(222, 45, 38), 1.0);




        _colors = _gradient.generateGradient(CImageGradient.InterType.Linear);
    }

    @Override
    public boolean canRender(Class<?> viewClass) {
        return Double.class.isAssignableFrom(viewClass);
    }

    @Override
    protected void _preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    protected void _prepareGraphics(Graphics2D g2D) {
    }

    @Override
    protected void _renderCellLD(Double v, VNode rowNode, VNode colNode, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight) {
        //_renderCellSD(v, g2D, anchorX, anchorY, cellWidth, cellHeight);
//        if (cellWidth > 1 || cellHeight > 1) {
//            _renderCellSD(v, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
//        } else {
//            //g2D.setColor(Color.RED);
//
//            if (v == null || v.isNaN()) {
//                //System.out.println(v);
//            } else {
//                int index = (int) ((v - _minValue) / (_maxValue - _minValue) * _colors.length);
//                if (index >= _colors.length) {
//                    index = _colors.length - 1;
//                }
//                if (index < 0) {
//                    index = 0;
//                }
//                Color c = _colors[index];
//                //System.out.println(c);
//
//
//
//                g2D.setColor(c);
//                g2D.drawLine(Math.round(anchorX), Math.round(anchorY), Math.round(anchorX), Math.round(anchorY));
//            }
//        }
        _renderCellSD(v, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);

    }

    @Override
    public Image getLegend() {
        int width = 100;
        int height = 25;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        LinearGradientPaint paint = new LinearGradientPaint(0, 0, width, 0, new float[]{0f, 0.5f, 1f}, new Color[]{_minColor, _mediumColor, _maxColor});
        //LinearGradientPaint paint = new LinearGradientPaint(0, 0, width, 0, new float[]{0f, 1f}, new Color[]{_minColor, _maxColor});

//        _gradient.addColor(new Color(44, 127, 184), 0.0);
//        _gradient.addColor(new Color(127, 205, 187), 0.14);
//        _gradient.addColor(new Color(189, 189, 189), 0.141);
//        _gradient.addColor(new Color(189, 189, 189), 0.239);
//        _gradient.addColor(new Color(252, 146, 114), 0.24);
//        _gradient.addColor(new Color(251, 106, 74), 0.5);
//        _gradient.addColor(new Color(222, 45, 38), 1.0);


//        LinearGradientPaint paint = new LinearGradientPaint(0, 0, width, 0, new float[]{0f, 0.14f, 0.141f, 0.239f, 0.24f, 0.5f, 1f}, new Color[]{new Color(44, 127, 184), new Color(127, 205, 187), new Color(189, 189, 189),         
//        new Color(189, 189, 189), new Color(252, 146, 114),new Color(251, 106, 74), new Color(222, 45, 38)
//        });


        g.setPaint(paint);
        g.fillRoundRect(0, 0, width, height - 12, 5, 5);
        g.setColor(UI.colorGrey1);
        g.setFont(UI.fontMono.deriveFont(10f));
        DecimalFormat format = new DecimalFormat("#.##");
        g.drawString(format.format(_minValue), 2, 23);

        String maxString = format.format(_maxValue);
        int swidth = g.getFontMetrics().stringWidth(maxString);
        g.drawString(maxString, width - 2 - swidth, 23);

        g.dispose();
        return image;
    }

    @Override
    protected void _renderCellSD(Double v, VNode rowNode, VNode colNode, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight) {
        //System.out.println("Rendered");
        //System.out.println(anchorX + " " + anchorY);

        //Color color = new Color((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255));
        //g2D.setColor(Color.RED);
        //System.out.println(color);
        //System.out.println("Render here:" + anchorX + " " + anchorY + " " + cellWidth + " " + cellHeight);


        //can skip if width or height < 0


//        if (v == null || v.isNaN()) {
//            //System.out.println(v);
//        } else {
//            try {
//                int index = (int) ((v - _minValue) / (_maxValue - _minValue) * _colors.length);
//                if (index >= _colors.length) {
//                    index = _colors.length - 1;
//                }
//                if (index < 0) {
//                    index = 0;
//                }
//                Color c = _colors[index];
//                //System.out.println(c);
//                g2D.setColor(c);
//                g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
//            } catch (Exception e) {
//                System.out.println("Null pointer exception:" + v + "," + _minValue + "," + _maxValue + "," + _colors);
//                e.printStackTrace();
//            }
//        }

        if (v == null) {
            //draw a cross
        } else if (v == 0) {
            //yeah
            g2D.setColor(new Color(0, 230, 167));
            g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
        } else if (v == 1) {
            //no
            g2D.setColor(new Color(255, 20, 0));
            g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
        } else if (v == 2) {
            //present
            g2D.setColor(new Color(0, 153, 255));
            g2D.fillOval((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);

        } else if (v == 3) {
            //not voting
            g2D.setColor(new Color(255, 247, 0));
            g2D.fillOval((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
        }







        //g2D.fillOval(anchorX, 50 + (int)(Math.random()*50), cellWidth, cellHeight);


        //g2D.fillRect(20, 20, 100, 100);
    }

    @Override
    protected void _renderCellHD(Double v, VNode rowNode, VNode colNode, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight) {
        _renderCellSD(v, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
    }

    @Override
    protected void _postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    public static void main(String args[]) {
//
//        RandomGrid rg = new RandomGrid();
//        DoubleCMatrix mx = new DoubleCMatrix("Test", 1000, 2000);
//        double d = 0;
//        for (int i = 0; i < mx.getNumRows(); i++) {
//            for (int j = 0; j < mx.getNumCols(); j++) {
//                mx.setValue(i, j, d++);
//            }
//        }
//        
//        DoubleDoubleMax maxAggr = new DoubleDoubleMax();
//        CoolMapObject<Double, Double> coolMapInstance = new CoolMapObject<Double, Double>();
//        coolMapInstance.setAggregator(maxAggr);
//        coolMapInstance.setBaseMatrix(mx);
////        it takes quite a bit time to initalize
//                List<String> rowLabels = mx.getRowLabelsAsList();
//        List<String> colLabels = mx.getColLabelsAsList();
//        
//        ArrayList<VNode> rowNodes = new ArrayList<VNode>();
//        ArrayList<VNode> colNodes = new ArrayList<VNode>();
//        
//        for(String r : rowLabels){
//            rowNodes.add(new VNode(r));
//        }
//        
//        for(String c : colLabels){
//            colNodes.add(new VNode(c));
//        }
//        
//        coolMapInstance.insertRowNodes(0, rowNodes);
//        coolMapInstance.insertColNodes(0, colNodes);
//        
//        //need to update view offset
//        float zoomX = 0.25f;
//        float zoomY = 0.25f;
//        
//        for(int i=0; i < coolMapInstance.getViewNumRows(); i++){
//            coolMapInstance.getViewNodeRow(i).setViewOffset(i * zoomX);
//        }
//        
//        for(int j=0; j < coolMapInstance.getViewNumCols(); j++){
//            coolMapInstance.getViewNodeCol(j).setViewOffset(j * zoomY);
//        }
//        
//        
//        
//        
//        //good to go
//        BufferedImage img = null;
//        long t1 = System.nanoTime();
//        
//        try{
//             img = rg.getRenderedMap(coolMapInstance, 0, 1000, 0, 2000, zoomX, zoomY);
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
//        long t2 = System.nanoTime();
//        
//        System.out.println((t2 - t1)/1000000.0);
//        
//        System.out.println(img);
//        
//        
//        try{
//            ImageIO.write(img, "jpg", new File("/Users/gangsu/Desktop/render.jpg"));
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
    }
    private JTextField _lowField = new JTextField();
    private JTextField _mediumField = new JTextField();
    private JTextField _highField = new JTextField();
    private JLabel _lowColorLabel = new JLabel("   ");
    private JLabel _mediumColorLabel = new JLabel("   ");
    private JLabel _highColorLabel = new JLabel("   ");
    private JButton _button = new JButton("Update", UI.getImageIcon("refresh"));

    @Override
    public JComponent getConfigUI() {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 5;
        c.ipady = 5;

        DecimalFormat format = new DecimalFormat("#.##");

        _lowField.setColumns(10);
        _lowField.setText(format.format(_minValue));

        panel.add(new JLabel("Low"), c);
        c.gridx++;
        panel.add(_lowField, c);
        c.gridx++;
        panel.add(_lowColorLabel, c);
        _lowColorLabel.setOpaque(true);
        _lowColorLabel.setBackground(_minColor);


        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("Middle"), c);
        c.gridx++;
        //panel.add(_mediumField, c);
        _mediumField.setEditable(false);
        c.gridx++;
        panel.add(_mediumColorLabel, c);
        _mediumColorLabel.setBackground(_mediumColor);
        _mediumColorLabel.setOpaque(true);



        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("High"), c);
        c.gridx++;
        panel.add(_highField, c);
        _highField.setColumns(10);
        c.gridx++;
        panel.add(_highColorLabel, c);
        _highColorLabel.setBackground(_maxColor);
        _highColorLabel.setOpaque(true);
        _highField.setText(format.format(_maxValue));

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        panel.add(_button, c);

        _button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = getCoolMapObject();
                if (object == null) {
                    return;
                }

                Double low = null;
                Double high = null;

                try {
                    low = Double.parseDouble(_lowField.getText());
                    high = Double.parseDouble(_highField.getText());
                } catch (Exception e) {
                }

                if (low == null || high == null) {
                    return;
                }

                _minValue = low;
                _maxValue = high;

                object.getCoolMapView().updateCanvasEnforceAll();
            }
        });

        _lowColorLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {
                CoolMapObject object = getCoolMapObject();
                if (object == null) {
                    return;
                }
                Color c = JColorChooser.showDialog(panel.getTopLevelAncestor(), "Choose low color", _minColor);
                if (c != null) {
                    _minColor = c;
                    _lowColorLabel.setBackground(c);
                    _updateGradient();
                    object.getCoolMapView().updateCanvasEnforceAll();
                }
            }
        });


        _mediumColorLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {
                CoolMapObject object = getCoolMapObject();
                if (object == null) {
                    return;
                }
                Color c = JColorChooser.showDialog(panel.getTopLevelAncestor(), "Choose low color", _mediumColor);
                if (c != null) {
                    _mediumColor = c;
                    _mediumColorLabel.setBackground(c);
                    _updateGradient();
                    object.getCoolMapView().updateCanvasEnforceAll();
                }
            }
        });

        _highColorLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {
                CoolMapObject object = getCoolMapObject();
                if (object == null) {
                    return;
                }
                Color c = JColorChooser.showDialog(panel.getTopLevelAncestor(), "Choose low color", _maxColor);
                if (c != null) {
                    _maxColor = c;
                    _highColorLabel.setBackground(c);
                    _updateGradient();
                    object.getCoolMapView().updateCanvasEnforceAll();
                }
            }
        });


        return panel;
    }

    @Override
    protected void updateRendererChanges() {
    }
}
