/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module;

import coolmap.utils.StateSavable;
import org.json.JSONObject;

/**
 * module needs to be considered  - widgets can be added at anytime
 * @author gangsu
 */
public class Module implements StateSavable{
    
    //will be needed for the Java simple plugin interface - do this later.
    public void intialize(){
        
    }

    @Override
    public JSONObject getCurrentState() {
        return null;
    }

    @Override
    public boolean restoreState(JSONObject savedState) {
        return false;
    }

}
