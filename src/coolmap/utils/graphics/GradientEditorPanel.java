/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

/**
 *
 * @author sugang
 */
public class GradientEditorPanel extends JPanel {

    private final GradientEditor editor = new GradientEditor();
    private JButton addButton = null; //new JButton("Add");
    /**
     * A button to edit a control point
     */
    private JButton editButton = null; //new JButton("Edit");
    /**
     * A button to delete a control point
     */
    private JButton delButton = null; //new JButton("Del");

    private float minValue = 0.0f;
    private float maxValue = 1.0f;

    public GradientEditorPanel() {
        setLayout(new BorderLayout());
        add(editor, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setLayout(new FlowLayout(FlowLayout.CENTER));

        add(toolBar, BorderLayout.NORTH);

        addButton = new JButton("Add Point", UI.getImageIcon("plusSmall"));
        editButton = new JButton("Edit Point", UI.getImageIcon("pen"));
        delButton = new JButton("Remove Point", UI.getImageIcon("minusSmall"));

//        addButton.setBounds(20, 70, 75, 20);
        toolBar.add(addButton);
//        editButton.setBounds(100, 70, 75, 20);
        toolBar.add(editButton);
//        delButton.setBounds(180, 70, 75, 20);
        toolBar.add(delButton);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.addPoint();
            }
        });
        delButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.delPoint();
            }
        });
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.editPoint();
            }
        });

        this.setPreferredSize(new Dimension(300, 150));
        this.setMinimumSize(new Dimension(100, 150));
    }

    public int getNumPoints() {
        return editor.getControlPointCount();
    }

    public Color getColorAt(int index) {
        try {
            return editor.getColor(index);
        } catch (Exception e) {
            return null;
        }
    }
    
    public float getColorPercentAt(int index){
        try{
            return editor.getPointPos(index);
        } catch (Exception e){
            return -1;
        }
    }
    
    
    public void setStart(Color color){
        if(color != null){
            editor.setStart(color);
        }
    }
    
    
    public void setEnd(Color color){
        if(color != null){
            editor.setEnd(color);
        }
    }
    
    public void addColor(Color color, float pos){
        if(color == null || pos < 0  || pos > 1){
            return;
        }
        else{
            editor.addPoint(pos, color);
        }
    }
    
    public void clearColors(){
        editor.clearPoints();
    }
    
    public void setMinValue(float val){
        minValue = val;
        editor.repaint();
    }
    
    public void setMaxValue(float val){
        maxValue = val;
        editor.repaint();
    }
    
    

    public static void main(String[] argv) {
        UI.initialize();
        JFrame frame = new JFrame();
//        JPanel panel = new JPanel();
//        panel.setBorder(BorderFactory.createTitledBorder("Gradient"));
//        panel.setLayout(null);
//        frame.setContentPane(panel);
//        
//        
//
//        GradientEditor editor = new GradientEditor();
//
//        editor.setBounds(10, 15, 270, 100);
//        panel.add(editor);
//        frame.setSize(300, 200);
//
//        frame.addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                System.exit(0);
//            }
//        });
//
//        editor.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                //System.out.println("Gradient updated");
//            }
//        });

        frame.setContentPane(new GradientEditorPanel());
        frame.setPreferredSize(new Dimension(400, 300));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    private final class GradientEditor extends JPanel implements ComponentListener {

        /**
         * The list of control points
         */
        private ArrayList list = new ArrayList();
        /**
         * The current selcted control point
         */
        private ControlPoint selected;
        /**
         * The polygon used for the markers
         */
        private Polygon poly = new Polygon();
        /**
         * A button to add a control point
         */
//    private JButton addButton = null; //new JButton("Add");
//    /**
//     * A button to edit a control point
//     */
//    private JButton editButton = null; //new JButton("Edit");
//    /**
//     * A button to delete a control point
//     */
//    private JButton delButton = null; //new JButton("Del");

        /**
         * The x position of the gradient bar
         */
        private int x;
        /**
         * The y position of the gradient bar
         */
        private int y;
        /**
         * The width of the gradient bar
         */
        private int width;
        /**
         * The height of the gradient bar
         */
        private int barHeight;

        /**
         * The listeners that should be notified of changes to this emitter
         */
        private ArrayList listeners = new ArrayList();

        /**
         * Create a new editor for gradients
         *
         */
        public GradientEditor() {
            setLayout(null);

            AffineTransform at = new AffineTransform();
            at.rotate(Math.PI / 3);

            tickFont = UI.fontMono.deriveFont(at).deriveFont(Font.BOLD).deriveFont(12f);

//        addButton = new JButton(UI.getImageIcon("plusSmall"));
//        editButton = new JButton(UI.getImageIcon("pen"));
//        delButton = new JButton(UI.getImageIcon("minusSmall"));
//
//        addButton.setBounds(20, 70, 75, 20);
//        add(addButton);
//        editButton.setBounds(100, 70, 75, 20);
//        add(editButton);
//        delButton.setBounds(180, 70, 75, 20);
//        add(delButton);
//
//        addButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                addPoint();
//            }
//        });
//        delButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                delPoint();
//            }
//        });
//        editButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                editPoint();
//            }
//        });
            list.add(new ControlPoint(Color.white, 0));
            list.add(new ControlPoint(Color.black, 1));

            poly.addPoint(0, 0);
            poly.addPoint(5, 10);
            poly.addPoint(-5, 10);

            this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    selectPoint(e.getX(), e.getY());
                    repaint(0);

                    if (e.getClickCount() == 2) {
                        editPoint();
                    }
                }
            });

            this.addMouseMotionListener(new MouseMotionListener() {
                public void mouseDragged(MouseEvent e) {
                    movePoint(e.getX(), e.getY());
                    repaint(0);
                }

                public void mouseMoved(MouseEvent e) {
                }
            });

            this.addComponentListener(this);

        }

        /**
         * @see javax.swing.JComponent#setEnabled(boolean)
         */
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);

            Component[] components = getComponents();
            for (int i = 0; i < components.length; i++) {
                components[i].setEnabled(enabled);
            }
        }

        /**
         * Add a listener that will be notified on change of this editor
         *
         * @param listener The listener to be notified on change of this editor
         */
        public void addActionListener(ActionListener listener) {
            listeners.add(listener);
        }

        /**
         * Remove a listener from this editor. It will no longer be notified
         *
         * @param listener The listener to be removed
         */
        public void removeActionListener(ActionListener listener) {
            listeners.remove(listener);
        }

        /**
         * Fire an update to all listeners
         */
        private void fireUpdate() {
            ActionEvent event = new ActionEvent(this, 0, "");
            for (int i = 0; i < listeners.size(); i++) {
                ((ActionListener) listeners.get(i)).actionPerformed(event);
            }
        }

        /**
         * Check if there is a control point at the specified mouse location
         *
         * @param mx The mouse x coordinate
         * @param my The mouse y coordinate
         * @param pt The point to check agianst
         * @return True if the mouse point conincides with the control point
         */
        private boolean checkPoint(int mx, int my, ControlPoint pt) {
            int dx = (int) Math.abs((10 + (width * pt.pos)) - mx);
            int dy = Math.abs((y + barHeight + 7) - my);

            if ((dx < 5) && (dy < 7)) {
                return true;
            }

            return false;
        }

        /**
         * Add a new control point
         */
        private void addPoint() {
            ControlPoint point = new ControlPoint(Color.white, 0.5f);
            for (int i = 0; i < list.size() - 1; i++) {
                ControlPoint now = (ControlPoint) list.get(i);
                ControlPoint next = (ControlPoint) list.get(i + 1);
                if ((now.pos <= 0.5f) && (next.pos >= 0.5f)) {
                    list.add(i + 1, point);
                    break;
                }

            }
            selected = point;
            sortPoints();
            repaint(0);

            fireUpdate();
        }

        /**
         * Sort the control points based on their position
         */
        private void sortPoints() {
            final ControlPoint firstPt = (ControlPoint) list.get(0);
            final ControlPoint lastPt = (ControlPoint) list.get(list.size() - 1);
            Comparator compare = new Comparator() {
                public int compare(Object first, Object second) {
                    if (first == firstPt) {
                        return -1;
                    }
                    if (second == lastPt) {
                        return -1;
                    }

                    float a = ((ControlPoint) first).pos;
                    float b = ((ControlPoint) second).pos;
                    return (int) ((a - b) * 10000);
                }
            };
            Collections.sort(list, compare);
        }

        /**
         * Edit the currently selected control point
         *
         */
        private void editPoint() {
            if (selected == null) {
                return;
            }
            Color col = JColorChooser.showDialog(this, "Select Color", selected.col);
            if (col != null) {
                selected.col = col;
                repaint(0);
                fireUpdate();
            }
        }

        /**
         * Select the control point at the specified mouse coordinate
         *
         * @param mx The mouse x coordinate
         * @param my The mouse y coordinate
         */
        private void selectPoint(int mx, int my) {
            if (!isEnabled()) {
                return;
            }

            for (int i = 1; i < list.size() - 1; i++) {
                if (checkPoint(mx, my, (ControlPoint) list.get(i))) {
                    selected = (ControlPoint) list.get(i);
                    return;
                }
            }
            if (checkPoint(mx, my, (ControlPoint) list.get(0))) {
                selected = (ControlPoint) list.get(0);
                return;
            }
            if (checkPoint(mx, my, (ControlPoint) list.get(list.size() - 1))) {
                selected = (ControlPoint) list.get(list.size() - 1);
                return;
            }

            selected = null;
        }

        /**
         * Delete the currently selected point
         */
        private void delPoint() {
            if (!isEnabled()) {
                return;
            }

            if (selected == null) {
                return;
            }
            if (list.indexOf(selected) == 0) {
                return;
            }
            if (list.indexOf(selected) == list.size() - 1) {
                return;
            }

            list.remove(selected);
            sortPoints();
            repaint(0);
            fireUpdate();
        }

        /**
         * Move the current point to the specified mouse location
         *
         * @param mx The x coordinate of the mouse
         * @param my The y coordinate of teh mouse
         */
        private void movePoint(int mx, int my) {
            if (!isEnabled()) {
                return;
            }

            if (selected == null) {
                return;
            }
            if (list.indexOf(selected) == 0) {
                return;
            }
            if (list.indexOf(selected) == list.size() - 1) {
                return;
            }

            float newPos = (mx - 10) / (float) width;
            newPos = Math.min(1, newPos);
            newPos = Math.max(0, newPos);

            selected.pos = newPos;
            sortPoints();
            fireUpdate();
        }

        /**
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        public void paintComponent(Graphics g1d) {

            super.paintComponent(g1d);

            Graphics2D g = (Graphics2D) g1d;
            width = getWidth() - 30;
            x = 10;
            y = 20;
            barHeight = 25;

            g.getTransform();//this is the way to save it. It's funny I can't restore to origin!

            g.setStroke(UI.stroke1_5);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < list.size() - 1; i++) {
                ControlPoint now = (ControlPoint) list.get(i);
                ControlPoint next = (ControlPoint) list.get(i + 1);

                int size = (int) ((next.pos - now.pos) * width);
                g.setPaint(new GradientPaint(x, y, now.col, x + size, y, next.col));
                g.fillRoundRect(x, y, size + 1, barHeight, 2, 2);
                x += size;
            }

            g.setColor(Color.black);
            g.drawRect(10, y, width, barHeight - 1);

            for (int i = 0; i < list.size(); i++) {
                ControlPoint pt = (ControlPoint) list.get(i);
                g.translate(10 + (width * pt.pos), y + barHeight);
                g.setColor(pt.col);
                g.fillPolygon(poly);
                g.setColor(Color.black);
                g.drawPolygon(poly);

                if (pt == selected) {
                    g.setStroke(UI.stroke3);
                    g.setColor(UI.colorLightGreen1);

                    //g.fillRect(-5, 12, 10, 3);
                    g.drawPolygon(poly);
                }
                g.translate(-10 - (width * pt.pos), -y - barHeight);
            }

//            g.fillRect(0, 0, 5, 5);
            //now the rectangle is from 10, y to width, bar Height
            //draw ticks
            g.translate(10, y + barHeight + 15);
//            g.drawLine(0,0,0,10);
            g.setFont(tickFont);
            g.setStroke(UI.stroke1_5);
            for (int i = 0; i <= tickNum; i++) {

                g.setColor(UI.colorGrey5);
                g.drawLine(0, 0, 0, 10);

//                System.out.println(i * (maxValue - minValue)/tickNum + minValue + " " + maxValue + " " + minValue);
                g.setColor(UI.colorBlack2);
                g.translate(-4, 17);
                g.drawString(df.format(i * (maxValue - minValue) / tickNum + minValue), 0, 0);
                g.translate(4, -17);

                g.translate((int) (Math.round(1.0 * width / tickNum)), 0);
            }

        }

        private DecimalFormat df = new DecimalFormat("#.###");
        private final Font tickFont;

        private int tickNum = 10;

        /**
         * Add a control point to the gradient
         *
         * @param pos The position in the gradient (0 -> 1)
         * @param col The color at the new control point
         */
        public void addPoint(float pos, Color col) {
            ControlPoint point = new ControlPoint(col, pos);
            for (int i = 0; i < list.size() - 1; i++) {
                ControlPoint now = (ControlPoint) list.get(i);
                ControlPoint next = (ControlPoint) list.get(i + 1);
                if ((now.pos <= 0.5f) && (next.pos >= 0.5f)) {
                    list.add(i + 1, point);
                    break;
                }
            }
            repaint(0);
        }

        /**
         * Set the starting colour
         *
         * @param col The color at the start of the gradient
         */
        public void setStart(Color col) {
            ((ControlPoint) list.get(0)).col = col;
            repaint(0);
        }

        /**
         * Set the ending colour
         *
         * @param col The color at the end of the gradient
         */
        public void setEnd(Color col) {
            ((ControlPoint) list.get(list.size() - 1)).col = col;
            repaint(0);
        }

        /**
         * Remove all the control points from the gradient editor (this does not
         * include start and end points)
         */
        public void clearPoints() {
            for (int i = 1; i < list.size() - 1; i++) {
                list.remove(1);
            }

            repaint(0);
            fireUpdate();
        }

        /**
         * Get the number of control points in the gradient
         *
         * @return The number of control points in the gradient
         */
        public int getControlPointCount() {
            return list.size();
        }

        /**
         * Get the graident position of the control point at the specified
         * index.
         *
         * @param index The index of the control point
         * @return The graident position of the control point
         */
        public float getPointPos(int index) {
            return ((ControlPoint) list.get(index)).pos;
        }

        /**
         * Get the color of the control point at the specified index.
         *
         * @param index The index of the control point
         * @return The color of the control point
         */
        public Color getColor(int index) {
            return ((ControlPoint) list.get(index)).col;
        }

        @Override
        public void componentResized(ComponentEvent e) {
            width = getWidth();
            repaint();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }

        /**
         * A control point defining the gradient
         *
         * @author kevin
         */
        public class ControlPoint {

            /**
             * The color at this control point
             */
            public Color col;
            /**
             * The position of this control point (0 -> 1)
             */
            public float pos;

            /**
             * Create a new control point
             *
             * @param col The color at this control point
             * @param pos The position of this control point (0 -> 1)
             */
            private ControlPoint(Color col, float pos) {
                this.col = col;
                this.pos = pos;
            }
        }

        /**
         * Simple test case for the gradient painter
         *
         * @param argv The arguments supplied at the command line
         */
    }
}
