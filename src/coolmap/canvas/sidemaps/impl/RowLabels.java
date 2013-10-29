/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.sidemaps.impl;

import com.google.common.collect.Range;
import coolmap.application.state.StateStorageMaster;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.misc.MatrixCell;
import coolmap.canvas.misc.ZoomControl;
import coolmap.canvas.sidemaps.RowMap;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.state.CoolMapState;
import coolmap.utils.graphics.CAnimator;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;

/**
 *
 * @author gangsu
 */
public class RowLabels extends RowMap<Object, Object> implements MouseListener, MouseMotionListener {

    private ZoomControl _zoomControlY;
    private int _marginSize = 16;
    private int _maxDescent = 0;
    private int _fontSize = 0;
    private final JPopupMenu _menu = new JPopupMenu();
    private final JMenuItem _sortAscending;
    private final JMenuItem _sortDescending, _removeSelected;

    @Override
    public void justifyView() {
        //System.out.println("view justified");
        _updateRectangle(getCoolMapView().getActiveCell());
        getViewPanel().repaint();
    }

    public RowLabels(CoolMapObject obj) {
        super(obj);
        setName("Row Labels");
        _sortAscending = new JMenuItem("Sort Ascending", UI.getImageIcon("leftThin"));
        _sortDescending = new JMenuItem("Sort Dscending", UI.getImageIcon("rightThin"));
        _zoomControlY = getCoolMapView().getZoomControlY();

        _sortAscending.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (isDataViewValid()) {
                    getCoolMapObject().sortRow(getCoolMapView().getActiveCell().getRow(), false);
                }
            }
        });

        _sortDescending.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (isDataViewValid()) {
                    getCoolMapObject().sortRow(getCoolMapView().getActiveCell().getRow(), true);
                }
            }
        });

        _menu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
                boolean matrixValid = _validateActions();
                if (matrixValid && Comparable.class.isAssignableFrom(getCoolMapView().getCoolMapObject().getViewClass())) {
                    _sortAscending.setEnabled(true);
                    _sortDescending.setEnabled(true);
                } else {
                    _sortAscending.setEnabled(false);
                    _sortDescending.setEnabled(false);
                }

                if (isDataViewValid()) {
                    ArrayList<Range<Integer>> selRows = getCoolMapView().getSelectedRows();
                    if (selRows == null || selRows.isEmpty()) {
                        _removeSelected.setEnabled(false);
                    } else {
                        _removeSelected.setEnabled(true);
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent pme) {
            }
        });

        getViewPanel().addMouseListener(this);
        getViewPanel().addMouseMotionListener(this);
        getViewPanel().setComponentPopupMenu(_menu);
        _menu.add(_sortAscending);
        _menu.add(_sortDescending);

        _removeSelected = new JMenuItem("Remove selected rows (w/o expanded nodes)", UI.getImageIcon("trashBin"));
        _removeSelected.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (isDataViewValid()) {
                    CoolMapObject obj = getCoolMapObject();
                    ArrayList<Range<Integer>> selRows = getCoolMapView().getSelectedRows();
                    ArrayList<VNode> nodesToBeRemoved = new ArrayList<VNode>();
                    for (Range<Integer> selections : selRows) {
                        for (int i = selections.lowerEndpoint(); i < selections.upperEndpoint(); i++) {
                            VNode node = obj.getViewNodeRow(i);
                            if (node.getParentNode() == null) {
                                nodesToBeRemoved.add(node);
                            }
                        }
                    }//end iteration
                    CoolMapState state = CoolMapState.createStateRows("Remove rows", obj, null);
                    obj.removeViewNodesRow(nodesToBeRemoved);
                    StateStorageMaster.addState(state);
                }
            }
        });

        _menu.add(_removeSelected);
    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

//    @Override
//    public void subSelectionRowChanged(CoolMapObject object) {
////        System.out.println("SubSelected Row changed");
////        System.out.println(object.getCoolMapView().getSubSelectedRows());
//    }
//
//    @Override
//    public void subSelectionColumnChanged(CoolMapObject object) {
//    }

//    @Override
//    public void stateStorageUpdated(CoolMapObject object) {
//    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
    }

    private boolean _validateActions() {
        CoolMapView view = getCoolMapView();
        if (view == null) {
            return false;
        }

        CoolMapObject object = view.getCoolMapObject();

        if (object == null) {
            return false;
        }

        if (object.getViewClass() == null || !Comparable.class.isAssignableFrom(object.getViewClass())) {
            return false;
        }

        return view.getActiveCell().isRowValidCell(object);

    }

    @Override
    public JComponent getConfigUI() {
        return null;
    }

    @Override
    protected void prePaint(Graphics2D g2D, CoolMapObject<Object, Object> obj, int width, int height) {
        if (!isDataViewValid()) {
            return;
        }

        CoolMapView canvas = getCoolMapView();

        if (canvas != null && obj != null) {
            ArrayList<Range<Integer>> selectedRows = canvas.getSelectedRows();
            if (selectedRows != null && !selectedRows.isEmpty()) {
                for (Range<Integer> range : selectedRows) {
                    int start = range.lowerEndpoint();
                    int end = range.upperEndpoint() - 1;
                    VNode startNode = obj.getViewNodeRow(start);
                    VNode endNode = obj.getViewNodeRow(end);

                    if (startNode != null && endNode != null && startNode.getViewOffset() != null && endNode.getViewOffset() != null) {
                        int x = 0;
                        int y = (int) (canvas.getMapAnchor().y + startNode.getViewOffset()) - getAnchorY();
                        int selectionHeight = (int) (endNode.getViewOffset(canvas.getZoomY()) - startNode.getViewOffset());
                        int selectionWidth = width;
                        g2D.setColor(UI.colorLightBlue0);
                        g2D.fillRect(x, y, selectionWidth, selectionHeight);
                    }

                }
            }

        }

        //System.out.println("prepainted");
        if (isEnabled() && canvas != null) {
            if (canvas.getActiveCell().isRowValidCell(canvas.getCoolMapObject())) {
                _activeRectangle.width = width;
                g2D.setColor(UI.colorLightGreen5);
                g2D.fillRect(_activeRectangle.x, _activeRectangle.y, _activeRectangle.width, _activeRectangle.height);
            }
        }
    }

    @Override
    protected void prepareRender(Graphics2D g2D) {
        g2D.setFont(_zoomControlY.getBoldFont());
        _maxDescent = g2D.getFontMetrics().getMaxDescent();
        _fontSize = _zoomControlY.getBoldFont().getSize();
    }

    @Override
    protected void postPaint(Graphics2D g2D, CoolMapObject<Object, Object> canvas, int width, int height) {
        if (!isDataViewValid()) {
            return;
        }

        if (_dragStart) {
            g2D.setColor(UI.colorGrey6);
            g2D.setStroke(UI.strokeDash2);
            g2D.fillOval(_dragStartPoint.x - 3, _dragStartPoint.y - 3, 6, 6);
            CoolMapView view = getCoolMapView();
            int targetY;
            if (isDataViewValid() && _targetRow != null) {

                if (_targetRow < 0) {
                    _targetRow = 0;
                }

                VNode node = view.getCoolMapObject().getViewNodeRow(_targetRow);

                if (node != null && node.getViewOffset() != null) {
                    targetY = (int) (view.getMapAnchor().y + node.getViewOffset() - getAnchorY());
                    g2D.drawLine(0, targetY, width, targetY);
                    g2D.drawLine(_dragStartPoint.x, _dragStartPoint.y, _dragEndPoint.x, targetY);
                    g2D.drawImage(UI.getImageIcon("insertRow").getImage(), _dragEndPoint.x - 18, _dragEndPoint.y - 18, null);
                } else if (_targetRow == view.getCoolMapObject().getViewNumRows()) {
                    node = view.getCoolMapObject().getViewNodeRow(_targetRow - 1);
                    if (node != null) {
                        targetY = (int) (view.getMapAnchor().y + node.getViewOffset(view.getZoomY()) - getAnchorY());
                        g2D.drawLine(0, targetY, width, targetY);
                        g2D.drawLine(_dragStartPoint.x, _dragStartPoint.y, _dragEndPoint.x, targetY);
                        g2D.drawImage(UI.getImageIcon("insertColumn").getImage(), _dragEndPoint.x - 8, _dragEndPoint.y - 8, null);
                    }
                }
            }

        }



        CoolMapObject obj = getCoolMapObject();
        VNode lastSortedRow = obj.getSortTracker().lastSortedRow;
        boolean lastSortedRowDescending = obj.getSortTracker().lastSortedRowDescending;
        if (lastSortedRow == null || lastSortedRow.getViewOffset() == null) {
            return;
        }
        Point anchor = getCoolMapView().getMapAnchor();
        int y = (int) (anchor.y + lastSortedRow.getViewOffset() - getAnchorY());
        int cellHeight = (int) lastSortedRow.getViewSizeInMap(getCoolMapView().getZoomY());

        g2D.setStroke(UI.stroke4);

        g2D = (Graphics2D) g2D.create(0, y, width, cellHeight);
        if (!lastSortedRowDescending) {
            g2D.setColor(UI.colorDarkGreen1);
            g2D.drawLine(12, 0, 8, 0 + cellHeight / 2);
            g2D.drawLine(8, 0 + cellHeight / 2, 12, 0 + cellHeight);

        } else {
            g2D.setColor(UI.colorOrange2);
            g2D.drawLine(8, 0, 12, 0 + cellHeight / 2);
            g2D.drawLine(12, 0 + cellHeight / 2, 8, 0 + cellHeight);
        }
        g2D.setClip(null);



    }

    @Override
    public boolean canRender(CoolMapObject coolMapObject) {
        return true;
    }

    @Override
    protected void renderRow(Graphics2D g2D, CoolMapObject<Object, Object> object, VNode node, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (node == null || getCoolMapView() == null) {
            return;
        }

        String label = node.getViewLabel();
        if (label != null) {
            g2D.setColor(UI.colorBlack3);
            g2D.drawString(label, anchorX + _marginSize, anchorY + cellHeight - _maxDescent - (cellHeight - _fontSize) / 2 + 1);
        }

        if (node.isGroupNode()) {
            Color color;

            if (node.getViewColor() == null) {
                color = node.getCOntology().getViewColor();
            } else {
                color = node.getViewColor();
            }

            if (cellHeight > 6) {
                g2D.setColor(color);
                g2D.fillRoundRect(anchorX - 4, anchorY + 1, 10, cellHeight - 2, 4, 4);
            } else {
                g2D.setColor(color);
                g2D.fillRect(anchorX - 4, anchorY, 10, cellHeight);
            }
        }
    }

    @Override
    public void activeCellChanged(CoolMapObject obj, MatrixCell oldCell, MatrixCell newCell) {
        //System.out.println(newCell);
        //_updateRectangle(newCell);
        if (_hoverAnimator.isRunning()) {
            _hoverAnimator.cancel();
        }
        _hoverTarget.setBeginEnd(oldCell, newCell);
        _hoverAnimator.start();
    }
    private final HoverTarget _hoverTarget = new HoverTarget();
    private final Animator _hoverAnimator = CAnimator.createInstance(_hoverTarget, 150);

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
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
    public void gridChanged(CoolMapObject object) {
    }

    private class HoverTarget implements TimingTarget {

        private final Rectangle _beginRect = new Rectangle();
        private final Rectangle _endRect = new Rectangle();

        public void setBeginEnd(MatrixCell oldCell, MatrixCell newCell) {
            
            
            
            CoolMapView view = getCoolMapView();
//            if (view == null || oldCell == null || oldCell.getRow() == null || newCell == null || newCell.getRow() == null) {
//                return;
//            }
            if (view == null){ // || oldCell == null || oldCell.getCol() == null || newCell == null || newCell.getCol() == null) {
                return;
            }
            
            if(oldCell == null || newCell == null){
                return;
            }
            
            if(newCell.row == null){
                return;
            }
            
            if(oldCell.row == null && newCell.row != null){
                oldCell.row = newCell.row;
            }
            
            
            VNode oldNode = getCoolMapView().getCoolMapObject().getViewNodeRow(oldCell.getRow());
            VNode newNode = getCoolMapView().getCoolMapObject().getViewNodeRow(newCell.getRow());

            if (oldNode == null || oldNode.getViewOffset() == null || newNode == null || newNode.getViewOffset() == null) {
                return;
            }

            _beginRect.x = 0;
            _beginRect.y = (int) (getCoolMapView().getMapAnchor().y + oldNode.getViewOffset() - getAnchorY());
            _beginRect.height = (int) oldNode.getViewSizeInMap(view.getZoomY());

            _endRect.x = 0;
            _endRect.y = (int) (getCoolMapView().getMapAnchor().y + newNode.getViewOffset() - getAnchorY());
            _endRect.height = (int) newNode.getViewSizeInMap(view.getZoomY());
            getViewPanel().repaint();
        }

        @Override
        public void begin(Animator source) {
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
            _activeRectangle.x = 0;
            _activeRectangle.y = (int) (_beginRect.y + (_endRect.y - _beginRect.y) * fraction);
            _activeRectangle.height = (int) (_beginRect.height + (_endRect.height - _beginRect.height) * fraction);
            getViewPanel().repaint();
        }
    }
    private Rectangle _activeRectangle = new Rectangle();

    private void _updateRectangle(MatrixCell activeCell) {
        CoolMapView view = getCoolMapView();
        if (view == null || activeCell == null || activeCell.getRow() == null) {
            return;
        }

        VNode node = getCoolMapView().getCoolMapObject().getViewNodeRow(activeCell.getRow());

        if (node == null || node.getViewOffset() == null) {
            return;
        }
        _activeRectangle.x = 0; //(int) (getCoolMapView().getMapAnchor().x + node.getViewOffset());
        _activeRectangle.y = (int) (getCoolMapView().getMapAnchor().y + node.getViewOffset() - getAnchorY());
        _activeRectangle.height = (int) node.getViewSizeInMap(view.getZoomY());
    }

    @Override
    public void selectionChanged(CoolMapObject obj) {
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (SwingUtilities.isLeftMouseButton(me)) {
            _rowSelectionChange(me);
        }
    }
    private Integer _anchorRow = null;

    private void _rowSelectionChange(MouseEvent me) {
        if (getCoolMapView() == null && getCoolMapView().getCoolMapObject() == null) {
            return;
        }

        CoolMapView view = getCoolMapView();
        CoolMapObject obj = view.getCoolMapObject();

        if (obj.getViewNumColumns() <= 0) {
            return;
        }

        Integer targetRow = view.getCurrentRow(me.getY() + getAnchorY());
        if (targetRow == null || targetRow < 0 || targetRow >= obj.getViewNumRows()) {
            return;
        }

//////////////////////////////////////////////////////////////////////////////////
        ArrayList<Range<Integer>> selectedRows = view.getSelectedRows();

        if (me.isControlDown() || me.isMetaDown()) {
            if (selectedRows.isEmpty()) {
                _newSingleSelection(obj, targetRow);
            } else {
                boolean containRange = false;
                for (Range<Integer> range : selectedRows) {
                    if (range.contains(targetRow)) {
                        //remove single selection
                        _removeSingleSelection(obj, targetRow);
                        containRange = true;
                        break;
                    }
                }
                if (containRange == false) {
                    _addSingleSelection(obj, targetRow);
                }
            }

        } else if (me.isShiftDown()) {
            _newSpanSelection(obj, targetRow);
        } else {
            _newSingleSelection(obj, targetRow);
        }

//        if (me.getClickCount() > 1) {
//            view.clearSelection();
//        }
    }

    private void _removeSingleSelection(CoolMapObject obj, int targetRow) {
        CoolMapView view = obj.getCoolMapView();
        ArrayList<Range<Integer>> selectedColumns = view.getSelectedColumns();
        if (selectedColumns.isEmpty()) {
            selectedColumns.add(Range.closedOpen(0, obj.getViewNumColumns()));
        }
        ////////////////////////////////////////////////////////////////
        ArrayList<Range<Integer>> selectedRows = view.getSelectedRows();
        if (selectedRows.isEmpty()) {
            return;
        } else {
            Range<Integer> tempRange = null;
            for (Range<Integer> range : selectedRows) {
                if (range.contains(targetRow)) {
                    tempRange = range;
                    break;
                }
            }

            //System.out.println("temp range:" + tempRange);

            if (tempRange == null) {
                return; //no range contain this range
            } else {
                if (tempRange.lowerEndpoint().intValue() == targetRow && tempRange.upperEndpoint().intValue() == targetRow + 1) {
                    selectedRows.remove(tempRange);


                } else {
                    //split the rectangles, and remove that columns
                    selectedRows.remove(tempRange);
                    //move lower end up by 1
                    if (tempRange.lowerEndpoint().intValue() == targetRow) {
                        tempRange = Range.closedOpen(targetRow + 1, tempRange.upperEndpoint());
                        selectedRows.add(tempRange);
                    } else if (tempRange.upperEndpoint().intValue() == targetRow + 1) {
                        tempRange = Range.closedOpen(tempRange.lowerEndpoint(), targetRow);
                        selectedRows.add(tempRange);
                    } else {
                        selectedRows.add(Range.closedOpen(tempRange.lowerEndpoint(), targetRow));
                        selectedRows.add(Range.closedOpen(targetRow + 1, tempRange.upperEndpoint()));
                    }
                }
                //use selected rows and selected columns to rebuild 
                ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
                for (Range<Integer> colRange : selectedColumns) {
                    for (Range<Integer> rowRange : selectedRows) {
                        newSelections.add(new Rectangle(colRange.lowerEndpoint(), rowRange.lowerEndpoint(), colRange.upperEndpoint() - colRange.lowerEndpoint(), rowRange.upperEndpoint() - rowRange.lowerEndpoint()));
                    }
                }
                CoolMapState state = CoolMapState.createStateRows("Remove selected row", obj, null);
                view.setSelections(newSelections);
                StateStorageMaster.addState(state);
//does not change anchor col
            }
        }
    }

    private void _newSpanSelection(CoolMapObject obj, int targetRow) {
        //does not change anchor
        //
        if (_anchorRow == null) {
            _newSingleSelection(obj, targetRow);
        } else {
            CoolMapView view = obj.getCoolMapView();
            ArrayList<Range<Integer>> selectedRows = view.getSelectedRows();
            Range<Integer> containingOne = null;
            for (Range<Integer> range : selectedRows) {
                if (range.contains(_anchorRow)) {
                    containingOne = range;
                    break;
                }
            }
            if (containingOne != null) {
                selectedRows.remove(containingOne);
            }
            Range<Integer> newRange;
            if (targetRow > _anchorRow) {
                //first remove every selection that contains targetCol
                //only one will contain colselection
                //remove anchorcol
                newRange = Range.closedOpen(_anchorRow, targetRow + 1);
            } else {
                newRange = Range.closedOpen(targetRow, _anchorRow + 1);
            }
            selectedRows.add(newRange);
            //build a new selection like this
            ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();

            ArrayList<Range<Integer>> selectedColumns = view.getSelectedColumns();

            for (Range<Integer> colRange : selectedColumns) {
                for (Range<Integer> rowRange : selectedRows) {
                    newSelections.add(new Rectangle(colRange.lowerEndpoint(), rowRange.lowerEndpoint(), colRange.upperEndpoint() - colRange.lowerEndpoint(), rowRange.upperEndpoint() - rowRange.lowerEndpoint()));
                }
            }
            CoolMapState state = CoolMapState.createStateRows("Select rows", obj, null);
            view.setSelections(newSelections);
            StateStorageMaster.addState(state);
        }
    }

    private void _newSingleSelection(CoolMapObject obj, int targetRow) {
        CoolMapView view = obj.getCoolMapView();
        ArrayList<Range<Integer>> selectedColumns = view.getSelectedColumns();
        if (selectedColumns.isEmpty()) {
            selectedColumns.add(Range.closedOpen(0, obj.getViewNumColumns()));
        }

        ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
        for (Range<Integer> range : selectedColumns) {
            newSelections.add(new Rectangle(range.lowerEndpoint(), targetRow, range.upperEndpoint() - range.lowerEndpoint(), 1));
        }
        CoolMapState state = CoolMapState.createStateRows("Select row", obj, null);
        view.setSelections(newSelections);
        StateStorageMaster.addState(state);
        
        _anchorRow = targetRow;
    }

    private void _addSingleSelection(CoolMapObject obj, int targetRow) {
        CoolMapView view = obj.getCoolMapView();
        ArrayList<Range<Integer>> selectedColumns = view.getSelectedColumns();
        if (selectedColumns.isEmpty()) {
            selectedColumns.add(Range.closedOpen(0, obj.getViewNumColumns()));
        }

        ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
        for (Range<Integer> range : selectedColumns) {
            newSelections.add(new Rectangle(range.lowerEndpoint(), targetRow, range.upperEndpoint() - range.lowerEndpoint(), 1));
        }
        CoolMapState state = CoolMapState.createStateRows("Add selected row", obj, null);
        view.addSelection(newSelections);
        StateStorageMaster.addState(state);
        
        _anchorRow = targetRow;
    }
    private boolean _dragStart = false;
    private final Point _dragStartPoint = new Point();
    private final Point _dragEndPoint = new Point();
    private Integer _targetRow = null;
    private Integer _startRow = null;

    @Override
    public void mousePressed(MouseEvent me) {
        if (SwingUtilities.isLeftMouseButton(me) && isDataViewValid()) {

            _startRow = getCoolMapView().getCurrentRow(me.getY() + getAnchorY());
            ArrayList<Range<Integer>> selectedRows = getCoolMapView().getSelectedRows();
            for (Range<Integer> range : selectedRows) {
                if (range.contains(_startRow)) {
                    _dragStart = true;
                    //no need to know the start col
                    _dragStartPoint.x = me.getX();
                    _dragStartPoint.y = me.getY();
                }
            }
        }
    }

    //can use translate to canvas function to translate 
    @Override
    public void mouseReleased(MouseEvent me) {
        if (SwingUtilities.isLeftMouseButton(me) && isDataViewValid() && _dragStart) {
            Integer endRow = getCoolMapView().getCurrentRow(me.getY() + getAnchorY());
            if (endRow != null) {
                //System.out.println("Drag row to:" + endRow);
                //getCoolMapView().getCoolMapObject().multiShiftColumns(getCoolMapView().getSelectedColumns(), endCol.intValue());
                if (_startRow != null && _startRow.intValue() != endRow.intValue()) {
                    
                    ArrayList<Range<Integer>> rows = getCoolMapObject().getCoolMapView().getSelectedRows();
                    if(rows == null || rows.isEmpty()){
                        return;
                    }
                    
                    CoolMapState state = CoolMapState.createStateRows("Shift rows", getCoolMapObject(), null);
                    getCoolMapObject().multiShiftRows(getCoolMapView().getSelectedRows(), endRow.intValue());
                    StateStorageMaster.addState(state);
                }
            }
            _dragStart = false;
            getViewPanel().repaint();
            mouseMoved(me);
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
        if (_dragStart && SwingUtilities.isLeftMouseButton(me)) {
            _dragEndPoint.x = me.getX();
            _dragEndPoint.y = me.getY();
            CoolMapView view = getCoolMapView();
            if (view != null) {
                _targetRow = view.getCurrentRow(me.getY() + getAnchorY());
            }
            //view panel is not null.
            getViewPanel().repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent me) {

        CoolMapView view = getCoolMapView();
        if (view != null) {
            Point mouse = translateToCanvas(me.getX(), me.getY());

            Integer row = view.getCurrentRow(mouse.y);
            MatrixCell oldCell = view.getActiveCell();
            MatrixCell newCell = new MatrixCell(row, oldCell.getCol());
            if (!newCell.valueEquals(oldCell)) {
                //System.out.println("Col changed:" + view.getActiveCell() + " " + newCell);
                view.setActiveCell(view.getActiveCell(), newCell);
                //

            }
        }
    }
}
