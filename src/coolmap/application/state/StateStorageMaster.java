/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.state;

import coolmap.application.CoolMapMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.state.CoolMapState;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.KeyEvent;
import java.util.HashMap;

/**
 *
 * @author sugang -> this stores all the statess
 */
public class StateStorageMaster {

    //static methods
    private static HashMap<String, StateQueues> _stateQueuesHash = new HashMap<String, StateQueues>();
    private static final MenuItem _undoOperation = new MenuItem("Undo", new MenuShortcut(KeyEvent.VK_Z));
    private static final MenuItem _redoOperation = new MenuItem("Redo", new MenuShortcut(KeyEvent.VK_Y));
    private static final MenuItem _saveStateOperation = new MenuItem("Quick save state", new MenuShortcut(KeyEvent.VK_M));
    private static final MenuItem _loadStateOperation = new MenuItem("Quick load state", new MenuShortcut(KeyEvent.VK_L));
    private static final StateStorageMasterListeners _listeners = new StateStorageMasterListeners();
    public static HashMap<String, CoolMapState> _quickStore = new HashMap<String, CoolMapState>();
//temporary methods for testing
    //Quick save and quick load
    //also save renderer, ... etc., but that will be worried about later on.
//    
//

/////////////////////////////////////////////////////////////////////////////////////////////////
//    The following are for quick save and quick restore a view and these are indpendent from uno, redo
    //
    public static void initialize() {
        _saveStateOperation.addActionListener(new SaveStateAction());
        _loadStateOperation.addActionListener(new LoadStateAction());
        _undoOperation.addActionListener(new UndoAction());
        _redoOperation.addActionListener(new RedoAction());
        CoolMapMaster.getCMainFrame().addMenuItem("Edit", _undoOperation, false, false);
        CoolMapMaster.getCMainFrame().addMenuItem("Edit", _redoOperation, false, true);
        CoolMapMaster.getCMainFrame().addMenuItem("Edit", _saveStateOperation, false, false);
        CoolMapMaster.getCMainFrame().addMenuItem("Edit", _loadStateOperation, false, false);

        //Use this as a proxy to listen to various state changes
        //It's quite awakward but I can keep the master design like this
        CoolMapMaster.addActiveCoolMapChangedListener(_listeners);
        
    }

    //
    public static void quickSave(CoolMapObject object) {
        if (object == null) {
            return;
        }

        CoolMapState quickSaved = CoolMapState.createState("Quick save", object, null);
        _quickStore.put(object.getID(), quickSaved);
        updateMenus(object);
    }

    public static void quickLoad(CoolMapObject object) {
        if (object == null) {
            return;
        }

        CoolMapState quickSaved = _quickStore.get(object.getID());
        if (quickSaved == null) {
            return;
        }

        
        
        System.out.println("\nQuick loading:");
        System.out.println(quickSaved);
        System.out.println("\n");

        
        //add 
        CoolMapState stateBeforeLoad = CoolMapState.createState("Quick load", object, null); //Save pretty much everything
        addState(stateBeforeLoad);
        //return quickSaved; //this could be null
        object.restoreState(quickSaved);

        //but this step needs to be undo, therefore a capture of the previous state is needed
        //iterate all widgets to save JSON info
        
        //After quick load, a new state should be added
        
        
        updateMenus(object);
    }
/////////////////////////////////////////////////////////////////////////////////////////////

//    ///////////////////////////////////////////////////////////////////////////////////////
    public static void undo(CoolMapObject object) {
        System.out.println("Trying to undo object:" + object);
        if(object == null){
            updateMenus(object);
            return;
        }
        
        StateQueues stateQueues = _stateQueuesHash.get(object.getID());
        if(stateQueues == null){
            updateMenus(object);
            return;
        }
        
        if(!stateQueues.hasUndo()){
            updateMenus(object);
            return;
        }
        
        //
        CoolMapState state = stateQueues.undo();
        object.restoreState(state);

        updateMenus(object);
    }

    public static void redo(CoolMapObject object) {
        System.out.println("Trying to redo object:" + object);
        if(object == null){
            updateMenus(object);
            return;
        }
        
        StateQueues stateQueues = _stateQueuesHash.get(object.getID());
        if(stateQueues == null){
            updateMenus(object);
            return;
        }
        
        if(!stateQueues.hasRedo()){
            updateMenus(object);
            return;
        }
        
        CoolMapState state = stateQueues.redo();
        object.restoreState(state);
        
        updateMenus(object);
    }

    public static void addState(CoolMapState state) {
        System.out.println("new state added:" + state);
        //Get the queue
        //If queue does not exist, create one
        StateQueues queues = _stateQueuesHash.get(state.getCoolMapObjectID());
        //lazy initialization
        if (queues == null) {
            queues = new StateQueues(state.getCoolMapObjectID());
            _stateQueuesHash.put(state.getCoolMapObjectID(), queues);
        }
        
        //
        queues.addState(state);
        
        

        //Not necessarily update state for the active one -> may add state to others as well.
        updateMenus(CoolMapMaster.getCoolMapObjectByID(state.getCoolMapObjectID()));
    }

    public static void clearStates(CoolMapObject object) {
        StateQueues queues = _stateQueuesHash.get(object.getID());
        if (queues != null) {
            queues.destroy();
        }
        _stateQueuesHash.remove(object.getID());
    }

    public static void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject newObject) {
        updateMenus(newObject);
    }

    public static void updateMenus(CoolMapObject object) {
        if (object == null) {
            _undoOperation.setEnabled(false);
            _redoOperation.setEnabled(false);
            _saveStateOperation.setEnabled(false);
            _saveStateOperation.setEnabled(false);
        } else {

            System.out.println("-----Menus to be updated");
            StateQueues queues = _stateQueuesHash.get(object.getID());
            if (queues == null) {
                _undoOperation.setEnabled(false);
                _redoOperation.setEnabled(false);
            } else {
                if (queues.hasRedo()) {
                    _redoOperation.setEnabled(true);
                } else {
                    _redoOperation.setEnabled(false);
                }

                if (queues.hasUndo()) {
                    _undoOperation.setEnabled(true);
                    _undoOperation.setLabel("Undo " + queues.getCurrentUndoOpereationName());
                } else {
                    _undoOperation.setEnabled(false);
                    _undoOperation.setLabel("Undo");
                }
            }
            
//            _undoOperation.setEnabled(true);
            if(_quickStore.containsKey(object.getID())){
                _loadStateOperation.setEnabled(true);
            }
            else{
                _loadStateOperation.setEnabled(false);
            }
            
            //if obj != null, it is set to true
            _saveStateOperation.setEnabled(true);
        }
    }

}
