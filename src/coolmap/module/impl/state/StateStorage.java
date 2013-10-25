/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module.impl.state;

import coolmap.data.CoolMapObject;
import coolmap.data.state.CoolMapState;
import java.util.ArrayDeque;
import java.util.HashMap;

/**
 *
 * @author sugang -> this stores all the statess
 */
public class StateStorage {

    //static methods
    private static HashMap<String, StateQueues> _stateStorages = new HashMap<String, StateQueues>();

    //temporary methods for testing
    //Quick save and quick load
    //also save renderer, ... etc., but that will be worried about later on.
//    
    public static void addState(CoolMapState state) {

    }

/////////////////////////////////////////////////////////////////////////////////////////////////
//    The following are for quick save and quick restore a view and these are indpendent from uno, redo
    //
    public static HashMap<String, CoolMapState> _quickStore = new HashMap<String, CoolMapState>();

    //
    public static void quickSave(CoolMapObject object) {
        if (object == null) {
            return;
        }

        CoolMapState quickSaved = CoolMapState.createState("Quick save", object, null);
        _quickStore.put(object.getID(), quickSaved);
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

        //return quickSaved; //this could be null
        object.restoreState(quickSaved);

        //but this step needs to be undo, therefore a capture of the previous state is needed
        //iterate all widgets to save JSON info
    }

    private class StateQueues {

        private final ArrayDeque<CoolMapState> _undoQueue = new ArrayDeque<CoolMapState>();
        private final ArrayDeque<CoolMapState> _redoQueue = new ArrayDeque<CoolMapState>();

    }

}
