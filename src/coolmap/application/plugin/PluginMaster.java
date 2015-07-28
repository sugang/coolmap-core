package coolmap.application.plugin;

import coolmap.application.widget.impl.console.CMConsole;
import coolmap.utils.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
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

    public Collection<CoolMapPlugin> getAllPlugins() {
        if (pluginManagerUtil != null) {
            return pluginManagerUtil.getPlugins(CoolMapPlugin.class);
        } else {
            return null;
        }
    }

    public static void initialize() {
        String pluginPath;
        if (Config.isInitialized()) {
            pluginPath = Config.getProperty(Config.PLUGIN_DIRECTORY);
        } else {
            pluginPath = "plugin";
        }

        File pluginFolder = new File(pluginPath);

        pluginManager = PluginManagerFactory.createPluginManager();

        File[] dirs = pluginFolder.listFiles();
        for (File file : dirs) {
            if (file.isDirectory()) {
                try {
                    pluginManager.addPluginsFrom(file.toURI());
                } catch (Exception e) {
                    System.err.println("Error loading plugin from folder:" + file);
                }
            } else {
                //single jar will also work.
                try {
                    if (file.getName().endsWith(".jar")) {
                        pluginManager.addPluginsFrom(file.toURI());
                    }
                } catch (Exception e) {
                    System.err.println("Error loading plugin from file:" + file);
                }
            }
        }

        //            after loading everything
        pluginManagerUtil = new PluginManagerUtil(pluginManager);
        Collection<CoolMapPlugin> plugins = pluginManagerUtil.getPlugins(CoolMapPlugin.class);

        Collection<PluginInformation> pluginInfo = pluginManagerUtil.getPlugins(PluginInformation.class);

        PluginInformationImpl piImpl = null;
        if (!pluginInfo.isEmpty()) {
            piImpl = (PluginInformationImpl) pluginInfo.iterator().next();
        }

        for (CoolMapPlugin plugin : plugins) {
            try {
                CMConsole.logInfo("Loaded " + plugin.getName() + " plugin");

                JSONObject config;
                if (piImpl != null) {
                    //gets the string
                    try {
                        Collection<String> paths = piImpl.getInformation(PluginInformation.Information.CLASSPATH_ORIGIN, plugin);

                        if (!paths.isEmpty()) {
                            String path = paths.iterator().next();
                            
                            if (path != null) {
                                URL url = new URL(path);
                                BufferedReader reader = new BufferedReader(new FileReader(url.getPath() + "config.json"));
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    sb.append(line);
                                }

                                config = new JSONObject(sb.toString());
                            } else {
                                config = new JSONObject();
                            }

                            config.put(CoolMapPluginTerms.ATTR_URI, path);
                        } else {
                            config = new JSONObject();
                        }
                    } catch (Exception e) {
                        config = new JSONObject();
                    }
                    
                    plugin.initialize(config);
                }

            } catch (Exception e) {
                CMConsole.logWarning("Loaded '" + plugin.getName() + "' plugin");
                CMConsole.logToFile(e);
            }
        }

    }
}
