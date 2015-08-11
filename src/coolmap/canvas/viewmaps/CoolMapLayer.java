package coolmap.canvas.viewmaps;

import coolmap.data.CoolMapObject;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gangsu
 * @param <BASE>
 * @param <VIEW>
 */
public class CoolMapLayer<BASE, VIEW> implements MapLayer<BASE, VIEW> {

    private final CoolMapObject _coolMapObject;
    
    
    public CoolMapLayer(CoolMapObject object){
        _coolMapObject = object;
    }
    
    @Override
    public void render(final Graphics2D g2D, final CoolMapObject<BASE, VIEW> coolMapObject, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int width, int height) {
        
        try {
            BufferedImage image = _coolMapObject.getViewRenderer().getRenderedMap(_coolMapObject, fromRow, toRow, fromCol, toCol, zoomX, zoomY);
            if (image == null) return;
            g2D.drawImage(image, 0, 0, _coolMapObject.getCoolMapView().getViewCanvas());      
        } catch(Exception e){
            System.err.println("Minor issue when trying to render a coolMap layer map");
            Logger.getLogger(CoolMapLayer.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
