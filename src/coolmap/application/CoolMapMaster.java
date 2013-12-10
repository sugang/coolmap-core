/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application;

import coolmap.application.io.IOMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.state.StateStorageMaster;
import coolmap.application.utils.ActiveCoolMapObjectListenerTunnel;
import coolmap.application.utils.DataMaster;
import coolmap.application.widget.WidgetMaster;
import coolmap.application.widget.impl.WidgetViewport;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.contology.model.COntology;
import coolmap.module.ModuleMaster;
import coolmap.utils.Config;
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
import javax.swing.UIManager;

/**
 *
 * @author gangsu
 */
public final class CoolMapMaster {

    private final static ActiveCoolMapObjectListenerTunnel _activeCoolMapObjectListenerTunnel = ActiveCoolMapObjectListenerTunnel.getInstance();
    
    private final static LinkedHashMap<String, CMatrix> _cMatrices = new LinkedHashMap<String, CMatrix>();
    private final static LinkedHashSet<CoolMapObject> _coolMapObjects = new LinkedHashSet<CoolMapObject>();
    private final static HashSet<ActiveCoolMapChangedListener> _activeCoolMapChangedListeners = new HashSet<ActiveCoolMapChangedListener>();
    private final static LinkedHashMap<String, COntology> _contologies = new LinkedHashMap<String, COntology>();
    private static CoolMapObject _activeCoolMapObject = null;
    private static String _sessionName = null;
    
    //can't be final because I need Config to load before static inialization
    private static CMainFrame _cMainFrame;

    public static String getSessionName() {
        return _sessionName;
    }

    public static CMatrix getCMatrixByID(String identifier) {
        if (identifier == null || identifier.length() == 0) {
            return null;
        }
        return _cMatrices.get(identifier);
    }

    /**
     * will be the folder name of the save file
     *
     * @param name
     */
    public static void setSessionName(String name) {
        if (name == null || name.length() == 0) {
            name = "Untitled";
        }
        _sessionName = name;
        getViewport().setTitle("Canvas ( " + name + " project)");
    }

    public static void newSession(String name) {
        CoolMapObject object = _activeCoolMapObject;
        _activeCoolMapObject = null;
        _fireActiveCoolMapChanged(object, null);

        List<CoolMapObject> coolMapObjects = new ArrayList<CoolMapObject>(_coolMapObjects);
        for (CoolMapObject obj : coolMapObjects) {
            destroyCoolMapObject(obj);
        }

        List<COntology> contologies = new ArrayList<COntology>(_contologies.values());
        for (COntology ontology : contologies) {
            destroyCOntology(ontology);
        }

        List<CMatrix> cmatrices = new ArrayList<CMatrix>(_cMatrices.values());
        for (CMatrix matrix : cmatrices) {
            destroyCMatrix(matrix);
        }

        setSessionName(name);

    }

    public static ActiveCoolMapObjectListenerTunnel getActiveCoolMapObjectListenerDelegate() {
        return _activeCoolMapObjectListenerTunnel;
    }

    public static List<CoolMapObject> getCoolMapObjects() {
        return new ArrayList<CoolMapObject>(_coolMapObjects);
    }
    
    public static CoolMapObject getCoolMapObjectByID(String ID){
        for(CoolMapObject object : _coolMapObjects){
            if(object.getID().equals(ID)){
                return object;
            }
        }
        return null;
    }
    
    
    

    /**
     * initialize the neceesary elements
     */
    public static void initialize() {

        try{
//            WebLookAndFeel.install();
//            UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
//            for( UIManager.LookAndFeelInfo info : infos){
//                System.out.println(info.getName());
//            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e){
            
        }
        
        
        Config.initialize();
        
        //
        _cMainFrame = new CMainFrame();
        
        UI.initialize();
        Tools.initialize();
        IOMaster.initialize();
        WidgetMaster.initialize();
        ModuleMaster.initialize();
        StateStorageMaster.initialize();
        
//        SnippetMaster.initialize();
        
       
//        ServiceMaster.initialize();
        
        //
        
//        CreaterMaster.initialize(); //Creater should be defined as a module
        
        
        //new session
        CoolMapMaster.newSession("");

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

    /**
     * return the viewport
     * @return 
     */
    public static WidgetViewport getViewport() {
        //return ((WidgetViewport) WidgetMaster.getWidget(WidgetMaster.CANVAS));
        return WidgetMaster.getViewport();
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
        
        System.out.println("adding:" + object);
        _coolMapObjects.add(object);
        addNewBaseMatrix(object.getBaseCMatrices());

        object.addCObjectDataListener(_activeCoolMapObjectListenerTunnel);
        object.getCoolMapView().addCViewListener(_activeCoolMapObjectListenerTunnel);

        getViewport().addCoolMapView(object);
        DataMaster.fireCoolMapObjectAdded(object);
    }
    
    public static void addNewCoolMapObject(Collection<CoolMapObject> objects){
        if(objects == null || objects.isEmpty()){
            return;
        }
        
        for(CoolMapObject object : objects){
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
        return new ArrayList<CMatrix>(_cMatrices.values());
    }

    public static COntology getCOntologyByID(String identifier) {
        if (identifier == null || identifier.length() == 0) {
            return null;
        }
        return _contologies.get(identifier);
    }

    public static List<COntology> getLoadedCOntologies() {
        return new ArrayList<COntology>(_contologies.values());
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

        if (matrix.isDestroyed() && _cMatrices.values().contains(matrix)) {
            _cMatrices.remove(matrix.getID());
        }

        _cMatrices.remove(matrix.getID());
        DataMaster.fireCMatrixToBeRemoved(matrix);
        matrix.destroy();
    }

    public static void destroyCOntology(COntology ontology) {
        //to be implemented
        if (ontology == null) {
            return;
        }

        if (ontology.isDestroyed() && _contologies.values().contains(ontology)) {
            _contologies.remove(ontology.getID());
        }

        DataMaster.fireCOntologyToBeDestroyed(ontology);
        _contologies.remove(ontology.getID());
        ontology.destroy();
    }

}
