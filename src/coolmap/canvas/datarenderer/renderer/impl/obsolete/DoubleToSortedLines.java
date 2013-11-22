/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl.obsolete;

import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.utils.CImageGradient;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author gangsu
 */
public class DoubleToSortedLines extends ViewRenderer<Double> {

    private double _minValue;
    private double _maxValue;
    private Color _minColor = new Color(127, 205, 187);
    private Color _mediumColor = new Color(25, 25, 25);
    private Color _maxColor = new Color(252, 146, 114);
    private Color[] _colors = null;
    private CImageGradient _gradient = new CImageGradient(10000);

    public DoubleToSortedLines() {
        setName("Double to Sorted Lines");
        setDescription("Sort values in ascending order");
    }

    @Override
    public void updateRendererChanges() {
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
    public void preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    public void prepareGraphics(Graphics2D g2D) {
    }

    @Override
    public void renderCellLD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //_renderCellSD(v, g2D, anchorX, anchorY, cellWidth, cellHeight);
//        if (cellWidth > 1 || cellHeight > 1) {
//            renderCellSD(v, g2D, anchorX, anchorY, cellWidth, cellHeight);
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
        renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);

    }

    @Override
    public void renderCellSD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
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

            //int height = (int) (cellHeight * (v - _minValue) / (_maxValue - _minValue));
            //GradientPaint paint = new GradientPaint(anchorX, anchorY, UI.colorLightYellow, anchorX, anchorY + cellHeight, UI.colorOrange0);
            //g2D.setPaint(paint);
//            g2D.setColor(UI.colorLightYellow);
//            g2D.fillRect((int) anchorX + 1, (int) (anchorY + cellHeight - height), (int) cellWidth - 2, (int) height);
            if (rowNode.isSingleNode() && columnNode.isSingleNode()) {
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

                if (columnNode.isGroupNode()) {
                    colIndices = columnNode.getBaseIndicesFromCOntology((CMatrix) getCoolMapObject().getBaseCMatrices().get(0), COntology.COLUMN);
                } else {
                    colIndices = new Integer[]{((CMatrix) getCoolMapObject().getBaseCMatrices().get(0)).getIndexOfColName(columnNode.getName())};
                }

                List<CMatrix> matrices = getCoolMapObject().getBaseCMatrices();
                Double value;
                ArrayList<Double> values = new ArrayList<Double>();

                //values not correct?
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

                //Why the values are not consistent?
                //System.out.println(v + ":" + values + "--" + rowIndices + ":" + colIndices);
//                System.out.println(v + ":" + Arrays.toString(rowIndices) + ":" + Arrays.toString(colIndices));
                //should have been sorted already
                Collections.sort(values);

                double range = _maxValue - _minValue;
                float divideSize = cellWidth / values.size();

                for (int i = 0; i < values.size() - 1; i++) {
                    int h1 = (int) (cellHeight * (values.get(i) - _minValue) / range);
                    int h2 = Math.round((float) ((values.get(i + 1) - _minValue) * cellHeight / range));

                    int x1 = Math.round(i * divideSize);
                    int x2 = Math.round((i + 1) * divideSize);

                    g2D.setColor(UI.colorLightGreen0);
                    g2D.drawLine((int) (anchorX + x1 + divideSize / 2), (int) (anchorY + cellHeight - h1), (int) (anchorX + x2 + divideSize / 2), (int) (anchorY + cellHeight - h2));

                    g2D.setColor(UI.colorLightBlue0);
                    g2D.fillOval((int) (anchorX + x1 + divideSize / 2) - 1, (int) (anchorY + cellHeight - h1) - 1, 3, 3);
                    g2D.fillOval((int) (anchorX + x2 + divideSize / 2) - 1, (int) (anchorY + cellHeight - h2) - 2, 3, 3);
                }

//                int size = values.size();
//                double min = values.get(0);
//                double max = values.get(values.size() - 1);
//
//                double median;
//                if (size % 2 == 0) {
//                    median = (values.get(size / 2) + values.get(size / 2 - 1)) / 2;
//                } else {
//                    median = (values.get(size / 2));
//                }
//
//                double[] valueArray = new double[values.size()];
//                int c = 0;
//                for (Double d : values) {
//                    valueArray[c++] = d.doubleValue();
//                }
//
//                Percentile percentile = new Percentile();
//                double q1 = percentile.evaluate(valueArray, 25);
//                double q3 = percentile.evaluate(valueArray, 75);
//This way is too slow                
//                double[] valueArray = new double[values.size()];
//                int c = 0;
//                for (Double d : values) {
//                    valueArray[c++] = d.doubleValue();
//                }
//
//                Percentile percentile = new Percentile();
//                percentile.setData(valueArray);
//
//
//
////                percentile.setQuantile(1);
//                double min = percentile.evaluate(valueArray, 1);
////                percentile.setQuantile(25);
//                double q1 = percentile.evaluate(valueArray, 25);
////                percentile.setQuantile(50);
//                double median = percentile.evaluate(valueArray, 50);
////                percentile.setQuantile(75);
//                double q3 = percentile.evaluate(valueArray, 75);
////                percentile.setQuantile(100);
//                double max = percentile.evaluate(valueArray, 100);
//                System.out.println(min + " " + q1 + " " + median + " " + q3 + " " + max);
//                
//
//
//                double range = _maxValue - _minValue;
//
//                g2D.setColor(UI.colorLightBlue0);
//                g2D.setStroke(UI.stroke1);
//
//                double minP = (min - _minValue) / range;
//                double maxP = (max - _minValue) / range;
//                double medianP = (median - _minValue) / range;
//                double q1P = (q1 - _minValue) / range;
//                double q3P = (q3 - _minValue) / range;
//
//                g2D.drawLine((int) (anchorX + cellWidth / 2), (int) (anchorY + cellHeight - cellHeight * maxP), (int) (anchorX + cellWidth / 2), (int) (anchorY + cellHeight - cellHeight * minP));
//                g2D.drawRect((int) (anchorX + cellWidth / 4), (int) (anchorY + cellHeight - cellHeight * q3P), (int) (cellWidth / 2), (int) (cellHeight * (q3P - q1P)));
//
//                g2D.drawLine((int) (anchorX + 1), (int) (anchorY + cellHeight - cellHeight * medianP), (int) (anchorX + cellWidth - 1), (int) (anchorY + cellHeight - cellHeight * medianP));
//            
            }

        }

    }

    @Override
    public void renderCellHD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
    }

    @Override
    public void postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }
}
