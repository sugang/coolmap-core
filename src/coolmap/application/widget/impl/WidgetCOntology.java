/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.DataStorageListener;
import coolmap.application.utils.DataMaster;
import coolmap.application.widget.Widget;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author gangsu
 */
public class WidgetCOntology extends Widget implements DataStorageListener {

    private JPanel _container = new JPanel();
    private JTable _ontologyTable = new JTable();
    private JComboBox _ontologyCombo = new JComboBox();
    private JPopupMenu _popupMenu = new JPopupMenu();
    private JTextField _searchField = new JTextField();

    public WidgetCOntology() {
        super("Ontology Table", W_DATA, L_DATAPORT, UI.getImageIcon("textList"), null);
        DataMaster.addDataStorageListener(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(_ontologyTable), BorderLayout.CENTER);
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        getContentPane().add(toolBar, BorderLayout.NORTH);
        JLabel label = new JLabel(UI.getImageIcon("ontologyTop"));
        label.setToolTipText("Choose loaded ontologies");
        toolBar.add(label);
        toolBar.add(_ontologyCombo);
        _ontologyCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    _updateTable();
                }
            }
        });

        _ontologyTable.setComponentPopupMenu(_popupMenu);
        JMenuItem item = new JMenuItem("Add selected nodes to View rows");
        _popupMenu.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                _insertNodesToRow();
            }
        });

        item = new JMenuItem("Add selected nodes to View columns");
        _popupMenu.add(item);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                _insertNodesToColumn();
            }
        });

        toolBar.addSeparator();
        JButton button = new JButton(UI.getImageIcon("rowLabel"));
        toolBar.add(button);
        button.setToolTipText("Add selected nodes to rows in the active CoolMap, at the beginning");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                _insertNodesToRow();
            }
        });


        button = new JButton(UI.getImageIcon("colLabel"));
        button.setToolTipText("Add selected nodes to columns in the active CoolMap, at the beginning");
        toolBar.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                _insertNodesToColumn();
            }
        });



        toolBar.addSeparator();
        toolBar.add(new JLabel(UI.getImageIcon("search")));
        toolBar.add(_searchField);
        _searchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent de) {
                _filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                _filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
            }
        });
        _ontologyTable.setAutoCreateRowSorter(true);
    }

    private void _filterTable() {
        String text = _searchField.getText();
        if (text == null || text.length() == 0) {
            _searchField.setBackground(Color.WHITE);
            if (_ontologyTable.getRowSorter() == null) {
                return;
            }

            ((TableRowSorter) _ontologyTable.getRowSorter()).setRowFilter(null);

        } else {
            try {
                _searchField.setBackground(Color.WHITE);
                ((TableRowSorter) _ontologyTable.getRowSorter()).setRowFilter(RowFilter.regexFilter("(?i)" + text));
            } catch (Exception e) {
                _searchField.setBackground(UI.colorRedWarning);

                if (_ontologyTable.getRowSorter() == null) {
                    return;
                }
                ((TableRowSorter) _ontologyTable.getRowSorter()).setRowFilter(null);

            }
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
        //update
        _updateOntologiesAndSelect(ontology);
    }

    @Override
    public void contologyToBeDestroyed(COntology ontology) {
        //update
        _updateOntologiesAndRemove(ontology);
    }

    private void _updateTable() {
        //System.out.println("Table needs to be updated.");
        if (_ontologyCombo.getSelectedItem() == null) {
            _ontologyTable.setModel(new DefaultTableModel());
        }
        COntology ontology = (COntology) _ontologyCombo.getSelectedItem();

        DefaultTableModel model = _getOntologyAsTableModel(ontology);
        _ontologyTable.setModel(model);

        ((TableRowSorter) _ontologyTable.getRowSorter()).setComparator(1, new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                if (o1 < o2) {
                    return -1;
                }
                if (o1 == o2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        ((TableRowSorter) _ontologyTable.getRowSorter()).setComparator(3, new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                if (o1 < o2) {
                    return -1;
                }
                if (o1 == o2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        ((TableRowSorter) _ontologyTable.getRowSorter()).setComparator(4, new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                if (o1 < o2) {
                    return -1;
                }
                if (o1 == o2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
    }

    private DefaultTableModel _getOntologyAsTableModel(COntology ontology) {
        HashSet<String> nodes = new HashSet<String>();
        nodes.addAll(ontology.getAllNodesWithChildren());
        nodes.addAll(ontology.getAllNodesWithParents());
        ArrayList<String> sortedNodes = new ArrayList<String>();
        sortedNodes.addAll(nodes);
        Collections.sort(sortedNodes);

        String[] headers = new String[]{"Node Name", "ChildCount", "ChildNodes", "ParentCount", "ParentNodes", "Depth"};
        Object[][] data = new Object[nodes.size()][6];
        for (int i = 0; i < data.length; i++) {
            String node = sortedNodes.get(i);
            List<String> child = ontology.getImmediateChildren(node);
            List<String> parent = ontology.getImmediateParents(node);

            data[i][0] = node;
            data[i][1] = child == null ? 0 : child.size();
            data[i][2] = child == null ? "" : Arrays.toString(child.toArray());
            data[i][3] = parent == null ? 0 : parent.size();
            data[i][4] = parent == null ? "" : Arrays.toString(parent.toArray());
            data[i][5] = ontology.getMinimalDepthFromLeaves(node);


        }

        DefaultTableModel model = new DefaultTableModel(data, headers) {

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        return model;
    }

    private void _insertNodesToRow() {
        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
        if (obj == null) {
            return;
        }

        int[] rows = _ontologyTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }

        COntology ontology = (COntology) _ontologyCombo.getSelectedItem();
        if (ontology == null) {
            return;
        }

        int col = _ontologyTable.getColumn("Node Name").getModelIndex();
        ArrayList<String> nodes = new ArrayList<String>(rows.length);

        for (int row : rows) {
            row = _ontologyTable.convertRowIndexToModel(row);
            nodes.add((String) _ontologyTable.getModel().getValueAt(row, col));
        }

        ArrayList<VNode> newNodes = new ArrayList<VNode>(nodes.size());
        for (String n : nodes) {
            newNodes.add(new VNode(n, ontology));
        }

        obj.insertRowNodes(0, newNodes);
    }

    private void _insertNodesToColumn() {
        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
        if (obj == null) {
            return;
        }

        int[] rows = _ontologyTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }

        COntology ontology = (COntology) _ontologyCombo.getSelectedItem();
        if (ontology == null) {
            return;
        }

        int col = _ontologyTable.getColumn("Node Name").getModelIndex();
        ArrayList<String> nodes = new ArrayList<String>(rows.length);

        for (int row : rows) {
            row = _ontologyTable.convertRowIndexToModel(row);
            nodes.add((String) _ontologyTable.getModel().getValueAt(row, col));
        }

        ArrayList<VNode> newNodes = new ArrayList<VNode>(nodes.size());
        for (String n : nodes) {
            newNodes.add(new VNode(n, ontology));
        }

        obj.insertColumnNodes(0, newNodes);

    }

    private void _updateOntologies() {
        List<COntology> ontologies = CoolMapMaster.getLoadedCOntologies();
        DefaultComboBoxModel model = new DefaultComboBoxModel(ontologies.toArray());
        _ontologyCombo.setModel(model);
        _updateTable();
    }

    private void _updateOntologiesAndRemove(COntology ontology) {
        List<COntology> ontologies = CoolMapMaster.getLoadedCOntologies();
        ontologies.remove(ontology);
        DefaultComboBoxModel model = new DefaultComboBoxModel(ontologies.toArray());
        _ontologyCombo.setModel(model);
        if (ontologies.size() > 0) {
            _updateTable();
        }
        else{
            _ontologyTable.setModel(new DefaultTableModel());
        }
    }

    private void _updateOntologiesAndSelect(COntology ontology) {
        List<COntology> ontologies = CoolMapMaster.getLoadedCOntologies();
        DefaultComboBoxModel model = new DefaultComboBoxModel(ontologies.toArray());
        _ontologyCombo.setModel(model);
        _ontologyCombo.setSelectedItem(ontology);
        _updateTable();
    }
}
