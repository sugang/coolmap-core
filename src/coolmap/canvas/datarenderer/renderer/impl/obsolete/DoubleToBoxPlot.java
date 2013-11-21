/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl.obsolete;

import coolmap.data.aggregator.impl.DoubleDoubleMax;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.impl.DoubleCMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.canvas.misc.MatrixCell;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.contology.model.COntology;
import coolmap.utils.CImageGradient;
import coolmap.utils.graphics.UI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

/**
 *
 * @author gangsu
 */
public class DoubleToBoxPlot extends ViewRenderer<Double> {

    private double _minValue = 0;
    private double _maxValue = 0;
    private Color _minColor = new Color(127, 205, 187);
    private Color _mediumColor = new Color(25, 25, 25);
    private Color _maxColor = new Color(252, 146, 114);
    private Color[] _colors = null;
    private CImageGradient _gradient = new CImageGradient(10000);

    public DoubleToBoxPlot() {
        setName("Double to Boxplot");
        setDescription("Use lines and boxplots to represent numeric values");
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
        CoolMapObject object = getCoolMapObject();
        //if(Double.class.isAssignableFrom(object.get))
        if (object != null && Double.class.isAssignableFrom(object.getBaseClass()) && Double.class.isAssignableFrom(object.getViewClass())) {

            for (Object mat : object.getBaseCMatrices()) {
                CMatrix matrix = (CMatrix) mat;
                for (int i = 0; i < matrix.getNumRows(); i++) {
                    for (int j = 0; j < matrix.getNumColumns(); j++) {
                        Object vo = matrix.getValue(i, j);
                        if (vo == null) {
                            continue;
                        } else {
                            try {
                                Double v = (Double) vo;
                                if (v == null || v.isNaN() || v.isInfinite()) {
                                    continue;
                                } else {
                                    if (v < _minValue) {
                                        _minValue = v;
                                    }
                                    if (v > _maxValue) {
                                        _maxValue = v;
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }

                    }
                }
            }

        }

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
//            _renderCellSD(v, g2D, anchorX, anchorY, cellWidth, cellHeight);
//        } else {
//            //g2D.setColor(Color.RED);
//
//            if (v == null || v.isNaN()) {
//                //System.out.println(v);
//            } else {
//                Color c = _colors[(int) ((v - _minValue) / (_maxValue - _minValue) * _colors.length)];
//                //System.out.println(c);
//                g2D.setColor(c);
//                g2D.drawLine(Math.round(anchorX), Math.round(anchorY), Math.round(anchorX), Math.round(anchorY));
//            }
//        }
        _renderCellSD(v, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);

    }

    @Override
    public Image getSubTip(MatrixCell activeCell, float percentX, float PercentY, int cellWidth, int cellHeight) {
        if (getCoolMapObject() == null || activeCell == null || !activeCell.isValidCell(getCoolMapObject())) {
            return null;
        }

        if (getCoolMapObject().getBaseCMatrices().isEmpty()) {
            return null;
        }

        CoolMapObject obj = getCoolMapObject();
        VNode rowNode = obj.getViewNodeRow(activeCell.getRow().intValue());
        VNode colNode = obj.getViewNodeColumn(activeCell.getCol().intValue());

        if (rowNode == null || colNode == null) {
            return null;
        }

        if (rowNode.isSingleNode() && colNode.isSingleNode()) {
            return null;
        }

        Integer[] rowIndices;
        Integer[] colIndices;

        CMatrix m0 = (CMatrix) getCoolMapObject().getBaseCMatrices().get(0);

        if (rowNode.isGroupNode()) {
            rowIndices = rowNode.getBaseIndicesFromCOntology((CMatrix) getCoolMapObject().getBaseCMatrices().get(0), COntology.ROW);
        } else {
            rowIndices = new Integer[]{((CMatrix) getCoolMapObject().getBaseCMatrices().get(0)).getIndexOfRowName(rowNode.getName())};
        }

        if (colNode.isGroupNode()) {
            colIndices = colNode.getBaseIndicesFromCOntology((CMatrix) getCoolMapObject().getBaseCMatrices().get(0), COntology.COLUMN);
        } else {
            colIndices = new Integer[]{((CMatrix) getCoolMapObject().getBaseCMatrices().get(0)).getIndexOfColName(colNode.getName())};
        }

        List<CMatrix> matrices = getCoolMapObject().getBaseCMatrices();
        Double value;
        ArrayList<BoxplotEntry> values = new ArrayList<BoxplotEntry>();

        for (Integer i : rowIndices) {
            if (i == null || i < 0) {
                continue;
            }

            String rowLabel = m0.getRowLabel(i);

            for (Integer j : colIndices) {

                if (j == null || j < 0) {
                    continue;
                }
                //Double value = (Double) getCoolMapObject().getViewValue(i, j);
                //This is wrong. it should eb the base matrix value, not the view values
                String colLabel = m0.getColLabel(j);

                for (CMatrix<Double> matrix : matrices) {

                    value = matrix.getValue(i, j);

                    if (value == null || value.isNaN()) {
                        continue;
                    } else {
                        //System.out.println(i + " " + j + " " + v);
                        values.add(new BoxplotEntry("<html><strong>Row:</strong>" + rowLabel + "<br/><strong>Col:</strong>" + colLabel + "</html>", value));
                    }
                }

            }
        }

        if (values.isEmpty()) {
            return null;
        }

        Collections.sort(values);

        int size = values.size();
        double min = values.get(0).value;
        double max = values.get(values.size() - 1).value;

        double median;
        if (size % 2 == 0) {
            median = (values.get(size / 2).value + values.get(size / 2 - 1).value) / 2;
        } else {
            median = (values.get(size / 2).value);
        }

        double[] valueArray = new double[values.size()];
        int c = 0;
        for (BoxplotEntry d : values) {
            valueArray[c++] = d.value.doubleValue();
        }

        Percentile percentile = new Percentile();
        double q1 = percentile.evaluate(valueArray, 25);
        double q3 = percentile.evaluate(valueArray, 75);

        double range = _maxValue - _minValue;

        double minP = (min - _minValue) / range;
        double maxP = (max - _minValue) / range;
        double medianP = (median - _minValue) / range;
        double q1P = (q1 - _minValue) / range;
        double q3P = (q3 - _minValue) / range;
        DecimalFormat format = new DecimalFormat("#.###");

        JLabel label = new JLabel("<html><table border='0' cellspacing='0'><tr><td><strong>Min</strong></td><td>" + format.format(min) + "</td><tr/><tr><td><strong>Median</strong></td><td>" + format.format(median) + "</td><tr/>"
                + "<tr><td><strong>" + "Max" + "</strong></td><td>" + format.format(max) + "</td></table></html>");
        Font font = UI.fontPlain.deriveFont(12f);
        label.setFont(font);
        label.setForeground(UI.colorBlack3);
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
        label.setSize(label.getPreferredSize());

        BufferedImage image = new BufferedImage(label.getWidth(), label.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = (Graphics2D) image.createGraphics();
        //g2D.setColor(Color.BLUE);
        //g2D.fillRect(0, 0, image.getWidth(), image.getHeight());
        label.paint(g2D);
        return image;
        
        //This is a way to use JLabel, also need to create one like this
    }

    private class BoxplotEntry implements Comparable<BoxplotEntry> {

        public String label;
        public Double value;

        public BoxplotEntry(String l, Double v) {
            label = l;
            value = v;
        }

        @Override
        public int compareTo(BoxplotEntry t) {
            return this.value.compareTo(t.value);
        }
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
        g2D.setColor(UI.colorBlack2);
        g2D.fillRect((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
        g2D.setColor(Color.BLACK);
        g2D.setStroke(UI.stroke2);
        g2D.drawLine((int) anchorX, (int) (anchorY + cellHeight), (int) (anchorX + cellWidth), (int) (anchorY + cellHeight));

        if (v == null || v.isNaN()) {
            //System.out.println(v);
        } else {
            //Color c = _colors[(int) ((v - _minValue) / (_maxValue - _minValue) * _colors.length)];
            //System.out.println(c);

            int height = (int) (cellHeight * (v - _minValue) / (_maxValue - _minValue));

            //GradientPaint paint = new GradientPaint(anchorX, anchorY, UI.colorLightYellow, anchorX, anchorY + cellHeight, UI.colorOrange0);
            //g2D.setPaint(paint);
//            g2D.setColor(UI.colorLightYellow);
//            g2D.fillRect((int) anchorX + 1, (int) (anchorY + cellHeight - height), (int) cellWidth - 2, (int) height);
            if (rowNode.isSingleNode() && colNode.isSingleNode()) {
                //draw v
                g2D.setColor(UI.colorLightBlue0);
                g2D.setStroke(UI.stroke1);
                double medianP = (v - _minValue) / (_maxValue - _minValue);
                g2D.drawLine((int) (anchorX + 1), (int) (anchorY + cellHeight - cellHeight * medianP), (int) (anchorX + cellWidth - 1), (int) (anchorY + cellHeight - cellHeight * medianP));
            } else {

                //find all
                Integer[] rowIndices;
                Integer[] colIndices;
                if (rowNode.isGroupNode()) {
                    rowIndices = rowNode.getBaseIndicesFromCOntology((CMatrix) getCoolMapObject().getBaseCMatrices().get(0), COntology.ROW);
                } else {
                    rowIndices = new Integer[]{((CMatrix) getCoolMapObject().getBaseCMatrices().get(0)).getIndexOfRowName(rowNode.getName())};
                }

                if (colNode.isGroupNode()) {
                    colIndices = colNode.getBaseIndicesFromCOntology((CMatrix) getCoolMapObject().getBaseCMatrices().get(0), COntology.COLUMN);
                } else {
                    colIndices = new Integer[]{((CMatrix) getCoolMapObject().getBaseCMatrices().get(0)).getIndexOfColName(colNode.getName())};
                }

                List<CMatrix> matrices = getCoolMapObject().getBaseCMatrices();
                Double value;
                ArrayList<Double> values = new ArrayList<Double>();
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
                    return;
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

                Percentile percentile = new Percentile();
                double q1 = percentile.evaluate(valueArray, 25);
                double q3 = percentile.evaluate(valueArray, 75);

                double range = _maxValue - _minValue;

                g2D.setColor(UI.colorLightBlue0);
                g2D.setStroke(UI.stroke1);

                double minP = (min - _minValue) / range;
                double maxP = (max - _minValue) / range;
                double medianP = (median - _minValue) / range;
                double q1P = (q1 - _minValue) / range;
                double q3P = (q3 - _minValue) / range;

                g2D.drawLine((int) (anchorX + cellWidth / 2), (int) (anchorY + cellHeight - cellHeight * maxP), (int) (anchorX + cellWidth / 2), (int) (anchorY + cellHeight - cellHeight * minP));
                g2D.drawRect((int) (anchorX + cellWidth / 4), (int) (anchorY + cellHeight - cellHeight * q3P), (int) (cellWidth / 2), (int) (cellHeight * (q3P - q1P)));

                g2D.drawLine((int) (anchorX + 1), (int) (anchorY + cellHeight - cellHeight * medianP), (int) (anchorX + cellWidth - 1), (int) (anchorY + cellHeight - cellHeight * medianP));
            }

        }

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
}
