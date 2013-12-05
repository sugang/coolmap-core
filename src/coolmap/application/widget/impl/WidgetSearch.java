/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.widget.Widget;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.canvas.sidemaps.impl.ColumnTree;
import coolmap.canvas.sidemaps.impl.RowTree;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.listeners.CObjectListener;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author gangsu
 */
public class WidgetSearch extends Widget implements CObjectListener, ActiveCoolMapChangedListener {

    private JPanel _container = new JPanel();
    private JToolBar _toolBar = new JToolBar();
    private JTable _table = new JTable();
    private JTextField _searchField = new JTextField();

    public WidgetSearch() {
        super("Search", W_MODULE, L_LEFTTOP, UI.getImageIcon("search"), "Search for nodes in the current view");
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCObjectListener(this);
        CoolMapMaster.addActiveCoolMapChangedListener(this);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_container, BorderLayout.CENTER);

        _container.setLayout(new BorderLayout());
        _container.add(new JScrollPane(_table), BorderLayout.CENTER);
        _container.add(_toolBar, BorderLayout.NORTH);

        _toolBar.setFloatable(false);

        JLabel label = new JLabel(UI.getImageIcon("search"));
        label.setToolTipText("<html>Type in row / colum node names in the current active view.<br/>Use <strong>|</strong> as 'OR' operator to separate terms</html>");
        _toolBar.add(label);

        _toolBar.add(_searchField);
        _table.setAutoCreateRowSorter(true);

        _searchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent de) {
                _searchField.setBackground(UI.colorLightYellow);
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                _searchField.setBackground(UI.colorLightYellow);
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
            }
        });

        _searchField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _filterTable();
            }
        });

        _searchField.setToolTipText("<html>Type in terms and hit <strong>Enter</strong> to search.<br/> "
                + "A yellow background indicates that an update is required (<strong>Enter</strong>) <br/>"
                + "Use spaces for multiple terms"
                + "Use | for OR, example: <strong>term1|term2</strong>"
                + "</html>");

        _table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {
                    _selectNodes();
                }
            }
        });

    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    private void _selectNodes() {
        int[] rows = _table.getSelectedRows();
        if (rows == null || rows.length <= 0) {
            return;
        }

        int j = _table.getColumn("Node").getModelIndex();
        int direction = _table.getColumn("Direction").getModelIndex();
        int location = _table.getColumn("Location").getModelIndex();

        HashSet<Integer> rowIndices = new HashSet<Integer>();
        HashSet<Integer> columnIndices = new HashSet<Integer>();

        HashSet<Float> rowTreeIndices = new HashSet<Float>();
        HashSet<Float> colTreeIndices = new HashSet<Float>();

        HashSet<VNode> rowTreeNodes = new HashSet<>();
        HashSet<VNode> columnTreeNodes = new HashSet<>();

        for (int row : rows) {
            VNode node = null;
            try {
                //
                int i = _table.convertRowIndexToModel(row);

                node = (VNode) _table.getModel().getValueAt(i, j);
                String dir = (String) _table.getModel().getValueAt(i, direction);

                String loc = (String) _table.getModel().getValueAt(i, location);

                if (node.getViewIndex() == null) {
                    continue;
                }

                //
                if (!loc.equals("Tree")) {
                    if (dir.equals("Row")) {
                        rowIndices.add(node.getViewIndex().intValue());
                        
                    }
                    //
                    if (dir.equals("Column")) {
                        columnIndices.add(node.getViewIndex().intValue());
                        
                    }
                } else {
                    //it's the tree node
                    if (dir.equals("Row")) {
                        rowTreeIndices.add(node.getViewIndex().floatValue());
                        rowTreeNodes.add(node);
                    }
                    //
                    if (dir.equals("Column")) {
                        colTreeIndices.add(node.getViewIndex().floatValue());
                        columnTreeNodes.add(node);
                    }

                }
            } catch (Exception e) {
                CMConsole.log("Exception occured when trying to select: " + node);
            }
        }

        //System.out.println(rowIndices);
        //System.out.println(columnIndices);
        //Set selections
//        System.out.println(rowTreeNodes);
//        System.out.println(columnTreeNodes);
        
        
        
        
        
        
        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
        if (obj == null) {
            return;
        }

        if (!rowTreeNodes.isEmpty()) {
            try {
                RowTree rowTree = (RowTree) (obj.getCoolMapView().getRowMap("coolmap.canvas.sidemaps.impl.RowTree"));
                rowTree.setSelectedTreeNodes(rowTreeNodes);
            } catch (Exception e) {
//                e.printStackTrace();
                CMConsole.logError("error when selecting row ontology nodes");
            }
        }

        if (!columnTreeNodes.isEmpty()) {
            try {
                ColumnTree columnTree = (ColumnTree) (obj.getCoolMapView().getColumnMap("coolmap.canvas.sidemaps.impl.ColumnTree"));
                columnTree.setSelectedTreeNodes(columnTreeNodes);
            } catch (Exception e) {
//                e.printStackTrace();
                CMConsole.logError("error when selecting column ontology nodes");
            }
        }

        if (!rowIndices.isEmpty()) {
//            System.out.println("Select rows:" + rowIndices);
            obj.getCoolMapView().setSelectionRowIndices(rowIndices);
        }
        if (!columnIndices.isEmpty()) {
            obj.getCoolMapView().setSelectionsColumnIndices(columnIndices);
        }

        obj.getCoolMapView().centerToSelections();

        if (rowIndices.isEmpty() && columnIndices.isEmpty()) {
            //only group nodes are
            Rectangle center = new Rectangle();
            if (!rowTreeIndices.isEmpty()) {
                float index = 0;
                for (float f : rowTreeIndices) {
                    index += f;
                }
                center.y = (int) (index / rowTreeIndices.size());
            }

            if (!colTreeIndices.isEmpty()) {
                float index = 0;
                for (float f : colTreeIndices) {
                    index += f;
                }
                center.x = (int) (index / colTreeIndices.size());
            }

            center.width = 1;
            center.height = 1;
            _table.getSelectionModel().clearSelection();
            obj.getCoolMapView().centerToRegion(center);
        }

    }

    private void _filterTable() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                String text = _searchField.getText();
                if (text == null || text.length() == 0) {
                    _searchField.setBackground(Color.WHITE);
                    if (_table.getRowSorter() == null) {
                        return;
                    }

                    ((TableRowSorter) _table.getRowSorter()).setRowFilter(null);

                } else {
                    try {
                        _searchField.setBackground(Color.WHITE);

//                ((TableRowSorter) _table.getRowSorter()).setRowFilter(RowFilter.regexFilter("(?i)" + text));
                        HashSet<RowFilter<Object, Object>> filters = new HashSet<>();

                        String ele[] = text.trim().split("\\s+");
                        for (String term : ele) {
                            filters.add(RowFilter.regexFilter("(?i)" + term)); //apply to all indices
                        }

//                        RowFilter.andFilter(filters);
                        ((TableRowSorter) _table.getRowSorter()).setRowFilter(RowFilter.andFilter(filters));

                    } catch (Exception e) {
                        _searchField.setBackground(UI.colorRedWarning);

                        if (_table.getRowSorter() == null) {
                            return;
                        }
                        ((TableRowSorter) _table.getRowSorter()).setRowFilter(null);

                    }
                }
            }
        });

    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
        _updateList();
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
        _updateList();
    }

    @Override
    public void baseMatrixChanged(CoolMapObject object) {
    }

//    @Override
//    public void stateStorageUpdated(CoolMapObject object) {
//    }
    private void _updateList() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //        System.out.println("Table model updated");
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    _table.setModel(new DefaultTableModel());
                    return;
                }
                _table.clearSelection();
                List<VNode> rowNodes = obj.getViewNodesRow();
                List<VNode> colNodes = obj.getViewNodesColumn();
                List<VNode> rowNodesInTree = obj.getViewTreeNodesRow();
                List<VNode> colNodesInTree = obj.getViewTreeNodesColumn();

                Object[][] data = new Object[rowNodes.size() + colNodes.size() + rowNodesInTree.size() + colNodesInTree.size()][4];
                String[] title = new String[]{"Node", "Direction", "Location", "Ontology"};

                //weird exception not thrown
                int row = 0;
                for (VNode node : rowNodes) {
                    data[row][0] = node;
                    data[row][1] = "Row";
                    data[row][2] = "";
                    data[row][3] = node.getCOntology() == null ? "" : node.getCOntology();
                    row++;
                }

                for (VNode node : colNodes) {
                    data[row][0] = node;
                    data[row][1] = "Column";
                    data[row][2] = "";
                    data[row][3] = node.getCOntology() == null ? "" : node.getCOntology();
                    row++;
                }

                for (VNode node : rowNodesInTree) {
                    data[row][0] = node;
                    data[row][1] = "Row";
                    data[row][2] = "Tree";
                    data[row][3] = node.getCOntology() == null ? "" : node.getCOntology();
                    row++;
                }

                for (VNode node : colNodesInTree) {
                    data[row][0] = node;
                    data[row][1] = "Column";
                    data[row][2] = "Tree";
                    data[row][3] = node.getCOntology() == null ? "" : node.getCOntology();
                    row++;
                }

                _table.setModel(new DefaultTableModel(data, title) {

                    @Override
                    public boolean isCellEditable(int row, int col) {
                        return false;
                    }
                });

                _table.getColumn("Ontology").setCellRenderer(new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(JTable jtable, Object o, boolean isSelected, boolean bln1, int i, int i1) {
                        JLabel label = (JLabel) super.getTableCellRendererComponent(jtable, o, isSelected, bln1, i, i1);
                        if (!isSelected) {
                            if (o != null && o instanceof COntology) {
                                COntology ontology = (COntology) o;
                                if (ontology.getViewColor() != null) {
                                    label.setBackground(ontology.getViewColor());
                                }
                            } else {
                                label.setBackground(null);
                            }
                        }
                        return label;
                    }
                });

                _table.getColumn("Node").setCellRenderer(new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(JTable jtable, Object o, boolean isSelected, boolean bln1, int i, int i1) {
                        JLabel label = (JLabel) super.getTableCellRendererComponent(jtable, o, isSelected, bln1, i, i1);
                        if (!isSelected) {
                            if (o != null && o instanceof VNode) {
                                if (((VNode) o).getViewColor() != null) {
                                    label.setBackground(((VNode) o).getViewColor());
                                } else {
                                    label.setBackground(null);
                                }
                            } else {
                                label.setBackground(null);
                            }
                        }
                        return label;
                    }
                });

                _searchField.setText(null);

                //_table.setCellSelectionEnabled(false);
                //_table.setColumnSelectionAllowed(false);
            }
        });
    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        //also clear search terms
        _searchField.setText("");
        _searchField.setBackground(Color.WHITE);

        _updateList();
    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void nameChanged(CoolMapObject object) {
    }
}
