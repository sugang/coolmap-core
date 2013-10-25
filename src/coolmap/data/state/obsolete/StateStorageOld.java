/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.state.obsolete;

import coolmap.data.CoolMapObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gangsu
 */
public class StateStorageOld {

    private final ArrayDeque<StateSnapshot> _undoQueue = new ArrayDeque<StateSnapshot>();
    private final ArrayDeque<StateSnapshot> _redoQueue = new ArrayDeque<StateSnapshot>();
    private static int _stateCap = 10;
    //private boolean _lastActionRedo = false;

    public StateStorageOld() {
    }

    public static void setStateCap(int cap) {
        _stateCap = cap;
    }

    public synchronized void addState(StateSnapshot snapShot) {
        if (snapShot == null) {
            return;
        }
        while (_undoQueue.size() >= _stateCap) {
            //System.out.println("Cleaned");
            _undoQueue.removeFirst();
        }
        _undoQueue.add(snapShot);
        _redoQueue.clear();
        _currentSnapshot = null;
    }

//    public synchronized void addRedo(StateSnapshot snapshot) {
//        if (snapshot != null) {
//            _redoQueue.add(snapshot);
//        }
//    }
    public synchronized List<StateSnapshot> getAllStates() {
        return new ArrayList<StateSnapshot>(_undoQueue);
    }

//    undo and redo will happen as part of the undo/redo widget
//    widget will add menu items that change the selected index.
    public void clear() {
        //Threre shoudld be nothing there.
        System.out.println("The saved states should have been cleared already!");
        
        _undoQueue.clear();
        _redoQueue.clear();
    }
    private StateSnapshot _currentSnapshot = null;

    public void lastState(CoolMapObject object) {

        if (_undoQueue.isEmpty()) {
            return;
        }

        StateSnapshot lastState = _undoQueue.getLast();

        if (_currentSnapshot == null) {
            _currentSnapshot = new StateSnapshot(object, lastState.getDirection(), StateSnapshot.UNDOLEADER);
        }


        _redoQueue.add(lastState);
        _undoQueue.pollLast();

//        object.restoreSnapshot(lastState, false);
        object.notifyStateStorageUpdated();

//        System.out.println("UNDO:=========================");
//        System.out.println("State restored to:" + lastState);
//        System.out.println(_currentSnapshot);
//        System.out.println("Undos:" + _undoQueue);
//        System.out.println("Redos:" + _redoQueue);
//        System.out.println();
//        System.out.println();
    }

    public void nextState(CoolMapObject object) {

        if (_redoQueue.isEmpty() || _currentSnapshot == null) {
            return;
        }

        StateSnapshot currentState = _redoQueue.pollLast();

        _undoQueue.add(currentState);

        StateSnapshot restoreToState;
        if (_redoQueue.isEmpty()) {
            restoreToState = _currentSnapshot;
        } else {
            restoreToState = _redoQueue.getLast();
        }

//        object.restoreSnapshot(restoreToState, false);
        object.notifyStateStorageUpdated();

//        System.out.println("REDO:=========================");
//        System.out.println("State restored to:" + restoreToState);
//        System.out.println(_currentSnapshot);
//        System.out.println("Undos:" + _undoQueue);
//        System.out.println("Redos:" + _redoQueue);
//        System.out.println();
//        System.out.println();
    }

    public StateSnapshot getLastState() {
        return _undoQueue.getLast();
    }

//    public StateSnapshot getLastRedo() {
//        return _redoQueue.getLast();
//    }
    public boolean hasUndos() {
        return !_undoQueue.isEmpty();
    }

    public boolean hasRedos() {
        return !(_redoQueue.isEmpty() || _currentSnapshot == null);
    }
//    /**
//     * only undo and redo will change the queues
//     * @return 
//     */
//    public StateSnapshot undo(){
//        if(_undoQueue.isEmpty())
//            return null;
//        
//        StateSnapshot snapshot = _undoQueue.removeLast();
//        _redoQueue.addFirst(snapshot);
//        
//        return snapshot;
//    }
//    
//    /**
//     * only undo and redo will change the queues
//     * @return 
//     */
//    public StateSnapshot redo(){
//        if(_redoQueue.isEmpty())
//            return null;
//        StateSnapshot snapshot = _redoQueue.removeFirst();
//        _undoQueue.addLast(snapshot);
//        return snapshot;
//    }
}
