/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module.impl;

import coolmap.module.Module;
import coolmap.utils.Config;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 *
 * @author sugang
 */
public class SideMapModule extends Module {

    private static final HashSet<Class> columnMaps = new LinkedHashSet<Class>();
    private static final HashSet<Class> rowMaps = new LinkedHashSet<Class>();

    public SideMapModule() {
//        System.out.println("Side maps initialized");
//        
        try {
            if (Config.isInitialized()) {
                
            }
        } catch (Exception e) {
            System.err.println("Failed to load sidemaps");
        }
    }

    public static void registerSideMapRow(Class rowMap) {
        
        rowMaps.add(rowMap);
        //Also add to menu
    }

    public static void registerSideMapColumn(Class columnMap) {
        columnMaps.add(columnMap);
        //Also add to menu
    }

}
