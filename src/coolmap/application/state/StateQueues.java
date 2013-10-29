/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.state;

import coolmap.application.CoolMapMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.state.CoolMapState;
import java.util.ArrayDeque;

/**
 *
 * @author sugang
 */
public class StateQueues {

    private final ArrayDeque<CoolMapState> _undoQueue = new ArrayDeque<CoolMapState>();
    private final ArrayDeque<CoolMapState> _redoQueue = new ArrayDeque<CoolMapState>();
    private CoolMapState _currentState = null;
    private final static int _stateCap = 10;
    private final String coolMapObjectID;

    public StateQueues(String id) {
        coolMapObjectID = id;
    }

    public void addState(CoolMapState state) {
        if (state != null) {

            //make sure it does not take too much storage
            while (_undoQueue.size() >= _stateCap) {
                //System.out.println("Cleaned");
                _undoQueue.removeFirst();
            }

            _redoQueue.clear();
            _undoQueue.addLast(state);
            _currentState = null;
        }
    }

    public void destroy() {
        _undoQueue.clear();
        _redoQueue.clear();
        _currentState = null;
    }

    public boolean hasUndo() {
        if (_undoQueue.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean hasRedo() {
        return !(_redoQueue.isEmpty() || _currentState == null);
    }

    public String getCurrentUndoOpereationName() {
        if (_undoQueue.isEmpty()) {
            return "";
        } else {
            return _undoQueue.getLast().getOperationName();
        }
    }

    public CoolMapState undo() {
        if (_undoQueue.isEmpty()) {
            return null;
        }

        CoolMapObject object = CoolMapMaster.getCoolMapObjectByID(coolMapObjectID);
        if (object == null) {
            return null;
        }

        //pool last
        CoolMapState lastState = _undoQueue.pollLast();

        if (_currentState == null) {
            //should capture the current state, using last state parameters
            //This function
            _currentState = CoolMapState.createFromStateConfig("HEAD", object, lastState);
            
            
            
        }

        _redoQueue.add(lastState);

        //object.restoreState(lastState);
        //State was restored
        return lastState;
    }

    public CoolMapState redo() {
        if (_redoQueue.isEmpty() || _currentState == null) {
            return null;
        }

        CoolMapObject object = CoolMapMaster.getCoolMapObjectByID(coolMapObjectID);
        if (object == null) {
            return null;
        }

        CoolMapState redoState = _redoQueue.pollLast(); //poll last added
        _undoQueue.add(redoState);
        CoolMapState restoreToState;

        //System.out.println("_redoQueue is Empty? " + _redoQueue);
        //System.out.println("Current State? " + _currentState);
        
        
        if (_redoQueue.isEmpty()) {
            System.err.println("Restore to: current State, this part has a bug:" + _currentState);
            restoreToState = _currentState;
        } else {
            restoreToState = _redoQueue.getLast();
        }

        //object.restoreState(redoState);
        return restoreToState;
    }

//    public CoolMapState lastState(){
//        //create a state, and figure out what was changed
//        if(_undoQueue.isEmpty()){
//            return null;
//        }
//        CoolMapState prevState = _undoQueue.pollLast();
//        if(_currentState == null){
//            _currentState = CoolMapState.createFromConfig(prevState.getOperationName(), object, prevState);
//        }
//        
//        
//        
//        
//        
//        
//        
//        if(prevState == null){
//            //do nothing
//            return null;
//        }
//        else{
//            //capture a currentState, and put it into redo
//            //fetch the object associated with this state
//            CoolMapObject object = CoolMapMaster.getCoolMapObjectByID(coolMapObjectID);
//            
//            //think carefully
//            CoolMapState currentStateCapture = CoolMapState.createFromConfig(prevState.getOperationName(), object, prevState);       
//            
//            _redoQueue.addFirst(currentStateCapture);
//            //the prevState is the lastState capture
//            _currentState = prevState;
//            //it is set to currentState
//            return prevState;
//        }
//    }
//    
//    public CoolMapState nextState(){
//        //no need to create a state - use redo last state to replace
//        CoolMapState lastState = _redoQueue.pollFirst();
//        if(lastState == null){
//            
//        }
//        else{
//            
//        }
//    }
}
