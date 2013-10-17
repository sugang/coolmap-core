/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module;

import java.io.OutputStream;
import net.xeoh.plugins.base.Plugin;
import org.json.JSONObject;

/**
 *
 * @author gangsu
 */
public interface Module extends Plugin {
    public JSONObject saveState();
    public void loadState(JSONObject state);
}
