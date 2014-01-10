/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.plugin;

import coolmap.application.widget.impl.console.CMConsole;
import coolmap.utils.Config;
import java.io.File;
import java.util.Collection;
import net.xeoh.plugins.base.PluginInformation;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginInformationImpl;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class PluginMaster {

    private static PluginManager pluginManager;
    private static PluginManagerUtil pluginManagerUtil;

    public static void initialize() {
        String pluginPath;
        if (Config.isInitialized()) {
            pluginPath = Config.getProperty(Config.PLUGIN_DIRECTORY);
        } else {
            pluginPath = "plugin";
        }

        File pluginFolder = new File(pluginPath);

//        System.out.println(pluginFolder.getAbsolutePath());
        pluginManager = PluginManagerFactory.createPluginManager();
        pluginManager.addPluginsFrom(pluginFolder.toURI());

//        after loading everything
        pluginManagerUtil = new PluginManagerUtil(pluginManager);
        Collection<CoolMapPlugin> plugins = pluginManagerUtil.getPlugins(CoolMapPlugin.class);
        
        Collection<PluginInformation> pluginInfo = pluginManagerUtil.getPlugins(PluginInformation.class);
        
        PluginInformationImpl piImpl = null;
        if(!pluginInfo.isEmpty()){
            piImpl = (PluginInformationImpl)pluginInfo.iterator().next();
        }
        
        for (CoolMapPlugin plugin : plugins) {
            try {
                
                
                
                
                CMConsole.logInfo("Loaded " + plugin.getName() + " plugin");
//                System.out.println(plugin.getName());
                JSONObject config = new JSONObject();
                if(piImpl != null){
                    //gets the string
                    try{
                    Collection<String> paths = piImpl.getInformation(PluginInformation.Information.CLASSPATH_ORIGIN, plugin);
                    if(!paths.isEmpty()){
                        config.put(CoolMapPluginTerms.ATTR_URI, paths.iterator().next());
                    }}
                    catch(Exception e){
                        //do nothing, not found
                    }
                }
                
                
                
                
                
                plugin.initialize(config);
                
            } catch (Exception e) {
                CMConsole.logWarning("Loaded '" + plugin.getName() + "' plugin");
                CMConsole.logToFile(e);
            }
        }
    }
}
