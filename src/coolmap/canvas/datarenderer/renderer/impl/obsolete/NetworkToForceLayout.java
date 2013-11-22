/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl.obsolete;

import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.network.LNetwork;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * do need to define a network layout, probably from the edge lists
 *
 * @author gangsu
 */
public class NetworkToForceLayout extends ViewRenderer<LNetwork> {
    
    public NetworkToForceLayout(){
        setName("LNetwork to FLayout");
        setDescription("Draw a network into force layout");
    }
    

    @Override
    public void initialize() {
        //set up basic parameters
       
    }
    
    //
    public void forceRecomputeLayout(){
        CoolMapObject object = getCoolMapObject(); // then do the job
    }
    

    @Override
    public boolean canRender(Class<?> viewClass) {
        try {
            if (LNetwork.class.isAssignableFrom(viewClass)) {
                return true;
            }
            else{
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    public void prepareGraphics(Graphics2D g2D) {
    }

    @Override
    public void renderCellLD(LNetwork v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //
    }

    @Override
    public void renderCellSD(LNetwork v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //
        //System.out.println("network:" + v);
        BufferedImage image = v.drawNetwork(Math.round(cellWidth), Math.round(cellHeight));
        g2D.drawImage(image, Math.round(anchorX), Math.round(anchorY), null);
    }

    @Override
    public void renderCellHD(LNetwork v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //more refined network
        renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
    }

    @Override
    public void postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    public void updateRendererChanges() {
    }
}
