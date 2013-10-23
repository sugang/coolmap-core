/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.viewmaps;

import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.graphics.UI;
import java.awt.Graphics2D;

/**
 *
 * @author gangsu
 */
public class FilterLayer implements MapLayer<Object, Object> {

    @Override
    public void render(Graphics2D g2D, CoolMapObject<Object, Object> coolMapObject, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int width, int height) throws Exception {
        
//        System.err.println("Filter layer rendered");
        
        VNode anchorRowNode = coolMapObject.getViewNodeRow(fromRow);
        VNode anchorColNode = coolMapObject.getViewNodeColumn(fromCol);
        
        if(anchorRowNode == null || anchorColNode == null || anchorRowNode.getViewOffset() == null || anchorColNode.getViewOffset() == null){
            return;
        }
        
        int anchorX, anchorY, cellWidth, cellHeight;
        for(int i=fromRow; i<toRow; i++){
            VNode rowNode = coolMapObject.getViewNodeRow(i);
            anchorY = Math.round( rowNode.getViewOffset() - anchorRowNode.getViewOffset());
            
            cellHeight = Math.round(rowNode.getViewSizeInMap(zoomY));
            for(int j=fromCol; j<toCol; j++){
                VNode colNode = coolMapObject.getViewNodeColumn(j);
                anchorX = Math.round( colNode.getViewOffset() - anchorColNode.getViewOffset());
                cellWidth = Math.round(colNode.getViewSizeInMap(zoomX));
                
                if(!coolMapObject.canPass(i, j)){
                    g2D.setColor(UI.colorBlack1);
                    g2D.fillRect(anchorX, anchorY, cellWidth, cellHeight);
                }
                
            }
        }
        
    }
    
}
