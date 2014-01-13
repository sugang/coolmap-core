/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.sidemaps;

import coolmap.canvas.CoolMapView;
import coolmap.canvas.listeners.CViewListener;
import coolmap.canvas.misc.MatrixCell;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.listeners.CObjectListener;
import coolmap.utils.StateSavable;
import coolmap.utils.graphics.UI;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.json.JSONObject;

/**
 *
 * @author gangsu
 */
public abstract class RowMap<BASE, VIEW> implements CViewListener, CObjectListener, StateSavable {

    private String _description = null;
    private final ViewPanel _viewPanel = new ViewPanel();
    private final static GraphicsConfiguration _graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private BufferedImage _mapBuffer;
    private final CoolMapObject<BASE, VIEW> _coolMapObject;
    private ImageIcon _icon;
    private Font _messageFont;
    //private AffineTransform _rotationTransform;
    
    public void destroy(){
        _mapBuffer = null;
    }
    
    public void clearBuffer(){
        _mapBuffer = null;
    }

    protected int getAnchorY() {
        if (_viewPanel.getParent() != null) {
            return _viewPanel.getParent().getY();
        } else {
            return 0;
        }
    }

    protected int getAnchorX() {
        if (_viewPanel.getParent() != null) {
            return _viewPanel.getParent().getX();
        } else {
            return 0;
        }
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    protected Point translateToCanvas(int x, int y) {
        return new Point((int)(x + getAnchorX()), (int)(y + getAnchorY()));
    }

    public final void setDescription(String description) {
        _description = description;
    }

    public RowMap(CoolMapObject object) {
        _coolMapObject = object;
        _icon = UI.getImageIcon("infoBW");
        AffineTransform rotationTransform = new AffineTransform();
        rotationTransform.rotate(-Math.PI / 2);
        _messageFont = UI.fontPlain.deriveFont(12f);
        
        getViewPanel().setName(getName());
    }
    
//    public void setCoolMapObject(CoolMapObject obj){
//        _coolMapObject = obj;
//    }

    public JComponent getViewPanel() {
        return _viewPanel;
    }

    public abstract JComponent getConfigUI();

    /**
     * paint underneath the buffer
     *
     * @param g2D
     * @param canvas
     */
    protected abstract void prePaint(final Graphics2D g2D, final CoolMapObject<BASE, VIEW> object, int width, int height);

    protected abstract void prepareRender(Graphics2D g2D);

    /**
     *
     * @param g2D
     * @param canvas
     * @param yOffset amount ot add to compensate the parent container Y
     */
    protected abstract void postPaint(final Graphics2D g2D, final CoolMapObject<BASE, VIEW> object, int width, int height);

    public abstract boolean canRender(CoolMapObject coolMapObject);

    public final String getDescription() {
        return _description;
    }

    public boolean isDataViewValid() {
        CoolMapView view = getCoolMapView();
        if (view == null || view.getCoolMapObject() == null || !view.getCoolMapObject().isViewMatrixValid()) {
            return false;
        } else {
            return true;
        }
    }

    public CoolMapObject<BASE, VIEW> getCoolMapObject() {
        return _coolMapObject;
    }
    ////////////////////////////////////////////////////////////////////////////

    private class ViewPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics grphcs) {



            super.paintComponent(grphcs);
            if (!isDataViewValid()) {

                if (_coolMapObject != null && _coolMapObject.getViewNumRows() == 0) {
                    //no row nodes
                    Graphics2D g2D = (Graphics2D)grphcs;
                    if (_icon != null) {
                        g2D.drawImage(_icon.getImage(), 5, 5, this);
                    }
                    g2D.setColor(UI.colorBlack3);
                    g2D.setFont(_messageFont);
                    g2D.drawString("No row nodes were added. Add row nodes to activate view.", 25, 17);
                }


                return;
            }
            
            
            if (_coolMapObject == null || this.getParent() == null) {
                return;
            }

            CoolMapView<BASE, VIEW> canvas = _coolMapObject.getCoolMapView();
            if (canvas == null) {
                return;
            }

            int yAnchorOffset = getAnchorY();

            Graphics2D g2D = (Graphics2D) grphcs;
            if (canvas.isAntiAliased()) {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            prePaint(g2D, _coolMapObject, _viewPanel.getWidth(), _viewPanel.getHeight());
            Rectangle subMapDim = canvas.getSubMapDimension();
            g2D.drawImage(_mapBuffer, 0, subMapDim.y - yAnchorOffset, this);
            postPaint(g2D, _coolMapObject, _viewPanel.getWidth(), _viewPanel.getHeight());


            if (!canRender(_coolMapObject)) {
                if (_icon != null) {
                    g2D.drawImage(_icon.getImage(), 5, 5, this);
                }
                g2D.setColor(UI.colorBlack3);
                g2D.setFont(_messageFont);
                g2D.drawString("Incompatible with current data", 25, 17);
            }
        }
    }
    private boolean _enabled = true;

    public void setSetEnabled(boolean enabled) {
        _enabled = enabled;
        _viewPanel.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return _enabled;
    }

    public abstract void justifyView();

    public CoolMapView<BASE, VIEW> getCoolMapView() {
        try{
        return _coolMapObject.getCoolMapView();
        }
        catch(Exception e){
            return null;
        }
    }

    public final synchronized void updateBuffer() {
        CoolMapView canvas = _coolMapObject.getCoolMapView();
        if (canvas != null && canvas.getMinRowInView() != null && canvas.getMaxRowInView() != null && canvas.getMinColInView() != null && canvas.getMaxColInView() != null && canvas.getSubMapDimension() != null) {
            updateBuffer(canvas.getMinRowInView(), canvas.getMaxRowInView(), canvas.getMinColInView(), canvas.getMaxColInView(), canvas.getSubMapDimension());
        }
    }

    public final synchronized void updateBuffer(int minRow, int maxRow, int minCol, int maxCol, Rectangle dimension) {
//        System.out.println("Row buffer updated");
        try{
        CoolMapView canvas = _coolMapObject.getCoolMapView();
        if (canvas == null || !canRender(canvas.getCoolMapObject())) {
            _mapBuffer = null;
            return;
        }

        int width = _viewPanel.getWidth();
        int height = dimension.height;
        if (width <= 0) {
            width = 200;
        }

        if (width > 0 && height > 0) {
            BufferedImage buffer = _graphicsConfiguration.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            Graphics2D g2D = buffer.createGraphics();
            g2D.setClip(0, 0, width, height);
            if (canvas.isAntiAliased()) {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            render(g2D, canvas.getCoolMapObject(), minRow, maxRow, minCol, maxCol, canvas.getZoomX(), canvas.getZoomY(), width, height);

            g2D.dispose();
            
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            _mapBuffer = buffer;

        } else {
            _mapBuffer = null;
        }



        justifyView();

        _viewPanel.repaint();
        }
        catch(Exception e){
            System.err.println("Minor issue when rendering row map");
        }
    }

    protected void render(Graphics2D g2D, CoolMapObject<BASE, VIEW> object, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int renderWidth, int renderHeight) {
        VNode rowNode;
        CoolMapView<BASE, VIEW> canvas = object.getCoolMapView();
        VNode firstNode = object.getViewNodeRow(fromRow);
        if (firstNode == null || firstNode.getViewOffset() == null) {
            return;
        }
        float anchorY = firstNode.getViewOffset();
        int parentYOffset = getViewPanel().getParent().getY();
        prepareRender(g2D);
        //System.out.println(fromRow + " " + toRow);
        for (int i = fromRow; i < toRow; i++) {
            rowNode = object.getViewNodeRow(i);
            if (rowNode == null || rowNode.getViewOffset() == null) {
                continue;
            }

            
            renderRow(g2D, object, rowNode, 0, (int) (rowNode.getViewOffset() - anchorY), renderWidth, (int) rowNode.getViewSizeInMap(zoomY));
        }
    }

    protected abstract void renderRow(Graphics2D g2D, CoolMapObject<BASE, VIEW> object, VNode node, int anchorX, int anchorY, int cellWidth, int cellHeight);

    public abstract String getName();
    
        public void aggregatorUpdated(CoolMapObject object) {
    }

    ;
    
    public void rowsChanged(CoolMapObject object) {
    }

    ;
    
    public void columnsChanged(CoolMapObject object) {
    }

    ;
    
    public void coolMapObjectBaseMatrixChanged(CoolMapObject object) {
    }

    ;
    
    public void stateStorageUpdated(CoolMapObject object) {
    }

    ;
    
    public void viewRendererChanged(CoolMapObject object) {
    }

    ;
    
    public void viewFilterChanged(CoolMapObject object) {
    }

    ;
    
    public void selectionChanged(CoolMapObject object) {
    }

    ;
    public void mapAnchorMoved(CoolMapObject object) {
    }

    ;
    public void activeCellChanged(CoolMapObject object, MatrixCell oldCell, MatrixCell newCell) {
    }

    ;
    public void mapZoomChanged(CoolMapObject object) {
    }

    ;
//    public void subSelectionRowChanged(CoolMapObject object) {
//    }
//
//    ;
//    public void subSelectionColumnChanged(CoolMapObject object) {
//    }

    @Override
    public JSONObject getCurrentState() {
        return null;
    }

    @Override
    public boolean restoreState(JSONObject savedState) {
        return false;
    }
    
    
    
}
