/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import com.google.common.collect.Range;
import coolmap.application.CMainFrame;
import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.state.StateStorageMaster;
import coolmap.application.utils.viewportActions.CascadeMapsAction;
import coolmap.application.utils.viewportActions.CenterSelectionAction;
import coolmap.application.utils.viewportActions.TileMapsAction;
import coolmap.application.utils.viewportActions.ToggleCanvasStateAction;
import coolmap.application.utils.viewportActions.ToggleColumnPanelsAction;
import coolmap.application.utils.viewportActions.ToggleLabeltipAction;
import coolmap.application.utils.viewportActions.ToggleSelectionLabelAction;
import coolmap.application.utils.viewportActions.ToggleSidePanelsRowAction;
import coolmap.application.utils.viewportActions.ToggleTooltipAction;
import coolmap.application.utils.viewportActions.ZoomInAction;
import coolmap.application.utils.viewportActions.ZoomOutAction;
import coolmap.application.widget.Widget;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.application.widget.misc.CanvasWidgetPropertyChangedListener;
import coolmap.canvas.CoolMapView;
import coolmap.data.CoolMapObject;
import coolmap.data.action.RenameCoolMapObjectAction;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.state.CoolMapState;
import coolmap.utils.BrowserLauncher;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 *
 * @author gangsu
 */
public final class WidgetViewport extends Widget implements ActiveCoolMapChangedListener {

    private JDesktopPane _desktop = new JDesktopPane();

    private JToolBar _toolBar = new JToolBar();
    private int frameMargin = 20;
    private final JPopupMenu _popupMenu = new JPopupMenu();
    private JToggleButton _gridMode;

    private void initPopup() {
        JMenuItem item = new JMenuItem("All Rows");
        addPopupMenuItem("Select", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    if (obj == null) {
                        return;
                    }

                    CoolMapView view = obj.getCoolMapView();
                    ArrayList<Range<Integer>> selectedColumns = view.getSelectedColumns();
                    ArrayList<Range<Integer>> selectedRows = view.getSelectedRows();

                    if (selectedColumns.isEmpty() || selectedRows.isEmpty()) {
                        return;
                    }

                    ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
                    for (Range<Integer> range : selectedColumns) {
                        newSelections.add(new Rectangle(range.lowerEndpoint(), 0, range.upperEndpoint() - range.lowerEndpoint(), obj.getViewNumRows()));
                    }

                    CoolMapState state = CoolMapState.createStateSelections("Select all rows", obj, null);
                    view.setSelections(newSelections);
                    StateStorageMaster.addState(state);

                } catch (Exception ex) {

                }
            }
        });

        item = new JMenuItem("All Columns");
        addPopupMenuItem("Select", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    if (obj == null) {
                        return;
                    }

                    CoolMapView view = obj.getCoolMapView();
                    ArrayList<Range<Integer>> selectedColumns = view.getSelectedColumns();
                    ArrayList<Range<Integer>> selectedRows = view.getSelectedRows();

                    if (selectedColumns.isEmpty() || selectedRows.isEmpty()) {
                        return;
                    }

                    ArrayList<Rectangle> newSelections = new ArrayList<Rectangle>();
                    for (Range<Integer> range : selectedRows) {
                        newSelections.add(new Rectangle(0, range.lowerEndpoint(), obj.getViewNumColumns(), range.upperEndpoint() - range.lowerEndpoint()));
                    }

                    CoolMapState state = CoolMapState.createStateSelections("Select all columns", obj, null);
                    view.setSelections(newSelections);
                    StateStorageMaster.addState(state);
                } catch (Exception ex) {

                }
            }
        });

        addPopupMenuSeparator(null);

        item = new JMenuItem("Selected Rows");
        addPopupMenuItem("Expand", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    ArrayList<Range<Integer>> selRows = obj.getCoolMapView().getSelectedRows();

                    ArrayList<VNode> rowNodes = new ArrayList<VNode>();

                    if (selRows == null || selRows.isEmpty()) {
                        return;
                    }

                    for (Range<Integer> range : selRows) {
                        for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                            rowNodes.add(obj.getViewNodeRow(i));
                        }
                    }

                    CoolMapState state = CoolMapState.createStateRows("Expand selected rows", obj, null);

                    List expandedNodes = obj.expandRowNodes(rowNodes, true);

                    if (expandedNodes == null || expandedNodes.isEmpty()) {
                        return;
                    }

                    StateStorageMaster.addState(state);

                } catch (Exception ex) {

                }
            }
        });

        item = new JMenuItem("Selected Rows to Leaf");
        addPopupMenuItem("Expand", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    ArrayList<Range<Integer>> selRows = obj.getCoolMapView().getSelectedRows();

                    ArrayList<VNode> rowNodes = new ArrayList<VNode>();

                    if (selRows == null || selRows.isEmpty()) {
                        return;
                    }

                    for (Range<Integer> range : selRows) {
                        for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                            rowNodes.add(obj.getViewNodeRow(i));
                        }
                    }

                    CoolMapState state = CoolMapState.createStateRows("Expand selected rows", obj, null);

                    List expandedNodes = obj.expandRowNodesToLeaf(rowNodes);

                    if (expandedNodes == null || expandedNodes.isEmpty()) {
                        return;
                    }

                    StateStorageMaster.addState(state);

                } catch (Exception ex) {

                }
            }
        });

        item = new JMenuItem("All Rows");
        addPopupMenuItem("Expand", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                obj.expandRowNodesToNextStep();
            }
        });

        item = new JMenuItem("Selected Columns");
        addPopupMenuItem("Expand", item, true);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    ArrayList<Range<Integer>> selColumns = obj.getCoolMapView().getSelectedColumns();

                    ArrayList<VNode> columnNodes = new ArrayList<VNode>();

                    if (selColumns == null || selColumns.isEmpty()) {
                        return;
                    }

                    for (Range<Integer> range : selColumns) {
                        for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                            columnNodes.add(obj.getViewNodeColumn(i));
                        }
                    }

                    CoolMapState state = CoolMapState.createStateColumns("Expand selected columns", obj, null);
                    List expandedNodes = obj.expandColumnNodes(columnNodes, true);

                    if (expandedNodes == null || expandedNodes.isEmpty()) {
                        return;
                    }

                    StateStorageMaster.addState(state);

                } catch (Exception ex) {

                }
            }
        });

        item = new JMenuItem("Selected Columns to Leaf");
        addPopupMenuItem("Expand", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    ArrayList<Range<Integer>> selColumns = obj.getCoolMapView().getSelectedColumns();

                    ArrayList<VNode> columnNodes = new ArrayList<VNode>();

                    if (selColumns == null || selColumns.isEmpty()) {
                        return;
                    }

                    for (Range<Integer> range : selColumns) {
                        for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                            columnNodes.add(obj.getViewNodeColumn(i));
                        }
                    }

                    CoolMapState state = CoolMapState.createStateColumns("Expand selected columns", obj, null);
                    List expandedNodes = obj.expandColumnNodesToLeaf(columnNodes);

                    if (expandedNodes == null || expandedNodes.isEmpty()) {
                        return;
                    }

                    StateStorageMaster.addState(state);

                } catch (Exception ex) {

                }
            }
        });

        item = new JMenuItem("All Columns");
        addPopupMenuItem("Expand", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                obj.expandColumnNodesToNextStep();
            }
        });

        item = new JMenuItem("Selected Rows");
        addPopupMenuItem("Collapse", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    ArrayList<Range<Integer>> selRows = obj.getCoolMapView().getSelectedRows();

                    LinkedHashSet<VNode> rowNodes = new LinkedHashSet<VNode>();

                    if (selRows == null || selRows.isEmpty()) {
                        return;
                    }

                    for (Range<Integer> range : selRows) {
                        for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                            VNode parentNode = obj.getViewNodeRow(i).getParentNode();
                            if (parentNode != null) {
                                rowNodes.add(parentNode);
                            }
                        }
                    }

                    if (rowNodes.isEmpty()) {
                        return;
                    }

                    CoolMapState state = CoolMapState.createStateRows("Collapse rows", obj, null);
                    List collapsedNodes = obj.collapseRowNodes(rowNodes, true);

                    if (collapsedNodes == null || collapsedNodes.isEmpty()) {
                        return;
                    }

                    StateStorageMaster.addState(state);

                } catch (Exception ex) {

                }
            }
        });

        item = new JMenuItem("All Rows");
        addPopupMenuItem("Collapse", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                obj.collapseRowNodesOneLayer();
            }
        });

        item = new JMenuItem("Selected Columns");
        addPopupMenuItem("Collapse", item, true);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    ArrayList<Range<Integer>> selColumns = obj.getCoolMapView().getSelectedColumns();

                    LinkedHashSet<VNode> colNodes = new LinkedHashSet<VNode>();

                    if (selColumns == null || selColumns.isEmpty()) {
                        return;
                    }

                    for (Range<Integer> range : selColumns) {
                        for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                            VNode parentNode = obj.getViewNodeColumn(i).getParentNode();
                            if (parentNode != null) {
                                colNodes.add(parentNode);
                            }
                        }
                    }

                    if (colNodes.isEmpty()) {
                        return;
                    }

                    CoolMapState state = CoolMapState.createStateColumns("Collapse columns", obj, null);
                    List collapsedNodes = obj.collapseColumnNodes(colNodes, true);

                    if (collapsedNodes == null || collapsedNodes.isEmpty()) {
                        return;
                    }

                    StateStorageMaster.addState(state);

                } catch (Exception ex) {

                }

            }
        });

        item = new JMenuItem("All Columns");
        addPopupMenuItem("Collapse", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                obj.collapseColumnNodesOneLayer();
            }
        });

        item = new JMenuItem("Selected Rows");
        addPopupMenuItem("Remove", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                ArrayList<Range<Integer>> selRows = obj.getCoolMapView().getSelectedRows();
                ArrayList<VNode> nodesToBeRemoved = new ArrayList<VNode>();
                for (Range<Integer> selections : selRows) {
                    for (int i = selections.lowerEndpoint(); i < selections.upperEndpoint(); i++) {
                        VNode node = obj.getViewNodeRow(i);
                        nodesToBeRemoved.add(node);

                    }
                }//end iteration
                try {
                    CoolMapState state = CoolMapState.createStateRows("Remove rows", obj, null);
                    obj.removeViewNodesRow(nodesToBeRemoved);
                    StateStorageMaster.addState(state);
                } catch (Exception ex) {
                    System.out.println("Error removing rows");
                }
            }
        });

        item = new JMenuItem("Selected Columns");
        addPopupMenuItem("Remove", item, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                ArrayList<Range<Integer>> selColumns = obj.getCoolMapView().getSelectedColumns();
                ArrayList<VNode> nodesToBeRemoved = new ArrayList<VNode>();
                for (Range<Integer> selections : selColumns) {
                    for (int i = selections.lowerEndpoint(); i < selections.upperEndpoint(); i++) {
                        VNode node = obj.getViewNodeColumn(i);
                        nodesToBeRemoved.add(node);
                    }
                }//end iteration

                try {
                    CoolMapState state = CoolMapState.createStateColumns("Remove columns", obj, null);
                    obj.removeViewNodesColumn(nodesToBeRemoved);
                    StateStorageMaster.addState(state);
                } catch (Exception ex) {
                    System.err.println("Error removing columns");
                }
            }
        });

        addPopupMenuSeparator(null);

        //add buttons to copy nodes
        //if it's copying part of the nodes; 
        JMenuItem copyItem = new JMenuItem("Row", UI.getImageIcon("insertRow"));
        addPopupMenuItem("Copy layout", copyItem, false);
        copyItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                CoolMapState rowState = CoolMapState.createStateRows("Copied rows", obj, null);

                StateStorageMaster.setCopiedState(rowState);

            }
        });

        copyItem = new JMenuItem("Column", UI.getImageIcon("insertColumn"));
        addPopupMenuItem("Copy layout", copyItem, false);
        copyItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                CoolMapState columnState = CoolMapState.createStateColumns("Copied columns", obj, null);

                StateStorageMaster.setCopiedState(columnState);
            }
        });

        copyItem = new JMenuItem("Both", UI.getImageIcon("grid"));
        addPopupMenuItem("Copy layout", copyItem, true);

        copyItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                CoolMapState bothState = CoolMapState.createState("Copied both", obj, null);

                StateStorageMaster.setCopiedState(bothState);
            }
        });

        JMenuItem pasteLayoutItem = new JMenuItem("Paste layout", UI.getImageIcon("paintRoll"));
        addPopupMenuItem("Paste", pasteLayoutItem, false);
        pasteLayoutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                CoolMapState state = StateStorageMaster.getCopiedState();
                if (state == null) {
                    return;
                }

                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                CoolMapState stateBeforePaste = CoolMapState.createState("Pasted state", obj, null);
                //can save here
                obj.restoreState(state);

                StateStorageMaster.addState(stateBeforePaste);
            }
        });

        //paste nodes from ontology browser
    }

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
        getContentPane().setBackground(UI.colorBlack3);

        initPopup();

        addPopupMenuSeparator(null);

        JMenuItem renameAction = new JMenuItem(new RenameCoolMapObjectAction());
        addPopupMenuItem("Edit", renameAction, false);

//        addPopupMenuSeparator(null);
        JMenuItem searchItem = new JMenuItem("Pubmed", UI.getImageIcon("pubmed"));
        addPopupMenuItem("Search selected...", searchItem, false);
        searchItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();

                    Rectangle sel = obj.getCoolMapView().getSelectionsUnion();

                    ArrayList<String> terms = new ArrayList<String>();
                    for (int i = sel.y; i < sel.y + sel.height; i++) {
                        terms.add(obj.getViewNodeRow(i).getName());
                    }
                    for (int i = sel.x; i < sel.x + sel.width; i++) {
                        terms.add(obj.getViewNodeColumn(i).getName());
                    }
                    BrowserLauncher.search(BrowserLauncher.pubmedURL, terms.toArray());
                } catch (Exception ex) {

                }
            }
        });

        searchItem = new JMenuItem("Google Scholar", UI.getImageIcon("googleScholar"));
        addPopupMenuItem("Search selected...", searchItem, false);
        searchItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();

                Rectangle sel = obj.getCoolMapView().getSelectionsUnion();

                ArrayList<String> terms = new ArrayList<String>();
                for (int i = sel.y; i < sel.y + sel.height; i++) {
                    terms.add(obj.getViewNodeRow(i).getName());
                }
                for (int i = sel.x; i < sel.x + sel.width; i++) {
                    terms.add(obj.getViewNodeColumn(i).getName());
                }
                BrowserLauncher.search(BrowserLauncher.googleScholarURL, terms.toArray());
            }
        });

        searchItem = new JMenuItem("Google", UI.getImageIcon("google"));
        addPopupMenuItem("Search selected...", searchItem, false);
        searchItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();

                Rectangle sel = obj.getCoolMapView().getSelectionsUnion();

                ArrayList<String> terms = new ArrayList<String>();
                for (int i = sel.y; i < sel.y + sel.height; i++) {
                    terms.add(obj.getViewNodeRow(i).getName());
                }
                for (int i = sel.x; i < sel.x + sel.width; i++) {
                    terms.add(obj.getViewNodeColumn(i).getName());
                }
                BrowserLauncher.search(BrowserLauncher.googleSearchURL, terms.toArray());
            }
        });

    }

//    public void setEnabled(boolean enabled) {
//        _desktop.setVisible(enabled);
//    }
//    public void setSessionName(String name){
//        if(name == null || name.length() == 0){
//            name = "Untitled";
//        }
//        getContentPane().setName("Canvas ( " + name + " )");
//        
//    }
    private void _initMainMenuItem() {
        CMainFrame frame = CoolMapMaster.getCMainFrame();

        MenuItem item;

        item = new MenuItem("Zoom in", new MenuShortcut(KeyEvent.VK_PLUS));
        frame.addMenuItem("View", item, false, false);
        item.addActionListener(new ZoomInAction());

        item = new MenuItem("Zoom out", new MenuShortcut(KeyEvent.VK_MINUS));
        frame.addMenuItem("View", item, false, false);
        item.addActionListener(new ZoomOutAction());

        item = new MenuItem("Center selection", new MenuShortcut(KeyEvent.VK_BACK_SPACE));
        frame.addMenuItem("View", item, false, true);
        item.addActionListener(new CenterSelectionAction());

        item = new MenuItem("Toggle canvas state", new MenuShortcut(KeyEvent.VK_1));
        frame.addMenuItem("View/Canvas config", item, false, false);
        item.addActionListener(new ToggleCanvasStateAction());

        item = new MenuItem("Toggle hover tooltip", new MenuShortcut(KeyEvent.VK_2));
        frame.addMenuItem("View/Canvas config", item, false, false);
        item.addActionListener(new ToggleTooltipAction());

        item = new MenuItem("Toggle label tooltip", new MenuShortcut(KeyEvent.VK_3));
        frame.addMenuItem("View/Canvas config", item, false, false);
        item.addActionListener(new ToggleLabeltipAction());

        item = new MenuItem("Toggle selection tooltip", new MenuShortcut(KeyEvent.VK_4));
        frame.addMenuItem("View/Canvas config", item, false, false);
        item.addActionListener(new ToggleSelectionLabelAction());

//        item = new MenuItem("Toggle annotation tooltip", new MenuShortcut(KeyEvent.VK_5));
//        frame.addMenuItem("View/Canvas config", item, false, false);
//        item.addActionListener(new ToggleAnnotationAction());
        MenuItem toggleRows = new MenuItem("Toggle row panels", new MenuShortcut(KeyEvent.VK_5));
        toggleRows.addActionListener(new ToggleSidePanelsRowAction());
        frame.addMenuItem("View/Canvas config", toggleRows, true, false);

        MenuItem toggleColumns = new MenuItem("Toggle column panels", new MenuShortcut(KeyEvent.VK_6));
        toggleColumns.addActionListener(new ToggleColumnPanelsAction());
        frame.addMenuItem("View/Canvas config", toggleColumns, false, false);

        ////
        frame.addMenuSeparator("View");

        item = new MenuItem("Tile", new MenuShortcut(KeyEvent.VK_OPEN_BRACKET));
        frame.addMenuItem("View/Arrange Maps", item, false, false);
        item.addActionListener(new TileMapsAction());

        item = new MenuItem("Cascade", new MenuShortcut(KeyEvent.VK_CLOSE_BRACKET));
        frame.addMenuItem("View/Arrange Maps", item, false, false);
        item.addActionListener(new CascadeMapsAction());

        //addPopupMenuItem("View", toggleColumns, false);
        //addPopupMenuItem("View", toggleRows, false);
    }

    public void addPopupMenuSeparator(String parentPath) {
        if ((parentPath == null || parentPath.equals(""))) {
            _popupMenu.addSeparator();
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
                        if (_popupMenu.getComponent(j) instanceof JPopupMenu.Separator) {
                            continue;
                        }

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
            }

            currentMenu.addSeparator();
        }
    }

    public void addPopupMenuItem(String parentPath, JMenuItem item, boolean sepBefore) {
        if (item == null) {
            return;
        }

        if ((parentPath == null || parentPath.length() == 0)) {
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
                        if (_popupMenu.getComponent(j) instanceof JPopupMenu.Separator) {
                            continue;
                        }

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
//            System.out.println(e);
            CMConsole.logError("Error: could not add new coolmap view.");
        }
    }
//    private JToggleButton _toggleZoomSubBar;
    private JToolBar _zoomSubBar;

    private void _initToolbar() {

        _toolBar.setFloatable(false);
        JButton button;

//        JButton button = new JButton(UI.getImageIcon("zoomIn"));
//        button.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                
//            }
//        });
//        _toolBar.add(button);
        _toolBar.add(new ZoomInAction());
        _toolBar.add(new ZoomOutAction());
        _toolBar.add(new CenterSelectionAction());

//        button = new JButton(UI.getImageIcon("zoomOut"));
//        button.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
//                if (object != null) {
//
//                    object.getCoolMapView().zoomOut(true, true);
//                }
//            }
//        });
//        _toolBar.add(button);
//        button = new JButton(UI.getImageIcon("emptyPage"));
//        button.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
//                if (object != null) {
//
//                    object.getCoolMapView().centerToSelections();
//                }
//            }
//        });
//        button.setToolTipText("Center selection");
//        _toolBar.add(button);
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

//        _toggleZoomSubBar = new JToggleButton(UI.getImageIcon("gear"));
//        _toolBar.add(_toggleZoomSubBar);
//        _toggleZoomSubBar.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                if (_toggleZoomSubBar.isSelected()) {
//                    _zoomSubBar.setVisible(true);
//                } else {
//                    _zoomSubBar.setVisible(false);
//                }
//            }
//        });
        _zoomSubBar = new JToolBar();
        _zoomSubBar.setFloatable(false);
        _zoomSubBar.setVisible(true);

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
        if (frames == null || frames.length == 0) {
            return;
        }

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
        if (frames == null || frames.length == 0) {
            return;
        }

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
            if (_desktop.getAllFrames().length == 0) {
//                System.out.println("No more active cool map objects");
                CoolMapMaster.setActiveCoolMapObject(null);
            }
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
