/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.utils.viewportActions;

import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.action.DefaultDockableStateAction;
import coolmap.application.CoolMapMaster;
import coolmap.application.widget.impl.WidgetViewport;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author sugang
 */
public class ToggleCanvasState implements ActionListener{

    @Override
    public void actionPerformed(ActionEvent e) {
        try{
                            WidgetViewport viewport = CoolMapMaster.getViewport();
                int state = viewport.getDockable().getState();
                if(state == DockableState.MAXIMIZED || state == DockableState.EXTERNALIZED){
                    DefaultDockableStateAction action = new DefaultDockableStateAction(viewport.getDockable(), DockableState.NORMAL);
                    action.actionPerformed(null);
                }
                else{
                    DefaultDockableStateAction action = new DefaultDockableStateAction(viewport.getDockable(), DockableState.MAXIMIZED);
                    action.actionPerformed(null);
                }
        }
        catch(Exception ex){
            
        }
    }
    
}
