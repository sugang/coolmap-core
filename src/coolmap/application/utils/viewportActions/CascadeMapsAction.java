/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.utils.viewportActions;

import coolmap.application.widget.WidgetMaster;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author sugang
 */
public class CascadeMapsAction extends AbstractAction{

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            WidgetMaster.getViewport().cascadeWindows();
        } catch (Exception ex) {
        }
    }
    
}
