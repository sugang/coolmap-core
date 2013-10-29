package coolmap;

import coolmap.application.CoolMapMaster;
import coolmap.application.io.external.ImportCOntologyFromFile;
import coolmap.application.io.external.ImportDoubleCMatrixFromFile;
import coolmap.canvas.datarenderer.renderer.impl.DoubleToColor;
import coolmap.canvas.datarenderer.renderer.impl.NetworkToForceLayout;
import coolmap.canvas.sidemaps.impl.ColumnLabels;
import coolmap.canvas.sidemaps.impl.ColumnTree;
import coolmap.canvas.sidemaps.impl.RowLabels;
import coolmap.canvas.sidemaps.impl.RowTree;
import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.impl.DoubleDoubleMean;
import coolmap.data.aggregator.impl.DoubleToNetwork;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrix.model.NetworkCMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.snippet.SnippetMaster;
import coolmap.utils.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.json.JSONObject;
import rcaller.RCaller;
import rcaller.RCode;


/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author gangsu
 */
public class Main {

    public static void main(String args[]) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                //First initialize
                CoolMapMaster.initialize();
                //CoolMapMaster.getCMainFrame().saveWorkspace(Config.getProperty(Config.WORKSPACE_DIRECTORY));
                //CoolMapMaster.getCMainFrame().loadWorkspace(Config.getProperty(Config.WORKSPACE_DIRECTORY) + "/default.dck");

//                CoolMapMaster.getCMainFrame().addMenuItem(null, null, true);
//            FloatExternalizer 
//                MenuItem saveWorkSpace = new MenuItem("Save Workspace");
//                CoolMapMaster.getCMainFrame().addMenuItem("View", saveWorkSpace, true);
//                saveWorkSpace.addActionListener(new ActionListener() {
//
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        CoolMapMaster.getCMainFrame().saveWorkspace(Config.getProperty(Config.WORKSPACE_DIRECTORY));
//                    }
//                });
                //then restore workspace
                CoolMapMaster.getCMainFrame().loadWorkspace(Config.getProperty(Config.WORKSPACE_DIRECTORY) + "/default.dck");

                loadSampleCoolMapProject();
                
                
//                System.err.println(CoolMapMaster.getCMainFrame().findMenu("Edit"));
            }
        });

//        System.out.println(Config.getProperty(Config.WORKSPACE_DIRECTORY));
//        dockmodel can not be saved?
//        CoolMapMaster.getCMainFrame().saveWorkspace(Config.getProperty(Config.WORKSPACE_DIRECTORY));
    }

    private static void loadSampleCoolMapProject() {
        try {
            CoolMapObject object;
            COntology onto;
            //CMatrix matrix = ImportDoubleCMatrixFromFile.importFromFile(new File("/Users/sugang/Dropbox/Research - Dropbox/CoolMap datasets/0TestData.txt"));
            //CoolMapObject object = importer.importFromFile(new File("/Users/gangsu/0correlation.txt"));
            //import sample
            //CMatrix matrix = ImportDoubleCMatrixFromFile.importFromFile(new File("/Users/gangsu/Dropbox/Research - Dropbox/TBC 2013/eisenFinal.txt"));
            
            CMatrix matrix = ImportDoubleCMatrixFromFile.importFromFile(new File("/Users/sugang/0correlation.txt"));
            System.out.println(matrix + " " + matrix.getNumRows() + " " + matrix.getNumColumns() + " " + matrix.getValue(0, 0));

            object = new CoolMapObject();
            object.addBaseCMatrix(matrix);

//            Add base nodes ===================================================
            ArrayList<VNode> nodes = new ArrayList<VNode>();
            for (Object label : matrix.getRowLabelsAsList()) {
                nodes.add(new VNode(label.toString()));
            }
            object.insertRowNodes(nodes);
            
            nodes.clear();
            for (Object label : matrix.getColLabelsAsList()) {
                nodes.add(new VNode(label.toString()));
            }
            object.insertColumnNodes(nodes);
            
            //need ontology nodes
////////////////////////////////////////////////////////////////////////////////
//            onto = ImportCOntologyFromFile.importFromFile(new File("/Users/sugang/Dropbox/Research - Dropbox/CoolMap datasets/0TestOntology.txt"));
//            CoolMapMaster.addNewCOntology(onto);
//
//            ArrayList<VNode> nodes = new ArrayList<VNode>();
//            nodes.add(new VNode("RG0", onto));
//            nodes.add(new VNode("RG1", onto));
//            nodes.add(new VNode("RG2", onto));
//            nodes.add(new VNode("RGG0", onto));
//            object.insertRowNodes(nodes);
//
//            nodes.clear();
//            nodes.add(new VNode("CG0", onto));
//            nodes.add(new VNode("CG1", onto));
//            nodes.add(new VNode("CG2", onto));
//            nodes.add(new VNode("CG3", onto));
//            nodes.add(new VNode("CG4", onto));
//            nodes.add(new VNode("CG5", onto));
//            nodes.add(new VNode("CGG0", onto));
//            nodes.add(new VNode("CGG1", onto));
//
//            object.insertColumnNodes(nodes);
            

            object.setAggregator(new DoubleDoubleMean());
            object.setViewRenderer(new DoubleToColor());

            object.setSnippetConverter(SnippetMaster.getConverter("D13"));
            object.getCoolMapView().addRowMap(new RowLabels(object));
            object.getCoolMapView().addRowMap(new RowTree(object));
            object.getCoolMapView().addColumnMap(new ColumnLabels(object));
            object.getCoolMapView().addColumnMap(new ColumnTree(object));
            CoolMapMaster.addNewBaseMatrix(matrix);
            CoolMapMaster.addNewCoolMapObject(object);

            CoolMapMaster.setActiveCoolMapObject(object);

            //No need for point nanotation for now
            //try to add some annotations
//            PointAnnotation annotation = new PointAnnotation(object.getViewNodeRow(0), object.getViewNodeColumn(object.getViewNumColumns()-1), "This is annotation test\nWith two lines\nMore lines!\nMore more lines!");
//            object.getAnnotationStorage().addAnnotation(annotation);
//            CoolMapMaster.getCMainFrame().showBusyDialog(true);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main3(String args[]) {
        CoolMapMaster.initialize();
        //Use this mechanism to determine which widgets should be loaded.
        if (true) {

            try {

//
//////            CoolMapObject object = CoolMapObject.createSampleCoolMapObject(500, 200);
//////            object.setName("Obj1");
//////            //object.setViewRenderer(new DoubleToColor(object));
//////            object.setViewRenderer(new DoubleToColor());
//////            CoolMapMaster.addNewCoolMapObject(object);
                //FileTSVDoubleImporter importer = new FileTSVDoubleImporter();
                //CoolMapObject object = importer.importFromFile(new File("/Users/gangsu/0correlation.txt"));
                //CMatrix matrix = ImportDoubleCMatrixFromFile.importFromFile(new File("/Users/gangsu/Dropbox/T1D/DataTables/229RTCGM.txt"));
                CoolMapObject object;
                COntology onto;

                if (true) {
                    CMatrix matrix = ImportDoubleCMatrixFromFile.importFromFile(new File("/Users/sugang/Dropbox/Research - Dropbox/CoolMap datasets/0correlation.txt"));

                    //import sample
                    //CMatrix matrix = ImportDoubleCMatrixFromFile.importFromFile(new File("/Users/gangsu/Dropbox/Research - Dropbox/TBC 2013/eisenFinal.txt"));
                    System.out.println(matrix + " " + matrix.getNumRows() + " " + matrix.getNumColumns() + " " + matrix.getValue(0, 0));

                    object = new CoolMapObject();
                    object.addBaseCMatrix(matrix);

                    ArrayList<VNode> nodes = new ArrayList<VNode>();
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
                    object.setViewRenderer(new DoubleToColor());

                    object.setSnippetConverter(SnippetMaster.getConverter("D13"));
                    object.getCoolMapView().addRowMap(new RowLabels(object));
                    object.getCoolMapView().addRowMap(new RowTree(object));
                    object.getCoolMapView().addColumnMap(new ColumnLabels(object));
                    object.getCoolMapView().addColumnMap(new ColumnTree(object));
                    CoolMapMaster.addNewBaseMatrix(matrix);
                    CoolMapMaster.addNewCoolMapObject(object);
                    onto = ImportCOntologyFromFile.importFromFile(new File("/Users/sugang/Dropbox/Research - Dropbox/CoolMap datasets/0Child_Parent.txt"));
                    CoolMapMaster.addNewCOntology(onto);
                }

//                mx.printMatrix();
//                create a network 
                if (false) {
                    NetworkCMatrix mx = new NetworkCMatrix(100, 100);
                    for (int i = 0; i < mx.getNumRows(); i++) {
                        for (int j = 0; j < mx.getNumColumns(); j++) {
                            if (Math.random() > 0.6) {
                                mx.setValue(i, j, 1.0);
                            }
                        }
                    }

                    onto = new COntology("Network", null);

                    //Then let's do something.
                    int numRows = mx.getNumRows();
                    int numColumns = mx.getNumColumns();

                    int rowSection = numRows / 5;
                    int colSection = numColumns / 5;

                    for (int i = 0; i < 5; i++) {
                        for (int j = i * rowSection; j < (i + 1) * rowSection; j++) {
                            onto.addRelationshipNoUpdateDepth("GRP" + i, mx.getRowLabel(j));
                        }
                    }

                    //COntologyUtils.printOntology(onto);
                    object = new CoolMapObject();
                    object.addBaseCMatrix(mx);
                    object.insertRowNodes(onto.getRootNodesOrdered());
                    object.insertColumnNodes(onto.getRootNodesOrdered());

                    object.setAggregator(new DoubleToNetwork());
                    object.setViewRenderer(new NetworkToForceLayout());
                    object.getCoolMapView().addRowMap(new RowLabels(object));
                    object.getCoolMapView().addRowMap(new RowTree(object));
                    object.getCoolMapView().addColumnMap(new ColumnLabels(object));
                    object.getCoolMapView().addColumnMap(new ColumnTree(object));

                    CoolMapMaster.addNewBaseMatrix(mx);
                    CoolMapMaster.addNewCoolMapObject(object);
                    CoolMapMaster.addNewCOntology(onto);
                }
//                ArrayList<Integer> rowIndices = new ArrayList<Integer>();
//                ArrayList<Integer> colIndices = new ArrayList<Integer>();

//            
//            object.clearStateStorage();
                //System.out.println(object + object.getAggregator().toString() + object.getViewRenderer().toString());
                //CoolMapMaster.addNewCoolMapObject(object);
//            object = CoolMapObject.createSampleCoolMapObject(500, 100);
//            object.setName("Obj2");
//            object.setViewRenderer(new DoubleToColor());
//            CoolMapMaster.addNewCoolMapObject(object);
//            
//            object = CoolMapObject.createSampleCoolMapObject(500, 100);
//            object.setName("Obj3");
//            object.setViewRenderer(new DoubleToHeight(object));
//            CoolMapMaster.addNewCoolMapObject(object);
//            object = CoolMapObject.createSampleCoolMapObject(20, 100);
//            object.setName("Obj2");
//            CoolMapMaster.addNewCoolMapObject(object);
//            object = CoolMapObject.createSampleCoolMapObject(300, 100);
//            object.setName("Obj3");
//            CoolMapMaster.addNewCoolMapObject(object);
//            object = CoolMapObject.createSampleCoolMapObject(200, 100);
//            object.setName("Obj2");
//            CoolMapMaster.addNewCoolMapObject(object);
//
//            object = CoolMapObject.createSampleCoolMapObject(200, 200);
//            object.setName("Obj3");
//            CoolMapMaster.addNewCoolMapObject(object);
//            object = CoolMapObject.createSampleCoolMapObject(2000, 2000);
//            object.setName("Obj2");
//            CoolMapMaster.addNewCoolMapObject(object);
//
//            
//            object = CoolMapObject.createSampleCoolMapObject(1000, 1000);
//            object.setName("Obj3");
//            CoolMapMaster.addNewCoolMapObject(object);
//            object = CoolMapObject.createSampleCoolMapObject(100, 40);
//            object.setName("Obj2");
//            CoolMapMaster.addNewCoolMapObject(object);
//            CoolMapObject object1 = CoolMapObject.createSampleCoolMapObject(40, 30);
////            object1.setName("Obj2");
//            CoolMapMaster.addNewCoolMapObject(object1);
//
//
//            CoolMapObject object2 = CoolMapObject.createSampleCoolMapObject(40, 30);
////            object1.setName("Obj2");
//            CoolMapMaster.addNewCoolMapObject(object2);
//            CoolMapObject object2 = CoolMapObject.createSampleCoolMapObject(40, 50);
////            object2.setName("Obj3");
//            CoolMapMaster.addNewCoolMapObject(object2);
//            object = CoolMapObject.createSampleCoolMapObject(20, 30);
//            CoolMapMaster.addNewCoolMapObject(object);
//
//            object = CoolMapObject.createSampleCoolMapObject(20, 30);
//            CoolMapMaster.addNewCoolMapObject(object);
//
//            object = CoolMapObject.createSampleCoolMapObject(20, 30);
//            CoolMapMaster.addNewCoolMapObject(object);
//            Test R    
                if (true) {
                    //test RCaller
                    try {

                        //need to get the json file
//                        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//                        String decodedPath = path;
                        //The working path can't be set to the dist path
//                        System.out.println("\nWorking path:" + path);
//                        System.out.println("\nUser Directory:" + System.getProperty("user.dir"));
//                        System.out.println("");
//
//                        try {
//                            decodedPath = URLDecoder.decode(path, "UTF-8");
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
                        //String absolutePath = decodedPath.substring(0, decodedPath.lastIndexOf("/")) + "\\";
                        RCaller caller = new RCaller();

                        //not quite right
//                        System.out.println("Running path:" + decodedPath);
//                        Try to load R
                        String coolMapPath = System.getProperty("user.dir");

                        try {
                            File file = new File(coolMapPath + File.separator + "config.json");
//                            file = new File(file.getParentFile() + File.separator + "rconfig.json");
                            BufferedReader reader = new BufferedReader(new FileReader(file));
                            StringBuffer sb = new StringBuffer();
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }

                            JSONObject config = new JSONObject(sb.toString());

                            JSONObject rConfig = config.getJSONObject("R");

                            String rscriptPath = rConfig.getString("Rscript-Path");
                            System.out.println("Rscript Path:: " + rscriptPath);
                            caller.setRscriptExecutable(rscriptPath);
                        } catch (Exception e) {
                            System.out.println("--R config file not found or eading error, set to default path");
                            caller.setRscriptExecutable("/usr/bin/Rscript");
                            e.printStackTrace();
                        }

                        //Inspect properties.
                        //
//                        Properties properties = System.getProperties();
//                        for( Map.Entry<Object, Object> entry : properties.entrySet()){
//                            System.out.println(entry.getKey() + "==>" + entry.getValue());
//                        }
                        RCode code = new RCode();
                        caller.setRCode(code);
                        caller.runOnly(); //see whether it works

                        //JOptionPane.showMessageDialog(null, "R engine initialization successful!");
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "R engine initialization failed.\nPlease config the R path in rconfig.json");
                        e.printStackTrace();
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    //obsolete code with mostly testing 
//    public static void main2(String[] args) {
//        //initializers
//        UI.initialize();
//        SnippetMaster.initialize();
//
//
//        SnippetConverter d13 = SnippetMaster.getConverter("D13");
//
//
//        JFrame frame = new JFrame();
//
//        final CoolMapObject object = CoolMapObject.createSampleCoolMapObject(100, 100);
//        //final Canvas canvas = new Canvas(object);
//        object.setSnippetConverter(d13);
//
//
//
//
//        JPanel panel = new JPanel();
//        panel.setLayout(new BorderLayout());
//
//
//
//        frame.setContentPane(panel);
//        //frame.setContentPane(new GLG2DCanvas(panel));
//
//
//
//        panel.add(object.getCoolMapView().getViewCanvas(), BorderLayout.CENTER);
//
//        JToolBar toolBar = new JToolBar();
//        toolBar.setFloatable(false);
//        panel.add(toolBar, BorderLayout.NORTH);
//        JButton btn = new JButton("Fade in");
//        btn.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                object.getCoolMapView().toggleGridMode(true);
//            }
//        });
//        toolBar.add(btn);
//
//        btn = new JButton("Fade out");
//        btn.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                object.getCoolMapView().toggleGridMode(false);
//            }
//        });
//        toolBar.add(btn);
//
//        btn = new JButton("Zoom In");
//        btn.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                object.getCoolMapView().zoomIn(true, true);
//            }
//        });
//        toolBar.add(btn);
//
//        btn = new JButton("Zoom Out");
//        btn.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                object.getCoolMapView().zoomOut(true, true);
//            }
//        });
//        toolBar.add(btn);
//
//        btn = new JButton("Reset");
//        btn.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                object.getCoolMapView().resetNodeWidth();
//            }
//        });
//        toolBar.add(btn);
//
//        btn = new JButton("Center Selection");
//        btn.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                object.getCoolMapView().centerToSelections();
//            }
//        });
//        toolBar.add(btn);
//
//
////        btn = new JButton("Save state");
////        btn.addActionListener(new ActionListener() {
////
////            @Override
////            public void actionPerformed(ActionEvent ae) {
////                Main.snapshotRow = new StateSnapshot("Row", System.currentTimeMillis(), object, COntology.ROW);
////                Main.snapshotColumn = new StateSnapshot("Column", System.currentTimeMillis(), object, COntology.COLUMN);
////
////                System.out.println(Main.snapshotRow.getDisplayLabel());
////            }
////        });
////        toolBar.add(btn);
////
////        btn = new JButton("Restore state");
////        btn.addActionListener(new ActionListener() {
////
////            @Override
////            public void actionPerformed(ActionEvent ae) {
////                if (snapshotRow != null) {
////                    object._restoreSnapshot(snapshotRow);
////                    object._restoreSnapshot(snapshotColumn);
////                }
////            }
////        });
////        toolBar.add(btn);
//
//        btn = new JButton("Garbage collection");
//        btn.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                System.gc();
//            }
//        });
//        toolBar.add(btn);
//
//
//
//
//        object.getCoolMapView().addColumnMap(new ColumnLabels(object));
//        object.getCoolMapView().addColumnMap(new ColumnTree(object));
//        object.getCoolMapView().addColumnMap(new ColumnValueTest(object));
//
//        object.getCoolMapView().addRowMap(new RowLabels(object));
//        object.getCoolMapView().addRowMap(new RowTree(object));
//
//
////        object.expandColumnNode(203);
////        object.expandColumnNode(203);
//////        object.expandColumnNode(1);
////
////        object.expandColumnNode(0);
////        object.expandColumnNode(4);
////        object.expandColumnNode(4);
////        
////        object.expandRowNode(0);
////        object.expandRowNode(0);
//
//        frame.setPreferredSize(new Dimension(500, 500));
//
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//
//        frame.pack();
//
//        System.out.println(frame.getClass().getName());
//
//
//
//        frame.validate();
//
////        frame.addComponentListener(new ComponentListener() {
////
////            @Override
////            public void componentResized(ComponentEvent ce) {
////                object.getCoolMapView().updateCanvasEnforceAll();
////            }
////
////            @Override
////            public void componentMoved(ComponentEvent ce) {
////            }
////
////            @Override
////            public void componentShown(ComponentEvent ce) {
////                System.out.println("Component Shown");
////                object.getCoolMapView().updateCanvasEnforceAll();
////            }
////
////            @Override
////            public void componentHidden(ComponentEvent ce) {
////            }
////        });
//
//        frame.setVisible(true);
//        frame.validate();
//
//        System.out.println("Frame is already visible..");
//
////        HCluster.hclustRow(object, HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.EUCLIDEAN);
//
////        KMeans.kMeansClusterRow(object, 3);
//////////////////////////////////////////////////////////////////////////////////////
//
//
//
//
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
////        Memory widget
//        MemoryPoolMXBean tenuredGenPool = null;
//        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
//            if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
//                tenuredGenPool = pool;
//            }
//        }
//
//        for (int i = 0; i < 1000; i++) {
//            System.out.println("Memory usage:" + tenuredGenPool.getUsage());
//            try {
//                Thread.currentThread().sleep(500);
//            } catch (InterruptedException e) {
//                break;
//            }
//        }
//
//        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
//        NotificationEmitter emitter = (NotificationEmitter) mbean;
//
//        emitter.addNotificationListener(new NotificationListener() {
//
//            @Override
//            public void handleNotification(Notification ntfctn, Object o) {
//            }
//        }, null, btn);
//
//        tenuredGenPool.setCollectionUsageThreshold((int) Math.floor(tenuredGenPool.getUsage().getMax() * 0.8));
//
//        emitter.addNotificationListener(new NotificationListener() {
//
//            public void handleNotification(Notification n, Object hb) {
//                if (n.getType().equals(
//                        MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED)) {
//                    // this is the signal => end the application early to avoid OOME
//                    System.out.println("Insufficient memory soon.");
//                }
//            }
//        }, null, null);
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////        
//
////        object.sortColumn(1, false);
//
////        object.sortRow(1, true);
////        try{
////            Thread.currentThread().sleep(2000);
//
////        enforce redraw when add a new panel
////         object.getCoolMapView().addColumnMap(new ColumnLabels(object));
////         object.getCoolMapView().updateCanvasEnforceAll();
//
//
//
////        }
////        catch(Exception e){
////            
////        }
////        ArrayList<String> list = new ArrayList<String>();
////        list.add(null);
////        list.removeAll(list);
////        System.out.println(list.size());
//
////      need to test some ranges
////        Range<Integer> r1 = Range.closedOpen(1, 9);
////        Range<Integer> r2 = Range.closedOpen(4, 6);
////        Range<Integer> r3 = Range.closedOpen(0, 3);
////        Range<Integer> r4 = Range.closedOpen(15, 20);
////        Range<Integer> r5 = Range.closedOpen(17, 32);
////        
//////        System.out.println(r1.isConnected(r4));
////        
////        ArrayList<Range<Integer>> rlist = new ArrayList<Range<Integer>>();
////        rlist.add(r1);
////        rlist.add(r2);
////        rlist.add(r3);
////        rlist.add(r4);
////        rlist.add(r5);
////        
////        //use treeset to make it even faster
////        Collections.sort(rlist, new RangeComparator());
////        
////        for(Range r : rlist){
////            System.out.println(r);
////        }
////        
////        //Merge
////        ArrayList<Range<Integer>> merged = new ArrayList<Range<Integer>>();
////        
////        Range<Integer> rtemp = Range.closedOpen(r1.lowerEndpoint(), r1.upperEndpoint());
////        for(Range<Integer> r : rlist){
////            if(rtemp.isConnected(r)){
////                System.out.println("Connected to :" + r);
////                rtemp = rtemp.span(r);
////            }
////            else{
////                System.out.println("added?");
////                merged.add(rtemp);
////                rtemp = Range.closedOpen(r.lowerEndpoint(), r.upperEndpoint());
////            }
////        }
////        System.out.println(merged);
////        
////        merged.add(rtemp);
////        
////        //
////        System.out.println("Merged");
////        for(Range r : merged){
////            System.out.println(r);
////        }
//
//
////        object.getCoolMapView().addSelection(new Rectangle(5, 7, 9, 20));
////        object.getCoolMapView().addSelection(new Rectangle(1, 2, 10, 12));
////        object.getCoolMapView().addSelection(new Rectangle(20,30,5,5));
////        object.getCoolMapView().addSelection(new Rectangle(0,0,2,2));
//
////        Range<Integer> r1 = Range.closedOpen(10, 20);
////        Range<Integer> r2 = Range.closedOpen(10, 20);
////        
////        System.out.println(r1.encloses(r2));
////        
////        Double i = new Double(1);
////        Comparable j = (Comparable) i;
////        
//
//        //try to create a node snapshot
////        object.expandColumnNode(1);
//
//
////        StateSnapshot snapShot = new StateSnapshot(object, COntology.COLUMN);
//
//
////        System.out.println(snapShot.duplicate().getViewNodesInBase().get(1).getParentNode());
//
//
//    }
}
