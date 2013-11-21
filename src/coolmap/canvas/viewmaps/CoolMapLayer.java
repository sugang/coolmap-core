/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.viewmaps;

import coolmap.data.CoolMapObject;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author gangsu
 */
public class CoolMapLayer<BASE, VIEW> implements MapLayer<BASE, VIEW> {

//    @Override
//    public void render(Graphics2D g2D, CoolMapObject<BASE, VIEW> coolMapObject, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int width, int height) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
    private final CoolMapObject _coolMapObject;
    
    
    public CoolMapLayer(CoolMapObject object){
        _coolMapObject = object;
    }
    
    @Override
    public void render(final Graphics2D g2D, final CoolMapObject<BASE, VIEW> coolMapObject, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int width, int height) {
        
        try{
            BufferedImage image = _coolMapObject.getViewRenderer().getRenderedMap(_coolMapObject, fromRow, toRow, fromCol, toCol, zoomX, zoomY);            
            g2D.drawImage(image, 0, 0, _coolMapObject.getCoolMapView().getViewCanvas());
        
        }
        catch(InterruptedException ie){
//            System.out.println("Interrupted");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        
        
//        g2D.setColor(Color.WHITE);
//        g2D.setStroke(UI.stroke1);
//        //g2D.setColor(Color.BLACK);
//        //why part of the map is drawn w/black?
//        
//        //the from row and to row view offset can be nul sometimes.
//        
//        int anchorRow = coolMapObject.getViewNodeRow(fromRow).getViewOffset().intValue();
//
//
////        for (int i = (int) fromRow; i < (int) toRow; i++) {
////            VNode node = coolMapObject.getViewNodeRow(i);
////            int viewOffset = node.getViewOffset().intValue(); //should not use subMapdimension.y as the anchor. may not be correctly
////            //System.out.println("Drawing lines....");
////            g2D.drawLine(0, viewOffset - anchorRow, width, viewOffset - anchorRow); //
////        }
//
//        //g2D.drawLine(0, subMapDimension.height, subMapDimension.width, subMapDimension.height);
//
//
//        int anchorCol = coolMapObject.getViewNodeColumn(fromCol).getViewOffset().intValue();
//
//
//        for (int i = (int) fromCol; i < (int) toCol; i++) {
//            VNode node = coolMapObject.getViewNodeCol(i);
//            int viewOffset = node.getViewOffset().intValue(); //should not use subMapdimension.y as the anchor. may not be correctly
//            //System.out.println("Drawing lines....");
//            g2D.drawLine(viewOffset - anchorCol, 0, viewOffset - anchorCol, height); //
//        }

        //g2D.drawLine(subMapDimension.width, 0, subMapDimension.width, subMapDimension.height);
//        for (int i = fromRow; i < toRow; i++) {
//            VNode rowNode = coolMapObject.getViewNodeRow(i);
//            if(rowNode.getViewOffset() == null){
//                return;
//            }
//            
//            int y = (int) (rowNode.getViewOffset() - anchorRow);
//            //There should not be a case when viewoffset is null. If do so, then
//            
//            
//            int cellHeight = (int) rowNode.getViewSizeInMap(zoomY);
//
//            for (int j = fromCol; j < toCol; j++) {
//                VNode colNode = coolMapObject.getViewNodeColumn(j);
//                int x = (int) (colNode.getViewOffset() - anchorCol);
//                int cellWidth = (int) colNode.getViewSizeInMap(zoomX);
//                if(colNode.getViewOffset() == null){
//                    return;
//                }
//                
//                
//                Double value = (Double)coolMapObject.getViewValue(i, j);
//                if(value != null){
//                    Color color = UI.mixOpacity(UI.colorOrange0, value.floatValue());
//                    g2D.setColor(color);
//                    g2D.fillRect(x, y, cellWidth, cellHeight);
//                }
//            }
//        }



    }
}
