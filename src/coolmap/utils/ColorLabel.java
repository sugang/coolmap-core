/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 *
 * @author sugang
 */
public class ColorLabel extends JLabel {
    
    
    public ColorLabel(Color color){
        super("   ");
        setBackground(color);
        setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
    }

    @Override
    protected void paintComponent(Graphics g) {
        
        Graphics2D g2D = (Graphics2D)g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setColor(getBackground());
        g2D.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 5, 5);
        
        super.paintComponent(g);
    }
    
    
}
