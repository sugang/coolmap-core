/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.listeners;

import coolmap.canvas.misc.MatrixCell;
import coolmap.data.CoolMapObject;

/**
 *
 * @author gangsu
 */
public interface CViewListener {
    
    public void selectionChanged(CoolMapObject object);
    public void mapAnchorMoved(CoolMapObject object);
    public void activeCellChanged(CoolMapObject object, MatrixCell oldCell, MatrixCell newCell);
    public void mapZoomChanged(CoolMapObject object);
    public void subSelectionRowChanged(CoolMapObject object);
    public void subSelectionColumnChanged(CoolMapObject object);
//    public void rowNodeViewUpdated(CoolMapObject object);
//    public void columNodeViewUpdated(CoolMapObject object);
}
