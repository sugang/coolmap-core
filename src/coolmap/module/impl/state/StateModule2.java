/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.module.impl.state;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.data.CoolMapObject;
import coolmap.data.listeners.CObjectListener;
import coolmap.module.Module;
import java.awt.MenuItem;

/**
 *
 * @author sugang
 */
public class StateModule2 extends Module implements ActiveCoolMapChangedListener, CObjectListener{
    
    private final MenuItem _undoOperation;
    private final MenuItem _redoOperation;
    private final MenuItem _saveStateOperation;
    private final MenuItem _loadStateOperation; //Need to insert a full capture before this operation
    
    
    
    public StateModule2(){
        
        _undoOperation = new MenuItem("Undo");
        _redoOperation = new MenuItem("Redo");
        _saveStateOperation = new MenuItem("Save State");
        _loadStateOperation = new MenuItem("Load State");
        
        
        _saveStateOperation.addActionListener(new SaveStateActionListener());
        
        CoolMapMaster.getCMainFrame().addMenuItem("Edit", _undoOperation, false, false);
        CoolMapMaster.getCMainFrame().addMenuItem("Edit", _redoOperation, false, true);
        CoolMapMaster.getCMainFrame().addMenuItem("Edit/State...", _saveStateOperation, false, false);
        CoolMapMaster.getCMainFrame().addMenuItem("Edit/State...", _loadStateOperation, false, false);
        
        CoolMapMaster.addActiveCoolMapChangedListener(this);
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCObjectListener(this);
    }
    

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        System.out.println("Active CoolMap changed -> in StateModule");
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
        System.out.println("State storage updated -> in StateModule");
    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }
    
}
