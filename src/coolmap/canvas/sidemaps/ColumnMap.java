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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.json.JSONObject;

/**
 *
 * @author gangsu
 */
public abstract class ColumnMap<BASE, VIEW> implements CViewListener, CObjectListener, StateSavable{

    private final ViewPanel _viewPanel = new ViewPanel();
    private final static GraphicsConfiguration _graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private BufferedImage _mapBuffer;
    private final CoolMapObject<BASE, VIEW> _coolMapObject;
    private ImageIcon _icon;
    private Font _messageFont;

    public void clearBuffer() {
        _mapBuffer = null;
    }

    public void destroy() {
        _mapBuffer = null;
    }

    public void aggregatorUpdated(CoolMapObject object) {
    }

    ;
    
    public void rowsChanged(CoolMapObject object) {
    }

    ;
    
    public void columnsChanged(CoolMapObject object) {
    }

    ;
    
    public void baseMatrixChanged(CoolMapObject object) {
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
    public void subSelectionRowChanged(CoolMapObject object) {
    }

    ;
    public void subSelectionColumnChanged(CoolMapObject object) {
    }

    ;
    
    

    /**
     * returns the parent canvas of this columnMap
     *
     * @return
     */
//    public Canvas<BASE, VIEW> getCanvas(){
//        return _canvas;
//    }
    private ColumnMap() {
        this(null);
    }

    public CoolMapView<BASE, VIEW> getCoolMapView() {
        return _coolMapObject.getCoolMapView();
    }

    public CoolMapObject<BASE, VIEW> getCoolMapObject() {
        return _coolMapObject;
    }

    public ColumnMap(CoolMapObject<BASE, VIEW> coolMapObject) {
        _coolMapObject = coolMapObject;
        _icon = UI.getImageIcon("infoBW");
        _messageFont = UI.fontPlain.deriveFont(12f);
        getViewPanel().setName(getName());

    }

    public JComponent getViewPanel() {
        return _viewPanel;
    }

    public final synchronized void updateBuffer() {
        CoolMapView canvas = _coolMapObject.getCoolMapView();
        if (canvas != null && canvas.getMinRowInView() != null && canvas.getMaxRowInView() != null && canvas.getMinColInView() != null && canvas.getMaxColInView() != null && canvas.getSubMapDimension() != null) {
            updateBuffer(canvas.getMinRowInView(), canvas.getMaxRowInView(), canvas.getMinColInView(), canvas.getMaxColInView(), canvas.getSubMapDimension());
        }
//        try{
//            Thread.currentThread().sleep(2000);
//        }
//        catch(Exception e){
//            
//        }
    }

//    protected int getXOffset(){
//        return 0;
//    }
    protected int getAnchorX() {
        return 0;
    }

    protected int getAnchorY() {
        return 0;
    }

    /**
     * only if view, object are not null, and there are rownodes and column
     * nodes
     *
     * @return
     */
    public boolean isDataViewValid() {
        CoolMapView view = getCoolMapView();
        if (view == null || view.getCoolMapObject() == null || !view.getCoolMapObject().isViewMatrixValid()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * update to specified region
     *
     * @param minRow
     * @param maxRow
     * @param minCol
     * @param maxCol
     * @param dimension
     */
    public final synchronized void updateBuffer(int minRow, int maxRow, int minCol, int maxCol, Rectangle dimension) {
        //System.out.println("Update buffer called!" + _canvas);
        //System.out.println(minRow + " " + maxRow + " " + minCol + " " + maxCol);

        CoolMapView canvas = _coolMapObject.getCoolMapView();
        //System.out.println(canvas);
        if (canvas == null || !canRender(canvas.getCoolMapObject())) {
            _mapBuffer = null;
            return;
        }



        int width = dimension.width;
        int height = _viewPanel.getHeight();

        //System.out.println(width + " " + height);
        //this may happen before component resize; result in an update that has 0 height
        if (height <= 0) {
            height = 100; //a hack that will at least make it work.
        }

        if (width > 0 && height > 0) {
            BufferedImage buffer = _graphicsConfiguration.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            Graphics2D g2D = buffer.createGraphics();
            g2D.setClip(0, 0, width, height); //maybe make it faster?
            if (canvas.isAntiAliased()) {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            if (Thread.currentThread().isInterrupted()) {
//                System.out.println("Interrupted");
                return;
            }

            render(g2D, canvas.getCoolMapObject(), minRow, maxRow, minCol, maxCol, canvas.getZoomX(), canvas.getZoomY(), width, height);

            g2D.dispose();
            if (Thread.currentThread().isInterrupted()) {
//                System.out.println("Interrupted");
                return;//If interrupted, don't update.
            }
            _mapBuffer = buffer;
        } else {
            //System.out.println("width:" + width + "height:" + height);
            _mapBuffer = null;
        }

        //System.out.println(_mapBuffer);
        justifyView();
        _viewPanel.repaint();
        //System.out.println(_mapBuffer);

    }

    /**
     * render the map whenever needed actually, it's better to separate the
     * render by column.
     *
     * overriding this function can make more flexible plots
     */
    protected void render(Graphics2D g2D, CoolMapObject<BASE, VIEW> object, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int renderWidth, int renderHeight) {
        VNode colNode;
        CoolMapView<BASE, VIEW> canvas = object.getCoolMapView();
        //find anchor
        VNode firstNode = object.getViewNodeColumn(fromCol);
        if (firstNode == null || firstNode.getViewOffset() == null) {
            return;
        }
        float anchorX = firstNode.getViewOffset();
        prepareRender(g2D);
        for (int i = fromCol; i < toCol; i++) {
            colNode = object.getViewNodeColumn(i);
            if (colNode == null || colNode.getViewOffset() == null) {
                continue;
            }

            renderColumn(g2D, object, colNode, (int) (colNode.getViewOffset() - anchorX), 0, (int) colNode.getViewSizeInMap(zoomX), renderHeight);
        }
        //Don't disploe! fuck
        //g2D.dispose();
    }

    /**
     * override this for column specific plotting
     *
     * @param g2D
     * @param anchorX
     * @param anchorY
     * @param cellWidth
     * @param cellHeight
     * @param node
     */
    protected abstract void renderColumn(Graphics2D g2D, CoolMapObject<BASE, VIEW> object, VNode node, int anchorX, int anchorY, int cellWidth, int cellHeight);

    protected abstract void prepareRender(Graphics2D g2D);
    private boolean _enabled = true;

    public void setSetEnabled(boolean enabled) {
        _enabled = enabled;
        _viewPanel.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return _enabled;
    }

    public abstract String getName();

    

    /**
     *
     */
    private class ViewPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);
            if (!isDataViewValid()) {

                if (_coolMapObject != null && _coolMapObject.getViewNumColumns() == 0) {
                    Graphics2D g2D = (Graphics2D) grphcs;
                    if (_icon != null) {
                        g2D.drawImage(_icon.getImage(), 5, getHeight() - 18, this);
                    }
                    g2D.setColor(UI.colorBlack3);
                    g2D.setFont(_messageFont);
                    g2D.drawString("No column nodes were added. Add column nodes to activate view.", 25, getHeight() - 18 + _messageFont.getSize() - 1);
                }

                return;
            }


            if (_coolMapObject == null) {
                return;
            }
            CoolMapView<BASE, VIEW> canvas = _coolMapObject.getCoolMapView();
            if (canvas == null) {
                return;
            }

            Graphics2D g2D = (Graphics2D) grphcs;


            if (canvas.isAntiAliased()) {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            prePaint(g2D, _coolMapObject, _viewPanel.getWidth(), _viewPanel.getHeight());
            Rectangle subMapDim = canvas.getSubMapDimension();
            g2D.drawImage(_mapBuffer, subMapDim.x, 0, this);
            postPaint(g2D, _coolMapObject, _viewPanel.getWidth(), _viewPanel.getHeight());


            if (!canRender(_coolMapObject)) {
                if (_icon != null) {
                    g2D.drawImage(_icon.getImage(), 5, getHeight() - 18, this);
                }
                g2D.setColor(UI.colorBlack3);
                g2D.setFont(_messageFont);
                g2D.drawString("Incompatible with current data", 25, getHeight() - 18 + _messageFont.getSize() - 1);
            }
        }
    }

    public abstract void justifyView();

    /**
     * paint underneath the buffer
     *
     * @param g2D
     * @param canvas
     */
    protected abstract void prePaint(final Graphics2D g2D, final CoolMapObject<BASE, VIEW> object, int width, int height);

    /**
     * paint on top of the buffer
     *
     * @param g2D
     */
    protected abstract void postPaint(final Graphics2D g2D, final CoolMapObject<BASE, VIEW> object, int width, int height);

    public abstract boolean canRender(CoolMapObject coolMapObject);

    public final String getDescription() {
        return _description;
    }
    private String _description = null;

    public final void setDescription(String description) {
        _description = description;
    }

    public abstract JComponent getConfigUI();

    @Override
    public JSONObject getCurrentState() {
        return null;
    }

    @Override
    public boolean restoreState(JSONObject savedState) {
        return false;
    }
    
    
    
    
}
