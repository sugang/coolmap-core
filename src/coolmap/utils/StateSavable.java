/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.utils;

import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public interface StateSavable {
    
    /**
     * state and be persisted using JSON
     * @return 
     */
    public abstract JSONObject getCurrentState();
    
    /**
     * state can be restored from JSON
     * @param savedState
     * @return 
     */
    public abstract boolean restoreState(JSONObject savedState);
}
