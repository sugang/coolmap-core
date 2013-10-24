/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.viewmaps;

import coolmap.data.CoolMapObject;
import coolmap.data.annotation.PointAnnotation;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sugang
 */
public class PointAnnotationLayer implements MapLayer<Object, Object> {

    private CoolMapObject _object;
    private Font labelFont;
    private float fontSize = 12f;
    private int margin = 10;
    private Color labelBackgroundColor;
    private Color shadowColor;
    
    private boolean render = true;
    
    public void setRender(boolean r){
        render = r;
    }
    
    
    public PointAnnotationLayer(CoolMapObject object) {
        _object = object;
        labelFont = UI.fontPlain.deriveFont(fontSize).deriveFont(Font.BOLD);
        labelBackgroundColor = UI.colorGrey2;
        shadowColor = UI.mixOpacity(UI.colorBlack2, 0.5f);
    }
    

    @Override
    public void render(Graphics2D g2D, CoolMapObject<Object, Object> coolMapObject, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int width, int height) throws Exception {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        if(!render)
            return;
        
//        System.err.println("\n\n annotation layer rendered ");
//        g2D.setColor(Color.RED);
//        g2D.fillRect(0, 0, width, height);
        VNode anchorRowNode = coolMapObject.getViewNodeRow(fromRow);
        VNode anchorColNode = coolMapObject.getViewNodeColumn(fromCol);

        if (anchorRowNode == null || anchorColNode == null || anchorRowNode.getViewOffset() == null || anchorColNode.getViewOffset() == null) {
            return;
        }

        int anchorX, anchorY, cellWidth, cellHeight;

        ArrayList<PointAnnotation> annotations = coolMapObject.getAnnotationStorage().getAnnotations();

//        System.err.println("Annotations:" + annotations.size());
        for (PointAnnotation pa : annotations) {
            String rowNodeName = pa.getRowNodeName();
            String colNodeName = pa.getColumnNodeName();
            String rowNodeOntoID = pa.getRowNodeOntologyID();
            String colNodeOntoID = pa.getColumnNodeOntologyID();
            String annotation = pa.getAnnotation();

            //or i can forget about ontology -> different ontologies - > yak!
            //check whether is in current view
            //check colNodes
            List<VNode> colNodes = coolMapObject.getViewNodesColumn(colNodeName);
            if (colNodes == null || colNodes.isEmpty()) {
                continue;
            }
//            System.err.println(colNodes);

            //check rowNodes
            List<VNode> rowNodes = coolMapObject.getViewNodesRow(rowNodeName);
            if (rowNodes == null || rowNodes.isEmpty()) {
                continue;
            }
//            System.err.println(rowNodes);

            //also check row + col
            //There are nodes within view
            List<VNode> colNodesInView = new ArrayList<VNode>();
            List<VNode> rowNodesInView = new ArrayList<VNode>();

            ////&& Objects.equal(node.getCOntology().getID(), colNodeOntoID)
            for (VNode node : colNodes) {
                if (node.getViewIndex() != null && node.getViewIndex() >= fromCol && node.getViewIndex() < toCol) {
                    COntology o = node.getCOntology();
//                    if( o == null && colNodeOntoID != null){}
////                        continue;
//                    else 
                    if (o == null && colNodeOntoID == null || o != null && o.getID().equals(colNodeOntoID)) {
                        colNodesInView.add(node);
                    }
                }
            }

            ////&& Objects.equal(node.getCOntology().getID(), rowNodeOntoID)
            for (VNode node : rowNodes) {
                if (node.getViewIndex() != null && node.getViewIndex() >= fromRow && node.getViewIndex() < toRow) {
                    COntology o = node.getCOntology();

                    if (o == null && rowNodeOntoID == null || o != null && o.getID().equals(rowNodeOntoID)) {
                        rowNodesInView.add(node);
                    }
                }
            }

            if (colNodesInView.isEmpty() || rowNodesInView.isEmpty()) {
                continue;
            }

            //then; Let US try!
            g2D.setFont(labelFont);

            for (VNode rowNode : rowNodesInView) {
                anchorY = Math.round(rowNode.getViewOffset() - anchorRowNode.getViewOffset());
                cellHeight = Math.round(rowNode.getViewSizeInMap(zoomY));
                for (VNode colNode : colNodesInView) {
                    //then paint this! Finally it is a valid one
                    anchorX = Math.round(colNode.getViewOffset() - anchorColNode.getViewOffset());
                    cellWidth = Math.round(colNode.getViewSizeInMap(zoomX));

//                    g2D.setColor(Color.BLUE);
                    g2D.setColor(UI.colorLightGreen6);
                    g2D.setStroke(UI.stroke2);
                    g2D.drawRoundRect(anchorX, anchorY, cellWidth, cellHeight, 2, 2);

                    //g2D.setColor(Color.WHITE);
                    //TextLayout layout = new TextLayout(pa.getAnnotation(), labelFont, g2D.getFontRenderContext());
                    //System.out.println(layout.getBounds());
                    //layout.draw(g2D, anchorX + cellWidth, anchorY + cellHeight);
                    if (annotation == null || annotation.length() == 0) {
                        continue;
                    }

                    g2D.setStroke(UI.strokeDash1_5);
                    g2D.drawLine(anchorX + cellWidth + 1, anchorY + cellHeight + 1, anchorX + cellWidth + 5, anchorY + cellHeight + 5);

                    String[] lines = annotation.split("\n", -1);
                    int labelWidth = 0;
                    for (String l : lines) {
                        int lw = g2D.getFontMetrics().stringWidth(l);
                        if (lw > labelWidth) {
                            labelWidth = lw;
                        }
                    }

                    labelWidth += margin * 2;
                    int labelHeight = (int) (lines.length * fontSize + margin * 2);

                    g2D.setColor(shadowColor);
                    g2D.fillRoundRect(anchorX + cellWidth + 5 + 3, anchorY + cellHeight + 5 + 3, labelWidth, labelHeight, 5, 5);

                    g2D.setColor(pa.getBackgroundColor());
                    g2D.fillRoundRect(anchorX + cellWidth + 5, anchorY + cellHeight + 5, labelWidth, labelHeight, 5, 5);

                    int offset = (int) fontSize;
                    g2D.setColor(pa.getFontColor());
                    for (String line : lines) {
                        g2D.drawString(line, anchorX + cellWidth + 5 + margin, anchorY + cellHeight + offset + margin + 2);
                        offset += fontSize;
                    }

                }
            }

        }

    }

}
