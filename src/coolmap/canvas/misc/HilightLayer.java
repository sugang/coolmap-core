/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.misc;

import coolmap.utils.graphics.CAnimator;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;

/**
 *
 * @author gangsu
 */
public final class HilightLayer extends JPanel {

    private Color _hilightBaseColor = new Color(255, 237, 160);
    private float _opacity = 1.0f;
    private Color _tempColor = null;
    private final HashSet<Rectangle> _regions = new HashSet<Rectangle>();
    private TargetHilight _targetHilight = new TargetHilight();
    private Animator _hilightAnimator = CAnimator.createInstance(_targetHilight, 300);

    public HilightLayer() {
        setOpaque(false);
    }

    /**
     * set the region on the map
     * @param regions 
     */
    public synchronized void setRegions(Set<Rectangle> regions) {
        if (_hilightAnimator.isRunning()) {
            _hilightAnimator.cancel();
//            try {
//                _hilightAnimator.await();
//                _hilightAnimator.
//            } catch (InterruptedException e) {
//                //Wait for it to finish.
//                //e.printStackTrace();
//            }
        }
        _regions.clear();
        if(regions == null)
            return;
        
        _regions.addAll(regions);
        _regions.removeAll(Collections.singletonList(null));
        //System.out.println("updated regions");
    }

//    public synchronized void highlight(List<Rectangle> regions){
//        _regions.clear();
//        _regions.addAll(regions);
//        _regions.removeAll(Collections.singletonList(null));
//        if(_hilightAnimator.isRunning()){
//            _hilightAnimator.stop();
//        }
//        _hilightAnimator.start();
//    }
    /**
     * highlight the regions that were just added
     */
    public synchronized void highlight() {
        if (_regions.isEmpty()) {
            return;
        }
        if (_hilightAnimator.isRunning()) {
            _hilightAnimator.cancel();
//            try {
//                _hilightAnimator.await();
//            } catch (InterruptedException e) {
//                //Wait for it to finish.
//            }
        }
        _hilightAnimator.start();
    }

    private class TargetHilight implements TimingTarget {

        @Override
        public void begin(Animator source) {
            //System.out.println("light began");
            setVisible(true);
            _opacity = 1.0f;
            _tempColor = _hilightBaseColor;
        }

        @Override
        public void end(Animator source) {
            //System.out.println("light end");
            setVisible(false);
            _opacity = 0.0f;
            _regions.clear();//once done, clear all regions.
        }

        @Override
        public void repeat(Animator source) {
        }

        @Override
        public void reverse(Animator source) {
        }

        @Override
        public void timingEvent(Animator source, double fraction) {
            _opacity = (float) (1 - fraction);
            _tempColor = new Color(_hilightBaseColor.getRed(), _hilightBaseColor.getGreen(), _hilightBaseColor.getBlue(), (int) (255 * _opacity));
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        super.paintComponent(grphcs);
        Graphics2D g2D = (Graphics2D) grphcs.create();
        g2D.setColor(_tempColor);
        for (Rectangle rectangle : _regions) {
            g2D.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }
    }
}
