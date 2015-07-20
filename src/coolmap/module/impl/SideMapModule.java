/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module.impl;

import coolmap.application.CoolMapMaster;
import coolmap.canvas.sidemaps.ColumnMap;
import coolmap.canvas.sidemaps.RowMap;
import coolmap.data.CoolMapObject;
import coolmap.module.Module;
import coolmap.utils.Config;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.LinkedHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class SideMapModule extends Module {

    private static final HashSet<Class> columnMaps = new LinkedHashSet<>();
    private static final HashSet<Class> rowMaps = new LinkedHashSet<>();

    public SideMapModule() {
//        System.out.println("Side maps initialized");
        CoolMapMaster.getCMainFrame().addMenuSeparator("View/Canvas config/");

        try {
            if (Config.isInitialized()) {
                JSONObject obj = Config.getJSONConfig().getJSONObject("module").getJSONObject("config").getJSONObject(this.getClass().getName()).getJSONObject("load");
                JSONArray rowMaps = obj.getJSONArray("row");
                JSONArray columnMaps = obj.getJSONArray("column");

                for (int i = 0; i < rowMaps.length(); i++) {
                    try {
                        String rowMapClass = rowMaps.getString(i);

                        Class<RowMap> cls = (Class<RowMap>) Class.forName(rowMapClass);

                        //System.out.println(cls);
                        //Constructor ct = cls.getDeclaredConstructor(CoolMapObject.class);
                        //ct.newInstance(new CoolMapObject<Object, Object>());
                        //registerSideMapRow(map.getClass());
                        registerSideMapRow(cls);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (int j = 0; j < columnMaps.length(); j++) {
                    try {
                        String colMapClass = columnMaps.getString(j);

                        Class<ColumnMap> cls = (Class<ColumnMap>) Class.forName(colMapClass);

//                        ColumnMap map = cls.getDeclaredConstructor(CoolMapObject.class).newInstance();
                        registerSideMapColumn(cls);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (Exception e) {
            System.err.println("Failed to load sidemaps");
        }
    }

    public static void registerSideMapRow(final Class<RowMap> rowMap) {

        rowMaps.add(rowMap);
        //Also add to menu
//        System.err.println(rowMap);

        try {
            Menu menu = new Menu(((RowMap) rowMap.getConstructor(CoolMapObject.class).newInstance(new CoolMapObject())).getName());
            
            MenuItem item = new MenuItem("Toggle");
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                        object.getCoolMapView().addRowMap(rowMap.getConstructor(CoolMapObject.class).newInstance(object));
                    } catch (Exception ex) {
                    }
                }
            });
            
            menu.add(item);

            CoolMapMaster.getCMainFrame().addMenuItem("View/Canvas config/Row side/", menu, false, false);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerSideMapColumn(final Class<ColumnMap> columnMap) {
        columnMaps.add(columnMap);
        //Also add to menu
//        System.err.println(columnMap);
        try {
            MenuItem item = new MenuItem(((ColumnMap) columnMap.getConstructor(CoolMapObject.class).newInstance(new CoolMapObject())).getName());
            CoolMapMaster.getCMainFrame().addMenuItem("View/Canvas config/Column side/", item, false, false);
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                        object.getCoolMapView().addColumnMap(columnMap.getConstructor(CoolMapObject.class).newInstance(object));
                    } catch (Exception ex) {
//                    ex.printStackTrace();
                        //need an error logging system
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
