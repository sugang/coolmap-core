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
    protected void _preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    protected void _prepareGraphics(Graphics2D g2D) {
    }

    @Override
    protected void _renderCellLD(LNetwork v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //
    }

    @Override
    protected void _renderCellSD(LNetwork v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //
        //System.out.println("network:" + v);
        BufferedImage image = v.drawNetwork(Math.round(cellWidth), Math.round(cellHeight));
        g2D.drawImage(image, Math.round(anchorX), Math.round(anchorY), null);
    }

    @Override
    protected void _renderCellHD(LNetwork v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //more refined network
        _renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
    }

    @Override
    protected void _postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    protected void updateRendererChanges() {
    }
}
