/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.utils;

import coolmap.application.CoolMapMaster;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.utils.Config;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author sugang
 */
public class AppClosingWindowAdapter extends WindowAdapter {

    @Override
    public void windowClosing(WindowEvent e) {

        CoolMapMaster.getCMainFrame().saveWorkspace(Config.getProperty(Config.WORKSPACE_DIRECTORY));
        Config.saveConfig();
        CMConsole.log("CoolMap Application will be closed");
        System.exit(0);
    }
}
