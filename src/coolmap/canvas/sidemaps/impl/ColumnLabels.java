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
import coolmap.canvas.sidemaps.ColumnMap;
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
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Set;
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
public class ColumnLabels extends ColumnMap<Object, Object> implements MouseListener, MouseMotionListener {

    private ZoomControl _zoomControlX;
    private int _fontSize = 0;
    private int _maxDescent = 0;
    private int _liftSize = 16;
    private Rectangle _activeRectangle = new Rectangle();
    private final JPopupMenu _menu = new JPopupMenu();
    private final JMenuItem _sortAscending;
    private final JMenuItem _sortDescending, _removeSelected;

    public ColumnLabels(CoolMapObject object) {
        super(object);
        setName("Column Labels");
        getViewPanel().addMouseListener(this);
        getViewPanel().addMouseMotionListener(this);

        _sortAscending = new JMenuItem("Sort ascending", UI.getImageIcon("upThin"));
        _sortDescending = new JMenuItem("Sort dscending", UI.getImageIcon("downThin"));
        _zoomControlX = getCoolMapView().getZoomControlX();


        _sortAscending.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (isDataViewValid()) {
                    getCoolMapObject().sortColumn(getCoolMapView().getActiveCell().getCol(), false);
                }
            }
        });

        _sortDescending.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (isDataViewValid()) {
                    getCoolMapObject().sortColumn(getCoolMapView().getActiveCell().getCol(), true);
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
                    ArrayList<Range<Integer>> selColumns = getCoolMapView().getSelectedColumns();
                    if (selColumns == null || selColumns.isEmpty()) {
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

        getViewPanel().setComponentPopupMenu(_menu);
        _menu.add(_sortAscending);
        _menu.add(_sortDescending);
        _menu.addSeparator();

        _removeSelected = new JMenuItem("Remove selected columns (w/o expanded nodes)", UI.getImageIcon("trashBin"));
        _removeSelected.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (isDataViewValid()) {
                    CoolMapObject obj = getCoolMapObject();
                    ArrayList<Range<Integer>> selColumns = getCoolMapView().getSelectedColumns();
                    ArrayList<VNode> nodesToBeRemoved = new ArrayList<VNode>();
                    for (Range<Integer> selections : selColumns) {
                        for (int i = selections.lowerEndpoint(); i < selections.upperEndpoint(); i++) {
                            VNode node = obj.getViewNodeColumn(i);
                            if (node.getParentNode() == null) {
                                nodesToBeRemoved.add(node);
                            }
                        }
                    }//end iteration
                    obj.removeViewNodesColumn(nodesToBeRemoved);
                }
            }
        });

        _menu.add(_removeSelected);

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

        return view.getActiveCell().isColValidCell(object);

    }

    @Override
    protected void renderColumn(Graphics2D g2D, CoolMapObject<Object, Object> object, VNode node, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (node == null || getCoolMapView() == null) {
            return;
        }

        String label = node.getViewLabel();
        if (label != null) {
            //System.out.println("Rendered label" + label);
            //Sometimes the label is drawn, but not shown.
            //System.out.println(label + " " + anchorX + " " + anchorY);
            g2D.setColor(UI.colorBlack3);
            g2D.drawString(label, anchorX + cellWidth - _maxDescent - (cellWidth - _fontSize) / 2 - 1, anchorY + cellHeight - _liftSize);
        }

        if (node.isGroupNode()) {


            Color color;

            if (node.getViewColor() == null) {
                color = node.getCOntology().getViewColor();
            } else {
                color = node.getViewColor();
            }




            if (cellWidth > 6) {
                g2D.setColor(color);
                g2D.fillRoundRect(anchorX + 1, anchorY + cellHeight - 5, cellWidth - 2, 10, 4, 4);
            } else {
                g2D.setColor(color);
                g2D.fillRect(anchorX, anchorY + cellHeight - 5, cellWidth, 10);
            }
        }
    }

    @Override
    public void prePaint(Graphics2D g2D, CoolMapObject<Object, Object> object, int width, int height) {


        //paint selected columns
        CoolMapObject obj = getCoolMapObject();
        CoolMapView canvas = getCoolMapView();
        if (canvas != null && obj != null) {
            ArrayList<Range<Integer>> selectedColumns = canvas.getSelectedColumns();
            if (selectedColumns != null && !selectedColumns.isEmpty()) {
                for (Range<Integer> range : selectedColumns) {
                    int start = range.lowerEndpoint();
                    int end = range.upperEndpoint() - 1;



                    VNode startNode = obj.getViewNodeColumn(start);
                    VNode endNode = obj.getViewNodeColumn(end);

                    if (startNode != null && endNode != null && startNode.getViewOffset() != null && endNode.getViewOffset() != null) {
                        int x = (int) (canvas.getMapAnchor().x + startNode.getViewOffset());
                        int y = 0;
                        int selectionWidth = (int) (endNode.getViewOffset(canvas.getZoomX()) - startNode.getViewOffset());
                        int selectionHeight = height;
                        g2D.setColor(UI.colorLightBlue0);
                        g2D.fillRect(x, y, selectionWidth, selectionHeight);
                    }

                }
            }
        }

        if (isEnabled() && canvas != null) {
            if (canvas.getActiveCell().isColValidCell(canvas.getCoolMapObject())) {
                _activeRectangle.height = height;
                g2D.setColor(UI.colorLightGreen5);
                g2D.fillRect(_activeRectangle.x, _activeRectangle.y, _activeRectangle.width, _activeRectangle.height);
            }
        }
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
        //System.out.println("anchor changed");
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
    }

//    @Override
//    public void stateStorageUpdated(CoolMapObject object) {
//    }

//    @Override
//    public void subSelectionRowChanged(CoolMapObject object) {
//    }

//    @Override
//    public void subSelectionColumnChanged(CoolMapObject object) {
//        System.out.println(object.getCoolMapView().getSubSelectedColumns());
//    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    @Override
    public void gridChanged(CoolMapObject object) {
    }

    private class HoverTarget implements TimingTarget {

        private final Rectangle _beginRect = new Rectangle();
        private final Rectangle _endRect = new Rectangle();

        public void setBeginEnd(MatrixCell oldCell, MatrixCell newCell) {
            
            //System.out.println(oldCell + " " + newCell);
            
            CoolMapView view = getCoolMapView();
            if (view == null){ // || oldCell == null || oldCell.getCol() == null || newCell == null || newCell.getCol() == null) {
                return;
            }
            
            if(oldCell == null || newCell == null){
                return;
            }
            
            if(newCell.col == null){
                return;
            }
            
            if(oldCell.col == null && newCell.col != null){
                oldCell.col = newCell.col;
            }
            
            
            

            VNode oldNode = getCoolMapView().getCoolMapObject().getViewNodeColumn(oldCell.getCol());
            VNode newNode = getCoolMapView().getCoolMapObject().getViewNodeColumn(newCell.getCol());

            if (oldNode == null || oldNode.getViewOffset() == null || newNode == null || newNode.getViewOffset() == null) {
                return;
            }

            _beginRect.x = (int) (getCoolMapView().getMapAnchor().x + oldNode.getViewOffset());
            _beginRect.y = 0;
            _beginRect.width = (int) oldNode.getViewSizeInMap(view.getZoomX());

            _endRect.x = (int) (getCoolMapView().getMapAnchor().x + newNode.getViewOffset());
            _endRect.y = 0;
            _endRect.width = (int) newNode.getViewSizeInMap(view.getZoomX());

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
            _activeRectangle.x = (int) (_beginRect.x + (_endRect.x - _beginRect.x) * fraction);
            _activeRectangle.y = 0;
            _activeRectangle.width = (int) (_beginRect.width + (_endRect.width - _beginRect.width) * fraction);
            getViewPanel().repaint();
        }
    }

    @Override
    public void postPaint(Graphics2D g2D, CoolMapObject<Object, Object> obj, int width, int height) {

        if (_dragStart) {
            g2D.setColor(UI.colorGrey6);
            g2D.setStroke(UI.strokeDash2);
            g2D.fillOval(_dragStartPoint.x - 3, _dragStartPoint.y - 3, 6, 6);




            CoolMapView view = getCoolMapView();
            int targetX;
            if (isDataViewValid() && _targetCol != null) {
                if (_targetCol < 0) {
                    _targetCol = 0;
                }
                VNode node = view.getCoolMapObject().getViewNodeColumn(_targetCol);

                if (node != null && node.getViewOffset() != null) {
                    targetX = (int) (view.getMapAnchor().x + node.getViewOffset());
                    g2D.drawLine(targetX, 0, targetX, height);
                    g2D.drawLine(_dragStartPoint.x, _dragStartPoint.y, targetX, _dragEndPoint.y);
                    g2D.drawImage(UI.getImageIcon("insertColumn").getImage(), _dragEndPoint.x - 18, _dragEndPoint.y - 18, null);
                } else if (_targetCol == view.getCoolMapObject().getViewNumColumns()) {
                    node = view.getCoolMapObject().getViewNodeColumn(_targetCol - 1);
                    if (node != null) {
                        targetX = (int) (view.getMapAnchor().x + node.getViewOffset(view.getZoomX()));
                        g2D.drawLine(targetX, 0, targetX, height);
                        g2D.drawLine(_dragStartPoint.x, _dragStartPoint.y, targetX, _dragEndPoint.y);
                        g2D.drawImage(UI.getImageIcon("insertColumn").getImage(), _dragStartPoint.x - 8, _dragStartPoint.y - 8, null);
                    }
                }

                //draw a marker for ontology


                //draw a marker for sorter
            }


        }

        if (isDataViewValid()) {

            obj = getCoolMapObject();
            VNode lastSortedColumn = obj.getSortTracker().lastSortedColumn;
            boolean lastSortedColumnDescending = obj.getSortTracker().lastSortedColumnDescending;
            if (lastSortedColumn == null || lastSortedColumn.getViewOffset() == null) {
                return;
            }
            Point anchor = getCoolMapView().getMapAnchor();
            int x = (int) (anchor.x + lastSortedColumn.getViewOffset());
            int cellWidth = (int) lastSortedColumn.getViewSizeInMap(getCoolMapView().getZoomX());

            g2D.setStroke(UI.stroke4);


            //Don't dispose
            g2D = (Graphics2D) g2D.create(x, 0, cellWidth, height);
            //g2D.setClip(x, 0, cellWidth, height);
            if (!lastSortedColumnDescending) {
//                g2D.drawLine(x, height-8, x + cellWidth / 2, height-12);
//                g2D.drawLine(x + cellWidth / 2, height-12, x + cellWidth, height-8);

                g2D.setColor(UI.colorDarkGreen1);
                g2D.drawLine(0, height - 8, cellWidth / 2, height - 12);
                g2D.drawLine(0 + cellWidth / 2, height - 12, cellWidth, height - 8);
            } else {
                g2D.setColor(UI.colorOrange2);
                g2D.drawLine(0, height - 12, cellWidth / 2, height - 8);
                g2D.drawLine(cellWidth / 2, height - 8, cellWidth, height - 12);
            }
            g2D.setClip(null);
        }




    }

    @Override
    public boolean canRender(CoolMapObject coolMapObject) {
        return true;
    }

    @Override
    public JComponent getConfigUI() {
        return null;
    }

    @Override
    public void justifyView() {
        _updateRectangle(getCoolMapView().getActiveCell());
        getViewPanel().repaint();
    }

    @Override
    public void activeCellChanged(CoolMapObject obj, MatrixCell oldCell, MatrixCell newCell) {
//        System.out.println(oldCell + " " + newCell);
//        _updateRectangle(newCell);
        if (_hoverAnimator.isRunning()) {
            _hoverAnimator.cancel();
        }
        _hoverTarget.setBeginEnd(oldCell, newCell);
        _hoverAnimator.start();
    }

    private void _updateRectangle(MatrixCell activeCell) {
        CoolMapView view = getCoolMapView();
        if (view == null || activeCell == null || activeCell.getCol() == null) {
            return;
        }

        VNode node = getCoolMapView().getCoolMapObject().getViewNodeColumn(activeCell.getCol());

        if (node == null || node.getViewOffset() == null) {
            return;
        }
        _activeRectangle.x = (int) (getCoolMapView().getMapAnchor().x + node.getViewOffset());
        _activeRectangle.y = 0;
        _activeRectangle.width = (int) node.getViewSizeInMap(view.getZoomX());
    }

    @Override
    public void selectionChanged(CoolMapObject obj) {
        Set<Rectangle> selections = obj.getCoolMapView().getSelections();
        //need to find out which columns are selected
        //need a way to merge rectangles.


    }

    @Override
    protected void prepareRender(Graphics2D g2D) {
        AffineTransform at = new AffineTransform();
        at.rotate(-Math.PI / 2);
        g2D.setFont(_zoomControlX.getBoldFont().deriveFont(at));
        _maxDescent = g2D.getFontMetrics().getMaxDescent();
        _fontSize = _zoomControlX.getBoldFont().getSize();
    }

    @Override
    public void mouseClicked(MouseEvent me) {




        if (SwingUtilities.isLeftMouseButton(me)) {
            _colSelectionChange(me);
        }

    }
    private Integer _anchorCol = null;

    private void _colSelectionChange(MouseEvent me) {
/////////////////////////////////////////////////////////////////////////////////////   
//Robust Judgement        
        if (getCoolMapView() == null && getCoolMapView().getCoolMapObject() == null) {
            return;
        }

        CoolMapView view = getCoolMapView();
        CoolMapObject obj = view.getCoolMapObject();

        if (obj.getViewNumRows() <= 0) {
            return;
        }

        Integer targetCol = view.getCurrentCol(me.getX());
        if (targetCol == null || targetCol < 0 || targetCol >= obj.getViewNumColumns()) {
            return;
        }
/////////////////////////////////////////////////////////////////////////////////////        

        ArrayList<Range<Integer>> selectedColumns = view.getSelectedColumns();

        //create new selections
        if (me.isControlDown() || me.isMetaDown()) {
            if (selectedColumns.isEmpty()) {
                //same as single selection
                _newSingleSelection(obj, targetCol);
            } else {
                boolean containRange = false;
                for (Range<Integer> range : selectedColumns) {
                    if (range.contains(targetCol)) {
                        _removeSingleSelection(obj, targetCol);
                        containRange = true;
                        break;
                    }
                }
                if (containRange == false) {
                    _addSingleSelection(obj, targetCol);
                }


            }


        } else if (me.isShiftDown()) {
            //select a range.
            _newSpanSelection(obj, targetCol);

        } //create a new single selection
        else {
            _newSingleSelection(obj, targetCol);
        }

        if (me.getClickCount() > 1) {
            view.clearSelection();
        }

    }

    private void _newSpanSelection(CoolMapObject obj, int targetCol) {
        //does not change anchor
        //
        if (_anchorCol == null) {
            _newSingleSelection(obj, targetCol);
        } else {
            CoolMapView view = obj.getCoolMapView();
            ArrayList<Range<Integer>> selectedColumns = view.getSelectedColumns();
            Range<Integer> containingOne = null;
            for (Range<Integer> range : selectedColumns) {
                if (range.contains(_anchorCol)) {
                    containingOne = range;
                    break;
                }
            }
            if (containingOne != null) {
                selectedColumns.remove(containingOne);
            }
            Range<Integer> newRange;
            if (targetCol > _anchorCol) {
                //first remove every selection that contains targetCol
                //only one will contain colselection
                //remove anchorcol
                newRange = Range.closedOpen(_anchorCol, targetCol + 1);
            } else {
                newRange = Range.closedOpen(targetCol, _anchorCol + 1);
            }
            selectedColumns.add(newRange);
            //build a new selection like this
            ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();

            ArrayList<Range<Integer>> selectedRows = view.getSelectedRows();

            for (Range<Integer> colRange : selectedColumns) {
                for (Range<Integer> rowRange : selectedRows) {
                    newSelections.add(new Rectangle(colRange.lowerEndpoint(), rowRange.lowerEndpoint(), colRange.upperEndpoint() - colRange.lowerEndpoint(), rowRange.upperEndpoint() - rowRange.lowerEndpoint()));
                }
            }
            view.setSelections(newSelections);
        }
    }

    private void _newSingleSelection(CoolMapObject obj, int targetCol) {
        CoolMapView view = obj.getCoolMapView();
        ArrayList<Range<Integer>> selectedRows = view.getSelectedRows();
        if (selectedRows.isEmpty()) {
            selectedRows.add(Range.closedOpen(0, obj.getViewNumRows()));
        }

        ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
        for (Range<Integer> range : selectedRows) {
            newSelections.add(new Rectangle(targetCol, range.lowerEndpoint(), 1, range.upperEndpoint() - range.lowerEndpoint()));
        }

        view.setSelections(newSelections);
        _anchorCol = targetCol;
    }

    private void _addSingleSelection(CoolMapObject obj, int targetCol) {
        CoolMapView view = obj.getCoolMapView();
        ArrayList<Range<Integer>> selectedRows = view.getSelectedRows();
        if (selectedRows.isEmpty()) {
            selectedRows.add(Range.closedOpen(0, obj.getViewNumRows()));
        }

        //the only difference is that the view was not cleared
        ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
        for (Range<Integer> range : selectedRows) {
            newSelections.add(new Rectangle(targetCol, range.lowerEndpoint(), 1, range.upperEndpoint() - range.lowerEndpoint()));
        }

        view.addSelection(newSelections);
        _anchorCol = targetCol;
    }

    private void _removeSingleSelection(CoolMapObject obj, int targetCol) {
        CoolMapView view = obj.getCoolMapView();
        ArrayList<Range<Integer>> selectedRows = view.getSelectedRows();
        if (selectedRows.isEmpty()) {
            selectedRows.add(Range.closedOpen(0, obj.getViewNumRows()));
        }
        ////////////////////////////////////////////////////////////////
        ArrayList<Range<Integer>> selectedColumns = view.getSelectedColumns();
        if (selectedColumns.isEmpty()) {
            return;
        } else {
            Range<Integer> tempRange = null;
            for (Range<Integer> range : selectedColumns) {
                if (range.contains(targetCol)) {
                    tempRange = range;
                    break;
                }
            }

            //System.out.println("temp range:" + tempRange);

            if (tempRange == null) {
                return; //no range contain this range
            } else {
                if (tempRange.lowerEndpoint().intValue() == targetCol && tempRange.upperEndpoint().intValue() == targetCol + 1) {
                    selectedColumns.remove(tempRange);


                } else {
                    //split the rectangles, and remove that columns
                    selectedColumns.remove(tempRange);
                    //move lower end up by 1
                    if (tempRange.lowerEndpoint().intValue() == targetCol) {
                        tempRange = Range.closedOpen(targetCol + 1, tempRange.upperEndpoint());
                        selectedColumns.add(tempRange);
                    } else if (tempRange.upperEndpoint().intValue() == targetCol + 1) {
                        tempRange = Range.closedOpen(tempRange.lowerEndpoint(), targetCol);
                        selectedColumns.add(tempRange);
                    } else {
                        selectedColumns.add(Range.closedOpen(tempRange.lowerEndpoint(), targetCol));
                        selectedColumns.add(Range.closedOpen(targetCol + 1, tempRange.upperEndpoint()));
                    }
                }
                //use selected rows and selected columns to rebuild 
                ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
                for (Range<Integer> colRange : selectedColumns) {
                    for (Range<Integer> rowRange : selectedRows) {
                        newSelections.add(new Rectangle(colRange.lowerEndpoint(), rowRange.lowerEndpoint(), colRange.upperEndpoint() - colRange.lowerEndpoint(), rowRange.upperEndpoint() - rowRange.lowerEndpoint()));
                    }
                }
                view.setSelections(newSelections);
                //does not change anchor col
            }
        }
    }
    //allowing multiple selection makes it super complex...
    private boolean _dragStart = false;
    private final Point _dragStartPoint = new Point();
    private final Point _dragEndPoint = new Point();
    private Integer _targetCol = null;
    private Integer _startCol = null;

    @Override
    public void mousePressed(MouseEvent me) {
        if (SwingUtilities.isLeftMouseButton(me) && isDataViewValid()) {

            _startCol = getCoolMapView().getCurrentCol(me.getX());

            ArrayList<Range<Integer>> selectedCols = getCoolMapView().getSelectedColumns();
            for (Range<Integer> range : selectedCols) {
                if (range.contains(_startCol)) {
                    _dragStart = true;
                    //no need to know the start col
                    _dragStartPoint.x = me.getX();
                    _dragStartPoint.y = me.getY();
                }
            }
        }

    }

    @Override
    public void mouseReleased(MouseEvent me) {
        if (SwingUtilities.isLeftMouseButton(me) && isDataViewValid() && _dragStart) {
            Integer endCol = getCoolMapView().getCurrentCol(me.getX());
            if (endCol != null) {
                //System.out.println("Drag column to:" + endCol);
                if (_startCol != null && _startCol.intValue() != endCol.intValue()) {
                    ArrayList<Range<Integer>> columns = getCoolMapView().getSelectedColumns();
                    if(columns == null || columns.isEmpty())
                        return;
                    
                    CoolMapState state = CoolMapState.createStateColumns("Shift columns", getCoolMapObject(), null);
                    getCoolMapView().getCoolMapObject().multiShiftColumns(getCoolMapView().getSelectedColumns(), endCol.intValue());
                    
                    StateStorageMaster.addState(state);
                    
                    
                } else {
                    _targetCol = null;
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
                _targetCol = view.getCurrentCol(me.getX());
            }
            //view panel is not null.
            getViewPanel().repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        //System.out.println("mouse moved");
        CoolMapView view = getCoolMapView();
        if (view != null) {
            Integer col = view.getCurrentCol(me.getX());
            MatrixCell oldCell = view.getActiveCell();
            MatrixCell newCell = new MatrixCell(oldCell.getRow(), col);
            if (!newCell.valueEquals(oldCell)) {
                //System.out.println("Col changed:" + view.getActiveCell() + " " + newCell);
                view.setActiveCell(view.getActiveCell(), newCell);
                //
            }
        }
    }
}
