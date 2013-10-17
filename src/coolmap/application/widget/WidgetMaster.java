/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget;

import coolmap.application.CoolMapMaster;
import coolmap.application.widget.impl.*;
import coolmap.utils.Config;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author gangsu
 */
public class WidgetMaster {

    private WidgetMaster() {

    }

    public static String CANVAS = "coolmap.application.widget.impl.WidgetViewport";

    private static HashMap<String, Widget> _coolMapWidgets = new HashMap<String, Widget>();

    public static void addWidget(Widget widget) {

        //System.out.println(widget.getClass().getName() + " added" );
        if (widget == null) {
            return;
        }
        _coolMapWidgets.put(widget.getClass().getName(), widget);

        CoolMapMaster.getCMainFrame().addWidget(widget);
        CoolMapMaster.getCMainFrame().addMenuItem("View/Show Widgets", widget.getMenuItem(), false);
    }

    public static void initialize() {

        if (Config.isInitialized()) {

            System.out.println("!!! Config file loading successful, loading widgets based on config file definitions");
            try {
                JSONArray widgetsToLoad = Config.getJSONConfig().getJSONObject("module").getJSONArray("load");
                for (int i = 0; i < widgetsToLoad.length(); i++) {
                    try {
                        //System.out.println(widgetsToLoad.getString(i));
                        String widgetClassName = widgetsToLoad.getString(i);

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
                        addWidget(widget);

                    } catch (InstantiationException ex) {
                        System.err.println("InstantiationException");
                    } catch (IllegalAccessException ex) {
                        System.err.println("Illegal access");
                    } catch (ClassNotFoundException ex) {
                        System.err.println("Class not found");
                    }

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
}
