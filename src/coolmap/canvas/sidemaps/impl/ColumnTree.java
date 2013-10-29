/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.sidemaps.impl;

import com.google.common.collect.Range;
import coolmap.application.state.StateStorageMaster;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.actions.CollapseColumnNodesUpAction;
import coolmap.canvas.actions.ExpandColumnNodesDownAction;
import coolmap.canvas.misc.MatrixCell;
import coolmap.canvas.sidemaps.ColumnMap;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.state.CoolMapState;
import coolmap.utils.Tools;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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
public class ColumnTree extends ColumnMap implements MouseListener, MouseMotionListener {

    private Color _leafColor;
    private Color _leafBorderColor;
    private int _ballInnerRadius = 2;
    private int _ballOutterRadius = 3;
    private int _highlightRadius = 5;
    private int _baseHeight = 8;
    private int _heightMultiple = 10; //this number X height = height of node
    public final int STRAIGHT = 0;
    public final int ORTHOGONAL = 1;
    public final int CURVE = 2;
    private Font _hoverFont;

    //This records the offset of node: x = parentNode 
    //private final HashSet<Point> _nodeOffset = new HashSet<Point>();
    public ColumnTree(CoolMapObject object) {
        super(object);
        setName("Column Ontology");
        _leafColor = UI.colorGrey3;
        _leafBorderColor = UI.colorBlack5;
        getViewPanel().addMouseListener(this);
        getViewPanel().addMouseMotionListener(this);
        _hoverFont = UI.fontMono.deriveFont(Font.BOLD).deriveFont(11f);
        _initPopupMenu();
    }
    private JPopupMenu _popupMenu;
    private JCheckBoxMenuItem[] _linetypes = new JCheckBoxMenuItem[3];
    private int[] _heightMultiples = new int[]{2, 3, 4, 5, 8, 10, 12, 16, 18, 20, 24, 36, 48, 80, 100};
    private ArrayList<JCheckBoxMenuItem> _heightMultipleItems = new ArrayList<JCheckBoxMenuItem>();

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
        if (isDataViewValid() && !_selectedNodes.isEmpty()) {
            List<VNode> viewColumnNodes = getCoolMapObject().getViewNodesColumn();
            _selectedNodes.retainAll(viewColumnNodes);
            getViewPanel().repaint();
        }
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

//    @Override
//    public void stateStorageUpdated(CoolMapObject object) {
//    }

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
    private JMenuItem _expandOne, _expandToAll, _collapse, _expandOneAll, _collapseOneAll, _colorTree, _colorChild, _clearColor, _selectSubtree;

    private void _selectSubTree() {
        if (_activeNode == null || !_activeNode.isGroupNode() || !_activeNode.isExpanded() || !isDataViewValid()) {
            return;
        }

        List<VNode> childNodeInTree = getCoolMapObject().getViewNodesColumn(_activeNode);
        if (childNodeInTree == null || childNodeInTree.isEmpty()) {
            return;
        }

        //must sort with 



        HashSet<Range<Integer>> selectedColumns = new HashSet<Range<Integer>>();

        VNode firstNode = childNodeInTree.get(0);
        if (firstNode.getViewIndex() == null) {
            return;
        }
        int startIndex = firstNode.getViewIndex().intValue();
        int currentIndex = startIndex;

        for (VNode node : childNodeInTree) {
            if (node.getViewIndex() == null) {
                return;//should not happen
            }
            if (node.getViewIndex().intValue() <= currentIndex + 1) {
                currentIndex = node.getViewIndex().intValue();
                continue;
            } else {
                //add last start and current
                selectedColumns.add(Range.closedOpen(startIndex, currentIndex + 1));
                currentIndex = node.getViewIndex().intValue();
                startIndex = currentIndex;
            }
        }

        selectedColumns.add(Range.closedOpen(startIndex, currentIndex + 1));
        getCoolMapView().setSelectionsColumn(selectedColumns);

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

        _expandOne = new JMenuItem("Expand selected one level");
        _expandOne.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                //Deal with this later
                getCoolMapObject().expandColumnNode(_activeNode);
            }
        });
        _popupMenu.add(_expandOne);

        _expandToAll = new JMenuItem("Expand to leaf");
        _expandToAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                getCoolMapObject().expandColumnNodeToBottom(_activeNode);
            }
        });
        _popupMenu.add(_expandToAll);

        _collapse = new JMenuItem("Collapse selected");
        _collapse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                getCoolMapObject().collapseColumnNode(_activeNode);
            }
        });
        _popupMenu.add(_collapse);
        _popupMenu.addSeparator();
        
        
        _expandOneAll = new JMenuItem(new ExpandColumnNodesDownAction(getCoolMapObject().getID()));
        
        //_expandOneAll = new JMenuItem("Expand all one level"); //, UI.getImageIcon("plusSmall")
        _popupMenu.add(_expandOneAll);
        
//        _expandOneAll.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                
//                getCoolMapObject().expandColumnNodesOneLayer();
//            }
//        });

        _collapseOneAll = new JMenuItem(new CollapseColumnNodesUpAction(getCoolMapObject().getID())); //UI.getImageIcon("minusSmall")
        _popupMenu.add(_collapseOneAll);
        
//        _collapseOneAll.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                getCoolMapObject().collapseColumnNodesOneLayer();
//            }
//        });








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
                        getCoolMapView().updateColumnMapBuffersEnforceAll();
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
                        getCoolMapView().updateColumnMapBuffersEnforceAll();
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
                    //updateBuffer();
                    getCoolMapView().updateColumnMapBuffersEnforceAll();
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
        //heightMultiple.setIcon(UI.getImageIcon("ruler"));
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

    ;
    
//    @Override
//    public void subSelectionRowChanged(CoolMapObject object) {
//    }
//
//    @Override
//    public void subSelectionColumnChanged(CoolMapObject object) {
//    }

    @Override
    protected void prepareRender(Graphics2D g2D) {
    }

    @Override
    public void justifyView() {
        getViewPanel().repaint();
    }

    @Override
    public boolean canRender(CoolMapObject coolMapObject) {
        return true;
    }

    @Override
    public JComponent getConfigUI() {
        //will need a config for tree height multiplier
        return null;
    }

    @Override
    public void activeCellChanged(CoolMapObject obj, MatrixCell oldCell, MatrixCell newCell) {
    }

    @Override
    public void selectionChanged(CoolMapObject obj) {
    }

    @Override
    protected void render(Graphics2D g2D, CoolMapObject object, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int renderWidth, int renderHeight) {

//        VNode firstNode = object.getViewNodeCol(fromCol);
//        computeNodeLocations();
        _renderTreeNodes(g2D, object, fromRow, toRow, fromCol, toCol, zoomX, zoomY, renderWidth, renderHeight);

        super.render(g2D, object, fromRow, toRow, fromCol, toCol, zoomX, zoomY, renderWidth, renderHeight);


    }
//    private final HashMap<VNode, Float> _nodeOffset = new HashMap<VNode, Float>();
//    private void computeNodeLocations(){
//        _nodeOffset.clear();
//        
//        
//        
//        
//    }
//    private void computeNodeViewOffset(VNode node, float zoomX){
//        if(_nodeOffset.containsKey(node)){
//            //already have it
//            return;
//        }
//        
//        if(node.isExpanded()){
//            List<VNode> childNodes = node.getChildNodes();
//            if(childNodes == null || childNodes.isEmpty()){
//                //should throw exception
//                return;
//            }
//            else{
//                VNode firstNode = childNodes.get(0);
//                VNode lastNode = childNodes.get(childNodes.size()-1);
//                if(!_nodeOffset.containsKey(firstNode)){
//                    computeNodeViewOffset(firstNode, zoomX);
//                }
//                if(!_nodeOffset.containsKey(lastNode)){
//                    computeNodeViewOffset(lastNode, zoomX);
//                }
//                Float firstOffset = _nodeOffset.get(firstNode);
//                Float lastOffset = _nodeOffset.get(lastNode);
//            }
//        }
//        else{
//            _nodeOffset.put(node, node.getViewOffset() + node.getViewSize(zoomX)/2);
//        }
//    }
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
            g2D.drawLine(px, py, cx, py);
            g2D.drawLine(cx, py, cx, cy);
        } else if (_drawingType == CURVE) {
            Path2D.Float path = new Path2D.Float();
            path.moveTo(px, py);
            path.curveTo(px, py, cx, py, cx, cy);
            g2D.draw(path);
        } else {
            g2D.drawLine(px, py, cx, cy);
        }
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
        VNode nodeLeft = object.getViewNodeColumn(indexLow);
        VNode nodeRight = object.getViewNodeColumn(indexHigh);
        if (nodeLeft == null || nodeRight == null || nodeLeft.getViewOffset() == null || nodeRight.getViewOffset() == null) {
            return null;
        }
        int leftOffset = (int) (nodeLeft.getViewOffset() + nodeLeft.getViewSizeInMap(object.getCoolMapView().getZoomX()) / 2);
        int rightOffset = (int) (nodeRight.getViewOffset() + nodeRight.getViewSizeInMap(object.getCoolMapView().getZoomX()) / 2);
        return (leftOffset + rightOffset) / 2;
    }

    /**
     *
     *
     *
     */
    private void _renderTreeNodes(Graphics2D g2D, CoolMapObject object, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY, int renderWidth, int renderHeight) {

        List<VNode> treeNodes = object.getViewTreeNodesColumn();
        int anchorX = getCoolMapObject().getViewNodeColumn(fromCol).getViewOffset().intValue();

        Color nodeColor;
        int childX;
        int childY;
        int parentX;
        int parentY;
        Float parentHeight, childHeight;
        Integer parentOffset, childOffset;
        for (VNode treeNode : treeNodes) {
            Float viewIndex = treeNode.getViewIndex();
            boolean parentInView = viewIndex >= fromCol && viewIndex < toCol;
            parentOffset = _getTreeNodeOffset(treeNode, object);


            //System.out.println("Parent offset:" + treeNode + ":" + parentOffset);
            if (parentOffset == null) {
                continue;
            }

            parentX = parentOffset - anchorX;
            parentHeight = treeNode.getViewHeightInTree();
            if (parentHeight == null) {
                continue;
            }
            parentY = (int) Math.round((renderHeight - _baseHeight - parentHeight * _heightMultiple));

            List<VNode> childNodes = treeNode.getChildNodes();
            for (VNode child : childNodes) {
                if (parentInView || (child != null && child.getViewIndex() >= fromCol && child.getViewIndex() < toCol && child.getViewHeightInTree() != null)) {
                    //renderline
                    if (!child.isExpanded()) {
                        childX = (int) (child.getViewOffset() + child.getViewSizeInMap(zoomX) / 2 - anchorX);
                    } else {
                        childOffset = _getTreeNodeOffset(child, object);
                        if (childOffset == null) {
                            continue;
                        }
                        childX = childOffset - anchorX;
                    }

                    childHeight = child.getViewHeightInTree();
                    if (childHeight == null) {
                        continue;
                    }

//                    System.out.println("Child:" + cx + " " + cy);
                    if (childHeight == 0) {
                        childY = renderHeight - _baseHeight + 2;
                    } else {
                        childY = (int) Math.round((renderHeight - _baseHeight - childHeight * _heightMultiple));
                    }



                    _renderLine(g2D, parentX - 1, parentY, childX - 1, childY, zoomX);
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







            if (zoomX > 6) {
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

    /**
     * render column is not useful now.
     *
     * @param g2D
     * @param object
     * @param node
     * @param anchorX
     * @param anchorY
     * @param cellWidth
     * @param cellHeight
     */
    @Override
    protected void renderColumn(Graphics2D g2D, CoolMapObject object, VNode node, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //Need to override render, because column is not enough
        Color nodeColor;

        if (node.getViewColor() != null) {
            nodeColor = node.getViewColor();
        } else if (node.isGroupNode()) {
            nodeColor = node.getCOntology().getViewColor();
        } else {
            nodeColor = _leafColor;
        }

        if (cellWidth > 6) {
            //draw fixed ball.
            g2D.setColor(_leafBorderColor);
            g2D.fillOval(anchorX + cellWidth / 2 - _ballOutterRadius, anchorY + cellHeight - _baseHeight, _ballOutterRadius * 2, _ballOutterRadius * 2);
            g2D.setColor(nodeColor);
            g2D.fillOval(anchorX + cellWidth / 2 - _ballInnerRadius, anchorY + cellHeight - _baseHeight + 1, _ballInnerRadius * 2, _ballInnerRadius * 2);

        } else {
            g2D.setColor(nodeColor);
            g2D.fillRect(anchorX, anchorY + cellHeight - _baseHeight + 1, cellWidth, _baseHeight);
        }

        //_nodeLocation.put(node, new Point(anchorX + cellWidth / 2, anchorY + cellHeight - _baseHeight + 1));
    }

    @Override
    protected void prePaint(Graphics2D g2D, CoolMapObject object, int width, int height) {

        if (_activeNodePoint != null) {
            g2D.setColor(UI.colorLightGreen0);
            //g2D.fillOval(_activeNodePoint.x + getCoolMapView().getMapAnchor().x - _highlightRadius, _activeNodePoint.y + height - _highlightRadius, _highlightRadius*2 , _highlightRadius*2);
            g2D.fillRect(_activeNodePoint.x + getCoolMapView().getMapAnchor().x - _highlightRadius,
                    0,
                    _highlightRadius * 2,
                    height);
        }
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

                int x = object.getCoolMapView().getMapAnchor().x + p.x;
                int y = height + p.y;

                String label = node.getViewLabel();
                if (label == null) {
                    label = "";
                }
                int labelWidth = g2D.getFontMetrics().stringWidth(label);
                int labelHeight = g2D.getFontMetrics().getHeight();

                g2D.setColor(UI.colorBlack5);
                g2D.fillRoundRect(x - labelWidth - 10 + 1, y - 4 - labelHeight + 1, labelWidth + 6, labelHeight + 4, 4, 5);

                g2D.setColor(UI.colorWhite);
                g2D.fillRoundRect(x - labelWidth - 10, y - 4 - labelHeight, labelWidth + 6, labelHeight + 4, 4, 5);

                g2D.setColor(UI.colorBlack2);
                g2D.drawString(label, x - labelWidth - 7, y - 5);

            }
        }

        if (_activeNodePoint != null && _activeNode != null && _plotHover) {
            g2D.setFont(_hoverFont);
            g2D.setColor(UI.colorBlack3);

            int x = _activeNodePoint.x + getCoolMapView().getMapAnchor().x;
            int y = height + _activeNodePoint.y;

            String label = _activeNode.getViewLabel();
            if (label == null) {
                label = "";
            }
            int labelWidth = g2D.getFontMetrics().stringWidth(label);
            int labelHeight = g2D.getFontMetrics().getHeight();

            g2D.setColor(UI.colorBlack5);
            g2D.fillRoundRect(x - labelWidth - 10 + 1, y - 4 - labelHeight + 1, labelWidth + 6, labelHeight + 4, 4, 5);

            g2D.setColor(UI.colorLightYellow);
            g2D.fillRoundRect(x - labelWidth - 10, y - 4 - labelHeight, labelWidth + 6, labelHeight + 4, 4, 5);

            g2D.setColor(UI.colorBlack2);
            g2D.drawString(label, x - labelWidth - 7, y - 5);


            //g2D.drawString(_activeNode.getViewLabel(), _activeNodePoint.x + getCoolMapView().getMapAnchor().x, height + _activeNodePoint.y);
        }


        if (_isSelecting && _selectionStartPoint != null && _selectionEndPoint != null && _screenRegion != null) {


            g2D.setColor(UI.colorRedWarning);
            g2D.setStroke(UI.strokeDash1_5);
            g2D.drawRect(_screenRegion.x, _screenRegion.y, _screenRegion.width, _screenRegion.height);

        }

    }
    private final LinkedHashSet<VNode> _selectedNodes = new LinkedHashSet<VNode>();
//    private Color _labelBackgroundColor = UI.mixOpacity(UI.colorLightYellow, 0.7f);

    @Override
    public void mouseClicked(MouseEvent me) {
        //VNode node = _getActiveNode(me.getX(), me.getY());
        //System.out.println(node);
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
                        
                        String operationName = "";
                        if(node.isExpanded()){
                            operationName = "Collapse column '" + node.getViewLabel() + "'";
                        }
                        else{
                            operationName = "Expand column '" + node.getViewLabel() + "' to bottom"; 
                        }
                        
                        CoolMapState state = CoolMapState.createStateColumns(operationName, getCoolMapObject(), null);
                        
                        
                        boolean success = getCoolMapObject().toggleColumnNode(node);
                        
                        if(success){
                            StateStorageMaster.addState(state);
                        }
                        
                    }
                }

                //single click
                getViewPanel().repaint();
                mouseMoved(me);
            }
        }
    }
    
    private Point _selectionStartPoint;
    private Point _selectionEndPoint;
    private boolean _isSelecting = false;
    private Rectangle _screenRegion;

    @Override
    public void mousePressed(MouseEvent me) {
        if (SwingUtilities.isLeftMouseButton(me)) {
            _selectionStartPoint = new Point(me.getX(), me.getY());
            _isSelecting = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        if (SwingUtilities.isLeftMouseButton(me)) {
            _isSelecting = false;
            //select nodes in region
            _selectNodesInRegion(_screenRegion);

            _selectionStartPoint = null;
            _selectionEndPoint = null;

            getViewPanel().repaint();
        }
    }

    private void _selectNodesInRegion(Rectangle screenRegion) {
        //of course you can use binary search here..
        
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        if (SwingUtilities.isLeftMouseButton(me) && _isSelecting) {
            _selectionEndPoint = new Point(me.getX(), me.getY());
            ///////////////////////// xyss
            int x, y, swidth, sheight;

            ///////////////////////// xyss
            if (_selectionStartPoint.x < _selectionEndPoint.x) {
                x = _selectionStartPoint.x;
                swidth = _selectionEndPoint.x - _selectionStartPoint.x + 1;
            } else {
                x = _selectionEndPoint.x;
                swidth = _selectionStartPoint.x - _selectionEndPoint.x;
            }

            ///////////////////////// xyss
            if (_selectionStartPoint.y < _selectionEndPoint.y) {
                y = _selectionStartPoint.y;
                sheight = _selectionEndPoint.y - _selectionStartPoint.y + 1;
            } else {
                y = _selectionEndPoint.y;
                sheight = _selectionStartPoint.y - _selectionEndPoint.y;
            }
            _screenRegion = new Rectangle(x,y,swidth,sheight);
            getViewPanel().repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        _plotHover = true;
        getViewPanel().repaint();
        mouseMoved(me);
    }
    private boolean _plotHover = false;

    @Override
    public void mouseExited(MouseEvent me) {
        _activeNode = null;
        _activeNodePoint = null;
        _plotHover = false;
        getViewPanel().repaint();
    }
    private Point _activeNodePoint = null;
    private VNode _activeNode = null;

    @Override
    public void mouseMoved(MouseEvent me) {
        if (isDataViewValid() && !_isSelecting) {

            getCoolMapView().setMouseXY(me.getX(), me.getY());
            VNode node = _getActiveNode(me.getX(), me.getY());
            if (node != null) {
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

    private VNode _getActiveNode(int screenX, int screenY) {
        CoolMapView view = getCoolMapView();
        JComponent panel = getViewPanel();
        CoolMapObject object = getCoolMapObject();
        int renderHeight = panel.getHeight();

        if (screenY > panel.getHeight() - _baseHeight) {
            //search in baseNodes
            Integer col = view.getCurrentCol(screenX);
            if (col != null) {
                return getCoolMapObject().getViewNodeColumn(col);
            } else {
                return null;
            }
        } else {
            List<VNode> treeNodes = object.getViewTreeNodesColumn();
            for (VNode node : treeNodes) {
                if (node == null || node.getViewHeightInTree() == null) {
                    continue;
                }

                Float index = node.getViewIndex();
                if (index < view.getMinColInView() || index >= view.getMaxColInView()) {
                    continue; //not in view
                } else {
                    Integer offset = _getTreeNodeOffset(node, object);
                    if (offset == null) {
                        continue;
                    }
                    int nodeX = offset + view.getMapAnchor().x;
                    if (Math.abs(nodeX - screenX) > _ballOutterRadius) {
                        continue;
                    }


                    int nodeY = (int) Math.round((renderHeight - _baseHeight - node.getViewHeightInTree() * _heightMultiple));
                    if (Math.abs(nodeY - screenY) > _ballOutterRadius) {
                        continue;
                    }

                    return node;
                }
            }//end of for all tree node
            return null;
        }
    }

    private Point _getNodePositionInView(VNode node) {
        if (node == null || node.getViewHeightInTree() == null) {
            return null;
        }

        CoolMapObject object = getCoolMapObject();
        CoolMapView view = getCoolMapView();
        int renderHeight = getViewPanel().getHeight();

        Point point = new Point();
        if (node.isExpanded()) {
            //tree node
            Integer offset = _getTreeNodeOffset(node, getCoolMapObject());
            if (offset == null) {
                return null;
            }
            point.x = offset;
            //only left the offset part
            point.y = (int) Math.round((-_baseHeight - node.getViewHeightInTree() * _heightMultiple));
            return point;
        } else {
            //base node
            if (node.getViewOffset() == null) {
                return null;
            }

            point.x = (int) Math.round((node.getViewOffset() + node.getViewSizeInMap(view.getZoomX()) / 2));
            point.y = -_baseHeight + 1;
            return point;
        }
    }
}
