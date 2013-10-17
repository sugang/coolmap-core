/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget;

import coolmap.application.CoolMapMaster;
import coolmap.application.widget.impl.*;
import java.util.HashMap;

/**
 *
 * @author gangsu
 */
public class WidgetMaster {
    
    private WidgetMaster(){
        
    }

    public static String CANVAS = "coolmap.application.widget.impl.WidgetViewport";
    
    private static HashMap<String, Widget> _coolMapWidgets = new HashMap<String, Widget>();
    
    
    
    public static void addWidget(Widget widget) {
        
        //System.out.println(widget.getClass().getName() + " added" );
        if(widget == null){
            return;
        }
        _coolMapWidgets.put(widget.getClass().getName(), widget);
        CoolMapMaster.getCMainFrame().addWidget(widget);
        CoolMapMaster.getCMainFrame().addMenuItem("View/Show Widgets", widget.getMenuItem(), false);
    }

    public static void initialize() {

        addWidget(new WidgetViewport());
        addWidget(new WidgetMemoryUsage());
        addWidget(new WidgetSyncer());
        addWidget(new WidgetSearch());
        addWidget(new WidgetMatrix());
        addWidget(new WidgetAggregator());
        addWidget(new WidgetViewRenderer());
        addWidget(new WidgetCoolMapAttributes());
        addWidget(new WidgetFilter());
        addWidget(new WidgetCMatrix());
        addWidget(new WidgetCOntology());
    }

    public static Widget getWidget(String className) {
        if (className != null) {
            //System.out.println("Getting: " + className);
            
            return _coolMapWidgets.get(className);
        }
        else{
            return null;
        }
    }
}
