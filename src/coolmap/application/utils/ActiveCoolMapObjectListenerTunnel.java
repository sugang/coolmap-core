/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.utils;

import coolmap.application.CoolMapMaster;
import coolmap.canvas.listeners.CViewListener;
import coolmap.canvas.misc.MatrixCell;
import coolmap.data.CoolMapObject;
import coolmap.data.listeners.CObjectListener;
import java.util.HashSet;

/**
 * only fires when the object is the active one
 *
 * @author gangsu
 */
public class ActiveCoolMapObjectListenerTunnel implements CViewListener, CObjectListener {

    private final static ActiveCoolMapObjectListenerTunnel _instance = new ActiveCoolMapObjectListenerTunnel();
    private final static HashSet<CViewListener> _viewListeners = new HashSet<CViewListener>();
    private final static HashSet<CObjectListener> _objectListeners = new HashSet<CObjectListener>();

    public void addCViewListener(CViewListener lis) {
        if (lis != null) {
            _viewListeners.add(lis);
        }
    }

    public void removeCViewListener(CViewListener lis) {
        if (lis != null) {
            _viewListeners.remove(lis);
        }
    }

    public void addCObjectListener(CObjectListener lis) {
        if (lis != null) {
            _objectListeners.add(lis);
        }
    }

    public void removeCObjectListener(CObjectListener lis) {
        if (lis != null) {
            _objectListeners.add(lis);
        }
    }

    private boolean _isActiveObject(CoolMapObject object) {
        CoolMapObject activeObj = CoolMapMaster.getActiveCoolMapObject();
        if (activeObj != null && activeObj == object) {
            return true;
        } else {
            return false;
        }
    }

    private ActiveCoolMapObjectListenerTunnel() {
    }

    public static ActiveCoolMapObjectListenerTunnel getInstance() {
        return _instance;
    }

    @Override
    public void selectionChanged(CoolMapObject object) {
        if (!_isActiveObject(object)) {
            return;
        }
        for (CViewListener lis : _viewListeners) {
            lis.selectionChanged(object);
        }
    }

    @Override
    public void mapAnchorMoved(CoolMapObject object) {
        if (!_isActiveObject(object)) {
            return;
        }

        //System.out.println("Active object anchor moved, and fired");
        for (CViewListener lis : _viewListeners) {
            lis.mapAnchorMoved(object);
        }
    }

    @Override
    public void activeCellChanged(CoolMapObject object, MatrixCell oldCell, MatrixCell newCell) {
        if (!_isActiveObject(object)) {
            return;
        }
        for (CViewListener lis : _viewListeners) {
            lis.activeCellChanged(object, oldCell, newCell);
        }
    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
        if (!_isActiveObject(object)) {
            return;
        }
        for (CObjectListener lis : _objectListeners) {
            lis.aggregatorUpdated(object);
        }
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
        if (!_isActiveObject(object)) {
            return;
        }
        for (CObjectListener lis : _objectListeners) {
            lis.rowsChanged(object);
        }
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
        if (!_isActiveObject(object)) {
            return;
        }
        for (CObjectListener lis : _objectListeners) {
            lis.columnsChanged(object);
        }
    }

    @Override
    public void baseMatrixChanged(CoolMapObject object) {
        if (!_isActiveObject(object)) {
            return;
        }
        for (CObjectListener lis : _objectListeners) {
            lis.baseMatrixChanged(object);
        }
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
        if (!_isActiveObject(object)) {
            return;
        }
        for (CViewListener lis : _viewListeners) {
            lis.mapZoomChanged(object);
        }
    }

//    @Override
//    public void stateStorageUpdated(CoolMapObject object) {
//        if (!_isActiveObject(object)) {
//            return;
//        }
//        for (CObjectListener lis : _objectListeners) {
//            lis.stateStorageUpdated(object);
//        }
//    }
//    @Override
//    public void subSelectionRowChanged(CoolMapObject object) {
//        if (!_isActiveObject(object)) {
//            return;
//        }
//        for (CViewListener lis : _viewListeners) {
//            lis.subSelectionRowChanged(object);
//        }
//    }
//    @Override
//    public void subSelectionColumnChanged(CoolMapObject object) {
//        if (!_isActiveObject(object)) {
//            return;
//        }
//        for (CViewListener lis : _viewListeners) {
//            lis.subSelectionColumnChanged(object);
//        }
//    }
    @Override
    public void viewRendererChanged(CoolMapObject object) {
        if (!_isActiveObject(object)) {
            return;
        }
        for (CObjectListener lis : _objectListeners) {
            lis.viewRendererChanged(object);
        }
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
        if (!_isActiveObject(object)) {
            return;
        }
        for (CObjectListener lis : _objectListeners) {
            lis.viewFilterChanged(object);
        }
    }

    //make sure only the active one's grid changed is fired
    @Override
    public void gridChanged(CoolMapObject object) {
        if (!_isActiveObject(object)) {
            return;
        }
        for (CViewListener lis : _viewListeners) {
            lis.gridChanged(object);
        }
    }

    @Override
    public void nameChanged(CoolMapObject object) {
        if (!_isActiveObject(object)) {
            return;
        }
        for (CObjectListener lis : _objectListeners) {
            lis.nameChanged(object);
        }
    }
}
