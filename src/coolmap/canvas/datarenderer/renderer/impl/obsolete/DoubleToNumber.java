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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

/**
 *
 * @author gangsu
 */
public class DoubleToNumber extends ViewRenderer<Double> {

    private double _minValue;
    private double _maxValue;
    private Color _minColor = new Color(127, 205, 187);
    private Color _mediumColor = new Color(25, 25, 25);
    private Color _maxColor = new Color(252, 146, 114);
    private Color[] _colors = null;
    private CImageGradient _gradient = new CImageGradient(10000);
    private DecimalFormat _format = new DecimalFormat("#.###");

    public DoubleToNumber() {
        setName("Double to Number");
        setDescription("Draw numeric values to matrix cells");
    }

    @Override
    protected void updateRendererChanges() {
    }

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

        System.out.println("Min/Max:" + _minValue + ":" + _maxValue);
        _gradient.reset();
        _gradient.addColor(_minColor, 0.0);
        _gradient.addColor(_mediumColor, 0.5);
        _gradient.addColor(_maxColor, 1.0);
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
        g2D.setFont(UI.fontMono.deriveFont(10f).deriveFont(Font.BOLD));
    }

    @Override
    protected void _renderCellLD(Double v, VNode rowNode, VNode colNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //_renderCellSD(v, g2D, anchorX, anchorY, cellWidth, cellHeight);
        if (cellWidth > 5 && cellHeight > 5) {
            _renderCellSD(v, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            //g2D.setColor(Color.RED);

            if (v == null || v.isNaN()) {
                //System.out.println(v);
            } else {
                Color c = _colors[(int) ((v - _minValue) / (_maxValue - _minValue) * _colors.length)];
                //System.out.println(c);
                g2D.setColor(c);
                g2D.drawLine(Math.round(anchorX), Math.round(anchorY), Math.round(anchorX), Math.round(anchorY));
            }
        }

    }

    @Override
    protected void _renderCellSD(Double v, VNode rowNode, VNode colNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //System.out.println("Rendered");
        //System.out.println(anchorX + " " + anchorY);

        //Color color = new Color((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255));
        //g2D.setColor(Color.RED);
        //System.out.println(color);
        //System.out.println("Render here:" + anchorX + " " + anchorY + " " + cellWidth + " " + cellHeight);
        //can skip if width or height < 0
        if (v == null || v.isNaN()) {
            //System.out.println(v);
        } else {
//            int index = (int) ((v - _minValue) / (_maxValue - _minValue) * _colors.length);
//            if(index >= _colors.length){
//                index = _colors.length-1;
//            }
//            Color c = _colors[index];
            //System.out.println(c);
//            g2D.setColor(c);
            g2D.setColor(UI.colorBlack2);
            g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
            g2D.setColor(UI.colorGrey1);
            String label;
            if (v >= 0) {
                label = " " + _format.format(v);
            } else {
                label = _format.format(v);
            }
            g2D.drawString(label, anchorX + 2, anchorY + 10);
            g2D.setColor(UI.colorBlack1);
            g2D.drawLine((int) anchorX, (int) (anchorY + cellHeight - 1), (int) (anchorX + cellWidth), (int) (anchorY + cellHeight - 1));
        }

        //g2D.fillOval(anchorX, 50 + (int)(Math.random()*50), cellWidth, cellHeight);
        //g2D.fillRect(20, 20, 100, 100);
    }

    @Override
    protected void _renderCellHD(Double v, VNode rowNode, VNode colNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
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
}
