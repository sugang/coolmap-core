/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget;

import coolmap.application.CoolMapMaster;
import coolmap.application.widget.impl.WidgetAggregator;
import coolmap.application.widget.impl.WidgetCMatrix;
import coolmap.application.widget.impl.WidgetCOntology;
import coolmap.application.widget.impl.WidgetDataMatrix;
import coolmap.application.widget.impl.WidgetSearch;
import coolmap.application.widget.impl.WidgetSyncer;
import coolmap.application.widget.impl.WidgetViewRenderer;
import coolmap.application.widget.impl.WidgetViewport;
import coolmap.utils.Config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author gangsu
 */
public class WidgetMaster {

    private WidgetMaster() {

    }

    //public static String CANVAS = "coolmap.application.widget.impl.WidgetViewport";
    private static HashMap<String, Widget> _coolMapWidgets = new HashMap<String, Widget>();

    public static void addWidget(Widget widget) {

        //System.out.println(widget.getClass().getName() + " added" );
        if (widget == null) {
            return;
        }

        //fullClassname
        _coolMapWidgets.put(widget.getClass().getName(), widget);

        CoolMapMaster.getCMainFrame().addWidget(widget);
        CoolMapMaster.getCMainFrame().addMenuItem("View/Show Widgets", widget.getMenuItem(), false, false);
    }

    public static void initialize() {

        if (Config.isInitialized()) {

            System.out.println("!!! Config file loading successful, loading widgets based on config file definitions");
            try {
                JSONArray widgetsToLoad = Config.getJSONConfig().getJSONObject("module").getJSONArray("load");
                String[] widgetsNames = new String[widgetsToLoad.length()];
                for(int i=0; i<widgetsToLoad.length(); i++){
                    widgetsNames[i] = widgetsToLoad.getString(i);
                }
                Arrays.sort(widgetsNames);
                
                //Widget[] widgets = new Widget[widgetsNames.length];
                ArrayList<Widget> widgets = new ArrayList<>();
                for (String widgetsName : widgetsNames) {
                    try {
                        //System.out.println(widgetsToLoad.getString(i));
                        String widgetClassName = widgetsName;
                        System.out.println(widgetClassName);
                        Widget widget = (Widget) (Class.forName(widgetClassName).newInstance());
                        try {
                            String preferredLocation = Config.getJSONConfig().getJSONObject("module").getJSONObject("config").getJSONObject(widgetClassName).getString("preferred-location");

                            System.out.println("PreferredLocation:" + widgetClassName + " preferredLocation" + preferredLocation);

                            if (preferredLocation != null) {
                                //System.out.println(widgetClassName + " preferredLocation" + preferredLocation);
                                switch (preferredLocation) {
                                    case "left-top":
                                        widget.setPreferredLocation(Widget.L_LEFTTOP);
                                        break;
                                    case "left-center":
                                        widget.setPreferredLocation(Widget.L_LEFTCENTER);
                                        break;
                                    case "left-bottom":
                                        widget.setPreferredLocation(Widget.L_LEFTBOTTOM);
                                        break;
                                    case "view-port":
                                        widget.setPreferredLocation(Widget.L_VIEWPORT);
                                        break;
                                    case "data-port":
                                        widget.setPreferredLocation(Widget.L_DATAPORT);
                                        break;
                                }
                            }
                        } catch (JSONException ex) {
                            //do nothing
                            //ex.printStackTrace();
                            //ex.printStackTrace();
                        }
                        //There are still chances to change the preferred location before adding
                        //addWidget(widget);
//                        widgets[i] = widget;
                        widgets.add(widget);
                    }catch (InstantiationException ex) {
                        System.err.println("InstantiationException");
                    }catch (IllegalAccessException ex) {
                        System.err.println("Illegal access");
                    }catch (ClassNotFoundException ex) {
                        System.err.println("Class not found");
                    }
                } //End of looping all the widgets
                
                Collections.sort(widgets, new Comparator<Widget>() {

                    @Override
                    public int compare(Widget o1, Widget o2) {
                        try{
                            return o1.getName().compareTo(o2.getName());
                        }
                        catch(Exception e){
                            return 1;
                        }
                    }
                });
                
                for(Widget widget : widgets){
                    addWidget(widget);
                }
                
                
                
                
            } catch (JSONException e) {
                initializeDefaults();
                return;
            }

        } else {
            initializeDefaults();
        }

    }

    private static void initializeDefaults() {
        //Load default Widgets
        //

        addWidget(new WidgetViewport());
//            addWidget(new WidgetMemoryUsage());

        addWidget(new WidgetSyncer());
        addWidget(new WidgetSearch());
        addWidget(new WidgetDataMatrix());
        addWidget(new WidgetAggregator());
        addWidget(new WidgetViewRenderer());
//            addWidget(new WidgetCoolMapProperties());
        //addWidget(new WidgetFilter());
        addWidget(new WidgetCMatrix());
        addWidget(new WidgetCOntology());
    }

    public static Widget getWidget(String className) {
        if (className != null) {
            //System.out.println("Getting: " + className);

            return _coolMapWidgets.get(className);
        } else {
            return null;
        }
    }

    public static WidgetViewport getViewport() {
        try {
            for (Map.Entry<String, Widget> entry : _coolMapWidgets.entrySet()) {
                if (entry.getValue().getClass().getName().endsWith(".WidgetViewport")) {
                    return (WidgetViewport) entry.getValue();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

}
