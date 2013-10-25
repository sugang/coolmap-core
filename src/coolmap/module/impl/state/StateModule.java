/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module.impl.state;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.data.CoolMapObject;
import coolmap.data.listeners.CObjectListener;
import coolmap.data.state.obsolete.StateStorage;
import coolmap.module.Module;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 *
 * @author gangsu
 */
public class StateModule extends Module implements ActiveCoolMapChangedListener, CObjectListener {

    private MenuItem _undoOperation;
    private MenuItem _redoOperation;

    public StateModule() {
        _undoOperation = new MenuItem("Undo", new MenuShortcut(KeyEvent.VK_Z));
        CoolMapMaster.getCMainFrame().addMenuItem("Edit", _undoOperation, false, false);
        _undoOperation.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                
                System.out.println("Undo fired");
                
                if (obj != null) {
                    obj.undo();
                }
                
                _updateMenuItems(obj);
            }
        });

        _redoOperation = new MenuItem("Redo", new MenuShortcut(KeyEvent.VK_Y));
        CoolMapMaster.getCMainFrame().addMenuItem("Edit", _redoOperation, false, false);
        _redoOperation.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj != null) {
                    obj.redo();
                }
                
                _updateMenuItems(obj);
            }
        });

        CoolMapMaster.addActiveCoolMapChangedListener(this);
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCObjectListener(this);
        
    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        _updateMenuItems(activeCoolMapObject);
    }

    private void _updateMenuItems(CoolMapObject activeCoolMapObject) {
        
        _undoOperation.setEnabled(false);
        _redoOperation.setEnabled(false);
        _undoOperation.setLabel("Undo");
        _redoOperation.setLabel("Redo");
        if (activeCoolMapObject == null) {
            return;
        }


        StateStorage storage = activeCoolMapObject.getStateStorage();
        if (storage.hasUndos()) {
            _undoOperation.setEnabled(true);
            _undoOperation.setLabel("Undo" + " " + activeCoolMapObject.getStateStorage().getLastState().getName());
        }

        if (storage.hasRedos()) {
            _redoOperation.setEnabled(true);
            _redoOperation.setLabel("Redo");
        }

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
    public void stateStorageUpdated(CoolMapObject object) {
        System.out.println("State storage detected in the state widget");
        if(object != null && object == CoolMapMaster.getActiveCoolMapObject()){
            _updateMenuItems(object);
        }
    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }
}
