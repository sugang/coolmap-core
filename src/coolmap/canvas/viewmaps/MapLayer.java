package coolmap.canvas.viewmaps;

import coolmap.data.CoolMapObject;
import java.awt.Graphics2D;

/**
 * a layer that only responds 
 * @author gangsu
 * @param <BASE>
 * @param <VIEW>
 */
public interface MapLayer<BASE, VIEW> {
    
    /**
     * render the sub region of coolMapObject into [minRow, minCol, maxRow, maxCol)
     * from 0,0 to width, height, with the minRow maxRow minCol maxCol
     * @param g2D
     * @param coolMapObject 
     * @param fromRow 
     * @param toRow 
     * @param width 
     * @param fromCol 
     * @param toCol 
     * @param zoomY 
     * @param zoomX 
     * @param height 
     * @throws java.lang.Exception 
     */
    public void render(final Graphics2D g2D, final CoolMapObject<BASE, VIEW> coolMapObject, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int width, int height) throws Exception;  
}
