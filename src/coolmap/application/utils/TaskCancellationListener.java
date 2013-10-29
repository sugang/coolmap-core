/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.utils;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author sugang
 */
public class TaskCancellationListener extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        //
        //CoolMapMaster.getCMainFrame().showBusyDialog(false);
        
        //Cancel any current executed tasks
        //SingleTaskExecutor.cancelTask();
        
        //Then something has to happen here
        TaskEngine.getInstance().cancelAll();
        
    }
    
}
