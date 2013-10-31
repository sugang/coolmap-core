/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application;

import com.javadocking.DockingManager;
import com.javadocking.component.DefaultSwComponentFactory;
import com.javadocking.dock.Position;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.TabDock;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.action.DefaultDockableStateAction;
import com.javadocking.model.FloatDockModel;
import com.javadocking.model.codec.DockModelPropertiesDecoder;
import com.javadocking.model.codec.DockModelPropertiesEncoder;
import com.javadocking.visualizer.FloatExternalizer;
import com.javadocking.visualizer.SingleMaximizer;
import coolmap.application.utils.TaskDialog;
import coolmap.application.widget.Widget;
import coolmap.application.widget.impl.WidgetViewport;
import coolmap.utils.Config;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.RadialGradientPaint;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

/**
 *
 * @author gangsu
 */
public class CMainFrame extends JFrame {

    private Toolkit _defaultToolkit = Toolkit.getDefaultToolkit();
    private MenuBar _menuBar = new MenuBar();
    //private final HashMap<String, MenuItem> _menus = new HashMap<String, MenuItem>();
    //private Menu _fileMenu = new Menu("File");
    private FloatDockModel dockModel;
    private TabDock leftTabDock1;
    private TabDock leftTabDock2;
    private TabDock leftTabDock3;
    private TabDock rightTopTabDock;
    private TabDock rightBottomTabDock;
    private final SplitDock rootDock = new SplitDock();
    //private 
    private final HashMap<String, Widget> widgetHashMap = new HashMap<>();
//    private DockingMinimizer minimizer = null;
    private SingleMaximizer maximizer = null;
    private FloatExternalizer externalizer = null;

    private TaskDialog busyWindow;

    private final GlassPane _glassPane = new GlassPane();

//    private GlassPane glassPane = new GlassPane();
    public CMainFrame() {
        _initFrame();
        _initListeners();
        _initMenuBar();
        _initDockableFrame();

        //The UI was not yet initialized at this moment; the CMainFrame needs to be static initalized
//        busyWindow = new TaskDialog();
//        setGlassPane(glassPane);
        setGlassPane(_glassPane);
    }

    public void showBusyDialog(String titleString) {
        //Why sometimes it turns into black?
//        _glassPane.updateBackgroundImage();

        _glassPane.setVisible(true);

        //lazy initalization
        if (busyWindow == null) {
            busyWindow = new TaskDialog();
        }

        if (titleString == null) {
            titleString = "";
        }

//        glassPane.updateBackgroundImage();
        busyWindow.setLabel(titleString);
        busyWindow.setVisible(true);
        //busyWindow.pack();

//        glassPane.setVisible(true);
//        CoolMapMaster.getViewport().setEnabled(false);
//        doLayout();
    }

    public void hideBusyDialog() {
        if (busyWindow != null && busyWindow.isVisible()) {
            busyWindow.setVisible(false);
        }
//        glassPane.setVisible(false);
//        CoolMapMaster.getViewport().setEnabled(true);
        _glassPane.setVisible(false);

    }

    public void saveWorkspace(String fileUrlString) {

        //not quite working now as I can't export
        DockModelPropertiesEncoder encoder = new DockModelPropertiesEncoder();

//        System.out.println("DockModel:" + dockModel);
//        System.out.println("Can export?" + encoder.canExport(dockModel, fileUrlString + "/workspace"));
//        System.out.println("Can save?" + encoder.canSave(dockModel));
//        System.out.println("Source?" + dockModel.getSource());
        try {

            //encoder.export(dockModel, fileUrlString);
            encoder.save(dockModel);

            //write custom parameters
            File file = new File(fileUrlString + "/default.dck");
//            System.out.println("Viewport:" + CoolMapMaster.getViewport());
            WidgetViewport viewport = CoolMapMaster.getViewport();
            FileWriter writer = new FileWriter(file, true);

            if (viewport != null && viewport.getDockable().getState() == DockableState.MAXIMIZED) {
                writer.write("#coolMap.widgetViewport.state=maximized\n");
            } else {
                writer.write("#coolMap.widgetViewport.state=minimized\n");
            }

            writer.flush();
            writer.close();

            //append
        } catch (IOException ex) {
            Logger.getLogger(CMainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CMainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Message: Current Work space saved.");
    }

    public void loadWorkspace(String fileUrlString) {

        File file = new File(fileUrlString);
        if (!file.exists()) {
            return;
        }

        DockModelPropertiesDecoder dockModelDecoder = new DockModelPropertiesDecoder();
        //Need to load all the current dockables
        System.out.println("Loading from:" + fileUrlString);

        //This works
        Map dockableMap = new HashMap();

        for (Widget w : widgetHashMap.values()) {
            dockableMap.put(w.getID(), w.getDockable());
            w.getDockable().getDock().removeDockable(w.getDockable()); //remove itself
        }

        //This works
        Map ownerMap = new HashMap();
        ownerMap.put("MainFrame", this);

        //maximizer does not work. Why?
        Map visualizerMap = new HashMap();
//        visualizerMap.put("maximizer", maximizer);

//        apparently the externalizer will work
//        visualizerMap.put("minimizer", minimizer);
        visualizerMap.put("externalizer", externalizer);

        try {

            boolean maximizeViewport = false;
            FloatDockModel newDockModel;
            try {
                //need a way to revert to default, if 
                newDockModel = (FloatDockModel) dockModelDecoder.decode(fileUrlString, dockableMap, ownerMap, visualizerMap);
            } catch (Exception IOException) {
                return;
            }

//            try {
//                for (Widget w : widgetHashMap.values()) {
//                    System.out.println(w + " :Docked at: " + w.getDockable().getDock().getParentDock());
//                    System.out.println(w.getDockable().getState());
//                }
//            } catch (Exception e) {
//                return;
//            }
            //File file = new File(fileUrlString);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    line = line.substring(1);

                    String[] ele = line.split("=");
                    if (ele.length == 2) {
                        if (ele[0].equals("coolMap.widgetViewport.state")) {
                            if (ele[1].equals("maximized")) {
                                //maximize!
                                //System.out.println("Maximize viewport!");
                                maximizeViewport = true;
                            }
                        }
                    }
                }
            }

//            System.out.println("old dockmodel owner count:" + dockModel.getOwnerCount());
//            System.out.println(dockModel.getOwner(0));//it is the ownder but can't be removed?
//
//            System.out.println("Contents of the old dockmodel:");
//            Iterator it = dockModel.getRootKeys(this);
//            while (it.hasNext()) {
//                Object key = it.next();
//                System.out.println(key + "->" + dockModel.getRootDock(key.toString()));
//            }
//            System.out.println("");
            //dockModel.getRootDock("splitDock")
//            System.out.println("new dockmodel owner count:" + newDockModel.getOwnerCount());
//            System.out.println("Contents of the new dockmodel:");
//            it = newDockModel.getRootKeys(this);
//            while (it.hasNext()) {
//                Object key = it.next();
//                System.out.println(key + "->" + newDockModel.getRootDock(key.toString()));
//            }
//            System.out.println("");
//
//            System.out.println(newDockModel.getOwnerCount() + " " + newDockModel.getOwner(0));
//            System.out.println("");
            dockModel.removeVisualizer(maximizer);
            dockModel.removeVisualizer(externalizer);

            JPanel contentPane = (JPanel) this.getContentPane();

            contentPane.removeAll();

            //Add the new maximizer;
            maximizer = new SingleMaximizer((SplitDock) newDockModel.getRootDock("splitDock"));
            newDockModel.addVisualizer("maximizer", maximizer, this);
            contentPane.add(maximizer, BorderLayout.CENTER);

            doLayout();

//            for(Widget w : widgetHashMap.values()){
//                Dockable d = w.getDockable();
//                System.out.println(w.getName() + " dock:" + d.getState() + " " + DockableState.NORMAL);
//            }
            //replace with current model
            dockModel = newDockModel;

            //determine whether maximize is needed
//            System.out.println("Is maximizing viewport needed?");
            if (maximizeViewport) {
                WidgetViewport viewport = CoolMapMaster.getViewport();
                if (viewport != null) {
                    DefaultDockableStateAction maximizeAction = new DefaultDockableStateAction(viewport.getDockable(), DockableState.MAXIMIZED);
                    maximizeAction.actionPerformed(null);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(CMainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void _initDockableFrame() {

//        System.out.println("\n\nThe content pane is:" + getContentPane() + "\n\n");
        JPanel contentPane = (JPanel) getContentPane();

        String source = Config.getProperty(Config.WORKSPACE_DIRECTORY) + File.separator + "default.dck";

        //cmain frame intialized before config??
//        System.out.println("CMainFrame was created + Source:" + source);
        dockModel = new FloatDockModel(source);

        //make sure the owner is always the main frame
        dockModel.addOwner("MainFrame", this);

        DockingManager.setDockModel(dockModel);
        DockingManager.setComponentFactory(new DefaultSwComponentFactory() {

            @Override
            public JSplitPane createJSplitPane() {
                JSplitPane splitPane = super.createJSplitPane();
                splitPane.setDividerSize(2);
                return splitPane;
            }
        });

//        DockingManager.setComponentFactory(new DefaultSwComponentFactory());
        leftTabDock1 = new TabDock();
        leftTabDock2 = new TabDock();
        leftTabDock3 = new TabDock();

        rightTopTabDock = new TabDock();
        rightBottomTabDock = new TabDock();

//        SplitDock splitDock = new SplitDock();
        SplitDock rightSplitDock = new SplitDock();
        SplitDock leftSplitDock1 = new SplitDock();
        SplitDock leftSplitDock2 = new SplitDock();
        SplitDock leftSplitDock3 = new SplitDock();

        rightSplitDock.addChildDock(rightTopTabDock, new Position(Position.TOP));
        rightSplitDock.addChildDock(rightBottomTabDock, new Position(Position.BOTTOM));

        leftSplitDock1.addChildDock(leftSplitDock2, new Position(Position.TOP));
        leftSplitDock1.addChildDock(leftSplitDock3, new Position(Position.BOTTOM));

        leftSplitDock2.addChildDock(leftTabDock1, new Position(Position.CENTER));
        leftSplitDock3.addChildDock(leftTabDock2, new Position(Position.TOP));
        leftSplitDock3.addChildDock(leftTabDock3, new Position(Position.BOTTOM));

        rootDock.addChildDock(leftSplitDock1, new Position(Position.LEFT));
        rootDock.addChildDock(rightSplitDock, new Position(Position.RIGHT));

        dockModel.addRootDock("splitDock", rootDock, this);

        externalizer = new FloatExternalizer(this);
        dockModel.addVisualizer("externalizer", externalizer, this);

//        The line minimizer setup
//        LineMinimizer minimizer = new LineMinimizer(rootDock);
//        dockModel.addVisualizer("minimizer", minimizer, this);
//        SingleMaximizer maximizer = new SingleMaximizer(minimizer);
//        dockModel.addVisualizer("maximizer", maximizer, this);
//        contentPane.add(maximizer, BorderLayout.CENTER);
//        the
        maximizer = new SingleMaximizer(rootDock);
        dockModel.addVisualizer("maximizer", maximizer, this);

//        BorderDock borderDock = new BorderDock(new ToolBarDockFactory());
//        borderDock.setMode(BorderDock.MODE_MINIMIZE_BAR);
//        borderDock.setCenterComponent(maximizer);
//        BorderDocker borderDocker = new BorderDocker();
//        borderDocker.setBorderDock(borderDock);
//        minimizer = new DockingMinimizer(borderDocker);
//        dockModel.addVisualizer("minimizer", minimizer, this);
//        dockModel.addRootDock("minimizerBorderDock", borderDock, this);
        //It is a border dock that was added
        //contentPane.add(borderDock, BorderLayout.CENTER);
        //maximer is the one to be added
        contentPane.add(maximizer, BorderLayout.CENTER);

        rightSplitDock.setDividerLocation((int) (_defaultToolkit.getScreenSize().height * 0.8 * 0.7));
        rootDock.setDividerLocation(400);
        leftSplitDock1.setDividerLocation(200);
        leftSplitDock3.setDividerLocation(300);

    }

    public void addWidget(final Widget widget) {
        if (widget != null) {
            widgetHashMap.put(widget.getID(), widget);
            //System.out.println(widget.getID() + "->" + widget.getDockable());
            switch (widget.getPreferredLocation()) {
                case Widget.L_LEFTTOP:
                    leftTabDock1.addDockable(widget.getDockable(), new Position(0));
                    break;
                case Widget.L_LEFTCENTER:
                    leftTabDock2.addDockable(widget.getDockable(), new Position(0));
                    break;
                case Widget.L_LEFTBOTTOM:
                    leftTabDock3.addDockable(widget.getDockable(), new Position(0));
                    break;
                case Widget.L_VIEWPORT:
                    rightTopTabDock.addDockable(widget.getDockable(), new Position(0));
                    break;
                case Widget.L_DATAPORT:
                    rightBottomTabDock.addDockable(widget.getDockable(), new Position(0));
                    break;
            }

        }
    }

    //private final Menu _viewMenu = new Menu("View");
    public Menu findRootMenu(String title) {
        for (int i = 0; i < _menuBar.getMenuCount(); i++) {
            Menu menu = _menuBar.getMenu(i);
            if (menu != null && menu.getLabel().equals(title)) {
                return menu;
            }
        }
        return null;
    }
//    public MenuItem findMenuItem(String parentPath){

    public void addMenuSeparator(String parentPath) {
        addMenuItem(parentPath, new MenuItem(), false, false);
    }

    //private MenuBar _menuBar = new MenuBar();
    public void addMenuItem(String parentPath, MenuItem item, boolean sepBefore, boolean sepAfter) {
        if (item == null) {
            return;
        }

        if ((parentPath == null || parentPath.equals(""))) {
            if (item instanceof Menu) {
                _menuBar.add((Menu) item);
            } else {
                System.err.println("MenuItem + " + item + " can not be added to Menu Bar");
            }
        } else {
            String ele[] = parentPath.split("/");
            Menu currentMenu = null;
            String menuLabel = null;
            Menu searchMenu = null;
            MenuItem searchItem = null;

            for (int i = 0; i < ele.length; i++) {
                menuLabel = ele[i].trim();
                if (currentMenu == null) {
                    //search root
                    boolean found = false;
                    for (int j = 0; j < _menuBar.getMenuCount(); j++) {
                        searchMenu = _menuBar.getMenu(j);
                        if (searchMenu.getLabel().equalsIgnoreCase(menuLabel)) {
                            currentMenu = searchMenu;
                            found = true;
                            break;
                        }
                    }//end of search all items, not found, add new entry
                    if (found == false) {
                        currentMenu = new Menu(menuLabel);
                        _menuBar.add((Menu) currentMenu);
                    }
                } else {
                    boolean found = false;
                    for (int j = 0; j < currentMenu.getItemCount(); j++) {
                        searchItem = currentMenu.getItem(j);
                        if (searchItem instanceof Menu && ((Menu) searchItem).getLabel().equalsIgnoreCase(menuLabel)) {
                            currentMenu = (Menu) searchItem;
                            found = true;
                            break;
                        }
                    }
                    if (found == false) {
                        Menu newMenu = new Menu(menuLabel);
                        currentMenu.add(newMenu);
                        currentMenu = newMenu;
                    }
                }
            }//Should iterate all and found it

            if (sepBefore) {
                currentMenu.addSeparator();
            }
            //if (item instanceof MenuItem) {

            if (item.getLabel().equals("")) {
                currentMenu.addSeparator();
            } else {
                currentMenu.add((MenuItem) item);
            }
            //}

//            else if(item instanceof JSeparator){
//                currentMenu.addSeparator();
//            }
            if (sepAfter) {
                currentMenu.addSeparator();
            }
        }

    }

    public MenuItem findMenu(String parentPath) {

        if (parentPath.startsWith("/")) {

        }

        String ele[] = parentPath.split("/");
        Menu currentMenu = null;
        String menuLabel = null;
        Menu searchMenu = null;
        MenuItem searchItem = null;

        System.out.println(Arrays.toString(ele));

        for (int i = 0; i < ele.length; i++) {
            menuLabel = ele[i].trim();
            if (currentMenu == null) {
                //search root
                boolean found = false;
                for (int j = 0; j < _menuBar.getMenuCount(); j++) {
                    searchMenu = _menuBar.getMenu(j);
                    if (searchMenu.getLabel().equalsIgnoreCase(menuLabel)) {
                        currentMenu = searchMenu;
                        found = true;
                        break;
                    }
                }//end of search all items, not found, add new entry
                if (found == false) {
                    return null;
                    //currentMenu = new Menu(menuLabel);
                    //_menuBar.add((Menu) currentMenu);
                }
            } else {
                boolean found = false;
                for (int j = 0; j < currentMenu.getItemCount(); j++) {
                    searchItem = currentMenu.getItem(j);
                    if (searchItem instanceof Menu && ((Menu) searchItem).getLabel().equalsIgnoreCase(menuLabel)) {
                        currentMenu = (Menu) searchItem;
                        found = true;
                        break;
                    }
                }
                if (found == false) {
                    //Menu newMenu = new Menu(menuLabel);
                    //currentMenu.add(newMenu);
                    //currentMenu = newMenu;
                    return null;
                }
            }
        }//Should iterate all and found it

//            currentMenu.add(item);
        return currentMenu;
    }

    private void _initMenuBar() {

        setMenuBar(_menuBar);

        addMenuItem("", new Menu("File"), false, false);
        addMenuItem("", new Menu("Edit"), false, false);
        addMenuItem("", new Menu("View"), false, false);
        addMenuItem("", new Menu("Analysis"), false, false);
        addMenuItem("", new Menu("About"), false, false);
    }

    private void _initFrame() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {

        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setTitle("CoolMap Application");
        Dimension d = _defaultToolkit.getScreenSize();
        d.width = (int) (d.width * 0.8);
        d.height = (int) (d.height * 0.8);
        setSize(d);
        setPreferredSize(d);
        setLocation((int) (_defaultToolkit.getScreenSize().width * 0.1), (int) (_defaultToolkit.getScreenSize().height * 0.1));
        pack();
    }

    private void _initListeners() {
        addWindowListener(new CMainFrameWindowAdapter());
    }

    private class CMainFrameWindowAdapter extends WindowAdapter {

        public void windowClosing(WindowEvent e) {
            //Do some prompt of asking the user to - save the current session
            //Here -> CoolMap will be closed
            CoolMapMaster.getCMainFrame().saveWorkspace(Config.getProperty(Config.WORKSPACE_DIRECTORY));

            System.out.println("CoolMap Application will be closed.");
            System.exit(0);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    private class GlassPane extends JPanel implements MouseListener, MouseMotionListener {

        private BufferedImage _background;
        private Color c1 = new Color(0,0,0,150);
        private Color c2 = new Color(0,0,0,220);

        public GlassPane() {
            setOpaque(false);
            addMouseListener(this);
            addMouseMotionListener(this);
        }

//        private BufferedImage _background;
//        public void updateBackgroundImage() {
//
//            _background = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight());
//            BufferedImage backgrondBlur = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight());
//            
//            
//            Graphics2D g2D = _background.createGraphics();
//
//            //why it captured the JDialog instead of the underneath...?
//            CMainFrame.this.getContentPane().paint(g2D);
//
//            g2D.dispose();
//
//            float data[] = {0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f,
//                0.0625f, 0.125f, 0.0625f};
//            Kernel kernel = new Kernel(3, 3, data);
//            ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP,
//                    null);
//            convolve.filter(_background, backgrondBlur);
//            
//            
//            _background = backgrondBlur;
//        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.
            Graphics2D g2D = (Graphics2D) g;

//            g2D.drawImage(_background, 0, 0, this);
            int width = getWidth();
            int height = getHeight();
            
            int radius = width>height?width/2:height/2;
            
            RadialGradientPaint paint = new RadialGradientPaint(width/2, height/2, radius, new float[]{0f, 1f}, new Color[]{
                c1,c2
            });
            g2D.setPaint(paint);
            g2D.fillRect(0, 0, getWidth(), getHeight());//
            
            
            
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

    }
}
