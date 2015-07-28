package coolmap;

import coolmap.application.CoolMapMaster;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.utils.CSplashScreen;
import coolmap.utils.Config;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
                loadAboutMenu();
            }
        });

    }

    private static void loadAboutMenu() {
        MenuItem testItem = new MenuItem("CoolMap Website");

        testItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Desktop.getDesktop().browse(java.net.URI.create("http://www.coolmap.org/"));
                        } catch (IOException ex) {
                            Logger.getLogger(CoolMap.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

            }
        });

        CoolMapMaster.getCMainFrame().addMenuItem("About", testItem, false, false);
    }
}
