/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module;

import coolmap.module.impl.StateModule;
import coolmap.module.impl.ClusterModule;
import java.util.HashMap;

/**
 *
 * @author gangsu
 */
public class ModuleMaster{

    private ModuleMaster() {
    }
    private static HashMap<String, Module1> _coolMapModules = new HashMap<String, Module1>();

    public static void addModule(Module1 module) {
        if (module == null) {
            return;
        }
        _coolMapModules.put(module.getClass().getName(), module);
    }

    public static Module1 getModule(String className) {
        if (className != null) {
            //System.out.println("Getting: " + className);
            return _coolMapModules.get(className);
        } else {
            return null;
        }
    }

    public static void initialize() {        
        addModule(new ClusterModule());
        addModule(new StateModule());
    }
}
