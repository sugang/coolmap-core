/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget;

import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.StateActionDockable;
import com.javadocking.dockable.action.DefaultDockableStateAction;
import com.javadocking.dockable.action.DefaultDockableStateActionFactory;
import coolmap.utils.StateSavable;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.json.JSONObject;

/**
 *
 * @author Gang Su
 */
public abstract class Widget implements StateSavable{

//    locations
    public static final int L_LEFTTOP = 0;
    public static final int L_LEFTCENTER = 1;
    public static final int L_LEFTBOTTOM = 2;
    public static final int L_VIEWPORT = 3;
    public static final int L_DATAPORT = 4;
    
//    widget types
    public static final int W_VIEWPORT = -1;
    public static final int W_MODULE = -2;
    public static final int W_DATA = -3;
    
//    
    private JPanel _contentPane = new JPanel();
    private DefaultDockable _dockable;
    private StateActionDockable _dockableWrapper;
    private int _preferredLocation = L_LEFTTOP;
    private int _type = W_DATA;
    private Icon _icon = null;
    private String _description = null;
    private String _title = null;
//    private String _id = null;
    
//    /**
//     * load application-wise global parameters
//     * @param config 
//     */
//    public void loadParameters(JSONObject config){
//        
//    }
//    
//    /**
//     * save application-wise global parameters
//     * such as 
//     * @param config
//     * @return 
//     */
//    public JSONObject saveParameters(JSONObject config){
//        return config;
//    }

    
    /**
     * save state of a widget to json string
     */
    public JSONObject saveToJSON(){
        return null;
    }
    
    /**
     * restore the state from a json config
     * @param config 
     */
    public void restoreFromJSON(JSONObject config){

    }
    
    /**
     * when extending this class, need to override constructor, and add type
     */
    private Widget() {

        this(null, 0, 0, null, null);
    }
    
    public void setPreferredLocation(int preferredLocation){
        _preferredLocation = preferredLocation;
    }
    
    

    public Widget(String name, int type, int preferredLocation, Icon icon, String description) {
        //There's a possibility that the unique names are not unique
        
//        _id = name;
        _title = name;
        _contentPane.setName(name);
        _type = type;
        _preferredLocation = preferredLocation;
        _icon = icon;
        _description = description;
        _contentPane.setPreferredSize(new Dimension(400, 300));
        
        initDockable();
        
        final DefaultDockableStateAction restoreAction = new DefaultDockableStateAction(getDockable(), DockableState.NORMAL);
        _showWidgetItem = new MenuItem(_title);
        
        _showWidgetItem.setEnabled(true);
        
        _showWidgetItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                restoreAction.actionPerformed(ae);
            }
        });
    }

    protected void initDockable() {
        int[] dockableStates = null;
        switch (_type) {
            case W_VIEWPORT:
                dockableStates = new int[]{DockableState.NORMAL, DockableState.EXTERNALIZED, DockableState.MAXIMIZED, DockableState.CLOSED};
                break;
            case W_MODULE:
                dockableStates = new int[]{DockableState.NORMAL, DockableState.EXTERNALIZED, DockableState.CLOSED};
                break;
            case W_DATA:
                dockableStates = new int[]{DockableState.NORMAL, DockableState.EXTERNALIZED, DockableState.CLOSED};
                break;
        }
        _dockable = new DefaultDockable(getClass().getName(), _contentPane, _title, _icon);
        
        
        _dockableWrapper = new StateActionDockable(_dockable, new DefaultDockableStateActionFactory(), dockableStates);
    }

    public void setTitle(String name, String description){
        _contentPane.setName(name);
        _dockable.setTitle(name);
        _contentPane.setToolTipText(description);
    }
    
    
    public final Dockable getDockable() {
        return _dockableWrapper;
    }

    public JComponent getContentPane() {
        return _contentPane;
    }

    /**
     * what is this ID? the class name. ID is the widget class name
     *
     * @return
     */
    public String getID() {
        return _dockable.getID();
    }

    public String getName() {
        return _title;
    }

    public final int getPreferredLocation() {
        return _preferredLocation;
    }
    private final MenuItem _showWidgetItem;

    public final MenuItem getMenuItem() {
        return _showWidgetItem;
    }
    
    public final void hide(){
        DefaultDockableStateAction hideAction = new DefaultDockableStateAction(getDockable(), DockableState.CLOSED);
        hideAction.actionPerformed(null);
        _showWidgetItem.setEnabled(true);
    }
    
    public final void show(){
        DefaultDockableStateAction restoreAction = new DefaultDockableStateAction(getDockable(), DockableState.NORMAL);
        restoreAction.actionPerformed(null);
        _showWidgetItem.setEnabled(false);
    }
    
    public final void enableMenuItem(){
        _showWidgetItem.setEnabled(true);
    }
    
    public final void disableMenuItem(){
        _showWidgetItem.setEnabled(false);
    }

    @Override
    public JSONObject getCurrentState() {
        return null;
    }

    @Override
    public boolean restoreState(JSONObject savedState) {
        return false;
    }
    
    
    
    
}
