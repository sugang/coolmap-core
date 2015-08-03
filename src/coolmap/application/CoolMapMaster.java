/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application;

import coolmap.application.io.IOMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.plugin.PluginMaster;
import coolmap.application.state.StateStorageMaster;
import coolmap.application.utils.ActiveCoolMapObjectListenerTunnel;
import coolmap.application.utils.DataMaster;
import coolmap.application.utils.Session;
import coolmap.application.widget.Widget;
import coolmap.application.widget.WidgetMaster;
import coolmap.canvas.datarenderer.renderer.impl.NumberToColor;
import coolmap.canvas.sidemaps.impl.ColumnLabels;
import coolmap.canvas.sidemaps.impl.ColumnTree;
import coolmap.canvas.sidemaps.impl.RowLabels;
import coolmap.canvas.sidemaps.impl.RowTree;
import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.impl.DoubleDoubleMean;
import coolmap.data.cmatrix.impl.DoubleCMatrix;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.snippet.DoubleSnippet1_3;
import coolmap.module.ModuleMaster;
import coolmap.utils.CSplashScreen;
import coolmap.utils.Tools;
import coolmap.utils.graphics.UI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 *
 * @author gangsu
 */
public final class CoolMapMaster {

    private final static ActiveCoolMapObjectListenerTunnel _activeCoolMapObjectListenerTunnel = ActiveCoolMapObjectListenerTunnel.getInstance();
    private final static LinkedHashMap<String, CMatrix> _cMatrices = new LinkedHashMap<>();
    private final static LinkedHashSet<CoolMapObject> _coolMapObjects = new LinkedHashSet<>();
    private final static HashSet<ActiveCoolMapChangedListener> _activeCoolMapChangedListeners = new HashSet<>();
    private final static LinkedHashMap<String, COntology> _contologies = new LinkedHashMap<>();

    private static CoolMapObject _activeCoolMapObject = null;

    //can't be final because I need Config to load before static inialization
    private static CMainFrame _cMainFrame;

    private static Session session;

    public static Session getSession() {
        if (session == null) {
            session = new Session(null, null);
        }
        return session;
    }

    public static CMatrix getCMatrixByID(String identifier) {
        if (identifier == null || identifier.length() == 0) {
            return null;
        }
        return _cMatrices.get(identifier);
    }

    private CoolMapMaster() {    //prevent initializing

    }

    public static void newSession(String name, String path) {

        if (name == null || name.length() == 0) {
            name = "Untitled";
        }

        session = new Session(name, path);
        WidgetMaster.getViewport().setTitle(name, path);

        CoolMapObject object = _activeCoolMapObject;
        _activeCoolMapObject = null;
        _fireActiveCoolMapChanged(object, null);

        List<CoolMapObject> coolMapObjects = new ArrayList<>(_coolMapObjects);
        for (CoolMapObject obj : coolMapObjects) {
            destroyCoolMapObject(obj);
        }

        List<COntology> contologies = new ArrayList<>(_contologies.values());
        for (COntology ontology : contologies) {
            destroyCOntology(ontology);
        }

        List<CMatrix> cmatrices = new ArrayList<>(_cMatrices.values());
        for (CMatrix matrix : cmatrices) {
            destroyCMatrix(matrix);
        }

        //Also clear all widget states
        for (Widget widget : WidgetMaster.getAllWidgets()) {
            widget.restoreState(null);
        }

//        for (Module module : ModuleMaster.getAllModules()) {
//            module.restoreState(null);
//        }
        COntology.clearAttributes();
        StateStorageMaster.clearAllStates();
    }

    public static void updateSession(String name, String path) {
        session = new Session(name, path);
        WidgetMaster.getViewport().setTitle(name, path);
    }

    public static ActiveCoolMapObjectListenerTunnel getActiveCoolMapObjectListenerDelegate() {
        return _activeCoolMapObjectListenerTunnel;
    }

    public static List<CoolMapObject> getCoolMapObjects() {
        return new ArrayList<>(_coolMapObjects);
    }

    public static CoolMapObject getCoolMapObjectByID(String ID) {
        for (CoolMapObject object : _coolMapObjects) {
            if (object.getID().equals(ID)) {
                return object;
            }
        }
        return null;
    }

    /**
     * initialize the necessary elements
     */
    public static void initialize() {
        //
        _cMainFrame = new CMainFrame();

        CSplashScreen.splashUpdate("Loading UI...", 10);
        UI.initialize();
        CSplashScreen.splashUpdate("Loading utilities...", 15);
        Tools.initialize();
        CSplashScreen.splashUpdate("Loading IO...", 25);
        IOMaster.initialize();
        CSplashScreen.splashUpdate("Loading widgets...", 50);
        WidgetMaster.initialize();
        ModuleMaster.initialize();
        CSplashScreen.splashUpdate("Loading state manager...", 60);
        StateStorageMaster.initialize();

//        SnippetMaster.initialize();
//        ServiceMaster.initialize();
        //
//        CreaterMaster.initialize(); //Creater should be defined as a module
        //new session
        CSplashScreen.splashUpdate("Creating new session...", 80);
        CoolMapMaster.newSession("Untitled", null);

        //Plugin loading after new session.
        CSplashScreen.splashUpdate("Loading plugins...", 90);
        PluginMaster.initialize();

        CSplashScreen.splashUpdate("Finalizing...", 95);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                _cMainFrame.setVisible(true);
                //ServiceMaster.getTaskEngine().showModularScreen();
            }
        });
    }

    public static void addActiveCoolMapChangedListener(ActiveCoolMapChangedListener lis) {
        if (lis != null) {
            _activeCoolMapChangedListeners.add(lis);
        }
    }

    private static void _fireActiveCoolMapChanged(CoolMapObject currentObject, CoolMapObject newObject) {
        for (ActiveCoolMapChangedListener lis : _activeCoolMapChangedListeners) {
            lis.activeCoolMapChanged(newObject, _activeCoolMapObject);
        }

    }

    public static CMainFrame getCMainFrame() {
        return _cMainFrame;
    }

    public static CoolMapObject getActiveCoolMapObject() {
        return _activeCoolMapObject;
    }

    public static void setActiveCoolMapObject(CoolMapObject newObject) {

        if (_activeCoolMapObject != newObject) {
            CoolMapObject oldObject = _activeCoolMapObject;
            _activeCoolMapObject = newObject;
            if (newObject != null && newObject.getCoolMapView() != null && newObject.getCoolMapView().getViewCanvas() != null) {
                newObject.getCoolMapView().getViewCanvas().requestFocus();
                newObject.getCoolMapView().setActive(true);
            }
            if (oldObject != null && oldObject.getCoolMapView() != null) {
                oldObject.getCoolMapView().setActive(false);
            }
            _fireActiveCoolMapChanged(oldObject, newObject);

        }

    }

    /**
     * add a new coolmap object into store. This should be a newly imported
     * coolmap object, with empty side panels
     *
     * @param object
     */
    public static void addNewCoolMapObject(CoolMapObject object) {
        if (object == null || _coolMapObjects.contains(object)) {
            return;
        }

//        System.out.println("adding:" + object);
        _coolMapObjects.add(object);
        addNewBaseMatrix(object.getBaseCMatrices());

        object.addCObjectDataListener(_activeCoolMapObjectListenerTunnel);
        object.getCoolMapView().addCViewListener(_activeCoolMapObjectListenerTunnel);

        WidgetMaster.getViewport().addCoolMapView(object);
        DataMaster.fireCoolMapObjectAdded(object);
    }

    public static void addNewCoolMapObject(Collection<CoolMapObject> objects) {
        if (objects == null || objects.isEmpty()) {
            return;
        }

        for (CoolMapObject object : objects) {
            addNewCoolMapObject(object);
        }
    }

    public static void addNewBaseMatrix(Collection<CMatrix> matrices) {
        if (matrices == null || matrices.isEmpty()) {
            return;
        }
        matrices.removeAll(Collections.singletonList(null));
        for (CMatrix matrix : matrices) {
            if (!_cMatrices.values().contains(matrix)) {
                _cMatrices.put(matrix.getID(), matrix);

                DataMaster.fireCMatrixAdded(matrix);
            }
        }
    }

    public static void addNewBaseMatrix(CMatrix matrix) {
        if (matrix == null) {
            return;
        }
        addNewBaseMatrix(Collections.singletonList(matrix));
    }

    public static void addNewCOntology(Collection<COntology> ontologies) {
        if (ontologies == null || ontologies.isEmpty()) {
            return;
        }
        ontologies.removeAll(Collections.singletonList(null));
        for (COntology ontology : ontologies) {
            if (!_contologies.values().contains(ontology)) {
                _contologies.put(ontology.getID(), ontology);
                DataMaster.fireCOntologyAdded(ontology);
            }
        }
    }

    public static void addNewCOntology(COntology ontology) {
        if (ontology == null) {
            return;
        }
        addNewCOntology(Collections.singletonList(ontology));
    }

    public static List<CMatrix> getLoadedCMatrices() {
        return new ArrayList<>(_cMatrices.values());
    }

    public static COntology getCOntologyByID(String identifier) {
        if (identifier == null || identifier.length() == 0) {
            return null;
        }
        return _contologies.get(identifier);
    }

    public static List<COntology> getLoadedCOntologies() {
        return new ArrayList<>(_contologies.values());
    }

    public static void destroyCoolMapObject(CoolMapObject object) {
        _coolMapObjects.remove(object);
        DataMaster.fireCoolMapObjectToBeDestroyed(object);
        StateStorageMaster.clearStates(object);
        object.destroy();

    }

    public static void destroyCMatrix(CMatrix matrix) {
        //To be implemented
        if (matrix == null) {
            return;
        }
//        if (matrix.isDestroyed() && _cMatrices.values().contains(matrix)) {
//            _cMatrices.remove(matrix.getID());
//        }
        _cMatrices.remove(matrix.getID());
        DataMaster.fireCMatrixToBeRemoved(matrix);

        List<CoolMapObject> objs = CoolMapMaster.getCoolMapObjects();
        if (objs != null && !objs.isEmpty()) {

            for (CoolMapObject obj : objs) {
                obj.removeBaseCMatrix((CMatrix) matrix);
            }
        }
        //how to programmatically set a coolMapObject is tricky

        matrix.destroy();
    }

    public static void renameCMatrix(String matrixID, String newName) {
        CMatrix mx = getCMatrixByID(matrixID);
        if (mx == null) {
            return;
        }
        mx.setName(newName);
        DataMaster.fireCMatrixListenerNameChanged(mx, newName);
    }

    public static void renameCOntology(String ontologyID, String newName) {
        COntology cOntology = getCOntologyByID(ontologyID);
        if (ontologyID == null) {
            return;
        }
        cOntology.setName(newName);
        DataMaster.fireCOntologyNameChanged(cOntology);
    }

    public static void destroyCMatrices(Collection<CMatrix> matrices) {
        if (matrices == null) {
            return;
        }

        for (CMatrix matrix : matrices) {
            try {
                destroyCMatrix(matrix);
            } catch (Exception e) {
                //throw an error message?
            }
        }
    }

    public static void destroyCOntology(COntology ontology) {
        //to be implemented
        if (ontology == null) {
            return;
        }

        String ontologyID = ontology.getID();

        //Also need to remove all view nodes that are associated with this ontology
        for (CoolMapObject object : CoolMapMaster.getCoolMapObjects()) {

            //check all row nodes and column nodes, see whether there are associated nodes
            ArrayList<VNode> rowNodesToRemove = new ArrayList();
            ArrayList<VNode> colNodesToRemove = new ArrayList();
            List<VNode> viewNodesRow = object.getViewNodesRow();
            List<VNode> viewNodesCol = object.getViewNodesColumn();

            for (VNode node : viewNodesRow) {
                if (node.getCOntologyID() != null && node.getCOntologyID().equals(ontologyID)) {
                    rowNodesToRemove.add(node);
                }
            }

            for (VNode node : viewNodesCol) {
                if (node.getCOntologyID() != null && node.getCOntologyID().equals(ontologyID)) {
                    colNodesToRemove.add(node);
                }
            }

            object.removeViewNodesRow(rowNodesToRemove);
            object.removeViewNodesColumn(colNodesToRemove);

            if (!rowNodesToRemove.isEmpty() || !colNodesToRemove.isEmpty()) {
                StateStorageMaster.clearStates(object);
            }
        }

        if (ontology.isDestroyed() && _contologies.values().contains(ontology)) {
            _contologies.remove(ontology.getID());
        }

        DataMaster.fireCOntologyToBeDestroyed(ontology);
        _contologies.remove(ontology.getID());
        ontology.destroy();
    }

    public static void loadNewMatrix(String matrixName, double[][] data, String[] rowLabels, String[] columnLabels, int rowNum, int colNum) {
        DoubleCMatrix matrix = new DoubleCMatrix(matrixName, rowNum, colNum);
        
        if (rowLabels != null) {
            for (int i = 0; i < rowLabels.length; ++i) {
                String rowLabel = rowLabels[i];
                matrix.setRowLabel(i, rowLabel);
            }
        }
        
        if (columnLabels != null) {
            for (int j = 0; j < columnLabels.length; ++j) {
                String columnLabel = columnLabels[j];
                matrix.setColLabel(j, columnLabel);

            }
        }

        for (int i = 0; i < rowNum; ++i) {
            for (int j = 0; j < colNum; ++j) {
                Double value = data[i][j];
                matrix.setValue(i, j, value);
            }
        }

        CoolMapObject object = new CoolMapObject();
        object.setName(Tools.removeFileExtension(matrixName));
        object.addBaseCMatrix(matrix);

        ArrayList<VNode> nodes = new ArrayList<>();
        for (Object label : matrix.getRowLabelsAsList()) {
            nodes.add(new VNode(label.toString()));
        }
        object.insertRowNodes(nodes);

        nodes.clear();
        for (Object label : matrix.getColLabelsAsList()) {
            nodes.add(new VNode(label.toString()));
        }
        object.insertColumnNodes(nodes);

        object.setAggregator(new DoubleDoubleMean());
        object.setSnippetConverter(new DoubleSnippet1_3());
        object.setViewRenderer(new NumberToColor(), true);

        object.getCoolMapView().addColumnMap(new ColumnLabels(object));
        object.getCoolMapView().addColumnMap(new ColumnTree(object));
        object.getCoolMapView().addRowMap(new RowLabels(object));
        object.getCoolMapView().addRowMap(new RowTree(object));

        CoolMapMaster.addNewCoolMapObject(object);
    }

}
