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
public class ToggleAnnotation implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            CoolMapMaster.getActiveCoolMapObject().getCoolMapView().togglePaintAnnotation();
        } catch (Exception ex) {

        }
    }

}
