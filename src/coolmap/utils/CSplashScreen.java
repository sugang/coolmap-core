/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SplashScreen;

/**
 *
 * @author sugang
 */
public class CSplashScreen {

    private static SplashScreen splashScreen = null;
    private static int splashWidth = 0;
    private static int splashHeight = 0;
    private static Rectangle splashTextBox = null;
    private static Color splashBoxBGColor = null;
    private static Color splashTextColor = null;
    private static Font splashFont = null;
    private static Color splashProgressColor = null;

    public static void splashInit() {
        splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) {
            Dimension dim = splashScreen.getSize();

            System.out.println(dim);
            splashWidth = dim.width;
            splashHeight = dim.height;

            splashTextBox = new Rectangle(10, splashHeight - 50, splashWidth - 20, 40);
            splashBoxBGColor = new Color(245, 245, 245, 255);
            splashTextColor = new Color(50, 50, 50);
            splashProgressColor = new Color(48, 224, 255);

            try {
                splashFont = new Font("Arial", Font.PLAIN, 12);

            } catch (Exception e) {
                splashFont = new Font("serif", Font.PLAIN, 12);
            }

//            Graphics2D sg = splashScreen.createGraphics();
//            sg.setFont(splashFont);
//            sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
////            sg.setPaint(Color.RED);
////            sg.fillRect(0, 0, dim.width, dim.height);
//            sg.translate(splashTextBox.x, splashTextBox.y);
//            sg.setColor(splashBoxBGColor);
//            sg.fillRoundRect(0, 0, splashTextBox.width, splashTextBox.height, 5, 5);
//
//            sg.setFont(splashFont);
//            sg.setColor(splashTextColor);
//            sg.drawString("Intializing...", 10, splashFont.getSize() + 5);
//            splashScreen.update();
            splashUpdate("Initializing...", 0);

            if (Config.isInitialized()) {
                String version = Config.getProperty(Config.VERSION);
                if (version != null) {

//                System.out.println("Version: " + version);
                    Graphics2D sg = splashScreen.createGraphics();
                    sg.setFont(splashFont);
                    sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    sg.setFont(splashFont.deriveFont(10f));
                    sg.setColor(splashTextColor);

                    version = "Version: " + version;

                    int versionW = sg.getFontMetrics().stringWidth(version);

                    System.out.println(version);
                    sg.drawString(version, splashWidth - 15 - versionW, 20);
                    splashScreen.update();
                }
            }
        }
    }

    public static void splashUpdate(String text, int progress) {
        if (splashScreen == null) {
            return;
        }
        try {
            Graphics2D sg = splashScreen.createGraphics();
            sg.setFont(splashFont);
            sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            sg.translate(splashTextBox.x, splashTextBox.y);

            sg.setColor(Color.LIGHT_GRAY);
            sg.fillRoundRect(0, 1, splashTextBox.width, splashTextBox.height, 5, 5);

            sg.setColor(splashBoxBGColor);
            sg.fillRoundRect(0, 0, splashTextBox.width, splashTextBox.height, 5, 5);

            sg.setFont(splashFont);
            sg.setColor(splashTextColor);
            sg.drawString(text, 10, splashFont.getSize() + 5);

            if (progress < 0) {
                progress = 0;
            }
            if (progress > 100) {
                progress = 100;
            }

            int width = (int) (progress * (splashTextBox.width) / 100.0f);

//            sg.setColor(Color.LIGHT_GRAY);
//            sg.fillRoundRect(10, 27, width, 5, 4, 4);
            sg.setColor(splashProgressColor);
            sg.fillRoundRect(10, 25, width, 5, 4, 4);

            splashScreen.update();
        } catch (Exception e) {

        }
    }
}
