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
    
    public abstract JSONObject saveState();
    
    public boolean restoreState(JSONObject savedState);
}
