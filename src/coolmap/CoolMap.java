package coolmap;

import coolmap.application.CoolMapMaster;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.utils.CSplashScreen;
import coolmap.utils.Config;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author gangsu
 */
public class CoolMap {

    public static void main(String args[]) {

        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {

                Config.initialize();
                CSplashScreen.splashInit();

                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    Logger.getLogger(CoolMap.class.getName()).log(Level.WARNING, null, e);
                }

                CoolMapMaster.initialize();
                CoolMapMaster.getCMainFrame().loadWorkspace(Config.getProperty(Config.WORKSPACE_DIRECTORY) + "/default.dck");

                CMConsole.log("CoolMap initialized.");

            }
        });
        
    }
}