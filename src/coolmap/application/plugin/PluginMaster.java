/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.plugin;

import coolmap.utils.Config;
import java.io.File;
import java.util.Collection;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

/**
 *
 * @author sugang
 */
public class PluginMaster {
    
    private static PluginManager pluginManager;
    private static PluginManagerUtil pluginManagerUtil;
    
    public static void initialize(){
        String pluginPath;
        if(Config.isInitialized()){
            pluginPath = Config.getProperty(Config.PLUGIN_DIRECTORY);
        }
        else{
            pluginPath = "plugin";
        }
        
        File pluginFolder = new File(pluginPath);
        
//        System.out.println(pluginFolder.getAbsolutePath());
        pluginManager = PluginManagerFactory.createPluginManager();
        pluginManager.addPluginsFrom(pluginFolder.toURI());
        
//        after loading everything
        pluginManagerUtil = new PluginManagerUtil(pluginManager);
        Collection<CoolMapPlugin> plugins = pluginManagerUtil.getPlugins(CoolMapPlugin.class);
        for(CoolMapPlugin plugin : plugins){
            plugin.initialize();
        }
    } 
}
