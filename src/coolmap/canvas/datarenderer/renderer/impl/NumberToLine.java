/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl;

import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.graphics.UI;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.text.DecimalFormat;

/**
 *
 * @author sugang
 */
public class NumberToLine extends NumberToBar {

    public NumberToLine() {
        super();
        setName("Number To Line");
        setDescription("Use consequtivelines to connect dots");
        setClipCell(true); //make rendering possible to span to neighbor cells
    }

    @Override
    public void renderCellSD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            //need to draw to lines with left and right, well.
            try {
                g2D.setColor(UI.colorBlack3);
                g2D.fillRect(anchorX, anchorY, cellWidth, cellHeight);

                g2D.setStroke(UI.stroke1_5);
                Float columnIndex = columnNode.getViewIndex();
                if (columnIndex == null || columnIndex < 0 || columnIndex >= getCoolMapObject().getViewNumColumns()) {
                    return;
                }

                //Else draw something
                int nextIndex = columnIndex.intValue() + 1;
                VNode nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                if (nextColumnNode == null) {
                    throw new Exception();
                }

                Double nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);

                //need to find center of these two columnNodes
                int centerX = anchorX + cellWidth / 2;
                int nextCenterX = (int) (anchorX + cellWidth + nextColumnNode.getViewSizeInMap(getCoolMapObject().getCoolMapView().getZoomX()) / 2);

                int heightV = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));
                //cell height are the same
                int heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));

                g2D.setColor(UI.colorTSUYUKUSA);

                g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);

                //bad fix -> as 4 threads, the block can't span...
                nextIndex = columnIndex.intValue() - 1;
                nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                if (nextColumnNode == null) {
                    throw new Exception();
                }

                nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);
                nextCenterX = (int) (anchorX - nextColumnNode.getViewSizeInMap(getCoolMapObject().getCoolMapView().getZoomX()) / 2);
                heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));
                g2D.setColor(UI.colorTSUYUKUSA);
                g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);

                g2D.setColor(UI.colorMIZU);
                g2D.fillOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);

                g2D.setColor(UI.colorBlack1);
                g2D.drawLine(anchorX, anchorY + cellHeight - 1, anchorX + cellWidth, anchorY + cellHeight - 1);
            } catch (Exception e) {
                g2D.setColor(UI.colorMIZU);
                int centerX = anchorX + cellWidth / 2;
                int heightV = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));
                g2D.fillOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
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

        g.setColor(UI.colorTSUYUKUSA);
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
    public void renderCellHD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
                if (v == null || v.isNaN()) {
            //System.out.println(v);
            _markNull(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
        } else {
            //need to draw to lines with left and right, well.
            try {
                g2D.setColor(UI.colorBlack3);
                g2D.fillRect(anchorX, anchorY, cellWidth, cellHeight);

                g2D.setStroke(UI.strokeDash1_5);
                Float columnIndex = columnNode.getViewIndex();
                if (columnIndex == null || columnIndex < 0 || columnIndex >= getCoolMapObject().getViewNumColumns()) {
                    return;
                }

                //Else draw something
                int nextIndex = columnIndex.intValue() + 1;
                VNode nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                if (nextColumnNode == null) {
                    throw new Exception();
                }

                Double nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);

                //need to find center of these two columnNodes
                int centerX = anchorX + cellWidth / 2;
                int nextCenterX = (int) (anchorX + cellWidth + nextColumnNode.getViewSizeInMap(getCoolMapObject().getCoolMapView().getZoomX()) / 2);

                int heightV = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));
                //cell height are the same
                int heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));

                g2D.setColor(UI.colorTSUYUKUSA);

                g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);

                //bad fix -> as 4 threads, the block can't span...
                nextIndex = columnIndex.intValue() - 1;
                nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                if (nextColumnNode == null) {
                    throw new Exception();
                }

                nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);
                nextCenterX = (int) (anchorX - nextColumnNode.getViewSizeInMap(getCoolMapObject().getCoolMapView().getZoomX()) / 2);
                heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));
                g2D.setColor(UI.colorTSUYUKUSA);
                g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);

                g2D.setStroke(UI.stroke2);
                
                g2D.setColor(UI.colorWhite);
                g2D.drawOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
                
                g2D.setColor(UI.colorMIZU);
                g2D.fillOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
                

                g2D.setColor(UI.colorBlack1);
                g2D.drawLine(anchorX, anchorY + cellHeight - 1, anchorX + cellWidth, anchorY + cellHeight - 1);
                
            } catch (Exception e) {
                
                g2D.setColor(UI.colorMIZU);
                int centerX = anchorX + cellWidth / 2;
                int heightV = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));
                
                
                g2D.setColor(UI.colorWhite);
                g2D.drawOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
                
                g2D.setColor(UI.colorMIZU);
                g2D.fillOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
                
                
                
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

                //Else draw something
                int nextIndex = columnIndex.intValue() + 1;
                VNode nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                if (nextColumnNode == null) {
                    throw new Exception();
                }

                Double nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);

                //need to find center of these two columnNodes
                int centerX = anchorX + cellWidth / 2;
                int nextCenterX = (int) (anchorX + cellWidth + nextColumnNode.getViewSizeInMap(getCoolMapObject().getCoolMapView().getZoomX()) / 2);

                int heightV = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));
                //cell height are the same
                int heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));

                g2D.setColor(UI.colorTSUYUKUSA);

                g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);

                //bad fix -> as 4 threads, the block can't span...
                nextIndex = columnIndex.intValue() - 1;
                nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                nextColumnNode = getCoolMapObject().getViewNodeColumn(nextIndex);
                if (nextColumnNode == null) {
                    throw new Exception();
                }

                nextV = (Double) getCoolMapObject().getViewValue(rowNode.getViewIndex().intValue(), nextIndex);
                nextCenterX = (int) (anchorX - nextColumnNode.getViewSizeInMap(getCoolMapObject().getCoolMapView().getZoomX()) / 2);
                heightNext = (int) Math.round(cellHeight * (nextV - _minValue) / (_maxValue - _minValue));
                g2D.setColor(UI.colorTSUYUKUSA);
                g2D.drawLine(centerX, anchorY + cellHeight - heightV, nextCenterX, anchorY + cellHeight - heightNext);

//                g2D.setColor(UI.colorMIZU);
//                g2D.fillOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
            } catch (Exception e) {
                g2D.setColor(UI.colorMIZU);
                int centerX = anchorX + cellWidth / 2;
                int heightV = (int) Math.round(cellHeight * (v - _minValue) / (_maxValue - _minValue));
                g2D.fillOval(centerX - 2, anchorY + cellHeight - heightV - 2, 4, 4);
            }

        }
    }

}
