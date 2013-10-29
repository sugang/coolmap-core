/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.data.state;

import coolmap.data.CoolMapObject;

/**
 *
 * @author sugang
 */
public interface CObjectStateStoreListener {
    /**
     * Ths is only called when a config file is needed
     * @param targetObject
     * @param sourceState
     */
    public abstract void stateToBeSaved(CoolMapObject targetObject, CoolMapState sourceState);
    
    /**
     * restore an object from the state; the source 
     * @param targetObject
     * @param sourceState
     */
    public abstract void stateToBeRestored(CoolMapObject targetObject, CoolMapState sourceState);
}
