/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module;

import coolmap.utils.Config;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author gangsu
 */
public class ModuleMaster{

    private ModuleMaster() {
    }
    private static final HashMap<String, Module> _coolMapModules = new HashMap<>();

    
    public static Set<Module> getAllModules(){
        return new HashSet<>(_coolMapModules.values());
    }
    
    public static void addModule(Module module) {
        if (module == null) {
            return;
        }
        module.intialize();
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
                System.out.println(modulesToLoad);
                for(int i=0; i<modulesToLoad.length(); i++){
                    try{
                        String className = modulesToLoad.getString(i);
                        Module module = (Module)(Class.forName(className).newInstance());
                        addModule(module); //
                    }
                    catch(JSONException | ClassNotFoundException | InstantiationException | IllegalAccessException e){
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
