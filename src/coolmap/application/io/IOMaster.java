/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io;

import coolmap.application.CoolMapMaster;
import coolmap.application.io.external.interfaces.ImportCOntology;
import coolmap.application.io.external.interfaces.ImportData;
import coolmap.application.io.internal.cmatrix.ICMatrixIO;
import coolmap.application.io.internal.contology.InternalCOntologyAttributeIO;
import coolmap.application.io.internal.contology.InternalCOntologyIO;
import coolmap.application.io.internal.contology.PrivateCOntologyStructureFileIO;
import coolmap.application.io.internal.coolmapobject.PrivateCoolMapObjectIO;
import coolmap.application.utils.LongTask;
import coolmap.application.utils.TaskEngine;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.impl.PassThrough;
import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.contology.model.COntology;
import coolmap.data.snippet.SnippetConverter;
import coolmap.utils.Config;
import coolmap.utils.Tools;
import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileOutputStream;
import de.schlichtherle.truezip.file.TFileReader;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import java.awt.Color;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
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

                                System.out.println("Trying to import data");

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

                                CoolMapMaster.addNewCoolMapObject(objects);
                                CoolMapMaster.addNewCOntology(ontologies);

                                CMConsole.logInSuccess("Data imported from: " + Arrays.toString(f));

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

                                        CMConsole.logInSuccess("Ontology imported from " + Arrays.toString(f));
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

    private static void _initActions() {

        MenuItem menuItem = new MenuItem("Open project", new MenuShortcut(KeyEvent.VK_O, false));
        CoolMapMaster.getCMainFrame().addMenuItem("File", menuItem, true, false);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                try {

                    //always new session.
                    CoolMapMaster.newSession("");

                    System.out.println("opening session");

                    //File projectDirectory = new File("/Users/gangsu/Dropbox/gdscluster");
                    JFileChooser fileChooser = Tools.getFolderChooser();
                    int returnValue = fileChooser.showOpenDialog(CoolMapMaster.getCMainFrame());
                    if (returnValue != JFileChooser.APPROVE_OPTION) {
                        return;
                    }

                    File projectDirectory = fileChooser.getSelectedFile();

                    //File projectDirectory = new File("/Users/gangsu/000");
                    if (projectDirectory == null || !projectDirectory.isDirectory()) {
                        //returns
                        //throws exception
                        return;
                    }

                    File projectInfoFile = new File(projectDirectory.getAbsolutePath() + File.separator + IOTerm.FILE_PROJECT_INFO);
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new FileReader(projectInfoFile));
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    JSONObject projectInfo = new JSONObject(sb.toString());

                    //System.out.println(projectInfo);
                    String cmatrixDirectory = projectDirectory.getAbsolutePath() + File.separator + IOTerm.OBJECT_CMATRIX_ID;

                    //restore cmatrices
                    JSONObject cmatrices = projectInfo.getJSONObject(IOTerm.OBJECT_CMATRIX_ID);
                    Iterator<String> iter = cmatrices.keys();
                    while (iter.hasNext()) {
                        String cmatrixID = iter.next();
                        JSONObject cmatrixEntry = cmatrices.getJSONObject(cmatrixID);
                        //then decompose
                        String cmatrixName = cmatrixEntry.optString(IOTerm.ATTR_NAME);
                        int numRow = cmatrixEntry.optInt(IOTerm.ATTR_NUMROW);
                        int numColumn = cmatrixEntry.optInt(IOTerm.ATTR_NUMCOLUMN);
                        String datatype = cmatrixEntry.optString(IOTerm.ATTR_MEMBERCLASS);
                        String io = cmatrixEntry.optString(IOTerm.FIELD_CMATRIX_ICMATRIXIO);
                        String cmatrixClassString = cmatrixEntry.optString(IOTerm.ATTR_CLASS);

//                        System.out.println(cmatrixID + " " + cmatrixName + " " + numRow + " " + numColumn + " " + datatype + " " + io);
                        Class loaderClass = Class.forName(io);
                        ICMatrixIO loader = (ICMatrixIO) loaderClass.newInstance();
                        Class cmatrixClass = Class.forName(cmatrixClassString);
//                        System.out.println("cmatrixClass:" + cmatrixClassString);

                        File directory = new File(cmatrixDirectory + File.separator + cmatrixID);

                        CMatrix matrix = loader.importFromDirectory(cmatrixID, cmatrixName, numRow, numColumn, directory, cmatrixClass);
                        CoolMapMaster.addNewBaseMatrix(matrix);
                        //matrix.printMatrix();
                        //System.out.println("Loaded matrix:" + matrix.getName() + "\n");
                    }

//                    System.out.println("Loaded CMatrices Num:" + CoolMapMaster.getLoadedCMatrices().size());
                    //////////////////////////////////////////////////////////////////////////
                    //finished restoring matrices
                    //restore ontologies
                    String contologyDirectory = projectDirectory.getAbsolutePath() + File.separator + IOTerm.OBJECT_CONTOLOGY_ID;

                    JSONObject contologies = projectInfo.getJSONObject(IOTerm.OBJECT_CONTOLOGY_ID);
                    iter = contologies.keys();
                    while (iter.hasNext()) {
                        String contologyID = iter.next();
                        JSONObject contologyEntry = contologies.getJSONObject(contologyID);
                        //decompose data
                        String name = contologyEntry.optString(IOTerm.ATTR_NAME);
                        String description = contologyEntry.optString(IOTerm.ATTR_DESCRIPTION);
                        String colorString = contologyEntry.optString(IOTerm.ATTR_COLOR, null);
                        Color color;
                        if (colorString == null) {
                            color = null;
                        } else {
                            try {
                                color = new Color(Integer.parseInt(colorString));
                            } catch (Exception e) {
                                color = null;
                            }
                        }
                        //String edgeAttributeClassString = contologyEntry.optString(IOTerm.FIELD_CONTOLOGY_EDGETATTRIBUTECLASS);
                        //the default 
//                            Class<COntologyEdgeAttributeImpl> edgeAttributeClass;
//                            if(edgeAttributeClassString == null){
//                                edgeAttributeClass = (Class<COntologyEdgeAttributeImpl> )COntologyEdgeAttributeImpl.class;
//                            }
//                            else{
//                                try{
//                                    edgeAttributeClass = (COntologyEdgeAttributeImpl)Class.forName(edgeAttributeClassString);
//                                }
//                                catch(Exception e){
//                                    
//                                }
//                            }
                        File directory = new File(contologyDirectory + File.separator + contologyID);
                        PrivateCOntologyStructureFileIO ontoIO = new PrivateCOntologyStructureFileIO();
                        COntology ontology = ontoIO.readFromFolder(contologyID, name, description, color, directory);

                        System.out.println("loaded ontology:" + ontology);
                        CoolMapMaster.addNewCOntology(ontology);
                    }

//                    for(COntology ontology : CoolMapMaster.getLoadedCOntologies()){
//                        COntologyUtils.printOntology(ontology);
//                        if(ontology.getName().equals("SCO")){
//                            System.out.println("difference:" + ontology.getHeightDifference("CG1", "C3"));
//                        }
//                    }
                    //restore coolmap objects
                    String coolmapObjectDirectory = projectDirectory.getAbsolutePath() + File.separator + IOTerm.OBJECT_COOLMAPOBJECT_ID;
                    JSONObject coolmapObjects = projectInfo.getJSONObject(IOTerm.OBJECT_COOLMAPOBJECT_ID);
                    iter = coolmapObjects.keys();
                    while (iter.hasNext()) {
                        String id = iter.next();
                        JSONObject entry = coolmapObjects.getJSONObject(id);

                        String name = entry.optString(IOTerm.ATTR_NAME);
                        float zoomX, zoomY;
                        try {
                            JSONArray zoom = entry.optJSONArray(IOTerm.ATTR_ZOOM);
                            zoomX = (float) zoom.getDouble(0);
                            zoomY = (float) zoom.getDouble(1);
                        } catch (Exception e) {
                            zoomX = 10f;
                            zoomY = 10f;
                        }

                        int mX, mY;
                        try {
                            JSONArray anchor = entry.optJSONArray(IOTerm.FIELD_COOLMAPVIEW_MAPANCHOR);
                            mX = anchor.getInt(0);
                            mY = anchor.getInt(1);
                        } catch (Exception e) {
                            mX = 150;
                            mY = 150;
                        }

                        CoolMapObject object = new CoolMapObject(id);
                        object.setName(name);

                        //set zoom only valid when the views are loaded
                        JSONArray cmatrixIDs = entry.optJSONArray(IOTerm.FIELD_COOLMAPOBJECT_LINKEDCMATRICES);
                        ArrayList<CMatrix> matrices = new ArrayList<CMatrix>();
                        for (int i = 0; i < cmatrixIDs.length(); i++) {
                            String cmatrixID = cmatrixIDs.getString(i);
                            CMatrix matrix = CoolMapMaster.getCMatrixByID(cmatrixID);
                            if (matrix != null) {
                                matrices.add(matrix);
                            }
                        }

                        System.out.println("Linked matrices:" + matrices.size());
                        System.out.println("Loaded base matrices:" + object.getBaseCMatrices());

                        //object.setBaseCMatrix(matrices);
                        for (CMatrix mx : matrices) {
                            object.addBaseCMatrix(mx);
                        }

                        System.out.println("Loaded base matrices:" + object.getBaseCMatrices());

                        System.out.println("First base matrix value:" + matrices.get(0).getValue(0, 0));

//                        if (object.getAggregator() != null) {
//                            jobject.put(IOTerm.FIELD_COOLMAPOBJECT_AGGREGATOR, object.getAggregator().getClass());
//                        }
//                        if (object.getAnnotationRenderer() != null) {
//                            jobject.put(IOTerm.FIELD_COOLMAPOBJECT_ANNOTATIONRENDERER, object.getAnnotationRenderer().getClass());
//                        }
//                        if (object.getViewRenderer() != null) {
//                            jobject.put(IOTerm.FIELD_COOLMAPOBJECT_VIEWRENDERER, object.getViewRenderer().getClass());
//                        }
                        //Then load snapshots
                        System.out.println("Loaded base matrices:" + object.getBaseCMatrices());

                        //System.out.println(object.getBaseCMatrices());
                        File entryFolder = new File(coolmapObjectDirectory + File.separator + id);
                        PrivateCoolMapObjectIO io = new PrivateCoolMapObjectIO();

//                        StateSnapshot rowSnapshot = io.getSnapshot(entryFolder, COntology.ROW);
//                        StateSnapshot columnSnapshot = io.getSnapshot(entryFolder, COntology.COLUMN);
//                        System.out.println(rowSnapshot.getBaseNodes());
//                        System.out.println(rowSnapshot.getTreeNodes());
//                        System.out.println(columnSnapshot.getBaseNodes());
//                        System.out.println(columnSnapshot.getTreeNodes());
//                        object.restoreSnapshot(rowSnapshot, false);
//                        object.restoreSnapshot(columnSnapshot, false);
                        object.getCoolMapView().setZoomLevels(zoomX, zoomY);
                        object.getCoolMapView().moveMapTo(mX, mY);
//                        
                        String aggrClassString = entry.optString(IOTerm.FIELD_COOLMAPOBJECT_AGGREGATOR);
//                        System.out.println("AggrString" + aggrClassString);
//                        try{
//                            Class.forName("coolmap.data.aggregator.impl.DoubleDoubleMax");
//                            Class.forName(aggrClassString);
//                        }
//                        catch(Exception e){
//                            e.printStackTrace();
//                        }

                        Class aggrClass;
                        try {
                            aggrClass = Class.forName(aggrClassString);
                            CAggregator aggregator = (CAggregator) aggrClass.newInstance();
                            object.setAggregator(aggregator);
                        } catch (Exception e) {
                            object.setAggregator(new PassThrough());
                            e.printStackTrace();
                        }

                        //object.setAggregator(new DoubleDoubleMax(object));
                        String viewRendererString = entry.optString(IOTerm.FIELD_COOLMAPOBJECT_VIEWRENDERER);
                        Class viewRendererClass;
                        try {
                            viewRendererClass = Class.forName(viewRendererString);
                            ViewRenderer viewRenderer = (ViewRenderer) viewRendererClass.newInstance(); //modify this later
//                            object.setViewRenderer(viewRenderer, true);
                        } catch (Exception e) {
                            //Assign a default String renderer:
                            e.printStackTrace();
                        }

                        //load the annotator
                        String annotatorString = entry.optString(IOTerm.FIELD_COOLMAPOBJECT_ANNOTATIONRENDERER);

                        //load the snippet
                        String snippetString = entry.optString(IOTerm.FIELD_COOLMAPOBJECT_SNIPPETCONVERTER);
                        Class snippetConverter;
                        try {
                            snippetConverter = Class.forName(snippetString);
                            SnippetConverter converter = (SnippetConverter) snippetConverter.newInstance();
                            object.setSnippetConverter(converter);
                        } catch (Exception e) {
                            // do nothing
                        }

                        System.out.println(object.getName());
                        System.out.println(object.getViewRenderer());
                        System.out.println(object.getAggregator());
                        System.out.println(object.getSnippetConverter());
                        System.out.println("==========================");

                        CoolMapMaster.addNewCoolMapObject(object);

                        for (int i = 0; i < 5; i++) {
                            System.out.println(object.getViewValue(0, i));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        menuItem = new MenuItem("view to TSV file");
//        CoolMapMaster.getCMainFrame().addMenuItem("File/Export", menuItem, false, false);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = Tools.getCustomMultiFileChooser(new FileNameExtensionFilter(".tsv", "tsv"));
                int returnVal = chooser.showSaveDialog(CoolMapMaster.getCMainFrame());

                if (returnVal != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File f = chooser.getSelectedFile();
                f = Tools.appendFileExtension(f, "tsv");

                try {
                    CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                    if (object == null) {
                        return;
                    }

                    BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                    //writer.write("Writing to file");
                    writer.write("Row/Column");
                    for (int i = 0; i < object.getViewNumColumns(); i++) {
                        writer.write("\t" + object.getViewNodeColumn(i).getName());
                    }
                    writer.write("\n");
                    for (int i = 0; i < object.getViewNumRows(); i++) {
                        writer.write(object.getViewNodeRow(i).getName() + "\t");
                        for (int j = 0; j < object.getViewNumColumns(); j++) {
                            writer.write("\t" + object.getViewValue(i, j));
                        }
                        writer.write("\n");
                    }

                    writer.flush();
                    writer.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        menuItem = new MenuItem("view to Excel file");
        CoolMapMaster.getCMainFrame().addMenuItem("File/Export", menuItem, false, false);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = Tools.getCustomMultiFileChooser(new FileNameExtensionFilter(".tsv", "tsv"));
                int returnVal = chooser.showSaveDialog(CoolMapMaster.getCMainFrame());

                if (returnVal != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File f = chooser.getSelectedFile();
                f = Tools.appendFileExtension(f, "xls");

                try {
                    //BufferedWriter propertyWriter = new BufferedWriter(new FileWriter(f));
                    //writer.write("Writing to file");
                    CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                    if (object == null) {
                        return;
                    }

                    HSSFWorkbook workbook = new HSSFWorkbook();
                    HSSFSheet sheet = workbook.createSheet("Sample sheet");

                    Row header = sheet.createRow(0);
                    Cell cell = header.createCell(0);
                    cell.setCellValue("Row/Column");

                    for (int i = 0; i < object.getViewNumColumns(); i++) {
                        cell = header.createCell(i + 1);
                        cell.setCellValue(object.getViewNodeColumn(i).getName());

                    }

                    for (int j = 0; j < object.getViewNumRows(); j++) {
                        Row row = sheet.createRow(j + 1);
                        cell = row.createCell(0);
                        cell.setCellValue(object.getViewNodeRow(j).getName());
                        for (int i = 0; i < object.getViewNumColumns(); i++) {
                            cell = row.createCell(i + 1);
                            cell.setCellValue(object.getViewValue(j, i).toString());
                        }
                    }

                    //writer.flush();
                    //writer.close();
                    FileOutputStream out = new FileOutputStream(f);
                    workbook.write(out);
                    out.close();

                    //have to check - may need to save as string
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

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

                CoolMapMaster.newSession("");
            }
        });

    }

    private static void initializeSave() {
        TConfig.get().setArchiveDetector(new TArchiveDetector("cpj", new JarDriver(IOPoolLocator.SINGLETON)));

        MenuItem item = new MenuItem("Save");
        CoolMapMaster.getCMainFrame().addMenuItem("File", item, true, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        item = new MenuItem("Save as...", new MenuShortcut(KeyEvent.VK_S));
        CoolMapMaster.getCMainFrame().addMenuItem("File", item, false, true);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                //dev
//                JFileChooser chooser = Tools.getCustomSingleFileChooser(new FileNameExtensionFilter("CoolMap project file", "cpj"));
//                int returnValue = chooser.showSaveDialog(CoolMapMaster.getCMainFrame());
//                if (returnValue != JFileChooser.APPROVE_OPTION) {
//                    return;
//                }
//
//                File saveFile = chooser.getSelectedFile();
//
//                String path = saveFile.getAbsolutePath();
//                path = Tools.appendPathExtension(path, "cpj");
//
//                TFile file;
//                if (saveFile.exists()) {
//                    try {
//                        saveFile.delete();
//                    } catch (Exception ex) {
//                        CMConsole.logError(saveFile + " can not be deleted. Try to save as an alternative file instead.");
//                        path = Tools.removeFileExtension("cpj");
//
//                        DateFormat dateFormat = new SimpleDateFormat(" yyyy-MM-dd HH-mm-ss ");
//                        Date date = new Date();
//                       
//
//                        path +=  dateFormat.format(date);
//                        
//                        Tools.appendPathExtension(path, "cpj");
//                    }
//                }
//
//                //create a new file
//                file = new TFile(path);
                //expedite development
                TFile file = new TFile("/Users/sugang/000.cpj");

                saveProject(file);
            }
        });

    }

    private static void saveProject(TFile projectFile) {
        
        //wrap this up in a long task to run in the background
        try {

            saveProjectProperties(projectFile);
            saveCOntologies(projectFile);

            //this is needed to commit all changes in the temp projectFile and finish the saving
            TVFS.umount(projectFile);
        } catch (Exception e) {
            e.printStackTrace(); // need to abort and delete the file
        }

        //try load project immediately
        loadProject(projectFile);
    }

    private static void saveCOntologies(TFile projectFile) throws Exception {
        List<COntology> ontologies = CoolMapMaster.getLoadedCOntologies();
        for (COntology ontology : ontologies) {
            String ontologyFolderPath = projectFile.getAbsolutePath() + File.separator + IOTerm.DIR_COntology + File.separator + ontology.getID();
            
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
        projectInfo.put(IOTerm.PROJECT_FIELD_DATE, dateFormat.format(new Date()));

        //session name
        String sessionName = CoolMapMaster.getSessionName();
        if (sessionName == null || sessionName.length() == 0) {
            sessionName = "Untitled";
        }
        projectInfo.put(IOTerm.PROJECT_FIELD_SESSION_NAME, sessionName);

        //cmatrices projectInfo object
        ArrayList<String> matrixIDs = new ArrayList<>();

        List<CMatrix> baseMatrices = CoolMapMaster.getLoadedCMatrices();
        for (CMatrix matrix : baseMatrices) {
//            JSONObject cmatrixPropertyEntry = new JSONObject();
//            cmatrixProperties.put(matrix.getID(), cmatrixPropertyEntry);
//
//            //add values
//            cmatrixPropertyEntry.put(IOTerm.ATTR_ID, matrix.getID());
//            move these to corresponding folders
//            cmatrixPropertyEntry.put(IOTerm.ATTR_NAME, matrix.getName());
//            cmatrixPropertyEntry.put(IOTerm.ATTR_NUMROW, matrix.getNumRows());
//            cmatrixPropertyEntry.put(IOTerm.ATTR_NUMCOLUMN, matrix.getNumColumns());
//            cmatrixPropertyEntry.put(IOTerm.ATTR_CLASS, matrix.getClass().getName()); //The class of CMatrix, i.e. DoubleCMatrix
//            cmatrixPropertyEntry.put(IOTerm.ATTR_MEMBERCLASS, matrix.getMemberClass()); //The class of the CMatrix members, i.e. Double
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
//            coolMapObjectPropertyEntry.put(IOTerm.ATTR_ZOOM, new float[]{view.getZoomX(), view.getZoomY()});
//
//            Point mapAnchor = object.getCoolMapView().getMapAnchor();
//            coolMapObjectPropertyEntry.put(IOTerm.FIELD_COOLMAPVIEW_MAPANCHOR, new int[]{mapAnchor.x, mapAnchor.y});
//
//            ArrayList<String> linkedMxIDs = new ArrayList<String>();
//            List<CMatrix> linkedMxs = object.getBaseCMatrices();
//            for (CMatrix mx : linkedMxs) {
//                linkedMxIDs.add(mx.getID());
//            }

            //other info should be read from the corresponding folders
            //as the state of these things need to be persisted as well.
//            coolMapObjectPropertyEntry.put(IOTerm.FIELD_COOLMAPOBJECT_LINKEDCMATRICES, linkedMxIDs); //can restore the cMatrix by using such matrices, when they are reloaded
            coolMapIDs.add(object.getID());
        }
        projectInfo.put(IOTerm.OBJECT_COOLMAPOBJECT_ID, coolMapIDs);

        propertyWriter.write(projectInfo.toString());
        propertyWriter.flush();
        propertyWriter.close();

    }

    private static void loadProject(TFile file) {
        try {

            System.out.println(Arrays.toString(file.listFiles()));
            File propertyFile = new TFile(file.getAbsolutePath() + File.separator + IOTerm.FILE_PROJECT_INFO);
//            System.out.println(propertyFile);
            StringBuilder contents = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new TFileReader(propertyFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                contents.append(line);
            }
            bufferedReader.close();

            JSONObject property = new JSONObject(contents.toString().trim());

            System.out.println(property.toString(2));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initializeLoad() {
        MenuItem item = new MenuItem("Open Project", new MenuShortcut(KeyEvent.VK_O));
        CoolMapMaster.getCMainFrame().addMenuItem("File", item, false, false);

    }

    public static void initialize() {

        initializeCreateNew();
        initializeLoad();
        initializeSave();
        initializeDataImporters();
        initializeOntologyImporters();

//        _initActions();
//        System.out.println("Initialize IO Master");
        MenuItem item = new MenuItem("Save Project", new MenuShortcut(KeyEvent.VK_S));
//        CoolMapMaster.getCMainFrame().addMenuItem("File", item, true, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                //save to default location.
                //test code here.
                JFileChooser fileChooser = Tools.getFolderChooser();
                int returnValue = fileChooser.showSaveDialog(CoolMapMaster.getCMainFrame());
                if (returnValue != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File rootDirectory = fileChooser.getSelectedFile();

                //File rootDirectory = new File("/Users/gangsu/000");
                try {
                    FileUtils.deleteDirectory(rootDirectory);
                } catch (Exception e) {
                    //e.printStackTrace();
                    System.out.println("directory does not exist");
                }

                if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
                    rootDirectory.mkdir();
                }

                if (!rootDirectory.exists()) {
                    //Exception: File save failed.

                    return;
                } else {
                    try {

//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
                        //String
                        String exporter = "coolmap.application.io.internal.cmatrix.PrivateDoubleCMatrixIO";

                        //Write project files
                        JSONObject projectInfo = new JSONObject();

                        //obtain cmatrices
                        JSONObject cmatrices = new JSONObject();
                        projectInfo.put(IOTerm.OBJECT_CMATRIX_ID, cmatrices);
                        List<CMatrix> baseMatrices = CoolMapMaster.getLoadedCMatrices();
                        for (CMatrix matrix : baseMatrices) {
                            JSONObject cmatrix = new JSONObject();
                            cmatrices.put(matrix.getID(), cmatrix);

                            cmatrix.put(IOTerm.ATTR_ID, matrix.getID());
                            cmatrix.put(IOTerm.ATTR_NAME, matrix.getName());
                            cmatrix.put(IOTerm.ATTR_NUMROW, matrix.getNumRows());
                            cmatrix.put(IOTerm.ATTR_NUMCOLUMN, matrix.getNumColumns());
                            cmatrix.put(IOTerm.ATTR_MEMBERCLASS, matrix.getMemberClass());
                            cmatrix.put(IOTerm.ATTR_CLASS, matrix.getClass().getName());

                            if (exporter != null) {
                                cmatrix.put(IOTerm.FIELD_CMATRIX_ICMATRIXIO, exporter);
                            }
                        }

                        //obtain contologies
                        JSONObject jcontologies = new JSONObject();
                        projectInfo.put(IOTerm.OBJECT_CONTOLOGY_ID, jcontologies);
                        List<COntology> ontologies = CoolMapMaster.getLoadedCOntologies();
                        for (COntology ontology : ontologies) {
                            JSONObject jontology = new JSONObject();
                            jcontologies.put(ontology.getID(), jontology);

                            jontology.put(IOTerm.ATTR_ID, ontology.getID());
                            jontology.put(IOTerm.ATTR_NAME, ontology.getName());
                            if (ontology.getDescription() != null) {
                                jontology.put(IOTerm.ATTR_DESCRIPTION, ontology.getDescription());
                            }

                            if (ontology.getViewColor() != null) {
                                Color c = ontology.getViewColor();
                                jontology.put(IOTerm.ATTR_COLOR, c.getRGB());
                            }

//                            if(ontology.getEdgetAttributeClass() != null){
//                                contologyPropertyEntry.put(IOTerm.FIELD_CONTOLOGY_EDGETATTRIBUTECLASS, ontology.getEdgetAttributeClass().getName());
//                            }
                        }

                        //obtain coolmap objects
                        //under each object
                        JSONObject jcoolMapObjects = new JSONObject();
                        projectInfo.put(IOTerm.OBJECT_COOLMAPOBJECT_ID, jcoolMapObjects);
                        List<CoolMapObject> objects = CoolMapMaster.getCoolMapObjects();
                        for (CoolMapObject object : objects) {
                            JSONObject jobject = new JSONObject();
                            jcoolMapObjects.put(object.getID(), jobject);

                            jobject.put(IOTerm.ATTR_ID, object.getID());
                            jobject.put(IOTerm.ATTR_NAME, object.getName());

                            CoolMapView view = object.getCoolMapView();

                            jobject.put(IOTerm.ATTR_ZOOM, new float[]{view.getZoomX(), view.getZoomY()});

                            Point mapAnchor = object.getCoolMapView().getMapAnchor();
                            jobject.put(IOTerm.FIELD_COOLMAPVIEW_MAPANCHOR, new int[]{mapAnchor.x, mapAnchor.y});

                            ArrayList<String> linkedMxIDs = new ArrayList<String>();
                            List<CMatrix> linkedMxs = object.getBaseCMatrices();
                            for (CMatrix mx : linkedMxs) {
                                linkedMxIDs.add(mx.getID());
                            }

                            jobject.put(IOTerm.FIELD_COOLMAPOBJECT_LINKEDCMATRICES, linkedMxIDs);

                            if (object.getAggregator() != null) {
                                jobject.put(IOTerm.FIELD_COOLMAPOBJECT_AGGREGATOR, object.getAggregator().getClass().getName());
                            }

//                            if (object.getAnnotationRenderer() != null) {
//                                jobject.put(IOTerm.FIELD_COOLMAPOBJECT_ANNOTATIONRENDERER, object.getAnnotationRenderer().getClass().getName());
//                            }
                            if (object.getViewRenderer() != null) {
                                jobject.put(IOTerm.FIELD_COOLMAPOBJECT_VIEWRENDERER, object.getViewRenderer().getClass().getName());
                            }
                            if (object.getSnippetConverter() != null) {
                                jobject.put(IOTerm.FIELD_COOLMAPOBJECT_SNIPPETCONVERTER, object.getSnippetConverter().getClass().getName());
                            }

                        }

                        //write to 
                        System.out.println(projectInfo.toString(2));

                        //////////
                        //Write damn data to file
                        File file = new File(rootDirectory.getAbsolutePath() + File.separator + IOTerm.FILE_PROJECT_INFO);

                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

                        projectInfo.write(writer);

                        writer.flush();
                        ///////////////////

                        //write base matrices
                        //using correct exporter: later
                        //
                        File matrixFolder = new File(rootDirectory.getAbsolutePath() + File.separator + IOTerm.DIR_CMATRIX);
                        matrixFolder.mkdir();

                        baseMatrices = CoolMapMaster.getLoadedCMatrices();
                        for (CMatrix matrix : baseMatrices) {
                            String ID = matrix.getID();
                            File entryFolder = new File(matrixFolder.getAbsolutePath() + File.separator + ID);
                            entryFolder.mkdir();

                            ICMatrixIO io = (ICMatrixIO) (Class.forName(exporter).newInstance());

                            io.exportToDirectory(matrix, entryFolder);
                        }

                        //write COntology
                        //
                        File contologyFolder = new File(rootDirectory.getAbsolutePath() + File.separator + IOTerm.DIR_COntology);
                        contologyFolder.mkdir();
                        ontologies = CoolMapMaster.getLoadedCOntologies();
                        for (COntology ontology : ontologies) {
                            String ID = ontology.getID();
                            File entryFolder = new File(contologyFolder.getAbsolutePath() + File.separator + ID);
                            entryFolder.mkdir();

                            PrivateCOntologyStructureFileIO io = new PrivateCOntologyStructureFileIO();
                            io.writeToFolder(ontology, entryFolder);

                        }

                        //write coolmapobjects
                        File coolmapfolder = new File(rootDirectory.getAbsolutePath() + File.separator + IOTerm.DIR_CoolMapObject);
                        coolmapfolder.mkdir();
                        objects = CoolMapMaster.getCoolMapObjects();
                        PrivateCoolMapObjectIO objIO = new PrivateCoolMapObjectIO();
                        for (CoolMapObject object : objects) {
                            String ID = object.getID();
                            File entryFolder = new File(coolmapfolder.getAbsolutePath() + File.separator + object.getID());
                            entryFolder.mkdir();
                            objIO.writeRowColumnSnapshots(object, entryFolder);

                        }

                        //dump widgets and other info
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        });
    }

}
