/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io;

import coolmap.application.CoolMapMaster;
import coolmap.application.io.external.interfaces.ImportCOntology;
import coolmap.application.io.external.interfaces.ImportData;
import coolmap.application.io.internal.cmatrix.DefaultCMatrixExporter;
import coolmap.application.io.internal.cmatrix.DoubleCMatrixImporter;
import coolmap.application.io.internal.cmatrix.interfaces.InternalCMatrixExporter;
import coolmap.application.io.internal.cmatrix.interfaces.InternalCMatrixImporter;
import coolmap.application.io.internal.contology.InternalCOntologyAttributeIO;
import coolmap.application.io.internal.contology.InternalCOntologyIO;
import coolmap.application.io.internal.coolmapobject.InternalCoolMapObjectIO;
import coolmap.application.state.StateStorageMaster;
import coolmap.application.utils.LongTask;
import coolmap.application.utils.TaskEngine;
import coolmap.application.widget.Widget;
import coolmap.application.widget.WidgetMaster;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.datarenderer.renderer.impl.TextRenderer;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.canvas.sidemaps.ColumnMap;
import coolmap.canvas.sidemaps.RowMap;
import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.impl.PassThrough;
import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.impl.DoubleCMatrix;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.contology.model.COntology;
import coolmap.data.snippet.SnippetConverter;
import coolmap.utils.Config;
import coolmap.utils.Tools;
import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TFileOutputStream;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import java.awt.Color;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author gangsu
 */
public class IOMaster {

    //linkedHashSet
    private static LinkedHashSet<Class<ImportCOntology>> ontologyImporters = new LinkedHashSet<Class<ImportCOntology>>();
    private static LinkedHashSet<Class<ImportData>> dataImporters = new LinkedHashSet<Class<ImportData>>();

    public static void registerDataImporter(final Class<ImportData> importerClass) {
        if (importerClass == null) {
            return;
        }

        dataImporters.add(importerClass);

        try {
            MenuItem menuItem = new MenuItem(importerClass.newInstance().getLabel());
            CoolMapMaster.getCMainFrame().addMenuItem("File/Import data", menuItem, false, false);
            menuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Action performed");
                    final ImportData importerInstance;
                    try {
                        importerInstance = importerClass.newInstance();
                    } catch (Exception ex) {
                        CMConsole.logError("failed to initialize data importer :" + importerClass);
                        return;
                    }

                    JFileChooser chooser;
                    final File[] f;
                    if (importerInstance.onlyImportFromSingleFile()) {
                        chooser = Tools.getCustomSingleFileChooser(importerInstance.getFileNameExtensionFilter());
                        chooser.setDialogTitle("Importing from " + importerInstance.getLabel() + " " + (importerInstance.onlyImportFromSingleFile() ? "(single file)" : "(multiple files)"));
                        int returnVal = chooser.showOpenDialog(CoolMapMaster.getCMainFrame());
                        if (returnVal != JFileChooser.APPROVE_OPTION) {
                            return;
                        }
                        f = new File[]{chooser.getSelectedFile()};
                        if (f == null || f.length == 0) {
                            CMConsole.logError("failed to import, no file was selected.");
                            return;
                        }
                        importerInstance.configure(f);

                    } else {

                        chooser = Tools.getCustomMultiFileChooser(importerInstance.getFileNameExtensionFilter());
                        chooser.setDialogTitle("Importing from " + importerInstance.getLabel() + " " + (importerInstance.onlyImportFromSingleFile() ? "(single file)" : "(multiple files)"));
                        int returnVal = chooser.showOpenDialog(CoolMapMaster.getCMainFrame());

                        if (returnVal != JFileChooser.APPROVE_OPTION) {
                            return;
                        }
                        f = chooser.getSelectedFiles();
                        if (f == null || f.length == 0) {
                            CMConsole.logError("failed to import, no files were selected.");
                            return;
                        }
                        importerInstance.configure(f);
                    }

//                    System.out.println("selected files" + f + Arrays.toString(f));
                    LongTask task = new LongTask("import data...") {

                        @Override
                        public void run() {
                            try {

//                                System.out.println("Trying to import data");
                                if (importerInstance.onlyImportFromSingleFile()) {
                                    importerInstance.importFromFile(f[0]);
                                } else {
                                    importerInstance.importFromFiles(f);
                                }

                                Set<CoolMapObject> objects = importerInstance.getImportedCoolMapObjects();
                                Set<COntology> ontologies = importerInstance.getImportedCOntology();

                                if (Thread.interrupted()) {
                                    return;
                                }

                                CoolMapMaster.addNewCOntology(ontologies);
                                CoolMapMaster.addNewCoolMapObject(objects);

                                //When the nodes were created, the contology does not exist, which resulted in base 
                                //System.out.println(objects.iterator().next().getViewNodeRow(0).getBaseIndicesFromCOntology(null, Integer.MIN_VALUE));
                                //The orders are quite important.
                                CMConsole.logInfo("Data imported from: " + Arrays.toString(f));

                            } catch (Exception ex2) {
                                CMConsole.logError("Failed to import data from: " + Arrays.toString(f));
                                return;
                            }
                        }

                    };

                    TaskEngine.getInstance().submitTask(task);
                }
            });
        } catch (Exception e) {
            CMConsole.logError("Failed to import data from: " + importerClass);
        }
    }

    public static void registerOntologyImporter(final Class<ImportCOntology> importerClass) {
        if (importerClass == null) {
            return;
        }

        ontologyImporters.add(importerClass);
        try {

            MenuItem menuItem = new MenuItem(importerClass.newInstance().getLabel());
            CoolMapMaster.getCMainFrame().addMenuItem("File/Import ontology", menuItem, false, false);
            menuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    final ImportCOntology importerInstance;
                    try {
                        importerInstance = importerClass.newInstance();
                    } catch (Exception ex) {
                        CMConsole.logError("failed to initialize ontology importer :" + importerClass);
                        return;
                    }

                    final JFileChooser chooser;
                    final File f[];
                    if (importerInstance.onlyImportFromSingleFile()) {
                        chooser = Tools.getCustomSingleFileChooser(importerInstance.getFileNameExtensionFilter());
                        chooser.setDialogTitle("Importing from " + importerInstance.getLabel() + " " + (importerInstance.onlyImportFromSingleFile() ? "(single file)" : "(multiple files)"));
                        int returnVal = chooser.showOpenDialog(CoolMapMaster.getCMainFrame());
                        if (returnVal != JFileChooser.APPROVE_OPTION) {
                            return;
                        }
                        f = new File[]{chooser.getSelectedFile()};

                    } else {
                        chooser = Tools.getCustomMultiFileChooser(importerInstance.getFileNameExtensionFilter());
                        chooser.setDialogTitle("Importing from " + importerInstance.getLabel() + " " + (importerInstance.onlyImportFromSingleFile() ? "(single file)" : "(multiple files)"));
                        int returnVal = chooser.showOpenDialog(CoolMapMaster.getCMainFrame());
                        if (returnVal != JFileChooser.APPROVE_OPTION) {
                            return;
                        }
                        f = chooser.getSelectedFiles();
                    }

                    importerInstance.configure(f);

                    if (f != null && f.length > 0) {

                        LongTask task = new LongTask("import ontology...") {

                            @Override
                            public void run() {
                                try {

                                    if (importerInstance.onlyImportFromSingleFile()) {
                                        importerInstance.importFromFile(f[0]);
                                    } else {
                                        importerInstance.importFromFiles(f);
                                    }

                                    Set<COntology> ontologies = importerInstance.getImportedCOntology();

                                    for (COntology ontology : ontologies) {
                                        if (ontology == null) {
                                            return;
                                        }

                                        if (Thread.interrupted()) {
                                            return;
                                        }

                                        CoolMapMaster.addNewCOntology(ontology);

                                        CMConsole.logInfo("Ontology imported from " + Arrays.toString(f));
                                    }
                                } catch (Exception e) {
                                    CMConsole.logError("failed to load ontology from " + Arrays.toString(f));
                                }
                            }

                        };

                        TaskEngine.getInstance().submitTask(task);
                    }

                }
            });

        } catch (Exception e) {
            CMConsole.logError("failed to initialize ontology importer :" + importerClass);
        }

    }

    private static void initializeOntologyImporters() {
        if (Config.isInitialized()) {
            try {

                JSONArray importers = Config.getJSONConfig().getJSONObject("io").getJSONObject("ontology-importer").getJSONArray("load");

                for (int i = 0; i < importers.length(); i++) {

                    try {
                        Class<ImportCOntology> importerClass = (Class<ImportCOntology>) Class.forName(importers.getString(i));
                        registerOntologyImporter(importerClass);
                    } catch (Exception e) {
                        CMConsole.logError("failed to initialize Ontology IO from config file." + importers.optString(i));
                    }
                }
            } catch (Exception e) {
                CMConsole.logError("failed to initialize Ontology IO from config file.");
            }
        }
    }

    private static void initializeDataImporters() {
        if (Config.isInitialized()) {
            try {

                JSONArray importers = Config.getJSONConfig().getJSONObject("io").getJSONObject("data-importer").getJSONArray("load");

                for (int i = 0; i < importers.length(); i++) {

                    try {
                        Class<ImportData> importerClass = (Class<ImportData>) Class.forName(importers.getString(i));
                        registerDataImporter(importerClass);
                    } catch (Exception e) {
                        CMConsole.logError("failed to initialize Data IO from config file." + importers.optString(i));
                    }
                }
            } catch (Exception e) {
                CMConsole.logError("failed to initialize Data IO from config file.");
            }
        }

        //other importers with Configuration panels
    }

    private static void initializeCreateNew() {
        MenuItem menuItem = new MenuItem("New project", new MenuShortcut(KeyEvent.VK_N, true));
        CoolMapMaster.getCMainFrame().addMenuItem("File", menuItem, false, true);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                //check whether need to save current session.

                CoolMapMaster.newSession("Untitled", null);
            }
        });

    }

    private static void initializeSave() {
        TConfig.get().setArchiveDetector(new TArchiveDetector("cpj", new JarDriver(IOPoolLocator.SINGLETON)));

        MenuItem item = new MenuItem("Save", new MenuShortcut(KeyEvent.VK_S));
        CoolMapMaster.getCMainFrame().addMenuItem("File", item, true, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                //The only difference is if the file is till
                File file = null;
                String uri = CoolMapMaster.getSession().getSessionURI();
                if (uri != null) {

//                    System.out.println("Current uri: " + uri);
                    file = new File(uri);

                    if (!file.exists() || file.isDirectory() || !file.canWrite()) {
                        //need a new File
                        file = null;
                    };
                }

                //if can't write to the file
                if (file == null) {
                    JFileChooser chooser = Tools.getCustomSingleFileChooser(new FileNameExtensionFilter("CoolMap project file", "cpj"));
                    int returnValue = chooser.showSaveDialog(CoolMapMaster.getCMainFrame());
                    if (returnValue != JFileChooser.APPROVE_OPTION) {
                        return;
                    }

                    File saveFile = chooser.getSelectedFile();

                    String path = saveFile.getAbsolutePath();
                    path = Tools.appendPathExtension(path, "cpj");

                    if (saveFile.exists()) {
                        int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), "Override existing file " + saveFile + "?", "Override warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (returnVal == JOptionPane.CANCEL_OPTION) {
                            return;
                        }

                        try {
                            saveFile.delete();
                        } catch (Exception ex) {
                            CMConsole.logError(saveFile + " can not be deleted. Try to save as an alternative file instead.");
                            path = Tools.removeFileExtension("cpj");

                            DateFormat dateFormat = new SimpleDateFormat(" yyyy-MM-dd HH-mm-ss ");
                            Date date = new Date();

                            path += dateFormat.format(date);

                            Tools.appendPathExtension(path, "cpj");
                        }
                    }
                    file = new TFile(path);
                }

                //save it
                final TFile outputFile = new TFile(file);
                LongTask task = new LongTask("Saving project...") {

                    @Override
                    public void run() {
                        try {
                            saveProject(outputFile);
                            CMConsole.logInfo("Saved project to: " + outputFile.getAbsolutePath());
                        } catch (Exception ex) {
                            FileUtils.deleteQuietly(new File(outputFile.getAbsolutePath())); //try to delete this corrupted file if anything occurs
                            CMConsole.logError("Error occured when saving to " + outputFile + ", please save to a different file or export data");
                        }

                    }
                };//task

                TaskEngine.getInstance().submitTask(task);
            }
        });

        item = new MenuItem("Save as...", new MenuShortcut(KeyEvent.VK_S, true));
        CoolMapMaster.getCMainFrame().addMenuItem("File", item, false, true);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = Tools.getCustomSingleFileChooser(new FileNameExtensionFilter("CoolMap project file", "cpj"));
                int returnValue = chooser.showSaveDialog(CoolMapMaster.getCMainFrame());
                if (returnValue != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File saveFile = chooser.getSelectedFile();

                String path = saveFile.getAbsolutePath();
                path = Tools.appendPathExtension(path, "cpj");

                if (saveFile.exists()) {

                    int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), "Override existing file " + saveFile + "?", "Override warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (returnVal == JOptionPane.CANCEL_OPTION) {
                        return;
                    }

                    try {
                        saveFile.delete();
                    } catch (Exception ex) {
                        CMConsole.logError(saveFile + " can not be deleted. Try to save as an alternative file instead.");
                        path = Tools.removeFileExtension("cpj");

                        DateFormat dateFormat = new SimpleDateFormat(" yyyy-MM-dd HH-mm-ss ");
                        Date date = new Date();

                        path += dateFormat.format(date);

                        Tools.appendPathExtension(path, "cpj");
                    }
                }
//  
                final TFile file = new TFile(path);

                LongTask task = new LongTask("Saving project...") {

                    @Override
                    public void run() {
                        //dev

//                        File pfile = new File("/Users/sugang/000.cpj");
//                        if (pfile.exists()) {
//                            pfile.delete();
//                        }
//                        TFile file = new TFile("/Users/sugang/000.cpj");
                        try {

                            saveProject(file);
                            CMConsole.logInfo("Saved project to: " + file.getAbsolutePath());
                        } catch (Exception ex) {
                            FileUtils.deleteQuietly(new File(file.getAbsolutePath())); //try to delete this corrupted file if anything occursfi
                            CMConsole.logError("Error occured when saving to " + file + ", please save to a different file or export data");
                        }

                    }
                };//task

                TaskEngine.getInstance().submitTask(task);

            }//end of action performed
        });

    }

    //also need to save widget states
    private static void saveWidgets(TFile projectFile) throws Exception {
        Set<Widget> allWidgets = WidgetMaster.getAllWidgets();
        JSONObject widgetObject = new JSONObject();
        for (Widget w : allWidgets) {
            JSONObject state = w.getCurrentState();
            if (state != null) {
                widgetObject.put(w.getClass().getName(), state);
            }
        }

        //Yeah nothing was saved.
        if (widgetObject.length() > 0) {
            TFile file = new TFile(projectFile.getAbsolutePath() + File.separator + IOTerm.DIR_WIDGET + File.separator + IOTerm.FILE_DATA);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(file)));
            writer.write(widgetObject.toString());
            writer.flush();
            writer.close();

//            System.out.println(file);
//            System.out.println("state to save:" + widgetObject);
        }
    }

    private static void saveProject(TFile projectFile) throws Exception {

        saveProjectProperties(projectFile);
        saveCOntologies(projectFile);
        saveCMatrices(projectFile);
        saveCoolMapObjects(projectFile);
        saveWidgets(projectFile);

        //this is needed to commit all changes in the temp projectFile and finish the saving
        //This is needed to synchornize the archive folder
        //may throw an exception - not sure whether it may throw a warning
        try {
            TVFS.umount(projectFile);
        } catch (Exception e) {
            //should make sure all streams are closed
            System.err.println("TVFS unmount error");
        }

        CoolMapMaster.updateSession(Tools.removeFileExtension(projectFile.getName()), projectFile.getPath());
        StateStorageMaster.clearAllStates();

        //try load project immediately
    }

    private static void saveCoolMapObjects(TFile projectFile) throws Exception {
        List<CoolMapObject> objects = CoolMapMaster.getCoolMapObjects();
        for (CoolMapObject object : objects) {
            InternalCoolMapObjectIO.dumpData(object, projectFile);
        }
    }

    private static void saveCMatrices(TFile projectFile) throws Exception {

        List<CMatrix> matrices = CoolMapMaster.getLoadedCMatrices();
        InternalCMatrixExporter exporter;
        for (CMatrix matrix : matrices) {
            TFile matrixFolder = new TFile(projectFile.getAbsolutePath() + File.separator + IOTerm.DIR_CMATRIX + File.separator + matrix.getID());

            //check if a custom exporter is needed
            if ((exporter = getExporter(matrix.getClass())) == null) {
                exporter = new DefaultCMatrixExporter();
            }

            exporter.dumpData(matrix, matrixFolder);
        }
    }

    public static InternalCMatrixExporter getExporter(Class<? extends CMatrix> cmatrixClass) {
        return exporters.get(cmatrixClass);
    }

    public static InternalCMatrixImporter getImporter(Class<? extends CMatrix> cmatrixClass) {
        return importers.get(cmatrixClass);
    }

    public static void setImporter(Class<? extends CMatrix> cmatrixClass, InternalCMatrixImporter importer) {
        importers.put(cmatrixClass, importer);
    }

    public static void setExporter(Class<? extends CMatrix> cmatrixClass, InternalCMatrixExporter exporter) {
        exporters.put(cmatrixClass, exporter);
    }

    //IO
    private static HashMap<Class<? extends CMatrix>, InternalCMatrixImporter> importers = new HashMap();
    private static HashMap<Class<? extends CMatrix>, InternalCMatrixExporter> exporters = new HashMap();

    private static void saveCOntologies(TFile projectFile) throws Exception {
        List<COntology> ontologies = CoolMapMaster.getLoadedCOntologies();
        for (COntology ontology : ontologies) {
            String ontologyFolderPath = projectFile.getAbsolutePath() + File.separator + IOTerm.DIR_CONTOLOGY + File.separator + ontology.getID();

            //first write the properties
            BufferedWriter propertyWriter = new BufferedWriter(new OutputStreamWriter(
                    new TFileOutputStream(ontologyFolderPath + File.separator + IOTerm.FILE_PROPERTY)));

            JSONObject contologyPropertyEntry = new JSONObject();

            contologyPropertyEntry.put(IOTerm.ATTR_ID, ontology.getID());
            contologyPropertyEntry.put(IOTerm.ATTR_NAME, ontology.getName());
            if (ontology.getDescription() != null && ontology.getDescription().length() > 0) {
                contologyPropertyEntry.put(IOTerm.ATTR_DESCRIPTION, ontology.getDescription());
            }

            if (ontology.getViewColor() != null) {
                Color c = ontology.getViewColor();
                //use c.getRGB
                contologyPropertyEntry.put(IOTerm.ATTR_COLOR, c.getRGB());
            }

            propertyWriter.write(contologyPropertyEntry.toString());
            propertyWriter.flush();
            propertyWriter.close();

            //then export the data
            BufferedWriter dataWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(
                    ontologyFolderPath + File.separator + IOTerm.FILE_DATA
            )));

            InternalCOntologyIO.dumpData(dataWriter, ontology);
        }

        //Also save contology attributes
        //Save ontology attributes
        String ontologyAttributeFolderPath = projectFile.getAbsolutePath() + File.separator + IOTerm.DIR_CONTOLOGY_ATTRIBUTE;
        BufferedWriter propertyWriter = new BufferedWriter(new OutputStreamWriter(
                new TFileOutputStream(ontologyAttributeFolderPath + File.separator + IOTerm.FILE_DATA)));
        InternalCOntologyAttributeIO.dumpData(propertyWriter);
    }

    private static void saveProjectProperties(TFile projectFile) throws Exception {

        BufferedWriter propertyWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(projectFile.getAbsolutePath() + File.separator + IOTerm.FILE_PROJECT_INFO)));
        JSONObject projectInfo = new JSONObject();

        //date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        projectInfo.put(IOTerm.ATTR_PROJECT_DATE, dateFormat.format(new Date()));

        //session name
        String sessionName = CoolMapMaster.getSession().getSessionName();
        if (sessionName == null || sessionName.length() == 0) {
            sessionName = "Untitled";
        }
        projectInfo.put(IOTerm.ATTR_PROJECT_SESSION_NAME, sessionName);

        //cmatrices projectInfo object
        ArrayList<String> matrixIDs = new ArrayList<>();

        List<CMatrix> baseMatrices = CoolMapMaster.getLoadedCMatrices();
        for (CMatrix matrix : baseMatrices) {
            matrixIDs.add(matrix.getID());
        }
        projectInfo.put(IOTerm.OBJECT_CMATRIX_ID, matrixIDs);

        //contology projectInfo object
        ArrayList contologyIDs = new ArrayList();

        List<COntology> ontologies = CoolMapMaster.getLoadedCOntologies();
        for (COntology ontology : ontologies) {
            contologyIDs.add(ontology.getID());
        }
        projectInfo.put(IOTerm.OBJECT_CONTOLOGY_ID, contologyIDs);

        //coolmap objects
        ArrayList coolMapIDs = new ArrayList();

        List<CoolMapObject> objects = CoolMapMaster.getCoolMapObjects();

        for (CoolMapObject object : objects) {
//            JSONObject coolMapObjectPropertyEntry = new JSONObject();
//            coolMapObjectProperties.put(object.getID(), coolMapObjectPropertyEntry);
//
//            coolMapObjectPropertyEntry.put(IOTerm.ATTR_ID, object.getID());
//            coolMapObjectPropertyEntry.put(IOTerm.ATTR_NAME, object.getName());
//
//            CoolMapView view = object.getCoolMapView();
//            coolMapObjectPropertyEntry.put(IOTerm.ATTR_VIEW_ZOOM, new float[]{view.getZoomX(), view.getZoomY()});
//
//            Point mapAnchor = object.getCoolMapView().getMapAnchor();
//            coolMapObjectPropertyEntry.put(IOTerm.ATTR_VIEW_ANCHOR, new int[]{mapAnchor.x, mapAnchor.y});
//
//            ArrayList<String> linkedMxIDs = new ArrayList<String>();
//            List<CMatrix> linkedMxs = object.getBaseCMatrices();
//            for (CMatrix mx : linkedMxs) {
//                linkedMxIDs.add(mx.getID());
//            }

            //other info should be read from the corresponding folders
            //as the state of these things need to be persisted as well.
//            coolMapObjectPropertyEntry.put(IOTerm.ATTR_VIEW_MATRICES, linkedMxIDs); //can restore the cMatrix by using such matrices, when they are reloaded
            coolMapIDs.add(object.getID());
        }
        projectInfo.put(IOTerm.OBJECT_COOLMAPOBJECT_ID, coolMapIDs);

        propertyWriter.write(projectInfo.toString());
        propertyWriter.flush();
        propertyWriter.close();

    }

    //The save would save to the last saved path - if saved before
    private static void loadProject(TFile projectFile) throws Exception {
        try {

            CoolMapMaster.newSession(Tools.removeFileExtension(projectFile.getName()), projectFile.getAbsolutePath());
            File propertyFile = new TFile(projectFile.getAbsolutePath() + File.separator + IOTerm.FILE_PROJECT_INFO);
            JSONObject property = new JSONObject(IOUtils.toString(new TFileInputStream(propertyFile)));

            //try to unzip it
            //no need to.
//            TFile directory = new TFile(projectFile.getParentFile().getAbsolutePath() + File.separator + "0000-unzipped");
//            FileUtils.deleteDirectory(directory);
//            TFile.cp_rp(projectFile, directory, TArchiveDetector.NULL);
            //Got to widget state are restored at the beginning of the .. not here
            loadCMatrices(projectFile, property);
            loadCOntologies(projectFile, property);
            loadCoolMapObjects(projectFile, property);
            loadWidgetStates(projectFile);

        } catch (InterruptedException e) {
            CoolMapMaster.newSession("Untitled", null);
            //Also get some warning message
        }

    }

    private static void loadWidgetStates(TFile projectFile) throws Exception {

        TFile widgetFile = new TFile(projectFile.getPath() + File.separator + IOTerm.DIR_WIDGET + File.separator + IOTerm.FILE_DATA);
        try {
            JSONObject widgetJSON = new JSONObject(
                    IOUtils.toString(new TFileInputStream(widgetFile))
            );

            Set<Widget> allWidgets = WidgetMaster.getAllWidgets();
            for (Widget w : allWidgets) {
                JSONObject config = widgetJSON.optJSONObject(w.getClass().getName());
                if (w != null) {
                    w.restoreState(config);
                }
            }

        } catch (IOException ex) {
            return;
        }

    }

    /**
     * load COntologies
     *
     * @param projectFile
     * @param property
     * @throws Exception
     */
    private static void loadCOntologies(TFile projectFile, JSONObject property) throws Exception {
        JSONArray contologyIDs = property.getJSONArray(IOTerm.OBJECT_CONTOLOGY_ID);
        List<COntology> contologies = new ArrayList<COntology>();
        for (int i = 0; i < contologyIDs.length(); i++) {
            Object cOntologyID = contologyIDs.get(i);
            TFile contologyFolder = new TFile(projectFile.getAbsolutePath() + File.separator + IOTerm.DIR_CONTOLOGY + File.separator + cOntologyID);
            TFile contologyPropertyFile = new TFile(contologyFolder.getAbsolutePath() + File.separator + IOTerm.FILE_PROPERTY);
            JSONObject contologyProperty = new JSONObject(IOUtils.toString(new TFileInputStream(contologyPropertyFile)));
//            System.out.println(contologyProperty);

            String id = contologyProperty.getString(IOTerm.ATTR_ID);
            String name = contologyProperty.optString(IOTerm.ATTR_NAME);
            Color c = null;
            try {
                c = new Color(Integer.parseInt(contologyProperty.getString(IOTerm.ATTR_COLOR)));
            } catch (Exception e) {
                //go on
            }
            String description = contologyProperty.optString(IOTerm.ATTR_DESCRIPTION, null);

            COntology ontology = new COntology(name, description, id);
            if (c != null) {
                ontology.setViewColor(c);
            }

            TFile contologyDataFile = new TFile(contologyFolder.getAbsolutePath() + File.separator + IOTerm.FILE_DATA);
            InternalCOntologyIO.loadData(new BufferedReader(new InputStreamReader(new TFileInputStream(contologyDataFile))), ontology);

//            COntologyUtils.printOntology(ontology);
            contologies.add(ontology);
        }
        CoolMapMaster.addNewCOntology(contologies);
    }

    /**
     * finally - load CoolMap Objects!
     *
     * @param projectFile
     * @param property
     * @throws Exception
     */
    private static void loadCoolMapObjects(TFile projectFile, JSONObject property) throws Exception {
        JSONArray coolMapObjectIDs = property.getJSONArray(IOTerm.OBJECT_COOLMAPOBJECT_ID);
        List<CoolMapObject> coolMapObjects = new ArrayList<>();
//        System.out.println("CoolMapObjectID:" + coolMapObjectIDs);
//        System.out.println("Length:" + coolMapObjectIDs.length());

        for (int i = 0; i < coolMapObjectIDs.length(); i++) {

            Object coolMapObjectID = coolMapObjectIDs.get(i);
//            System.out.println("Creating coolMapObject:" + coolMapObjectID );
            TFile coolMapObjectFolder = new TFile(projectFile.getAbsolutePath() + File.separator + IOTerm.DIR_COOLMAPOBJECT + File.separator + coolMapObjectID);
            TFile coolMapPropertyFile = new TFile(coolMapObjectFolder.getAbsolutePath() + File.separator + IOTerm.FILE_PROPERTY);
            JSONObject coolMapProperty = new JSONObject(IOUtils.toString(new TFileInputStream(coolMapPropertyFile)));

//            System.out.println(coolMapProperty.toString(2));
            String id = coolMapProperty.getString(IOTerm.ATTR_ID);
            String name = coolMapProperty.optString(IOTerm.ATTR_NAME, "Untitled");
            String aggrClass = coolMapProperty.optString(IOTerm.ATTR_VIEW_AGGREGATOR_CLASS, null);
            String rendererClass = coolMapProperty.optString(IOTerm.ATTR_VIEW_RENDERER_CLASS, null);
            String snipClass = coolMapProperty.optString(IOTerm.ATTR_VIEW_SNIPPETCONVERTER_CLASS, null);
            JSONArray anchor = coolMapProperty.getJSONArray(IOTerm.ATTR_VIEW_ANCHOR);
            JSONArray zoom = coolMapProperty.getJSONArray(IOTerm.ATTR_VIEW_ZOOM);

            JSONArray linkedCMatrixIDs = coolMapProperty.getJSONArray(IOTerm.ATTR_VIEW_MATRICES);

            CoolMapObject object = new CoolMapObject(id);
            object.setName(name);

            //First need to restore state.
            InternalCoolMapObjectIO.restoreCoolMapObjectState(object, coolMapObjectFolder);

            //add matrices            
            for (int j = 0; j < linkedCMatrixIDs.length(); j++) {
                CMatrix matrix = CoolMapMaster.getCMatrixByID(linkedCMatrixIDs.getString(j));
                object.addBaseCMatrix(matrix);
//                System.out.println(matrix);
            }

            //Set aggregator
            if (aggrClass != null) {
                CAggregator aggregator;
                try {
                    aggregator = (CAggregator) Class.forName(aggrClass).newInstance();
                    //Also need to restore from JSON
                } catch (Exception e) {
                    aggregator = new PassThrough();
                }
                object.setAggregator(aggregator);
            }

            //Set snippet converter
            if (snipClass != null) {
                SnippetConverter snip;
                try {
                    snip = (SnippetConverter) Class.forName(snipClass).newInstance();
                } catch (Exception e) {
                    snip = null;
                    e.printStackTrace();
                }
                object.setSnippetConverter(snip);
            }

            //viewRenderer
            if (rendererClass != null) {
                ViewRenderer renderer = null;
                try {
                    renderer = (ViewRenderer) Class.forName(rendererClass).newInstance();

                    //also need to restore state.
                    TFile rendererPropsFile = new TFile(coolMapObjectFolder.getAbsolutePath() + File.separator + IOTerm.FILE_PROPERTY_RENDERER);
                    object.setViewRenderer(renderer, true);

                    try {
                        if (rendererPropsFile.exists()) {
                            JSONObject props = new JSONObject(IOUtils.toString(new TFileInputStream(rendererPropsFile)));
//                            System.out.println(props);
                            renderer.restoreState(props);
                        }
                    } catch (Exception e) {
                        CMConsole.logError("Error loading renderer " + renderer.getName() + " state. Using default parameters instead.");
                    }

                } catch (Exception e) {
                    renderer = new TextRenderer(); //Set to default String renderer if all fails

                    //A default text renderer
                    object.setViewRenderer(renderer, true);
                }

            }

            //set base nodes
//            System.out.println(object.getAggregator());
            if (zoom != null) {
                try {
                    int zoomX = zoom.getInt(0);
                    int zoomY = zoom.getInt(1);
                    object.getCoolMapView().setZoomLevels(zoomX, zoomY);
                } catch (Exception e) {

                }
            }

            if (anchor != null) {

                try {

                    Point pt = new Point(anchor.getInt(0), anchor.getInt(1));
                    object.getCoolMapView().moveMapTo(pt.x, pt.y);

                } catch (Exception e) {

                }

            }

            //load row panel configs
            JSONObject rowPanelConfig = coolMapProperty.getJSONObject(IOTerm.ATTR_VIEW_PANEL_ROW);
            try {
                boolean visible = rowPanelConfig.optBoolean(IOTerm.ATTR_VIEW_PANEL_CONTAINER_VISIBLE, false);
                if (!visible) {
                    object.getCoolMapView().setRowPanelsVisible(false);
                }

                JSONArray panels = rowPanelConfig.optJSONArray(IOTerm.ATTR_VIEW_PANEL);
                for (int j = 0; j < panels.length(); j++) {
                    try {
                        JSONObject panelEntry = panels.getJSONObject(j);
                        RowMap map = (RowMap) Class.forName(panelEntry.getString(IOTerm.ATTR_CLASS)).getConstructor(CoolMapObject.class).newInstance(object);
                        //restore map config
                        JSONObject config = panelEntry.optJSONObject(IOTerm.ATTR_CONFIG);
                        map.restoreState(config);
                        object.getCoolMapView().addRowMap(map);
                    } catch (Exception e) {

                    }
                }

            } catch (Exception e) {

            }

            JSONObject columnPanelConfig = coolMapProperty.getJSONObject(IOTerm.ATTR_VIEW_PANEL_COLUMN);
            try {
                boolean visible = columnPanelConfig.optBoolean(IOTerm.ATTR_VIEW_PANEL_CONTAINER_VISIBLE, false);
                if (!visible) {
                    object.getCoolMapView().setColumnPanelsVisible(false);
                }

                JSONArray panels = columnPanelConfig.optJSONArray(IOTerm.ATTR_VIEW_PANEL);
                for (int j = 0; j < panels.length(); j++) {
                    try {
                        JSONObject panelEntry = panels.getJSONObject(j);
                        ColumnMap map = (ColumnMap) Class.forName(panelEntry.getString(IOTerm.ATTR_CLASS)).getConstructor(CoolMapObject.class).newInstance(object);
                        //restore map config
                        JSONObject config = panelEntry.optJSONObject(IOTerm.ATTR_CONFIG);
                        map.restoreState(config);
                        object.getCoolMapView().addColumnMap(map);
                    } catch (Exception e) {

                    }
                }

            } catch (Exception e) {

            }

            if (Thread.interrupted()) {
                throw new InterruptedException("Loading cancelled");
            }

//            System.out.println(coolMapProperty);
            coolMapObjects.add(object);

//            CoolMapMaster.addNewCoolMapObject(object);
            CoolMapView view = object.getCoolMapView();
            //
            boolean isMaximized = coolMapProperty.optBoolean(IOTerm.ATTR_VIEW_MAXIMIZED, false);
            if (isMaximized) {
                try {
                    view.setMaximize(true);
                } catch (Exception e) {

                }
            }

            //
            boolean isMinimized = coolMapProperty.optBoolean(IOTerm.ATTR_VIEW_MINIMIZED, false);
            if (isMinimized) {
                try {
                    view.setMinimize(true);
                } catch (Exception e) {

                }
            }

            //configure frame after adding.
            JSONArray bounds = coolMapProperty.optJSONArray(IOTerm.ATTR_VIEW_BOUNDS);

            if (bounds != null && !isMaximized && !isMinimized) {
                try {
                    Rectangle r = new Rectangle(bounds.getInt(0), bounds.getInt(1), bounds.getInt(2), bounds.getInt(3));
                    view.setBounds(r);
                } catch (Exception e) {
                }
            }

//            System.out.println("Current index:" + i);
        }//break

//        System.out.println("Loading completed:");
//        System.out.println(coolMapObjects);
//        System.out.println(CoolMapMaster.getLoadedCMatrices());
        CoolMapMaster.addNewCoolMapObject(coolMapObjects);
//        CMConsole.logInfo("Load successful\nit is !\n");
//        CMConsole.logWarning("This is a warning");
//        CMConsole.logError("This is an error");
//        CMConsole.log("This is a message");
    }

    private static void loadCMatrices(TFile projectFile, JSONObject property) throws Exception {
        JSONArray cmatrixIDs = property.getJSONArray(IOTerm.OBJECT_CMATRIX_ID);
        List<CMatrix> matrices = new ArrayList<CMatrix>();
        for (int i = 0; i < cmatrixIDs.length(); i++) {
            Object cMatrixID = cmatrixIDs.get(i);
            TFile matrixFolder = new TFile(projectFile.getAbsolutePath() + File.separator + IOTerm.DIR_CMATRIX + File.separator + cMatrixID);
            TFile matrixPropertyFile = new TFile(matrixFolder.getAbsolutePath() + File.separator + IOTerm.FILE_PROPERTY);
            JSONObject matrixProperty = new JSONObject(IOUtils.toString(new TFileInputStream(matrixPropertyFile)));

            Class cmatrixClass = Class.forName(matrixProperty.getString(IOTerm.ATTR_CLASS));

            InternalCMatrixImporter importer = getImporter(cmatrixClass);

            if (importer != null) {
                CMatrix matrix = importer.loadData(matrixFolder, matrixProperty);
                matrices.add(matrix);
            }
        }
        CoolMapMaster.addNewBaseMatrix(matrices);
    }

    private static void initializeLoad() {
        MenuItem item = new MenuItem("Open Project", new MenuShortcut(KeyEvent.VK_O));
        CoolMapMaster.getCMainFrame().addMenuItem("File", item, false, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = Tools.getCustomSingleFileChooser(new FileNameExtensionFilter("Coolmap project file", "cpj"));
                int returnVal = chooser.showOpenDialog(CoolMapMaster.getCMainFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        File inFile = chooser.getSelectedFile();
                        TFile file = new TFile(inFile);
                        loadProject(file);
                        CMConsole.logInfo("Opened project from: " + file.getAbsolutePath());
                    } catch (Exception ex) {
                        CMConsole.logError("Error occured when trying to open project. File maybe corrupted.");
                    }
                }
            }
        });

    }

    private static void initializeDefaultInternalIO() {
        setImporter(DoubleCMatrix.class, new DoubleCMatrixImporter());
//        setExporter(DoubleCMatrix.class, new DefaultCMatrixExporter());//not necessary; default always use toString method

    }

    public static void initialize() {

        initializeDefaultInternalIO();
        initializeCreateNew();
        initializeLoad();
        initializeSave();
        initializeDataImporters();
        initializeOntologyImporters();
        initializeOntologyAttributes();

    }

    private static void initializeOntologyAttributes() {
        MenuItem item = new MenuItem("from tsv file");
        CoolMapMaster.getCMainFrame().addMenuItem("File/Import ontology attributes", item, false, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = Tools.getCustomSingleFileChooser(new FileNameExtensionFilter("tsv file", "tsv", "txt"));
                int returnVal = chooser.showOpenDialog(CoolMapMaster.getCMainFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    if (file == null) {
                        CMConsole.logError("No ontology attribute file was selected");
                        return;
                    }
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line;
                        int counter = 0;
                        String[] header = null;
                        while((line = reader.readLine()) != null){
                            if(counter == 0){
                                header = line.split("\\t", -1);
                            }
                            else{
                            
                                String ele[] = line.split("\t", -1);
                                String nodeName = ele[0];
                                for( int i = 1; i < header.length; i++){
                                    String attrName = header[i];
                                    COntology.setAttribute(nodeName, attrName, ele[i]);
                                }
                            }
                            counter++;
                        }
                        
                        System.out.println("headers: " + Arrays.toString(header));
                        System.out.println("Imported: " + counter);
                        
                        System.out.println(COntology.getAttribute("2", "Disorder class"));
                        
                        
                    } catch (Exception ex) {
                        CMConsole.logError("Error importing ontology attributes, please check data.");
                    }
                        

                }
            }
        });
    }

}
