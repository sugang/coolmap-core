/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.sidemaps.impl;

import coolmap.canvas.misc.MatrixCell;
import coolmap.canvas.sidemaps.ColumnMap;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.graphics.UI;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 *
 * @author gangsu
 */
public class ColumnValueTest extends ColumnMap {

    public ColumnValueTest(CoolMapObject obj) {
        super(obj);
    }

    @Override
    protected void renderColumn(Graphics2D g2D, CoolMapObject object, VNode node, int anchorX, int anchorY, int cellWidth, int cellHeight) {

        int boxheight = (int) (cellHeight * Math.random());
        g2D.setColor(UI.colorDarkGreen1);
        g2D.fillRect(anchorX, anchorY + cellHeight - boxheight, cellWidth, boxheight);
    }

    @Override
    protected void prepareRender(Graphics2D g2D) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
    }

    @Override
    public void stateStorageUpdated(CoolMapObject object) {
    }

    @Override
    public void justifyView() {
    }

    @Override
    protected void prePaint(Graphics2D g2D, CoolMapObject object, int width, int height) {
    }

    @Override
    protected void postPaint(Graphics2D g2D, CoolMapObject object, int width, int height) {
    }

    @Override
    public boolean canRender(CoolMapObject coolMapObject) {
        return true;
    }

    @Override
    public JComponent getConfigUI() {
        return null;
    }

    @Override
    public void activeCellChanged(CoolMapObject obj, MatrixCell oldCell, MatrixCell newCell) {
    }

    @Override
    public void selectionChanged(CoolMapObject obj) {
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
    public void subSelectionRowChanged(CoolMapObject object) {
    }

    @Override
    public void subSelectionColumnChanged(CoolMapObject object) {
    }
}
