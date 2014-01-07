/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data;

import com.google.common.collect.Range;
import coolmap.application.CoolMapMaster;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.canvas.sidemaps.impl.ColumnLabels;
import coolmap.canvas.sidemaps.impl.ColumnTree;
import coolmap.canvas.sidemaps.impl.RowLabels;
import coolmap.canvas.sidemaps.impl.RowTree;
import coolmap.data.aggregator.impl.DoubleDoubleMax;
import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.annotation.AnnotationStorage;
import coolmap.data.cmatrix.impl.DoubleCMatrix;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.cmatrixview.utils.SortTracker;
import coolmap.data.cmatrixview.utils.VNodeColumnSorter;
import coolmap.data.cmatrixview.utils.VNodeRowSorter;
import coolmap.data.contology.model.COntology;
import coolmap.data.contology.utils.COntologyUtils;
import coolmap.data.filter.CombinationFilter;
import coolmap.data.filter.ViewFilter;
import coolmap.data.listeners.CObjectListener;
import coolmap.data.snippet.SnippetConverter;
import coolmap.data.snippet.SnippetMaster;
import coolmap.data.state.CObjectStateStoreListener;
import coolmap.data.state.CoolMapState;
import coolmap.utils.TableCache;
import coolmap.utils.Tools;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gangsu
 */
public final class CoolMapObject<BASE, VIEW> {

    @Override
    public String toString() {
        //    return super.toString(); //To change body of generated methods, choose Tools | Templates.
        return getName();
    }

    private final String _ID;
    private String _name = "Untitled";
    //Basic instance
    //can contain many matrices
    private ArrayList<CMatrix<BASE>> _cMatrices = new ArrayList<CMatrix<BASE>>();
    //vMatrix carries all the other info
    private final VMatrix<BASE, VIEW> _vMatrix = new VMatrix<>();
    ;
    //Aggregator
    private CAggregator<BASE, VIEW> _cAggregator = null;
    //cache
    protected TableCache<VIEW> _cache = new TableCache<VIEW>();
    //filter
    //protected Filter<VIEW> _viewFilter = null;
    //view renderer
    protected ViewRenderer<VIEW> _viewRenderer = null;
    //annotation renderer
    protected SnippetConverter _snippetConverter = null;
    private final HashSet<CObjectListener> _coolMapDataListeners = new HashSet<CObjectListener>();
    private final HashSet<CObjectStateStoreListener> _cObjectStateRestoreListeners = new HashSet<CObjectStateStoreListener>();

//    private StateStorage _stateStorage;
    private final CombinationFilter _masterFilter;

    private final AnnotationStorage annotationStorage = new AnnotationStorage();

    public AnnotationStorage getAnnotationStorage() {
        return annotationStorage;
    }

    public void addViewFilter(ViewFilter filter) {
        if (filter != null && filter.canFilter(getViewClass())) {
            _masterFilter.addFilter(filter);
            notifyViewRendererUpdated();
        }
    }

//    public void clearViewFilter(ViewFilter filter) {
//        _masterFilter.clearFilters();
//        notifyFilterUpdated();
//    }
//    public void clearStateStorage() {
//        _stateStorage.clear();
//        notifyStateStorageUpdated();
//    }
    public void removeViewFilter(ViewFilter filter) {
        if (filter != null && _masterFilter.getCurrFilters().contains(filter)) {
            _masterFilter.removeFilter(filter); //filter deleted
            notifyFilterUpdated();
        }
    }

    public void setViewFilterMode(int mode) {
        if (mode == CombinationFilter.AND) {
            _masterFilter.setMode(mode);
            notifyFilterUpdated();
        } else if (mode == CombinationFilter.OR) {
            _masterFilter.setMode(mode);
            notifyFilterUpdated();
        }
    }

    public int getViewFilterMode() {
        return _masterFilter.getFilterMode();
    }

    public boolean canPass(int viewRow, int viewColumn) {
        return _masterFilter.canPass(this, viewRow, viewColumn);
    }

    public boolean isFilterActive() {
        if (_masterFilter.getCurrFilters().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public ArrayList<ViewFilter> getActiveFilters() {
        return _masterFilter.getCurrFilters();
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
        if (getCoolMapView() != null && getCoolMapView().getViewFrame() != null) {
            getCoolMapView().getViewFrame().setTitle(name);
        }

        for (CObjectListener lis : _coolMapDataListeners) {
            lis.nameChanged(this);
        }

    }

    /**
     * returns all view base nodes that has the name in row
     */
    public List<VNode> getViewNodesRow(String nodeName) {
        return _vMatrix.getActiveRowNodes(nodeName);
    }

    /**
     * returns all view base nodes that has the name in column
     *
     * @param nodeName
     * @return
     */
    public List<VNode> getViewNodesColumn(String nodeName) {
        return _vMatrix.getActiveColumnNodes(nodeName);
    }

    /**
     * returns a subset of tree nodes that currently have view index between
     * fromViewIndex and toViewIndex
     *
     * @param fromViewIndex
     * @param toViewIndex
     * @return
     */
    public List<VNode> getViewNodesRowTree(float fromViewIndex, float toViewIndex) {
        return _vMatrix.getTreeNodesRow(fromViewIndex, toViewIndex);
    }

    /**
     * returns a subset of tree nodes that currently have view index between
     * fromViewIndex and toViewIndex
     *
     * @param fromViewIndex
     * @param toViewIndex
     * @return
     */
    public List<VNode> getViewNodesColumnTree(float fromViewIndex, float toViewIndex) {
        return _vMatrix.getTreeNodesColumn(fromViewIndex, toViewIndex);
    }

    /**
     * saves the row/column layout
     */
//    public void saveViewState(String name, int direction) {
//        if (direction == COntology.ROW || direction == COntology.COLUMN) {
//            if (name == null) {
//                name = "";
//            }
//            //snapshot
//            StateSnapshot snapshot = new StateSnapshot(this, direction);
//        }
//    }
//    public StateStorage getStateStorage() {
//        return _stateStorage;
//    }
    public void addCObjectDataListener(CObjectListener lis) {
        _coolMapDataListeners.add(lis);
    }

    public void removeCObjectListener(CObjectListener lis) {
        _coolMapDataListeners.remove(lis);
    }

    public void addCObjectStateRestoreListener(CObjectStateStoreListener lis) {
        _cObjectStateRestoreListeners.add(lis);
    }

    public void removeCObjectStateRestoreListener(CObjectStateStoreListener lis) {
        _cObjectStateRestoreListeners.remove(lis);
    }

    //This model doesn't help, as one aggregator will always return to the VIEW class
    //VIEW class is always the same
//    protected CMatrixAggregator _baseAggregator = null;
//    protected final HashMap<VNode, CAggregator> _rowAggregators = new HashMap<VNode, CAggregator>();
//    protected final HashMap<VNode, CAggregator> _colAggregators = new HashMap<VNode, CAggregator>();
    //Renderer, Annotator, Etc.
    //Renderer model will be considered later
    //cache
    /**
     * does view contain both row and column nodes.
     *
     * @return
     */
//    public void centerSelections() {
//        _canvas.centerToSelections();
//    }
    /**
     * an aggregator must be assigned; otherwise the classes are unknown. but
     * the base class must also be known.
     */
    public Class<BASE> getBaseClass() {
        if (_cMatrices.isEmpty()) {
            return null;
        } else {
            return _cMatrices.get(0).getMemberClass();
        }
    }

    public Class<VIEW> getViewClass() {
        if (_cAggregator == null) {
            return null;
        } else {
            return _cAggregator.getViewClass();
        }
    }

//    public void resetSizeRows() {
//        _vMatrix.resetActiveRowNodeDisplayMultipliers();
//        _canvas.updateNodeDisplayParams();
//        _canvas.updateCanvas(true);
//    }
//
//    public void resetSizeCols() {
//        _vMatrix.resetActiveColNodeDisplayMultipliers();
//        _canvas.updateNodeDisplayParams();
//        _canvas.updateCanvas(true);
//    }
//
//    public void resetSizeBoth() {
//        _vMatrix.resetActiveRowNodeDisplayMultipliers();
//        _vMatrix.resetActiveColNodeDisplayMultipliers();
//        _canvas.updateNodeDisplayParams();
//        _canvas.updateCanvas(true);
//    }
    public CoolMapView<BASE, VIEW> getCoolMapView() {
        return _coolMapView;
    }
//    public void zoomIn(boolean zoomX, boolean zoomY) {
//        _canvas.zoomIn(zoomX, zoomY);
//    }
//
//    public void zoomOut(boolean zoomX, boolean zoomY) {
//        _canvas.zoomOut(zoomX, zoomY);
//    }
    private final CoolMapView<BASE, VIEW> _coolMapView = new CoolMapView<BASE, VIEW>(this);

//    public JComponent getCanvas() {
//        return _canvas.getRenderCanvas();
//    }
    public boolean isViewMatrixValid() {
        if (getViewNumColumns() > 0 && getViewNumRows() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCellSelected(int row, int col) {
        return _coolMapView.isCellSelected(row, col);
    }

//    public synchronized void toggleGridMode(boolean gridMode) {
//        _canvas.toggleGridMode(gridMode);
//    }
    /**
     * a converter used to convert view matrix value into a short phrase for
     * tool tip
     *
     * @param converter
     */
    public void setSnippetConverter(SnippetConverter converter) {
        try {
            _snippetConverter = converter;
        } catch (Exception e) {
            _snippetConverter = null;
        }
    }

    public SnippetConverter getSnippetConverter() {
        return _snippetConverter;
    }

    public CoolMapObject() {
        this(null);
    }

    public CoolMapObject(String ID) {
        if (ID == null || ID.length() > 0) {
            _ID = Tools.randomID();
        } else {
            _ID = ID;
        }

//        _stateStorage = new StateStorage();
        _masterFilter = new CombinationFilter(this);

//        if (baseMatrix != null) {
//            setBaseMatrix(baseMatrix);
//            setName(baseMatrix.getName());
//        }
//        addCObjectStateRestoreListener(new ZoomTracker(this));
    }

    /**
     * set view renderer maybe an event is needed to notify changed
     */
    public void setViewRenderer(ViewRenderer<VIEW> viewRenderer, boolean initialize) {
        _viewRenderer = viewRenderer;
        if (_viewRenderer != null) {
//            System.out.println("Set the view renderer to: " + initialize);
            _viewRenderer.setCoolMapObject(this, initialize);
            notifyViewRendererUpdated();
        }
    }

//    public void setAnnotationRenderer(AnnotationRenderer<BASE, VIEW> annotationRenderer) {
//        _annotationRenderer = annotationRenderer;
//    }
    /**
     * get view value, attempt to retreive from cache used for generate tooltips
     *
     * @param row
     * @param column
     * @return
     */
//    private VIEW _getViewValueWCache(int row, int column) {
//        VIEW value;
//        if (_cache.contains(row, column)) {
//            value = _cache.get(row, column);
//        } else {
//            value = _vMatrix.getValue(row, column, _cAggregator, _cMatrices);
//            _cache.put(row, column, value);
//        }
//        return value;
//    }
//    public void setViewFilter(Filter<VIEW> viewFilter) {
//        _viewFilter = viewFilter;
//    }
    /**
     * get view cache directly from underlying view matrix used for rendering
     *
     * @param row
     * @param column
     * @return
     */
    public VIEW getViewValue(int row, int column) {
        //get nodes
        VNode rowNode = getViewNodeRow(row);
        VNode colNode = getViewNodeColumn(column);

        VIEW value = _vMatrix.getValue(row, column, _cAggregator, _cMatrices);

//        if (rowNode.getName().equals("APC4")) {
//            System.out.println(rowNode + " " + colNode + ":" + value);
//        }
        return value;

    }

//    public List<VNode> getViewNodesRowFromTreeNodes() {
//        return _vMatrix.getActiveRowNodes();
//    }
//
//    public List<VNode> getViewNodesCol() {
//        return _vMatrix.getActiveColNodes();
//    }
    /**
     * consider enforce cache mechanism later
     */
    public String getViewValueAsSnippet(int row, int column) {
        VIEW value = getViewValue(row, column);
        if (value == null) {
            return null;
        }
        if (_snippetConverter != null && _snippetConverter.canConvert(value.getClass())) {
            return _snippetConverter.convert(value);
        }

        //default
        return value.toString();
    }

    public BASE getBaseValue(int row, int column, int matrixIndex) {
        if (_cMatrices.isEmpty() || matrixIndex < 0 || matrixIndex >= _cMatrices.size()) {
            return null;
        } else {
            CMatrix<BASE> mx = _cMatrices.get(matrixIndex);
            if (mx == null) {
                return null;
            }

            if (row < 0 || column < 0 || row >= mx.getNumRows() || column >= mx.getNumColumns()) {
                return mx.getValue(row, column);
            } else {
                return null;
            }
        }
    }

    /**
     * only removes nodes without a parent.
     *
     * @param nodes
     */
    public void removeViewNodesColumn(Collection<VNode> nodes) {
        //make sure only nodes with no parents
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        HashSet<VNode> nodesToBeRemoved = new HashSet<VNode>(nodes.size());
        for (VNode node : nodes) {
            if (node != null && node.getParentNode() == null) {
                nodesToBeRemoved.add(node);
            }
        }

        //This is dangerous. The parents must be checked;
        _vMatrix.removeActiveColNodes(nodesToBeRemoved);

        _coolMapView.updateNodeDisplayParams();
        _sortTracker.clearSortedRow();
        _sortTracker.clearSortedColumn();
        getCoolMapView().clearSelection();
        getCoolMapView().updateCanvasEnforceAll();
        notifyColumnsChanged();

        //System.out.println(getViewValue(3, 10));
    }

    public void removeViewNodesRow(Collection<VNode> nodes) {
        //make sure only nodes with no parents
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

//        System.out.println(System.currentTimeMillis());
        HashSet<VNode> nodesToBeRemoved = new HashSet<VNode>(nodes.size());
        for (VNode node : nodes) {
            if (node != null && node.getParentNode() == null) {
                nodesToBeRemoved.add(node);
            }
        }
//        System.out.println(System.currentTimeMillis());

        //slow
        _vMatrix.removeActiveRowNodes(nodesToBeRemoved);

//        System.out.println(System.currentTimeMillis());
        _coolMapView.updateNodeDisplayParams();
        _sortTracker.clearSortedColumn();
        _sortTracker.clearSortedRow();
        getCoolMapView().clearSelection();
        getCoolMapView().updateCanvasEnforceAll();
        notifyRowsChanged();
    }

    public int getViewNumRows() {
//        if (_vMatrix == null) {
//            return 0;
//        }
        return _vMatrix.getNumRows();
    }

    public int getViewNumColumns() {
//        if (_vMatrix == null) {
//            return 0;
//        }
        return _vMatrix.getNumCols();
    }

//    public boolean isViewCellVisible(int row, int col) {
//        if (_viewFilter == null) {
//            return true;
//        } else {
//            return _viewFilter.canPass(this, row, col);
//        }
//    }
    public void printViewMatrix() {
        printViewMatrix(getViewNumRows(), getViewNumColumns());
    }

    public void insertRowNodes(int index, List<VNode> nodes, boolean select) {

//        StateSnapshot snapshot = new StateSnapshot(this, COntology.ROW, StateSnapshot.ROWINSERT);
//        getStateStorage().addState(snapshot);
//        notifyStateStorageUpdated();
        nodes.removeAll(Collections.singletonList(null));
        getCoolMapView().clearSelection();

        _vMatrix.insertActiveRowNodes(index, nodes, null);
        _coolMapView.updateNodeDisplayParams();
        _sortTracker.clearSortedColumn();
        //No update yet
        getCoolMapView().updateCanvasEnforceAll();
        notifyRowsChanged();

        if (select) {
            Rectangle sel = new Rectangle(0, index, getViewNumColumns(), nodes.size());
            getCoolMapView().setSelection(sel);
            HashSet<Rectangle> selections = new HashSet<>();
            selections.add(sel);
            getCoolMapView().highlightNodeRegions(selections);
        }
    }

    /**
     * This method is only used to remove nodes, but the treenodes are kept
     *
     * @param nodes
     */
    public void replaceRowNodes(List<VNode> nodes, List<VNode> treeNodes) {

        //This is only used here
        getCoolMapView().clearSelection();
        _vMatrix.removeActiveRowNodes(); //treenodes are also removed
        nodes.removeAll(Collections.singletonList(null));
        _vMatrix.insertActiveRowNodes(0, nodes, treeNodes);

        _coolMapView.updateNodeDisplayParams();
        _sortTracker.clearSortedColumn();
        
        
        
        //No update yet
        getCoolMapView().updateCanvasEnforceAll();
        notifyRowsChanged();

        getCoolMapView().centerToPercentage(0.5f, 0.5f);

    }

    public void replaceColumnNodes(List<VNode> nodes, List<VNode> treeNodes) {
//        StateSnapshot snapshot = new StateSnapshot(this, COntology.COLUMN, StateSnapshot.COLUMNREPLACE);
//        getStateStorage().addState(snapshot);
//        notifyStateStorageUpdated();

        getCoolMapView().clearSelection();
        _vMatrix.removeActiveColNodes();
        nodes.removeAll(Collections.singletonList(null));
        _vMatrix.insertActiveColNodes(0, nodes, treeNodes);
        _coolMapView.updateNodeDisplayParams();
        _sortTracker.clearSortedRow();
        //No update yet
        getCoolMapView().updateCanvasEnforceAll();
        notifyColumnsChanged();

        getCoolMapView().centerToPercentage(0.5f, 0.5f);
    }

    //insertRowNodes without updating view
    public void insertColumnNodes(int index, List<VNode> nodes, boolean select) {

//        StateSnapshot snapshot = new StateSnapshot(this, COntology.COLUMN, StateSnapshot.COLUMNINSERT);
//        getStateStorage().addState(snapshot);
//        notifyStateStorageUpdated();
        _coolMapView.clearSelection();

        nodes.removeAll(Collections.singletonList(null));
        _vMatrix.insertActiveColNodes(index, nodes, null);
        _coolMapView.updateNodeDisplayParams();
        _sortTracker.clearSortedRow();
        //No update yet
        getCoolMapView().updateCanvasEnforceAll();
        notifyColumnsChanged();
        //
        if (select) {
            Rectangle sel = new Rectangle(index, 0, nodes.size(), getViewNumRows());
            _coolMapView.setSelection(sel);
            HashSet<Rectangle> nodeRegion = new HashSet<Rectangle>();
            nodeRegion.add(sel);
            _coolMapView.highlightNodeRegions(nodeRegion);
        }

    }

    public void insertColumnNodes(List<VNode> nodes) {
        insertColumnNodes(0, nodes, false);
    }

    public void insertRowNodes(List<VNode> nodes) {
        insertRowNodes(0, nodes, false);
    }

    /**
     * now there are multiple base matrices. Multiple!
     *
     * @param row
     * @param col
     * @param baseClass
     * @return
     */
    private List<Object[][]> _getSubBaseMatrix(int row, int col) {

        if (_cMatrices == null || _cMatrices.isEmpty()) {
            return null;
        }

        VNode rowNode = getViewNodeRow(row);
        VNode colNode = getViewNodeColumn(col);

        if (rowNode == null || colNode == null) {
            return null;
        }

        //use the first cmatrix as a reference
        Integer[] rowBaseIndices = rowNode.getBaseIndicesFromCOntology(_cMatrices.get(0), COntology.ROW);
        Integer[] colBaseIndices = colNode.getBaseIndicesFromCOntology(_cMatrices.get(0), COntology.COLUMN);

        if (rowBaseIndices == null || colBaseIndices == null) {
            return null;
        }

        //Object[][][] baseMatrix = (BASE[][][]) (Array.newInstance(baseClass, _cMatrices.size(), rowBaseIndices.length, colBaseIndices.length));
        ArrayList<Object[][]> baseMatrixList = new ArrayList<Object[][]>(_cMatrices.size());
        Integer rowIndex;
        Integer colIndex;
        for (int mi = 0; mi < _cMatrices.size(); mi++) {
            CMatrix<BASE> mx = _cMatrices.get(mi);
            Object[][] baseMatrix = new Object[rowBaseIndices.length][colBaseIndices.length];
            for (int i = 0; i < rowBaseIndices.length; i++) {
                for (int j = 0; j < colBaseIndices.length; j++) {
                    rowIndex = rowBaseIndices[i];
                    colIndex = colBaseIndices[j];
                    if (rowIndex != null && colIndex != null) {
                        //assign a value if not null
                        baseMatrix[i][j] = mx.getValue(rowBaseIndices[i], colBaseIndices[j]);
                    }
                }
            }
            baseMatrixList.add(baseMatrix);
        }
        //first dimension: matrix index. second dimension: row, third dimension: colujmn
        return baseMatrixList;
    }

    public List<CMatrix<BASE>> getBaseCMatrices() {
        return new ArrayList<CMatrix<BASE>>(_cMatrices);
    }

    public void printViewMatrix(int rowCap, int colCap) {
        if (rowCap > getViewNumRows()) {
            rowCap = getViewNumRows();
        }
        if (colCap > getViewNumColumns()) {
            colCap = getViewNumColumns();
        }

        System.out.println("---------------------");
        //System.out.println("CMatrixController name: " + getName());
        System.out.println("NumRows in view: " + getViewNumRows() + " NumCols: " + getViewNumColumns());
//        System.out.println("Row Nodes in Tree:" + _activeRowNodesInTree);
//        System.out.println("Col Nodes in Tree:" + _activeColNodesInTree);

        System.out.println("\n");
        System.out.format("%10s", "Row\\Col");
        for (int j = 0; j < colCap; j++) {
            System.out.format("%10s", _vMatrix.getActiveColNode(j));
        }
        System.out.println();

        for (int i = 0; i < rowCap; i++) {
            System.out.format("%10s", _vMatrix.getActiveRowNode(i));
            for (int j = 0; j < colCap; j++) {
                System.out.format("%10s", getViewValue(i, j));
            }
            System.out.println();
        }
        System.out.println("---------------------");
        System.out.println("View Tree info:");
        //_printViewTreeInfo(rowCap, colCap);
    }

    /**
     * set the base matrix associated with this view
     *
     * @param matrix
     */
//    public void setBaseCMatrix(CMatrix<BASE>... matrix) {
//        if (matrix == null) {
//            return;
//        } else {
//            _cMatrices.clear();
//            for(CMatrix mat : matrix){
//                _cMatrices.add(mat);
//            }
//            _cache.clear();
//            notifyBaseMatrixChanged();
//        }
//    }
    public void addBaseCMatrix(CMatrix<BASE>... matrix) {
        if (matrix == null || matrix.length == 0) {
            return;
        } else {
//            _cMatrices.clear();
            //need to check
            //need to make sure everyone is compatible
            if (_cMatrices.isEmpty()) {
                if (matrix.length > 1) {
                    CMatrix<BASE> m0 = matrix[0];
                    for (CMatrix m1 : matrix) {
                        if (_cMatrices.contains(m1)) {
                            continue;
                        } else if (m1 == null || !m1.canBeGroupedTogether(m0)) {
                            System.out.println(m1 + "Can't add, incompatible.");
                            continue;
                        } else {
                            _cMatrices.add(m1);
                        }
                    }
                } else {
                    //only 1
                    if (matrix[0] != null) {
                        _cMatrices.add(matrix[0]);
                    }
                }
            } else {
                CMatrix<BASE> m0 = _cMatrices.get(0);
                for (CMatrix m1 : matrix) {
                    if (_cMatrices.contains(m1)) {
                        continue;
                    } else if (m1 == null || !m1.canBeGroupedTogether(m0)) {
                        System.out.println(m1 + "Can't add, incompatible.");
                        continue;
                    } else {

                        _cMatrices.add(m1);
                    }
                }
            }
            _cache.clear();
            notifyBaseMatrixChanged();
        }
    }

    public void clearBaseCMatrices() {
        _cMatrices.clear();
        _cache.clear();
        notifyBaseMatrixChanged();
    }

//    /**
//     * set the base matrices associated with this view
//     *
//     * does not work
//     *
//     * @param matrices
//     */
//    public void setBaseCMatrices(ArrayList<CMatrix<BASE>> matrices) {
//        /**
//         * Only when all matrices have the same row/column definition
//         */
//        if (matrices == null || matrices.isEmpty()) {
//            return;
//        }
//
//        System.out.println("Attempting to set matrix:" + matrices);
//
//        _cMatrices.clear();
//        matrices.removeAll(Collections.singletonList(null));
//        if (matrices.isEmpty()) {
//            return;
//        }
//
//        System.out.println("Attempting to set matrix:" + matrices);
//
//        CMatrix m0 = matrices.get(0);
//        boolean pass = true;
//        for (CMatrix<BASE> m : matrices) {
//            if (!m0.canBeGroupedTogether(m)) {
//                pass = false;
//                break;
//            }
//        }
//
//        System.out.println("Pass?" + pass);
//
//        if (pass) {
//            _cMatrices.addAll(matrices);
//        }
//        _cache.clear();
//        notifyBaseMatrixChanged();
//    }
    public void removeCMatrices(List<CMatrix<BASE>> matrices) {
        if (matrices == null || matrices.isEmpty()) {
            return;
        }
        //Must ensure one CoolMapObject has at least one basematrix.
        //if not, then many troubles may occur.
        _cache.clear();
        notifyBaseMatrixChanged();

    }

//    public void addBaseMatrix(CMatrix<BASE> matrix) {
//        if (matrix == null) {
//            return;
//        }
//        if (_cMatrices.isEmpty()) {
//            _cMatrices.add(matrix);
//        } else {
//            CMatrix<BASE> m0 = _cMatrices.get(0);
//            if (m0.canBeGroupedTogether(matrix)) {
//                _cMatrices.add(matrix);
//            }
//        }
//    }
    public void removeBaseCMatrix(CMatrix<BASE> matrix) {
        if (matrix == null) {
            return;
        }

        _cMatrices.remove(matrix);
        _cache.clear();
        notifyBaseMatrixChanged();
    }

    /**
     *
     * @param aggregator
     */
    public void setAggregator(CAggregator<BASE, VIEW> aggregator) {
        //The base aggregator
        //change aggregator -> clean cache
        _cAggregator = aggregator;
        if (_cAggregator != null) {
            _cAggregator.setCoolMapObject(this);
        }
        _cache.clear();
        notifyAggregatorUpdated();
    }

    public void notifyViewRendererUpdated() {
        if (getCoolMapView() != null) {
            //update
            getCoolMapView().updateCanvasEnforceAll();
        }
        for (CObjectListener lis : _coolMapDataListeners) {
            lis.viewRendererChanged(this);
        }
    }

    public void notifyFilterUpdated() {
        if (getCoolMapView() != null) {
            //update
            getCoolMapView().updateCanvasEnforceOverlay();
        }
        for (CObjectListener lis : _coolMapDataListeners) {
            //knows which one's viewfitler was changed.
            lis.viewFilterChanged(this);
        }
    }

    public void notifyAggregatorUpdated() {
        _cache.clear();
        if (getCoolMapView() != null) {
            //update
            getCoolMapView().updateCanvasEnforceAll();
        }
        for (CObjectListener lis : _coolMapDataListeners) {
            lis.aggregatorUpdated(this);
        }
    }

//    public void notifyStateStorageUpdated() {
//        for (CObjectListener lis : _coolMapDataListeners) {
//            lis.stateStorageUpdated(this);
//        }
//    }
    public void notifyRowsChanged() {
        //should update 
//        getCoolMapView().updateActiveCell();
        for (CObjectListener lis : _coolMapDataListeners) {
            lis.rowsChanged(this);
        }
    }

    public void notifyColumnsChanged() {
        //should update 
//        getCoolMapView().updateActiveCell();
        for (CObjectListener lis : _coolMapDataListeners) {
            lis.columnsChanged(this);
        }
    }

    public void notifyBaseMatrixChanged() {
        _cache.clear();
        getCoolMapView().updateCanvasEnforceAll();
        for (CObjectListener lis : _coolMapDataListeners) {
            lis.baseMatrixChanged(this);
        }
    }

    public void notifyStateRestored(CoolMapState stateToRestore) {
        for (CObjectStateStoreListener lis : _cObjectStateRestoreListeners) {
            lis.stateToBeRestored(this, stateToRestore);
        }
    }

    public void notifyStateToBeSaved(CoolMapState stateToSave) {
        for (CObjectStateStoreListener lis : _cObjectStateRestoreListeners) {
            lis.stateToBeSaved(this, stateToSave);
        }
    }

    public CAggregator<BASE, VIEW> getAggregator() {
        return _cAggregator;
    }

    /**
     * multi shift the column nodes
     */
    public void multiShiftColumns(ArrayList<Range<Integer>> selectedColumns, int target) {
        if (selectedColumns == null || selectedColumns.isEmpty()) {
            return;
        }

        try {

//            StateSnapshot snapshot = new StateSnapshot(this, COntology.COLUMN, StateSnapshot.COLUMNSHIFT);
//            getStateStorage().addState(snapshot);
//            notifyStateStorageUpdated();
            int[][] ranges = new int[selectedColumns.size()][2];
            int counter = 0;
            for (Range<Integer> range : selectedColumns) {
                ranges[counter][0] = range.lowerEndpoint();
                ranges[counter][1] = range.upperEndpoint();
                counter++;
            }

            VNode firstNode = getCoolMapView().getCoolMapObject().getViewNodeColumn(selectedColumns.get(0).lowerEndpoint());

            _vMatrix.multiShiftColNodes(ranges, target);

            //must update the display parameters.
            getCoolMapView().updateNodeDisplayParams();

            //and also set selection to be 
            CoolMapView view = getCoolMapView();
            selectedColumns = view.getSelectedColumns();
            ArrayList<Range<Integer>> selectedRows = view.getSelectedRows();

            int width = 0;
            for (Range<Integer> selection : selectedColumns) {
                width += selection.upperEndpoint() - selection.lowerEndpoint();
            }
            selectedColumns.clear();

            int newIndex = firstNode.getViewIndex().intValue();
            selectedColumns.add(Range.closedOpen(newIndex, newIndex + width));

            ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
            for (Range<Integer> colRange : selectedColumns) {
                for (Range<Integer> rowRange : selectedRows) {
                    newSelections.add(new Rectangle(colRange.lowerEndpoint(), rowRange.lowerEndpoint(), colRange.upperEndpoint() - colRange.lowerEndpoint(), rowRange.upperEndpoint() - rowRange.lowerEndpoint()));
                }
            }
            view.setSelections(newSelections);

            //make this call last. It must not be interrupted.
            if (getCoolMapView() != null) {
                getCoolMapView().updateCanvasEnforceAll();
            }

            _sortTracker.clearSortedRow();

            //also 
            _updateColumnTreeNodeOrder();

            //need a listener to state change
            notifyColumnsChanged();

            //save it in the end
        } catch (Exception e) {
            System.err.println("Multi-shift Error");
            if (getCoolMapView() != null) {
                getCoolMapView().clearSelection();
                getCoolMapView().updateCanvasEnforceAll();
            }
            //e.printStackTrace();
            _sortTracker.clearSortedRow();
        }

    }

    private void _updateRowTreeNodeOrder() {
        Set<VNode> rowTreeNodes = _vMatrix.getDepthOneTreeNodesRow();
        if (!rowTreeNodes.isEmpty()) {
            for (VNode node : rowTreeNodes) {
                node.sortChildNodes();
            }
        }
    }

    private void _updateColumnTreeNodeOrder() {
        Set<VNode> colTreeNodes = _vMatrix.getDepthOneTreeNodesCol();
        if (!colTreeNodes.isEmpty()) {
            for (VNode node : colTreeNodes) {
                node.sortChildNodes();
            }
        }
    }

    public List<VNode> getViewNodesColumn() {
        return _vMatrix.getActiveColumnNodes();
    }

    public List<VNode> getViewNodesRow() {
        return _vMatrix.getActiveRowNodes();
    }

/////////////////////////////////////////////////////////////////////////////////////////////////
    public void multiShiftRows(ArrayList<Range<Integer>> selectedRows, int target) {
        if (selectedRows == null || selectedRows.isEmpty()) {
            return;
        }

        try {
//            StateSnapshot snapshot = new StateSnapshot(this, COntology.ROW, StateSnapshot.ROWSHIFT);
//            getStateStorage().addState(snapshot);
//            notifyStateStorageUpdated();

            int[][] ranges = new int[selectedRows.size()][2];
            int counter = 0;
            for (Range<Integer> range : selectedRows) {
                ranges[counter][0] = range.lowerEndpoint();
                ranges[counter][1] = range.upperEndpoint();
                counter++;
            }

            VNode firstNode = getCoolMapView().getCoolMapObject().getViewNodeRow(selectedRows.get(0).lowerEndpoint());

            _vMatrix.multiShiftRowNodes(ranges, target);

            //must update the display parameters.
            getCoolMapView().updateNodeDisplayParams();

            //and also set selection to be 
            CoolMapView view = getCoolMapView();
            selectedRows = view.getSelectedRows();
            ArrayList<Range<Integer>> selectedColumns = view.getSelectedColumns();

            int height = 0;
            for (Range<Integer> selection : selectedRows) {
                height += selection.upperEndpoint() - selection.lowerEndpoint();
            }
            selectedRows.clear();

            int newIndex = firstNode.getViewIndex().intValue();
            selectedRows.add(Range.closedOpen(newIndex, newIndex + height));

//            System.out.println("First node index:" + firstNode + " " + newIndex);
            ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
            for (Range<Integer> colRange : selectedColumns) {
                for (Range<Integer> rowRange : selectedRows) {
                    newSelections.add(new Rectangle(colRange.lowerEndpoint(), rowRange.lowerEndpoint(), colRange.upperEndpoint() - colRange.lowerEndpoint(), rowRange.upperEndpoint() - rowRange.lowerEndpoint()));
                }
            }
            view.setSelections(newSelections);

            //make this call last. It must not be interrupted.
            if (getCoolMapView() != null) {
                getCoolMapView().updateCanvasEnforceAll();
            }

            _sortTracker.clearSortedColumn();
            _updateRowTreeNodeOrder();
            notifyRowsChanged();

        } catch (Exception e) {
            System.err.println("Multi-shift Error");
            if (getCoolMapView() != null) {
                getCoolMapView().clearSelection();
                getCoolMapView().updateCanvasEnforceAll();
            }
            //e.printStackTrace();
            _sortTracker.clearSortedColumn();
        }

    }

    /**
     *
     * @return
     */
    public void sortColumn(int column, boolean descending) {
        if (column < 0 || column >= getViewNumColumns()) {
            return;
        }

        VNode node = getViewNodeColumn(column);
        if (node == null) {
            return;
        }

        VNodeColumnSorter sorter = new VNodeColumnSorter(this, column, descending);

        List<VNode> rowNodes = _vMatrix.getActiveRowNodes();
        Collections.sort(rowNodes, sorter);

        //set active rownodes, keep the tree as original
        _vMatrix.setActiveRowNodes(rowNodes, false);

        //this must be done
        getCoolMapView().updateNodeDisplayParams();
        getCoolMapView().updateCanvasEnforceAll();

        _sortTracker.lastSortedColumn = node;
        _sortTracker.lastSortedColumnDescending = descending;

        //System.out.println(node + " " + descending);
        getCoolMapView().clearSelection();
        notifyRowsChanged();
    }
    private SortTracker _sortTracker = new SortTracker();

    public SortTracker getSortTracker() {
        return _sortTracker;
    }

    public void sortRow(int row, boolean descending) {
        if (row < 0 || row >= getViewNumRows()) {
            return;
        }

        VNode node = getViewNodeRow(row);
        if (node == null) {
            return;
        }

        VNodeRowSorter sorter = new VNodeRowSorter(this, row, descending);

        List<VNode> colNodes = _vMatrix.getActiveColumnNodes();
        Collections.sort(colNodes, sorter);

        //set active rownodes, keep the tree as original
        _vMatrix.setActiveColNodes(colNodes, false);

        //this must be done
        getCoolMapView().updateNodeDisplayParams();
        getCoolMapView().updateCanvasEnforceAll();

        _sortTracker.lastSortedRow = node;
        _sortTracker.lastSortedRowDescending = descending;

        getCoolMapView().clearSelection();
        notifyColumnsChanged();
    }

    public String getID() {
        return _ID;
    }

    public synchronized boolean expandColumnNode(VNode node) {
        if (node != null && getCoolMapView() != null && !node.isExpanded()) {

            _vMatrix.expandColNodeToChildNodes(node);
            getCoolMapView().updateNodeDisplayParams();
            //Also set a new selection
            List<VNode> childNodes = node.getChildNodes();
            //set column selection
//            getCoolMapView().clearSelection();
            if (childNodes == null || childNodes.isEmpty()) {
                getCoolMapView().clearSelection();
            } else {
                VNode firstNode = childNodes.get(0);
                VNode lastNode = childNodes.get(childNodes.size() - 1);
                Float i1 = firstNode.getViewIndex();
                Float i2 = lastNode.getViewIndex();
                if (i1 == null || i2 == null) {
                    getCoolMapView().clearSelection();
                } else {
                    Range<Integer> columnSelection = Range.closedOpen(i1.intValue(), i2.intValue() + 1);
                    getCoolMapView().setSelectionsColumn(Collections.singletonList(columnSelection));
                }
            }

            //expansion
            _sortTracker.clearSortedRow();
            getCoolMapView().updateCanvasEnforceAll();

            notifyColumnsChanged();

            return true;
        } else {
            return false;
        }
    }

    public synchronized List<VNode> expandColumnNodes(List<VNode> inputNodes, boolean select) {
        try {
            if (inputNodes == null || inputNodes.isEmpty() || !isViewMatrixValid()) {
                return null;
            }

            ArrayList<VNode> nodesToBeExpanded = new ArrayList<VNode>();
            for (VNode node : inputNodes) {
                if (node != null && node.isGroupNode() && !node.isExpanded()) {
                    nodesToBeExpanded.add(node);
                }
            }

//            System.out.println("Trying to expand" + nodesToBeExpanded);
            _vMatrix.expandColNodeToChildNodes(nodesToBeExpanded);

            getCoolMapView().clearSelection();
            getCoolMapView().updateNodeDisplayParams();
            _sortTracker.clearSortedRow();
            getCoolMapView().updateCanvasEnforceAll();

            if (select) {
                //reselect the child inputNodes of these inputNodes
//                ArrayList<VNode> retainedNodes = new ArrayList<VNode>();
//                for (VNode node : inputNodes) {
//                    if (node != null && node.isGroupNode() && node.isExpanded()) {
//                        retainedNodes.add(node);
//                    }
//                }

                List<VNode> toBeSelectedNodes = getViewNodesColumnFromTreeNodes(nodesToBeExpanded);
                if (toBeSelectedNodes != null) {
                    ArrayList<Integer> retainedIndices = new ArrayList<Integer>();
                    for (VNode node : toBeSelectedNodes) {
                        if (node != null && node.getViewIndex() != null) {
                            try {
                                retainedIndices.add(node.getViewIndex().intValue());
                            } catch (Exception e) {

                            }
                        }
                    }
                    HashSet<Range<Integer>> columnSelections = Tools.createRangesFromIndices(retainedIndices);

                    if (columnSelections != null) {
                        getCoolMapView().setSelectionsColumn(columnSelections);
                        getCoolMapView().centerToSelections();
                    }
                }
            }

            notifyColumnsChanged();
            return nodesToBeExpanded;

        } catch (Exception e) {
            System.err.println("Minor issue: expand column error");
            return null;
        }
    }

    public synchronized List<VNode> expandRowNodes(List<VNode> nodes, boolean select) {
        try {

            if (nodes == null || nodes.isEmpty() || !isViewMatrixValid()) {
                return null;
            }

            //first need to remove nodes that are already expanded
            ArrayList<VNode> nodesToBeExpanded = new ArrayList<VNode>();
            for (VNode node : nodes) {
                if (node != null && node.isGroupNode() && !node.isExpanded()) {
                    nodesToBeExpanded.add(node);
                }
            }
            //add a filtering step

            _vMatrix.expandRowNodeToChildNodes(nodesToBeExpanded);
            
            getCoolMapView().clearSelection();
            getCoolMapView().updateNodeDisplayParams();
            _sortTracker.clearSortedColumn();
            getCoolMapView().updateCanvasEnforceAll();

            if (select) {

//                ArrayList<VNode> retainedNodes = new ArrayList<VNode>();
//                for (VNode node : nodes) {
//                    if (node != null && node.isGroupNode() && node.isExpanded()) {
//                        retainedNodes.add(node);
//                    }
//                }
                List<VNode> toBeSelectedNodes = getViewNodesRowFromTreeNodes(nodesToBeExpanded);
                ArrayList<Integer> retainedIndices = new ArrayList<Integer>();

                if (toBeSelectedNodes != null) {
                    for (VNode node : toBeSelectedNodes) {
                        if (node != null && node.getViewIndex() != null) {
                            try {
                                retainedIndices.add(node.getViewIndex().intValue());
                            } catch (Exception e) {

                            }
                        }
                    }
                    HashSet<Range<Integer>> rowSelections = Tools.createRangesFromIndices(retainedIndices);

                    if (rowSelections != null) {
                        getCoolMapView().setSelectionsRow(rowSelections);
                        getCoolMapView().centerToSelections();
                    }
                }
            }

            notifyRowsChanged();
            return nodesToBeExpanded;

        } catch (Exception e) {
            System.err.println("Minor issue: exapnd row error");
            return null;
        }
    }

    public synchronized boolean expandRowNode(VNode node) {

        if (node != null && getCoolMapView() != null && !node.isExpanded()) {

            _vMatrix.expandRowNodeToChildNodes(node);

            getCoolMapView().updateNodeDisplayParams();
            //Also set a new selection
            List<VNode> childNodes = node.getChildNodes();
            //set column selection
//            getCoolMapView().clearSelection();
            if (childNodes == null || childNodes.isEmpty()) {
                getCoolMapView().clearSelection();
            } else {
                VNode firstNode = childNodes.get(0);
                VNode lastNode = childNodes.get(childNodes.size() - 1);
                Float i1 = firstNode.getViewIndex();
                Float i2 = lastNode.getViewIndex();
                if (i1 == null || i2 == null) {
                    getCoolMapView().clearSelection();
                } else {
                    //maybe retain the previous selection

                    Range<Integer> rowSelection = Range.closedOpen(i1.intValue(), i2.intValue() + 1);
                    getCoolMapView().setSelectionsRow(Collections.singletonList(rowSelection));
                }
            }

            _sortTracker.clearSortedColumn();
            getCoolMapView().updateCanvasEnforceAll();
            notifyRowsChanged();
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean collapseColumnNode(VNode node) {
        if (node != null && getCoolMapView() != null && node.isExpanded()) {

            _vMatrix.collapseTreeColNode(node);
            getCoolMapView().updateNodeDisplayParams();
            //should all have something
            Float index = node.getViewIndex();
            if (index == null) {
                getCoolMapView().clearSelection();
            } else {
                Range<Integer> selectedColumn = Range.closedOpen(index.intValue(), index.intValue() + 1);
                getCoolMapView().setSelectionsColumn(Collections.singletonList(selectedColumn));
                getCoolMapView().centerToSelections();
            }

            //
            _sortTracker.clearSortedRow();
            if (_sortTracker.lastSortedColumn != null && !_vMatrix.getActiveColumnNodes().contains(_sortTracker.lastSortedColumn)) {
                _sortTracker.clearSortedColumn();//It's gone
            }
            getCoolMapView().updateCanvasEnforceAll();
            notifyColumnsChanged();
            return true;
        } else {
            return false;
        }
    }

    public synchronized List<VNode> collapseColumnNodes(Collection<VNode> inputNodes, boolean select) {
        if (inputNodes == null || inputNodes.isEmpty() || getCoolMapView() == null) {
            return null;
        }

        List<VNode> nodesToCollapse = new ArrayList<VNode>();
        for (VNode node : inputNodes) {
            if (node != null && node.isGroupNode() && node.isExpanded()) {
                nodesToCollapse.add(node);
            }
        }

        _vMatrix.collapseTreeColNodes(nodesToCollapse);

        getCoolMapView().updateNodeDisplayParams();
        _sortTracker.clearSortedRow();
        if (_sortTracker.lastSortedColumn != null && !_vMatrix.getActiveColumnNodes().contains(_sortTracker.lastSortedColumn)) {
            _sortTracker.clearSortedColumn();//It's gone
        }
        getCoolMapView().clearSelection();
        getCoolMapView().updateCanvasEnforceAll();
        notifyColumnsChanged();

        if (select) {

            ArrayList<Integer> retainedIndices = new ArrayList<Integer>();

            for (VNode node : nodesToCollapse) {
                if (node != null && node.getViewIndex() != null && node.isGroupNode()) {
                    try {
                        retainedIndices.add(node.getViewIndex().intValue());
                    } catch (Exception e) {

                    }
                }
            }
            
            HashSet<Range<Integer>> columnSelections = Tools.createRangesFromIndices(retainedIndices);

            if (columnSelections != null) {
                getCoolMapView().setSelectionsColumn(columnSelections);
                getCoolMapView().centerToSelections();
            }
        }
        //
//        Collections.sort(retainedIndices, new VNodeIndexComparator());
        //then set selection 
        return nodesToCollapse;
    }

    /**
     * returns collapsed nodes
     *
     * @param inputNodes
     * @param select
     * @return
     */
    public synchronized List<VNode> collapseRowNodes(Collection<VNode> inputNodes, boolean select) {
        if (inputNodes == null || inputNodes.isEmpty() || getCoolMapView() == null) {
            return null;
        }

        //again first check that inputNodes are actually tree inputNodes
        //
        ArrayList<VNode> nodesToCollapse = new ArrayList<VNode>();

        //only keep tree nodes
        for (VNode node : inputNodes) {
            if (node != null && node.isGroupNode() && node.isExpanded()) {
                nodesToCollapse.add(node);
            }
        }

        _vMatrix.collapseTreeRowNodes(nodesToCollapse);

        getCoolMapView().updateNodeDisplayParams();
        _sortTracker.clearSortedColumn();
        if (_sortTracker.lastSortedRow != null && !_vMatrix.getActiveRowNodes().contains(_sortTracker.lastSortedRow)) {
            _sortTracker.clearSortedRow();//It's gone
        }

        getCoolMapView().clearSelection();
        getCoolMapView().updateCanvasEnforceAll();
        notifyRowsChanged();

        if (select) {
            ArrayList<Integer> retainedIndices = new ArrayList<Integer>();

            for (VNode node : inputNodes) {
                if (node != null && node.getViewIndex() != null && node.isGroupNode()) {
                    try {
                        retainedIndices.add(node.getViewIndex().intValue());
                    } catch (Exception e) {

                    }
                }
            }
            HashSet<Range<Integer>> rowSelections = Tools.createRangesFromIndices(retainedIndices);

            if (rowSelections != null) {
                getCoolMapView().setSelectionsRow(rowSelections);
                getCoolMapView().centerToSelections();
            }
        }

        return nodesToCollapse;
    }

    public boolean expandColumnNodesOneLayer() {
        return expandColumnNodes(getViewNodesColumn(), false)==null?false:true;
    }

    public boolean expandRowNodesOneLayer() {
        return expandRowNodes(getViewNodesRow(), false)==null?false:true;
    }

    public boolean collapseColumnNodesOneLayer() {
        if (getCoolMapView() == null) {
            return false;
        }

//        StateSnapshot snapshot = new StateSnapshot(this, COntology.COLUMN, StateSnapshot.COLUMNCOLLAPSE);
//        notifyStateStorageUpdated();
//        find the first level parents
        HashSet<VNode> levelOneParents = new HashSet<VNode>();
        for (VNode node : getViewNodesColumn()) {
            if (node.getParentNode() != null) {
                levelOneParents.add(node.getParentNode());
            }
        }

//        System.out.println("Level one parents:" + levelOneParents);
//        must check to ensure that all level one parents children are also level one
        HashSet<VNode> onlyLevelOneParents = new HashSet<VNode>();
        for (VNode node : levelOneParents) {
            if (!node.isGroupNode()) {
                continue;
            }

            List<VNode> childNodes = node.getChildNodes();

            boolean pass = true;
            for (VNode childNode : childNodes) {
                if (childNode.isExpanded()) {
                    pass = false;
                    break;
                }
            }
            if (pass) {
                onlyLevelOneParents.add(node);
            }
        }

        if (!onlyLevelOneParents.isEmpty()) {
            return collapseColumnNodes(onlyLevelOneParents, false) == null?false:true;
        }
        return false;
    }

    public boolean collapseRowNodesOneLayer() {
        if (getCoolMapView() == null) {
            return false;
        }

        HashSet<VNode> levelOneParents = new HashSet<VNode>();
        for (VNode node : getViewNodesRow()) {
            if (node.getParentNode() != null) {
                levelOneParents.add(node.getParentNode());
            }
        }

        HashSet<VNode> onlyLevelOneParents = new HashSet<VNode>();
        for (VNode node : levelOneParents) {
            if (!node.isGroupNode()) {
                continue;
            }

            List<VNode> childNodes = node.getChildNodes();

            boolean pass = true;
            for (VNode childNode : childNodes) {
                if (childNode.isExpanded()) {
                    pass = false;
                    break;
                }
            }
            if (pass) {
                onlyLevelOneParents.add(node);
            }
        }

        if (!onlyLevelOneParents.isEmpty()) {
            return collapseRowNodes(onlyLevelOneParents, false) == null ? false : true;
        }

        //There are better ways to do this? 
        //It's tricky because intermediate nodes need also to be removed
        return false;
    }

    public synchronized boolean collapseRowNode(VNode node) {
        if (node != null && getCoolMapView() != null && node.isExpanded()) {

            _vMatrix.collapseTreeRowNode(node);

            getCoolMapView().updateNodeDisplayParams();
            //should all have something
            Float index = node.getViewIndex();
            if (index == null) {
                getCoolMapView().clearSelection();
            } else {
                Range<Integer> selectedRow = Range.closedOpen(index.intValue(), index.intValue() + 1);
                getCoolMapView().setSelectionsRow(Collections.singletonList(selectedRow));
                getCoolMapView().centerToSelections();
            }

            //
            _sortTracker.clearSortedColumn();
            if (_sortTracker.lastSortedRow != null && !_vMatrix.getActiveRowNodes().contains(_sortTracker.lastSortedRow)) {
                _sortTracker.clearSortedRow();//It's gone
            }

            getCoolMapView().updateCanvasEnforceAll();
            notifyRowsChanged();
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean expandRowNodeToBottom(VNode node) {
        if (node != null && getCoolMapView() != null && !node.isExpanded()) {

            List<VNode> childNodes = _vMatrix.expandRowNodeToChildNodesAll(node);

            getCoolMapView().updateNodeDisplayParams();

            getCoolMapView().clearSelection();
            if (childNodes == null || childNodes.isEmpty()) {

            } else {
                VNode firstNode = childNodes.get(0);
                VNode lastNode = childNodes.get(childNodes.size() - 1);
                Float i1 = firstNode.getViewIndex();
                Float i2 = lastNode.getViewIndex();

                if (i1 == null || i2 == null) {
                    getCoolMapView().clearSelection();
                } else {
                }
            }
            _sortTracker.clearSortedColumn();
            getCoolMapView().updateCanvasEnforceAll();

            notifyRowsChanged();

            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean expandColumnNodeToBottom(VNode node) {
        if (node != null && getCoolMapView() != null && !node.isExpanded()) {

            List<VNode> childNodes = _vMatrix.expandColNodeToChildNodesAll(node);

            getCoolMapView().updateNodeDisplayParams();
            getCoolMapView().clearSelection();

            if (childNodes == null || childNodes.isEmpty()) {

            } else {
                VNode firstNode = childNodes.get(0);
                VNode lastNode = childNodes.get(childNodes.size() - 1);
                Float i1 = firstNode.getViewIndex();
                Float i2 = lastNode.getViewIndex();
                if (i1 == null || i2 == null) {
                    getCoolMapView().clearSelection();
                } else {
                }
            }
            _sortTracker.clearSortedRow();

            getCoolMapView().updateCanvasEnforceAll();

            notifyColumnsChanged();
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean toggleColumnNode(VNode node) {
        if (node == null) {
            return false;
        } else {
            if (node.isExpanded()) {
                return collapseColumnNode(node);
            } else {
                return expandColumnNodeToBottom(node);
            }
        }

    }

    public synchronized boolean toggleRowNode(VNode node) {
        if (node == null) {
            return false;
        } else {
            if (node.isExpanded()) {
                return collapseRowNode(node);
            } else {
                return expandRowNodeToBottom(node);
            }
        }

    }

    /**
     * obtains a copy of nodes in row
     *
     * @return
     */
    public List<VNode> getViewTreeNodesRow() {
        return _vMatrix.getTreeNodesRow();
    }

    public void prepareSync() {
        getCoolMapView().setForceDrawHover(true);
        getCoolMapView().redrawCanvas();
    }

    public void prepareDesync() {
        getCoolMapView().setForceDrawHover(false);
//        getCoolMapView().deSyncAll(); //set all sync flag to false
        getCoolMapView().redrawCanvas();
    }

    /**
     * obtains the nodes in tree
     *
     * @return
     */
    public List<VNode> getViewTreeNodesColumn() {
        return _vMatrix.getTreeNodesColumn();
    }

    public VNode getViewNodeRow(int index) {
        return _vMatrix.getActiveRowNode(index);
    }

    public VNode getViewNodeColumn(int index) {
        return _vMatrix.getActiveColNode(index);
    }

//  can build hash on the fly? sometimes not only names -> it will probably be selections.    
//    /**
//     * find nodes by unique ID
//     * @param ID
//     * @return 
//     */
//    public VNode getViewNodeColumn(String ID){
//        return null;
//    }
//    
//    /**
//     * find nodes by unique ID
//     * @param ID
//     * @return 
//     */
//    public VNode getViewNodeRow(String ID){
//        return null;
//    }
    /**
     * returns all child nodes in view to this parent
     *
     * @param parent
     * @return list of nodes, or null if parentnode is not a treenode or not
     * currently in view
     */
    public List<VNode> getViewNodesColumn(VNode parentNode) {
        return _vMatrix.getChildNodesInViewColumn(parentNode);
    }

    /**
     * get leaft nodes from seleted tree nodes in row
     *
     * @param treeNodes
     * @return
     */
    public List<VNode> getViewNodesColumnFromTreeNodes(Collection<VNode> treeNodes) {
        return _vMatrix.getChildNodesInViewColumn(treeNodes);
    }

    /**
     * get all nodes associated with the selected tree nodes
     *
     * @param treeNodes
     * @return
     */
    public List<VNode> getViewNodesColumnFromTreeNodesAll(Collection<VNode> treeNodes) {
        return _vMatrix.getChildNodesInViewColumnAll(treeNodes);
    }

    /**
     * get all nodes associated with the selected tree nodes
     *
     * @param treeNodes
     * @return
     */
    public List<VNode> getViewNodesRowFromTreeNodesAll(Collection<VNode> treeNodes) {
        return _vMatrix.getChildNodesInViewRowAll(treeNodes);
    }

    /**
     * get all leaf nodes from selected tree nodes in row
     *
     * @param treeNodes
     * @return
     */
    public List<VNode> getViewNodesRowFromTreeNodes(Collection<VNode> treeNodes) {
        return _vMatrix.getChildNodesInViewRow(treeNodes);
    }

    public List<VNode> getViewNodesRow(VNode parentNode) {
        return _vMatrix.getChildNodesInViewRow(parentNode);
    }

    public static void main(String args[]) {
    }

    /**
     * reset multipliers. Treenode multipliers unchanged
     */
//    public void resetActiveViewNodeMultipliers() {
//        _vMatrix.resetActiveColNodeDisplayMultipliers();
//        _vMatrix.resetActiveRowNodeDisplayMultipliers();
//    }
    public ViewRenderer<VIEW> getViewRenderer() {
        return _viewRenderer;
    }

//    public AnnotationRenderer<BASE, VIEW> getAnnotationRenderer() {
//        return _annotationRenderer;
//    }
//    public Filter<VIEW> getViewFilter() {
//        return _viewFilter;
//    }
//    public void restoreSnapshot(StateSnapshot snapshot, boolean trackState) {
//
//        if (snapshot == null) {
//            return;
//        } else {
//            _vMatrix.restoreState(snapshot.duplicate());
//            _sortTracker.clearSortedRow();
//            _sortTracker.clearSortedColumn();
//            getCoolMapView().updateNodeDisplayParams();
//            getCoolMapView().clearSelection();
//            if (snapshot.getDirection() == COntology.ROW) {
//                getCoolMapView().setSelectionsRow(snapshot.getSelections());
//            } else {
//                getCoolMapView().setSelectionsColumn(snapshot.getSelections());
//            }
//            getCoolMapView().updateCanvasEnforceAll();
//
//            if (snapshot.getDirection() == COntology.ROW) {
//                notifyRowsChanged();
//            } else if (snapshot.getDirection() == COntology.COLUMN) {
//                notifyColumnsChanged();
//            }
//
//            if (trackState) {
//                _stateStorage.addState(snapshot);
//            }
//        }
//    }
    /**
     * The state can be restored from a previously saved This state also must
     * contain
     *
     * @param state
     */
    public void restoreState(CoolMapState state) {
        //The state can be from a different ID'ed object
        if (state == null) {
            return;
        } else {
            _vMatrix.restoreState(state);
            _sortTracker.clearSortedRow();
            _sortTracker.clearSortedColumn();
            getCoolMapView().updateNodeDisplayParams();

//            if(state.loggedRows()){
//                
//            }
//            if(state.loggedColumns()){
//                
//            }
            //change selection if it was previously recorded?
            if (state.loggedSelections()) {
                getCoolMapView().clearSelection(); //This line may be redundant
                getCoolMapView().setSelections(state.getSelections()); //also 
            }

            //notify downstream listeners
            if (state.loggedRows()) {
                notifyRowsChanged();
            }
            if (state.loggedColumns()) {
                notifyColumnsChanged();
            }

            //also need to parse JSON -> save to other operations
            notifyStateRestored(state);

            getCoolMapView().updateCanvasEnforceAll();

        }

    }

//    public void setSnapshot(StateSnapshot snapshot) {
//
//
//        restoreSnapshot(snapshot);
//    }
    private boolean _isDestroyed = false;

    public void destroy() {
        if (_coolMapView != null) {
            _coolMapView.destroy();
        }
        _vMatrix.destroy();
        _coolMapDataListeners.clear();
        _cObjectStateRestoreListeners.clear();
        _cMatrices.clear();
//        _viewFilter = null;
        _masterFilter.clearFilters();
        _viewRenderer = null;
        _cAggregator = null;
        _snippetConverter = null;
        _sortTracker.clear();
        _isDestroyed = true;

//        
    }

    public boolean isDestroyed() {
        return _isDestroyed;
    }

/////////////////////////////////////////////////////////////////////////////////////////////
    public static CoolMapObject<Double, Double> createSampleCoolMapObject(int row, int col) {
        DoubleCMatrix mx = new DoubleCMatrix("SampleMatrix:" + 1, row, col);
        for (int i = 0; i < mx.getNumRows(); i++) {
            for (int j = 0; j < mx.getNumColumns(); j++) {
                mx.setValue(i, j, Math.random());
            }
        }

        DoubleCMatrix mx1 = new DoubleCMatrix("SampleMatrix:" + 2, row, col);
        for (int i = 0; i < mx.getNumRows(); i++) {
            for (int j = 0; j < mx.getNumColumns(); j++) {
                mx1.setValue(i, j, Math.random() + 0.5);
            }
        }

        DoubleCMatrix mx2 = new DoubleCMatrix("SampleMatrix:" + 3, row, col);
        for (int i = 0; i < mx2.getNumRows(); i++) {
            for (int j = 0; j < mx2.getNumColumns(); j++) {
                mx2.setValue(i, j, Math.random() + 0.2);
            }
        }

        CoolMapMaster.addNewBaseMatrix(mx);
        CoolMapMaster.addNewBaseMatrix(mx1);
        CoolMapMaster.addNewBaseMatrix(mx2);

        CoolMapObject<Double, Double> coolMapObject = new CoolMapObject<Double, Double>();

        DoubleDoubleMax maxAggr = new DoubleDoubleMax();
        coolMapObject.setAggregator(maxAggr);

        //must happen after the aggregator. if aggr is null, no filer can be added. order is very important!
//        coolMapObject.addViewFilter(new DoubleAboveFilter());
//        coolMapObject.addViewFilter(new DoubleBelowFilter());
//        ArrayList<DoubleCMatrix> mxs = new ArrayList<DoubleCMatrix>();
//        mxs.add(mx);
//        mxs.add(mx1);
//        coolMapObject.setBaseCMatrix(mxs);
        coolMapObject.clearBaseCMatrices();
        coolMapObject.addBaseCMatrix(mx);
        coolMapObject.addBaseCMatrix(mx1);
        coolMapObject.addBaseCMatrix(mx2);

        List<String> rowLabels = mx.getRowLabelsAsList();
        List<String> colLabels = mx.getColLabelsAsList();

        ArrayList<VNode> rowNodes = new ArrayList<VNode>();
        ArrayList<VNode> colNodes = new ArrayList<VNode>();

        for (String r : rowLabels) {
            rowNodes.add(new VNode(r));
        }

        for (String c : colLabels) {
            colNodes.add(new VNode(c));
        }

        COntology rowOnto = COntologyUtils.createSampleRowOntology();
        COntology colOnto = COntologyUtils.createSampleColOntology();

        CoolMapMaster.addNewCOntology(rowOnto);
        CoolMapMaster.addNewCOntology(colOnto);

        coolMapObject.insertRowNodes(0, rowNodes, false);

        rowNodes.clear();
        VNode node1 = new VNode("RG00", rowOnto);
        VNode node2 = new VNode("RG0", rowOnto);
        rowNodes.add(node1);
        rowNodes.add(node2);
        VNode node3 = new VNode("XXXX", rowOnto);
        rowNodes.add(node3);

        coolMapObject.insertRowNodes(0, rowNodes, false);

        coolMapObject.insertColumnNodes(0, colNodes, false);

        colNodes.clear();
        node1 = new VNode("CG1", colOnto);
        node2 = new VNode("CG00", colOnto);
        colNodes.add(node1);
        colNodes.add(node2);

        //coolMapObject.insertColNodes(201, colNodes);
        colNodes.clear();
        node2 = new VNode("CG00", colOnto);
        node1 = new VNode("CG1", colOnto);
        colNodes.add(node1);
        colNodes.add(node2);
        coolMapObject.insertColumnNodes(0, colNodes, false);

//        coolMapObject.setViewRenderer(new DoubleToBoxPlot(), true);
        node1.setViewColor(Color.RED);

//        colOnto.setEdgeAttribute("CG1", "C3", new COntologyEdgeAttributeImpl(7.5f));
//        colOnto.setEdgeAttribute("CG00", "CG0", new COntologyEdgeAttributeImpl(2.5f));
        //coolMapObject.setName("Sample Object");
        coolMapObject.setName("M" + System.currentTimeMillis());
        coolMapObject.setSnippetConverter(SnippetMaster.getConverter("D13"));

        coolMapObject.getCoolMapView().addColumnMap(new ColumnLabels(coolMapObject));
        coolMapObject.getCoolMapView().addColumnMap(new ColumnTree(coolMapObject));

        coolMapObject.getCoolMapView().addRowMap(new RowLabels(coolMapObject));
        coolMapObject.getCoolMapView().addRowMap(new RowTree(coolMapObject));

        return coolMapObject;

    }
}
