/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas;

import com.google.common.collect.Range;
import coolmap.application.state.StateStorageMaster;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.canvas.listeners.CViewListener;
import coolmap.canvas.misc.ColDrawer;
import coolmap.canvas.misc.HilightLayer;
import coolmap.canvas.misc.MatrixCell;
import coolmap.canvas.misc.ProgressMask;
import coolmap.canvas.misc.RowDrawer;
import coolmap.canvas.misc.ZoomControl;
import coolmap.canvas.sidemaps.ColumnMap;
import coolmap.canvas.sidemaps.RowMap;
import coolmap.canvas.viewmaps.CoolMapLayer;
import coolmap.canvas.viewmaps.FilterLayer;
import coolmap.canvas.viewmaps.MapLayer;
import coolmap.canvas.viewmaps.PointAnnotationLayer;
import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.state.CoolMapState;
import coolmap.utils.RangeComparator;
import coolmap.utils.graphics.CAnimator;
import coolmap.utils.graphics.UI;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;

/**
 *
 * @author gangsu
 */
public final class CoolMapView<BASE, VIEW> {

    private CoolMapObject<BASE, VIEW> _coolMapObject;
    private JLayeredPane _canvas = new JLayeredPane();
    private final Rectangle _subMapDimension = new Rectangle();
    private final Rectangle _mapDimension = new Rectangle();
    //
    //layers
    private final HashSet<JComponent> _layers = new HashSet<JComponent>();
    //map layers. Can be extended
    private final ArrayList<MapLayer> _mapLayers = new ArrayList<MapLayer>();
    private final ArrayList<MapLayer> _overLayers = new ArrayList<MapLayer>();
    private final SelectionLayer _selectionLayer = new SelectionLayer();
    //submap bufferesize
    private int _subMapBufferSize = 350;
    private final MatrixCell _subMapIndexMin = new MatrixCell();
    private final MatrixCell _subMapIndexMax = new MatrixCell();
    private final MatrixCell _activeCell = new MatrixCell();
    //
    private boolean _antiAlias = true;
    //
    private UpdateBufferWorker _updateBufferWorker = null;
    //container used to store map layers
    private final Backdrop _backdrop = new Backdrop();
    private final MapContainer _mapContainer = new MapContainer();
    private final OverlayContainer _overlayContainer = new OverlayContainer();
    private final ProgressMask _progressMask = new ProgressMask();
    private final HilightLayer _hilightLayer = new HilightLayer();
    private final HoverLayer _hoverLayer = new HoverLayer();
    private final GridLayer _gridLayer = new GridLayer();
    private final EventLayer _eventLayer = new EventLayer();
    //private final ActivationIndicatorLayer _activationIndicator = new ActivationIndicatorLayer();
    private final Point _cursor = new Point();
    //selections
    private final HashSet<Rectangle> _selections = new HashSet<Rectangle>();
    private final MatrixCell _selectionAnchorCell = new MatrixCell();
    //The dimensions of row,col containers
    private final Point _colRowDimensions = new Point(150, 150);
    private final ColDrawer _colDrawer = new ColDrawer(_colRowDimensions, this);
    private final RowDrawer _rowDrawer = new RowDrawer(_colRowDimensions, this);
    private final ZoomControl _zoomControlX = new ZoomControl();
    private final ZoomControl _zoomControlY = new ZoomControl();
    private final Point2D.Float _zoom = new Point2D.Float(_zoomControlX.getCurrentZoom(), _zoomControlY.getCurrentZoom());
    private final MapMover _mapMover = new MapMover();
    private final ResizeListener _resizeListener = new ResizeListener();
//    private final HashSet<CViewActiveCellChangedListener> _viewActiveCellChangedListeners = new HashSet<CViewActiveCellChangedListener>();
//    private final HashSet<CViewSelectionChangedListener> _viewSelectionChangedListeners = new HashSet<CViewSelectionChangedListener>();
//    private final HashSet<CViewAnchorMovedListener> _viewAnchorMovedListeners = new HashSet<CViewAnchorMovedListener>();
    private final HashSet<CViewListener> _viewListeners = new HashSet<CViewListener>();
    private final NotificationLayer _notificationLayer = new NotificationLayer();

//    private final RealTimeLayer _realTimeLayer = new RealTimeLayer();
    public void addColumnMap(ColumnMap map) {
        _colDrawer.addColumnMap(map);
    }

    public void addRowMap(RowMap map) {
        _rowDrawer.addRowMap(map);
    }

    public void addCViewListener(CViewListener listener) {
        _viewListeners.add(listener);
    }

    public void removeCViewListener(CViewListener listener) {
        _viewListeners.remove(listener);
    }

    public boolean isGridMode() {
        return _gridLayer.isVisible();
    }

//    public void addViewAnchorMovedListener(CViewAnchorMovedListener lis) {
//        _viewAnchorMovedListeners.add(lis);
//    }
//
//    public void removeViewAnchorMovedListener(CViewAnchorMovedListener lis) {
//        _viewAnchorMovedListeners.remove(lis);
//    }
//
//    public void addCViewActiveCellChangedListener(CViewActiveCellChangedListener viewActiveCellChangedListener) {
//        _viewActiveCellChangedListeners.add(viewActiveCellChangedListener);
//    }
//
//    public void removeViewActiveCellChangedListener(CViewActiveCellChangedListener viewActiveCellChangedListener) {
//        _viewActiveCellChangedListeners.remove(viewActiveCellChangedListener);
//    }
    private boolean drawAnnotation = true;
    
    public synchronized void togglePaintAnnotation(){
        drawAnnotation = !drawAnnotation;
        _pAnnotationLayer.setRender(drawAnnotation);
        updateCanvasEnforceOverlay();
    }
    
    
    public void setRowPanelsVisible(boolean visible) {
        _rowDrawer.setVisible(visible);
    }

    public void setColumnPanelsVisible(boolean visible) {
        _colDrawer.setVisible(visible);
    }

    public boolean isRowPanelsVisible() {
        return _rowDrawer.isVisible();
    }

    public boolean isColumnPanelsVisible() {
        return _colDrawer.isVisible();
    }

    private void _fireViewActiveCellChanged(MatrixCell oldCell, MatrixCell newCell) {
//        for (CViewActiveCellChangedListener viewActiveCellChangedListener : _viewActiveCellChangedListeners) {
//            viewActiveCellChangedListener.activeCellChanged(_coolMapObject, oldCell, newCell);
//        }
        for (CViewListener listener : _viewListeners) {
            listener.activeCellChanged(_coolMapObject, oldCell, newCell);
        }
    }
//    public void addCViewSelectionChangedListener(CViewSelectionChangedListener vs) {
//        _viewSelectionChangedListeners.add(vs);
//    }
//
//    public void removeViewSelectionChangedListener(CViewSelectionChangedListener vs) {
//        _viewSelectionChangedListeners.remove(vs);
//    }
    private final ArrayList<Range<Integer>> _selectedColumns = new ArrayList<Range<Integer>>();
    private final ArrayList<Range<Integer>> _selectedRows = new ArrayList<Range<Integer>>();

    private void _fireViewSelectionChanged() {
        //update the selected rows and regions.
        _updateColRowRanges();

//        for (CViewSelectionChangedListener vs : _viewSelectionChangedListeners) {
//            vs.selectionChanged(_coolMapObject);
//        }
        for (CViewListener listener : _viewListeners) {
            listener.selectionChanged(_coolMapObject);
        }
    }

//    private void _fireSubSelectionRowChanged() {
//        for (CViewListener listener : _viewListeners) {
//            listener.subSelectionRowChanged(_coolMapObject);
//        }
//    }
//
//    private void _fireSubSelectionColumnChanged() {
//        for (CViewListener listener : _viewListeners) {
//            listener.subSelectionColumnChanged(_coolMapObject);
//        }
//    }
    public void setPopupMenu(JPopupMenu menu) {
        _eventLayer.setComponentPopupMenu(menu);
    }

    public void destroy() {
        _viewListeners.clear();
        _internalFrame.dispose();
        _canvas = null;
        _coolMapObject = null;
        _colDrawer.clearColumnMaps();
        _rowDrawer.clearRowMaps();
    }

    /**
     * update the currently selected rows and columns
     */
    private void _updateColRowRanges() {
        _selectedRows.clear();
        _selectedColumns.clear();

        Set<Rectangle> selections = getSelections();
        if (selections == null || selections.isEmpty()) {
            return;
        }

        TreeSet<Range<Integer>> rowRanges = new TreeSet<Range<Integer>>(new RangeComparator());
        TreeSet<Range<Integer>> colRanges = new TreeSet<Range<Integer>>(new RangeComparator());

        for (Rectangle selection : selections) {
            //column ranges
            Range colRange = Range.closedOpen(selection.x, selection.x + selection.width);
            colRanges.add(colRange);
            //row ranges
            Range rowRange = Range.closedOpen(selection.y, selection.y + selection.height);
            rowRanges.add(rowRange);
        }

        if (!colRanges.isEmpty()) {
            Iterator<Range<Integer>> it = colRanges.iterator();
            Range<Integer> first = it.next();
            Range<Integer> rTemp = Range.closedOpen(first.lowerEndpoint(), first.upperEndpoint());
            for (Range<Integer> r : colRanges) {
                if (rTemp.isConnected(r)) {
                    rTemp = rTemp.span(r);
                } else {
                    _selectedColumns.add(rTemp);
                    rTemp = Range.closedOpen(r.lowerEndpoint(), r.upperEndpoint());
                }
            }
            _selectedColumns.add(rTemp);
        }

        if (!rowRanges.isEmpty()) {
            Iterator<Range<Integer>> it = rowRanges.iterator();
            Range<Integer> first = it.next();
            Range<Integer> rTemp = Range.closedOpen(first.lowerEndpoint(), first.upperEndpoint());
            for (Range<Integer> r : rowRanges) {
                if (rTemp.isConnected(r)) {
                    rTemp = rTemp.span(r);
                } else {
                    _selectedRows.add(rTemp);
                    rTemp = Range.closedOpen(r.lowerEndpoint(), r.upperEndpoint());
                }
            }
            _selectedRows.add(rTemp);
        }

    }

    public ArrayList<Range<Integer>> getSelectedColumns() {
        ArrayList<Range<Integer>> selectedColumns = new ArrayList<Range<Integer>>();
        for (Range<Integer> r : _selectedColumns) {
            selectedColumns.add(Range.closedOpen(r.lowerEndpoint(), r.upperEndpoint()));
        }
        return selectedColumns;
    }

    public ArrayList<Range<Integer>> getSelectedRows() {
        ArrayList<Range<Integer>> selectedRows = new ArrayList<Range<Integer>>();
        for (Range<Integer> r : _selectedRows) {
            selectedRows.add(Range.closedOpen(r.lowerEndpoint(), r.upperEndpoint()));
        }
        return selectedRows;
    }

    /**
     * returns the outter bounds of all selections
     *
     * @return
     */
    public Rectangle getSelectionsUnion() {
        if (_selections.isEmpty()) {
            return null;
        }

        Area a = new Area();
        for (Rectangle selection : _selections) {
            a.add(new Area(selection));
        }
        if (a.isEmpty()) {
            return null;
        }

        return a.getBounds();
    }

//    public Integer getActiveRow() {
//        if (_activeCell.row == null) {
//            return null;
//        }
//        return _activeCell.row.intValue();
//    }
//
//    public Integer getActiveCol() {
//        if (_activeCell.col == null) {
//            return null;
//        }
//        return _activeCell.col.intValue();
//    }
    /**
     * gets a copy of the active cell
     *
     * @return
     */
    public MatrixCell getActiveCell() {
        return _activeCell.duplicate();
    }

    public float getZoomX() {
        return _zoom.x;
    }

    public float getZoomY() {
        return _zoom.y;
    }

    public ZoomControl getZoomControlX() {
        return _zoomControlX;
    }

    public ZoomControl getZoomControlY() {
        return _zoomControlY;
    }

    public CoolMapObject<BASE, VIEW> getCoolMapObject() {
        return _coolMapObject;
    }

    public void centerToSelections() {
        if (_selections.isEmpty()) {
            return;
        } else {
            Area area = new Area();
            for (Rectangle selection : _selections) {
                if (selection != null) {
                    area.add(new Area(selection));
                }
            }
            //get a node region //node regions
            System.out.println("Node region:" + area.getBounds());
            centerToRegion(area.getBounds());
        }
    }

    public void centerToRegion(Rectangle nodeRegion) {
        if (_coolMapObject == null || nodeRegion == null) {
            return;
        }
        Set<Rectangle> viewRegions = convertNodeRegionToViewRegion(Collections.singleton(nodeRegion));

//        System.out.println("View Region:" + viewRegions);
        if (viewRegions.isEmpty()) {
            return;
        }

        try {
            //System.out.println(nodeRegion);
            Rectangle viewRegion = (Rectangle) viewRegions.toArray()[0];
            _centerToView(viewRegion);
            Point mouse = _getMouseXY();
            setMouseXY(mouse.x, mouse.y);
            _hoverLayer.setActiveCell(_activeCell);
        } catch (Exception e) {
        }
    }

    public void centerToPercentage(float colPercent, float rowPercent) {
        try {
            //need to find these nodes
            
            
            
            VNode colNode = _findColNode(colPercent);
            VNode rowNode = _findRowNode(rowPercent);
//            System.out.println(colNode + "->" + colNode.getViewIndex());
//            System.out.println(rowNode + "->" + rowNode.getViewIndex());
            
            Rectangle nodeRegion = new Rectangle(colNode.getViewIndex().intValue(), rowNode.getViewIndex().intValue(), 1, 1);
            centerToRegion(nodeRegion);

        } catch (Exception e) {

        }

    }

    //same function for rowNode
    /**
     * returrn the column node at the relative percentage of the map
     * @param colPercentage
     * @return
     * @throws Exception 
     */
    private VNode _findColNode(float colPercentage) throws Exception {
        if (colPercentage <= 0) {
            return _coolMapObject.getViewNodeColumn(0);
        }
        if (colPercentage >= 1) {
            return _coolMapObject.getViewNodeColumn(_coolMapObject.getViewNumColumns() - 1);
        }

        VNode startNode = _coolMapObject.getViewNodeColumn(0);
        VNode endNode = _coolMapObject.getViewNodeColumn(_coolMapObject.getViewNumColumns() - 1);
        VNode currentNode;

        while (startNode.getViewIndex() != null && endNode.getViewIndex() != null
                && startNode.getViewIndex() < endNode.getViewIndex()) {

            currentNode = _coolMapObject.getViewNodeColumn(
                    Math.round(
                            (startNode.getViewIndex() + endNode.getViewIndex()) / 2)
            );

            float currentPerStart = currentNode.getViewOffset() / _mapDimension.width;
            float currentPerEnd = currentNode.getViewOffset(_zoom.x) / _mapDimension.width;

            if (colPercentage < currentPerStart) {
                //search left
                endNode = currentNode;
                continue;
            }
            if (colPercentage >= currentPerEnd) {
                startNode = currentNode;
                continue;
            }
                return currentNode;
            
        }

        throw new Exception("Searching for percentage col node failed possibly due to error");
    }

    /**
     * return the row node at the relative percentage of the map
     * @param rowPercentage
     * @return
     * @throws Exception 
     */
    private VNode _findRowNode(float rowPercentage) throws Exception {
        if (rowPercentage <= 0) {
            return _coolMapObject.getViewNodeRow(0);
        }
        if (rowPercentage >= 1) {
            return _coolMapObject.getViewNodeRow(_coolMapObject.getViewNumRows() - 1);
        }

        VNode startNode = _coolMapObject.getViewNodeRow(0);
        VNode endNode = _coolMapObject.getViewNodeRow(_coolMapObject.getViewNumRows() - 1);
        VNode currentNode;

        while (startNode.getViewIndex() != null && endNode.getViewIndex() != null
                && startNode.getViewIndex() < endNode.getViewIndex()) {

            currentNode = _coolMapObject.getViewNodeRow(
                    Math.round(
                            (startNode.getViewIndex() + endNode.getViewIndex()) / 2)
            );

            float currentPerStart = currentNode.getViewOffset() / _mapDimension.height;
            float currentPerEnd = currentNode.getViewOffset(_zoom.x) / _mapDimension.height;
            
            if (rowPercentage < currentPerStart){
                endNode = currentNode;
                continue;
            }
            if (rowPercentage >= currentPerEnd){
                startNode = currentNode;
                continue;
            }

            return currentNode;
        }
        throw new Exception("Searching for percentage row node failed possibly due to error");
    }

    /**
     * center the map, with regard to the viewport
     */
    private void _centerToView(Rectangle viewRegion) {

//        System.out.println("Center to view called");
        int centerX = (int) viewRegion.getCenterX();
        int centerY = (int) viewRegion.getCenterY();

//        simply move centerX, centerY to the center
        Rectangle viewport = _getViewportBounds();

        int viewportCenterX = (int) viewport.getCenterX();
        int viewportCenterY = (int) viewport.getCenterY();

        int xOffset = viewportCenterX - centerX; //move by these much
        int yOffset = viewportCenterY - centerY; //move by these much

        int newAnchorX = _mapDimension.x + xOffset;
        int newAnchorY = _mapDimension.y + yOffset;

        //System.out.println(centerX + " " + centerY);
        //Integer centerRow = getCurrentRow(centerY);
        //Integer centerCol = getCurrentCol(centerX);
        //These returns null because it's out
        //System.out.println("Center row:column" + centerRow + " " + centerCol);
        //MatrixCell centerCell = new MatrixCell(centerRow, centerCol);
        //centerCell.confineToValidCell(_coolMapObject);
        //VNode rowNode = _coolMapObject.getViewNodeRow(centerCell.row.intValue());
        //VNode colNode = _coolMapObject.getViewNodeColumn(centerCell.col.intValue());
        //if (rowNode == null || colNode == null) {
        //    return;
        //}
        //Rectangle viewport = _getViewportBounds();
        //_moveMapTo((int) viewport.getCenterX() - colNode.getViewOffset() - colNode.getViewSizeInMap(_zoom.x) / 2, (int) viewport.getCenterY() - rowNode.getViewOffset() - rowNode.getViewSizeInMap(_zoom.y) / 2, false);
        _moveMapTo(newAnchorX, newAnchorY, false);

        updateCanvasEnforceAll();
        _fireViewAnchorMoved();
    }

    public boolean zoomIn(boolean zoomX, boolean zoomY) {
        //figure out the center row/column
        if (_coolMapObject == null || !_coolMapObject.isViewValid() || zoomX == false && zoomY == false) {
            return false;
        }

        Rectangle viewport = _getViewportBounds();
        int centerX = (int) viewport.getCenterX();
        int centerY = (int) viewport.getCenterY();

        //System.out.println(centerX + " " + centerY);
        Integer centerRow = getCurrentRow(centerY);
        Integer centerCol = getCurrentCol(centerX);

        MatrixCell centerCell = new MatrixCell(centerRow, centerCol);
        centerCell.confineToValidCell(_coolMapObject);

        //Just need to decode certain things
        
        
        if (zoomX) {
            _zoom.x = _zoomControlX.getNextZoom();
        }

        if (zoomY) {
            _zoom.y = _zoomControlY.getNextZoom();
        }

        //force update
        updateNodeDisplayParams();
        VNode rowNode = _coolMapObject.getViewNodeRow(centerCell.row.intValue());
        VNode colNode = _coolMapObject.getViewNodeColumn(centerCell.col.intValue());
        if (rowNode == null || colNode == null) {
            return false;
        }
        //center rowNode, colNode to centerX and centerY
        //but don't update canvas
        _moveMapTo(centerX - colNode.getViewOffset() - colNode.getViewSizeInMap(_zoom.x) / 2, centerY - rowNode.getViewOffset() - rowNode.getViewSizeInMap(_zoom.y) / 2, false);

        updateCanvasEnforceAll();

        _fireViewZoomChanged();
        return true;
    }

    public void setZoomLevels(float zoomX, float zoomY) {
        System.out.println("Loaded zoom levels:" + zoomX + " " + zoomY);
        setZoomIndices(_zoomControlX.getNearestZoomIndex(zoomX), _zoomControlY.getNearestZoomIndex(zoomY));
    }

    /**
     * it will only work when the view is valid
     *
     * @param xIndex
     * @param yIndex
     */
    public void setZoomIndices(int xIndex, int yIndex) {
        if (_coolMapObject == null || !_coolMapObject.isViewValid()) {
            return;
        }

        Rectangle viewport = _getViewportBounds();
        int centerX = (int) viewport.getCenterX();
        int centerY = (int) viewport.getCenterY();

        Integer centerRow = getCurrentRow(centerY);
        Integer centerCol = getCurrentCol(centerX);

        MatrixCell centerCell = new MatrixCell(centerRow, centerCol);
        centerCell.confineToValidCell(_coolMapObject);

        _zoomControlX.setZoom(xIndex);
        _zoomControlY.setZoom(yIndex);

        _zoom.x = _zoomControlX.getCurrentZoom();
        _zoom.y = _zoomControlY.getCurrentZoom();

        updateNodeDisplayParams();
        VNode rowNode = _coolMapObject.getViewNodeRow(centerCell.row.intValue());
        VNode colNode = _coolMapObject.getViewNodeColumn(centerCell.col.intValue());
        if (rowNode == null || colNode == null) {
            return;
        }
        //center rowNode, colNode to centerX and centerY
        //but don't update canvas
        _moveMapTo(centerX - colNode.getViewOffset() - colNode.getViewSizeInMap(_zoom.x) / 2, centerY - rowNode.getViewOffset() - rowNode.getViewSizeInMap(_zoom.y) / 2, false);

        updateCanvasEnforceAll();

        _fireViewZoomChanged();

    }

    public boolean zoomOut(boolean zoomX, boolean zoomY) {
        if (_coolMapObject == null || !_coolMapObject.isViewValid() || zoomX == false && zoomY == false) {
            return false;
        }

        Rectangle viewport = _getViewportBounds();
        int centerX = (int) viewport.getCenterX();
        int centerY = (int) viewport.getCenterY();

        //System.out.println(centerX + " " + centerY);
        Integer centerRow = getCurrentRow(centerY);
        Integer centerCol = getCurrentCol(centerX);

        MatrixCell centerCell = new MatrixCell(centerRow, centerCol);
        centerCell.confineToValidCell(_coolMapObject);

        if (zoomX) {
            _zoom.x = _zoomControlX.getPreviousZoom();
        }

        if (zoomY) {
            _zoom.y = _zoomControlY.getPreviousZoom();
        }

        System.out.println("Current zooms are set to:" + _zoom);

        //force update
        //node sizes can't be smaller than 1
        updateNodeDisplayParams();

        VNode rowNode = _coolMapObject.getViewNodeRow(centerCell.row.intValue());
        VNode colNode = _coolMapObject.getViewNodeColumn(centerCell.col.intValue());
        if (rowNode == null || colNode == null) {
            return false;
        }
        //center rowNode, colNode to centerX and centerY
        //but don't update canvas

        //
        _moveMapTo(centerX - colNode.getViewOffset() - colNode.getViewSizeInMap(_zoom.x) / 2, centerY - rowNode.getViewOffset() - rowNode.getViewSizeInMap(_zoom.y) / 2, false);
        updateCanvasEnforceAll();

        _fireViewZoomChanged();
        return true;
    }

    public void setSelectionsColumnIndices(Collection<Integer> selectedIndices) {
        if (selectedIndices == null || selectedIndices.isEmpty()) {
            return;
        }
        selectedIndices.removeAll(Collections.singletonList(null));
        TreeSet<Integer> selIndices = new TreeSet<Integer>(selectedIndices);

        if (selIndices.isEmpty()) {
            return;
        }

        int startIndex = selIndices.first();
        int currentIndex = startIndex;
        HashSet<Range<Integer>> selectedRanges = new HashSet<Range<Integer>>();

        for (Integer index : selIndices) {
            if (index <= currentIndex + 1) {
                currentIndex = index;
                continue;
            } else {
                selectedRanges.add(Range.closedOpen(startIndex, currentIndex + 1));
                currentIndex = index;
                startIndex = currentIndex;
            }
        }
        selectedRanges.add(Range.closedOpen(startIndex, currentIndex + 1));
        setSelectionsColumn(selectedRanges);
    }

    public void setSelectionRowIndices(Collection<Integer> selectedIndices) {
        if (selectedIndices == null || selectedIndices.isEmpty()) {
            return;
        }
        selectedIndices.removeAll(Collections.singletonList(null));
        TreeSet<Integer> selIndices = new TreeSet<Integer>(selectedIndices);

        if (selIndices.isEmpty()) {
            return;
        }

        int startIndex = selIndices.first();
        int currentIndex = startIndex;
        HashSet<Range<Integer>> selectedRanges = new HashSet<Range<Integer>>();

        for (Integer index : selIndices) {
            if (index <= currentIndex + 1) {
                currentIndex = index;
                continue;
            } else {
                selectedRanges.add(Range.closedOpen(startIndex, currentIndex + 1));
                currentIndex = index;
                startIndex = currentIndex;
            }
        }
        selectedRanges.add(Range.closedOpen(startIndex, currentIndex + 1));
        setSelectionsRow(selectedRanges);
    }

    public void setSelectionsColumn(Collection<Range<Integer>> selectedColumns) {
        if (selectedColumns == null || selectedColumns.isEmpty()) {
            clearSelection();
            return;
        }

        ArrayList<Range<Integer>> selectedRows = getSelectedRows();
        if (selectedRows.isEmpty()) {
            selectedRows.add(Range.closedOpen(0, _coolMapObject.getViewNumRows()));
        }

        ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
        for (Range<Integer> colRange : selectedColumns) {
            for (Range<Integer> rowRange : selectedRows) {
                newSelections.add(new Rectangle(colRange.lowerEndpoint(), rowRange.lowerEndpoint(), colRange.upperEndpoint() - colRange.lowerEndpoint(), rowRange.upperEndpoint() - rowRange.lowerEndpoint()));
            }
        }
        setSelections(newSelections);
    }

    public void setSelectionsRow(Collection<Range<Integer>> selectedRows) {

//        System.out.println("Selected Rows here:" + selectedRows);

        if (selectedRows == null || selectedRows.isEmpty()) {
            clearSelection();
            return;
        }

        ArrayList<Range<Integer>> selectedColumns = getSelectedColumns();
        if (selectedColumns.isEmpty()) {
            selectedColumns.add(Range.closedOpen(0, _coolMapObject.getViewNumColumns()));
        }

        ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
        for (Range<Integer> colRange : selectedColumns) {
            for (Range<Integer> rowRange : selectedRows) {
                newSelections.add(new Rectangle(colRange.lowerEndpoint(), rowRange.lowerEndpoint(), colRange.upperEndpoint() - colRange.lowerEndpoint(), rowRange.upperEndpoint() - rowRange.lowerEndpoint()));
            }
        }

//        System.out.println("New selections:" + newSelections);
        setSelections(newSelections);
    }

    /**
     * a very specific function: the supplies nodes are nodes 'marked' to be
     * selected under the collapsed ontology nodes It's an ontological selection
     *
     * @param nodes
     */
//    private HashSet<String> _subSelectedRowNodeNames = new HashSet<String>();
//    private HashSet<String> _subSelectedColumnNodeNames = new HashSet<String>();
//
//    public void setSubSelectionRows(Set<String> nodeNames) {
//        //System.out.println("Set subselection rows called:" + nodeNames);
//
//        _subSelectedRowNodeNames.clear();
//        if (nodeNames == null || nodeNames.isEmpty()) {
//            return;
//        } else {
//            _subSelectedRowNodeNames.addAll(nodeNames);
//        }
//        _fireSubSelectionRowChanged();
//    }
//
//    public void setSubSelectionColumns(Set<String> nodeNames) {
//        _subSelectedColumnNodeNames.clear();
//        if (nodeNames == null || nodeNames.isEmpty()) {
//            return;
//        } else {
//            _subSelectedColumnNodeNames.addAll(nodeNames);
//        }
//        _fireSubSelectionColumnChanged();
//    }
//
//    public Set<String> getSubSelectedRows() {
//        return new HashSet<String>(_subSelectedRowNodeNames);
//    }
//
//    public Set<String> getSubSelectedColumns() {
//        return new HashSet<String>(_subSelectedColumnNodeNames);
//    }
    /**
     * get the anchoring point of the map all otehr node anchors are mapAnchor +
     * viewoffset
     *
     * @return
     */
    public Point getMapAnchor() {
        return new Point(_mapDimension.x, _mapDimension.y);
    }

    public float getMapWidth() {
        return _mapDimension.width;
    }

    public float getMapHeight() {
        return _mapDimension.height;
    }

    public void updateColDrawerBounds() {
        _colDrawer.updateBounds();
    }

    public void updateRowDrawerBounds() {
        _rowDrawer.updateBounds();
    }

//////////////////////////////////////////////////    
    public void addSelection(Rectangle selection) {
        addSelection(Collections.singletonList(selection));
    }

    public boolean isCellSelected(int row, int col) {
        if (row < 0 || col < 0 || row >= _coolMapObject.getViewNumRows() || col >= _coolMapObject.getViewNumColumns()) {
            return false;
        } else {
            for (Rectangle region : _selections) {
                if (region.contains(col, row)) {
                    return true;
                }
            }
            return false;
        }
    }

    public synchronized void addSelection(Collection<Rectangle> selections) {
        if (selections == null) {
            return;
        } else {
            for (Rectangle selection : selections) {
                if (_isValidRegion(selection)) {
                    _selections.add(selection);
                }
            }
            _fireViewSelectionChanged();
            _selectionLayer.updateViewArea();
        }
    }

    public void clearSelection() {
        _selections.clear();
        _selectionLayer.updateViewArea();
        _fireViewSelectionChanged();
    }

    /**
     * set the current cell as the active cell.
     *
     * @param selection
     * @param row
     * @param col
     */
//    public void setActiveCell(int row, int col) {
//        _activeCell.row(row);
//        _activeCell.col(col);
//    }
    public void setSelection(Rectangle selection) {
        _selections.clear();
        addSelection(selection);
    }

    public void setSelections(Collection<Rectangle> selections) {
        _selections.clear();
        addSelection(selections);
    }

    public void setSelection(Collection<Range<Integer>> selectedRows, Collection<Range<Integer>> selectedColumns) {
        _selections.clear();
        if (selectedRows == null || selectedColumns == null) {
            return;
        }
        if (selectedRows.isEmpty()) {
            selectedRows.add(Range.closedOpen(0, _coolMapObject.getViewNumRows()));
        }
        if (selectedColumns.isEmpty()) {
            selectedColumns.add(Range.closedOpen(0, _coolMapObject.getViewNumColumns()));
        }

        ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
        for (Range<Integer> colRange : selectedColumns) {
            for (Range<Integer> rowRange : selectedRows) {
                newSelections.add(new Rectangle(colRange.lowerEndpoint(), rowRange.lowerEndpoint(), colRange.upperEndpoint() - colRange.lowerEndpoint(), rowRange.upperEndpoint() - rowRange.lowerEndpoint()));
            }
        }

    }

//    public void setSelectedColumns(Collection<Range<Integer>> selectedColumns) {
//        setSelection(getSelectedRows(), selectedColumns);
//    }
//
//    public void setSelectedRows(Collection<Range<Integer>> selectedRows) {
//        setSelection(selectedRows, getSelectedColumns());
//    }
    /**
     * returns a copy of the selections
     */
    public Set<Rectangle> getSelections() {
        HashSet<Rectangle> selection = new HashSet<Rectangle>();
        for (Rectangle r : _selections) {
            if (r == null) {
                continue;
            }
            selection.add(new Rectangle(r.x, r.y, r.width, r.height));
        }
        return selection;
    }

/////////////////////////////////////////////////////    
    private boolean _isValidRegion(Rectangle selection) {
        if (_coolMapObject != null && selection != null && selection.x >= 0 && selection.y >= 0 && selection.width <= _coolMapObject.getViewNumColumns() && selection.height <= _coolMapObject.getViewNumRows()) {
            return true;
        }
        return false;
    }

    public Rectangle getCanvasDimension() {
        return (Rectangle) _canvas.getBounds().clone();
    }

    public synchronized void toggleGridMode(boolean on) {
        if (on) {
            _gridLayer.fadeIn();

        } else {
            _gridLayer.fadeOut();
        }
    }

    public synchronized void toggleTooltip() {
        if (paintHoverTip) {
            paintHoverTip = false;
        } else {
            paintHoverTip = true;
        }
        redrawCanvas();
    }

    public synchronized void toggleLabeltip() {
        if (paintLabelsTip) {
            paintLabelsTip = false;
        } else {
            paintLabelsTip = true;
        }
        redrawCanvas();
    }

    public synchronized void setMouseXY(int x, int y) {
        _cursor.x = x;
        _cursor.y = y;
        //The x,y will be updated here as well.
        Integer activeCol = getCurrentCol(_cursor.x);
        Integer activeRow = getCurrentRow(_cursor.y);

        //check out the old active cell.
        //change to the new cell.
        MatrixCell newCell = new MatrixCell(activeRow, activeCol);
        //System.out.println("New Cell:" + newCell + " Active Cell:" + _activeCell);
        //System.out.println(newCell.equals(_activeCell));        
        if (!newCell.equals(_activeCell)) {

            setActiveCell(_activeCell, newCell);

        }
        //System.out.println(_activeCell);
        //redrawCanvas();
    }

//    public synchronized void setMouseXYtoActiveCell(){
//        setMouseXY(_hoverLayer._hoverBounds.x, _hoverLayer._hoverBounds.y);
//    }
    public synchronized void setActiveCell(MatrixCell oldCell, MatrixCell newCell) {
        if (oldCell == null) {
            oldCell = new MatrixCell();
        }
        if (newCell == null) {
            newCell = new MatrixCell();
        }

        _hoverLayer.gridMovedTo(_activeCell, newCell);

        _fireViewActiveCellChanged(_activeCell, newCell);
        _activeCell.setValueTo(newCell);
        _selectionLayer.updateViewArea();
        //_fireActiveCellChanged(oldCell, newCell);

    }

//    private void _fireActiveCellChanged(MatrixCell oldCell, MatrixCell newCell){
//         for(ViewActiveCellChangedListener vacl : _viewActiveCellChangedListeners){
//            vacl.activeCellChanged(_coolMapObject, oldCell.row, oldCell.col, newCell.row, newCell.col);
//        }
//    }
    private synchronized Point _getMouseXY() {
        return new Point(_cursor.x, _cursor.y);
    }

    public Integer getCurrentCol(int screenX) {
        if (_coolMapObject == null || !_subMapIndexMin.isValidRange(_coolMapObject) || !_subMapIndexMax.isValidRange(_coolMapObject)) {
            return null;
        }

        VNode midNode = null;

        //problem mostlikely is that the search was from subIndex.. which are not changed
        //search from 
        int startIndex = _subMapIndexMin.col.intValue();
        int endIndex = _subMapIndexMax.col.intValue();

        //binary search, should be fast enough
//        int startIndex = 0;
//        int endIndex = _coolMapObject.getViewNumColumns();
        VNode startNode = _coolMapObject.getViewNodeColumn(startIndex);
        VNode endNode = _coolMapObject.getViewNodeColumn(endIndex - 1);

        //System.out.println(startNode + " " + endNode);
//        System.out.println("screenX" + screenX);
//        System.out.println(startNode + "><" + endNode);
        if (startNode == null || endNode == null) {
            return null;
        }

        if (startNode.getViewOffset() + _mapDimension.x > screenX) {
//            System.out.println("first column" + "screenX: " + screenX + " " + _mapDimension.x + " " + (startNode.getViewOffset(_zoom.x) + _mapDimension.x));
            return -1;//the left most node
        }

        if (endNode.getViewOffset(_zoom.x) + _mapDimension.x <= screenX) {

//            System.out.println("last column" + "screenX: " + screenX + " " + _mapDimension.x + " " +  (endNode.getViewOffset(_zoom.x) + _mapDimension.x));            
            return _coolMapObject.getViewNumColumns();
        }

        //first?
        if (startNode.getViewOffset() + _mapDimension.x <= screenX
                && startNode.getViewOffset(_zoom.x) + _mapDimension.x > screenX) {
            return startIndex;
        }

        //last?
        if (endNode.getViewOffset() + _mapDimension.x <= screenX
                && endNode.getViewOffset(_zoom.x) + _mapDimension.x > screenX) {
            return endIndex - 1; //note it's -1
        }

        int midIndex = (startIndex + endIndex) / 2;

        while (midIndex != startIndex) {
            midNode = _coolMapObject.getViewNodeColumn(midIndex);
            if (midNode.getViewOffset() + _mapDimension.x <= screenX && midNode.getViewOffset(_zoom.x) + _mapDimension.x > screenX) {
                return midIndex;
            }

            if (screenX < midNode.getViewOffset() + _mapDimension.x) {
                endIndex = midIndex;
            }
            if (screenX >= midNode.getViewOffset(_zoom.x) + _mapDimension.x) {
                startIndex = midIndex;
            }
            midIndex = (startIndex + endIndex) / 2;
        }
        //Error, nothing can be found.
        //Should not rech here.
        return null;
    }

    /**
     * find the current Col using binary search -1 means left bound cols = right
     * bound, null = undef
     *
     * @param screenX
     * @return
     */
    public Integer getCurrentColSearchAll(int screenX) {
        if (_coolMapObject == null || !_subMapIndexMin.isValidRange(_coolMapObject) || !_subMapIndexMax.isValidRange(_coolMapObject)) {
            return null;
        }

        VNode midNode = null;

        //problem mostlikely is that the search was from subIndex.. which are not changed
        //search from 
//        int startIndex = _subMapIndexMin.col.intValue();
//        int endIndex = _subMapIndexMax.col.intValue();
        //binary search, should be fast enough
        int startIndex = 0;
        int endIndex = _coolMapObject.getViewNumColumns();

        VNode startNode = _coolMapObject.getViewNodeColumn(startIndex);
        VNode endNode = _coolMapObject.getViewNodeColumn(endIndex - 1);

        //System.out.println(startNode + " " + endNode);
//        System.out.println("screenX" + screenX);
//        System.out.println(startNode + "><" + endNode);
        if (startNode == null || endNode == null) {
            return null;
        }

        if (startNode.getViewOffset() + _mapDimension.x > screenX) {
//            System.out.println("first column" + "screenX: " + screenX + " " + _mapDimension.x + " " + (startNode.getViewOffset(_zoom.x) + _mapDimension.x));
            return -1;//the left most node
        }

        if (endNode.getViewOffset(_zoom.x) + _mapDimension.x <= screenX) {

//            System.out.println("last column" + "screenX: " + screenX + " " + _mapDimension.x + " " +  (endNode.getViewOffset(_zoom.x) + _mapDimension.x));            
            return _coolMapObject.getViewNumColumns();
        }

        //first?
        if (startNode.getViewOffset() + _mapDimension.x <= screenX
                && startNode.getViewOffset(_zoom.x) + _mapDimension.x > screenX) {
            return startIndex;
        }

        //last?
        if (endNode.getViewOffset() + _mapDimension.x <= screenX
                && endNode.getViewOffset(_zoom.x) + _mapDimension.x > screenX) {
            return endIndex - 1; //note it's -1
        }

        int midIndex = (startIndex + endIndex) / 2;

        while (midIndex != startIndex) {
            midNode = _coolMapObject.getViewNodeColumn(midIndex);
            if (midNode.getViewOffset() + _mapDimension.x <= screenX && midNode.getViewOffset(_zoom.x) + _mapDimension.x > screenX) {
                return midIndex;
            }

            if (screenX < midNode.getViewOffset() + _mapDimension.x) {
                endIndex = midIndex;
            }
            if (screenX >= midNode.getViewOffset(_zoom.x) + _mapDimension.x) {
                startIndex = midIndex;
            }
            midIndex = (startIndex + endIndex) / 2;
        }
        //Error, nothing can be found.
        //Should not rech here.
        return null;
    }

    /**
     * get a copy of the current mouse location in view
     *
     * @return
     */
    public Point getMouseXY() {
        return new Point(_cursor.x, _cursor.y);
    }

    public Integer getCurrentRow(int screenY) {
        if (_coolMapObject == null || !_subMapIndexMin.isValidRange(_coolMapObject) || !_subMapIndexMax.isValidRange(_coolMapObject)) {
            return null;
        }

        VNode midNode = null;

        int startIndex = _subMapIndexMin.row.intValue();
        int endIndex = _subMapIndexMax.row.intValue();

//        int startIndex = 0;
//        int endIndex = _coolMapObject.getViewNumRows();
        VNode startNode = _coolMapObject.getViewNodeRow(startIndex);
        VNode endNode = _coolMapObject.getViewNodeRow(endIndex - 1);

        if (startNode == null || endNode == null) {
            return null;
        }

        if (startNode.getViewOffset() + _mapDimension.y > screenY) {
            return -1;//the left most node
        }

        if (endNode.getViewOffset(_zoom.y) + _mapDimension.y <= screenY) {
            return _coolMapObject.getViewNumRows();
        }

        //first?
        if (startNode.getViewOffset() + _mapDimension.y <= screenY
                && startNode.getViewOffset(_zoom.y) + _mapDimension.y > screenY) {
            return startIndex;
        }

        //last?
        if (endNode.getViewOffset() + _mapDimension.y <= screenY
                && endNode.getViewOffset(_zoom.y) + _mapDimension.y > screenY) {
            return endIndex - 1; //note it's -1
        }

        int midIndex = (startIndex + endIndex) / 2;

        while (midIndex != startIndex) {
            midNode = _coolMapObject.getViewNodeRow(midIndex);
            if (midNode.getViewOffset() + _mapDimension.y <= screenY && midNode.getViewOffset(_zoom.y) + _mapDimension.y > screenY) {
                return midIndex;
            }

            if (screenY < midNode.getViewOffset() + _mapDimension.y) {
                endIndex = midIndex;
            }
            if (screenY >= midNode.getViewOffset(_zoom.y) + _mapDimension.y) {
                startIndex = midIndex;
            }
            midIndex = (startIndex + endIndex) / 2;
        }
        //Error, nothing can be found.
        //Should not rech here.
        return null;
    }

    public Integer getCurrentRowSearchAll(int screenY) {
        if (_coolMapObject == null || !_subMapIndexMin.isValidRange(_coolMapObject) || !_subMapIndexMax.isValidRange(_coolMapObject)) {
            return null;
        }

        VNode midNode = null;

//        int startIndex = _subMapIndexMin.row.intValue();
//        int endIndex = _subMapIndexMax.row.intValue();
        int startIndex = 0;
        int endIndex = _coolMapObject.getViewNumRows();

        VNode startNode = _coolMapObject.getViewNodeRow(startIndex);
        VNode endNode = _coolMapObject.getViewNodeRow(endIndex - 1);

        if (startNode == null || endNode == null) {
            return null;
        }

        if (startNode.getViewOffset() + _mapDimension.y > screenY) {
            return -1;//the left most node
        }

        if (endNode.getViewOffset(_zoom.y) + _mapDimension.y <= screenY) {
            return _coolMapObject.getViewNumRows();
        }

        //first?
        if (startNode.getViewOffset() + _mapDimension.y <= screenY
                && startNode.getViewOffset(_zoom.y) + _mapDimension.y > screenY) {
            return startIndex;
        }

        //last?
        if (endNode.getViewOffset() + _mapDimension.y <= screenY
                && endNode.getViewOffset(_zoom.y) + _mapDimension.y > screenY) {
            return endIndex - 1; //note it's -1
        }

        int midIndex = (startIndex + endIndex) / 2;

        while (midIndex != startIndex) {
            midNode = _coolMapObject.getViewNodeRow(midIndex);
            if (midNode.getViewOffset() + _mapDimension.y <= screenY && midNode.getViewOffset(_zoom.y) + _mapDimension.y > screenY) {
                return midIndex;
            }

            if (screenY < midNode.getViewOffset() + _mapDimension.y) {
                endIndex = midIndex;
            }
            if (screenY >= midNode.getViewOffset(_zoom.y) + _mapDimension.y) {
                startIndex = midIndex;
            }
            midIndex = (startIndex + endIndex) / 2;
        }
        //Error, nothing can be found.
        //Should not rech here.
        return null;
    }

    public void activate() {
        _canvas.requestFocusInWindow();
    }

    public void setAntiAlias(boolean antiAlias) {
        _antiAlias = antiAlias;
    }
    private JInternalFrame _internalFrame;

    private void _createInternalFrame() {
        _internalFrame = new JInternalFrame(getCoolMapObject().getName(), true, true, true, true);
        _internalFrame.setSize(new Dimension(800, 600));
        _internalFrame.setLocation((int) (30 + Math.random() * 40), (int) (30 + Math.random() * 40));
        _internalFrame.add(_canvas);
        _internalFrame.setVisible(true);
        _internalFrame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

//        _internalFrame.addInternalFrameListener(new InternalFrameAdapter() {
//
//            @Override
//            public void internalFrameClosing(InternalFrameEvent ife) {
//                super.internalFrameClosing(ife);
//                
//                //Need a destroy process
//                
//            }
//
//            @Override
//            public void internalFrameActivated(InternalFrameEvent ife) {
//                super.internalFrameActivated(ife);
//                if(CoolMap.getActiveCoolMapObject() != getCoolMapObject()){
//                    CoolMap.setActiveCoolMapObject(getCoolMapObject());
//                }
//            }
//        });
    }

    public JInternalFrame getViewFrame() {
        if (_internalFrame == null) {
            _createInternalFrame();
        }
        return _internalFrame;
    }

    /**
     * returns the JComponent that displays CoolMap
     *
     * @return
     */
    public JComponent getViewCanvas() {
        return _canvas;
    }
    //Graphics configuration.
    private final static GraphicsConfiguration _graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

//    public Dimension getDimension(){
//        return new Dimension(_canvas.getWidth(), _canvas.getHeight());
//    }
    /**
     * returns the rectangle of viewport, anchors @ 0,0 returns the visible
     * viewport area
     *
     * @return
     */
    public Integer getMinRowInView() {
        if (_subMapIndexMin.row == null) {
            return null;
        } else {
            return _subMapIndexMin.row.intValue();
        }
    }

    public Integer getMinColInView() {
        if (_subMapIndexMin.col == null) {
            return null;
        } else {
            return _subMapIndexMin.col.intValue();
        }
    }

    /**
     * max sub row, exclusive
     *
     * @return
     */
    public Integer getMaxRowInView() {
        if (_subMapIndexMax.row == null) {
            return null;
        } else {
            return _subMapIndexMax.row.intValue();
        }
    }

    /**
     * max sub col, exclusive
     *
     * @return
     */
    public Integer getMaxColInView() {
        if (_subMapIndexMax.col == null) {
            return null;
        } else {
            return _subMapIndexMax.col.intValue();
        }
    }

    public boolean isAntiAliased() {
        return _antiAlias;
    }

    /**
     * returns the x,y,width,height of submapbuffer
     *
     * @return
     */
    public Rectangle getSubMapDimension() {
        return (Rectangle) _subMapDimension.clone();
    }

    private Rectangle _getViewportBounds() {
        int anchorX = 0;
        int anchorY = _colRowDimensions.y;
        int width = _canvas.getWidth() - _colRowDimensions.x;
        int height = _canvas.getHeight() - _colRowDimensions.y;

        if (!_rowDrawer.isVisible()) {
            width = _canvas.getWidth();
        }

        if (!_colDrawer.isVisible()) {
            height = _canvas.getHeight();
            anchorY = 0;
        }

        return new Rectangle(anchorX, anchorY, width, height);
    }

    private CoolMapView() {
        this(null);
    }

    private void _updateActiveColNodeViewOffsets() {
        //Just update all col nodes for simplicity
        if (_coolMapObject != null && _coolMapObject.getViewNumColumns() > 0) {
            VNode node = _coolMapObject.getViewNodeColumn(0);
            node.setViewOffset(0f);
            float xIncrement = node.getViewOffset(_zoom.x);
            for (int i = 1; i < _coolMapObject.getViewNumColumns(); i++) {
                node = _coolMapObject.getViewNodeColumn(i);
                node.setViewOffset(xIncrement);
                xIncrement += node.getViewSizeInMap(_zoom.x);
            }
            //_selectionLayer.updateViewArea();
        }
    }

    private void _updateActiveRowNodeViewOffsets() {
        //
        if (_coolMapObject != null && _coolMapObject.getViewNumRows() > 0) {
            VNode node = _coolMapObject.getViewNodeRow(0);
            node.setViewOffset(0f);
            float yIncrement = node.getViewOffset(_zoom.y);
            for (int i = 1; i < _coolMapObject.getViewNumRows(); i++) {
                node = _coolMapObject.getViewNodeRow(i);
                node.setViewOffset(yIncrement);
                yIncrement += node.getViewSizeInMap(_zoom.y);
            }
            //_selectionLayer.updateViewArea();
        }
    }

    /**
     * must be called, when nodes are added, removed, reordered or resized
     *
     */
    public void updateNodeDisplayParams() {
        //When display offsets are needed: nodes dragged, etc.
        _updateActiveColNodeViewOffsets();
        _updateActiveRowNodeViewOffsets();
        _updateMapDimension();
        _selectionLayer.updateViewArea();
    }

    public CoolMapView(CoolMapObject<BASE, VIEW> coolMapObject) {

        _coolMapObject = coolMapObject;
        //make sure the display offsets are updated.
        //
        //updateNodeDisplayParams();
        //initiaize

        _initUI();
        _initParameters();
        _initMapLayers();
        _postInit();
        //addSelection(new Rectangle(0, 0, 2, 2));
        //addSelection(new Rectangle(0, 1, 8, 10));
    }

    public void highlightNodeRegions(Set<Rectangle> nodeRegions) {
        _hilightLayer.setRegions(convertNodeRegionToViewRegion(nodeRegions));
        _hilightLayer.highlight();
    }
    /**
     * add the maplayers and or
     */
    private FilterLayer _maskLayer;
    private CoolMapLayer _coolMapLayer;
    private PointAnnotationLayer _pAnnotationLayer;

    private void _initMapLayers() {

        //This is the default render layer
        _coolMapLayer = new CoolMapLayer(_coolMapObject);
        _maskLayer = new FilterLayer();
        _pAnnotationLayer = new PointAnnotationLayer(_coolMapObject);

        addMapLayer(_coolMapLayer);
        addOverlayer(_maskLayer);
        
        //doesn't seem to be working at all
        addOverlayer(_pAnnotationLayer);
    }

    private void _postInit() {
    }

    private void _initUI() {
        //0 as the background layer
        Integer stackCounter = 0;
        _addLayer(_backdrop, stackCounter++);
        _addLayer(_mapContainer, stackCounter++);
        _addLayer(_overlayContainer, stackCounter++);

        //Fesible but not using this now
        //_addLayer(_realTimeLayer, stackCounter);
        _addLayer(_hilightLayer, stackCounter++);
        _addLayer(_selectionLayer, stackCounter++);
        _addLayer(_hoverLayer, stackCounter++);
        _addLayer(_eventLayer, stackCounter++);
        _addLayer(_notificationLayer, stackCounter++);
        _addLayer(_gridLayer, stackCounter++);
        _addLayer(_colDrawer, stackCounter++);
        _addLayer(_rowDrawer, stackCounter++);

//        _addLayer(new RealTimeLayer(), stackCounter++);
//        _rowDrawer.setEnabled(true);
//        SampleColumnMap scm1 = new SampleColumnMap(_coolMapObject);
//        scm1.setName("Sample Map 1");
//        _colDrawer.addColumnMap(scm1);
//        SampleColumnMap scm2 = new SampleColumnMap(_coolMapObject);
//        scm2.setName("Sample Map 2");
//        _colDrawer.addColumnMap(scm2);
//
//        SampleColumnMap scm3 = new SampleColumnMap(_coolMapObject);
//        scm3.setName("Sample Map 3");
//        _colDrawer.addColumnMap(scm3);
//        _colDrawer.clearColumnMaps();
//           
//        scm1 = new SampleColumnMap(this);
//        scm1.setName("Sample Map 1");
//        _colDrawer.addColumnMap(scm1);
//        
//        scm2 = new SampleColumnMap(this);
//        scm2.setName("Sample Map 2");
//        _colDrawer.addColumnMap(scm2);
//        _rowDrawer.addRowMap(new SampleRowMap(_coolMapObject));
//        _rowDrawer.addRowMap(new SampleRowMap(_coolMapObject));
//        _rowDrawer.addRowMap(new SampleRowMap(_coolMapObject));
//        _rowDrawer.clearRowMaps();
//
//        SampleRowMap srm = new SampleRowMap(_coolMapObject);
//        srm.setName("SR1");
//        _rowDrawer.addRowMap(srm);
//
//        srm = new SampleRowMap(_coolMapObject);
//        srm.setName("SR2");
//        _rowDrawer.addRowMap(srm);
        //Not useful.
//        _addLayer(_activationIndicator, stackCounter++);
        _addLayer(_progressMask, stackCounter++);
        _canvas.setFocusable(true);
        _canvas.setRequestFocusEnabled(true);
        _canvas.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent fe) {
                _isKeyboardActive = true;
                _notificationLayer.repaint();
            }

            @Override
            public void focusLost(FocusEvent fe) {
                //System.out.println("Focus lost");
                _isKeyboardActive = false;
                _notificationLayer.repaint();
            }
        });

        //Listeners
        _canvas.addComponentListener(_resizeListener);
        //_canvas.addFocusListener(_activationIndicator);
        setDefaultMouseListenersEnabled(true);
        setDefaultKeyListenersEnabled(true);

    }

    public synchronized void setDefaultMouseListenersEnabled(boolean enabled) {
        if (enabled) {
            MouseWheelListener[] listenersW = _eventLayer.getMouseWheelListeners();
            if (listenersW == null || listenersW.length == 0) {
                _eventLayer.addMouseWheelListener(_mapMover);
            } else {
                HashSet<MouseWheelListener> mwls = new HashSet<MouseWheelListener>(Arrays.asList(listenersW));
                if (!mwls.contains(_mapMover)) {
                    _eventLayer.addMouseWheelListener(_mapMover);
                }
            }

            MouseMotionListener[] listenersM = _eventLayer.getMouseMotionListeners();
            if (listenersM == null || listenersM.length == 0) {
                _eventLayer.addMouseMotionListener(_eventLayer);
            } else {
                HashSet<MouseMotionListener> mwls = new HashSet<MouseMotionListener>(Arrays.asList(listenersM));
                if (!mwls.contains(_eventLayer)) {
                    _eventLayer.addMouseMotionListener(_eventLayer);
                }
            }

            MouseListener[] listeners = _eventLayer.getMouseListeners();
            if (listeners == null || listeners.length == 0) {
                _eventLayer.addMouseListener(_eventLayer);
            } else {
                HashSet<MouseListener> mwls = new HashSet<MouseListener>(Arrays.asList(listeners));
                if (!mwls.contains(_eventLayer)) {
                    _eventLayer.addMouseListener(_eventLayer);
                }
            }
        } else {
            _eventLayer.removeMouseWheelListener(_mapMover);
            _eventLayer.removeMouseMotionListener(_eventLayer);
            _eventLayer.removeMouseListener(_eventLayer);
        }

    }

    public synchronized void setDefaultKeyListenersEnabled(boolean enabled) {
        if (enabled) {
            KeyListener[] listenersW = _canvas.getKeyListeners();
            if (listenersW == null || listenersW.length == 0) {
                _canvas.addKeyListener(_mapMover);
            } else {
                HashSet<KeyListener> mwls = new HashSet<KeyListener>(Arrays.asList(listenersW));
                if (!mwls.contains(_mapMover)) {
                    _canvas.addKeyListener(_mapMover);
                }
            }
        } else {
            _canvas.removeKeyListener(_mapMover);
        }
    }

    private void _initParameters() {
        _mapDimension.x = 50;
        _mapDimension.y = 180;
    }

    /**
     * Difference from before: now it's [inclusive, exclusive)
     */
    private synchronized boolean _computeSubMapParams(final MatrixCell subMapIndexMin, final MatrixCell subMapIndexMax, final Rectangle subMapDimension) {

        //System.out.println(_coolMapObject.getViewNumRows() + "--" + _coolMapObject.getViewNumCols());
        int viewportWidth = _canvas.getWidth();
        int viewportHeight = _canvas.getHeight();

        if (viewportHeight == 0 || viewportWidth == 0) {
            return false;
        }

        if (_coolMapObject == null) {
            return false;
        }

        //compute col params
        if (_mapDimension.x >= -_subMapBufferSize && _mapDimension.x <= viewportWidth + _subMapBufferSize) {
            //The first column is within view
            subMapIndexMin.col(0);
            boolean found = false;
            VNode node;
            for (int i = 1; i < _coolMapObject.getViewNumColumns(); i++) {
                node = _coolMapObject.getViewNodeColumn(i);
                if (node == null || node.getViewOffset() == null) {
                    return false;
                }
                if (node.getViewOffset() + _mapDimension.x > _subMapBufferSize + viewportWidth) {
                    found = true;
                    subMapIndexMax.col(i);
                    break;
                }
            }
            if (!found) {
                subMapIndexMax.col(_coolMapObject.getViewNumColumns());
            }
        } else if (_mapDimension.x < -_subMapBufferSize) {
            boolean foundStart = false;
            boolean foundEnd = false;
            VNode nodePrev, node;
            for (int i = 1; i < _coolMapObject.getViewNumColumns(); i++) {
                nodePrev = _coolMapObject.getViewNodeColumn(i - 1);
                node = _coolMapObject.getViewNodeColumn(i);
                if (node == null || nodePrev == null) {
                    return false;
                }
                if (!foundStart && _mapDimension.x + nodePrev.getViewOffset() < -_subMapBufferSize && _mapDimension.x + node.getViewOffset() >= -_subMapBufferSize) {
                    subMapIndexMin.col(i - 1);
                    foundStart = true;
                }
                if (foundStart && !foundEnd && _mapDimension.x + node.getViewOffset() > viewportWidth + _subMapBufferSize) {
                    subMapIndexMax.col(i);
                    foundEnd = true;
                    break;
                }
            }//End of loop col nodes
            if (!foundStart) {
                //Don't draw anything if it's null.
                subMapIndexMin.col(null);
                subMapIndexMax.col(null);
            }
            if (foundStart && !foundEnd) {
                subMapIndexMax.col(_coolMapObject.getViewNumColumns());
            }
        } else {
            //the map is to the rightmost
            subMapIndexMin.col(null);
            subMapIndexMax.col(null);
        }//end of update min, max col

        //update col dimension
        if (subMapIndexMin.col != null && subMapIndexMax.col != null) {
            VNode minColNode = _coolMapObject.getViewNodeColumn(subMapIndexMin.col.intValue());
            VNode maxColNode = _coolMapObject.getViewNodeColumn(subMapIndexMax.col.intValue() - 1);
            if (minColNode == null || maxColNode == null) {
                subMapDimension.x = _mapDimension.x - 1;
                subMapDimension.width = 0;
            } else {
                subMapDimension.x = (int) (minColNode.getViewOffset() + _mapDimension.x);
                //_subMapDimension.width = (int)(maxColNode.getViewOffset() - minColNode.getViewOffset() + maxColNode.getViewSize(_zoom.x));
                subMapDimension.width = VNode.distanceInclusive(minColNode, maxColNode, _zoom.x);
            }
        } else {
            subMapDimension.x = _mapDimension.x - 1; //smaller than the minimal mapAnchor, invalid
            subMapDimension.width = 0;
        }

////////////////////////////////////////////////////////////////////////////////
        if (_mapDimension.y >= -_subMapBufferSize && _mapDimension.y <= viewportHeight + _subMapBufferSize) {
            subMapIndexMin.row(0);
            boolean found = false;
            VNode node;
            for (int i = 1; i < _coolMapObject.getViewNumRows(); i++) {
                node = _coolMapObject.getViewNodeRow(i);
                if (node == null || node.getViewOffset() == null) {
                    return false;
                }
                //System.out.println("node:" + node);
                if (node.getViewOffset() + _mapDimension.y > _subMapBufferSize + viewportHeight) {
                    found = true;
                    subMapIndexMax.row(i);
                    break;
                }
            }
            if (!found) {
                subMapIndexMax.row(_coolMapObject.getViewNumRows());
            }
        } else if (_mapDimension.y < -_subMapBufferSize) {
            boolean foundStart = false;
            boolean foundEnd = false;
            VNode nodePrev, node;
            for (int i = 1; i < _coolMapObject.getViewNumRows(); i++) {
                nodePrev = _coolMapObject.getViewNodeRow(i - 1);
                node = _coolMapObject.getViewNodeRow(i);
                if (node == null || nodePrev == null) {
                    return false;
                }
                if (!foundStart && _mapDimension.y + nodePrev.getViewOffset() < -_subMapBufferSize && _mapDimension.y + node.getViewOffset() >= -_subMapBufferSize) {
                    subMapIndexMin.row(i - 1);
                    foundStart = true;
                }
                if (foundStart && !foundEnd && _mapDimension.y + node.getViewOffset() > viewportHeight + _subMapBufferSize) {
                    subMapIndexMax.row(i);
                    foundEnd = true;
                    break;
                }
            }

            if (!foundStart) {
                subMapIndexMin.row(null);
                subMapIndexMax.row(null);
            }

            if (foundStart && !foundEnd) {
                subMapIndexMax.row(_coolMapObject.getViewNumRows());
            }
        } else {
            subMapIndexMin.row(null);
            subMapIndexMax.row(null);
        }

        if (subMapIndexMin.row != null && subMapIndexMax.row != null) {
            VNode minRowNode = _coolMapObject.getViewNodeRow(subMapIndexMin.row.intValue());
            VNode maxRowNode = _coolMapObject.getViewNodeRow(subMapIndexMax.row.intValue() - 1);
            if (minRowNode == null || maxRowNode == null) {
                subMapDimension.y = _mapDimension.y - 1; //make it invalid
                subMapDimension.height = 0;
            } else {

                subMapDimension.y = (int) (minRowNode.getViewOffset() + _mapDimension.y);
                subMapDimension.height = VNode.distanceInclusive(minRowNode, maxRowNode, _zoom.y);
            }
        } else {
            //The only time that the height is -1 is here.
            subMapDimension.y = _mapDimension.y - 1; //make it invalid
            subMapDimension.height = 0;
        }
        //////////////////////////////////////////////////////////////////////////////////////
        //submap dimension x, y, width, height, all updated.
//        System.out.println("Update params");
//        System.out.println(subMapIndexMin);
//        System.out.println(subMapIndexMax);

        //objects updated:
        //subMapDimension
        //subMapIndexMin
        //subMapIndexMax
        //System.out.println(subMapDimension + "-->" + _mapDimension);
        
        //now, here the submapdimension is correct actually
//        System.out.println(subMapIndexMin + "===" + subMapIndexMax + "===" + subMapDimension);
        
        return true;
    }

    /**
     * reposition the map at x,y. Redraw buffers if needed if update is false,
     * the canvas is not updated, but only anchor reset.
     *
     * @param x
     * @param y
     */
    private synchronized void _moveMapTo(float newX, float newY, boolean update) {

        //System.out.println("Move map to called");
        float oldX = _mapDimension.x;
        float oldY = _mapDimension.y;
        float diffX = newX - oldX;
        float diffY = newY - oldY;

        _mapDimension.x = (int) newX;
        _mapDimension.y = (int) newY;
        _subMapDimension.x += diffX;
        _subMapDimension.y += diffY;

//        System.out.println("MapDimension Updated");
//Redraw if needed.
        if (update) {
            updateCanvasIfNecessary();
            _fireViewAnchorMoved();
        }

        //map was moved. justify the moving parts.
        //this could only be a quick plot, not hte buffer
//        _rowDrawer.justifyView();
//        _colDrawer.justifyView();
        _justifyView();
    }

    private void _fireViewAnchorMoved() {
//        for (CViewAnchorMovedListener lis : _viewAnchorMovedListeners) {
//            lis.mapAnchorMoved(_coolMapObject);
//        }
        //System.out.println("Map anchor changed");
        for (CViewListener listener : _viewListeners) {
            //System.out.println(listener);
            listener.mapAnchorMoved(_coolMapObject);
        }
    }

    private void _fireViewZoomChanged() {
//        for (CViewAnchorMovedListener lis : _viewAnchorMovedListeners) {
//            lis.mapAnchorMoved(_coolMapObject);
//        }
        //System.out.println("Map anchor changed");
        for (CViewListener listener : _viewListeners) {
            //System.out.println(listener);
            listener.mapZoomChanged(_coolMapObject);
        }
    }

    private void _fireGridChanged() {
        for (CViewListener listener : _viewListeners) {
            listener.gridChanged(_coolMapObject);
        }
    }

    public synchronized void moveMapTo(float newX, float newY) {
        _moveMapTo(newX, newY, true);
    }

    /**
     * shift map by some offsets
     *
     * @param offsetX
     * @param offsetY
     */
    public synchronized void moveMapBy(float offsetX, float offsetY) {
        _moveMapBy(offsetX, offsetY, true);
    }

    private synchronized void _moveMapBy(float offsetX, float offsetY, boolean updateMap) {

//        System.out.println("Move map by called");
        _mapDimension.x += offsetX;
        _mapDimension.y += offsetY;
        _subMapDimension.x += offsetX;
        _subMapDimension.y += offsetY;
//        System.out.println("Map dimension updated in moveMapBy, and is definitely before mapAnchor fire event");

        if (updateMap) {

            updateCanvasIfNecessary();
            _fireViewAnchorMoved();
        }

        _justifyView();
    }

    private void _assignSubMapParams(final MatrixCell subMapIndexMin, final MatrixCell subMapIndexMax, final Rectangle subMapDimension) {
        _subMapIndexMin.row = subMapIndexMin.row;
        _subMapIndexMax.row = subMapIndexMax.row;
        _subMapIndexMin.col = subMapIndexMin.col;
        _subMapIndexMax.col = subMapIndexMax.col;
        _subMapDimension.x = subMapDimension.x;
        _subMapDimension.y = subMapDimension.y;
        _subMapDimension.width = subMapDimension.width;
        _subMapDimension.height = subMapDimension.height;
    }

    /**
     * update the width and height of the map
     */
    private void _updateMapDimension() {
        VNode lastNode;
        if (_coolMapObject.getViewNumRows() > 0) {
            lastNode = _coolMapObject.getViewNodeRow(_coolMapObject.getViewNumRows() - 1);
            if (lastNode != null) {
                _mapDimension.height = (int) (lastNode.getViewOffset() + lastNode.getViewSizeInMap(_zoom.y));
            } else {
                _mapDimension.height = -1;
            }
        } else {
            _mapDimension.height = -1;
        }

        if (_coolMapObject.getViewNumColumns() > 0) {
            lastNode = _coolMapObject.getViewNodeColumn(_coolMapObject.getViewNumColumns() - 1);
            if (lastNode != null) {
                _mapDimension.width = (int) (lastNode.getViewOffset() + lastNode.getViewSizeInMap(_zoom.x));
            } else {
                _mapDimension.width = -1;
            }
        } else {
            _mapDimension.width = -1;
        }

    }

    private boolean _isReRenderNeeded() {

        //boolean reRenderNeeded =
        if (_subMapIndexMin.row == null || _subMapIndexMax.row == null || _subMapIndexMin.col == null || _subMapIndexMax.col == null) {
            return true;
        }

        return _subMapDimension.x > 0 && _subMapIndexMin.col > 0 //_submap already x is insufficient
                || _subMapDimension.y > 0 && _subMapIndexMin.row > 0 //_submap y is insufficient
                || _subMapDimension.x + _subMapDimension.width < _canvas.getWidth() && _subMapIndexMax.col < _coolMapObject.getViewNumColumns() //right is insufficient
                || _subMapDimension.y + _subMapDimension.height < _canvas.getHeight() && _subMapIndexMax.row < _coolMapObject.getViewNumRows();

    }

    private void _addLayer(JComponent component, Integer stackIndex) {
        _layers.add(component);
//        System.out.println(component);
//        System.out.println(stackIndex);
        _canvas.add(component, stackIndex);
    }

    /**
     * reset the width and height of view nodes
     */
    public void resetNodeWidth() {
        VNode node;
        for (int i = 0; i < _coolMapObject.getViewNumRows(); i++) {
            node = _coolMapObject.getViewNodeRow(i);
            if (node != null) {
                node.resetViewMultiplier();
            }
        }
        for (int i = 0; i < _coolMapObject.getViewNumColumns(); i++) {
            node = _coolMapObject.getViewNodeColumn(i);
            if (node != null) {
                node.resetViewMultiplier();
            }
        }
        updateNodeDisplayParams();
        updateCanvasEnforceAll();
    }

    public void resetNodeWidthRow() {
        VNode node;
        for (int i = 0; i < _coolMapObject.getViewNumRows(); i++) {
            node = _coolMapObject.getViewNodeRow(i);
            if (node != null) {
                node.resetViewMultiplier();
            }
        }
        _fireGridChanged();
        updateNodeDisplayParams();
        updateCanvasEnforceAll();
    }

    public void resetNodeWidthColumn() {
        VNode node;
        for (int i = 0; i < _coolMapObject.getViewNumColumns(); i++) {
            node = _coolMapObject.getViewNodeColumn(i);
            if (node != null) {
                node.resetViewMultiplier();
            }
        }
        _fireGridChanged();
        updateNodeDisplayParams();
        updateCanvasEnforceAll();

    }
//    public void resetTreeRowNodeDisplayMultipliers(){
//        for(VNode node : _activeRowNodesInTree){
//            if(node != null){
//                node.resetViewMultiplier();
//            }
//        }
//    }
//    
//    public void resetTreeColNodeDisplayMultipliers(){
//        for(VNode node : _activeColNodesInTree){
//            if(node != null){
//                node.resetViewMultiplier();
//            }
//        }
//    }
    /**
     * redraws the current canvas only allows 1 to run at a time also possibly
     * update buffers if needed
     *
     * hilihgt the noderegions after update
     *
     * update canvas, and alternatively, highlight the underlying nodeRegions
     *
     *
     */
    private volatile boolean _forceUpdateNeeded = false;

    public synchronized void updateCanvas(Set<Rectangle> nodeRegions, boolean forceUpdateAll, boolean forceUpdateOverlay) {

        if (_coolMapObject == null || _coolMapObject.getAggregator() == null) {
            return;
        }

        //stop early.
        if (forceUpdateAll == false && forceUpdateOverlay == false && !_isReRenderNeeded()) {
            redrawCanvas();
            return;
        }

        if (forceUpdateAll == true) {
            _forceUpdateNeeded = true;
        }

        //The actual update 
        if (Thread.currentThread().isInterrupted()) {
            redrawCanvas();
            return;
        }

        if (_updateBufferWorker != null && _updateBufferWorker.isAlive()) {
            //System.out.println("Interrupted");
            //If it is alive, it will then be interruped. Only the last invocation is performed.
            //Other tasks will be submitted to executors later.
            _updateBufferWorker.interrupt();//Force it to return.
        }

        if (nodeRegions != null && !nodeRegions.isEmpty()) {
            _hilightLayer.setRegions(convertNodeRegionToViewRegion(nodeRegions));
        } else {
            _hilightLayer.setRegions(null);
        }

        _selectionLayer.updateViewArea();


        /*
         * The update worker itself will redraw canvas when it's done
         */
//        System.out.println("update overlay?" + forceUpdateOverlay);
        _updateBufferWorker = new UpdateBufferWorker(forceUpdateAll, forceUpdateOverlay);

        //covert the nodeRegions into actual regions
        //assign highlight
        //If regions need to be updated, add them here.        
        _updateBufferWorker.start();

        /*
         * Another thread then, is needed to determine whether the mask is
         * needed to be shown.
         */
        /*
         * Note that update canvas is only allowed to be called once at a time.
         */
        /*
         * other computations may also be needed
         */
    }

    /**
     * converts rectangles from node selection regions, to view plotting
     * regions, anchored at 0,0
     *
     * @param nodeRegions
     * @return
     */
    public Set<Rectangle> convertNodeRegionToViewRegion(Set<Rectangle> nodeRegions) {
        if (_coolMapObject == null) {
            return new HashSet<Rectangle>(0);
        }
        if (nodeRegions != null && !nodeRegions.isEmpty()) {
            HashSet<Rectangle> viewRegions = new HashSet<Rectangle>(nodeRegions.size());
            for (Rectangle nodeRegion : nodeRegions) {
                if (!_isValidRegion(nodeRegion)) {
                    continue;
                }

                int minRowNodeIndex = nodeRegion.y;
                int minColNodeIndex = nodeRegion.x;
                int maxRowNodeIndex = nodeRegion.y + nodeRegion.height - 1;
                int maxColNodeIndex = nodeRegion.x + nodeRegion.width - 1;

                VNode minRowNode = _coolMapObject.getViewNodeRow(minRowNodeIndex);
                VNode minColNode = _coolMapObject.getViewNodeColumn(minColNodeIndex);
                VNode maxRowNode = _coolMapObject.getViewNodeRow(maxRowNodeIndex);
                VNode maxColNode = _coolMapObject.getViewNodeColumn(maxColNodeIndex);

                if (minRowNode == null || minColNode == null || maxRowNode == null || maxColNode == null) {
                    continue;
                }

                Rectangle viewRegion = new Rectangle();
                viewRegion.x = (int) (minColNode.getViewOffset() + _mapDimension.x);
                viewRegion.y = (int) (minRowNode.getViewOffset() + _mapDimension.y);
                viewRegion.width = (int) (maxColNode.getViewOffset(_zoom.x) - minColNode.getViewOffset());
                viewRegion.height = (int) (maxRowNode.getViewOffset(_zoom.y) - minRowNode.getViewOffset());
                viewRegions.add(viewRegion);

//                if(viewRegion.width == 0 || viewRegion.height == 0){
//                    System.out.println("Error region");
//                }
            }
            return viewRegions;
        } else {
            return new HashSet<Rectangle>(0);
        }
    }

    /**
     * update all when it's necessary
     */
    public synchronized void updateCanvasIfNecessary() {
        updateCanvas(null, false, false);
    }

    /**
     * force to update everything note: if enforce all is called but
     * interrupted, next call to udpates must be at least one full enforce all.
     */
    public synchronized void updateCanvasEnforceAll() {

        updateCanvas(null, true, false);
    }

    public synchronized void updateRowMapBuffersEnforceAll() {
        _rowDrawer.updateDrawerBuffers();
    }

    public synchronized void updateColumnMapBuffersEnforceAll() {
        _colDrawer.updateDrawerBuffers();
    }

    /**
     * update all if needed, but enforce update the overlay layer (respond to
     * selection change) if necessary
     *
     * not for selections, but for mask
     */
    public synchronized void updateCanvasEnforceOverlay() {

//        System.out.println("Update overlay");
        updateCanvas(null, false, true);
    }

    /**
     * repaint the entire canvas area
     */
    public synchronized void redrawCanvas() {

        _canvas.repaint();
    }

    public void addMapLayer(MapLayer mapLayer) {
        if (mapLayer != null) {
            _mapLayers.add(mapLayer);
        }
    }

    public void addOverlayer(MapLayer overLayer) {
        if (overLayer != null) {
            _overLayers.add(overLayer);
        }
    }

//Private classes-------------------------------------------------------------------------------------------------------------------    
    private class Backdrop extends JPanel {

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);
            Graphics2D g = (Graphics2D) grphcs.create();

            g.setColor(UI.colorBlack2);
            g.fillRect(0, 0, getWidth(), getHeight());
            //System.out.println("Backdrop painted" + getWidth() + " " + getHeight());
            if (_antiAlias) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            if (_coolMapObject != null && _coolMapObject.getViewNumRows() > 0 && _coolMapObject.getViewNumColumns() > 0) {
                //Draw a frame/shadow around the rectangle, only if it's in view.
                //System.out.println("Map Dimension:" + _mapDimension);
                if (_mapDimension.intersects(getCanvasDimension())) {
                    //draw a bounding box
                    g.setColor(UI.colorBlack5);
                    g.setStroke(UI.stroke8);
                    g.drawRoundRect(_mapDimension.x - 8, _mapDimension.y - 8, _mapDimension.width + 16, _mapDimension.height + 16, 10, 10);

                    g.setColor(UI.colorWhite);
                    g.setStroke(UI.stroke4);
                    g.drawRoundRect(_mapDimension.x - 4, _mapDimension.y - 4, _mapDimension.width + 8, _mapDimension.height + 8, 10, 10);

                }
            }

        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Overlay updates with selection. MapContainer does not.

    //Overlay layer will update everytime with the selection change.
    //If it needs to be replotted with selection change, use overlay
    //If not, use map
    private class OverlayContainer extends JPanel {

        private BufferedImage _buffer = null;

        public void setBufferImage(BufferedImage image) {
            _buffer = image;
        }

        public OverlayContainer() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);
            Graphics2D g = (Graphics2D) grphcs.create();
            if (_buffer != null) {
                g.drawImage(_buffer, _subMapDimension.x, _subMapDimension.y, _canvas);
            }
        }

        /**
         * update the map buffers using current info
         */
        public synchronized void updateMapBuffers(MatrixCell subMapIndexMin, MatrixCell subMapIndexMax, Rectangle subMapDimension) throws Exception {

//            System.out.println("Updating overlay... what?" + subMapIndexMin + " " + subMapIndexMax);
            if (!subMapIndexMin.isValidRange(_coolMapObject) || !subMapIndexMax.isValidRange(_coolMapObject)) {
                _buffer = null;
                return;
            }

//            System.out.println("Updating overlay... what 2?");
            if (subMapDimension.width <= 0 || subMapDimension.height <= 0) {
                _buffer = null;
                return;
            }

//            System.out.println("Updating overlay...");
            
//            System.out.println("In overlay container:" + subMapDimension);
            
            BufferedImage buffer = _graphicsConfiguration.createCompatibleImage(subMapDimension.width, subMapDimension.height, Transparency.TRANSLUCENT);
            Graphics2D g2D = buffer.createGraphics();

            if (_antiAlias) {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

//            g2D.setColor(Color.BLACK);
//            g2D.clearRect(0, 0, getWidth(), getHeight());
            //render maplayer into buffer
            
//            System.out.println("Number of overlayer:" +_overLayers.size());
            for (MapLayer mapLayer : _overLayers) {

                
                //change to selections
                //The only difference here is that only selection will be updated.
                mapLayer.render(g2D, _coolMapObject, subMapIndexMin.row.intValue(), subMapIndexMax.row.intValue(), subMapIndexMin.col.intValue(), subMapIndexMax.col.intValue(), _zoom.x, _zoom.y, subMapDimension.width, subMapDimension.height);

                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
            g2D.dispose();
            //If interrupted, then map buffer will not be updated.
            //The only possible way to interrupt is from another thread that also wants to update.
            if (Thread.currentThread().isInterrupted()) {
                //no update if interrupted.
                return;
            }
            //Only when it's not interrupted.
            _buffer = buffer;
        }
    }

    private class MapContainer extends JPanel {

        /**
         * return the current buffer.
         */
        public BufferedImage getBufferImage() {
            return _buffer;
        }

        /**
         * replace the current buffer image with something new. can be an
         * intermediate proxy or something.
         *
         * @param image
         */
        public void setBufferImage(BufferedImage image) {
            _buffer = image;
        }
        private BufferedImage _buffer = null;

        public MapContainer() {
            //Required for it to be transparent.
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);
            Graphics2D g = (Graphics2D) grphcs.create();
            if (_buffer != null) {
                g.drawImage(_buffer, _subMapDimension.x, _subMapDimension.y, _canvas);
            }
        }

        /**
         * update the map buffers using current info use intermediate
         * parameters, so that the function can be interrupted.
         */
        public synchronized void updateMapBuffers(MatrixCell subMapIndexMin, MatrixCell subMapIndexMax, Rectangle subMapDimension) throws Exception {

            //first do a check.
            //if any of the parameters are invalid, return.
            //Make sure the ranges are valid
            if (!subMapIndexMin.isValidRange(_coolMapObject) || !subMapIndexMax.isValidRange(_coolMapObject)) {
                _buffer = null;
                return;
            }

            //Make sure the submap dimensions are valid, specifically width and height > 0
            if (subMapDimension.width <= 0 || subMapDimension.height <= 0) {
                _buffer = null;
                return;
            }

            BufferedImage buffer = _graphicsConfiguration.createCompatibleImage(subMapDimension.width, subMapDimension.height);
            Graphics2D g2D = buffer.createGraphics();

            //Clear again.
            if (_antiAlias) {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            //clear rect
            g2D.setColor(Color.BLACK);
            g2D.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());

            //render maplayer into buffer
            for (MapLayer mapLayer : _mapLayers) {
                //Draw directly ontop without using any buffers.-> not entirely correct though; it's drawing on the buffer.
                //if (mapLayer.isDirectDraw()) {

                //Render to buffer.
                //Must be careful though.
                //The API is quite redundant for 
                mapLayer.render(g2D, _coolMapObject, subMapIndexMin.row.intValue(), subMapIndexMax.row.intValue(), subMapIndexMin.col.intValue(), subMapIndexMax.col.intValue(), _zoom.x, _zoom.y, subMapDimension.width, subMapDimension.height);

                //} else {
                //    g2D.drawImage(mapLayer.getRenderedImage(_coolMapObject, subMapIndexMin.row, subMapIndexMin.col, subMapIndexMax.row, subMapIndexMax.col, subMapDimension.width, subMapDimension.height), 0, 0, null);
                //}
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
            g2D.dispose();
            //If interrupted, then map buffer will not be updated.
            //The only possible way to interrupt is from another thread that also wants to update.
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            //Only when it's not interrupted.
            _buffer = buffer;

        }
    }

    private Rectangle _generateNodeRegion(MatrixCell n1, MatrixCell n2) {

        if (n1 == null || n2 == null) {
            return null;
        }
        MatrixCell a1 = n1.duplicate();
        a1.confineToValidCell(_coolMapObject);
        MatrixCell a2 = n2.duplicate();
        a2.confineToValidCell(_coolMapObject);

        VNode rn1 = _coolMapObject.getViewNodeRow(a1.row.intValue());
        VNode rn2 = _coolMapObject.getViewNodeRow(a2.row.intValue());
        VNode cn1 = _coolMapObject.getViewNodeColumn(a1.col.intValue());
        VNode cn2 = _coolMapObject.getViewNodeColumn(a2.col.intValue());

        if (rn1 == null || rn2 == null || cn1 == null || cn2 == null) {
            return null;
        }

        Rectangle region = new Rectangle();
        if (a1.col < a2.col) {
            region.x = a1.col.intValue();
            region.width = a2.col.intValue() - a1.col.intValue() + 1;
        } else {
            region.x = a2.col.intValue();
            region.width = a1.col.intValue() - a2.col.intValue() + 1;
        }
        if (a1.row < a2.row) {
            region.y = a1.row.intValue();
            region.height = a2.row.intValue() - a1.row.intValue() + 1;
        } else {
            region.y = a2.row.intValue();
            region.height = a1.row.intValue() - a2.row.intValue() + 1;
        }

        //System.out.println("generated region:" + region);
        return region;
    }
    private boolean _initialized = false;

    private class ResizeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent ce) {
            Rectangle newBounds = new Rectangle(0, 0, _canvas.getWidth(), _canvas.getHeight());
            for (JComponent component : _layers) {
                //override setBounds if other operations are needed
                component.setBounds(newBounds);
            }
            //Must update the buffers if needed
            if (!_initialized) {
                _initialized = true;

                _colDrawer.updateDrawerHeight();
                _rowDrawer.updateDrawerHeight();

                updateCanvasEnforceAll();
            } else {
                updateCanvasIfNecessary();
            }

        }

        @Override
        public void componentMoved(ComponentEvent ce) {
        }

        @Override
        public void componentShown(ComponentEvent ce) {
//            System.out.println("Component Shown");
        }

        @Override
        public void componentHidden(ComponentEvent ce) {
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////////////

    private final class GridLayer extends JPanel implements MouseListener, MouseMotionListener {

        private Color _defaultGridColor = Color.WHITE;
        private Color _gridColor = null;
        private Color _defaultBackgroundColor = UI.mixOpacity(UI.colorRedWarning, 0.5f);
        private Color _backgroundColor = null;
        private Color _labelBackgroundColor = null;
        private Color _defaultLabelBackgroundColor = UI.colorGrey2;
        private Color _fontColor = null;
        private Color _defaultFontColor = UI.colorBlack2;
        private Font _labelFont = null;
        private int _handleLength = 20;
        private int _labelCornerRaidus = 12;
        private String _label = "Resize Grid Mode";
        private float _detectionRadius = 1.25f;
        //resize parameters
        private Integer _dragStartCol = null;
        private Integer _dragStartRow = null;
        private Integer _dragStartX = null;
        private Integer _dragStartY = null;
        private int _dragDeltaX = 0;
        private int _dragDeltaY = 0;
        private Float _dragStartColDisplaySize;
        private Float _dragStartRowDisplaySize;
        private BufferedImage _beforeDragMapImage = null;
        private float _gridLayerOpacity = 0.0f;
        //private BufferedImage _beforeDragAnnotationImage = null;
        //private BufferedImage _beforeDragMaskImage = null;
        //private BufferedImage _gridMapBuffer;
        private UpdateGridWorker _updateGridWorker = null;
        private TargetFadeAnimator _fadeTarget = new TargetFadeAnimator();
        private Animator _fadeAnimator = CAnimator.createInstance(_fadeTarget, 200);

        private Integer _nearbyCol(int screenX) {
            if (_coolMapObject == null || _coolMapObject.getViewNumColumns() <= 0 || _coolMapObject.getViewNumRows() <= 0 || !_subMapIndexMin.isValidRange(_coolMapObject) || !_subMapIndexMax.isValidRange(_coolMapObject)) {
                return null;
            }

            int searchFromCol = _subMapIndexMin.col.intValue();
            int searchToCol = _subMapIndexMax.col.intValue() - 1;
            int searchTempCol;

            float gridX;
            VNode fromNode, toNode;
            fromNode = _coolMapObject.getViewNodeColumn(searchFromCol);
            toNode = _coolMapObject.getViewNodeColumn(searchToCol);
            if (fromNode == null || toNode == null) {
                return null;
            }

            Float fromOffset = fromNode.getViewOffset(_zoom.x);
            Float toOffset = toNode.getViewOffset(_zoom.x);

            if (fromOffset == null || toOffset == null) {
                return null;
            }

            float fromGridX = fromOffset + _mapDimension.x;
            float toGridX = toOffset + _mapDimension.x;

            if (screenX < fromGridX - _detectionRadius) {
                return null;
            }

            if (screenX > toGridX + _detectionRadius) {
                return null;
            }

            if (fromGridX - _detectionRadius <= screenX && screenX <= fromGridX + _detectionRadius) {
                return searchFromCol;

            }

            if (toGridX - _detectionRadius <= screenX && screenX <= toGridX + _detectionRadius) {
                return searchToCol;
            }

            Integer col = null;
            while (col == null) {
                searchTempCol = (searchFromCol + searchToCol) / 2;
                if (searchTempCol == searchFromCol) {
                    break;
                }
                VNode node = _coolMapObject.getViewNodeColumn(searchTempCol);
                if (node == null || node.getViewOffset() == null) {
                    continue;
                }
                gridX = node.getViewOffset(_zoom.x) + _mapDimension.x;
                if (gridX - _detectionRadius <= screenX && screenX <= gridX + _detectionRadius) {
                    col = searchTempCol;
                    return col;
                }
                if (screenX > gridX + _detectionRadius) {
                    searchFromCol = searchTempCol;
                }
                if (screenX < gridX - _detectionRadius) {
                    searchToCol = searchTempCol;
                }

            }
            //reached error?
            return null;
        }

        private Integer _nearbyRow(int screenY) {
            if (_coolMapObject == null || _coolMapObject.getViewNumColumns() <= 0 || _coolMapObject.getViewNumRows() <= 0 || !_subMapIndexMin.isValidRange(_coolMapObject) || !_subMapIndexMax.isValidRange(_coolMapObject)) {
                return null;
            }

            int searchFromRow = _subMapIndexMin.row.intValue();
            int searchToRow = _subMapIndexMax.row.intValue() - 1;
            int searchTempRow;

            float gridY;
            VNode fromNode, toNode;
            fromNode = _coolMapObject.getViewNodeRow(searchFromRow);
            toNode = _coolMapObject.getViewNodeRow(searchToRow);
            if (fromNode == null || toNode == null) {
                return null;
            }

            Float fromOffset = fromNode.getViewOffset(_zoom.y);
            Float toOffset = toNode.getViewOffset(_zoom.y);

            if (fromOffset == null || toOffset == null) {
                return null;
            }

            float fromGridY = fromOffset + _mapDimension.y;
            float toGridY = toOffset + _mapDimension.y;

            if (screenY < fromGridY - _detectionRadius) {
                return null;
            }

            if (screenY > toGridY + _detectionRadius) {
                return null;
            }

            if (fromGridY - _detectionRadius <= screenY && screenY <= fromGridY + _detectionRadius) {
                return searchFromRow;

            }

            if (toGridY - _detectionRadius <= screenY && screenY <= toGridY + _detectionRadius) {
                return searchToRow;
            }

            Integer row = null;
            while (row == null) {
                searchTempRow = (searchFromRow + searchToRow) / 2;
                if (searchTempRow == searchFromRow) {
                    break;
                }
                VNode node = _coolMapObject.getViewNodeRow(searchTempRow);
                if (node == null || node.getViewOffset() == null) {
                    continue;
                }
                gridY = node.getViewOffset(_zoom.y) + _mapDimension.y;
                if (gridY - _detectionRadius <= screenY && screenY <= gridY + _detectionRadius) {
                    row = searchTempRow;
                    return row;
                }
                if (screenY > gridY + _detectionRadius) {
                    searchFromRow = searchTempRow;
                }
                if (screenY < gridY - _detectionRadius) {
                    searchToRow = searchTempRow;
                }

            }
            //reached error?
            return null;
        }

        public GridLayer() {
            setOpaque(false);
            addMouseListener(this);
            addMouseMotionListener(this);
            _backgroundColor = _defaultBackgroundColor;
            _gridColor = _defaultGridColor;
            _labelBackgroundColor = _defaultLabelBackgroundColor;
            _fontColor = _defaultFontColor;
            _labelFont = UI.fontPlain.deriveFont(18f);
            setVisible(false);
        }

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);
            Graphics2D g2D = (Graphics2D) grphcs.create();
            if (_antiAlias) {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            //Completely block view from behind.
//            if (_dragStartX != null || _dragStartY != null) {
//                g2D.setColor(UI.colorBlack2);
//                g2D.fillRect(0, 0, getWidth(), getHeight());
//            }
//            g2D.drawImage(_gridMapBuffer, _subMapDimension.x, _subMapDimension.y, this);
            _paintGrid(g2D);
        }

        private void _paintGrid(Graphics2D g2D) {
            g2D.setColor(_backgroundColor);
            g2D.fillRect(0, 0, getWidth(), getHeight());

            //If
            if (_coolMapObject.getViewNumColumns() != 0 && _coolMapObject.getViewNumRows() != 0) {

                if (_subMapIndexMin.isValidRange(_coolMapObject) && _subMapIndexMax.isValidRange(_coolMapObject)) {

                    int minX = (int) (_mapDimension.x - _handleLength);
                    if (minX < 0) {
                        minX = 0;
                    }
                    VNode maxColNode = _coolMapObject.getViewNodeColumn(_coolMapObject.getViewNumColumns() - 1);

                    int minY = (int) (_mapDimension.y - _handleLength);
                    if (minY < 0) {
                        minY = 0;
                    }
                    VNode maxRowNode = _coolMapObject.getViewNodeRow(_coolMapObject.getViewNumRows() - 1);
                    if (maxColNode == null || maxRowNode == null) {
                        return; //whenever error occurs, just skip
                    }

                    int maxX = (int) (_mapDimension.x + maxColNode.getViewOffset(_zoom.x) + _handleLength);
                    if (maxX > getWidth()) {
                        maxX = getWidth();
                    }

                    int maxY = (int) (_mapDimension.y + maxRowNode.getViewOffset(_zoom.y) + _handleLength);
                    if (maxY > getHeight()) {
                        maxY = getHeight();
                    }

                    int anchorX, anchorY;
                    VNode node;
                    g2D.setColor(_gridColor);
                    g2D.setStroke(UI.stroke1_5);

                    //draw all vertical lines
                    for (int i = _subMapIndexMin.col.intValue(); i < _subMapIndexMax.col; i++) {
                        node = _coolMapObject.getViewNodeColumn(i);
                        Float offset = node.getViewOffset(_zoom.x);
                        if (offset != null) {
                            anchorX = (int) (_mapDimension.x + offset);
                            g2D.drawLine(anchorX, minY, anchorX, maxY);
                        }
                    }

                    //draw all horizontal lines
                    for (int i = _subMapIndexMin.row.intValue(); i < _subMapIndexMax.row; i++) {
                        node = _coolMapObject.getViewNodeRow(i);
                        Float offset = node.getViewOffset(_zoom.y);
                        if (offset != null) {
                            anchorY = (int) (_mapDimension.y + offset);
                            g2D.drawLine(minX, anchorY, maxX, anchorY);
                        }
                    }

                }//end of draw grid
            }

            g2D.setFont(_labelFont);
            g2D.setColor(UI.colorBlack5);
            g2D.setStroke(UI.stroke3);
            g2D.fillRoundRect(-_labelCornerRaidus + 2, getHeight() - _labelCornerRaidus - 21, g2D.getFontMetrics().stringWidth(_label) + 30, 45, _labelCornerRaidus, _labelCornerRaidus);

            g2D.setColor(_labelBackgroundColor);
            g2D.fillRoundRect(-_labelCornerRaidus, getHeight() - _labelCornerRaidus - 23, g2D.getFontMetrics().stringWidth(_label) + 30, 45, _labelCornerRaidus, _labelCornerRaidus);

            //draw label
            g2D.setColor(_fontColor);
            g2D.drawString(_label, 8, getHeight() - 10);
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            //System.out.println(_getCurrentCol(me.getX()));
            //System.out.println(_nearbyCol(me.getX()));
            //System.out.println("Clicked on grid layer...");
            //fadeOut();
        }

        @Override
        public void mousePressed(MouseEvent me) {
//            System.out.println("Pressed on grid layer...");
//            fadeOut();

            setMouseXY(me.getX(), me.getY());
            if (SwingUtilities.isLeftMouseButton(me)) {
                if (_coolMapObject == null) {
                    return;
                }
                int screenX = me.getX();
                int screenY = me.getY();
                _dragStartCol = _nearbyCol(screenX);
                _dragStartRow = _nearbyRow(screenY);

                _beforeDragMapImage = _mapContainer.getBufferImage(); //set the before dragging image
//                _gridMapBuffer = _beforeDragMapImage;

                //_beforeDragMapImage = _mapContainer.getBufferImage();
                if (_dragStartCol != null && _dragStartRow != null) {
                    _dragStartX = screenX;
                    _dragStartColDisplaySize = _coolMapObject.getViewNodeColumn(_dragStartCol).getViewSizeInMap(_zoom.x);
                    _dragStartY = screenY;
                    _dragStartRowDisplaySize = _coolMapObject.getViewNodeRow(_dragStartRow).getViewSizeInMap(_zoom.y);
                } else if (_dragStartCol != null) {
                    _dragStartX = screenX;
                    _dragStartColDisplaySize = _coolMapObject.getViewNodeColumn(_dragStartCol).getViewSizeInMap(_zoom.x);
                    _dragStartY = null;
                    _dragStartRowDisplaySize = null;

                } else if (_dragStartRow != null) {
                    _dragStartX = null;
                    _dragStartColDisplaySize = null;
                    _dragStartY = screenY;
                    _dragStartRowDisplaySize = _coolMapObject.getViewNodeRow(_dragStartRow).getViewSizeInMap(_zoom.y);

                } else {
                    _dragStartX = null;
                    _dragStartY = null;
                    _dragStartColDisplaySize = null;
                    _dragStartRowDisplaySize = null;
                    _beforeDragMapImage = null;
                }

                //May need to do certain things here
                _selectionLayer.setVisible(false);
                _overlayContainer.setVisible(false);

                _colDrawer.setEnabled(false);
                _rowDrawer.setEnabled(false);
                repaint();
            }

        }

        @Override
        public void mouseReleased(MouseEvent me) {
            setMouseXY(me.getX(), me.getY());
            if (SwingUtilities.isLeftMouseButton(me)) {
                //Start the update worker thread
                //redrawCanvas();
                if (_updateGridWorker != null && _updateGridWorker.isAlive()) {
                    _updateGridWorker.interrupt();//Stop any unfinished resizing tasks
                }
                //reset parameters
//                _gridMapBuffer = null;
                _dragStartX = null;
                _dragStartY = null;
                _dragStartColDisplaySize = null;
                _dragStartRowDisplaySize = null;
                _beforeDragMapImage = null;
//                if(_dragStartCol != null || _dragStartRow != null){
//                    //if actually updated.
//                    updateCanvas(true);
//                }
                _dragStartRow = null;
                _dragStartCol = null;

                if (_coolMapObject.isViewValid()) {
                    updateCanvasEnforceAll(); //grid change not fired..
                }

                _selectionLayer.setVisible(true);
                _overlayContainer.setVisible(true);
                _colDrawer.setEnabled(true);
                _rowDrawer.setEnabled(true);

                //fireGridChanged
                _fireGridChanged();
            }
        }

        @Override
        public void mouseEntered(MouseEvent me) {
//            if(_hoverLayer.isVisible()){
//                _hoverLayer.fadeOut();
//            }
            _hoverLayer.setVisible(false);
            setMouseXY(me.getX(), me.getY());
        }

        @Override
        public void mouseExited(MouseEvent me) {
            setMouseXY(me.getX(), me.getY());
            mouseReleased(me);
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            setMouseXY(me.getX(), me.getY());
            if (SwingUtilities.isLeftMouseButton(me)) {
                if (_updateGridWorker != null && _updateGridWorker.isAlive()) {
                    _updateGridWorker.interrupt();//mark as to stop
                    //System.out.println("trying to interrupt...");
                }
                _updateGridWorker = new UpdateGridWorker();
                _updateGridWorker.start();
            }
        }

        @Override
        public void mouseMoved(MouseEvent me) {
            setMouseXY(me.getX(), me.getY());
            redrawCanvas();

            Integer col = _nearbyCol(me.getX());
            Integer row = _nearbyRow(me.getY());

            if (col != null && row != null) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            } else if (col != null) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            } else if (row != null) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            } else {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

        }//mouse moved

        private class UpdateGridWorker extends Thread {

            private void _updateGrid() {
                //update the dragging image.
                if (_coolMapObject == null || !_subMapIndexMin.isValidRange(_coolMapObject) || !_subMapIndexMax.isValidRange(_coolMapObject)) {
                    return;
                }

                //System.out.println("GridMapBuffer:" + _gridMapBuffer);
                BufferedImage templateImage = _beforeDragMapImage;
                BufferedImage tempImage = null;
                Point mouse = _getMouseXY();

                //if(templateImage == null){
                //No buffer here, simply return
                //    return;
                //}
                if (_dragStartX != null) {

                    _dragDeltaX = mouse.x - _dragStartX;
                    if (_dragStartColDisplaySize + _dragDeltaX < 1) {
                        _dragDeltaX = (int) (-_dragStartColDisplaySize + 1);
                    }
                    float newWidth = _dragStartColDisplaySize + _dragDeltaX;
                    if (newWidth < 1) {
                        newWidth = 1;
                    }
                    VNode node = _coolMapObject.getViewNodeColumn(_dragStartCol);
                    VNode anchorNode = _coolMapObject.getViewNodeColumn(_subMapIndexMin.col.intValue());
                    if (node != null && anchorNode != null) {
                        node.setViewMultiplier(newWidth / _zoom.x);
                        updateNodeDisplayParams();//should still be fast for thousands of nodes.

                        if (templateImage != null && templateImage.getWidth() > 0 && templateImage.getHeight() > 0) {
                            if (Thread.currentThread().isInterrupted()) {
                                repaint();
                                return;
                            }
                            tempImage = _graphicsConfiguration.createCompatibleImage(templateImage.getWidth() + _dragDeltaX, templateImage.getHeight(), Transparency.OPAQUE);
                            Graphics2D g2Dtemp = tempImage.createGraphics();

//                            g2Dtemp.setColor(Color.BLUE);
//                            g2Dtemp.fillRect(0, 0, tempImage.getWidth(), tempImage.getHeight());
                            int originalWidth = _dragStartColDisplaySize.intValue();

                            //This is the new 
                            int subXStart = (int) (node.getViewOffset() - anchorNode.getViewOffset());
                            int subXEnd = (int) (node.getViewOffset(_zoom.x) - anchorNode.getViewOffset());

                            //something needs to be drawn before
                            //draw leading part
                            if (Thread.currentThread().isInterrupted()) {
                                repaint();
                                return;
                            }
                            if (subXStart > 0 && subXStart < templateImage.getWidth()) {
                                g2Dtemp.drawImage(templateImage.getSubimage(0, 0, subXStart, templateImage.getHeight()), 0, 0, null);
                            }
                            if (Thread.currentThread().isInterrupted()) {
                                repaint();
                                return;
                            }
                            //draw trailing part
                            if (subXStart + originalWidth < templateImage.getWidth()) {
                                g2Dtemp.drawImage(templateImage.getSubimage((int) (subXStart + originalWidth), 0, (int) (templateImage.getWidth() - subXStart - originalWidth), templateImage.getHeight()), (int) (subXStart + newWidth), 0, null);
                            }

                            //draw resized part
                            //The resized part is not drawn.
                            //have some trouble for last column
                            //There's a barrier - broken
                            if (Thread.currentThread().isInterrupted()) {
                                repaint();
                                return;
                            }

                            g2Dtemp.drawImage(templateImage.getSubimage(subXStart, 0, originalWidth, templateImage.getHeight()).getScaledInstance((int) newWidth, templateImage.getHeight(), BufferedImage.SCALE_FAST), subXStart, 0, null);

                            //pass the template image on
                            templateImage = tempImage;
                        }
                    }

                }//End of dragging X

                if (_dragStartY != null) {
                    _dragDeltaY = mouse.y - _dragStartY;
                    if (_dragStartRowDisplaySize + _dragDeltaY < 1) {
                        _dragDeltaY = (int) (-_dragStartRowDisplaySize + 1);
                    }
                    float newHeight = _dragStartRowDisplaySize + _dragDeltaY;
                    if (newHeight < 1) {
                        newHeight = 1;
                    }
                    VNode node = _coolMapObject.getViewNodeRow(_dragStartRow);
                    VNode anchorNode = _coolMapObject.getViewNodeRow(_subMapIndexMin.row.intValue());
                    if (node != null && anchorNode != null) {
                        node.setViewMultiplier(newHeight / _zoom.y);
                        updateNodeDisplayParams();//should still be fast for thousands of nodes.

                        if (templateImage != null && templateImage.getWidth() > 0 && templateImage.getHeight() > 0) {
                            if (Thread.currentThread().isInterrupted()) {
                                repaint();
                                return;
                            }

                            tempImage = _graphicsConfiguration.createCompatibleImage(templateImage.getWidth(), templateImage.getHeight() + _dragDeltaY, Transparency.OPAQUE);
                            Graphics2D g2Dtemp = tempImage.createGraphics();
                            int originalHeight = _dragStartRowDisplaySize.intValue();
                            int subYStart = (int) (node.getViewOffset() - anchorNode.getViewOffset());
                            int subYEnd = (int) (node.getViewOffset(_zoom.y) - anchorNode.getViewOffset());

                            //something needs to be drawn before
                            //draw leading part
                            if (Thread.currentThread().isInterrupted()) {
                                repaint();
                                return;
                            }

                            //draw starting part
                            if (subYStart > 0 && subYStart < templateImage.getHeight()) {
                                g2Dtemp.drawImage(templateImage.getSubimage(0, 0, templateImage.getWidth(), subYStart), 0, 0, null);
                            }
                            if (Thread.currentThread().isInterrupted()) {
                                repaint();
                                return;
                            }
                            //draw trailing part
                            if (subYStart + originalHeight < templateImage.getHeight()) {
                                g2Dtemp.drawImage(templateImage.getSubimage(0, (int) (subYStart + originalHeight), templateImage.getWidth(), (int) (templateImage.getHeight() - subYStart - originalHeight)), 0, (int) (subYStart + newHeight), null);
                            }
                            if (Thread.currentThread().isInterrupted()) {
                                repaint();
                                return;
                            }
                            //draw resize part
                            g2Dtemp.drawImage(templateImage.getSubimage(0, subYStart, templateImage.getWidth(), originalHeight).getScaledInstance((int) templateImage.getWidth(), (int) newHeight, BufferedImage.SCALE_FAST), 0, subYStart, null);

                            //pass the template image on
                            templateImage = tempImage;
                        }
                    }
                }//end of drag Y

                //If interrupted, immediately return.
                if (Thread.currentThread().isInterrupted()) {
                    repaint();
                    return;
                }

                if (tempImage != null) {
                    _mapContainer.setBufferImage(tempImage);
                }

                tempImage = null;
                templateImage = null;
                //If the thread is interrupted, then
                //System.out.println("Grid Buffer reassigned.");

                repaint();
            }

            @Override
            public void run() {
//                _selectionLayer.setVisible(false);
//                _overlayContainer.setVisible(false);
                _updateGrid();
                if (Thread.interrupted()) {
                    return;
                }
                //Only the uninterrupted one will restore selection layer;
                //And this can only be interrupted 
//                _selectionLayer.setVisible(true);
//                _overlayContainer.setVisible(true);
            }
        }

        public void fadeIn() {
//            setVisible(true);

//            System.out.println("Fade IN:==>" + isVisible() + " " + _gridLayerOpacity);
            if (isVisible() && _gridLayerOpacity == 1.0f) {
                return;
            }

            if (_fadeAnimator.isRunning()) {
                _fadeAnimator.cancel();
            }
            _fadeTarget.setFadeIn(true);
            _fadeAnimator.start();
        }

        public void fadeOut() {
//            setVisible(false);
            if (!isVisible() && _gridLayerOpacity == 0.0f) {
                //already faded out.
                return;
            }

            if (_fadeAnimator.isRunning()) {
                _fadeAnimator.cancel();
            }
            _fadeTarget.setFadeIn(false);
            _fadeAnimator.start();
        }

        private class TargetFadeAnimator implements TimingTarget {
//            private float _startOpacity;
//            private float _endOpacity;

            private boolean _fadeIn;

            public void setFadeIn(boolean fadeIn) {
                _fadeIn = fadeIn;
//                if(fadeIn){
//                    _startOpacity = 0.0f;
//                    _endOpacity = 1.0f;
//                }
//                else{
//                    _startOpacity = 1.0f;
//                    _endOpacity = 0.0f;
//                }
            }

            @Override
            public void begin(Animator source) {
                if (_fadeIn) {
                    setVisible(true);
                    _gridLayerOpacity = 0.0f;
                } else {
                    //fade out
                    _gridLayerOpacity = 1.0f;
                }
                repaint();
                //System.out.println("Fade:" + _fadeIn + " | " + _gridLayerOpacity);
            }

            @Override
            public void end(Animator source) {
                if (_fadeIn) {
//                    setVisible(true);
                    _gridLayerOpacity = 1.0f;
                } else {
                    setVisible(false);
                    _gridLayerOpacity = 0.0f;
                }
                repaint();
                //System.out.println("Fade:" + _fadeIn + " | " + _gridLayerOpacity);
            }

            @Override
            public void repeat(Animator source) {
            }

            @Override
            public void reverse(Animator source) {
            }

            @Override
            public void timingEvent(Animator source, double fraction) {
                if (!_fadeIn) {
                    fraction = 1 - fraction;
                }
                //a better way is to use alpha composite.
                _backgroundColor = UI.mixOpacity(_defaultBackgroundColor, (int) (_defaultBackgroundColor.getAlpha() * fraction));
                _gridColor = UI.mixOpacity(_defaultGridColor, (int) (_defaultGridColor.getAlpha() * fraction));
                _labelBackgroundColor = UI.mixOpacity(_defaultLabelBackgroundColor, (int) (_defaultLabelBackgroundColor.getAlpha() * fraction));
                _fontColor = UI.mixOpacity(_defaultFontColor, (int) (_defaultFontColor.getAlpha() * fraction));
                repaint();
            }
        }
    }

/////////////////////////////////////////////////////////////////////////////////////////////////
    private final class EventLayer extends JPanel implements MouseListener, MouseMotionListener {

        private final Point _dragStart = new Point();
        private boolean _isDragging = false;

        @SuppressWarnings("LeakingThisInConstructor")
        public EventLayer() {
            super();
//            addMouseListener(this);
//            addMouseMotionListener(this);
            setOpaque(false);
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            setMouseXY(me.getX(), me.getY());
//            System.out.println("Click received");
//            System.out.println("Min index:" + _subMapIndexMin);
//            System.out.println("Max index" + _subMapIndexMax);
//            _progressMask.fadeIn();
//            _progressMask.fadeOut();
            //Rectangle region = new Rectangle(50,50,200,300);
            //_highlightLayer.highlight(Collections.singletonList(region));
//            System.out.println("Clicked on event layer!");
//            Rectangle region1 = new Rectangle(0, 0, 5, 10);
//            Rectangle region2 = new Rectangle(30, 30, 10, 5);
//            ArrayList<Rectangle> nodeRegions = new ArrayList<Rectangle>();
//            nodeRegions.add(region1);
//            nodeRegions.add(region2);
//            List<Rectangle> regions = _convertNodeRegionToViewRegion(nodeRegions);
//
////            for(Rectangle r : regions){
////                System.out.println(r);
////            }
////            System.out.println("Mouse Click triggers events----------------");
//            _highlightLayer.setRegions(regions);
//            _highlightLayer.highlight();

            if (SwingUtilities.isLeftMouseButton(me)) {

                if (me.isShiftDown()) {
                    //System.out.println(_selectionAnchorCell);
                    //System.out.println(_selectionAnchorCell.isValidCell());
                    if (_selectionAnchorCell.isValidCell(_coolMapObject)) {

                        MatrixCell endCell = new MatrixCell(_activeCell.row, _activeCell.col);
                        Rectangle region = _generateNodeRegion(_selectionAnchorCell, endCell);
                        //System.out.println(region);
                        
                        
                        
                        StateStorageMaster.addState(CoolMapState.createStateSelections("Selection change", _coolMapObject, null));
                        setSelection(region);
                        
                        //
                        //
                        
                        
                    } else {
                        //simply treat as a regular
                        if (_activeCell.isValidCell(_coolMapObject)) {
                            
                            //Add a state before applying setSelection
                            StateStorageMaster.addState(CoolMapState.createStateSelections("Selection change", _coolMapObject, null));
                            
                            setSelection(new Rectangle(_activeCell.col.intValue(), _activeCell.row.intValue(), 1, 1));
                            _selectionAnchorCell.col(_activeCell.col.intValue());
                            _selectionAnchorCell.row(_activeCell.row.intValue());
                        } else {
                            StateStorageMaster.addState(CoolMapState.createStateSelections("Selection change", _coolMapObject, null));
                            clearSelection();
                            _selectionAnchorCell.row(null);
                            _selectionAnchorCell.col(null);
                        }
                    }
                    return;
                }

                if (me.isControlDown()) {
                    StateStorageMaster.addState(CoolMapState.createStateSelections("Selection change", _coolMapObject, null));
                    clearSelection();
                    return;
                }

                if (_activeCell.isValidCell(_coolMapObject)) {
                    StateStorageMaster.addState(CoolMapState.createStateSelections("Selection change", _coolMapObject, null));
                    setSelection(new Rectangle(_activeCell.col.intValue(), _activeCell.row.intValue(), 1, 1));
                    _selectionAnchorCell.col(_activeCell.col.intValue());
                    _selectionAnchorCell.row(_activeCell.row.intValue());
                } else {
                    StateStorageMaster.addState(CoolMapState.createStateSelections("Selection change", _coolMapObject, null));
                    clearSelection();
                    _selectionAnchorCell.row(null);
                    _selectionAnchorCell.col(null);
                    return;
                }

//                
//                if (me.getClickCount() > 1) {
//                    clearSelection();
//                }
            }

        }

        @Override
        public void mousePressed(MouseEvent me) {
//            System.out.println("Pressed on event layer!");
//            _gridLayer.fadeIn();

            setMouseXY(me.getX(), me.getY());
            if (SwingUtilities.isLeftMouseButton(me)) {
                _dragStart.x = me.getX();
                _dragStart.y = me.getY();
                _isDragging = true;
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                //_hoverLayer.fadeOut();
                _hoverLayer.setVisible(false);
                _rowDrawer.setEnabled(false);
                _colDrawer.setEnabled(false);
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            setMouseXY(me.getX(), me.getY());
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            if (SwingUtilities.isLeftMouseButton(me)) {

                if (getCanvasDimension().contains(_getMouseXY())) {
                    //_hoverLayer.fadeIn();
                    _hoverLayer.setVisible(true);
                    //_hoverLayer.fadeIn();
                    _hoverLayer.setActiveCell(_activeCell);//reposition to activeCell
                }
                _justifyView();
                _rowDrawer.setEnabled(true);
                _colDrawer.setEnabled(true);

                //it will update if necessary.
                if (_isDragging) {
                    updateCanvasIfNecessary();
                    _fireViewAnchorMoved();
                }
                _isDragging = false;
            }

        }

        @Override
        public void mouseEntered(MouseEvent me) {
            //_hoverLayer.fadeIn();
            setMouseXY(me.getX(), me.getY());
            _hoverLayer.setVisible(true);
            if (_activeCell.isValidCell(_coolMapObject)) {
//                _hoverLayer.setActiveCell(_activeCell); 
                _hoverLayer.fadeIn(_activeCell);
            }

            //Focus
            _canvas.requestFocusInWindow();
//            _messageLayer.displayMessage("Activated");

        }

        @Override
        public void mouseExited(MouseEvent me) {
            setMouseXY(me.getX(), me.getY());
            mouseReleased(me);
            _hoverLayer.setVisible(false);
            _notificationLayer.repaint();
        }

        @Override
        public void mouseDragged(MouseEvent me) {

            //System.out.println("Dragged");
            setMouseXY(me.getX(), me.getY());
            if (_isDragging) {
                int x = me.getX();
                int y = me.getY();
                _moveMapBy(me.getX() - _dragStart.x, me.getY() - _dragStart.y, false);
                _dragStart.x = me.getX();
                _dragStart.y = me.getY();
                //moveMapTo(_mapDimension.x + me.getX() - _dragStart.x, _mapDimension.y + me.getY() - _dragStart.y);
            }
        }

        @Override
        public void mouseMoved(MouseEvent me) {
            setMouseXY(me.getX(), me.getY());
            redrawCanvas();
        }
    }

    private class UpdateBufferWorker extends Thread {

        private boolean _forceUpdateAll;
        private boolean _forceUpdateOverlay;

        public UpdateBufferWorker(boolean forceUpdateAll, boolean forceUpdateOverlay) {
            _forceUpdateAll = forceUpdateAll;
            _forceUpdateOverlay = forceUpdateOverlay;

//            System.out.println("Overlay updated: " + _forceUpdateOverlay);
        }

        @Override
        public void run() {
            //_selectionLayer.setVisible(false);
            try {
                _updateMapBuffers();
                //_selectionLayer.setVisible(true);
                redrawCanvas();
                //After it is done, some other things may need to be done
                if (Thread.currentThread().isInterrupted()) {
                    return;
                } else {
                    //Post update processing
                    //highlight any regions if needed.
                    _hilightLayer.highlight();
                }
            } catch (InterruptedException ei) {
                System.out.println();
            } catch (Exception e) {
                //All exceptions related to render are thrown here.
                e.printStackTrace();

            }
        }

        /**
         * functions actually update map buffers.
         */
        private synchronized void _updateMapBuffers() throws Exception {
            //The map buffers need to be re-rendered, including
            //all mapMap layers, and all row/column layers
            //System.out.println("Force to update!" + _force + " " + _isReRenderNeeded());

            if (_forceUpdateAll || _isReRenderNeeded() || _forceUpdateNeeded) {
                _progressMask.fadeIn();
                //do something to re-draw
                //The parameters must be updated; this is called only once.
                //
                //System.out.println("Map parameters are updated.");
                MatrixCell subMapIndexMin = new MatrixCell();
                MatrixCell subMapIndexMax = new MatrixCell();
                Rectangle subMapDimension = new Rectangle();

                //compute
                //must use intermediate parameters
                boolean success = _computeSubMapParams(subMapIndexMin, subMapIndexMax, subMapDimension);
                if (!success || !subMapIndexMin.isValidRange(_coolMapObject) || !subMapIndexMax.isValidRange(_coolMapObject)) {
                    //Still need to update.
                    //if indexes are null, then map is empty
                    //System.out.println("Update failed");
                    _mapContainer.setBufferImage(null);
                    _overlayContainer.setBufferImage(null);
                    _colDrawer.clearBuffers();
                    _rowDrawer.clearBuffer();
                    _progressMask.fadeOut();
                    return;
                }

                //update all the necessary buffers using these parameters
                if (Thread.currentThread().isInterrupted()) {
                    redrawCanvas();
                    return;
                }

//                try {
//                    //System.out.println("Update Task is being processed:...");
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    //If interrupted, immediately return
//                    //only need the update functions throw interrupted exception
//                    //System.out.println("Update Task was cancelled.");
//                    return;
//                }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                if (!subMapIndexMin.isValidRange(_coolMapObject) || !subMapIndexMax.isValidRange(_coolMapObject)) {
                    redrawCanvas();
                    return;
                }

                //update map container
                try {
                    _mapContainer.updateMapBuffers(subMapIndexMin, subMapIndexMax, subMapDimension);
                    _overlayContainer.updateMapBuffers(subMapIndexMin, subMapIndexMax, subMapDimension);
                } catch (Exception e) {
                    throw e;
                }
                _colDrawer.updateDrawerBuffers(subMapIndexMin.row.intValue(), subMapIndexMax.row.intValue(), subMapIndexMin.col.intValue(), subMapIndexMax.col.intValue(), subMapDimension);
                _rowDrawer.updateDrawerBuffers(subMapIndexMin.row.intValue(), subMapIndexMax.row.intValue(), subMapIndexMin.col.intValue(), subMapIndexMax.col.intValue(), subMapDimension);
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
                //System.out.println("Temp params:" + subMapIndexMin + "--" + subMapIndexMax + "--" + subMapDimension);

                //Assign the parameters to
                if (Thread.currentThread().isInterrupted()) {
                    redrawCanvas();
                    return;
                }
                //don't assign.

                _assignSubMapParams(subMapIndexMin, subMapIndexMax, subMapDimension);

                //System.out.println("Updated params:" + _subMapIndexMin + "--" + _subMapIndexMax + "--" + _subMapDimension);
                //Also it's possible to only repaint a certain region
                _progressMask.fadeOut();
                _forceUpdateNeeded = false; //Once a round reached here, he forceUpdateNeeded switch is set to false.
            } else if (_forceUpdateOverlay) {

//                System.out.println("Overlay updated");
                _progressMask.fadeIn();
//                System.out.println("Overlay needs to be updated..");
                MatrixCell subMapIndexMin = new MatrixCell();
                MatrixCell subMapIndexMax = new MatrixCell();
                Rectangle subMapDimension = new Rectangle();
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

//                should use the new parameters?
                boolean success = _computeSubMapParams(subMapIndexMin, subMapIndexMax, subMapDimension);
                
//                should be success -> however, this was done in the enforce all i think, as it's a MOVE.
                System.out.println("Force update overlay called. ");
                
                _overlayContainer.updateMapBuffers(_subMapIndexMin, _subMapIndexMax, _subMapDimension);

                _progressMask.fadeOut();
            }
            redrawCanvas();

        }
    }
    private boolean _drawHover = false;

    public void setSyncActiveCell(boolean activeCellSynced) {
        this._activeCellSynced = activeCellSynced;
    }

    public void setSyncMapAnchor(boolean anchorSynced) {
        this._anchorSynced = anchorSynced;
    }

    public void setSyncColumnLayout(boolean columnLayoutSynced) {
        this._columnLayoutSynced = columnLayoutSynced;
    }

    public void setSyncColumnSelection(boolean columnSelectionSynced) {
        this._columnSelectionSynced = columnSelectionSynced;
    }

    public void setSyncRowLayout(boolean rowLayoutSynced) {
        this._rowLayoutSynced = rowLayoutSynced;
    }

    public void setSyncRowSelection(boolean rowSelectionSynced) {
        this._rowSelectionSynced = rowSelectionSynced;
    }

    public void setSyncZoom(boolean zoomSynced) {
        this._zoomSynced = zoomSynced;
    }
    /////////////////////////////////////
    private boolean _anchorSynced = false;
    private boolean _zoomSynced = false;
    private boolean _activeCellSynced = false;
    private boolean _rowSelectionSynced = false;
    private boolean _columnSelectionSynced = false;
    private boolean _rowLayoutSynced = false;
    private boolean _columnLayoutSynced = false;

    /**
     * deSync
     */
    public void deSyncAll() {
        setSyncMapAnchor(false);
        setSyncZoom(false);
        setSyncActiveCell(false);
        setSyncRowSelection(false);
        setSyncColumnSelection(false);
        setSyncRowLayout(false);
        setSyncColumnLayout(false);
        redrawCanvas();
    }

    /**
     * force drawing hover whenever active row/column is valid
     *
     * @param draw
     */
    public void setForceDrawHover(boolean draw) {
        _drawHover = draw;
    }

    public boolean isDrawHover() {
        return _drawHover;
    }

//    private boolean _forceHover = false;
//    
//    public void setForceDrawHover(boolean forceHover){
//        _forceHover = forceHover;
//    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
    private boolean paintHoverTip = true; //paint tip
    private boolean paintLabelsTip = false; //paint labels tip

    private class HoverLayer extends JPanel {

        private TargetHoverPan _panTarget = new TargetHoverPan();
        private Animator _panAnimator = CAnimator.createInstance(_panTarget, 200);
        private TargetVisibility _visTarget = new TargetVisibility();
        private Animator _visAnimator = CAnimator.createInstance(_visTarget, 200);
        private final Rectangle _hoverBounds = new Rectangle();
        //private final MatrixCell _hoverActiveCell = new MatrixCell();
        //private Color _hoverColor;
        private Color _defaultHoverColor = Color.YELLOW;
        private float _opacity = 1.0f;
        private Font _hoverLabelFont = UI.fontPlain.deriveFont(16.0f);
        private Font _hoverValueFont = UI.fontMono.deriveFont(20.0f).deriveFont(Font.BOLD);
        private Color _hoverShadow = new Color(0, 0, 0, 120);
        private Color _hoverTipBackgroundColor = new Color(240, 240, 240);
        private Color _hoverSubTipBackgroundColor = new Color(237, 248, 177);
        private Color _labelFontColor = UI.colorBlack3;
        private Color _valueFontColor = UI.colorBlack6;
        private BufferedImage _mainToolTip = null;
        private Font _tipNameFont = UI.fontPlain.deriveFont(12.0f).deriveFont(Font.BOLD);
        private Color _tipNameColor = new Color(67, 162, 202);

        //private boolean _enabled = true;
//        @Override
//        public void setEnabled(boolean enabled) {
//            super.setEnabled(enabled);
//            _enabled = enabled;
//        }
        public void setActiveCell(MatrixCell cell) {
            if (cell != null && cell.isValidCell(_coolMapObject)) {

                //System.out.println("trying to set active cell");
                if (_panAnimator.isRunning()) {
                    _panAnimator.cancel();//set cell immediately.
                }

                VNode colNode = _coolMapObject.getViewNodeColumn(cell.col.intValue());
                VNode rowNode = _coolMapObject.getViewNodeRow(cell.row.intValue());
                _hoverBounds.x = (int) (colNode.getViewOffset() + _mapDimension.x);
                _hoverBounds.y = (int) (rowNode.getViewOffset() + _mapDimension.y);
                _hoverBounds.width = (int) colNode.getViewSizeInMap(_zoom.x);
                _hoverBounds.height = (int) rowNode.getViewSizeInMap(_zoom.y);
            }

        }

        public void gridMovedTo(MatrixCell startCell, MatrixCell endCell) {
            //move the renderGrid from fromCell to destinationcell
            //move the hoverGrid from _hoverCell to destination cell.
            //System.out.println(startCell + " " + destinationCell);
            //System.out.println(startCell + " " + endCell);
            //setActiveCell(endCell);//make endcell the active cell

            if (startCell == null || endCell == null) {
                return;
            }

            startCell = startCell.duplicate();
            endCell = endCell.duplicate();

            if (startCell.isValidCell(_coolMapObject) && endCell.isValidCell(_coolMapObject)) {
                //MOVE
                //System.out.println("Cell panned?");
                if (!isVisible() && !_eventLayer._isDragging && _drawHover) {
                    setVisible(true);
                }

                _panCell(startCell, endCell);
                _updateMainToolTip(endCell);

            } else if (startCell.isValidCell(_coolMapObject) && !endCell.isValidCell(_coolMapObject)) {
                //FADE OUT immediately
                fadeOut();
            } else if (!startCell.isValidCell(_coolMapObject) && endCell.isValidCell(_coolMapObject)) {
                //Fade IN 
//                setActiveCell(endCell);
                if (!isVisible() && !_eventLayer._isDragging && _drawHover) {
                    setVisible(true);
                }

                fadeIn(endCell);

            } else {
                //Don't do anything
            }
        }

        private void _panCell(MatrixCell startCell, MatrixCell endCell) {
            if (_panAnimator.isRunning()) {
                _panAnimator.cancel();
            }
            _panTarget.setStartEnd(startCell, endCell);
            _panAnimator.start();
        }

        public void fadeIn(MatrixCell cell) {
            if (_visAnimator.isRunning()) {
                _visAnimator.cancel(); //call animation stop won't stop it immediately.await won't work either. only cancel works
            }
            _visTarget.setVisibility(0.0f, 1.0f);
            _visAnimator.start();

            if (cell != null && cell.isValidCell(_coolMapObject)) {
                //System.out.println("Pan cell after release");
                setActiveCell(cell);
                _panCell(cell, cell);
                _updateMainToolTip(cell);
            }
        }

        public void fadeOut() {
            //System.out.println("Fade out called");

            if (_visAnimator.isRunning()) {
                _visAnimator.cancel(); //call animation stop won't stop it immediately.await won't work either. only cancel works
            }
            _visTarget.setVisibility(1.0f, 0.0f);
            _visAnimator.start();

            //Remove all residual information
            //_panTarget.resetStartEnd();
        }

        private final Font labelFont;
        private final Font labelFontRotated;
        private final Font labelFontValue;

        public HoverLayer() {
            setOpaque(false);
            //_hoverColor = _defaultHoverColor;
            labelFont = UI.fontPlain.deriveFont(labelFontSize);
            AffineTransform at = new AffineTransform();
            at.rotate(-Math.PI / 2);
            labelFontRotated = labelFont.deriveFont(at);
            labelFontValue = UI.fontPlain.deriveFont(16f).deriveFont(Font.BOLD);
        }

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);
            if (isVisible()) {
                Graphics2D g2D = (Graphics2D) grphcs.create();
                if (_antiAlias) {
                    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }

                if (_opacity <= 0) {
                    return;
                }

                final float shadowOpacity = _opacity * 0.7f;
                //_hoverColor = UI.mixOpacity(_defaultHoverColor, _opacity);

//          The hover rectangle should always be visible.  
                g2D.setColor(UI.mixOpacity(_defaultHoverColor, _opacity));
                g2D.drawRect(_hoverBounds.x, _hoverBounds.y, _hoverBounds.width, _hoverBounds.height);
                g2D.setStroke(UI.stroke2);

                g2D.setColor(UI.mixOpacity(_defaultHoverColor, shadowOpacity));
                g2D.drawRoundRect(_hoverBounds.x - 1, _hoverBounds.y - 1, _hoverBounds.width + 2, _hoverBounds.height + 2, 2, 2);

                //
                if (paintHoverTip) {
                    _paintHovertip(g2D);
                }

                if (paintLabelsTip) {
                    _paintLabelsTip(g2D);
                }
            }
        }

        private float labelFontSize = 12f;
        private int labelSize = 18;
        //private int labelMargin = 120;
        //private int labelTick = 100;

        private void _paintLabelsTip(Graphics2D g2D) {
            try {
                if (!_activeCell.isValidCell(_coolMapObject)) {
                    return;
                }

                int row = _activeCell.row.intValue();
                int col = _activeCell.col.intValue();

                int fromRow = row - 5;
                int toRow = row + 5;

                int fromCol = col - 5;
                int toCol = col + 5;

                if (fromRow < 0) {
                    fromRow = 0;
                }
                if (fromCol < 0) {
                    fromCol = 0;
                }
                if (toRow >= _coolMapObject.getViewNumRows()) {
                    toRow = _coolMapObject.getViewNumRows() - 1;
                }
                if (toCol >= _coolMapObject.getViewNumColumns()) {
                    toCol = _coolMapObject.getViewNumColumns() - 1;
                }

                //center
                Point center = new Point(Math.round((int) _hoverBounds.getCenterX()), (int) Math.round(_hoverBounds.getCenterY()));

                //rows
                int rowLabelHeight = (toRow - fromRow + 1) * labelSize;
                int rowLabelWidth = 5;
                int rowLabelTop = center.y - rowLabelHeight / 2;
                //columns
                int columnLabelWidth = (toCol - fromCol + 1) * labelSize;
                int columnLabelHeight = 5;
                int columnLabelLeft = center.x - columnLabelWidth / 2;

                //determine margin and tick
                //center.x + labelmargin
//                if(columnLabelLeft + columnLabelWidth > center.x + 120){
//                    labelMargin = columnLabelLeft + columnLabelWidth - center.x + 10;
//                    labelTick = labelMargin - 20;
//                }
//                else{
//                    labelMargin = 120;
//                    labelTick = 100;
//                }
                VNode toColNode = _coolMapObject.getViewNodeColumn(toCol);
                VNode fromRowNode = _coolMapObject.getViewNodeRow(fromRow);
                int rowLabelMargin;
                int colLabelMargin;
                int rowLabelTick;
                int colLabelTick;

                if (toColNode.getViewOffset() + _mapDimension.x + toColNode.getViewSizeInMap(_zoom.x) > center.x + 120) {
                    rowLabelMargin = (int) (toColNode.getViewOffset() + _mapDimension.x - center.x + toColNode.getViewSizeInMap(_zoom.x));

                } else {
                    rowLabelMargin = 120;

                }
                rowLabelTick = rowLabelMargin - 20;

                if (fromRowNode.getViewOffset() + _mapDimension.y < center.y - 120) {
                    colLabelMargin = -(int) (fromRowNode.getViewOffset() + _mapDimension.y - center.y);
                } else {
                    colLabelMargin = 120;
                }
                colLabelTick = colLabelMargin - 20;

                if (rowLabelTop < _mapDimension.y) {
                    rowLabelTop = _mapDimension.y;
                }

                if (rowLabelTop + rowLabelHeight > _mapDimension.y + _mapDimension.height) {
                    rowLabelTop = _mapDimension.y + _mapDimension.height - rowLabelHeight;
                }

                g2D.setFont(labelFont);
                g2D.setStroke(UI.strokeDash1_5);
                int rowX, rowY;
                for (int i = fromRow; i <= toRow; i++) {
                    VNode node = _coolMapObject.getViewNodeRow(i);
                    int nodeY = (int) (node.getViewOffset() + _mapDimension.y);
                    g2D.setColor(_hoverTipBackgroundColor);
                    rowX = center.x + rowLabelTick;
                    rowY = (int) (nodeY - 2 + node.getViewSizeInMap(_zoom.y) / 2);
                    g2D.fillOval(rowX - 2, rowY - 2, 5, 5);

                    String label = node.getViewLabel();
                    int width = g2D.getFontMetrics().stringWidth(label);
                    if (rowLabelWidth < width) {
                        rowLabelWidth = width;
                    }

                    g2D.drawLine(rowX, rowY, center.x + rowLabelMargin, rowLabelTop + (i - fromRow) * labelSize + labelSize / 2);

                }

                g2D.setColor(_hoverTipBackgroundColor);

                rowLabelWidth += 20;
                g2D.fillRoundRect(center.x + rowLabelMargin, rowLabelTop, rowLabelWidth, rowLabelHeight, 5, 5);

                g2D.setColor(UI.colorLightGreen0);
                g2D.fillRoundRect(center.x + rowLabelMargin, rowLabelTop + (row - fromRow) * labelSize, rowLabelWidth, labelSize, 5, 5);

                g2D.setColor(UI.colorBlack2);
                for (int i = fromRow; i <= toRow; i++) {

                    VNode node = _coolMapObject.getViewNodeRow(i);
                    g2D.drawString(node.getViewLabel(), center.x + rowLabelMargin + 10, rowLabelTop + (i - fromRow) * labelSize + labelSize / 2 + 4);
                }

                //////////////////////end of rows
                if (columnLabelLeft < _mapDimension.x) {
                    columnLabelLeft = _mapDimension.x;
                }
                if (columnLabelLeft + columnLabelWidth > _mapDimension.x + _mapDimension.width) {
                    columnLabelLeft = _mapDimension.x + _mapDimension.width - columnLabelWidth;
                }
                g2D.setFont(labelFont);
                g2D.setStroke(UI.strokeDash1_5);
                int colX, colY;
                for (int i = fromCol; i <= toCol; i++) {
                    VNode node = _coolMapObject.getViewNodeColumn(i);
                    int nodeX = (int) (node.getViewOffset() + _mapDimension.x);
                    g2D.setColor(_hoverTipBackgroundColor);
                    colX = (int) (nodeX - 2 + node.getViewSizeInMap(_zoom.x) / 2);
                    colY = center.y - colLabelTick;
                    g2D.fillOval(colX - 2, colY - 2, 5, 5);
                    String label = node.getViewLabel();
                    int height = g2D.getFontMetrics().stringWidth(label);

//                    System.out.println(height);
                    if (columnLabelHeight < height) {
                        columnLabelHeight = height;
                    }

                    g2D.drawLine(colX, colY, columnLabelLeft + (i - fromCol) * labelSize + labelSize / 2, center.y - colLabelMargin);
                }
                g2D.setColor(_hoverTipBackgroundColor);
                columnLabelHeight += 20;

                g2D.fillRoundRect(columnLabelLeft, center.y - colLabelMargin - columnLabelHeight, columnLabelWidth, columnLabelHeight, 5, 5);

                g2D.setColor(UI.colorLightGreen0);
                g2D.fillRoundRect(columnLabelLeft + (col - fromCol) * labelSize, center.y - colLabelMargin - columnLabelHeight, labelSize, columnLabelHeight, 5, 5);

                g2D.setColor(UI.colorBlack2);
                g2D.setFont(labelFontRotated);
                for (int i = fromCol; i <= toCol; i++) {

                    VNode node = _coolMapObject.getViewNodeColumn(i);
                    g2D.drawString(node.getViewLabel(), columnLabelLeft + (i - fromCol) * labelSize + labelSize / 2 + 4, center.y - colLabelMargin - 10);
                }

                //also need to paint the value
                String value = _coolMapObject.getViewValueAsSnippet(row, col);
                g2D.setFont(labelFontValue);
                int stringWidth = g2D.getFontMetrics().stringWidth(value);
                int vHeight = 25;
                int vWidth = 20 + stringWidth;

                g2D.setColor(_hoverSubTipBackgroundColor);
                g2D.fillRoundRect(center.x - vWidth / 2, rowLabelTop + rowLabelHeight + 5, vWidth, vHeight, 5, 5);

                g2D.setColor(UI.colorBlack2);
                g2D.drawString(value, center.x - vWidth / 2 + 10, rowLabelTop + rowLabelHeight + 23);

            } catch (Exception e) {
                e.printStackTrace(); //debugging when error occurs
            }
        }

        //This layer is always visible; but opacity can be different
        private void _paintHovertip(Graphics2D g2D) {

            if (_coolMapObject == null) {
                return;
            }
            //update paint, paint grid
            //paint grid

//            g2D.setStroke(UI.stroke3);
//            g2D.setColor(UI.mixOpacity(_defaultHoverColor, shadowOpacity));
//            g2D.drawRoundRect(_hoverBounds.x - 1, _hoverBounds.y - 1, _hoverBounds.width + 2, _hoverBounds.height + 2, 2, 2);
            //Paint hte hover layer
//            if (_activeCell == null || !_activeCell.isValidCell()) {
//                //Draw an empty one used for fading.
//                Composite translucent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _opacity);
//                g2D.setComposite(translucent);
//                g2D.drawImage(_mainToolTip, hoverX, HoverLayer, null);
//            } else {
            if (_mainToolTip == null) {
                return;
            }

            //Image is drawn here.
            Point mouse = _getMouseXY();
            int hoverWidth = _mainToolTip.getWidth();
            int hoverHeight = _mainToolTip.getHeight();
            int hoverX = _hoverBounds.x - hoverWidth - 5;
            int hoverY = _hoverBounds.y - hoverHeight - 5;

            //confine
//                            if (hoverX < 0) {
//                hoverX = 0;
//            }
//
//            if (hoverX > getWidth() - hoverWidth - _layerDrawerRow.getWidth() + _layerDrawerRow.getContainerHandleWidth()) {
//                hoverX = getWidth() - hoverWidth - _layerDrawerRow.getWidth() + _layerDrawerRow.getContainerHandleWidth();
//            }
//
//            if (hoverY < _layerDrawerCol.getHeight() - _layerDrawerCol.getContainerHandleHeight()) {
//                hoverY = _layerDrawerCol.getHeight() - _layerDrawerCol.getContainerHandleHeight();
//            }
            //confine
            Rectangle jail = _getViewportBounds();
            if (!jail.contains(new Point(_hoverBounds.x, _hoverBounds.y))) {
//                Rectangle canvas = getCanvasDimension();
//                jail = new Rectangle(jail.width, 0, canvas.width - jail.width, canvas.height - jail.height);
                return;
            }

            if (hoverX < jail.x) {
                hoverX = jail.x;
            }
            if (hoverY < jail.y) {
                hoverY = jail.y;
            }

            if (hoverX + hoverWidth > jail.x + jail.width) {
                hoverX = jail.x + jail.width - hoverWidth;
            }

            if (hoverY + hoverHeight > jail.y + jail.height) {
                hoverY = jail.y + jail.height - hoverHeight;
            }

            if (_opacity < 1) {
                Composite translucent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _opacity);
                g2D.setComposite(translucent);
            }

            g2D.drawImage(_mainToolTip, hoverX, hoverY, null);

//            }
            //This is the sub tip from the view renderer if any
            ViewRenderer renderer = _coolMapObject.getViewRenderer();
            if (renderer != null && _activeCell.isValidCell(_coolMapObject)) {
                VNode rowNode = _coolMapObject.getViewNodeRow(_activeCell.getRow().intValue());
                VNode colNode = _coolMapObject.getViewNodeColumn(_activeCell.getCol().intValue());
                if (rowNode == null || colNode == null || rowNode.getViewOffset() == null || colNode.getViewOffset() == null) {
                    return;
                }

                float percentX = (mouse.x - (colNode.getViewOffset() + _mapDimension.x)) / colNode.getViewSizeInMap(_zoom.x);
                float percentY = (mouse.y - (rowNode.getViewOffset() + _mapDimension.y)) / rowNode.getViewSizeInMap(_zoom.y);

                Image subTip = renderer.getSubTip(_activeCell, percentX, percentY, Math.round(colNode.getViewSizeInMap(_zoom.x)), Math.round(rowNode.getViewSizeInMap(_zoom.y)));
                if (subTip == null) {
                    return;
                }

                int subTipWidth = subTip.getWidth(this);
                int subTipHeight = subTip.getHeight(this);

                //Subtip hovershadow
                //g2D.setColor(_hoverShadow);
                //g2D.fillRoundRect(hoverX + hoverWidth - subTipWidth - 15, hoverHeight + hoverY + 10, subTipWidth + 16, subTipHeight + 16, 8, 8);
                if (_coolMapObject.canPass(_activeCell.row.intValue(), _activeCell.col.intValue())) {
                    g2D.setColor(_hoverTipBackgroundColor);
                } else {
                    g2D.setColor(UI.colorRedWarning);
                }

                g2D.fillRoundRect(hoverX + hoverWidth - subTipWidth - 19, hoverHeight + hoverY + 6, subTipWidth + 16, subTipHeight + 16, 8, 8);

                g2D.drawImage(subTip, hoverX + hoverWidth - subTipWidth - 11, hoverY + hoverHeight + 14, this);
            }

        }

        private void _updateMainToolTip(MatrixCell cell) {
            if (cell == null || !cell.isValidCell(_coolMapObject)) {
                return;
            }
            BufferedImage image = _graphicsConfiguration.createCompatibleImage(1, 1, Transparency.OPAQUE);
            Graphics2D g2D = image.createGraphics();
            int labelFontHeight = _hoverLabelFont.getSize();
            int valueFontHeight = _hoverValueFont.getSize();
            int activeRow = cell.row.intValue();
            int activeCol = cell.col.intValue();
            VNode rowNode = _coolMapObject.getViewNodeRow(activeRow);
            VNode colNode = _coolMapObject.getViewNodeColumn(activeCol);

            String rowLabel = "";
            String colLabel = "";

            //Excellent design that let node display using other than name.
            if (rowNode != null) {
                rowLabel = rowNode.getViewLabel();
            }
            if (colNode != null) {
                colLabel = colNode.getViewLabel();
            }

            g2D.setFont(_hoverLabelFont);
            int rowLabelWidth = 0;
            int colLabelWidth = 0;

            if (rowLabel == null) {
                rowLabel = "";
            }

            if (colLabel == null) {
                colLabel = "";
            }

            rowLabelWidth = g2D.getFontMetrics().stringWidth(rowLabel);
            colLabelWidth = g2D.getFontMetrics().stringWidth(colLabel);
            int labelDescent = g2D.getFontMetrics().getMaxDescent();
            String toolTip = _coolMapObject.getViewValueAsSnippet(activeRow, activeCol);
            if (toolTip == null) {
                toolTip = "";
            }

            boolean useAggregator = false;
            String tipName = "";
            _coolMapObject.getAggregator().getTipName();
            //System.out.println(_coolMapObject.getAggregator().getTipName());

            if (rowNode != null && rowNode.isGroupNode() || colNode != null && colNode.isGroupNode() || _coolMapObject.getBaseCMatrices().size() > 1) {
                if (_coolMapObject.getAggregator() != null && _coolMapObject.getAggregator().getTipName() != null) {
                    tipName = _coolMapObject.getAggregator().getTipName();
                    //System.out.println("Get tip name:" + tipName);
                }
            }

            g2D.setFont(_tipNameFont);
            int tipWidth = g2D.getFontMetrics().stringWidth(tipName);

            if (_coolMapObject.getBaseCMatrices().size() > 1) {
                tipWidth += 20;
            }

            int additionalWidth = tipWidth - 16 + 4;
            if (additionalWidth < 0) {
                additionalWidth = 0;
            }
            int tipNameHeight = _tipNameFont.getSize();
            int tipFontDescent = g2D.getFontMetrics().getMaxDescent();

            g2D.setFont(_hoverValueFont);
            int toolTipWidth = 0;
            if (toolTip != null) {
                toolTipWidth = g2D.getFontMetrics().stringWidth(toolTip);
            }
            int valueDescent = g2D.getFontMetrics().getMaxDescent();
            int hoverHeight = 5 + 5 + 5 + 5 + labelFontHeight * 2 + valueFontHeight + 8;
            int hoverWidth = rowLabelWidth;
            if (hoverWidth < colLabelWidth) {
                hoverWidth = colLabelWidth;
            }
            if (hoverWidth < toolTipWidth) {
                hoverWidth = toolTipWidth;
            }
            hoverWidth += 16 + 5 * 2 + 5 + 5; //house 1 icon, and some other spacings
            hoverWidth += additionalWidth;

            //System.out.println(tipName + " " + additionalWidth)
            //        ;
            image = _graphicsConfiguration.createCompatibleImage(hoverWidth, hoverHeight, Transparency.TRANSLUCENT);

            Graphics2D gI = image.createGraphics();
            if (_antiAlias) {
                gI.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                gI.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            //### hover shadow
            //Hovershadow - remove
            //remove hover shadow
//            gI.setColor(_hoverShadow);
//            gI.fillRoundRect(3, 3, hoverWidth - 3, hoverHeight - 3, 8, 8);
//          change color here
//            active cell
            if (_coolMapObject.canPass(activeRow, activeCol)) {
                gI.setColor(_hoverTipBackgroundColor);
            } else {
                gI.setColor(UI.colorRedWarning);
            }

            gI.fillRoundRect(0, 0, hoverWidth - 3, hoverHeight - 3, 8, 8);
            gI.setFont(_hoverLabelFont);

            gI.drawImage(UI.getImageIcon("rowLabel").getImage(), 5 + additionalWidth, 6, null);

            if (rowNode.isGroupNode()) {
                if (rowNode.getViewColor() != null) {
                    gI.setColor(UI.mixOpacity(rowNode.getViewColor(), 0.4f));
                    gI.fillRoundRect(23 + additionalWidth, 9 - labelDescent, hoverWidth - 25 - additionalWidth, labelFontHeight + 3, 4, 4);
                } else if (rowNode.getCOntology() != null && rowNode.getCOntology().getViewColor() != null) {
                    gI.setColor(UI.mixOpacity(rowNode.getCOntology().getViewColor(), 0.4f));
                    gI.fillRoundRect(23 + additionalWidth, 9 - labelDescent, hoverWidth - 25 - additionalWidth, labelFontHeight + 3, 4, 4);
                }
            }

            gI.setColor(_labelFontColor);
            gI.drawString(rowLabel, 25 + additionalWidth, 8 + labelFontHeight - labelDescent);

            gI.drawImage(UI.getImageIcon("colLabel").getImage(), 5 + additionalWidth, 5 + 21, null);

            if (colNode.isGroupNode()) {
                if (colNode.getViewColor() != null) {
                    gI.setColor(UI.mixOpacity(colNode.getViewColor(), 0.4f));
                    gI.fillRoundRect(23 + additionalWidth, 9 - labelDescent + labelFontHeight + 3, hoverWidth - 25 - additionalWidth, labelFontHeight + 3, 4, 4);
                } else if (colNode.getCOntology() != null && colNode.getCOntology().getViewColor() != null) {
                    gI.setColor(UI.mixOpacity(colNode.getCOntology().getViewColor(), 0.4f));
                    gI.fillRoundRect(23 + additionalWidth, 9 - labelDescent + labelFontHeight + 3, hoverWidth - 25 - additionalWidth, labelFontHeight + 3, 4, 4);
                }
            }

            gI.setColor(_labelFontColor);
            gI.drawString(colLabel, 25 + additionalWidth, 15 + labelFontHeight * 2 - labelDescent * 2);
//                Composite translucent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _opacity);
//                g2D.setComposite(translucent);

            gI.setStroke(UI.strokeDash1_5);
            gI.setColor(UI.colorGrey5);
            gI.drawLine(25 + additionalWidth, 23 + labelFontHeight * 2 - labelDescent * 2, hoverWidth - 10, 23 + labelFontHeight * 2 - labelDescent * 2);

            gI.setFont(_tipNameFont);

            //gI.fillRoundRect(5, 31 + tipNameHeight + 2 + labelFontHeight * 2 - tipFontDescent - labelDescent * 2, 25 + additionalWidth, , WIDTH, WIDTH);
            int tipAnchorX = 5;
            if (_coolMapObject.getBaseCMatrices().size() > 1) {
                tipAnchorX += 20;
                gI.drawImage(UI.getImageIcon("layers").getImage(), 4, 31 + tipNameHeight + 2 + labelFontHeight * 2 - 22, null);
            }
            gI.setColor(UI.colorWhite);
            gI.drawString(tipName, tipAnchorX + 1, 31 + tipNameHeight + 2 + labelFontHeight * 2 - tipFontDescent - labelDescent * 2 + 1);
            gI.setColor(_tipNameColor);
            gI.drawString(tipName, tipAnchorX, 31 + tipNameHeight + 2 + labelFontHeight * 2 - tipFontDescent - labelDescent * 2);

            gI.setFont(_hoverValueFont);
            gI.setColor(_valueFontColor);
            gI.drawString(toolTip, 25 + additionalWidth, 31 + valueFontHeight + labelFontHeight * 2 - valueDescent - labelDescent * 2);

            //test jlabel
//          Use JLabel to write HTML will be a lot easier. Use Empty border to position; use html and preferred size            
//            JLabel label = new JLabel("<html><strong>ABCDEFG</strong><br/><font color=#ff0000>DEF</color></html>");
//            label.setSize(label.getPreferredSize());
//            System.out.println(label.getSize());
//            //use empty border to set location
//            label.setBorder(BorderFactory.createEmptyBorder(5,10,0,0));
//            
//            label.paint(gI);
            _mainToolTip = image;
        }

        ///////////////////////////////////////////
        //Animator classes
        private class TargetHoverPan implements TimingTarget {

            //private MatrixCell _startCell;
            //private MatrixCell _endCell;
            private final Rectangle _startHoverBound = new Rectangle();
            private Rectangle _endHoverBound = new Rectangle();

            public void resetStartEnd() {
                _startHoverBound.x = 0;
                _startHoverBound.y = 0;
                _startHoverBound.width = 0;
                _startHoverBound.height = 0;
                _endHoverBound.x = 0;
                _endHoverBound.y = 0;
                _endHoverBound.width = 0;
                _endHoverBound.height = 0;
            }

            public void setStartEnd(MatrixCell start, MatrixCell end) {
                //_startCell = start.duplicate();
                //_endCell = end.duplicate();
                //guaranteed valid cell.
                VNode startRowNode = _coolMapObject.getViewNodeRow(start.row.intValue());
                VNode startColNode = _coolMapObject.getViewNodeColumn(start.col.intValue());
                VNode endRowNode = _coolMapObject.getViewNodeRow(end.row.intValue());
                VNode endColNode = _coolMapObject.getViewNodeColumn(end.col.intValue());

                _startHoverBound.x = (int) (startColNode.getViewOffset() + _mapDimension.x);
                _startHoverBound.y = (int) (startRowNode.getViewOffset() + _mapDimension.y);
                _startHoverBound.width = (int) startColNode.getViewSizeInMap(_zoom.x);
                _startHoverBound.height = (int) startRowNode.getViewSizeInMap(_zoom.y);

                _endHoverBound.x = (int) (endColNode.getViewOffset() + _mapDimension.x);
                _endHoverBound.y = (int) (endRowNode.getViewOffset() + _mapDimension.y);
                _endHoverBound.width = (int) endColNode.getViewSizeInMap(_zoom.x);
                _endHoverBound.height = (int) endRowNode.getViewSizeInMap(_zoom.y);
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
                _hoverBounds.x = (int) (_startHoverBound.x + (_endHoverBound.x - _startHoverBound.x) * fraction);
                _hoverBounds.y = (int) (_startHoverBound.y + (_endHoverBound.y - _startHoverBound.y) * fraction);
                _hoverBounds.width = (int) (_startHoverBound.width + (_endHoverBound.width - _startHoverBound.width) * fraction);
                _hoverBounds.height = (int) (_startHoverBound.height + (_endHoverBound.height - _startHoverBound.height) * fraction);
                repaint();
            }
        }//hover pan

        private class TargetVisibility implements TimingTarget {

            private float _from = 1f;
            private float _to = 0f;
            private float _diff = 1f;

            public void setVisibility(float from, float to) {
                if (from < 0) {
                    from = 0;
                }
                if (from > 1) {
                    from = 1;
                }
                if (to < 0) {
                    to = 0;
                }
                if (to > 1) {
                    to = 1;
                }
                _from = from;
                _to = to;
                _diff = to - from;
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
                _opacity = (float) (_from + _diff * fraction);
                repaint();
            }
        }
    }

    /**
     *
     */
    private class RealTimeLayer extends JPanel {

        public RealTimeLayer() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);

            Point pt1 = new Point();
            Point pt2 = new Point();
            if (_subMapIndexMin.row == null || _subMapIndexMax.row == null || _subMapIndexMin.col == null || _subMapIndexMax.col == null) {
                return;
            }

            Color c = UI.mixOpacity(Color.WHITE, 0.5f);
            grphcs.setColor(Color.WHITE);
            for (int i = _subMapIndexMin.row.intValue(); i < _subMapIndexMax.row; i++) {
                pt1.y = (int) (_coolMapObject.getViewNodeRow(i).getViewOffset() + _mapDimension.y);
                pt1.x = _getViewportBounds().width;

                for (int j = _subMapIndexMin.col.intValue(); j < _subMapIndexMax.col; j++) {
                    pt2.x = (int) (_coolMapObject.getViewNodeColumn(j).getViewOffset() + _mapDimension.x);
                    pt2.y = _getViewportBounds().y;

                    Double v = (Double) _coolMapObject.getViewValue(i, j);
                    if (v >= 0.95) {
                        Path2D.Float path = new Path2D.Float();
                        path.moveTo(pt1.x, pt1.y);
                        path.curveTo(pt1.x, pt1.y, pt2.x, pt1.y, pt2.x, pt2.y);
                        ((Graphics2D) grphcs).draw(path);
                    }

                }
            }

        }
    }

    //private boolean _drawLabelsRowSelections = true;
    //private boolean _drawLabelsColumnSelections = true;
    private boolean _paintLabelAlongSelecctions = false;

    public void togglePaintLabelAlongSelections() {
        _paintLabelAlongSelecctions = !_paintLabelAlongSelecctions;
        redrawCanvas();
    }

    private class SelectionLayer extends JPanel {

        private Color _selectionInnerColor = new Color(255, 255, 255);
        private Color _selectionOutterColor = new Color(255, 255, 255, 150);
        private final Area _viewArea = new Area();

        /**
         * this must be called whenever resize... changes
         */
        public synchronized void updateViewArea() {
            _viewArea.reset();
            Set<Rectangle> viewRegions = convertNodeRegionToViewRegion(_selections);
//            if(viewRegions.isEmpty()){
//                System.out.println("Returned an empty area");
//            }
            //System.out.println(viewRegions.size());
//            if(viewRegions.isEmpty()){
//                System.out.println("Empty view region");
//            }
            //but returned a 0,0,0,0?why?

            for (Rectangle region : viewRegions) {
                _viewArea.add(new Area(region));
            }
            repaint();
        }

        public SelectionLayer() {
            setOpaque(false);
            labelFont = UI.fontPlain.deriveFont(labelFontSize).deriveFont(Font.BOLD);
            AffineTransform at = new AffineTransform();
            at.rotate(-Math.PI / 2);
            labelFontRotated = labelFont.deriveFont(at);
            labelFontValue = UI.fontPlain.deriveFont(16f).deriveFont(Font.BOLD);
            labelBackgroundColor = UI.mixOpacity(UI.colorGrey2, 0.8f);
        }

        @Override
        protected void paintComponent(Graphics grphcs) {

            super.paintComponent(grphcs);

            if (_viewArea.isEmpty()) {
                return;
            }

            Graphics2D g2D = (Graphics2D) grphcs;
            //No need to antiAlias

//            Rectangle bounds = getCanvasDimension();
//            bounds.grow(50, 50);
//            Area boundArea = new Area(bounds);
//            boundArea.intersect(_viewArea);            
            //Sometimes the viewArea becomes empty. Which should not be true
            //System.out.println(_viewArea.isEmpty());
            g2D.setColor(_selectionOutterColor);
            g2D.setStroke(UI.stroke3);
            g2D.draw(_viewArea);
            g2D.setColor(_selectionInnerColor);
            g2D.setStroke(UI.stroke1);
            g2D.draw(_viewArea);
//            g2D.setStroke(UI.strokeDash1_5);
//            g2D.setColor(_selectionInnerColor);
//            g2D.draw(_viewArea);
            //It can be zero
            //System.out.println(_viewArea.getBounds());
//            Rectangle bounds = _viewArea.getBounds();

//            if(bounds.width == 0 || bounds.height == 0){
//                System.out.println("Bound is empty:" + _viewArea.isEmpty() + ":" + bounds);
//            }
//            if(_drawLabelsAlongSelections && _coolMapObject != null ){
//                ArrayList<Range<Integer>> selectedRows = getSelectedRows()
//            }
            if (_paintLabelAlongSelecctions) {
                _paintLabels(g2D);
            }
        }

        private float labelFontSize = 12f;
        private int labelSize = 18;
        private Font labelFont;
        private Font labelFontRotated;
        private Font labelFontValue;
        private Color labelBackgroundColor;

        private void _paintLabels(Graphics2D g2D) {

            try {
                Rectangle selection = getSelectionsUnion();
                int toCol = selection.x + selection.width - 1;
                int fromCol = selection.x;
                int fromRow = selection.y;
                int toRow = selection.y + selection.height - 1;

                int rowCount = selection.height;
                int colCount = selection.width;

//                System.out.println(toCol + "  " + fromRow);
                //draw right of toCol, and draw top of from Row
                //similar to the idea of floating labels
                //need to find the center firt, then everything else can be copied
                int row = rowCount / 2 + fromRow;
                int col = colCount / 2 + fromCol;

                int centerY = _mapDimension.y + _coolMapObject.getViewNodeRow(row).getrViewOffsetCenter(_zoom.y).intValue();
                int centerX = _mapDimension.x + _coolMapObject.getViewNodeColumn(col).getrViewOffsetCenter(_zoom.x).intValue();

                int rowLabelLeft = (int) (_mapDimension.x + _coolMapObject.getViewNodeColumn(toCol).getViewOffset(_zoom.x)) + 10;
                int colLabelBottom = (int) (_mapDimension.y + _coolMapObject.getViewNodeRow(fromRow).getViewOffset() - 10);

//                int rowLabelHeight = (rowCount) * labelSize;
                //int rowLabelHeight = (rowCount) * _zoom.y;
                int rowLabelHeight = VNode.distanceInclusive(_coolMapObject.getViewNodeRow(fromRow), _coolMapObject.getViewNodeRow(toRow), _zoom.y);
                int colLabelWidth = VNode.distanceInclusive(_coolMapObject.getViewNodeColumn(fromCol), _coolMapObject.getViewNodeColumn(toCol), _zoom.x);

                int rowLabelWidth = 5;
                int rowLabelTop = (int) (_mapDimension.y + _coolMapObject.getViewNodeRow(fromRow).getViewOffset());

//                int colLabelWidth = (colCount) * labelSize;
                int colLabelHeight = 5;
                int colLabelLeft = (int) (_mapDimension.x + _coolMapObject.getViewNodeColumn(fromCol).getViewOffset());

                //now draw the labels, same pain as before
                labelFont = _zoomControlY.getBoldFont();
                g2D.setFont(labelFont);
                for (int i = fromRow; i <= toRow; i++) {
                    String value = _coolMapObject.getViewNodeRow(i).getViewLabel();
                    int sWidth = g2D.getFontMetrics().stringWidth(value);
                    if (sWidth > rowLabelWidth) {
                        rowLabelWidth = sWidth;
                    }
                }

                rowLabelWidth += 20;

                labelFont = _zoomControlX.getBoldFont();
                g2D.setFont(labelFont);
                for (int i = fromCol; i <= toCol; i++) {
                    String value = _coolMapObject.getViewNodeColumn(i).getViewLabel();
                    int sWidth = g2D.getFontMetrics().stringWidth(value);
                    if (sWidth > colLabelHeight) {
                        colLabelHeight = sWidth;
                    }
                }

                colLabelHeight += 20;

                g2D.setColor(UI.colorBlack2);

                g2D.setColor(labelBackgroundColor);

                //Using similar approaches as hover is not a good idea... won't be able to read all labels anyway
                //center to labels then
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2D.fillRoundRect(rowLabelLeft, rowLabelTop, rowLabelWidth, rowLabelHeight, 5, 5);
                g2D.fillRoundRect(colLabelLeft, colLabelBottom - colLabelHeight, colLabelWidth, colLabelHeight, 5, 5);

                g2D.setColor(UI.colorBlack2);

//                color it!
                labelFont = _zoomControlY.getBoldFont();
                g2D.setFont(labelFont);
                int maxDescent = g2D.getFontMetrics().getMaxDescent();
                for (int i = fromRow; i <= toRow; i++) {
                    VNode node = _coolMapObject.getViewNodeRow(i);
                    g2D.drawString(node.getViewLabel(), rowLabelLeft + 10, node.getrViewOffsetCenter(_zoom.y) + _mapDimension.y + 6 - maxDescent);
                }

                labelFont = _zoomControlX.getBoldFont();
                g2D.setFont(labelFont);
                maxDescent = g2D.getFontMetrics().getMaxDescent();
                AffineTransform at = new AffineTransform();
                at.rotate(-Math.PI / 2);
                labelFontRotated = _zoomControlX.getBoldFont().deriveFont(at);
                g2D.setFont(labelFontRotated);
                for (int i = fromCol; i <= toCol; i++) {
                    VNode node = _coolMapObject.getViewNodeColumn(i);
                    g2D.drawString(node.getViewLabel(), node.getrViewOffsetCenter(_zoom.x) + _mapDimension.x + 6 - maxDescent, colLabelBottom - 10);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Shows some short message
     */
    private class ActivationIndicatorLayer extends JPanel implements FocusListener {

        @Override
        public void setBounds(Rectangle rctngl) {
            super.setBounds(0, 0, _canvas.getWidth(), 5);
        }
        private Font _messageFont = UI.fontPlain.deriveFont(10f);
        private Color _fontColor = UI.colorBlack2;
//        private FadeTarget _fadeTarget = new FadeTarget();
//        private Animator _fadeAnimator = CAnimator.createInstance(_fadeTarget, 200);
//        private float _opacity = 0.0f;
        private Color _backgroundColor = UI.colorLightBlue0;

        public ActivationIndicatorLayer() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);
            Graphics2D g2D = (Graphics2D) grphcs;
            if (_antiAlias) {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            //Eventually, if it's the active CoolMap Object
            if (_canvas.hasFocus()) {
                g2D.setStroke(UI.stroke1);
                g2D.setColor(_backgroundColor);
                g2D.fillRect(0, 0, _canvas.getWidth(), 3);
            } else {
                //do nothing
            }
        }
//
//        private class FadeTarget implements TimingTarget {
//
//            @Override
//            public void begin(Animator source) {
//                _opacity = 1.0f;
//            }
//
//            @Override
//            public void end(Animator source) {
//                setVisible(false);
//            }
//
//            @Override
//            public void repeat(Animator source) {
//            }
//
//            @Override
//            public void reverse(Animator source) {
//            }
//
//            @Override
//            public void timingEvent(Animator source, double fraction) {
//                _opacity = (float)(1 - fraction);
//                repaint(0, 0, getWidth(), 15);
//            }
//        }

        @Override
        public void focusGained(FocusEvent fe) {
            //System.out.println("gained focus");
            repaint(0, 0, _canvas.getWidth(), 3);
        }

        @Override
        public void focusLost(FocusEvent fe) {
            //System.out.println("lost focus");
            repaint(0, 0, _canvas.getWidth(), 3);
        }
    }

    private class MapMover implements KeyListener, MouseWheelListener {

        private MoverTarget _moverTarget = new MoverTarget();
        private Animator _moverAnimator = CAnimator.createInstance(_moverTarget, 200);
        private int _step = 40;
        private int _bigStep = 100;
        public final int UP = KeyEvent.VK_UP;
        public final int DOWN = KeyEvent.VK_DOWN;
        public final int LEFT = KeyEvent.VK_LEFT;
        public final int RIGHT = KeyEvent.VK_RIGHT;

        @Override
        public void keyTyped(KeyEvent ke) {
        }

        @Override
        public void keyPressed(KeyEvent ke) {
//            System.out.println(ke);

            Rectangle viewport = _getViewportBounds();
            if (viewport == null) {
                return;
            }

            Integer centerRow = getCurrentRow(viewport.y + viewport.height / 2);
            Integer centerCol = getCurrentCol(viewport.x + viewport.width / 2);

            if (ke.isMetaDown() || ke.isControlDown()) {
                if (ke.getKeyCode() == LEFT) {
                    if (centerRow == null || centerRow == -1) {
                        centerRow = 0;
                    } else if (centerRow == _coolMapObject.getViewNumRows()) {
                        centerRow = _coolMapObject.getViewNumRows() - 1;
                    }
                    centerToRegion(new Rectangle(0, centerRow, 1, 1));
//                    Point mouse = _getMouseXY();
//                    setMouseXY(mouse.x, mouse.y);
//                    _hoverLayer.setActiveCell(_activeCell);
                }
                if (ke.getKeyCode() == RIGHT) {
                    if (centerRow == null || centerRow == -1) {
                        centerRow = 0;
                    } else if (centerRow == _coolMapObject.getViewNumRows()) {
                        centerRow = _coolMapObject.getViewNumRows() - 1;
                    }
                    centerToRegion(new Rectangle(_coolMapObject.getViewNumColumns() - 1, centerRow, 1, 1));
//                    Point mouse = _getMouseXY();
//                    setMouseXY(mouse.x, mouse.y);
//                    _hoverLayer.setActiveCell(_activeCell);
                }
                if (ke.getKeyCode() == UP) {
                    if (centerCol == null || centerCol == -1) {
                        centerCol = 0;
                    } else if (centerCol == _coolMapObject.getViewNumColumns()) {
                        centerCol = _coolMapObject.getViewNumColumns() - 1;
                    }
                    centerToRegion(new Rectangle(centerCol, 0, 1, 1));
//                    Point mouse = _getMouseXY();
//                    setMouseXY(mouse.x, mouse.y);
//                    _hoverLayer.setActiveCell(_activeCell);
                }
                if (ke.getKeyCode() == DOWN) {
                    if (centerCol == null || centerCol == -1) {
                        centerCol = 0;
                    } else if (centerCol == _coolMapObject.getViewNumColumns()) {
                        centerCol = _coolMapObject.getViewNumColumns() - 1;
                    }
                    centerToRegion(new Rectangle(centerCol, _coolMapObject.getViewNumRows() - 1, 1, 1));
//                    Point mouse = _getMouseXY();
//                    setMouseXY(mouse.x, mouse.y);
//                    _hoverLayer.setActiveCell(_activeCell);
                }

            } else if (ke.isShiftDown()) {

                move(_bigStep, ke.getKeyCode());

            } else {

                move(_step, ke.getKeyCode());
            }

        }

        @Override
        public void keyReleased(KeyEvent ke) {
            //Justify grid
            //System.out.println("Key released");
            _hoverLayer.setActiveCell(_activeCell);

        }

        public void move(int step, int direction) {
            Point currentAnchor = getMapAnchor();
            Point newAnchor;
//            
//            new Point(currentAnchor.x + _smallStep, currentAnchor.y + _smallStep);
//            System.out.println(direction + " " + UP);

            switch (direction) {
                case UP:
                    newAnchor = new Point(currentAnchor.x, currentAnchor.y - step);
                    break;
                case DOWN:
                    newAnchor = new Point(currentAnchor.x, currentAnchor.y + step);
                    break;
                case LEFT:
                    newAnchor = new Point(currentAnchor.x - step, currentAnchor.y);
                    break;
                case RIGHT:
                    newAnchor = new Point(currentAnchor.x + step, currentAnchor.y);
                    break;
                default:
                    return;
            }

            if (_moverAnimator.isRunning()) {
                _moverAnimator.cancel();
            }
            _moverTarget.setStartEnd(currentAnchor, newAnchor);
            _moverAnimator.start();

        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent mwe) {
            int rotation = mwe.getWheelRotation();
            //System.out.println(rotation);
            if (rotation >= 3) {
                if (mwe.isAltDown() || mwe.isMetaDown() || mwe.isControlDown()) {
                    move(_bigStep, LEFT);
                } else {
                    move(_bigStep, UP);
                }
            } else if (rotation > 0) {
                if (mwe.isAltDown() || mwe.isMetaDown() || mwe.isControlDown()) {
                    move(_step, LEFT);
                } else {
                    move(_step, UP);
                }
            } else if (rotation > -3) {
                if (mwe.isAltDown() || mwe.isMetaDown() || mwe.isControlDown()) {
                    move(_step, RIGHT);
                } else {
                    move(_step, DOWN);
                }
            } else if (rotation <= -3) {
                if (mwe.isAltDown() || mwe.isMetaDown() || mwe.isControlDown()) {
                    move(_bigStep, RIGHT);
                } else {
                    move(_bigStep, DOWN);
                }
            }
        }

        private class MoverTarget implements TimingTarget {

            private final Point _startAnchor = new Point();
            private final Point _endAnchor = new Point();

            public MoverTarget() {
            }

            public void setStartEnd(Point start, Point end) {
                _startAnchor.x = start.x;
                _startAnchor.y = start.y;
                _endAnchor.x = end.x;
                _endAnchor.y = end.y;
                //System.out.println(_startAnchor + " " + _endAnchor);
            }

            @Override
            public void begin(Animator source) {
            }

            @Override
            public void end(Animator source) {
                Point mouse = _getMouseXY();
                setMouseXY(mouse.x, mouse.y);
                _hoverLayer.setActiveCell(_activeCell);
                updateCanvasIfNecessary();
                _fireViewAnchorMoved();
            }

            @Override
            public void repeat(Animator source) {
            }

            @Override
            public void reverse(Animator source) {
            }

            @Override
            public void timingEvent(Animator source, double fraction) {
                //System.out.println("moved");

                _moveMapTo((int) (_startAnchor.x + (_endAnchor.x - _startAnchor.x) * fraction),
                        (int) (_startAnchor.y + (_endAnchor.y - _startAnchor.y) * fraction), false);

                //due to the fact that the active row/cell not changed, but the map anchor has changed
                _justifyView();
            }
        }
    }

    /**
     * called if active row/column not changed, but the anchor of the map has
     * changed (such as map was changed without dragging ) could only be a
     * problem if animation is used to update anchors.
     */
    private void _justifyView() {
        _selectionLayer.updateViewArea();
        _rowDrawer.justifyView();
        _colDrawer.justifyView();
    }

    private class NotificationLayer extends JPanel {

        private Color _bgColor = UI.mixOpacity(Color.WHITE, 0.2f);
        private Color _warningBgColor = UI.mixOpacity(UI.colorRedWarning, 0.3f);
        private Color _activeBgColor = UI.mixOpacity(UI.colorLightBlue0, 0.3f);

        public NotificationLayer() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);
            Graphics2D g = (Graphics2D) grphcs;
            if (_antiAlias) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            //Don't want to refer to CoolMapMaster...
            if (_isActive) {

                g.setColor(_bgColor);
                g.fillRoundRect(getWidth() - 25, 5, 17, 20, 5, 5);
                g.drawImage(UI.getImageIcon("powerOnSmall").getImage(), getWidth() - 24, 8, this);
            }

            if (_isKeyboardActive) {
                if (_isActive) {
                    g.setColor(_activeBgColor);
                } else {
                    g.setColor(_warningBgColor);
                }
                g.fillRoundRect(getWidth() - 26 - 24 + 3 + 2, 5, 18, 20, 5, 5);
                g.drawImage(UI.getImageIcon("keyboard").getImage(), getWidth() - 24 - 24 - 1 + 3 + 2, 8 - 1, this);
            }

            if (_coolMapObject.getBaseCMatrices().size() > 1) {
                g.setColor(_bgColor);
                g.fillRoundRect(getWidth() - 26 - 48 + 3 + 2 + 4, 5, 18, 20, 5, 5);
                g.drawImage(UI.getImageIcon("layers").getImage(), getWidth() - 24 - 48 - 1 + 3 + 2 + 4, 8 - 1, this);
            }

            if (_coolMapObject.isFilterActive()) {
                g.setColor(_bgColor);
                g.fillRoundRect(getWidth() - 26 - 72 + 3 + 2 + 4 + 4, 5, 18, 20, 5, 5);
                g.drawImage(UI.getImageIcon("funnel").getImage(), getWidth() - 24 - 72 - 1 + 3 + 2 + 4 + 4, 8 - 1, this);
            }

            _paintLinkIcons(g);

            _paintAggr(g);

            _paintLegend(g);
        }
        private Font tipFont = UI.fontMono.deriveFont(12f).deriveFont(Font.BOLD);

        private void _paintLegend(Graphics2D g) {
            if (_coolMapObject == null) {
                return;
            }

            ViewRenderer renderer = _coolMapObject.getViewRenderer();
            if (renderer == null) {
                return;
            }

            Image lgd = renderer.getLegend();
            if (lgd == null) {
                return;
            }

            g.setColor(_bgColor);
            g.fillRoundRect(getWidth() - 8 - lgd.getWidth(this) - 10, 29 + 24 + 24, lgd.getWidth(this) + 10, lgd.getHeight(this) + 10, 5, 5);
            g.drawImage(lgd, getWidth() - 8 - lgd.getWidth(this) - 5, 29 + 24 + 24 + 5, this);
        }

        private void _paintAggr(Graphics2D g) {
            if (_coolMapObject == null) {
                return;
            }

            CAggregator aggr = _coolMapObject.getAggregator();
            if (aggr == null) {
                return;
            }

            String tip = aggr.getTipName();
            if (tip == null || tip.length() == 0) {
                tip = "Unnamed Aggr";
            }

            g.setFont(tipFont);

            int width = g.getFontMetrics().stringWidth(tip) + 6;

            g.setColor(_bgColor);
            g.fillRoundRect(getWidth() - width - 8, 29 + 24, width, 18, 5, 5);
//            g.setColor(UI.colorGrey2);
//            g.drawString(tip, getWidth() - width - 8 + 4, 29 + 24 + 18 - 4);
            g.setColor(UI.colorGrey2);
            g.drawString(tip, getWidth() - width - 8 + 3, 29 + 24 + 18 - 5);
        }
        private ArrayList<Image> _linkIcons = new ArrayList(10);

        private void _paintLinkIcons(Graphics2D g2D) {
            _linkIcons.clear();

            if (_anchorSynced) {
                _linkIcons.add(UI.getImageIcon("anchor").getImage());
            }

            if (_activeCellSynced) {
                _linkIcons.add(UI.getImageIcon("activeCell").getImage());
            }

            if (_zoomSynced) {
                _linkIcons.add(UI.getImageIcon("search").getImage());
            }

            if (_rowSelectionSynced) {
                _linkIcons.add(UI.getImageIcon("rangeRow").getImage());
            }

            if (_columnSelectionSynced) {
                _linkIcons.add(UI.getImageIcon("rangeColumn").getImage());
            }

            if (_rowLayoutSynced) {
                _linkIcons.add(UI.getImageIcon("rowLabel").getImage());
            }

            if (_columnLayoutSynced) {
                _linkIcons.add(UI.getImageIcon("colLabel").getImage());
            }

            //System.out.println(_linkIcons.size() + " " + _activeCellSynced);
            if (_linkIcons.isEmpty()) {
                //System.out.println("Is empty");
                return;
            } else {

                int width = _linkIcons.size() * 18 + 2;
                int height = 20;

                g2D.setColor(_bgColor);
                g2D.fillRoundRect(getWidth() - width - 8, 29, width, height, 5, 5);

                int anchorX = getWidth() - width - 8 + 2;
                int anchorY = 31;

                int counter = 0;
                for (Image img : _linkIcons) {
                    g2D.drawImage(img, anchorX + counter * 18, anchorY, null);
                    counter++;
                }
            }
        }
    }
    private boolean _isActive = false;
    private boolean _isKeyboardActive = false;

    private boolean isActive() {
        return _isActive;
    }

    public void setActive(boolean active) {
        _isActive = active;
    }
}
