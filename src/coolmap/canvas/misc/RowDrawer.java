package coolmap.canvas.misc;

import coolmap.canvas.CoolMapView;
import coolmap.canvas.sidemaps.RowMap;
import coolmap.utils.Tools;
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
import java.awt.image.BufferedImage;
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
public class RowDrawer extends JLayeredPane implements ComponentListener {

//    private int _defaultWidth = 200;
//    private int _drawerCointainerWidth = _defaultWidth;
    private Integer _stackCounter = 0;
    private ArrayList<CDrawerHandle> _drawerHandles = new ArrayList<>();
    private ArrayList<RowMap> _rowMaps = new ArrayList<>();
    private CDrawerContainerHandle _handle = new CDrawerContainerHandle();
    private final int _drawerContainerHandleHeight = 30;
    private final int _drawerContainerHandleWidth = 10;
    private final int _drawerHandleWidth = 13;
    private TargetDrawerCointainerToggle _targetContainerToggle = new TargetDrawerCointainerToggle();
    private Animator _toggleContainerAnimator = CAnimator.createInstance(_targetContainerToggle, 300);
    private JPanel _background = new JPanel();
    private final Point _colRowDimensions;
    private final int _defaultInitialWidth = 120;
    private int _initialWidth = _defaultInitialWidth;

    public void clearBuffer() {
        for (RowMap map : _rowMaps) {
            map.clearBuffer();
        }
    }

    public void updateDrawerBuffers() {
        for (RowMap map : _rowMaps) {
            map.updateBuffer();
        }
    }

    @Override
    public void setEnabled(boolean bln) {
        super.setEnabled(bln);
        for (RowMap map : _rowMaps) {
            map.setSetEnabled(bln);
        }
    }

    private RowDrawer() {
        this(null, null);
    }

    public void justifyView() {
        for (RowMap map : _rowMaps) {
            map.justifyView();
        }
    }
    private CoolMapView _canvas;

    public RowDrawer(Point colRowDimensions, CoolMapView canvas) {
        _init();
        _background.addMouseListener(new MouseAdapter() {
        });
        _colRowDimensions = colRowDimensions;
        _canvas = canvas;
    }

    public int getContainerHandleWidth() {
        return _drawerContainerHandleWidth;
    }

    private void _init() {
        setOpaque(false);
        _background.setBackground(UI.mixOpacity(UI.colorGrey3, 200));
        add(_background, _stackCounter++);
        add(_handle, _stackCounter++);
        addComponentListener(this);
    }

    public void moveRowMapToBottom(RowMap rowMap) {
        if (rowMap == null) {
            return;
        }

        List<RowMap> maps = getRowMaps();
        clearRowMaps();
        maps.remove(rowMap);
        maps.add(0, rowMap);
        setRowMaps(maps);
    }

    public void moveRowMapToTop(RowMap rowMap) {
        if (rowMap == null) {
            return;
        }

        List<RowMap> maps = getRowMaps();
        clearRowMaps();
        maps.remove(rowMap);
        maps.add(rowMap);
        setRowMaps(maps);
    }

    public void removeRowMap(RowMap rowMap) {
        if (rowMap == null) {
            return;
        }
        List<RowMap> maps = getRowMaps();
        maps.removeAll(Collections.singleton(rowMap));
        setRowMaps(maps);
//        rowMap.getCoolMapView().removeViewActiveCellChangedListener(rowMap);
//        rowMap.getCoolMapView().removeViewSelectionChangedListener(rowMap);
        rowMap.getCoolMapView().removeCViewListener(rowMap);
        rowMap.getCoolMapObject().removeCObjectListener(rowMap);
        rowMap.destroy();
    }

    public void setRowMaps(List<RowMap> rowMaps) {
        clearRowMaps();
        if (rowMaps == null) {
            return;
        }
        for (RowMap rowMap : rowMaps) {
            if (rowMap != null) {
                addRowMap(rowMap);
            }
        }
        for (CDrawerHandle _drawerHandle : _drawerHandles) {
            _drawerHandle.updateDrawerBoundsWithContainer(getBounds());
        }

        for (RowMap map : _rowMaps) {
            map.updateBuffer();
            //System.out.println("buffer updated");
        }

        repaint();
    }

    public List<RowMap> getRowMaps() {
        return new ArrayList<>(_rowMaps);
    }

    public void updateDrawerBuffers(int minRow, int maxRow, int minCol, int maxCol, Rectangle dimension) {
        for (RowMap rowMap : _rowMaps) {
            rowMap.updateBuffer(minRow, maxRow, minCol, maxCol, dimension);
        }
    }

    public void clearRowMaps() {
        _stackCounter = 2;//Need to change the stack counter to 2
        _initialWidth = _defaultInitialWidth;
        for (CDrawerHandle handle : _drawerHandles) {
            remove(handle);
        }
        for (RowMap map : _rowMaps) {
            remove(map.getViewPanel());
            map.getCoolMapView().removeCViewListener(map);
            map.getCoolMapObject().removeCObjectListener(map);
        }

        _drawerHandles.clear();
        _rowMaps.clear();
    }

    @Override
    public void setBounds(Rectangle bounds) {
        //System.out.println("Bounds set");
        Rectangle b = new Rectangle();

        b.x = this.getParent().getWidth() - _colRowDimensions.x;
        b.y = _colRowDimensions.y + 5;
        b.width = _colRowDimensions.x;
        b.height = bounds.height - _colRowDimensions.y - 5;
        //System.out.println(b);
        super.setBounds(b);

        //System.out.println(_handle.getBounds());
        //System.out.println(getHeight());
        _canvas.updateColDrawerBounds();
    }

    public void updateBounds() {
        Rectangle b = new Rectangle();
        b.x = this.getParent().getWidth() - _colRowDimensions.x;
        b.y = _colRowDimensions.y + 5;
        b.width = _colRowDimensions.x;
        b.height = this.getParent().getHeight() - _colRowDimensions.y - 5;
        //System.out.println(b);
        super.setBounds(b);
    }

    public void addRowMap(RowMap map) {
        addRowMap(map, _defaultInitialWidth);
    }

    public void updateDrawerHeight() {
        for (CDrawerHandle handle : _drawerHandles) {
            handle.updateDrawerBoundsWithContainer(RowDrawer.this.getBounds());
        }
    }

    public RowMap getRowMap(String className) {
        try {
            for (RowMap map : _rowMaps) {
                if (map.getClass().getName().equals(className)) {
                    return map;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void addRowMap(RowMap rowMap, int initialWidth) {
        if (rowMap == null) {
            return;
        }

        if (_rowMaps.contains(rowMap)) {
            return;
        }

        for (RowMap existingRowMap : _rowMaps) {
            if (existingRowMap.getClass().equals(rowMap.getClass())) {
                return;
            }
        }

        JComponent panel = rowMap.getViewPanel();

        if (initialWidth < 0) {
            initialWidth = 0;
        }
        if (initialWidth > _initialWidth) {
            initialWidth = _initialWidth;
        }

        //
        CDrawerHandle handle = new CDrawerHandle(rowMap, initialWidth);

        _drawerHandles.add(handle);

        _rowMaps.add(rowMap);
        //int index = _drawerHandles.size() - 1;

        add(panel, _stackCounter++);
        add(handle, _stackCounter++);

        _initialWidth -= 50;
        rowMap.getCoolMapView().addCViewListener(rowMap);
        rowMap.getCoolMapObject().addCObjectDataListener(rowMap);
        
        if (RowDrawer.this.getBounds().getWidth() > 0) {
            handle.updateDrawerBoundsWithContainer(RowDrawer.this.getBounds());
            rowMap.updateBuffer();
        }
    }

    private void _updateCointainerWidth(int delta) {
        //System.out.println("ContainerWidth updated");
        //also want to monitor delta

        _colRowDimensions.x -= delta;

        if (_colRowDimensions.x < _drawerContainerHandleWidth + _drawerHandles.size() * _drawerHandleWidth) {
            _colRowDimensions.x = _drawerContainerHandleWidth + _drawerHandles.size() * _drawerHandleWidth;
        }

        if (_colRowDimensions.x > this.getParent().getWidth()) {
            _colRowDimensions.x = this.getParent().getWidth();
        }

        setBounds(new Rectangle(0, 0, this.getParent().getWidth(), this.getParent().getHeight()));
        //set the containers bounds to
        //width ---
        _background.setBounds(_drawerContainerHandleWidth, 0, _colRowDimensions.x - _drawerContainerHandleWidth, getHeight());

        //also all drawers need to update width
    }

    public void setDrawerContainerWidth(int newWidth) {
        //need to do some control on the newWidth
        if (newWidth < _drawerContainerHandleWidth + _drawerHandles.size() * _drawerHandleWidth) {
            newWidth = _drawerContainerHandleWidth + +_drawerHandles.size() * _drawerHandleWidth;//can't be smaller than width
        }

        int delta = -(newWidth - _colRowDimensions.x);
        _updateCointainerWidth(delta);
    }
    private int _handleOffset = 0;

    public void updateHandleOffset(int offset) {
        //System.out.println("Handle offset:" + _handleOffset);
        _handleOffset = offset;
        _forceUpdateBounds();
    }

    private void _forceUpdateBounds() {
        _handle.setBounds(0, _handleOffset, _drawerContainerHandleWidth, _drawerContainerHandleHeight);

        //repaint();
        Rectangle bounds = new Rectangle(_drawerContainerHandleWidth, 0, _colRowDimensions.x - _drawerContainerHandleWidth, getHeight());
        _background.setBounds(bounds);

        //
        for (CDrawerHandle drawerHandle : _drawerHandles) {
            drawerHandle.updateDrawerBoundsWithContainer(bounds);
        }
    }

    @Override
    public void componentResized(ComponentEvent ce) {
        //Not the bound change, when reflected to. The component resize only changes the heigth of all components.
        //And the position of the handle.
        //_handle.setBounds(0, getHeight() - _drawerContainerHandleHeight, _drawerContainerHandleWidth, _drawerContainerHandleHeight);

        //need to know the height of layer drawer col
        _forceUpdateBounds();

    }

    @Override
    public void componentMoved(ComponentEvent ce) {
    }

    @Override
    public void componentShown(ComponentEvent ce) {
    }

    @Override
    public void componentHidden(ComponentEvent ce) {
    }

    private class CDrawerHandle extends JPanel implements MouseListener, MouseMotionListener {

        private JComponent __contentPane = null;
        private int __anchorX;
        private boolean __isDragging;
        private int __drawerWidth = 100;
        private TargetDrawerToggle __targetDrawerToggle;
        private Animator __toggleDrawerAnimator;
        private RowMap __rowMapLinkedToHandle;

        public CDrawerHandle(int initialWidth) {
            this.setOpaque(false);
            __drawerWidth = initialWidth;

            addMouseListener(this);
            addMouseMotionListener(this);

            __targetDrawerToggle = new TargetDrawerToggle(this);
            __toggleDrawerAnimator = CAnimator.createInstance(__targetDrawerToggle, 200);

            //setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            MouseAdapter blocker = new MouseAdapter() {
            };
            _background.addMouseListener(blocker);
        }

        public CDrawerHandle(RowMap map, int initalWidth) {
            this(initalWidth);

            __contentPane = map.getViewPanel();
            setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            __rowMapLinkedToHandle = map;
            _initPopupMenu();
        }

        private void _initPopupMenu() {
            JPopupMenu menu = new JPopupMenu();
            setComponentPopupMenu(menu);

            setComponentPopupMenu(menu);
            JMenuItem item = new JMenuItem("Move to Top", UI.getImageIcon("upThin"));
            menu.add(item);
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    moveRowMapToTop(__rowMapLinkedToHandle);
                }
            });

            item = new JMenuItem("Move to Bottom", UI.getImageIcon("downThin"));
            menu.add(item);
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    moveRowMapToBottom(__rowMapLinkedToHandle);
                }
            });
            menu.addSeparator();
            item = new JMenuItem("Remove", UI.getImageIcon("trashBin"));
            menu.add(item);
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    removeRowMap(__rowMapLinkedToHandle);
                }
            });

        }
        private Color c = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());

        public JComponent getDrawer() {
            return __contentPane;
        }
        private GradientPaint __paint = new GradientPaint(0, 0, UI.colorBlack6, _drawerHandleWidth, 0, UI.colorBlack3);
        private Font __labelFont = UI.fontPlain.deriveFont(Font.BOLD).deriveFont(11f);

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2D = (Graphics2D) g;
            //g2D.setColor(c);
            g2D.fillRect(0, 0, getWidth(), getHeight());
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2D.setPaint(__paint);
            g2D.fillRect(0, 0, getWidth(), getHeight());

            String label = __contentPane.getName();
            if (label == null || label.length() == 0) {
                label = "Untitled";
            }

            g2D.setFont(__labelFont);
            int stringWidth = g2D.getFontMetrics(__labelFont).stringWidth(label);
            int stringHeight = g2D.getFontMetrics(__labelFont).getHeight();

            int x = 1;
            int y = 1;

            // to make the enclosing rectangle wider than the actual text in it.
            int width = stringHeight + 4;
            int height = stringWidth + 4;

            g2D.setColor(UI.colorBlack2);
            g2D.fillRoundRect(x,
                    y,
                    width, height, 4, 4);

            g2D.setColor(UI.colorGrey1);

            BufferedImage image = Tools.createStringImage(g2D, label);
            g2D.rotate(Math.PI / 2);
            g2D.drawImage(image, null, y + 2, -x - stringHeight);
            g2D.rotate(-Math.PI / 2);

        }

        public Animator getToggleAnimator() {
            return __toggleDrawerAnimator;
        }

        public TargetDrawerToggle getToggleTarget() {
            return __targetDrawerToggle;
        }

        public int getDrawerWidth() {
            return __drawerWidth;
        }

        public void updateDrawerWidth(int width) {
            int i = _drawerHandles.indexOf(this);
            int maxWidth, minWidth;

            if (i <= 0) {
                //leftmost drawer
                maxWidth = _colRowDimensions.x - _drawerContainerHandleWidth - _drawerHandleWidth;
            } else {
                maxWidth = _drawerHandles.get(i - 1).getDrawerWidth() - _drawerHandleWidth;
            }

            if (i >= _drawerHandles.size() - 1) {
                minWidth = 0;
            } else {
                minWidth = _drawerHandles.get(i + 1).getDrawerWidth() + _drawerHandleWidth;
            }

            if (width < minWidth) {
                width = minWidth;
            }
            if (width > maxWidth) {
                width = maxWidth;
            }
//            
//            System.out.println(minWidth + "--" + maxWidth);

//            int delta = width - __drawerWidth;
//            __drawerWidth = width;
//            
//            //need to set the bounds.
//            Rectangle b = getBounds();
//            b.x -= delta;
//            setBounds(b);
            //__contentPane.setBounds(b.x + _drawerHandleWidth, 0, __drawerWidth, this.getParent().getHeight());//Content pane is directly added
            //update width.
            //System.out.println(__contentPane.getBounds());
            int delta = width - __drawerWidth;
            __drawerWidth = width;

            Rectangle bounds = this.getBounds();

            bounds.x = this.getParent().getWidth() - width - _drawerHandleWidth;
            setBounds(bounds);
            //
            //This is the bounds for the handle
            __contentPane.setBounds(bounds.x + _drawerHandleWidth, 0, __drawerWidth, this.getParent().getHeight());

        }

        //This is the bounds from the outside.
        public void updateDrawerBoundsWithContainer(Rectangle b) {
            //Rectangle oldBounds = (Rectangle) b.clone();//This is the bound of the outside container
            int index = _drawerHandles.indexOf(this);
            //System.out.println(b);
            //Need to compensate for the change in container bounds
            if (index <= 0) {
                if (b.width - __drawerWidth - _drawerHandleWidth <= 0) {
                    __drawerWidth = b.width - _drawerHandleWidth;
                }
            } else {
                //must leave enough room for previous one
                if (__drawerWidth >= _drawerHandles.get(index - 1).__drawerWidth - _drawerHandleWidth) {
                    __drawerWidth = _drawerHandles.get(index - 1).__drawerWidth - _drawerHandleWidth;
                }
            }

            Rectangle bound = new Rectangle(b.width - __drawerWidth - _drawerHandleWidth + _drawerContainerHandleWidth, 0, _drawerHandleWidth, b.height);

            setBounds(bound);

//            System.out.println("Parent panel:" + getParent());
//          Sometimes the parent might be null. The order of things are initialized can be quite random...            
            __contentPane.setBounds(
                    bound.x + _drawerHandleWidth, 0,
                    this.getParent().getWidth() - bound.x - _drawerHandleWidth, this.getParent().getHeight());

        }

        @Override
        public void mouseClicked(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me) && me.getClickCount() >= 2) {
                if (__toggleDrawerAnimator.isRunning()) {
                    __toggleDrawerAnimator.stop();
                }

                int index = _drawerHandles.indexOf(this);
                int maxWidth = 0;
                if (index == _drawerHandles.size() - 1) {
                    //last one
                    maxWidth = this.getParent().getWidth();
                } else {
                    maxWidth = _drawerHandles.get(index + 1).getX() - _drawerHandleWidth;
                }

//                System.out.println("Toggle should start");
//                System.out.println(maxWidth + " " + this.getX());
//                System.out.println(this.getX() + " " + maxWidth);
                if (this.getX() >= maxWidth - _drawerHandleWidth - 10) {
                    __toggleDrawerAnimator.startReverse();
                } else {
//                    System.out.println("Should start");
                    __targetDrawerToggle.setLastWidth(__drawerWidth);
                    __toggleDrawerAnimator.start();
                }

            }

        }
        private int _paneWidth;

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me)) {
                __anchorX = me.getXOnScreen();
                __isDragging = true;
                //why not working?
                _paneWidth = __contentPane.getWidth();

            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me)) {

                __isDragging = false;
                //__contentPane.getWidth();
                //System.out.println(_paneWidth + " " + __contentPane.getWidth());
                if (__contentPane.getWidth() != _paneWidth) {
                    __rowMapLinkedToHandle.updateBuffer();
                    __rowMapLinkedToHandle.getViewPanel().repaint();
                    _paneWidth = __contentPane.getWidth();
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
                int delta = me.getXOnScreen() - __anchorX;
                //System.out.println("Boundx:" + getBounds().x);
                updateDrawerWidth(__drawerWidth - delta);
                __anchorX = me.getXOnScreen();
                this.getParent().repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent me) {
        }
    }

    private class CDrawerContainerHandle extends JPanel implements MouseListener, MouseMotionListener {

        private int __anchorX;
        private boolean __isDragging;

        public CDrawerContainerHandle() {
            addMouseListener(this);
            addMouseMotionListener(this);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2D = (Graphics2D) g;

//            g2D.setColor(Color.RED);
//            g2D.fillRect(0, 0, getWidth(), getHeight());
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2D.setColor(UI.colorGrey4);

            g2D.fillRoundRect(3, 4 - 8, this.getWidth() + 4, this.getHeight() + 4, 10, 10);
            g2D.setStroke(UI.stroke1_5);
            g2D.setColor(UI.colorBlack2);
            g2D.drawRoundRect(2, 3 - 8, this.getWidth() + 4, this.getHeight() + 4, 8, 8);

            g2D.drawLine(3, 0, getWidth(), 0);

            g2D.setColor(UI.colorBlack4);
            g2D.drawLine(5, 8 - 4, 5, _drawerContainerHandleHeight - 8);

            g2D.setColor(UI.colorBlack4);
            g2D.drawLine(7, 8 - 4, 7, _drawerContainerHandleHeight - 8);

        }

        @Override
        public void mouseClicked(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me) && me.getClickCount() >= 1) {
                if (_toggleContainerAnimator.isRunning()) {
                    _toggleContainerAnimator.stop();
                }

                if (_colRowDimensions.x < _drawerContainerHandleWidth + 10 + _drawerHandles.size() * _drawerHandleWidth) {
                    _toggleContainerAnimator.startReverse();

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            //throw new UnsupportedOperationException("Not supported yet.");
                            try {
                                Thread.sleep(100);
                                for (CDrawerHandle handle : _drawerHandles) {
                                    if (handle.getToggleAnimator().isRunning()) {
                                        handle.getToggleAnimator().stop();
                                    }
                                    handle.getToggleAnimator().startReverse();
                                    Thread.sleep(50);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } else {
                    _targetContainerToggle.setLastWidth(_colRowDimensions.x);
                    _toggleContainerAnimator.start();
                    for (CDrawerHandle _drawerHandle : _drawerHandles) {
                        _drawerHandle.getToggleTarget().setLastWidth(_drawerHandle.__drawerWidth);
                    }
                }
            }

        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me)) {
                __anchorX = me.getXOnScreen();
                __isDragging = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
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
                int delta = me.getXOnScreen() - __anchorX;

                _updateCointainerWidth(delta);

                __anchorX = me.getXOnScreen();
                this.getParent().repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent me) {
        }
    }

    //////
    private class TargetDrawerCointainerToggle implements TimingTarget {

        private int __lastWidth; //= _defaultWidth;
        private int __from, __to;

        public void setLastWidth(int width) {
            __lastWidth = width;
        }

        @Override
        public void begin(Animator source) {
            __from = __lastWidth;
            __to = 0;
        }

        @Override
        public void end(Animator source) {
        }

        @Override
        public void repeat(Animator source) {
        }

        @Override
        public void reverse(Animator source) {
        }

        @Override
        public void timingEvent(Animator source, double fraction) {
            setDrawerContainerWidth((int) (__from + (__to - __from) * fraction));
        }
    }

    private class TargetDrawerToggle implements TimingTarget {

        private CDrawerHandle __drawerHandle;
        private int __from, __to;
        private int __lastWidth; //= _defaultWidth;

        public TargetDrawerToggle(CDrawerHandle drawerHandle) {
            __drawerHandle = drawerHandle;
        }

        public void setLastWidth(int width) {
            __lastWidth = width;
        }

        @Override
        public void begin(Animator source) {
            this.__from = this.__lastWidth;
            this.__to = 0;
        }

        @Override
        public void end(Animator source) {
        }

        @Override
        public void repeat(Animator source) {
        }

        @Override
        public void reverse(Animator source) {
        }

        @Override
        public void timingEvent(Animator source, double fraction) {
            __drawerHandle.updateDrawerWidth((int) (__from + (__to - __from) * fraction));
            RowDrawer.this.repaint();
        }
    }
}
