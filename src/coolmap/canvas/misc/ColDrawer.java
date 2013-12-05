/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.misc;

import coolmap.canvas.CoolMapView;
import coolmap.canvas.sidemaps.ColumnMap;
import coolmap.utils.graphics.CAnimator;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;

/**
 *
 * @author gangsu
 */
public class ColDrawer extends JLayeredPane implements ComponentListener {

    //private int _defaultHeight = 200;
    //private int _drawerContainerHeight = _defaultHeight; //actual height
    //private int __lastHeight = __defaultHeight; //records the last height before collapse
    private Integer _stackCounter = 0;
    private ArrayList<CDrawerHandle> _drawerHandles = new ArrayList<CDrawerHandle>();
    private ArrayList<ColumnMap> _columnMaps = new ArrayList<ColumnMap>();
    //private ArrayList<Integer> _drawerHeights = new ArrayList<Integer>();
    private final int _drawerContainerHandleHeight = 10;
    private final int _drawerContainerHandleWidth = 30;
    private final int _drawerHandleHeight = 13;
    private CDrawerContainerHandle _handle = new CDrawerContainerHandle();
    private TargetDrawerContainerToggle _targetToggle = new TargetDrawerContainerToggle();
    private Animator _toggleAnimator = CAnimator.createInstance(_targetToggle, 300);
    private JPanel _background = new JPanel() {

        @Override
        protected void paintComponent(Graphics grphcs) {
            //Graphics2D g2D = (Graphics2D) grphcs;
            //Graphics newGraphics = g2D.create(0, 0, getWidth(), getHeight());

            //newGraphics.setClip(new RoundRectangle2D.Double(-12, -12, getWidth() + 12, getHeight() + 12, 12, 12));
            super.paintComponent(grphcs);
        }
    };
    private final Point _colRowDimensions;
    private final int _defaultInitialHeight = 120;
    private int _initialHeight = _defaultInitialHeight;

    public void clearBuffers() {
        for (ColumnMap map : _columnMaps) {
            map.clearBuffer();
        }
    }

    public void justifyView() {
        for (ColumnMap map : _columnMaps) {
            map.justifyView();
        }
    }

    @Override
    public void setEnabled(boolean bln) {
        super.setEnabled(bln);
        for (ColumnMap map : _columnMaps) {
            map.setSetEnabled(bln);
        }
    }

    private ColDrawer() {
        this(null, null);
    }
    private CoolMapView _canvas;

    public ColDrawer(Point colRowDimensions, CoolMapView canvas) {
        _init();
        _background.addMouseListener(new MouseAdapter() {
        });
        _colRowDimensions = colRowDimensions;
        _canvas = canvas;
    }

    public int getContainerHandleHeight() {
        return _drawerContainerHandleHeight;
    }

    private void _init() {
        _background.setBackground(UI.mixOpacity(UI.colorGrey3, 200));
        add(_background, _stackCounter++);
        add(_handle, _stackCounter++);
        addComponentListener(this);
    }

    public void moveColumnMapToBottom(ColumnMap columnMap) {
        if (columnMap == null) {
            return;
        }

        List<ColumnMap> maps = getColumnMaps();
        clearColumnMaps();
        maps.remove(columnMap);
        maps.add(0, columnMap);
        setColumnMaps(maps);
    }

    public void moveColumnMapToTop(ColumnMap columnMap) {
        if (columnMap == null) {
            return;
        }

        List<ColumnMap> maps = getColumnMaps();
        clearColumnMaps();
        maps.remove(columnMap);
        maps.add(columnMap);
        setColumnMaps(maps);
    }

    public void addColumnMap(ColumnMap columnMap) {
        addColumnMap(columnMap, _defaultInitialHeight);

    }

    public ColumnMap getColumnMap(String className){
        for (ColumnMap map : _columnMaps) {
            if (map.getClass().getName().equals(className)) {
                return map;
            }
        }
        return null;
    }

    public void addColumnMap(ColumnMap columnMap, int initialHeight) {
        if (columnMap == null) {
            return;
        }

        if (_columnMaps.contains(columnMap)) {
            return;
        }

        for (ColumnMap exisitingColumnMap : _columnMaps) {
            if (exisitingColumnMap.getClass().equals(columnMap.getClass())) {
                return;
            }
        }

        //////////////////////////////////////////////////////////////
        JComponent panel = columnMap.getViewPanel();
        //if (_columnMaps.size() > 0) {
        //int lastHeight = _columnMaps.get(_columnMaps.size() - 1).getViewPanel().getHeight();
        //System.out.println(lastHeight);
        //}

        if (initialHeight < 0) {
            initialHeight = 0;
        }
        if (initialHeight > _initialHeight) {
            initialHeight = _initialHeight;
        }
        CDrawerHandle handle = new CDrawerHandle(columnMap, initialHeight);

        //These are the drawers
        _drawerHandles.add(handle);
        _columnMaps.add(columnMap);

        //int index = _drawerHandles.size() - 1;
        //In order to remove 
        add(panel, _stackCounter++);
        add(handle, _stackCounter++);

        _initialHeight -= 50;

//        for (int i = 0; i < _drawerHandles.size(); i++) {
//            _drawerHandles.get(i).updateDrawerBoundsWithContainer(getBounds());
//        }
//
//        for (ColumnMap map : _columnMaps) {
//            map.updateBuffer();
//            //System.out.println("buffer updated");
//        }
//
//        repaint();
//        columnMap.getCoolMapView().addCViewActiveCellChangedListener(columnMap);
//        columnMap.getCoolMapView().addCViewSelectionChangedListener(columnMap);
        columnMap.getCoolMapView().addCViewListener(columnMap);
        columnMap.getCoolMapObject().addCObjectDataListener(columnMap);
//        System.out.println(getBounds());

//      Should fix the problem here later.
        if (ColDrawer.this.getBounds().getHeight() > 0) {
            handle.updateDrawerBoundsWithContainer(ColDrawer.this.getBounds());
            columnMap.updateBuffer();
        }
    }

    public void updateDrawerHeight() {
        for (CDrawerHandle handle : _drawerHandles) {
            handle.updateDrawerBoundsWithContainer(ColDrawer.this.getBounds());
        }
    }

    /**
     * remove a certain drawer, and remove it
     *
     * @param columnMap
     */
    public void removeColumnMap(ColumnMap columnMap) {
        if (columnMap == null) {
            return;
        }
        List<ColumnMap> maps = getColumnMaps();
        maps.removeAll(Collections.singleton(columnMap));
        setColumnMaps(maps);
//        columnMap.getCoolMapView().removeViewActiveCellChangedListener(columnMap);
//        columnMap.getCoolMapView().removeViewSelectionChangedListener(columnMap);
        columnMap.getCoolMapView().removeCViewListener(columnMap);
        columnMap.getCoolMapObject().removeCObjectListener(columnMap);
        columnMap.destroy();
    }

    public void clearColumnMaps() {
        _stackCounter = new Integer(2);//Need to change the stack counter to 2
        _initialHeight = _defaultInitialHeight;
        for (CDrawerHandle handle : _drawerHandles) {
            remove(handle);
        }
        for (ColumnMap map : _columnMaps) {
            remove(map.getViewPanel());
//            map.getCoolMapView().removeViewActiveCellChangedListener(map);
//            map.getCoolMapView().removeViewSelectionChangedListener(map);
            map.getCoolMapView().removeCViewListener(map);
            map.getCoolMapObject().removeCObjectListener(map);
        }

        _drawerHandles.clear();
        _columnMaps.clear();

    }

    public void setColumnMaps(List<ColumnMap> columnMaps) {
        clearColumnMaps();
        if (columnMaps == null) {
            return;
        }
        for (ColumnMap columnMap : columnMaps) {
            if (columnMap != null) {
                addColumnMap(columnMap);
            }
        }
        for (int i = 0; i < _drawerHandles.size(); i++) {
            _drawerHandles.get(i).updateDrawerBoundsWithContainer(getBounds());
        }

        for (ColumnMap map : _columnMaps) {
            map.updateBuffer();
            //System.out.println("buffer updated");
        }

        repaint();
    }

    public List<ColumnMap> getColumnMaps() {
        return new ArrayList<ColumnMap>(_columnMaps);
    }

    /**
     * update all buffers of the column maps when needed.
     */
    public void updateDrawerBuffers(int minRow, int maxRow, int minCol, int maxCol, Rectangle dimension) {
        for (ColumnMap columnMap : _columnMaps) {
            columnMap.updateBuffer(minRow, maxRow, minCol, maxCol, dimension);
        }
    }

    public void updateDrawerBuffers() {
        for (ColumnMap columnMap : _columnMaps) {
            columnMap.updateBuffer();
        }
    }

    public void setDrawerContainerHeight(int height) {
        if (height < _drawerContainerHandleHeight + _drawerHandles.size() * _drawerHandleHeight) {
            height = _drawerContainerHandleHeight + _drawerHandles.size() * _drawerHandleHeight;
        }

        //difficult part... can't be more than 
        if (height > this.getParent().getHeight()) {
            height = this.getParent().getHeight();
        }

        //int delta = height - _drawerContainerHeight;
        int delta = (int) (height - _colRowDimensions.y);

        //__updateDrawerContainerBounds(delta);
        _updateDrawerContainerBounds(delta);
    }

    private void _updateDrawerContainerBounds(int delta) {
        //Rectangle bd = (Rectangle) getBounds().clone();
        //bd.height+=delta;
        //updateBounds(bd)
        //_drawerContainerHeight += delta;
        _colRowDimensions.y += delta;
        //quite awkward
        Rectangle b = getBounds();
        b.height += delta;
        setBounds(b);
    }

    @Override
    public void setBounds(Rectangle bounds) {
        Rectangle bd = (Rectangle) bounds.clone();
        //bd.height = _drawerContainerHeight;
        bd.height = (int) _colRowDimensions.y;
        bd.width = (int) (this.getParent().getWidth() - _colRowDimensions.x - 5);
        //bd.width -= _layerDrawerRow.getWidth();
        super.setBounds(bd);

        _handle.setBounds(0, bd.height - _drawerContainerHandleHeight, _drawerContainerHandleWidth, _drawerContainerHandleHeight);
        _canvas.updateRowDrawerBounds();
    }

    public void updateBounds() {
        Rectangle bd = new Rectangle();
        //bd.height = _drawerContainerHeight;
        bd.height = (int) (_colRowDimensions.y);
        bd.width = (int) (this.getParent().getWidth() - _colRowDimensions.x - 5);
        //bd.width -= _layerDrawerRow.getWidth();
        super.setBounds(bd);
        _handle.setBounds(0, bd.height - _drawerContainerHandleHeight, _drawerContainerHandleWidth, _drawerContainerHandleHeight);
    }

    @Override
    public void componentResized(ComponentEvent ce) {
        //System.out.println("Component resized");
        //need to trigger updates. but actually not quite useful.
        Rectangle b = (Rectangle) getBounds().clone();
        _background.setBounds(0, 0, b.width, b.height - _drawerContainerHandleHeight);

        for (int i = 0; i < _drawerHandles.size(); i++) {
            _drawerHandles.get(i).updateDrawerBoundsWithContainer(b);
        }

        _colRowDimensions.y = getHeight();

        //System.out.println(ColDrawer.this.getBounds());
    }

    @Override
    public void componentMoved(ComponentEvent ce) {
    }

    @Override
    public void componentShown(ComponentEvent ce) {
//        System.out.println("Component shown");
    }

    @Override
    public void componentHidden(ComponentEvent ce) {
    }

    private class TargetDrawerContainerToggle implements TimingTarget {

        private int __lastHeight; //= _colRowDimensions.y; //_defaultHeight;
        private int __from, __to;

        public void setLastHeight(int height) {
            __lastHeight = height;
        }

        @Override
        public void begin(Animator source) {
            this.__from = this.__lastHeight;
            this.__to = 0;
        }

        @Override
        public void end(Animator source) {
            for (ColumnMap map : _columnMaps) {
                map.updateBuffer();
                map.getViewPanel().repaint();
            }
        }

        @Override
        public void repeat(Animator source) {
        }

        @Override
        public void reverse(Animator source) {
            //this.____from = 0;
            //this.____to = ____lastHeight;
        }

        @Override
        public void timingEvent(Animator source, double fraction) {
            setDrawerContainerHeight((int) (__from + (__to - __from) * fraction));
        }
    }

    private class TargetDrawerToggle implements TimingTarget {

        private int __lastHeight = (int) (_colRowDimensions.y);  //_defaultHeight;
        private int __from, __to;
        private CDrawerHandle __drawerHandle;

        public TargetDrawerToggle(CDrawerHandle drawerHandle) {
            __drawerHandle = drawerHandle;
        }

        public void setLastHeight(int height) {
            this.__lastHeight = height;
        }

        @Override
        public void begin(Animator source) {
            this.__from = this.__lastHeight;
            this.__to = 0;
        }

        @Override
        public void end(Animator source) {
            __drawerHandle.__columnMapLinkedToHandle.updateBuffer();
        }

        @Override
        public void repeat(Animator source) {
        }

        @Override
        public void reverse(Animator source) {
        }

        @Override
        public void timingEvent(Animator source, double fraction) {
            __drawerHandle.updateDrawerHeight((int) (__from + (__to - __from) * fraction));
            ColDrawer.this.repaint();
        }
    }

//////////Private class for handle of each drawer
    private class CDrawerHandle extends JPanel implements MouseListener, MouseMotionListener {

        private JComponent __contentPane = null;
        private int __anchorY;
        private boolean __isDragging;
        private int __drawerHeight = 100;
        private TargetDrawerToggle __targetDrawerToggle;
        private Animator __toggleDrawerAnimator;

        public Animator getToggleAnimator() {
            return __toggleDrawerAnimator;
        }

        public TargetDrawerToggle getToggleTarget() {
            return __targetDrawerToggle;
        }

        public int getDrawerHeight() {
            return __drawerHeight;
        }

        public void updateDrawerBoundsWithContainer(Rectangle b) {
            Rectangle bounds = (Rectangle) b.clone();
            //Extra work is needed to check height!
            //makesure it's smaller 

            int index = _drawerHandles.indexOf(this);
            if (__drawerHeight + index * _drawerHandleHeight > bounds.height - _drawerContainerHandleHeight - _drawerHandleHeight) {
                __drawerHeight = bounds.height - index * _drawerHandleHeight - _drawerContainerHandleHeight - _drawerHandleHeight;
            }

            if (__drawerHeight < (_drawerHandles.size() - index - 1) * _drawerHandleHeight) {
                __drawerHeight = (_drawerHandles.size() - index - 1) * _drawerHandleHeight;
            }

            //Extra work is needed to check height yeah!
            bounds.height = __drawerHeight;

            //System.out.println(bounds);
            __contentPane.setBounds(bounds);
            setBounds(0, bounds.height, bounds.width, _drawerHandleHeight);

            //
            //__targetDrawerToggle.setLastHeight(bounds.height);
            //__columnMapLinkedToHandle.updateBuffer();
            //System.out.println(bounds);
        }

        public void updateDrawerHeight(int height) {

            //b.y = height;
            //Height need to be tested.
            int i = _drawerHandles.indexOf(this);
            int maxHeight, minHeight;

            if (i <= 0) {
                maxHeight = //_drawerContainerHeight 
                        (int) (_colRowDimensions.y - _drawerHandleHeight - _drawerContainerHandleHeight);
            } else {
                maxHeight = _drawerHandles.get(i - 1).getDrawerHeight() - _drawerHandleHeight;
            }

            if (i >= _drawerHandles.size() - 1) {
                minHeight = 0;
            } else {
                minHeight = _drawerHandles.get(i + 1).getDrawerHeight() + _drawerHandleHeight;
            }

            //System.out.println(maxHeight + " " + minHeight);
            if (height < minHeight) {
                height = minHeight;
            }
            if (height > maxHeight) {
                height = maxHeight;
            }

            __drawerHeight = height;
            Rectangle b = getBounds();
            b.y = height;
            setBounds(b);
            __contentPane.setBounds(0, 0, b.width, b.y);
            //System.out.println(__contentPane.getHeight());
        }

        private CDrawerHandle(int initialHeight) {
            __drawerHeight = initialHeight;
            //Must have a content pane
            addMouseListener(this);
            addMouseMotionListener(this);

            __targetDrawerToggle = new TargetDrawerToggle(this);
            __toggleDrawerAnimator = CAnimator.createInstance(__targetDrawerToggle, 200);
        }
        private ColumnMap __columnMapLinkedToHandle = null;

        public CDrawerHandle(ColumnMap map, int initialHeight) {

            this(initialHeight);
            __contentPane = map.getViewPanel();
            setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            __columnMapLinkedToHandle = map;

            _initPopupMenu();
        }

        private void _initPopupMenu() {
            JPopupMenu menu = new JPopupMenu();

            setComponentPopupMenu(menu);
            JMenuItem item = new JMenuItem("Move to Top", UI.getImageIcon("upThin"));
            menu.add(item);
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    moveColumnMapToTop(__columnMapLinkedToHandle);
                }
            });

            item = new JMenuItem("Move to Bottom", UI.getImageIcon("downThin"));
            menu.add(item);
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    moveColumnMapToBottom(__columnMapLinkedToHandle);
                }
            });
            menu.addSeparator();
            item = new JMenuItem("Remove", UI.getImageIcon("trashBin"));
            menu.add(item);
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    removeColumnMap(__columnMapLinkedToHandle);
                }
            });
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            //If it's a collapse, nothing need to be done. It will be updated accordingly.
            if (SwingUtilities.isLeftMouseButton(me) && me.getClickCount() >= 2) {
                if (__toggleDrawerAnimator.isRunning()) {
                    __toggleDrawerAnimator.stop();
                }

                int index = _drawerHandles.indexOf(this);
                //int maxHeight = (_drawerHandles.size()-1-index) * _drawerHandleHeight + 10;
                //The upper limit
                int maxHeight = 0;
                if (index == _drawerHandles.size() - 1) {
                    maxHeight = 0;
                } else {//if(index >=1){
                    maxHeight = _drawerHandles.get(index + 1).getY() + _drawerHandleHeight;
                }

                //no other maxibility
                //System.out.println(this.getY() + " " + maxHeight + " " + __drawerHeight);
                if (this.getY() <= maxHeight + 10) {
                    __toggleDrawerAnimator.startReverse();
                } else {
                    __targetDrawerToggle.setLastHeight(__drawerHeight); //__drawerHeight already set here.
                    __toggleDrawerAnimator.start();
                }

            }

        }
        private int _paneHeight;

        @Override
        public void mousePressed(MouseEvent me) {
            //System.out.println("Mouse handle pressed");
            if (SwingUtilities.isLeftMouseButton(me)) {
                __anchorY = me.getYOnScreen();
                __isDragging = true;
                _paneHeight = __contentPane.getHeight();
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            //update width
            if (SwingUtilities.isLeftMouseButton(me)) {

                __isDragging = false;

                if (__contentPane.getHeight() != _paneHeight) {
                    //should not be to much a deal to update a single panel.
                    __columnMapLinkedToHandle.updateBuffer();
                    __columnMapLinkedToHandle.getViewPanel().repaint();
                    _paneHeight = __contentPane.getHeight();
                }

            }
        }

        @Override
        public void mouseEntered(MouseEvent me) {
        }

        @Override
        public void mouseExited(MouseEvent me) {
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me) && __isDragging) {
                int delta = me.getYOnScreen() - __anchorY;
//                    __updateDrawerBounds(this, delta);

                updateDrawerHeight(getBounds().y + delta);

                __anchorY = me.getYOnScreen();
                ColDrawer.this.repaint();
                //_initialHeight = _columnMaps.get(0).getViewPanel().getHeight();
            }
        }

        @Override
        public void mouseMoved(MouseEvent me) {
        }
        private Color c = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
        private GradientPaint __paint = new GradientPaint(0, 0, UI.colorBlack3, 0, _drawerHandleHeight, UI.colorBlack6);
        private Font __labelFont = UI.fontPlain.deriveFont(Font.BOLD).deriveFont(11f);

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2D = (Graphics2D) g;
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2D.setPaint(__paint);
            g2D.fillRect(0, 0, getWidth(), getHeight());

            g2D.setColor(UI.colorBlack2);

            g2D.setFont(__labelFont);
            String label = __contentPane.getName();
            if (label == null || label.length() == 0) {
                label = "Untitled";
            }
            g2D.fillRoundRect(1, -3, g2D.getFontMetrics().stringWidth(label) + 14, _drawerHandleHeight + 2, 4, 4);

            g2D.setColor(UI.colorGrey1);

            g2D.drawString(label, 5, 10);
        }

        public JComponent getDrawer() {
            return __contentPane;
        }
    }//end of drawerhandle

/////////Private class
    private class CDrawerContainerHandle extends JPanel implements MouseListener, MouseMotionListener {

        private int __anchorY;
        private boolean __isDragging;

        public CDrawerContainerHandle() {
            addMouseListener(this);
            addMouseMotionListener(this);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void paint(Graphics g) {
            //Graphics2D g2D = (Graphics2D) g;
            //g2D.setColor(Color.RED);
            //g2D.fillRect(0, 0, getWidth(), getHeight());

            Graphics2D g2D = (Graphics2D) g;
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2D.setColor(UI.colorGrey4);
            g2D.fillRoundRect(-6, -6, this.getWidth() + 5, this.getHeight() - 1 + 5, 10, 10);

            g2D.setStroke(UI.stroke1_5);
            g2D.setColor(UI.colorBlack2);
            g2D.drawRoundRect(-7, -7, this.getWidth() + 5, this.getHeight() - 1 + 5, 8, 8);

            g2D.setStroke(UI.stroke1_5);
            g2D.setColor(UI.colorBlack4);
            g2D.drawLine(2, 2, _drawerContainerHandleWidth - 6, 2);

            g2D.drawLine(2, 4, _drawerContainerHandleWidth - 6, 4);

//            g2D.setColor(UIConstants.colorBlack2);
//            g2D.drawLine(3, 3, _drawerContainerHandleWidth-6+1, 3);
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me) && me.getClickCount() >= 1) {

                //setDrawerHeight(200);
                if (_toggleAnimator.isRunning()) {
                    _toggleAnimator.stop();
                }

                //Close enough to bondary
                //also need to take other handles into concern
                if (//_drawerContainerHeight 
                        _colRowDimensions.y < _drawerContainerHandleHeight + 10 + +_drawerHandles.size() * _drawerHandleHeight) {
                    //expand
                    //__targetToggle.reverse(__toggleAnimator);
//                    SwingUtilities.invokeLater(new Runnable() {
//
//                        @Override
//                        public void run() {
                    _toggleAnimator.startReverse();

                    //Also need to progressively expand other as well.
                    try {
                        Thread.sleep(100);
                        for (CDrawerHandle handle : _drawerHandles) {
                            if (handle.getToggleAnimator().isRunning()) {
                                handle.getToggleAnimator().stop();
                            }

                            handle.getToggleAnimator().startReverse();
                            //Thread.sleep(100);
                            Thread.sleep(50);
                            //break;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                        }
//                    });
                } else {
                    _targetToggle.setLastHeight((int) _colRowDimensions.y); //record height
                    for (CDrawerHandle handle : _drawerHandles) {
                        handle.getToggleTarget().setLastHeight(handle.getY());
                    }
                    _toggleAnimator.start();
                }

            }

        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me)) {
                this.__anchorY = me.getYOnScreen(); //getY is related to the source. If the target is moving, need to use different
                __isDragging = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me)) {
//                __isDragging = false;
                for (ColumnMap map : _columnMaps) {
                    map.updateBuffer();
                    map.getViewPanel().repaint();
                }
            }

        }

        @Override
        public void mouseEntered(MouseEvent me) {
        }

        @Override
        public void mouseExited(MouseEvent me) {
            //__isDragging = false;
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me) && __isDragging) {
                int delta = me.getYOnScreen() - __anchorY;

                setDrawerContainerHeight((int) (_colRowDimensions.y + delta));
                __anchorY = me.getYOnScreen();
                //System.out.println(___anchorY + " " + delta + " " + __height);
                ColDrawer.this.repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent me) {
        }
    }//end of inner class
}
