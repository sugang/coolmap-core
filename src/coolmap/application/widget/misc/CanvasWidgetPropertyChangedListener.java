/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.misc;

import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import coolmap.application.CoolMapMaster;
import java.awt.Menu;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author sugang
 */
public class CanvasWidgetPropertyChangedListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Dockable.class.isAssignableFrom(
                evt.getSource().getClass()) && evt.getPropertyName().equals("state") ) {
            //System.out.println("Dockable state changed");
            System.out.println(((Dockable)evt.getSource()).getTitle());
            
            if(evt.getNewValue().equals(DockableState.MAXIMIZED)){
                //disable a menu
                Menu view = (Menu)CoolMapMaster.getCMainFrame().findMenu("View/Show Widgets");
                view.setEnabled(false);
                //fix this later
                
                
            }
            else if(evt.getNewValue().equals(DockableState.NORMAL)){
                Menu view = (Menu)CoolMapMaster.getCMainFrame().findMenu("View/Show Widgets");
                if(!view.isEnabled()){
                    view.setEnabled(true);
                }
                                
            }
        }
    }
}
