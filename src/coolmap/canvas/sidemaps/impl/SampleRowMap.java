/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.sidemaps.impl;

import coolmap.canvas.listeners.CViewListener;
import coolmap.canvas.misc.MatrixCell;
import coolmap.canvas.sidemaps.RowMap;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 *
 * @author gangsu
 */
public class SampleRowMap extends RowMap<Object, Object> implements CViewListener {

//    @Override
//    public void subSelectionRowChanged(CoolMapObject object) {
//    }
//
//    @Override
//    public void subSelectionColumnChanged(CoolMapObject object) {
//    }

    public SampleRowMap(CoolMapObject object) {
        super(object);
    }

    @Override
    public void render(Graphics2D g2D, CoolMapObject<Object, Object> object, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int renderWidth, int renderHeight) {
        g2D.setColor(Color.BLACK);
        g2D.fillRect(0, 0, renderWidth, renderHeight);
        super.render(g2D, object, fromRow, toRow, fromCol, toCol, zoomX, zoomY, renderWidth, renderHeight);
    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public JComponent getConfigUI() {
        return null;
    }

    @Override
    public boolean canRender(CoolMapObject coolMapObject) {
        if (Object.class.isAssignableFrom(coolMapObject.getViewClass())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void renderRow(Graphics2D g2D, CoolMapObject<Object, Object> object, VNode node, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        g2D.setColor(UI.randomColor());
        g2D.fillRect(anchorX, anchorY, cellWidth, cellHeight);
    }

    @Override
    public void activeCellChanged(CoolMapObject obj, MatrixCell oldCell, MatrixCell newCell) {
        //System.out.println("Row respond to active cell change" + oldCell + " " + newCell);
    }

    @Override
    public void selectionChanged(CoolMapObject obj) {
        //System.out.println("Row respond to selection change");
    }

    @Override
    protected void prepareRender(Graphics2D g2D) {
    }

    @Override
    public void justifyView() {
    }

    @Override
    protected void prePaint(Graphics2D g2D, CoolMapObject<Object, Object> object, int width, int height) {
    }

    @Override
    protected void postPaint(Graphics2D g2D, CoolMapObject<Object, Object> object, int width, int height) {
    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
    }

    @Override
    public void baseMatrixChanged(CoolMapObject object) {
    }

    @Override
    public void mapAnchorMoved(CoolMapObject object) {
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
    }

    @Override
    public void stateStorageUpdated(CoolMapObject object) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    @Override
    public void gridChanged(CoolMapObject object) {
    }
}
