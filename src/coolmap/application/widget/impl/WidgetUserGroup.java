/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Range;
import coolmap.application.CoolMapMaster;
import coolmap.application.io.internal.coolmapobject.InternalCoolMapObjectIO;
import coolmap.application.listeners.DataStorageListener;
import coolmap.application.state.StateStorageMaster;
import coolmap.application.utils.DataMaster;
import coolmap.application.utils.Messenger;
import coolmap.application.widget.Widget;
import static coolmap.application.widget.Widget.W_MODULE;
import coolmap.application.widget.WidgetMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.state.CoolMapState;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class WidgetUserGroup extends Widget implements DataStorageListener {

    private String ATTR_COLOR = "color";
    private String ATTR_NODES = "nodes";
    private String ATTR_NAME = "name";

    @Override
    public boolean restoreState(JSONObject savedState) {
//        System.out.println("Restore from state:" + savedState);
        if (savedState != null) {
            nodeColor.clear();
            nodeGroups.clear();
            Iterator i = savedState.keys();
            while (i.hasNext()) {
                try {
                    String groupName = (String) i.next();
                    JSONObject entry = savedState.optJSONObject(groupName);
                    if (entry != null) {
                        Color c = new Color(entry.optInt(ATTR_COLOR, UI.randomColor().getRGB()));
                        JSONArray nodes = entry.optJSONArray(ATTR_NODES);
                        if (nodes != null && nodes.length() > 0 && c != null) {

                            //add
                            ArrayList<VNode> vnodes = new ArrayList<>();
                            for (int j = 0; j < nodes.length(); j++) {
                                JSONObject obj = nodes.getJSONObject(j);
                                VNode node = InternalCoolMapObjectIO.createNodeFromJSON(obj);
                                vnodes.add(node);
                            }

                            //now added all nodes
                            nodeColor.put(groupName, c);
                            nodeGroups.putAll(groupName, vnodes);
                        }
                    }
                } catch (Exception e) {
                    //parse error, dont add
                }
            }//end of iterate all

            updateTable();
            return true;

        } else {
            //if null, reset
            nodeColor.clear();
            nodeGroups.clear();
            updateTable();
            return true;
        }

    }

    @Override
    public JSONObject getCurrentState() {
        JSONObject obj = new JSONObject();
        for (String key : nodeColor.keySet()) {
            JSONObject entry = new JSONObject();
            try {
                obj.put(key, entry);
                entry.put(ATTR_NAME, key);
                entry.put(ATTR_COLOR, nodeColor.get(key).getRGB());
                List<VNode> nodes = nodeGroups.get(key);
                ArrayList<JSONObject> nodeJSON = new ArrayList<>(nodes.size());
                for (VNode node : nodes) {
                    nodeJSON.add(InternalCoolMapObjectIO.nodeToJSON(node));
                }
                entry.put(ATTR_NODES, nodeJSON);
            } catch (Exception e) {

            }
        }
        try {
            obj = new JSONObject(obj.toString());
            if (obj.length() > 0) {
                return obj;
            } else {
                return null;
            }
//            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    private void initPopup() {
        JPopupMenu popup = new JPopupMenu();
        table.setComponentPopupMenu(popup);

        JMenuItem item = new JMenuItem("Rename");
        popup.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> groupNames = getSelectedGroups();
                if (groupNames.isEmpty()) {
                    return;
                }

                String returnVal = JOptionPane.showInputDialog(CoolMapMaster.getCMainFrame(), "Please provide a new name:");
                if (returnVal == null || returnVal.length() == 0) {
                    returnVal = "Untitled";
                }

                int counter = 0;
                String newName;
                for (String groupName : groupNames) {
                    if (counter == 0) {
                        newName = returnVal;
                    } else {
                        newName = returnVal + "_" + counter;
                    }

                    //new name must not exist
                    int subCounter = 0;
                    String name = newName;
                    while (nodeGroups.containsKey(name)) {
                        subCounter++;
                        name = newName + "_" + subCounter;
                    }
                    newName = name;

                    Color c = nodeColor.get(groupName);
                    Set<VNode> nodes = new HashSet(nodeGroups.get(groupName));

                    nodeColor.remove(groupName);
                    nodeGroups.removeAll(groupName);

                    nodeColor.put(newName, c);
                    nodeGroups.putAll(newName, nodes);

//                    System.out.println(newName + " " + c + " " + nodes);
                    counter++;
                }

                updateTable();

            }
        });
        ////////////////////////////////////////////////////////////////////////

        //remove operations
        item = new JMenuItem("Remove");
        popup.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> groupNames = getSelectedGroups();
                for (String group : groupNames) {
                    nodeColor.remove(group);
                    nodeGroups.removeAll(group);
                }
                updateTable();
            }
        });

        //add separarator
        popup.addSeparator();
        JMenu insertRow = new JMenu("Add selected to row");
        popup.add(insertRow);

        JMenu insertColumn = new JMenu("Add selected to column");
        popup.add(insertColumn);

        item = new JMenuItem("prepend", UI.getImageIcon("prependRow"));
        insertRow.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                insertRow(0);
            }
        });

        item = new JMenuItem("prepend", UI.getImageIcon("prependColumn"));
        insertColumn.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                insertColumn(0);
            }
        });

        item = new JMenuItem("insert", UI.getImageIcon("insertRow"));
        item.setToolTipText("Insert selected groups to the selected region in the active coolmap view");
        insertRow.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                int index = 0;
                ArrayList selectedRows = obj.getCoolMapView().getSelectedRows();
                if (!selectedRows.isEmpty()) {
                    index = ((Range<Integer>) selectedRows.iterator().next()).lowerEndpoint();
                }
                insertRow(index);
            }
        });

        item = new JMenuItem("insert", UI.getImageIcon("insertColumn"));
        item.setToolTipText("Insert selected groups to the selected region in the active coolmap view");
        insertColumn.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                int index = 0;
                ArrayList selectedColumns = obj.getCoolMapView().getSelectedColumns();
                if (!selectedColumns.isEmpty()) {
                    index = ((Range<Integer>) selectedColumns.iterator().next()).lowerEndpoint();
                }
                insertColumn(index);
            }
        });

        item = new JMenuItem("append", UI.getImageIcon("appendRow"));
        insertRow.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (CoolMapMaster.getActiveCoolMapObject() == null) {
                    return;
                }
                insertRow(CoolMapMaster.getActiveCoolMapObject().getViewNumRows());
            }
        });

        item = new JMenuItem("append", UI.getImageIcon("appendColumn"));
        insertColumn.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (CoolMapMaster.getActiveCoolMapObject() == null) {
                    return;
                }
                insertColumn(CoolMapMaster.getActiveCoolMapObject().getViewNumColumns());
            }
        });

        item = new JMenuItem("replace", UI.getImageIcon("replaceRow"));
        insertRow.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                replaceRow();
            }
        });

        item = new JMenuItem("replace", UI.getImageIcon("replaceColumn"));
        insertColumn.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                replaceColumn();
            }
        });
    }

    private void replaceRow() {
        List<String> selectedGroups = getSelectedGroups();
        if (selectedGroups.isEmpty()) {
            Messenger.showWarningMessage("Please select group nodes to continue", "Empty selection");
            return;
        }

        List<VNode> nodesToAdd = getNodesToAdd(selectedGroups);
        if (nodesToAdd.isEmpty()) {
            return;
        }

        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
        if (obj == null) {
            return;
        }

        CoolMapState state = CoolMapState.createStateRows("Replace group nodes to row", obj, null);
        obj.replaceRowNodes(nodesToAdd, null);
        StateStorageMaster.addState(state);
    }

    private void insertRow(int index) {

        List<String> selectedGroups = getSelectedGroups();
        if (selectedGroups.isEmpty()) {
            Messenger.showWarningMessage("Please select group nodes to continue", "Empty selection");
            return;
        }

        List<VNode> nodesToAdd = getNodesToAdd(selectedGroups);
        if (nodesToAdd.isEmpty()) {
            return;
        }

        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
        if (obj == null) {
            return;
        }

        CoolMapState state = CoolMapState.createStateRows("Add group nodes to row", obj, null);
        obj.insertRowNodes(index, nodesToAdd, true);
        obj.getCoolMapView().centerToSelections();
        StateStorageMaster.addState(state);
    }

    private void insertColumn(int index) {

        List<String> selectedGroups = getSelectedGroups();
        if (selectedGroups.isEmpty()) {
            Messenger.showWarningMessage("Please select group nodes to continue", "Empty selection");
            return;
        }

        List<VNode> nodesToAdd = getNodesToAdd(selectedGroups);
        if (nodesToAdd.isEmpty()) {
            return;
        }

        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
        if (obj == null) {
            return;
        }

        CoolMapState state = CoolMapState.createStateColumns("Add group nodes to column", obj, null);
        obj.insertColumnNodes(index, nodesToAdd, true);
        obj.getCoolMapView().centerToSelections();
        StateStorageMaster.addState(state);
    }

    private void replaceColumn() {

        List<String> selectedGroups = getSelectedGroups();
        if (selectedGroups.isEmpty()) {
            Messenger.showWarningMessage("Please select group nodes to continue", "Empty selection");
            return;
        }

        List<VNode> nodesToAdd = getNodesToAdd(selectedGroups);
        if (nodesToAdd.isEmpty()) {
            return;
        }

        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
        if (obj == null) {
            return;
        }

        CoolMapState state = CoolMapState.createStateColumns("Replace group nodes to column", obj, null);
        obj.replaceColumnNodes(nodesToAdd, null);
        StateStorageMaster.addState(state);
    }

    private List<VNode> getNodesToAdd(List<String> selectedGroups) {
        ArrayList<VNode> nodesToAdd = new ArrayList<>();
        for (String groupName : selectedGroups) {
            Color c = nodeColor.get(groupName);
            List<VNode> nodes = nodeGroups.get(groupName);
            for (VNode node : nodes) {
                VNode nodeCopy = node.duplicate(true);
                nodeCopy.setViewColor(c);
                nodeCopy.setViewLabel("grp" + groupName);
                nodesToAdd.add(nodeCopy);//You may have nodes with the same ID, could cause some issues. Though these nodes are not associated with a tree
            }
        }
        return nodesToAdd;
    }

    private List<String> getSelectedGroups() {
        int[] selectedRows = table.getSelectedRows();
        ArrayList<String> groups = new ArrayList<String>();
        for (int row : selectedRows) {
            int index = table.convertRowIndexToModel(row);
            try {
                String groupName = table.getModel().getValueAt(index, 0).toString();
                if (groupName != null) {
                    groups.add(groupName);
                }
            } catch (Exception e) {

            }
        }
        return groups;
    }

    private JTable table = new JTable();

    public WidgetUserGroup() {
        super("Node Group", W_MODULE, L_LEFTCENTER, UI.getImageIcon("tag"), "User defined node groups");
        DataMaster.addDataStorageListener(this);
        nodeGroups = ArrayListMultimap.create();
        init();
        initPopup();
    }

    private final ListMultimap<String, VNode> nodeGroups;
    private final HashMap<String, Color> nodeColor = new HashMap();

    private void createNewGroup(ArrayList<VNode> nodes) {

        if (nodes.isEmpty()) {
            Messenger.showWarningMessage("Could not create new node group.\nNo nodes were selected.", "Selection error");
            return;
        }

        String groupName = JOptionPane.showInputDialog(CoolMapMaster.getCMainFrame(), "Name for new node group.");
        if (groupName == null || groupName.length() == 0) {
            groupName = "Untitled";
        }

        int counter = 0;
        String name = groupName;
        while (nodeGroups.containsKey(name)) {
            counter++;
            name = groupName + "_" + counter;
        }
        groupName = name;

        Color c = UI.randomColor();

        ArrayList<VNode> groupNodes = new ArrayList<VNode>();
        for (VNode node : nodes) {
            VNode copy = node.duplicate();
            copy.setParentNode(null); //remove parent!
            copy.setViewColor(c);
            nodeGroups.put(groupName, copy);
        }

        if (!nodeColor.containsKey(groupName)) {
            nodeColor.put(groupName, c);
        }
        updateTable();
    }

    private final String[] colLabels = new String[]{"Name", "Color", "Member count", "Members"};

    private void updateTable() {
        ArrayList<String> keys = new ArrayList<>(nodeGroups.keySet());
        Collections.sort(keys);

        Object[][] data = new Object[keys.size()][4];

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            List<VNode> nodes = nodeGroups.get(key);

            data[i][0] = key;
            data[i][1] = "      ";
            data[i][2] = nodes.size();
            data[i][3] = nodes.toString();

        }

        final DefaultTableModel model = new DefaultTableModel(data, colLabels) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        };

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                table.setModel(model);
            }
        });

    }

    private void init() {

        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    return label;
                }

                if (column == 1) {
                    try {
                        label.setBackground(nodeColor.get(table.getModel().getValueAt(table.convertRowIndexToModel(row), 0)));
                    } catch (Exception e) {

                    }
                } else {
                    label.setBackground(UI.colorWhite);
                }

                return label;
            }

        });
        //Need a search box as well.

        //
        getContentPane().setLayout(new BorderLayout());

        //
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        JToolBar t = new JToolBar();
        getContentPane().add(t, BorderLayout.NORTH);
        t.setFloatable(false);

        try {
            //also add an action to add group nodes
            JMenuItem item = new JMenuItem("selected row nodes");
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    CoolMapObject o = CoolMapMaster.getActiveCoolMapObject();
                    if (o == null) {
                        return;
                    }

                    ArrayList<Range<Integer>> selected = o.getCoolMapView().getSelectedRows();
                    ArrayList<VNode> selectedNodes = new ArrayList<>();

                    for (Range<Integer> r : selected) {
                        for (int i = r.lowerEndpoint(); i < r.upperEndpoint(); i++) {
                            selectedNodes.add(o.getViewNodeRow(i));
                        }
                    }

                    createNewGroup(selectedNodes);

                    //create a group
                }
            });
            WidgetMaster.getViewport().addPopupMenuItem("Create group", item, false);

            item = new JMenuItem("selected column nodes");
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    CoolMapObject o = CoolMapMaster.getActiveCoolMapObject();
                    if (o == null) {
                        return;
                    }

                    ArrayList<Range<Integer>> selected = o.getCoolMapView().getSelectedColumns();
                    ArrayList<VNode> selectedNodes = new ArrayList<>();

                    for (Range<Integer> r : selected) {
                        for (int i = r.lowerEndpoint(); i < r.upperEndpoint(); i++) {
                            selectedNodes.add(o.getViewNodeColumn(i));
                        }
                    }

                    createNewGroup(selectedNodes);

                }
            });
            WidgetMaster.getViewport().addPopupMenuItem("Create group", item, false);
        } catch (Exception e) {
            //
            //Error handling.
        }

    }

    @Override
    public void coolMapObjectAdded(CoolMapObject newObject) {
    }

    @Override
    public void coolMapObjectToBeDestroyed(CoolMapObject objectToBeDestroyed) {
    }

    @Override
    public void baseMatrixAdded(CMatrix newMatrix) {
    }

    @Override
    public void baseMatrixToBeRemoved(CMatrix matrixToBeRemoved) {
    }

    @Override
    public void contologyAdded(COntology ontology) {
    }

    @Override
    public void contologyToBeDestroyed(COntology ontology) {
    }

}
