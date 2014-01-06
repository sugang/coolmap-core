/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.snippet;

import coolmap.utils.StateSavable;
import org.json.JSONObject;

/**
 *
 * @author gangsu
 */
public abstract class SnippetConverter implements StateSavable{
    
    public abstract String convert(Object obj);
    public abstract boolean canConvert(Class cls);

    @Override
    public JSONObject getCurrentState() {
        return null;
    }

    @Override
    public boolean restoreState(JSONObject savedState) {
        return false;
    }
    
    
}
