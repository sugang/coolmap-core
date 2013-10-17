/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.viewmaps;

import coolmap.data.CoolMapObject;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * a layer that only responds 
 * @author gangsu
 */
public interface MapLayer<BASE, VIEW> {
    
    /**
     * whether the mapLayer draws directly using graphics2D
     * @return 
     */
//    private boolean _isDirectDraw = false;
//    
//    
//    public boolean isDirectDraw(){
//        return _isDirectDraw;
//    };
    
//    public void setDirectDraw(boolean directDraw){
//        _isDirectDraw = directDraw;
//    }
    
    /**
     * render the sub region of coolMapObject into [minRow, minCol, maxRow, maxCol)
     * from 0,0 to width, height, with the minRow maxRow minCol maxCol
     * @param g2D
     * @param coolMapObject
     * @param minRow
     * @param maxRow
     * @param minCol
     * @param maxCol
     * @param mapWidth
     * @param mapHeight 
     */
    public void render(final Graphics2D g2D, final CoolMapObject<BASE, VIEW> coolMapObject, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int width, int height) throws Exception;
    
    //public abstract BufferedImage getRenderedImage(final CoolMapObject coolMapObject, float minRow, float minCol, float maxRow, float maxCol, int mapWidth, int mapHeight);
    
//    public abstract void updateRenderedImage();
    
//    public final String getUniqueName(){
//        return this.getClass().getName();
//    };
    /**
     * redraws the map only within selection
     * @return 
     */
    
    
}
