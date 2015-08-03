package coolmap.application.plugin;

import net.xeoh.plugins.base.Plugin;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public interface CoolMapPlugin extends Plugin {
    
    public void initialize(JSONObject pluginConfig);
    public String getName();
}
