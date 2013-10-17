/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import com.google.common.util.concurrent.ServiceManager;
import coolmap.application.CMainFrame;
import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.utils.LongTask;
import coolmap.application.utils.ServiceMaster;
import coolmap.application.widget.Widget;
import coolmap.application.widget.misc.CanvasWidgetPropertyChangedListener;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.sidemaps.impl.ColumnLabels;
import coolmap.data.CoolMapObject;
import coolmap.utils.cluster.HCluster;
import coolmap.utils.graphics.UI;
import edu.ucla.sspace.clustering.HierarchicalAgglomerativeClustering;
import edu.ucla.sspace.common.Similarity;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 *
 * @author gangsu
 */
public class WidgetViewport extends Widget implements ActiveCoolMapChangedListener {

    private JDesktopPane _desktop = new JDesktopPane();
    private JToolBar _toolBar = new JToolBar();
    private int frameMargin = 20;
    private final JPopupMenu _popupMenu = new JPopupMenu();
    private JToggleButton _gridMode;

    public WidgetViewport() {
        super("Canvas", W_VIEWPORT, L_VIEWPORT, UI.getImageIcon("layers"), "Display area for CoolMaps");
        getContentPane().setPreferredSize(new Dimension(640, 480));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_desktop, BorderLayout.CENTER);
        getContentPane().add(_toolBar, BorderLayout.NORTH);
        _initToolbar();
        _initMainMenuItem();
        CoolMapMaster.addActiveCoolMapChangedListener(this);
        getDockable().addPropertyChangeListener(new CanvasWidgetPropertyChangedListener());
    }

//    public void setSessionName(String name){
//        if(name == null || name.length() == 0){
//            name = "Untitled";
//        }
//        getContentPane().setName("Canvas ( " + name + " )");
//        
//    }
    private void _initMainMenuItem() {
        CMainFrame frame = CoolMapMaster.getCMainFrame();
        MenuItem item = new MenuItem("Tile");
        frame.addMenuItem("View/Arrange Views", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                tileWindows();
            }
        });

        item = new MenuItem("Cascade");
        frame.addMenuItem("View/Arrange Views", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                cascadeWindows();
            }
        });

        JMenuItem toggleRows = new JMenuItem("Toggle row panels");
        JMenuItem toggleColumns = new JMenuItem("Toggle column panels");
        addPopupMenuItem("View", toggleRows, false);
        addPopupMenuItem("View", toggleColumns, false);
        toggleRows.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {
                    CoolMapView view = object.getCoolMapView();
                    if (view.isRowPanelsVisible()) {
                        view.setRowPanelsVisible(false);
                    } else {
                        view.setRowPanelsVisible(true);
                    }
                }
            }
        });

        toggleColumns.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {
                    CoolMapView view = object.getCoolMapView();
                    if (view.isColumnPanelsVisible()) {
                        view.setColumnPanelsVisible(false);
                    } else {
                        view.setColumnPanelsVisible(true);
                    }
                }
            }
        });
    }

    public void addPopupMenuItem(String parentPath, JMenuItem item, boolean sepBefore) {
        if (item == null) {
            return;
        }

        if ((parentPath == null || parentPath.equals(""))) {
            if (item instanceof JMenu) {
                _popupMenu.add((JMenu) item);
            } else {
                System.err.println("MenuItem + " + item + " can not be added to Menu Bar");
            }
        } else {
            String ele[] = parentPath.split("/");
            JMenu currentMenu = null;
            String menuLabel = null;
            JMenu searchMenu = null;
            JMenuItem searchItem = null;

            for (int i = 0; i < ele.length; i++) {
                menuLabel = ele[i].trim();
                if (currentMenu == null) {
                    //search root
                    boolean found = false;
                    for (int j = 0; j < _popupMenu.getComponentCount(); j++) {
                        searchMenu = (JMenu) _popupMenu.getComponent(j);
                        if (searchMenu.getText().equalsIgnoreCase(menuLabel)) {
                            currentMenu = searchMenu;
                            found = true;
                            break;
                        }
                    }//end of search all items, not found, add new entry
                    if (found == false) {
                        currentMenu = new JMenu(menuLabel);
                        _popupMenu.add((JMenu) currentMenu);
                    }
                } else {
                    boolean found = false;
                    for (int j = 0; j < currentMenu.getItemCount(); j++) {
                        searchItem = currentMenu.getItem(j);
                        if (searchItem instanceof JMenu && ((JMenu) searchItem).getText().equalsIgnoreCase(menuLabel)) {
                            currentMenu = (JMenu) searchItem;
                            found = true;
                            break;
                        }
                    }
                    if (found == false) {
                        JMenu newMenu = new JMenu(menuLabel);
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

    public void addCoolMapView(CoolMapObject object) {
        if (object == null) {
            return;
        }

        object.getCoolMapView().setPopupMenu(_popupMenu);

        JInternalFrame frame = object.getCoolMapView().getViewFrame();
        frame.addInternalFrameListener(new CoolMapViewCloseListener(object));

        _addCoolMapFrame(frame);

    }

    private void _addCoolMapFrame(JInternalFrame frame) {
//        System.out.println(frame);
        _desktop.add(frame);

        try {
            //Seems that it only works after the desktop has been created.
            frame.setSelected(true);
            _desktop.getDesktopManager().activateFrame(frame);
            frame.requestFocus();
            frame.grabFocus();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    private JToggleButton _toggleZoomSubBar;
    private JToolBar _zoomSubBar;

    private void _initToolbar() {
        _toolBar.setFloatable(false);

        JButton button = new JButton(UI.getImageIcon("zoomIn"));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {

                    object.getCoolMapView().zoomIn(true, true);
                }
            }
        });
        _toolBar.add(button);

        button = new JButton(UI.getImageIcon("zoomOut"));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {

                    object.getCoolMapView().zoomOut(true, true);
                }
            }
        });
        _toolBar.add(button);


        button = new JButton(UI.getImageIcon("emptyPage"));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {

                    object.getCoolMapView().centerToSelections();
                }
            }
        });
        button.setToolTipText("Center selection");
        _toolBar.add(button);





        _gridMode = new JToggleButton(UI.getImageIcon("ruler"));
        _gridMode.setToolTipText("Enter/Exit grid mode, allows resizing of row/column nodes");
        _gridMode.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {


                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {
                    object.getCoolMapView().toggleGridMode(_gridMode.isSelected());
                }
            }
        });

        _toolBar.add(_gridMode);

        _toolBar.addSeparator();

        _toggleZoomSubBar = new JToggleButton(UI.getImageIcon("gear"));
        _toolBar.add(_toggleZoomSubBar);
        _toggleZoomSubBar.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (_toggleZoomSubBar.isSelected()) {
                    _zoomSubBar.setVisible(true);
                } else {
                    _zoomSubBar.setVisible(false);
                }
            }
        });

        _zoomSubBar = new JToolBar();
        _zoomSubBar.setFloatable(false);
        _zoomSubBar.setVisible(false);

        button = new JButton(UI.getImageIcon("zoomInX"));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {

                    object.getCoolMapView().zoomIn(true, false);
                }
            }
        });
        _zoomSubBar.add(button);

        button = new JButton(UI.getImageIcon("zoomOutX"));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {

                    object.getCoolMapView().zoomOut(true, false);
                }
            }
        });
        _zoomSubBar.add(button);

        button = new JButton(UI.getImageIcon("resetX"));
        button.setToolTipText("Reset cell widths");

        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {

                    object.getCoolMapView().resetNodeWidthColumn();
                }
            }
        });
        _zoomSubBar.add(button);

        _zoomSubBar.addSeparator();


        button = new JButton(UI.getImageIcon("zoomInY"));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {

                    object.getCoolMapView().zoomIn(false, true);
                }
            }
        });
        _zoomSubBar.add(button);

        button = new JButton(UI.getImageIcon("zoomOutY"));
        button.setToolTipText("Reset cell heights");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {

                    object.getCoolMapView().zoomOut(false, true);
                }
            }
        });
        _zoomSubBar.add(button);




        button = new JButton(UI.getImageIcon("resetY"));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {

                    object.getCoolMapView().resetNodeWidthRow();
                }
            }
        });
        _zoomSubBar.add(button);





        _zoomSubBar.setBackground(UI.colorLightBlue0);
        _toolBar.add(_zoomSubBar);








//        button = new JButton("Task");
//        button.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                ServiceMaster.getTaskEngine().submitTask(new LongTask("Hierarchical Clustering 1") {
//
//                    @Override
//                    public void run() {
//                        System.out.println("Task started 1");
//                        try {
//                            HCluster.hclustRow(CoolMapMaster.getActiveCoolMapObject(), HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.COSINE);
//                        } catch (Exception e) {
//                            return;
//                        }
//                        System.out.println("Task ended 1");
//                    }
//
//                    @Override
//                    public String getMessage() {
//                        return null;
//                    }
//
//                    @Override
//                    public double getProgress() {
//                        return -1;
//                    }
//                });
//
//                ServiceMaster.getTaskEngine().submitTask(new LongTask("Hierarchical Clustering 2") {
//
//                    @Override
//                    public void run() {
//                        System.out.println("Task started 2");
//                        try {
//                            HCluster.hclustRow(CoolMapMaster.getActiveCoolMapObject(), HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.COSINE);
//                        } catch (Exception e) {
//                            return;
//                        }
//                        System.out.println("Task ended 2");
//                    }
//
//                    @Override
//                    public String getMessage() {
//                        return null;
//                    }
//
//                    @Override
//                    public double getProgress() {
//                        return -1;
//                    }
//                });
//            }
//        });
//        _toolBar.add(button);




    }

    public void cascadeWindows() {
        JInternalFrame[] frames = _desktop.getAllFrames();
        int x = 0;
        int y = 0;
        int width = _desktop.getWidth() / 2;
        int height = _desktop.getHeight() / 2;

        for (int i = 0; i < frames.length; i++) {
            if (!frames[i].isIcon()) {
                try {  /*
                     * try to make maximized frames resizable this might be
                     * vetoed
                     */
                    frames[i].setMaximum(false);
                    frames[i].reshape(x, y, width, height);

                    x += frameMargin;
                    y += frameMargin;
                    // wrap around at the desktop edge
                    if (x + width > _desktop.getWidth()) {
                        x = 0;
                    }
                    if (y + height > _desktop.getHeight()) {
                        y = 0;
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    public void tileWindows() {
        JInternalFrame[] frames = _desktop.getAllFrames();

        // count frames that aren't iconized
        int frameCount = 0;
        for (int i = 0; i < frames.length; i++) {
            if (!frames[i].isIcon()) {
                frameCount++;
            }
        }

        int rows = (int) Math.sqrt(frameCount);
        int cols = frameCount / rows;
        int extra = frameCount % rows;
        // number of columns with an extra row

        int width = _desktop.getWidth() / cols;
        int height = _desktop.getHeight() / rows;
        int r = 0;
        int c = 0;
        for (int i = 0; i < frames.length; i++) {
            if (!frames[i].isIcon()) {
                try {
                    frames[i].setMaximum(false);
                    frames[i].reshape(c * width,
                            r * height, width, height);
                    r++;
                    if (r == rows) {
                        r = 0;
                        c++;
                        if (c == cols - extra) {  // start adding an extra row
                            rows++;
                            height = _desktop.getHeight() / rows;
                        }
                    }
                } catch (PropertyVetoException e) {
                }
            }
        }
    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {

        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
        if (obj == null) {
            return;
        }

        if (obj.getCoolMapView().isGridMode()) {
            _gridMode.setSelected(true);
        } else {
            _gridMode.setSelected(false);
        }

    }

    private class CoolMapViewCloseListener extends InternalFrameAdapter {

        private CoolMapObject _object = null;

        private CoolMapViewCloseListener() {
        }

        public CoolMapViewCloseListener(CoolMapObject object) {
            _object = object;
        }

        @Override
        public void internalFrameClosing(InternalFrameEvent ife) {
            super.internalFrameClosing(ife);
            CoolMapMaster.destroyCoolMapObject(_object);
            _object = null;
        }

        @Override
        public void internalFrameActivated(InternalFrameEvent ife) {
            super.internalFrameActivated(ife);
            if (CoolMapMaster.getActiveCoolMapObject() != _object) {
                CoolMapMaster.setActiveCoolMapObject(_object);
            }
        }
    }
}
