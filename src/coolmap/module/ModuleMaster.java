/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module;

import coolmap.utils.Config;
import java.util.HashMap;
import org.json.JSONArray;

/**
 *
 * @author gangsu
 */
public class ModuleMaster{

    private ModuleMaster() {
    }
    private static HashMap<String, Module> _coolMapModules = new HashMap<String, Module>();

    public static void addModule(Module module) {
        if (module == null) {
            return;
        }
        _coolMapModules.put(module.getClass().getName(), module);
    }

    public static Module getModule(String className) {
        if (className != null) {
            //System.out.println("Getting: " + className);
            return _coolMapModules.get(className);
        } else {
            return null;
        }
    }

    public static void initialize() {
        //This part will be controlled by JSON
        
        
        //addModule(new ClusterModule());
        //addModule(new StateModule());
        if(Config.isInitialized()){
//            System.out.println("!!! Config file loading successful, loading modules based on config file definitions");
            
            try{
                JSONArray modulesToLoad = Config.getJSONConfig().getJSONObject("module").getJSONArray("load");
                for(int i=0; i<modulesToLoad.length(); i++){
                    try{
                        String className = modulesToLoad.getString(i);
                        Module module = (Module)(Class.forName(className).newInstance());
                        addModule(module); //
                    }
                    catch(Exception e){
                        System.err.println("Initializing '" + modulesToLoad.optString(i) + "' error");
                    }
                }
            }
            catch(Exception e){
                System.err.println("Module config error. No modules were initialized");
            }
        }
    }
}
