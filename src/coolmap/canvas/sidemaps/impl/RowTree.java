/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.sidemaps.impl;

import com.google.common.collect.Range;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.misc.MatrixCell;
import coolmap.canvas.sidemaps.RowMap;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.Tools;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 *
 * @author gangsu
 */
public class RowTree extends RowMap implements MouseListener, MouseMotionListener {

    private Color _leafColor;
    private Color _leafBorderColor;
    private int _ballInnerRadius = 2;
    private int _ballOutterRadius = 3;
    private int _highlightRadius = 5;
    private int _baseWidth = 8;
    private int _heightMultiple = 10; //this number X height = height of node
    public final int STRAIGHT = 0;
    public final int ORTHOGONAL = 1;
    public final int CURVE = 2;
    private Font _hoverFont;
    private JPopupMenu _popupMenu;
    private JCheckBoxMenuItem[] _linetypes = new JCheckBoxMenuItem[3];
    private int[] _heightMultiples = new int[]{2, 3, 4, 5, 8, 10, 12, 16, 18, 20, 24, 36, 48, 80, 100};
    private ArrayList<JCheckBoxMenuItem> _heightMultipleItems = new ArrayList<JCheckBoxMenuItem>();
    private JMenuItem _expandOne, _expandToAll, _collapse, _expandOneAll, _collapseOneAll, _colorTree, _colorChild, _clearColor, _selectSubtree;

    ;

    private void _selectSubTree() {
        if (_activeNode == null || !_activeNode.isGroupNode() || !_activeNode.isExpanded() || !isDataViewValid()) {
            return;
        }

        List<VNode> childNodeInTree = getCoolMapObject().getViewNodesRow(_activeNode);
        if (childNodeInTree == null || childNodeInTree.isEmpty()) {
            return;
        }

        HashSet<Range<Integer>> selectedRows = new HashSet<Range<Integer>>();

        VNode firstNode = childNodeInTree.get(0);
        if (firstNode.getViewIndex() == null) {
            return;
        }
        int startIndex = firstNode.getViewIndex().intValue();
        int currentIndex = startIndex;

        for (VNode node : childNodeInTree) {
            //System.out.println(node.getViewIndex());
            if (node.getViewIndex() == null) {
                return;//should not happen
            }
            if (node.getViewIndex().intValue() <= currentIndex + 1) {
                currentIndex = node.getViewIndex().intValue();
                continue;
            } else {
                //add last start and current
                selectedRows.add(Range.closedOpen(startIndex, currentIndex + 1));
                currentIndex = node.getViewIndex().intValue();
                startIndex = currentIndex;
            }
        }

        selectedRows.add(Range.closedOpen(startIndex, currentIndex + 1));

        for (Range range : selectedRows) {
            System.out.println(range);
        }


        getCoolMapView().setSelectionsRow(selectedRows);

    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    public RowTree(CoolMapObject object) {
        super(object);
        setName("Row Ontology");
        _leafColor = UI.colorGrey3;
        _leafBorderColor = UI.colorBlack5;
        getViewPanel().addMouseListener(this);
        getViewPanel().addMouseMotionListener(this);
        _hoverFont = UI.fontMono.deriveFont(Font.BOLD).deriveFont(11f);
        _initPopupMenu();
    }

    private void _initPopupMenu() {
        _popupMenu = new JPopupMenu();
        getViewPanel().setComponentPopupMenu(_popupMenu);

        _selectSubtree = new JMenuItem("Select subtree");
        _popupMenu.add(_selectSubtree);
        _selectSubtree.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                _selectSubTree();
            }
        });



        JMenu linetype = new JMenu("Line type");

        _expandOne = new JMenuItem("Expand one level");
        _expandOne.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                getCoolMapObject().expandRowNode(_activeNode);
            }
        });
        _popupMenu.add(_expandOne);

        _expandToAll = new JMenuItem("Expand to leaf");
        _expandToAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                getCoolMapObject().expandRowNodeToBottom(_activeNode);
            }
        });
        _popupMenu.add(_expandToAll);

        _collapse = new JMenuItem("Collapse");
        _collapse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                getCoolMapObject().collapseRowNode(_activeNode);
            }
        });
        _popupMenu.add(_collapse);
        _popupMenu.addSeparator();
        _expandOneAll = new JMenuItem("Expand all one level");
        _popupMenu.add(_expandOneAll);
        _expandOneAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                getCoolMapObject().expandRowNodesOneLayer();
            }
        });
        
        _collapseOneAll = new JMenuItem("Collapse all one level");
        _popupMenu.add(_collapseOneAll);
        _collapseOneAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getCoolMapObject().collapseRowNodesOneLayer();
            }
        });
        
        


        _colorTree = new JMenuItem("Color subtree");
        _popupMenu.addSeparator();
        _popupMenu.add(_colorTree);
        _colorTree.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                JColorChooser chooser = Tools.getColorChooser();
                Color color = chooser.showDialog(getViewPanel().getTopLevelAncestor(), "Chooser brush color", _leafColor);
                if (color == null) {
                    return;
                } else {
                    if (_activeNode != null) {
                        _activeNode.colorTree(color);
                        getCoolMapView().updateRowMapBuffersEnforceAll();
                    }
                }
            }
        });

        _colorChild = new JMenuItem("Color child");
        _popupMenu.add(_colorChild);
        _colorChild.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                JColorChooser chooser = Tools.getColorChooser();
                Color color = chooser.showDialog(getViewPanel().getTopLevelAncestor(), "Chooser brush color", _leafColor);
                if (color == null) {
                    return;
                } else {
                    if (_activeNode != null) {
                        _activeNode.colorChild(color);
                        getCoolMapView().updateRowMapBuffersEnforceAll();
                    }
                }
            }
        });

        _clearColor = new JMenuItem("Clear color");
        _popupMenu.add(_clearColor);
        _clearColor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                if (_activeNode != null) {
                    _activeNode.colorTree(null);
                    updateBuffer();
                }

            }
        });

        //line types
        _popupMenu.addSeparator();
        _popupMenu.add(linetype);
        LinetypeChangedListener listener = new LinetypeChangedListener();

        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Straight");
        linetype.add(item);
        _linetypes[0] = item;
        item.addActionListener(listener);

        item = new JCheckBoxMenuItem("Orthogonal");
        linetype.add(item);
        _linetypes[1] = item;
        item.addActionListener(listener);

        item = new JCheckBoxMenuItem("Curve");
        linetype.add(item);
        _linetypes[2] = item;
        item.addActionListener(listener);
        item.setSelected(true);
        /////////////////////////////////

        HeightMultipleListener hListener = new HeightMultipleListener();
        JMenu heightMultiple = new JMenu("Unit height");
        _popupMenu.add(heightMultiple);
        for (int i : _heightMultiples) {
            item = new JCheckBoxMenuItem(Integer.toString(i));
            heightMultiple.add(item);
            _heightMultipleItems.add(item);
            item.addActionListener(hListener);
            if (i == 10) {
                item.setSelected(true);
            }
        }


        ////
        _popupMenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
                _expandOne.setEnabled(false);
                _expandToAll.setEnabled(false);
                _collapse.setEnabled(false);
                _colorTree.setEnabled(false);
                _colorChild.setEnabled(false);
                _clearColor.setEnabled(false);
                _selectSubtree.setEnabled(false);

                if (_activeNode != null) {
                    _colorTree.setEnabled(true);
                    _colorChild.setEnabled(true);
                    _clearColor.setEnabled(true);
                }

                if (_activeNode != null && _activeNode.isExpanded() && _activeNode.isGroupNode()) {
                    _collapse.setEnabled(true);
                    _selectSubtree.setEnabled(true);
                }
                if (_activeNode != null && !_activeNode.isExpanded() && _activeNode.isGroupNode()) {
                    _expandOne.setEnabled(true);
                    _expandToAll.setEnabled(true);
                }
//                getViewPanel().removeMouseListener(ColumnTree.this);
//                getViewPanel().removeMouseMotionListener(ColumnTree.this);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
//                getViewPanel().addMouseListener(ColumnTree.this);
//                getViewPanel().addMouseMotionListener(ColumnTree.this);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent pme) {
//                getViewPanel().addMouseListener(ColumnTree.this);
//                getViewPanel().addMouseMotionListener(ColumnTree.this);
            }
        });

    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
        if (isDataViewValid() && !_selectedNodes.isEmpty()) {
            List<VNode> viewRowNodes = getCoolMapObject().getViewNodesRow();
            _selectedNodes.retainAll(viewRowNodes);
            getViewPanel().repaint();
        }
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
    }

    @Override
    public void baseMatrixChanged(CoolMapObject object) {
    }

    @Override
    public void mapAnchorMoved(CoolMapObject object) {
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
    }

    @Override
    public void gridChanged(CoolMapObject object) {
    }

    private class LinetypeChangedListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            for (JCheckBoxMenuItem item : _linetypes) {
                item.setSelected(false);
            }
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) ae.getSource();
            item.setSelected(true);

            String label = item.getText();
            if (label.equals("Straight")) {
                _drawingType = STRAIGHT;
            } else if (label.equals("Orthogonal")) {
                _drawingType = ORTHOGONAL;
            } else if (label.equals("Curve")) {
                _drawingType = CURVE;
            } else {
                _drawingType = STRAIGHT;
            }
            updateBuffer();
        }
    }

    private class HeightMultipleListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            for (JCheckBoxMenuItem item : _heightMultipleItems) {
                item.setSelected(false);
            }
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) ae.getSource();
            item.setSelected(true);

            int multiple = Integer.parseInt(item.getText());
            _heightMultiple = multiple;
            updateBuffer();
        }
    }

    @Override
    public JComponent getConfigUI() {
        return null;
    }

    @Override
    protected void prePaint(Graphics2D g2D, CoolMapObject object, int width, int height) {
        if (_activeNodePoint != null) {
            g2D.setColor(UI.colorLightGreen0);
            //g2D.fillOval(_activeNodePoint.x + getCoolMapView().getMapAnchor().x - _highlightRadius, _activeNodePoint.y + height - _highlightRadius, _highlightRadius*2 , _highlightRadius*2);
            g2D.fillRect(0, _activeNodePoint.y + getCoolMapView().getMapAnchor().y - getAnchorY() - _highlightRadius, width,
                    _highlightRadius * 2);
        }
    }

    @Override
    protected void prepareRender(Graphics2D g2D) {
    }

    @Override
    protected void postPaint(Graphics2D g2D, CoolMapObject object, int width, int height) {
        if (!_selectedNodes.isEmpty()) {
            g2D.setFont(_hoverFont);
            g2D.setColor(UI.colorBlack3);
            for (VNode node : _selectedNodes) {
                Point p = _getNodePositionInView(node);
                if (p == null) {
                    continue;
                }

                int x = p.x;
                int y = p.y + getCoolMapView().getMapAnchor().y - getAnchorY();

                String label = node.getViewLabel();
                if (label == null) {
                    label = "";
                }
                int labelWidth = g2D.getFontMetrics().stringWidth(label);
                int labelHeight = g2D.getFontMetrics().getHeight();

                g2D.setColor(UI.colorBlack5);
                g2D.fillRoundRect(x + 2, y - 4 - labelHeight + 1, labelWidth + 6, labelHeight + 4, 4, 5);

                g2D.setColor(UI.colorWhite);
                g2D.fillRoundRect(x + 2, y - 4 - labelHeight, labelWidth + 6, labelHeight + 4, 4, 5);

                g2D.setColor(UI.colorBlack2);
                g2D.drawString(label, x + 4, y - 5);

            }
        }

        if (_activeNodePoint != null && _activeNode != null && _plotHover) {
            g2D.setFont(_hoverFont);
            g2D.setColor(UI.colorBlack3);
            int x = _activeNodePoint.x;
            int y = _activeNodePoint.y + getCoolMapView().getMapAnchor().y - getAnchorY();

            String label = _activeNode.getViewLabel();
            if (label == null) {
                label = "";
            }
            int labelWidth = g2D.getFontMetrics().stringWidth(label);
            int labelHeight = g2D.getFontMetrics().getHeight();

            g2D.setColor(UI.colorBlack5);
            g2D.fillRoundRect(x + 2, y - 4 - labelHeight + 1, labelWidth + 6, labelHeight + 4, 4, 5);

            g2D.setColor(UI.colorLightYellow);
            g2D.fillRoundRect(x + 2, y - 4 - labelHeight, labelWidth + 6, labelHeight + 4, 4, 5);

            g2D.setColor(UI.colorBlack2);
            g2D.drawString(label, x + 4, y - 5);
        }
    }
    private final LinkedHashSet<VNode> _selectedNodes = new LinkedHashSet<VNode>();

    @Override
    public boolean canRender(CoolMapObject coolMapObject) {
        return true;
    }

    @Override
    public void justifyView() {
        getViewPanel().repaint();
    }

    @Override
    protected void renderRow(Graphics2D g2D, CoolMapObject object, VNode node, int anchorX, int anchorY, int cellWidth, int cellHeight) {

        Color nodeColor;

        if (node.getViewColor() != null) {
            nodeColor = node.getViewColor();
        } else if (node.isGroupNode()) {
            nodeColor = node.getCOntology().getViewColor();
        } else {
            nodeColor = _leafColor;
        }

        if (cellHeight > 6) {
            //draw fixed ball.
            g2D.setColor(_leafBorderColor);
            //g2D.fillOval(anchorX + cellWidth / 2 - _ballOutterRadius, anchorY + cellHeight - _baseHeight, f, _ballOutterRadius * 2);
            g2D.fillOval(anchorX + _baseWidth - _ballOutterRadius * 2, anchorY + cellHeight / 2 - _ballOutterRadius, _ballOutterRadius * 2, _ballOutterRadius * 2);
            g2D.setColor(nodeColor);
            //g2D.fillOval(anchorX + cellWidth / 2 - _ballInnerRadius, anchorY + cellHeight - _baseHeight + 1, _ballInnerRadius * 2, _ballInnerRadius * 2);
            g2D.fillOval(anchorX + _baseWidth - 1 - _ballInnerRadius * 2, anchorY + cellHeight / 2 - _ballOutterRadius + 1, _ballInnerRadius * 2, _ballInnerRadius * 2);
        } else {

            g2D.setColor(nodeColor);
            g2D.fillRect(anchorX, anchorY, _baseWidth, cellHeight);
        }
    }

    @Override
    protected void render(Graphics2D g2D, CoolMapObject object, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int renderWidth, int renderHeight) {
        _renderTreeNodes(g2D, object, fromRow, toRow, fromCol, toCol, zoomX, zoomY, renderWidth, renderHeight);
        super.render(g2D, object, fromRow, toRow, fromCol, toCol, zoomX, zoomY, renderWidth, renderHeight);
    }

    private Integer _getTreeNodeOffset(VNode treeNode, CoolMapObject object) {
        if (!treeNode.isExpanded()) {
            return null;
        }
        List<VNode> childNodes = treeNode.getChildNodes();
        if (childNodes.isEmpty()) {
            return null;
        }
        Float viewIndex = treeNode.getViewIndex();
        int indexLow = (int) Math.floor(viewIndex);
        int indexHigh = (int) Math.ceil(viewIndex);
        VNode nodeLeft = object.getViewNodeRow(indexLow);
        VNode nodeRight = object.getViewNodeRow(indexHigh);
        if (nodeLeft == null || nodeRight == null || nodeLeft.getViewOffset() == null || nodeRight.getViewOffset() == null) {
            return null;
        }
        int leftOffset = (int) (nodeLeft.getViewOffset() + nodeLeft.getViewSizeInMap(object.getCoolMapView().getZoomY()) / 2);
        int rightOffset = (int) (nodeRight.getViewOffset() + nodeRight.getViewSizeInMap(object.getCoolMapView().getZoomY()) / 2);
        return (leftOffset + rightOffset) / 2;
    }

    @Override
    public void subSelectionRowChanged(CoolMapObject object) {
    }

    @Override
    public void subSelectionColumnChanged(CoolMapObject object) {
    }

    private void _renderTreeNodes(Graphics2D g2D, CoolMapObject object, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int renderWidth, int renderHeight) {
        List<VNode> treeNodes = object.getViewTreeNodesRow();

        //Attn: minor bug may exist here. Null pointer exception?

        int anchorY = getCoolMapObject().getViewNodeRow(fromRow).getViewOffset().intValue();


        Color nodeColor;
        int childX;
        int childY;
        int parentX;
        int parentY;
        Float parentHeight, childHeight; //towards right ->
        Integer parentOffset, childOffset;

        for (VNode treeNode : treeNodes) {
            Float viewIndex = treeNode.getViewIndex();
            boolean parentInView = viewIndex >= fromRow && viewIndex < toRow;
            parentOffset = _getTreeNodeOffset(treeNode, object);

            if (parentOffset == null) {
                continue;
            }

            parentY = parentOffset - anchorY;
            parentHeight = treeNode.getViewHeightInTree();
            if (parentHeight == null) {
                continue;
            }
            parentX = (int) Math.round(_baseWidth + parentHeight * _heightMultiple);
            List<VNode> childNodes = treeNode.getChildNodes();

            for (VNode child : childNodes) {
                if (parentInView || (child != null && child.getViewIndex() >= fromRow && child.getViewIndex() < toRow && child.getViewHeightInTree() != null)) {

                    if (!child.isExpanded()) {
                        childY = (int) (child.getViewOffset() + child.getViewSizeInMap(zoomY) / 2 - anchorY);
                    } else {
                        childOffset = _getTreeNodeOffset(child, object);
                        if (childOffset == null) {
                            continue;
                        }
                        childY = childOffset - anchorY;
                    }

                    childHeight = child.getViewHeightInTree();
                    if (childHeight == null) {
                        continue;
                    }

                    if (childHeight == 0) {
                        childX = _baseWidth - 2;
                    } else {
                        childX = (int) Math.round(_baseWidth + childHeight * _heightMultiple);
                    }

//                    System.out.println(parentX + " " + parentY + " " + childX + " " + childY);
                    _renderLine(g2D, parentX, parentY, childX, childY, zoomY);


                }
            }

            if (!parentInView) {
                continue;
            }

            if (treeNode.getViewColor() != null) {
                nodeColor = treeNode.getViewColor();
            } else {
                nodeColor = treeNode.getCOntology().getViewColor();
            }

            if (zoomY > 6) {
                //draw fixed ball.
                g2D.setColor(_leafBorderColor);
                g2D.fillOval(parentX - _ballOutterRadius, parentY - _ballOutterRadius, _ballOutterRadius * 2, _ballOutterRadius * 2);
                g2D.setColor(nodeColor);
                g2D.fillOval(parentX - _ballInnerRadius, parentY - _ballInnerRadius, _ballInnerRadius * 2, _ballInnerRadius * 2);

            } else {
                g2D.setColor(nodeColor);
                g2D.fillRect(parentX - _ballInnerRadius, parentY - _ballInnerRadius, _ballInnerRadius * 2, _ballInnerRadius * 2);
            }



        }



    }

    @Override
    public void stateStorageUpdated(CoolMapObject object) {
    }

    private void _renderLine(Graphics2D g2D, int px, int py, int cx, int cy, float zoomX) {
        g2D.setColor(UI.colorGrey5);

        if (zoomX < 6) {
            g2D.setStroke(UI.stroke1);
        } else {
            g2D.setStroke(UI.stroke1_5);
        }

        if (_drawingType == STRAIGHT) {
            g2D.drawLine(px, py, cx, cy);
        } else if (_drawingType == ORTHOGONAL) {
            g2D.drawLine(px, py, px, cy);
            g2D.drawLine(px, cy, cx, cy);
        } else if (_drawingType == CURVE) {
            Path2D.Float path = new Path2D.Float();
            path.moveTo(px, py);
            path.curveTo(px, py, px, cy, cx, cy);
            g2D.draw(path);
        } else {
            g2D.drawLine(px, py, cx, cy);

        }
    }
    private int _drawingType = CURVE;

    public void setType(int type) {
        if (type == STRAIGHT) {
            _drawingType = STRAIGHT;
        } else if (type == ORTHOGONAL) {
            _drawingType = ORTHOGONAL;
        } else if (type == CURVE) {
            _drawingType = CURVE;
        } else {
            _drawingType = STRAIGHT;
        }
    }

    @Override
    public void activeCellChanged(CoolMapObject obj, MatrixCell oldCell, MatrixCell newCell) {
    }

    @Override
    public void selectionChanged(CoolMapObject obj) {
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (SwingUtilities.isLeftMouseButton(me)) {
            if (isDataViewValid()) {
                VNode node = _getActiveNode(me.getX(), me.getY());
                if (node == null) {
                    _selectedNodes.clear();

                } else {
                    if (_selectedNodes.contains(node)) {
                        _selectedNodes.remove(node);
                    } else {
                        _selectedNodes.add(node);
                    }
                    if (me.getClickCount() > 1) {
                        //getCoolMapObject().toggleColumnNode(node);
                        getCoolMapObject().toggleRowNode(node);
                    }
                }

                //single click
                getViewPanel().repaint();
                mouseMoved(me);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        _plotHover = true;
    }
    private boolean _plotHover = false;

    @Override
    public void mouseExited(MouseEvent me) {
//        _activeNode = null;
//        _activeNodePoint = null;
        _plotHover = false;
        getViewPanel().repaint();
    }

    @Override
    public void mouseDragged(MouseEvent me) {
    }
    private Point _activeNodePoint = null;
    private VNode _activeNode = null;

    @Override
    public void mouseMoved(MouseEvent me) {
        if (isDataViewValid()) {
            Point point = translateToCanvas(me.getX(), me.getY());
            getCoolMapView().setMouseXY(point.x, point.y);

            VNode node = _getActiveNode(me.getX(), me.getY());
            if (node != null) {
                //System.out.println(node);
                _activeNodePoint = _getNodePositionInView(node);
                _activeNode = node;
                getViewPanel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                _activeNodePoint = null;
                _activeNode = null;
                getViewPanel().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            getViewPanel().repaint();
        }
    }

    private Point _getNodePositionInView(VNode node) {
        if (node == null || node.getViewHeightInTree() == null) {
            return null;
        }

        CoolMapObject object = getCoolMapObject();
        CoolMapView view = getCoolMapView();
        int renderWidth = getViewPanel().getWidth();

        Point point = new Point();
        if (node.isExpanded()) {
            //tree node
            Integer offset = _getTreeNodeOffset(node, getCoolMapObject());
            if (offset == null) {
                return null;
            }
            point.y = offset;
            //only left the offset part
            point.x = (int) (_baseWidth + node.getViewHeightInTree() * _heightMultiple);
            return point;
        } else {
            //base node
            if (node.getViewOffset() == null) {
                return null;
            }

            point.y = (int) (node.getViewOffset() + node.getViewSizeInMap(view.getZoomY()) / 2);
            point.x = _baseWidth - 1;
            return point;
        }
    }

    private VNode _getActiveNode(int screenX, int screenY) {
        CoolMapView view = getCoolMapView();
        JComponent panel = getViewPanel();
        CoolMapObject object = getCoolMapObject();
        int renderWidth = panel.getWidth();

        if (screenX < _baseWidth) {
            //search in baseNodes
            //System.out.println(screenY);
            Integer row = view.getCurrentRow(screenY + getAnchorY());
            //System.out.println(row);
            if (row != null) {
                return getCoolMapObject().getViewNodeRow(row);
            } else {
                return null;
            }
        } else {
            List<VNode> treeNodes = object.getViewTreeNodesRow();
            for (VNode node : treeNodes) {
                if (node == null || node.getViewHeightInTree() == null) {
                    continue;
                }

                Float index = node.getViewIndex();
                if (index < view.getMinRowInView() || index >= view.getMaxRowInView()) {
                    continue; //not in view
                } else {
                    Integer offset = _getTreeNodeOffset(node, object);
                    if (offset == null) {
                        continue;
                    }
                    int nodeY = offset + view.getMapAnchor().y - getAnchorY();
                    if (Math.abs(nodeY - screenY) > _ballOutterRadius) {
                        continue;
                    }


                    int nodeX = (int) Math.round((_baseWidth + node.getViewHeightInTree() * _heightMultiple));
                    if (Math.abs(nodeX - screenX) > _ballOutterRadius) {
                        continue;
                    }

                    return node;
                }
            }//end of for all tree node
            return null;
        }
    }
}
