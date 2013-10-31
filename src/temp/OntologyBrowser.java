/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package temp;

import com.apple.jobjc.JObjCRuntime;
import com.javadocking.util.SwingUtil;
import com.javadocking.visualizer.Visualizer;
import coolmap.data.contology.model.COntology;
import coolmap.data.contology.utils.COntologyUtils;
import coolmap.utils.graphics.CAnimator;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;
import sun.security.pkcs11.P11TlsKeyMaterialGenerator;

/**
 *
 * @author sugang
 */
public class OntologyBrowser {

    private COntology activeOntology;
    private OntologyVisualizer visualizer;
    private String activeTerm;
    private ArrayList<String> parents;
    private ArrayList<String> children;
    private ArrayList<String> siblings;

    public OntologyBrowser() {
        visualizer = new OntologyVisualizer();
    }

    public void setActiveCOntology(COntology ontology) {
        activeOntology = ontology;

    }

    public JPanel getCanvas() {
        return (JPanel) visualizer;
    }

    /**
     * will be a direct jump to after mouse clicks
     *
     * @param term
     */
    public void jumpToActiveTerm(String term) {
        parents = activeOntology.getImmediateParentsOrdered(term);
        children = activeOntology.getImmediateChildrenOrdered(term);
        siblings = null;
        activeTerm = term;
//        System.out.println(parents);
//        System.out.println(children)
        //reset many parameters, or trigger the animation process - you name it
        activeCenterIndex = null;
        activeParentIndex = null;
        activeChildIndex = null;

        visualizer.resetAnchors();

    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frame = new JFrame();
                UI.initialize();
                //frame.add();
                frame.setPreferredSize(new Dimension(800, 600));
                OntologyBrowser browser = new OntologyBrowser();
                frame.getContentPane().add(browser.getCanvas());
                frame.pack();
                frame.show();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

                //OntologyBrowser browser = new OntologyBrowser();
                //frame.add(browser);
                COntology ontology = COntologyUtils.createSampleOntology();
                browser.setActiveCOntology(ontology);

                browser.jumpToActiveTerm("RG2");
            }
        });
    }

    private MouseActionResponder mouseResponder = new MouseActionResponder();
    private KeyStrokeResponder keyResponder = new KeyStrokeResponder();
    private boolean parentsColumnActive = false;
    private boolean centerColumnActive = false;
    private boolean childrenColumnActive = false;
    private float fontSize = 10f;
    private int marginTB = 3;
    private int marginLR = 5;
    private Font labelFont;
    private Font labelFontBold;
    private int cellHeight = 20;

    private enum nodeType {

        ACTIVE_PARENT, ACTIVE_CENTER, ACTIVE_CHILD, PARENT, CENTER, CHILD
    };

    private class OntologyVisualizer extends JPanel {

        public Point anchorParents = new Point();
        public Point anchorCenter = new Point();
        public Point anchorChildren = new Point();

//        private Integer activeParentIndex = null;
//        private Integer activeChildIndex = null;
        public OntologyVisualizer() {
            labelFont = UI.fontMono.deriveFont(fontSize);
            labelFontBold = UI.fontMono.deriveFont(fontSize).deriveFont(Font.BOLD);
            addMouseListener(mouseResponder);
            addMouseMotionListener(mouseResponder);
            addKeyListener(keyResponder);
            addMouseWheelListener(keyResponder);
        }

        public void resetAnchors() {
            anchorParents.x = 0;
            anchorParents.y = 0;

            anchorCenter.x = getWidth() / 3;
            anchorCenter.y = getHeight() / 2 - cellHeight / 2;

            anchorChildren.x = getWidth() * 2 / 3;
            anchorChildren.y = 0;

            //refine them if parents and chilren are not null
            if (parents != null && !parents.isEmpty()) {
                int parentHeight = parents.size() * cellHeight;
                anchorParents.y = getHeight() / 2 - parentHeight / 2;
            }

            if (children != null && !children.isEmpty()) {
                int childrenHeight = children.size() * cellHeight;
                anchorChildren.y = getHeight() / 2 - childrenHeight / 2;
            }

//            System.out.println(getWidth() + " " + getHeight() + anchorCenter);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.
            Graphics2D g2D = (Graphics2D) g;

            int cellWidth = getWidth() / 3;
            anchorCenter.x = cellWidth;
            anchorChildren.x = cellWidth * 2;

            paintCell(g2D, anchorCenter, cellWidth, cellHeight, activeTerm, nodeType.ACTIVE_CENTER);

            if (parents != null && !parents.isEmpty()) {
                int offset = 0;
                for (int i = 0; i < parents.size(); i++) {
                    String parent = parents.get(i);
                    nodeType type = nodeType.PARENT;
                    if (activeParentIndex != null && i == activeParentIndex) {
                        type = nodeType.ACTIVE_PARENT;
                    }

                    paintCell(g2D, new Point(anchorParents.x, anchorParents.y + offset), cellWidth, cellHeight, parent, type);
                    offset += cellHeight;
                }
            }

            if (children != null && !children.isEmpty()) {
                int offset = 0;
                for (int i = 0; i < children.size(); i++) {
                    String child = children.get(i);
                    nodeType type = nodeType.CHILD;
                    if (activeChildIndex != null && i == activeChildIndex) {
                        type = nodeType.ACTIVE_CHILD;
                    }

                    paintCell(g2D, new Point(anchorChildren.x, anchorChildren.y + offset), cellWidth, cellHeight, child, type);
                    offset += cellHeight;
                }
            }

            //paint siblings over
            if (siblings != null && !siblings.isEmpty()) {
                
                //need to find the index
                int index = siblings.indexOf(activeTerm);
///               
//                System.out.println("Index of the current active node:" + index + "==" + siblings.size());
                
                if (index >= 0) {
                    //there are index ones before 
                    int siblingStart = anchorCenter.y - cellHeight * index;

                    for (int i = 0; i < index; i++) {
                        String label = siblings.get(i);
                        paintCell(g2D, new Point(getWidth() / 3, siblingStart + i * cellHeight), cellWidth, cellHeight, label, nodeType.CENTER);
                    }

                    //this part was not painted?
//                    System.out.println((index+1) + " " + siblings.size());
                    
                    for (int i = index + 1; i < siblings.size(); i++) {
//                        System.out.println("What the fuck this is not printed?");
                        String label = siblings.get(i);
//                        System.out.println(label);
                        paintCell(g2D, new Point(getWidth() / 3, siblingStart + i * cellHeight), cellWidth, cellHeight, label, nodeType.CENTER);
                    }
                }

            }

            if (visualizer.hasFocus()) {
                g2D.setColor(UI.colorLightGreen0);
                g2D.fillRect(0, 0, getWidth(), 10);
            }

            if (drawCellMoveIndicator) {
                g2D.setColor(Color.red);
                g2D.fillRect(cellMoveAnchor.x, cellMoveAnchor.y, cellWidth, cellHeight);
            }
        }

        private void paintCell(Graphics2D g2D, Point anchor, int width, int height, String label, nodeType type) {
            Color backgroundColor = Color.WHITE;

            switch (type) {
                case ACTIVE_PARENT:
                    backgroundColor = Color.BLUE;
                    break;
                case PARENT:
                    backgroundColor = Color.CYAN;
                    break;
                case ACTIVE_CHILD:
                    backgroundColor = Color.YELLOW;
                    break;
                case CHILD:
                    backgroundColor = Color.ORANGE;
                    break;
                case CENTER:
                    backgroundColor = Color.GRAY;
                    break;
            }

            g2D.setColor(backgroundColor);
            g2D.fillRect(anchor.x, anchor.y, width, height);
            g2D.setColor(Color.BLACK);
            g2D.drawRect(anchor.x, anchor.y, width, height);

            if (activeTerm != null) {
                g2D.drawString(label, anchor.x, anchor.y + height);
            }
        }

    }

    private Point cellMoveAnchor = new Point();
    private boolean drawCellMoveIndicator = false;
    private CellMoveTarget cellMoveTarget = new CellMoveTarget();
    private Animator cellMoveAnimator = CAnimator.createInstance(cellMoveTarget, 200);

    private class CellMoveTarget implements TimingTarget {

        private Point startAnchor, endAnchor;

        public CellMoveTarget() {

        }

        public void setCellMoveTarget(Point startAnchor, Point endAnchor) {
            this.startAnchor = startAnchor;
            this.endAnchor = endAnchor;
        }

        @Override
        public void begin(Animator source) {
            drawCellMoveIndicator = true;
        }

        @Override
        public void end(Animator source) {
            drawCellMoveIndicator = false;
            cellMoveAnchor.x = endAnchor.x;
            cellMoveAnchor.y = endAnchor.y;
        }

        @Override
        public void repeat(Animator source) {
        }

        @Override
        public void reverse(Animator source) {
        }

        @Override
        public void timingEvent(Animator source, double fraction) {
            cellMoveAnchor.x = (int) (startAnchor.x + ((endAnchor.x - startAnchor.x)) * fraction);
            cellMoveAnchor.y = (int) (startAnchor.y + ((endAnchor.y - startAnchor.y)) * fraction);
            visualizer.repaint();
        }

    }

    private class ColumnMoveTarget implements TimingTarget {

        Point anchorToMove;
        int moveBy;

        int startY;
        int endY;

        public ColumnMoveTarget() {
        }

        public void setup(Point anchorMove, int moveBy) {
            this.anchorToMove = anchorMove;
            this.moveBy = moveBy;
        }

        @Override
        public void begin(Animator source) {
            startY = anchorToMove.y;
            endY = anchorToMove.y + moveBy;
        }

        @Override
        public void end(Animator source) {
            anchorToMove.y = endY;
        }

        @Override
        public void repeat(Animator source) {
        }

        @Override
        public void reverse(Animator source) {
        }

        @Override
        public void timingEvent(Animator source, double fraction) {

            anchorToMove.y = (int) (startY + (endY - startY) * fraction);

//            System.out.println(fraction + " " + anchorToMove.y);q
            visualizer.repaint();
        }

    }

    private class KeyStrokeResponder implements KeyListener, MouseWheelListener {

        public final int UP = KeyEvent.VK_UP;
        public final int DOWN = KeyEvent.VK_DOWN;
        private ColumnMoveTarget columnScrollTarget = new ColumnMoveTarget();
        private Animator columnMoveAnimator = CAnimator.createInstance(columnScrollTarget, 200);

        public void moveColumn(Point anchor, int offset) {
            if (columnMoveAnimator.isRunning()) {
                //columnMoveAnimator.cancel();
                return;
            }
            columnScrollTarget.setup(anchor, offset);
            columnMoveAnimator.start();
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
//            System.out.println(parentsColumnActive + " " + centerColumnActive + " " + childrenColumnActive);
            if (parentsColumnActive) {
                if (e.getKeyCode() == UP) {
                    moveColumn(visualizer.anchorParents, -100);
                } else if (e.getKeyCode() == DOWN) {
                    moveColumn(visualizer.anchorParents, 100);
                }
            } else if (centerColumnActive) {
                if (e.getKeyCode() == UP) {
                    moveColumn(visualizer.anchorCenter, -100);
                } else if (e.getKeyCode() == DOWN) {
                    moveColumn(visualizer.anchorCenter, 100);
                }
            } else if (childrenColumnActive) {
                if (e.getKeyCode() == UP) {
                    moveColumn(visualizer.anchorChildren, -100);
                } else if (e.getKeyCode() == DOWN) {
                    moveColumn(visualizer.anchorChildren, 100);
                }
            }

        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int rotation = e.getWheelRotation();
//            System.out.println(rotation);
            Point anchorToMove;
            if (parentsColumnActive) {
                anchorToMove = visualizer.anchorParents;
            } else if (centerColumnActive) {
                anchorToMove = visualizer.anchorCenter;
            } else if (childrenColumnActive) {
                anchorToMove = visualizer.anchorChildren;
            } else {
                return;
            }

            if (rotation >= 3) {
                moveColumn(anchorToMove, 100);
            } else if (rotation > 0) {
                moveColumn(anchorToMove, 50);
            } else if (rotation > -3) {
                moveColumn(anchorToMove, -50);
            } else {
                moveColumn(anchorToMove, -100);
            }
        }

    }

    private Integer activeParentIndex = null;
    private Integer activeChildIndex = null;
    private Integer activeCenterIndex = null;

    private class MouseActionResponder implements MouseListener, MouseMotionListener {

        int dragStartY = 0;
        boolean draggingParents = false;
        boolean draggingCenter = false;
        boolean draggingChildren = false;

//        boolean activateParents;
//        boolean activateCenter;
//        boolean activateChildren;
        public void setActiveNodes(int x, int y) {
//            activateParents = activateCenter = activateChildren = false;
//            int newIndex;
//
//            //
//            if (x < visualizer.getWidth() / 3) {
//                activateParents = true;
//            } else if (x < visualizer.getWidth() * 2 / 3) {
//                activateCenter = true;
//            } else {
//                activateChildren = true;
//            }

            int newIndex;
            if (parentsColumnActive) {
                newIndex = (int) Math.floor(1.0f * (y - visualizer.anchorParents.y) / cellHeight);
                if (activeParentIndex == null || activeParentIndex != newIndex) {
                    activeParentIndex = newIndex;
                    //Then also update 
                    //siblings = activeOntology.getImmediateChildrenOrdered(active)

                    //System.out.println(activeParentIndex);
                    try {
                        if (activeParentIndex < 0) {
                            activeParentIndex = null;
                        } else if (activeParentIndex >= parents.size()) {
                            activeParentIndex = null;
                        }
                    } catch (Exception e) {
                        activeParentIndex = null;
                    }

                    if (activeParentIndex != null) {
                        String activeParentLabel = parents.get(activeParentIndex);
                        siblings = activeOntology.getImmediateChildren(activeParentLabel);
                        System.out.println(siblings);
                    } else {
                        siblings = null;
                    }
                    System.out.println("Siblings:" + siblings);
                    
//                    System.out.println("Pindex:" + activeParentIndex);
                }
                //else
                //activeParentIndex = null;

            }

            //deal with this later on
            if (centerColumnActive && siblings != null && !siblings.isEmpty()) {

            }

            if (childrenColumnActive) {
                newIndex = (int) Math.floor(1.0 * (y - visualizer.anchorChildren.y) / cellHeight);
                if (activeChildIndex == null || activeChildIndex != newIndex) {
                    activeChildIndex = newIndex;

                    //else
                    //activeChildIndex = null;
                    try {
                        if (activeChildIndex < 0) {
                            activeChildIndex = null;
                        } else if (activeChildIndex >= children.size()) {
                            activeChildIndex = null;
                        }
                    } catch (Exception e) {
                        activeChildIndex = null;
                    }

//                    System.out.println("CIndex: " + activeChildIndex);
                }
            }

        }

        @Override
        public void mouseClicked(MouseEvent e) {

            if (SwingUtilities.isLeftMouseButton(e)) {

                setActiveNodes(e.getX(), e.getY());
                visualizer.repaint();

                //repaint -> 
                if (e.getClickCount() > 1 && (childrenColumnActive || parentsColumnActive || centerColumnActive)) {
                    //jump to corresponding children or parent node
                    try {
                        if (childrenColumnActive) {
//                        System.out.println(activeChildIndex);
                            String newActiveTerm = children.get(activeChildIndex); //if exception happens

                            //move to center
                            if (cellMoveAnimator.isRunning()) {
                                cellMoveAnimator.cancel();
                            }
                            cellMoveTarget.setCellMoveTarget(new Point(visualizer.getWidth() * 2 / 3, visualizer.anchorChildren.y + activeChildIndex * cellHeight), new Point(visualizer.getWidth() / 3, visualizer.getHeight() / 2 - cellHeight / 2));
                            cellMoveAnimator.start();
                            jumpToActiveTerm(newActiveTerm);

                        } else if (parentsColumnActive) {
//                        System.out.println(activeParentIndex);
                            String newActiveTerm = parents.get(activeParentIndex);
                            //Need an indicator
                            if (cellMoveAnimator.isRunning()) {
                                cellMoveAnimator.cancel();
                            }
                            cellMoveTarget.setCellMoveTarget(new Point(0, visualizer.anchorParents.y + activeParentIndex * cellHeight), new Point(visualizer.getWidth() / 3, visualizer.getHeight() / 2 - cellHeight / 2));
                            cellMoveAnimator.start();

                            jumpToActiveTerm(newActiveTerm);
                        } else if (centerColumnActive) {
                            try {
                                int newIndex;
                                int indexOfCenter = siblings.indexOf(activeTerm);
                                System.out.println("index of center:" + indexOfCenter); //this is correct

                                if (indexOfCenter < 0) {
                                    return;
                                }

                                newIndex = (int) Math.floor(1.0 * (e.getY() - visualizer.anchorCenter.y + indexOfCenter * cellHeight) / cellHeight);
//                                if (newIndex < 0 || newIndex >= siblings.size()) {
//                                    return;
//                                } else {
//
//                                }

                                String term = siblings.get(newIndex);
                                if (cellMoveAnimator.isRunning()) {
                                    cellMoveAnimator.cancel();
                                }
                                cellMoveTarget.setCellMoveTarget(new Point(visualizer.getWidth()/3, visualizer.anchorCenter.y + newIndex * cellHeight - indexOfCenter * cellHeight), new Point(visualizer.getWidth() / 3, visualizer.getHeight() / 2 - cellHeight / 2));
                                cellMoveAnimator.start();
                                jumpToActiveTerm(term);

                            } catch (Exception ex) {

                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Change active center node error");
                    }
                }

            } else if (SwingUtilities.isRightMouseButton(e)) {
                if (parentsColumnActive) {
                    activeParentIndex = null;
                } else if (childrenColumnActive) {
                    activeChildIndex = null;
                }
            }

            visualizer.repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            draggingParents = draggingCenter = draggingChildren = false;

            if (e.getX() < visualizer.getWidth() / 3) {
                draggingParents = true;
                dragStartY = e.getY();
            } else if (e.getX() < visualizer.getWidth() * 2 / 3) {
                draggingCenter = true;
                dragStartY = e.getY();
            } else {
                draggingChildren = true;
                dragStartY = e.getY();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (draggingParents || draggingCenter || draggingChildren) {
                //stop drag
            }
            draggingParents = draggingCenter = draggingChildren = false;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            mouseReleased(e);
            visualizer.requestFocus();
            mouseMoved(e);
            visualizer.repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            mouseReleased(e);
            parentsColumnActive = centerColumnActive = childrenColumnActive = false;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (draggingParents || draggingCenter || draggingChildren) {
                //dragging
                if (draggingParents) {
                    int offset = e.getY() - dragStartY;
                    dragStartY = e.getY();
                    visualizer.anchorParents.y += offset;
                } else if (draggingCenter) {
                    int offset = e.getY() - dragStartY;
                    dragStartY = e.getY();
                    visualizer.anchorCenter.y += offset;
                } else if (draggingChildren) {
                    int offset = e.getY() - dragStartY;
                    dragStartY = e.getY();
                    visualizer.anchorChildren.y += offset;
                }

                visualizer.repaint();

            }

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            parentsColumnActive = centerColumnActive = childrenColumnActive = false;
            if (x < visualizer.getWidth() / 3) {
                parentsColumnActive = true;
            } else if (x < visualizer.getWidth() * 2 / 3) {
                centerColumnActive = true;
            } else {
                childrenColumnActive = true;
            }

        }

    }
}
