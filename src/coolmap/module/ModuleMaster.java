/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module;

import java.util.HashMap;

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
    }
}
