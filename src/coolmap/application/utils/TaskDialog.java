/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.utils;

import coolmap.application.CoolMapMaster;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 *
 * @author sugang
 */
public class TaskDialog extends JDialog {

    private String _taskName = "";
    private TaskPanel _taskPanel;
    
    public void setLabel(String label){
        _taskName = label;
    }
    
    public TaskDialog() {
        super(CoolMapMaster.getCMainFrame(), "Executing Task", false); //setting a modal window can cause a lot of issues
        _taskPanel = new TaskPanel();
                JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(_taskPanel, BorderLayout.CENTER);
        JButton button = new JButton("Cancel");
        panel.add(button, BorderLayout.SOUTH);
        this.getContentPane().add(panel);
        setSize(new Dimension(250, 140));
//        setAlwaysOnTop(true);
        setUndecorated(true);
        setLocationRelativeTo(CoolMapMaster.getCMainFrame());
        button.addActionListener(new TaskCancellationListener());
    }

    private class TaskPanel extends JPanel {

        private Image blockloader; 
        private Font font;
        
        public TaskPanel() {
            font = UI.fontPlain.deriveFont(11f).deriveFont(Font.BOLD);
            blockloader = UI.blockLoader;
        }

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);
            grphcs.drawImage(blockloader, getWidth() / 2 - blockloader.getWidth(this) / 2, getHeight() / 2 - blockloader.getHeight(this) / 2 - 10, this);
            Graphics2D g = (Graphics2D) grphcs.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(font);

            int width = g.getFontMetrics().stringWidth("Running: " + _taskName);
            g.setColor(UI.colorBlack3);
            g.drawString("Running: " + _taskName, getWidth() / 2 - width / 2, 100);
        }
    }
    
    

}
