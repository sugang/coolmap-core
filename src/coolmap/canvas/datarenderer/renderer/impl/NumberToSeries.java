/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl;

import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.utils.graphics.UI;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 *
 * @author sugang
 */
public class NumberToSeries extends NumberToBar {

    public NumberToSeries() {
        super();
        setName("Number to Series");
        setDescription("Use consequtivelines to connect dots");
        toolTipLabel = new JLabel();
        toolTipLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        toolTipLabel.setBackground(UI.colorGrey3);
        toolTipLabel.setOpaque(true);
        toolTipLabel.setForeground(UI.colorBlack2);
        toolTipLabel.setFont(UI.fontPlain.deriveFont(12f));

        setClipCell(true); //make rendering possible to span to neighbor cells
    }

    //always going row priority for now
    @Override
    public void renderCellSD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            //need to draw to lines with left and right, well.
            try {

                if (rowNode.isSingleNode() && columnNode.isSingleNode()) {
                    g2D.setColor(UI.colorBlack3);
                    g2D.fillRect(anchorX, anchorY, cellWidth, cellHeight);

                    g2D.setStroke(UI.stroke1_5);
                    Float columnIndex = columnNode.getViewIndex();
                    if (columnIndex == null || columnIndex < 0 || columnIndex >= getCoolMapObject().getViewNumColumns()) {
                        return;
                    }

                    //Else draw something
                    int nextIndex = columnIndex.intValue() + 1;
                    int centerX = anchorX + cellWidth / 2;
                    int heightV = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));

                    //compute effective cellWidth
                    //effective
                    float effectiveZoomX = cellWidth / columnNode.getCurrentViewMultiplier();

                    try {
                        Double nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);

                        //need to find center of these two columnNodes
                        VNode nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);

                        int nextCenterX = (int) (anchorX + cellWidth + nextColumnNode.getViewSizeInMap(effectiveZoomX) / 2);

                        //cell height are the same
                        int heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));

                        g2D.setColor(UI.colorUSUKI);

                        g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);
                    } catch (Exception e) {
                    }

                    //bad fix -> as 4 threads, the block can't span...
                    try {
                        nextIndex = columnIndex.intValue() - 1;
                        Double nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);
                        VNode nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                        int nextCenterX = (int) (anchorX - nextColumnNode.getViewSizeInMap(effectiveZoomX) / 2);
                        int heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));
                        g2D.setColor(UI.colorUSUKI);
                        g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);

                    } catch (Exception e) {
                    }
                    g2D.setColor(UI.colorUSUKI);
                    g2D.fillOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
                    g2D.setColor(UI.colorBlack1);
                    g2D.drawLine(anchorX, anchorY + cellHeight - 1, anchorX + cellWidth, anchorY + cellHeight - 1);
                } else {
                    //group nodes
                    g2D.setColor(UI.colorBlack5);
                    g2D.fillRect(anchorX, anchorY, cellWidth, cellHeight);

                    //ok then split
                    CoolMapObject obj = getCoolMapObject();
                    List<CMatrix> matrices = obj.getBaseCMatrices();

                    //find the indices
                    Integer[] rowIndices;
                    Integer[] colIndices;
                    if (rowNode.isGroupNode()) {
                        rowIndices = rowNode.getBaseIndicesFromCOntology((CMatrix) obj.getBaseCMatrices().get(0), COntology.ROW);
                    } else {
                        rowIndices = new Integer[]{((CMatrix) obj.getBaseCMatrices().get(0)).getIndexOfRowName(rowNode.getName())};
                    }
                    if (columnNode.isGroupNode()) {
                        colIndices = columnNode.getBaseIndicesFromCOntology((CMatrix) obj.getBaseCMatrices().get(0), COntology.COLUMN);
                    } else {
                        colIndices = new Integer[]{((CMatrix) obj.getBaseCMatrices().get(0)).getIndexOfColName(columnNode.getName())};
                    }

                    //find dat 
                    Double v0, v1;
                    int subMatrixWidth = (int) Math.round(cellWidth * 1.0 / matrices.size());

                    g2D.setColor(UI.colorUSUKI);
                    for (int mIndex = 0; mIndex < matrices.size(); mIndex++) {
                        int xAnchor = anchorX + Math.round(cellWidth * mIndex / matrices.size());
                        int yAnchor = anchorY;
                        int sWidth = subMatrixWidth;
                        int sHeight = (int) Math.round(cellHeight * 1.0f / rowIndices.length);

                        CMatrix mx = matrices.get(mIndex);

                        //need to split rows
                        int iC = 0;

                        for (Integer i : rowIndices) {

                            yAnchor = Math.round(anchorY + iC * cellHeight * 1.0f / rowIndices.length);
                            if (i == null || i < 0) {
                                continue;

                            }

                            if (colIndices.length > 1) {
                                //loop through all js
                                for (int jC = 0; jC < colIndices.length - 1; jC++) {

                                    try {
                                        Integer j = colIndices[jC];
                                        Integer j1 = colIndices[jC + 1];

                                        //get the v0
                                        v0 = (Double) mx.getValue(i, j);
                                        v1 = (Double) mx.getValue(i, j1);

                                        int heightV0 = (int) Math.round(sHeight * (v0 - _minValue) / (_maxValue - _minValue));
                                        int heightV1 = (int) Math.round(sHeight * (v1 - _minValue) / (_maxValue - _minValue));

                                        //System.out.println("xAnchor:yAnchor:sWidth:sHeight:  " + xAnchor + " " + yAnchor + " " + sWidth + " " + sHeight);
//                                System.out.println(jC * 1.0 / colIndices.length * sWidth);
                                        int x0 = (int) Math.round(xAnchor + (jC) * 1.0 / colIndices.length * sWidth + sWidth * 1.0 / colIndices.length / 2);
                                        int y0 = (int) (yAnchor + sHeight - heightV0);
                                        int x1 = (int) Math.round(xAnchor + (jC + 1) * 1.0 / colIndices.length * sWidth + sWidth * 1.0 / colIndices.length / 2);
                                        int y1 = (int) (yAnchor + sHeight - heightV1);

//                                int w = (int) Math.round(sWidth * 1.0 / colIndices.length);
//                                int h = heightV0;
                                        //System.out.println("small bar dim:" + x + " " + y + " " + w + " " + h);
//                                g2D.fillRect(
//                                        x,
//                                        y,
//                                        w,
//                                        h);
                                        g2D.setStroke(UI.stroke1);
                                        g2D.drawLine(x0, y0, x1, y1);

                                    } catch (Exception e) {

                                    }

                                }//end of for
                            } else {
                                //draw a single dot
                                Integer j = colIndices[0];
                                v0 = (Double) mx.getValue(i, j);
                                int heightV0 = (int) Math.round(sHeight * (v0 - _minValue) / (_maxValue - _minValue));
                                int x0 = (int) Math.round(xAnchor + (0) * 1.0 / colIndices.length * sWidth + sWidth * 1.0 / colIndices.length / 2);
                                int y0 = (int) (yAnchor + sHeight - heightV0);
                                g2D.setStroke(UI.stroke2);
                                g2D.drawLine(x0, y0, x0, y0);
                            }

                            iC++;
                        }

                    }//end of for

                }

            } catch (Exception e) {
//                g2D.setColor(UI.colorUSUKI);
                int centerX = anchorX + cellWidth / 2;
                int heightV = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));

//                g2D.fillOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
                g2D.setColor(UI.colorUSUKI);
                g2D.fillOval(centerX - 1, anchorY + cellHeight - heightV - 1, 3, 3);
                g2D.setColor(UI.colorBlack1);
                g2D.drawLine(anchorX, anchorY + cellHeight - 1, anchorX + cellWidth, anchorY + cellHeight - 1);

            }

        }
    }

    @Override
    protected void updateLegend() {
        int width = DEFAULT_LEGEND_WIDTH;
        int height = DEFAULT_LEGENT_HEIGHT;
        legend = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        Graphics2D g = (Graphics2D) legend.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setPaint(UI.colorBlack2);
        g.fillRoundRect(0, 0, width, height - 12, 5, 5);

        g.setColor(UI.colorUSUKI);
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

    @Override
    public Image getSubTip(CoolMapObject obj, VNode rowNode, VNode columnNode, float percentX, float PercentY, int cellWidth, int cellHeight) {
        try {
            if (rowNode.isSingleNode() && columnNode.isSingleNode()) {
                return null;
            }

            List<CMatrix> matrices = obj.getBaseCMatrices();
            int matIndex = (int) (percentX * matrices.size());
            CMatrix mat = matrices.get(matIndex);

            Integer[] rowIndices;
            Integer[] colIndices;
            if (rowNode.isGroupNode()) {
                rowIndices = rowNode.getBaseIndicesFromCOntology((CMatrix) obj.getBaseCMatrices().get(0), COntology.ROW);
            } else {
                rowIndices = new Integer[]{((CMatrix) obj.getBaseCMatrices().get(0)).getIndexOfRowName(rowNode.getName())};
            }
            if (columnNode.isGroupNode()) {
                colIndices = columnNode.getBaseIndicesFromCOntology((CMatrix) obj.getBaseCMatrices().get(0), COntology.COLUMN);
            } else {
                colIndices = new Integer[]{((CMatrix) obj.getBaseCMatrices().get(0)).getIndexOfColName(columnNode.getName())};
            }

            int rowIndex = (int) (PercentY * rowIndices.length);

            float subPercent = (percentX - 1.0f * matIndex / matrices.size()) * matrices.size();
            if (subPercent < 0) {
                subPercent = 0;
            } else if (subPercent > 1) {
                subPercent = 1;
            }

            int colIndex = (int) (colIndices.length * subPercent);

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

            int rowI = rowIndices[rowIndex];
            int colI = colIndices[colIndex];
            Double val = (Double) mat.getValue(rowI, colI);
            String rowLabel = mat.getRowLabel(rowI);
            String colLabel = mat.getColLabel(colI);

            String htmlLabel = "<html><table cellspacing='1' border='0' cellpadding='1'>"
                    + ((matrices.size() > 1) ? "<tr><td><strong>Data: </strong></td><td>" + matrices.get(matIndex).getName() + "</td></tr>" : "")
                    + "<tr><td><strong>Row: </strong></td><td>" + rowLabel + "</td></tr><tr><td><strong>Column: </strong></td><td>" + colLabel + "</td></tr><tr><td><strong>Value: </strong></td><td><span style='color:#020202;font-weight:bold;'>" + df.format(val) + "</span></td></tr></table></html>";

            toolTipLabel.setText(htmlLabel);

            return createToolTipFromJLabel(toolTipLabel);

        } catch (Exception e) {
            return null;
        }
    }

    private final JLabel toolTipLabel;

    @Override
    public void renderCellHD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            //need to draw to lines with left and right, well.

            try {
                if (rowNode.isSingleNode() && columnNode.isSingleNode()) {
                    g2D.setColor(UI.colorBlack3);
                    g2D.fillRect(anchorX, anchorY, cellWidth, cellHeight);

                    g2D.setStroke(UI.stroke1_5);
                    Float columnIndex = columnNode.getViewIndex();
                    if (columnIndex == null || columnIndex < 0 || columnIndex >= getCoolMapObject().getViewNumColumns()) {
                        return;
                    }

                    //Else draw something
                    int nextIndex = columnIndex.intValue() + 1;
                    int centerX = anchorX + cellWidth / 2;
                    int heightV = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));
                    try {
                        Double nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);

                        //need to find center of these two columnNodes
                        VNode nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                        int nextCenterX = (int) (anchorX + cellWidth + nextColumnNode.getViewSizeInMap(getCoolMapObject().getCoolMapView().getZoomX()) / 2);

                        //cell height are the same
                        int heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));

                        g2D.setColor(UI.colorUSUKI);

                        g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);

                    } catch (Exception e) {
                    }

                    //bad fix -> as 4 threads, the block can't span...
                    try {
                        nextIndex = columnIndex.intValue() - 1;
                        Double nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);
                        VNode nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                        int nextCenterX = (int) (anchorX - nextColumnNode.getViewSizeInMap(getCoolMapObject().getCoolMapView().getZoomX()) / 2);
                        int heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));
                        g2D.setColor(UI.colorUSUKI);
                        g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);

                    } catch (Exception e) {

                    }

                    g2D.setStroke(UI.stroke2);

//                    g2D.setColor(UI.colorWhite);
//                    g2D.drawOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
                    g2D.setColor(UI.colorUSUKI);
                    g2D.fillOval(centerX - 1, anchorY + cellHeight - heightV - 1, 3, 3);

                    g2D.setColor(UI.colorBlack1);
                    g2D.drawLine(anchorX, anchorY + cellHeight - 1, anchorX + cellWidth, anchorY + cellHeight - 1);
                } else {
                    //draw group nodes
                    //group nodes
                    g2D.setColor(UI.colorBlack5);
                    g2D.fillRect(anchorX, anchorY, cellWidth, cellHeight);

                    //ok then split
                    CoolMapObject obj = getCoolMapObject();
                    List<CMatrix> matrices = obj.getBaseCMatrices();

                    //find the indices
                    Integer[] rowIndices;
                    Integer[] colIndices;
                    if (rowNode.isGroupNode()) {
                        rowIndices = rowNode.getBaseIndicesFromCOntology((CMatrix) obj.getBaseCMatrices().get(0), COntology.ROW);
                    } else {
                        rowIndices = new Integer[]{((CMatrix) obj.getBaseCMatrices().get(0)).getIndexOfRowName(rowNode.getName())};
                    }
                    if (columnNode.isGroupNode()) {
                        colIndices = columnNode.getBaseIndicesFromCOntology((CMatrix) obj.getBaseCMatrices().get(0), COntology.COLUMN);
                    } else {
                        colIndices = new Integer[]{((CMatrix) obj.getBaseCMatrices().get(0)).getIndexOfColName(columnNode.getName())};
                    }

                    //find dat 
                    Double v0, v1;
                    int subMatrixWidth = (int) Math.round(cellWidth * 1.0 / matrices.size());

                    for (int mIndex = 0; mIndex < matrices.size(); mIndex++) {
                        int xAnchor = anchorX + Math.round(cellWidth * mIndex / matrices.size());
                        int yAnchor = anchorY;
                        int sWidth = subMatrixWidth;
                        int sHeight = (int) Math.round(cellHeight * 1.0f / rowIndices.length);

                        CMatrix mx = matrices.get(mIndex);

                        //need to split rows
                        int iC = 0;

                        for (Integer i : rowIndices) {

                            yAnchor = Math.round(anchorY + iC * cellHeight * 1.0f / rowIndices.length);

                            g2D.setColor(UI.colorBlack2);
                            g2D.setStroke(UI.stroke1_5);
                            g2D.drawLine(xAnchor, yAnchor + sHeight, xAnchor + subMatrixWidth, yAnchor + sHeight);

                            if (i == null || i < 0) {
                                continue;

                            }

                            //loop through all js
                            if (colIndices.length > 1) {
                                for (int jC = 0; jC < colIndices.length - 1; jC++) {

                                    try {
                                        Integer j = colIndices[jC];
                                        Integer j1 = colIndices[jC + 1];

                                        //get the v0
                                        v0 = (Double) mx.getValue(i, j);
                                        v1 = (Double) mx.getValue(i, j1);

                                        int heightV0 = (int) Math.round(sHeight * (v0 - _minValue) / (_maxValue - _minValue));
                                        int heightV1 = (int) Math.round(sHeight * (v1 - _minValue) / (_maxValue - _minValue));

                                        //System.out.println("xAnchor:yAnchor:sWidth:sHeight:  " + xAnchor + " " + yAnchor + " " + sWidth + " " + sHeight);
//                                System.out.println(jC * 1.0 / colIndices.length * sWidth);
                                        int x0 = (int) Math.round(xAnchor + (jC) * 1.0 / colIndices.length * sWidth + sWidth * 1.0 / colIndices.length / 2);
                                        int y0 = (int) (yAnchor + sHeight - heightV0);
                                        int x1 = (int) Math.round(xAnchor + (jC + 1) * 1.0 / colIndices.length * sWidth + sWidth * 1.0 / colIndices.length / 2);
                                        int y1 = (int) (yAnchor + sHeight - heightV1);

                                        int w = (int) Math.round(sWidth * 1.0 / colIndices.length);
//                                int h = heightV0;
                                        //System.out.println("small bar dim:" + x + " " + y + " " + w + " " + h);
//                                g2D.fillRect(
//                                        x,
//                                        y,
//                                        w,
//                                        h);

                                        g2D.setColor(UI.colorUSUKI);
                                        g2D.setStroke(UI.stroke1_5);
                                        g2D.drawLine(x0, y0, x1, y1);

//                                        g2D.setColor(UI.colorUSUKI);
                                        g2D.fillOval(x0 - 1, y0 - 1, 3, 3);
                                        g2D.fillOval(x1 - 1, y1 - 1, 3, 3);

                                    } catch (Exception e) {

                                    }

                                }//end of column
                            } else {
                                g2D.setColor(UI.colorUSUKI);
                                Integer j = colIndices[0];
                                v0 = (Double) mx.getValue(i, j);
                                int heightV0 = (int) Math.round(sHeight * (v0 - _minValue) / (_maxValue - _minValue));
                                int x0 = (int) Math.round(xAnchor + (0) * 1.0 / colIndices.length * sWidth + sWidth * 1.0 / colIndices.length / 2);
                                int y0 = (int) (yAnchor + sHeight - heightV0);
                                g2D.setStroke(UI.stroke1_5);
                                g2D.drawLine(x0, y0, x0, y0);
                            }
                            iC++;
                        }//end of row

                    }//end of for
                }
            } catch (Exception e) {

                g2D.setColor(UI.colorUSUKI);
                int centerX = anchorX + cellWidth / 2;
                int heightV = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));

//                g2D.setColor(UI.colorWhite);
//                g2D.drawOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
//                g2D.setColor(UI.colorUSUKI);
//                g2D.fillOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
                g2D.setColor(UI.colorUSUKI);
                g2D.fillOval(centerX - 1, anchorY + cellHeight - heightV - 1, 3, 3);

                g2D.setColor(UI.colorBlack1);
                g2D.drawLine(anchorX, anchorY + cellHeight - 1, anchorX + cellWidth, anchorY + cellHeight - 1);
            }

        }
    }

    @Override
    public void renderCellLD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            //need to draw to lines with left and right, well.
            try {
                g2D.setStroke(UI.stroke1);
                Float columnIndex = columnNode.getViewIndex();
                if (columnIndex == null || columnIndex < 0 || columnIndex >= getCoolMapObject().getViewNumColumns()) {
                    return;
                }

                float effectiveZoomX = cellWidth / columnNode.getCurrentViewMultiplier();
                //Else draw something
                int nextIndex = columnIndex.intValue() + 1;
                int centerX = anchorX + cellWidth / 2;
                int heightV = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));
                try {
                    Double nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);

                    //need to find center of these two columnNodes
                    VNode nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                    int nextCenterX = (int) (anchorX + cellWidth + nextColumnNode.getViewSizeInMap(effectiveZoomX) / 2);

                    //cell height are the same
                    int heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));

                    g2D.setColor(UI.colorUSUKI);

                    g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);
                } catch (Exception e) {
                }

                //bad fix -> as 4 threads, the block can't span...
                try {
                    nextIndex = columnIndex.intValue() - 1;
                    Double nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);
                    VNode nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                    int nextCenterX = (int) (anchorX - nextColumnNode.getViewSizeInMap(effectiveZoomX) / 2);
                    int heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));
                    g2D.setColor(UI.colorUSUKI);
                    g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);

                } catch (Exception e) {
                }

//                g2D.setColor(UI.colorUSUKI);
//                g2D.fillOval(centerX - 2, anchorY + cellHeight - heightV0 - 2, 4, 4);
            } catch (Exception e) {
            }

        }
    }

}
