/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl.console;

import coolmap.application.widget.WidgetMaster;

/**
 *
 * @author sugang
 */
public class CMConsole {

    private static WidgetConsole instance = null;

    public static void logError(String message) {
        if (instance == null) {
            try {
                instance = (WidgetConsole) WidgetMaster.getWidget(WidgetConsole.class.getName());
            } catch (Exception e) {
                instance = null;
                return;
            }
        }

        if (instance != null) {
            instance.logError("[Error] " + message);
        } else {
            System.err.println("[Error] " + message);
        }

    }

    public static void logInfo(String message) {
        if (instance == null) {
            try {
                instance = (WidgetConsole) WidgetMaster.getWidget(WidgetConsole.class.getName());
            } catch (Exception e) {
                instance = null;
                return;
            }
        }

        instance.logInfo("[Info] " + message);
    }

    public static void log(String message) {
        if (instance == null) {
            try {
                instance = (WidgetConsole) WidgetMaster.getWidget(WidgetConsole.class.getName());
            } catch (Exception e) {
                instance = null;
                return;
            }
        }

        instance.log(message);
    }

    public static void logWarning(String message) {
        if (instance == null) {
            try {
                instance = (WidgetConsole) WidgetMaster.getWidget(WidgetConsole.class.getName());
            } catch (Exception e) {
                instance = null;
                return;
            }
        }

        instance.logWaring("[Warning] " + message);
    }

}
