package coolmap.application.utils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import coolmap.application.CoolMapMaster;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

/**
 *
 * @author gangsu
 */
public class Messenger {

    public static void showWarningMessage(String message, String title) {
        JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), message, title, JOptionPane.WARNING_MESSAGE, null);
    }

    public static void showErrorMessage(String message, String title) {
        //message will be very long. need a scroll pane
        JTextArea textArea = new JTextArea(message);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), scrollPane, title, JOptionPane.ERROR_MESSAGE, null);
    }
    
    
    public static void showStackTrace(Exception e, String title){
       showStackTrace(e, title, "No description");
    }
    
    
    public static void showStackTrace(Exception e, String title, String message){
        StringBuilder sb = new StringBuilder();
        sb.append("--Error Description---------------\n\n");
        sb.append(message);
        sb.append("\n\n--Stack Trace---------------\n\n");
        sb.append(e.getMessage());
        sb.append("\n");
        sb.append("\n");
        for(StackTraceElement s : e.getStackTrace()){
            
            sb.append(s);
            sb.append("\n");
        }
        showErrorMessage(sb.toString(), title);
    }
}
