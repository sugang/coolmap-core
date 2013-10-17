/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application;

import coolmap.application.widget.Widget;
import com.javadocking.DockingManager;
import com.javadocking.component.*;
import com.javadocking.dock.BorderDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.TabDock;
import com.javadocking.dock.docker.BorderDocker;
import com.javadocking.dock.factory.ToolBarDockFactory;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.action.DefaultDockableStateAction;
import com.javadocking.event.DockingEvent;
import com.javadocking.event.DockingListener;
import com.javadocking.model.FloatDockModel;
import com.javadocking.model.codec.DockModelPropertiesDecoder;
import com.javadocking.model.codec.DockModelPropertiesEncoder;
import com.javadocking.visualizer.DockingMinimizer;
import com.javadocking.visualizer.FloatExternalizer;
import com.javadocking.visualizer.SingleMaximizer;
import coolmap.utils.Config;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

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

    public CMainFrame() {
        _initFrame();
        _initListeners();
        _initMenuBar();
        _initDockableFrame();
    }

    public void saveWorkspace(String fileUrlString) {

        //not quite working now as I can't export
        DockModelPropertiesEncoder encoder = new DockModelPropertiesEncoder();
        System.out.println("DockModel:" + dockModel);
        System.out.println("Can export?" + encoder.canExport(dockModel, fileUrlString + "/workspace"));
        System.out.println("Can save?" + encoder.canSave(dockModel));
        System.out.println("Source?" + dockModel.getSource());

        try {
            
            //encoder.export(dockModel, fileUrlString);

            encoder.save(dockModel);

        } catch (IOException ex) {
            Logger.getLogger(CMainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CMainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadWorkspace(String fileUrlString) {
        DockModelPropertiesDecoder dockModelDecoder = new DockModelPropertiesDecoder();
        
    }
    
    

    private void _initDockableFrame() {
        JPanel contentPane = (JPanel) getContentPane();

        String source = Config.getProperty(Config.WORKSPACE_DIRECTORY) + File.separator + "default.dck";

        //cmain frame intialized before config??
        System.out.println("CMainFrame was created + Source:" + source);

        dockModel = new FloatDockModel(source);

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

        FloatExternalizer externalizer = new FloatExternalizer(this);
        dockModel.addVisualizer("externalizer", externalizer, this);

//        The line minimizer setup
//        LineMinimizer minimizer = new LineMinimizer(rootDock);
//        dockModel.addVisualizer("minimizer", minimizer, this);
//        SingleMaximizer maximizer = new SingleMaximizer(minimizer);
//        dockModel.addVisualizer("maximizer", maximizer, this);
//        contentPane.add(maximizer, BorderLayout.CENTER);
//        the
        SingleMaximizer maximizer = new SingleMaximizer(rootDock);
        dockModel.addVisualizer("maximizer", maximizer, this);
        BorderDock borderDock = new BorderDock(new ToolBarDockFactory());
        borderDock.setMode(BorderDock.MODE_MINIMIZE_BAR);
        borderDock.setCenterComponent(maximizer);
        BorderDocker borderDocker = new BorderDocker();
        borderDocker.setBorderDock(borderDock);
        DockingMinimizer minimizer = new DockingMinimizer(borderDocker);
        dockModel.addVisualizer("minimizer", minimizer, this);
        dockModel.addRootDock("minimizerBorderDock", borderDock, this);
        contentPane.add(borderDock, BorderLayout.CENTER);

        rightSplitDock.setDividerLocation((int) (_defaultToolkit.getScreenSize().height * 0.8 * 0.7));
        rootDock.setDividerLocation(400);
        leftSplitDock1.setDividerLocation(200);
        leftSplitDock3.setDividerLocation(300);

    }

    public void addWidget(final Widget widget) {
        if (widget != null) {

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

//            final MenuItem showWidget = new MenuItem("Show " + widget.getName());
//            final DefaultDockableStateAction restoreAction = new DefaultDockableStateAction(widget.getDockable(), DockableState.NORMAL);
//            showWidget.setEnabled(false);
//            showWidget.addActionListener(new ActionListener() {
//
//                @Override
//                public void actionPerformed(ActionEvent ae) {
//                    restoreAction.actionPerformed(ae);
//                }
//            });
//
//            widget.getDockable().addDockingListener(new DockingListener() {
//
//                @Override
//                public void dockingWillChange(DockingEvent dockingEvent) {
//                }
//
//                @Override
//                public void dockingChanged(DockingEvent dockingEvent) {
//                    if (dockingEvent.getDestinationDock() == null) {
//                        showWidget.setEnabled(true);
//                    } else {
//                        showWidget.setEnabled(false);
//                    }
//                }
//            });
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

    //private MenuBar _menuBar = new MenuBar();
    public void addMenuItem(String parentPath, MenuItem item, boolean sepBefore) {
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
            currentMenu.add(item);
        }

    }

    public MenuItem findMenuItem(String parentPath) {

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

        addMenuItem("", new Menu("File"), false);
        addMenuItem("", new Menu("Edit"), false);
        addMenuItem("", new Menu("View"), false);
        addMenuItem("", new Menu("Analysis"), false);
        addMenuItem("", new Menu("About"), false);
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
            System.out.println("CoolMap Application will be closed");
            System.exit(0);
        }
    }
}
