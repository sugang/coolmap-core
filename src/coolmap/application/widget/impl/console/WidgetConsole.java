/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl.console;

import coolmap.application.widget.Widget;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author sugang
 */
public class WidgetConsole extends Widget {

    private final JPanel _container = new JPanel();
    private JTextPane consolePane = new JTextPane();

    public WidgetConsole() {
        super("Console", W_DATA, L_DATAPORT, UI.getImageIcon("console"), "Displays console information");
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_container, BorderLayout.CENTER);
        _container.setLayout(new BorderLayout());
        _container.add(new JScrollPane(consolePane), BorderLayout.CENTER);

        //
        consolePane.setFont(UI.fontMono.deriveFont(12f));
        consolePane.setBackground(new Color(255, 255, 204));

//        for (int i = 0; i < 100; i++) {
//            logError("abcdefgh\n");
//            logInfo("ABCDEFGH\n");
//            log("DEFGHIJKL\n");
//        }
        JPopupMenu popupMenu = new JPopupMenu();
        consolePane.setComponentPopupMenu(popupMenu);
        JMenuItem item = new JMenuItem("Clear", UI.getImageIcon("trashBin"));
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                consolePane.setText("");
            }
        });
        popupMenu.add(item);
    }

    public void logError(String message) {
        message = message.replaceAll("\n", "\n  ");
        message = "> " + message.trim();
        message += "\n";
        SimpleAttributeSet aset = new SimpleAttributeSet();
        StyleConstants.setBold(aset, true);
        StyleConstants.setForeground(aset, UI.colorAKABENI);
        appendToPane(message, aset);
    }

    public void logWaring(String message) {
        message = message.replaceAll("\n", "\n  ");
        message = "> " + message.trim();
        message += "\n";
        SimpleAttributeSet aset = new SimpleAttributeSet();
        StyleConstants.setBold(aset, true);
        StyleConstants.setForeground(aset, UI.colorOrange0);
        appendToPane(message, aset);
    }

    public void logInfo(String message) {
        message = message.replaceAll("\n", "\n  ");
        message = "> " + message.trim();
        message += "\n";
        SimpleAttributeSet aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset, UI.colorMIDORI);
        StyleConstants.setBold(aset, true);
        appendToPane(message, aset);
    }

    public void logData(String message) {
        message = message.replaceAll("\n", "\n  ");
        message = "> " + message.trim();
        message += "\n\n";
        SimpleAttributeSet aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset, UI.colorMIDORI);
        StyleConstants.setBold(aset, true);
        appendToPane(message, aset);
    }

    public void log(String message) {
        message = message.replaceAll("\n", "\n  ");
        message = "> " + message.trim();
        message += "\n";
        SimpleAttributeSet aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset, UI.colorBlack2);
        StyleConstants.setBold(aset, false);
        appendToPane(message, aset);
    }

    private void appendToPane(String message, SimpleAttributeSet aset) {
        int len = consolePane.getDocument().getLength();
        consolePane.setCaretPosition(len);
        consolePane.setCharacterAttributes(aset, false);
        consolePane.replaceSelection(message);
    }
}
