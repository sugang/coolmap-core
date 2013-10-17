/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.misc;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import coolmap.utils.graphics.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;

/**
 *
 * @author gangsu
 */
public class ProgressMask extends JPanel {
    
    private BufferedImage _mask = null;
    private float _opacity = 1.0f;
    private final static GraphicsConfiguration _graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private int _maskHeight = 30;
    private int _maskWidth = 220;
    private int _offset = 10;
    //private Color _background = new Color(255,255,255,100);
    private TargetFade _fadeTarget = new TargetFade();
    private Animator _fadeAnimator = CAnimator.createInstance(_fadeTarget, 300);
    
    public ProgressMask() {
        setOpaque(false);
        MouseAdapter adapter = new MouseAdapter() {
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        setVisible(false);

        //Don't change cursor then.
        //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
    @Override
    protected void paintComponent(Graphics grphcs) {
        super.paintComponent(grphcs);
        Graphics2D g = (Graphics2D) grphcs.create();
        
        
        _mask = _graphicsConfiguration.createCompatibleImage(_maskWidth, _maskHeight, Transparency.TRANSLUCENT);
        Graphics2D g2b = _mask.createGraphics();
        g2b.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2b.setColor(UI.colorGrey3);
        
        g2b.fillRoundRect(-_offset, 0, _maskWidth + _offset, _maskHeight + _offset, _offset, _offset);

        //g2b.setColor(UI.colorBlack3);
        //g2b.setStroke(UI.stroke2);
        //g2b.drawRoundRect(-_offset, 0, _maskWidth + _offset, _maskHeight + _offset, _offset, _offset);
        //g2b.fillRoundRect(-_offset+2, 2, _maskWidth + _offset + 2, _maskHeight + _offset + 2, _offset, _offset);
        
        
        
        g2b.drawImage(UI.progressBar, _maskWidth / 2 - UI.progressBar.getWidth(null) / 2, _maskHeight / 2 - UI.progressBar.getHeight(null) / 2, this);
        
        g2b.dispose();
        
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _opacity));
        g.drawImage(_mask, 0, getHeight() - _maskHeight, this);

        ///////////
    }
    
    public synchronized void fadeIn() {
        try{
        if (_fadeAnimator.isRunning()) {
            _fadeAnimator.stop();
        }
        
        _opacity = 1.0f;
        setVisible(true);
        }
        catch(Exception e){
            _opacity = 1.0f;
            setVisible(true);
        }
    }
    
    public void fadeOut() {
        try {
            if (_fadeAnimator.isRunning()) {
                return;
            } else {
                _fadeAnimator.start();
            }
        } catch (Exception e) {
            _fadeAnimator.cancel();
            setVisible(false);
        }
    }

    /////////
    private class TargetFade implements TimingTarget {

        private float __startOpacity;
        private float __endOpacity;
        
        @Override
        public void begin(Animator source) {
            setVisible(true);
            __startOpacity = 1.0f;
            __endOpacity = 0.0f;
        }
        
        @Override
        public void end(Animator source) {
            //obviously set visible to false.
            setVisible(false);
        }
        
        @Override
        public void repeat(Animator source) {
        }
        
        @Override
        public void reverse(Animator source) {
        }
        
        @Override
        public void timingEvent(Animator source, double fraction) {
            _opacity = (float) (__startOpacity + (__endOpacity - __startOpacity) * fraction);
            repaint();
        }
    }
}
