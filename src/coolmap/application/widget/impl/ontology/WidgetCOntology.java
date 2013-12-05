/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl.ontology;

import com.google.common.collect.Range;
import coolmap.application.CoolMapMaster;
import coolmap.application.actions.DeleteCOntologyAction;
import coolmap.application.actions.RenameCOntologyAction;
import coolmap.application.listeners.DataStorageListener;
import coolmap.application.state.StateStorageMaster;
import coolmap.application.utils.DataMaster;
import coolmap.application.widget.Widget;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.state.CoolMapState;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.table.TableColumnExt;
import org.json.JSONObject;

/**
 *
 * @author gangsu
 */
public class WidgetCOntology extends Widget implements DataStorageListener {

    private JPanel _container = new JPanel();
    private JXTable _ontologyTable = new JXTable();
    private JComboBox _ontologyCombo = new JComboBox();
    private JPopupMenu _popupMenu = new JPopupMenu();
    private JTextField _searchField = new JTextField();
    private OntologyBrowser _ontologyBrowswer = new OntologyBrowser();
    private BrowserSelectionListener _browserSelectionListener = new BrowserSelectionListener();

    private class BrowserSelectionListener implements OntologyBrowserActiveTermChangedListener {

        @Override
        public void activeTermChanged(String term, COntology ontology) {
//            System.out.println("Term changed");
            if (ontology != (COntology) _ontologyCombo.getSelectedItem()) { //this should change later -> to a private one
                return;
            }

            Integer modelRow = nodeToTableRowHash.get(term);
            if (modelRow == null) {
                return;
            }

            int viewRow = _ontologyTable.convertRowIndexToView(modelRow);
//            System.out.println(viewRow);

            _ontologyTable.getSelectionModel().setSelectionInterval(viewRow, viewRow); //This does not fire the list selection listener. great! otherwise it would be a pain
            //This will fire back; however when the two terms are equal the cicular thing is broken on the other side
            //now I just need to find this row!
            _ontologyTable.scrollRectToVisible(new Rectangle(_ontologyTable.getCellRect(viewRow, 0, true)));
        }

    }

    private class CheckListItem {

        private boolean _isSelected = false;
        String label;

        public CheckListItem(String label) {
            this.label = label;
        }

        public boolean isSelected() {
            return _isSelected;
        }

        public void setSelected(boolean isSelected) {
            _isSelected = isSelected;
        }

        @Override
        public String toString() {
            return label;
        }

    }

    private class CheckListRenderer extends JCheckBox
            implements ListCellRenderer {

        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean hasFocus) {
            setEnabled(list.isEnabled());
            setSelected(((CheckListItem) value).isSelected());
            setFont(list.getFont());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }
            setForeground(list.getForeground());
            setText(value.toString());
            return this;
        }
    }

    private JPopupMenu configPopupMenu = new JPopupMenu();

//    private class ColumnDialog extends JDialog{
//        public ColumnDialog(){
//            setTitle("Configure Ontology Table Columns");
//            setLayout(new GridBagLayout());
//        }
//        
//        public boolean showDialog
//    }
    private final Executor executor;

    public WidgetCOntology() {

        super("Ontology Table", W_DATA, L_DATAPORT, UI.getImageIcon("textList"), null);

        executor = Executors.newSingleThreadExecutor();

        _ontologyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        _ontologyTable.addHighlighter(new ColorHighlighter(new HighlightPredicate.ColumnHighlightPredicate(0),
                UI.colorKAMENOZOKI, null));
        _ontologyTable.addHighlighter(new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW,
                null, UI.colorKARAKURENAI));

//        this fails on JXTable
//        _ontologyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
//
//            @Override
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.
//
//                if (!isSelected) {
//                    int modelIndex = table.convertColumnIndexToModel(column);
//                    System.out.println(modelIndex + " " + row + "," + column + " " + value);
////                    label.setBackground(Color.BLUE);
//                    
//                    //Why the fuck this does not fucking work?
////                    if (modelIndex == 0) {
////                        label.setBackground(UI.colorLightGreen0);
////                    }
//                    
////                    if(hasFocus)
////                        label.setBackground(Color.RED);
//                    
////                    
//////                        label.setBackground(UI.colorLightGreen0);
////                    } else {
//////                        System.out.println("setting column bg to white");
////                        //why the fuck this does not work?
////                        label.setBackground(Color.BLUE);
////                    }
//                }
//
//                return label;
//            }
//
//        });
        final JButton columnCtrl = new JButton(UI.getImageIcon("gear"));
        _ontologyTable.setColumnControl(columnCtrl);
        columnCtrl.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final JList visibleColumns = new JList();
                visibleColumns.setCellRenderer(new CheckListRenderer());
                DefaultListModel model = new DefaultListModel();
                visibleColumns.setModel(model);

//                model.addElement(new CheckListItem("ABC"));
                //get all base columns
                for (int i = 0; i < _ontologyTable.getModel().getColumnCount(); i++) {
                    CheckListItem item = new CheckListItem(_ontologyTable.getModel().getColumnName(i));
                    TableColumnExt ext = _ontologyTable.getColumnExt(item.toString());
                    if (ext != null && ext.isVisible()) {
                        item.setSelected(true);
                    }
                    model.addElement(item);
                }

                visibleColumns.addMouseListener(new MouseAdapter() {

                    public void mouseClicked(MouseEvent event) {

                        if (SwingUtilities.isLeftMouseButton(event)) {
                            JList list = (JList) event.getSource();

                            // Get index of item clicked
                            int index = list.locationToIndex(event.getPoint());
                            CheckListItem item = (CheckListItem) list.getModel().getElementAt(index);

                            // Toggle selected state
                            item.setSelected(!item.isSelected());

                            // Repaint cell
                            list.repaint(list.getCellBounds(index, index));

                        }

                    }
                });

                //int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), new JScrollPane(visibleColumns), "Toggle visible ontology columns", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                JOptionPane pane = new JOptionPane(new JScrollPane(visibleColumns), JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
                JDialog dialog = pane.createDialog(CoolMapMaster.getCMainFrame(), "Configure Columns");

                Point pt = columnCtrl.getLocationOnScreen();
                pt.x -= dialog.getSize().width + 10;
                pt.y -= dialog.getSize().height + 10;
                dialog.setLocation(pt);
                dialog.setVisible(true);

                //System.out.println("selected:" + pane.getValue());
                try {
                    Object val = pane.getValue();
                    if (Integer.parseInt(val.toString()) == JOptionPane.OK_OPTION) {
                        //YEAH!
                        //System.out.println("Hide/show columns");
                        for (int i = 0; i < model.getSize(); i++) {
                            CheckListItem item = (CheckListItem) model.getElementAt(i);
                            //System.out.println("Table column:" + _ontologyTable.getColumnExt(item.toString()).getIdentifier());

                            TableColumnExt ext = _ontologyTable.getColumnExt(item.toString());

//                            System.out.println(item); //why interspaced?
                            if (ext == null) {
                                //why this is even happening
                                continue;
                            }

                            if (!item.isSelected()) {
                                ext.setVisible(false);
                            } else {
                                ext.setVisible(true);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton button;

        _ontologyTable.setColumnControlVisible(true);

//        _ontologyTable.getColumnModel().addColumnModelListener(columnListener);
        _ontologyCombo.setEnabled(false);
//        System.err.println("Ontology module updated");

        DataMaster.addDataStorageListener(this);
        getContentPane().setLayout(new BorderLayout());

        _ontologyBrowswer.addActiveTermChangedListener(_browserSelectionListener);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _ontologyBrowswer.getCanvas(), new JScrollPane(_ontologyTable));
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(350);
        getContentPane().add(splitPane, BorderLayout.CENTER);

//        System.out.println("Table updated...");
        _ontologyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

//                System.out.println("Selection changed");
                if (!e.getValueIsAdjusting()) {

                    int[] rowIndices = _ontologyTable.getSelectedRows();

                    if (rowIndices.length == 1) {
                        int row = _ontologyTable.convertRowIndexToModel(rowIndices[0]); //This could yeild arrayIndex out of bounds exception
                        //System.out.println("Node:" + _ontologyTable.getModel().getValueAt(row, 0));
                        String node = (String) _ontologyTable.getModel().getValueAt(row, 0);
//                        System.out.println("Table Selection changed");
                        _ontologyBrowswer.jumpToActiveTerm(node);

                    } else {
                        _ontologyBrowswer.jumpToActiveTerm(null);
                    }

                }
            }
        });

        _ontologyTable.setRowSelectionAllowed(true);
        _ontologyTable.setColumnSelectionAllowed(false);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        getContentPane().add(toolBar, BorderLayout.NORTH);

//        JLabel label = new JLabel(UI.getImageIcon("ontologyTop"));
//        label.setToolTipText("Choose loaded ontologies");
//        toolBar.add(label);
//        JMenu menu = new JMenu();
//        menu.setIcon(UI.getImageIcon("ontologyTop"));
//        menu;
        final JButton ontologyButton = new JButton(UI.getImageIcon("ontologyTop"));
        toolBar.add(ontologyButton);
        ontologyButton.setBorder(null);
        ontologyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        configPopupMenu.add(new RenameCOntologyAction());
        configPopupMenu.add(new DeleteCOntologyAction());

        ontologyButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                configPopupMenu.show(ontologyButton, e.getX(), e.getY());
            }

        });

        toolBar.add(_ontologyCombo);

//////////////////////////////////////////////////////////////////////////////////////////        
        _ontologyCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {      
                    _searchField.setText("");
                    _searchField.setBackground(Color.WHITE);
                    _updateTable();
                }
            }
        });

        _ontologyTable.setComponentPopupMenu(_popupMenu);

        JMenuItem item = new JMenuItem("Copy selected nodes to clipboard", UI.getImageIcon("duplicate"));
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();

                if (_ontologyCombo.getSelectedItem() == null || _ontologyTable.getSelectedRowCount() == 0) {
                    return;
                }

                //get selected rows
//                StringBuilder builder = new StringBuilder();
//                builder.append(
//                        "#OntologyID:");
//                builder.append(((COntology) _ontologyCombo.getSelectedItem()).getID());
//                builder.append("\n");
                JSONObject nodesToInsert = new JSONObject();

                ArrayList<String> list = new ArrayList();

                for (int i : _ontologyTable.getSelectedRows()) {
                    //builder.append(_ontologyTable.getModel().getValueAt(_ontologyTable.convertRowIndexToModel(i), 0)); //node name
                    //builder.append("\n");
                    list.add(_ontologyTable.getModel().getValueAt(_ontologyTable.convertRowIndexToModel(i), 0).toString());
                }

                try {
                    nodesToInsert.put("OntologyID", ((COntology) _ontologyCombo.getSelectedItem()).getID());
                    nodesToInsert.put("OntologyName", ((COntology) _ontologyCombo.getSelectedItem()).getName());
                    nodesToInsert.put("Terms", list);
                    board.setContents(new StringSelection(nodesToInsert.toString()), null);
                } catch (Exception ex) {
                    System.err.println("JSON creation exception in copying ontology nodes to clipbard");
                }

            }
        });

        _popupMenu.add(item);
//        _popupMenu.addSeparator();

//        item = new JMenuItem("Add selected nodes to View rows");
//        _popupMenu.add(item);
//        item.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                _insertNodesToRow();
//            }
//        });
//
//        item = new JMenuItem("Add selected nodes to View columns");
//        _popupMenu.add(item);
//        item.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                _insertNodesToColumn();
//            }
//        });
//        toolBar.add(new DeleteCOntologyAction());
        toolBar.addSeparator();

        button = new JButton(UI.getImageIcon("baseRow"));
        toolBar.add(button);
        button.setToolTipText("Reset view rows to original data rows");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null || obj.getBaseCMatrices().isEmpty()) {
                    return;
                }

                CMatrix mx = (CMatrix) obj.getBaseCMatrices().get(0);

                ArrayList<VNode> rowNodes = new ArrayList<VNode>();
                for (Object label : mx.getRowLabelsAsList()) {
                    rowNodes.add(new VNode(label.toString()));
                }

                CoolMapState state = CoolMapState.createStateRows("Row reset", obj, null);
                obj.replaceRowNodes(rowNodes, null);
                StateStorageMaster.addState(state);

            }
        });

        button = new JButton(UI.getImageIcon("baseColumn"));
        toolBar.add(button);
        button.setToolTipText("Rest view columns to original data columns");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null || obj.getBaseCMatrices().isEmpty()) {
                    return;
                }

                CMatrix mx = (CMatrix) obj.getBaseCMatrices().get(0);

                ArrayList<VNode> colNodes = new ArrayList<VNode>();
                for (Object label : mx.getColLabelsAsList()) {
                    colNodes.add(new VNode(label.toString()));
                }

                CoolMapState state = CoolMapState.createStateColumns("Column reset", obj, null);
                obj.replaceColumnNodes(colNodes, null);
                StateStorageMaster.addState(state);

            }
        });

        toolBar.addSeparator();
        button = new JButton(UI.getImageIcon("prependRow"));
        toolBar.add(button);
        button.setToolTipText("Prepend selected ontology nodes to rows");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                _prependRows();
            }
        });

        button = new JButton(UI.getImageIcon("appendRow"));
        button.setToolTipText("Append selected ontology nodes to rows");
        toolBar.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _appendRows();
            }
        });

        button = new JButton(UI.getImageIcon("replaceRow"));
        button.setToolTipText("Replace rows with selected row nodes");
        toolBar.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _replaceRows();
            }
        });

        button = new JButton(UI.getImageIcon("rootRow"));
        button.setToolTipText("Replace rows with the root ontology nodes");
        toolBar.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null || obj.getBaseCMatrices().isEmpty()) {
                    return;
                }

                COntology ontology = (COntology) _ontologyCombo.getSelectedItem();
                if (ontology == null) {
                    return;
                }

                CoolMapState state = CoolMapState.createStateRows("Row replacement", obj, null);
                obj.replaceRowNodes(ontology.getRootNodesOrdered(), null);
                StateStorageMaster.addState(state);
            }
        });

        toolBar.addSeparator();

        button = new JButton(UI.getImageIcon("prependColumn"));
        button.setToolTipText("Add selected nodes to columns in the active CoolMap, at the beginning");
        button.setToolTipText("Prepend selected ontology nodes to columns");
        toolBar.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                _prependColumns();
            }
        });

        button = new JButton(UI.getImageIcon("appendColumn"));
        button.setToolTipText("Append selected ontology nodes to columns");
        toolBar.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _appendColumns();
            }
        });

        button = new JButton(UI.getImageIcon("replaceColumn"));
        button.setToolTipText("Replace columns with selected column nodes");
        toolBar.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _replaceColumns();
            }
        });

        button = new JButton(UI.getImageIcon("rootColumn"));
        button.setToolTipText("Replace columns with the root ontology nodes");
        toolBar.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null || obj.getBaseCMatrices().isEmpty()) {
                    return;
                }

                COntology ontology = (COntology) _ontologyCombo.getSelectedItem();
                if (ontology == null) {
                    return;
                }

                CoolMapState state = CoolMapState.createStateColumns("Column replacement", obj, null);
                obj.replaceColumnNodes(ontology.getRootNodesOrdered(), null);
                StateStorageMaster.addState(state);
            }
        });

        toolBar.addSeparator();
        JLabel label = new JLabel();

        label.setToolTipText("\"<html>Type in terms in the current active view.<br/>Use <strong>|</strong> as 'OR' operator to separate terms</html>\"");

        toolBar.add(label);

//        toolBar = new JToolBar();
        label = new JLabel(UI.getImageIcon("search"));
        toolBar.add(label);
//        getContentPane().add(toolBar, BorderLayout.SOUTH);
        toolBar.setFloatable(false);

        toolBar.add(_searchField);
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
        
        //_searchField.setToolTipText("Hit the Enter key to update the ontology table based on the current search terms\nWhen the search box is yellow, an update is required by the user");
        
        _searchField.setToolTipText("<html>Type in terms and hit <strong>Enter</strong> to search.<br/> "
                + "A yellow background indicates that an update is required (<strong>Enter</strong>) <br/>"
                + "Use spaces for multiple terms"
                + "Use | for OR, example: <strong>term1|term2</strong>"
                + "</html>");
        
        _searchField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _filterTable();
            }
        });
        
        _ontologyTable.setAutoCreateRowSorter(true);
    }

    private void _filterTable() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                String text = _searchField.getText();
                if (text == null || text.length() == 0) {
                    _searchField.setBackground(Color.WHITE);
                    if (_ontologyTable.getRowSorter() == null) {
                        return;
                    }

                    ((DefaultRowSorter) _ontologyTable.getRowSorter()).setRowFilter(null);

                } else {
                    try {

                        //Some more work with the filter for multiple terms
                        _searchField.setBackground(Color.WHITE);
                        String ele[] = text.trim().split("\\s+");
                        if (ele.length == 1) {
                            ((DefaultRowSorter) _ontologyTable.getRowSorter()).setRowFilter(RowFilter.regexFilter("(?i)" + ele[0]));
                        } else {

                            HashSet<RowFilter<Object, Object>> filters = new HashSet<>();

                            for (String term : ele) {
                                filters.add(RowFilter.regexFilter("(?i)" + term));
                            }

                            ((DefaultRowSorter) _ontologyTable.getRowSorter()).setRowFilter(RowFilter.andFilter(filters));
                        }

                    } catch (Exception e) {

                        //e.printStackTrace();
                        _searchField.setBackground(UI.colorRedWarning);

                        //e.printStackTrace();
                        if (_ontologyTable.getRowSorter() == null) {
                            return;
                        }
                        ((DefaultRowSorter) _ontologyTable.getRowSorter()).setRowFilter(null);

                    }
                }
            }
        });

        //add a default header
    }

    private ArrayList<String> tableHeaders = new ArrayList<String>();

    @Override
    public void coolMapObjectAdded(CoolMapObject newObject) {
        newObject.getCoolMapView(); //

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

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                if (_ontologyCombo.getSelectedItem() == null) {
                    _ontologyTable.setModel(new DefaultTableModel());
                    nodeToTableRowHash.clear();
                }
                COntology ontology = (COntology) _ontologyCombo.getSelectedItem();

                //make sure the setting new model won't affect the last recorded state
//                _ontologyTable.getColumnModel().removeColumnModelListener(columnListener);
                DefaultTableModel model = _getOntologyAsTableModel(ontology);
                //maintain the same column model layout if the last one has
                TableColumnModel columnModel = _ontologyTable.getColumnModel();
                TableModel currentTableModel = _ontologyTable.getModel();

                ArrayList<String> order = new ArrayList<String>();
                for (int i = 0; i < columnModel.getColumnCount(); i++) {
                    TableColumn col = columnModel.getColumn(i);
//                        System.out.println(col + cold);
                    order.add(col.getIdentifier().toString());
                }

                HashSet<String> visibles = new HashSet<String>();
                visibles.addAll(order);

                //This would contain everything
                _ontologyTable.setModel(model);

                _ontologyBrowswer.setActiveCOntology(ontology);

                _ontologyTable.setColumnModel(columnModel); //use the same column model OH I C!

                if (currentTableModel.getColumnCount() >= 1) {
                    //ensure it's a valid model esp for the first time
                    //obtain an order

//                    System.out.println(order);
//                    TableColumnModel newModel = _ontologyTable.getColumnModel();
//                    System.out.println("Visible columns:" + visibles);
                    HashSet<TableColumnExt> toBeHidden = new HashSet<TableColumnExt>();
                    for (int i = 0; i < _ontologyTable.getColumnCount(true); i++) {
                        TableColumnExt colExt = _ontologyTable.getColumnExt(i);

                        if (!visibles.contains(colExt.getIdentifier())) {
                            //colExt.setVisible(false);
                            toBeHidden.add(colExt);
                        }
                    }

                    for (TableColumnExt ext : toBeHidden) {
                        ext.setVisible(false);
                    }

                    HashMap<String, TableColumn> columns = new HashMap<String, TableColumn>();
//                    columns.put(colExt.getIdentifier().toString(), _ontologyTable.getColumn(i));
                    while (_ontologyTable.getColumnCount() > 0) {
                        TableColumn col = _ontologyTable.getColumn(0);
                        columns.put(col.getIdentifier().toString(), col);
                        _ontologyTable.removeColumn(col);
                    }
////
////                    //add these ones back
                    for (String label : order) {
                        _ontologyTable.addColumn(columns.get(label));
                    }

                    //This works, but the hidden ones are gone           
                }

//                for(int i=0; i<_ontologyTable.getColumnCount(); i++){
//                    TableColumnExt column = _ontologyTable.getColumnExt(i);
//                    if(columnListener.hiddenColumns.contains(column.getIdentifier())){
//                        column.setVisible(false); //make it invisible if it was invisible previously
//                    }
//                }
                //set it back
//                _ontologyTable.getColumnModel().addColumnModelListener(columnListener);
//                _ontologyTable.getColumnExt(0).setVisible(false); //this is the way to hide it
            }
        });

        //System.out.println("Table needs to be updated.");
//        _ontologyTable.removeColumn(_ontologyTable.getColumn("ChildCount"));
        //Throws an execption
        //System.out.println("Column exists?" + _ontologyTable.getColumn("ChildCount"));
        //So this actually works.
//        System.out.println("Child count after remvoal:" + _ontologyTable.getModel().getValueAt(0, 1));
//        ((TableRowSorter) _ontologyTable.getRowSorter()).setComparator(1, new Comparator<Integer>() {
//            
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                if (o1 < o2) {
//                    return -1;
//                }
//                if (o1 == o2) {
//                    return 0;
//                } else {
//                    return 1;
//                }
//            }
//        });
//        
////        ((TableRowSorter) _ontologyTable.getRowSorter()).setCom
//        
//        
//        ((TableRowSorter) _ontologyTable.getRowSorter()).setComparator(3, new Comparator<Integer>() {
//            
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                if (o1 < o2) {
//                    return -1;
//                }
//                if (o1 == o2) {
//                    return 0;
//                } else {
//                    return 1;
//                }
//            }
//        });
//        
//        ((TableRowSorter) _ontologyTable.getRowSorter()).setComparator(5, new Comparator<Integer>() {
//            
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                if (o1 < o2) {
//                    return -1;
//                }
//                if (o1 == o2) {
//                    return 0;
//                } else {
//                    return 1;
//                }
//            }
//        });
    }

    private OntologyTableColumnListener columnListener = new OntologyTableColumnListener();

    private class OntologyTableColumnListener implements TableColumnModelListener {

//        public HashSet<String> hiddenColumns = new HashSet<String>();
        @Override
        public void columnAdded(TableColumnModelEvent e) {
//            System.out.println("column added: " + e.getFromIndex() + " " + e.getToIndex());

//            hiddenColumns.clear();
//            HashSet<String> modelColumns = new HashSet<String>();
//            for(int i=0; i<_ontologyTable.getModel().getColumnCount(); i++){
//                modelColumns.add(_ontologyTable.getModel().getColumnName(i));
//            }
//            
//            HashSet<String> viewColumns = new HashSet<String>();
//            for(int i=0; i<_ontologyTable.getColumnModel().getColumnCount(); i++){
//                viewColumns.add(_ontologyTable.getColumnModel().getColumn(i).getHeaderValue().toString());
//            }
//            
//            modelColumns.removeAll(viewColumns);
//            
//            hiddenColumns.addAll(modelColumns);
//            System.out.println("column added: hidden ones:" + modelColumns);
        }

        @Override
        public void columnRemoved(TableColumnModelEvent e) {
//            System.out.println("column removed: " + e.getFromIndex() + " " + e.getToIndex());
//            hiddenColumns.clear();
//                        hiddenColumns.clear();
//            HashSet<String> modelColumns = new HashSet<String>();
//            for(int i=0; i<_ontologyTable.getModel().getColumnCount(); i++){
//                modelColumns.add(_ontologyTable.getModel().getColumnName(i));
//            }
//            
//            HashSet<String> viewColumns = new HashSet<String>();
//            for(int i=0; i<_ontologyTable.getColumnModel().getColumnCount(); i++){
//                viewColumns.add(_ontologyTable.getColumnModel().getColumn(i).getHeaderValue().toString());
//            }
//            
//            modelColumns.removeAll(viewColumns);
//            
//            hiddenColumns.addAll(modelColumns);
//            System.out.println("column removed: hidden ones" + modelColumns);

        }

        @Override
        public void columnMoved(TableColumnModelEvent e) {
        }

        @Override
        public void columnMarginChanged(ChangeEvent e) {
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {
        }

    }

    private class OntologyTableModel extends DefaultTableModel {

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 1 || columnIndex == 3 || columnIndex == 5) {
                return Integer.class;
            } else {
                return Object.class;
            }
        }

        public OntologyTableModel(Object[][] data, String[] headers) {
            super(data, headers);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            //return super.isCellEditable(row, column); //To change body of generated methods, choose Tools | Templates.
            return false;
        }

    }

    private static final String NODE_NAME = "Node Name";
    private static final String CHILD_COUNT = "Child Count";
    private static final String PARENT_COUNT = "Parent Count";
    private static final String CHILD_NODES = "Child Nodes";
    private static final String PARENT_NODES = "Parent Nodes";
    private static final String DEPTH = "Depth";

    private DefaultTableModel _getOntologyAsTableModel(COntology ontology) {
        //Shows how rows 
        nodeToTableRowHash.clear();

        HashSet<String> nodes = new HashSet<String>();
        nodes.addAll(ontology.getAllNodesWithChildren());
        nodes.addAll(ontology.getAllNodesWithParents());
        ArrayList<String> sortedNodes = new ArrayList<String>();
        sortedNodes.addAll(nodes);
        Collections.sort(sortedNodes);

        tableHeaders.clear();
        tableHeaders.add(NODE_NAME);
        tableHeaders.add(CHILD_COUNT);
        tableHeaders.add(CHILD_NODES);
        tableHeaders.add(PARENT_COUNT);
        tableHeaders.add(PARENT_NODES);
        tableHeaders.add(DEPTH);

        List<String> attributeNames = COntology.getAttributeNames();
        tableHeaders.addAll(attributeNames);

        String[] headers = new String[tableHeaders.size()];
        tableHeaders.toArray(headers);

        //String[] headers = new String[]{NODE_NAME, CHILD_COUNT, CHILD_NODES, PARENT_COUNT, PARENT_NODES, DEPTH};
        Object[][] data = new Object[nodes.size()][tableHeaders.size()];
        for (int i = 0; i < data.length; i++) {
            String node = sortedNodes.get(i);
            List<String> child = ontology.getImmediateChildren(node);
            List<String> parent = ontology.getImmediateParents(node);

            data[i][0] = node;
            data[i][1] = child == null ? 0 : child.size();
            data[i][2] = (child == null || child.isEmpty()) ? "" : Arrays.toString(child.toArray());
            data[i][3] = parent == null ? 0 : parent.size();
            data[i][4] = (parent == null || parent.isEmpty()) ? "" : Arrays.toString(parent.toArray());
            data[i][5] = ontology.getMinimalDepthFromLeaves(node);

            if (attributeNames == null || attributeNames.isEmpty()) {
                continue;
            }

            int offset = 6;
            for (int k = 0; k < attributeNames.size(); k++) {
                Object value = COntology.getAttribute(node, attributeNames.get(k));
                if (value != null) {
                    data[i][k + 6] = value;
                }
            }

            nodeToTableRowHash.put(node, i);
        }

        OntologyTableModel model = new OntologyTableModel(data, headers);

        //Also needs to create a hash for nodes
        return model;
    }

    private final HashMap<String, Integer> nodeToTableRowHash = new HashMap<>();

    private void _prependRows() {
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

//        int col = _ontologyTable.getColumn("Node Name").getModelIndex();
        ArrayList<String> nodes = new ArrayList<String>(rows.length);

        for (int row : rows) {
            row = _ontologyTable.convertRowIndexToModel(row);
            nodes.add((String) _ontologyTable.getModel().getValueAt(row, 0));
        }

        ArrayList<VNode> newNodes = new ArrayList<VNode>(nodes.size());
        for (String n : nodes) {
            newNodes.add(new VNode(n, ontology));
        }

        Rectangle centerTo = new Rectangle(0, 0, 1, 1);
        if (obj.getCoolMapView().getSelectedColumns() != null && !obj.getCoolMapView().getSelectedColumns().isEmpty()) {
            centerTo.x = ((Range<Integer>) (obj.getCoolMapView().getSelectedColumns().get(0))).lowerEndpoint();
        }

        CoolMapState state = CoolMapState.createStateRows("Prepend nodes to row", obj, null);
        obj.insertRowNodes(0, newNodes, true);
//        obj.getCoolMapView().centerToRegion(centerTo);
        obj.getCoolMapView().centerToSelections();
        StateStorageMaster.addState(state);

    }

    private void _appendRows() {
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

//        int col = _ontologyTable.getColumn("Node Name").getModelIndex();
        ArrayList<String> nodes = new ArrayList<String>(rows.length);

        for (int row : rows) {
            row = _ontologyTable.convertRowIndexToModel(row);
            nodes.add((String) _ontologyTable.getModel().getValueAt(row, 0));
        }

        ArrayList<VNode> newNodes = new ArrayList<VNode>(nodes.size());
        for (String n : nodes) {
            newNodes.add(new VNode(n, ontology));
        }

//        Rectangle centerTo = new Rectangle(0, 0, 1, 1);
//        if (obj.getCoolMapView().getSelectedColumns() != null && !obj.getCoolMapView().getSelectedColumns().isEmpty()) {
//            centerTo.x = ((Range<Integer>) (obj.getCoolMapView().getSelectedColumns().get(0))).lowerEndpoint();
//        }
        CoolMapState state = CoolMapState.createStateRows("Append nodes to row", obj, null);
        obj.insertRowNodes(obj.getViewNumRows(), newNodes, true);
        obj.getCoolMapView().centerToSelections();
        StateStorageMaster.addState(state);

    }

    private void _replaceRows() {
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

//        int col = _ontologyTable.getColumn("Node Name").getModelIndex();
        ArrayList<String> nodes = new ArrayList<String>(rows.length);

        for (int row : rows) {
            row = _ontologyTable.convertRowIndexToModel(row);
            nodes.add((String) _ontologyTable.getModel().getValueAt(row, 0));
        }

        ArrayList<VNode> newNodes = new ArrayList<VNode>(nodes.size());
        for (String n : nodes) {
            newNodes.add(new VNode(n, ontology));
        }

//        Rectangle centerTo = new Rectangle(0, 0, 1, 1);
//        if (obj.getCoolMapView().getSelectedColumns() != null && !obj.getCoolMapView().getSelectedColumns().isEmpty()) {
//            centerTo.x = ((Range<Integer>) (obj.getCoolMapView().getSelectedColumns().get(0))).lowerEndpoint();
//        }
        CoolMapState state = CoolMapState.createStateRows("Replace row nodes", obj, null);
        int index = 0;
        try {
            index = ((Range<Integer>) obj.getCoolMapView().getSelectedRows().get(0)).lowerEndpoint();
        } catch (Exception e) {

        }

        obj.replaceRowNodes(newNodes, null);
        obj.getCoolMapView().centerToSelections();
        StateStorageMaster.addState(state);

    }

    private void _replaceColumns() {

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

//        int col = _ontologyTable.getColumn("Node Name").getModelIndex();
        ArrayList<String> nodes = new ArrayList<String>(rows.length);

        for (int row : rows) {
            row = _ontologyTable.convertRowIndexToModel(row);
            nodes.add((String) _ontologyTable.getModel().getValueAt(row, 0));
        }

        ArrayList<VNode> newNodes = new ArrayList<VNode>(nodes.size());
        for (String n : nodes) {
            newNodes.add(new VNode(n, ontology));
        }

//        Rectangle centerTo = new Rectangle(0, 0, 1, 1);
//        if (obj.getCoolMapView().getSelectedRows() != null && !obj.getCoolMapView().getSelectedRows().isEmpty()) {
//            centerTo.y = ((Range<Integer>) (obj.getCoolMapView().getSelectedRows().get(0))).lowerEndpoint();
//        }
        CoolMapState state = CoolMapState.createStateColumns("Replace column nodes", obj, null);
        obj.replaceColumnNodes(newNodes, null);
//        obj.getCoolMapView().centerToSelections();
        StateStorageMaster.addState(state);

    }

    private void _appendColumns() {

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

//        int col = _ontologyTable.getColumn("Node Name").getModelIndex();
        ArrayList<String> nodes = new ArrayList<String>(rows.length);

        for (int row : rows) {
            row = _ontologyTable.convertRowIndexToModel(row);
            nodes.add((String) _ontologyTable.getModel().getValueAt(row, 0));
        }

        ArrayList<VNode> newNodes = new ArrayList<VNode>(nodes.size());
        for (String n : nodes) {
            newNodes.add(new VNode(n, ontology));
        }

//        Rectangle centerTo = new Rectangle(0, 0, 1, 1);
//        if (obj.getCoolMapView().getSelectedRows() != null && !obj.getCoolMapView().getSelectedRows().isEmpty()) {
//            centerTo.y = ((Range<Integer>) (obj.getCoolMapView().getSelectedRows().get(0))).lowerEndpoint();
//        }
        CoolMapState state = CoolMapState.createStateColumns("Append nodes to column", obj, null);
        obj.insertColumnNodes(obj.getViewNumColumns(), newNodes, true);
        obj.getCoolMapView().centerToSelections();
        StateStorageMaster.addState(state);

        //
    }

    private void _prependColumns() {

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

//        int col = _ontologyTable.getColumn("Node Name").getModelIndex();
        ArrayList<String> nodes = new ArrayList<String>(rows.length);

        for (int row : rows) {
            row = _ontologyTable.convertRowIndexToModel(row);
            nodes.add((String) _ontologyTable.getModel().getValueAt(row, 0));
        }

        ArrayList<VNode> newNodes = new ArrayList<VNode>(nodes.size());
        for (String n : nodes) {
            newNodes.add(new VNode(n, ontology));
        }

        Rectangle centerTo = new Rectangle(0, 0, 1, 1);
        if (obj.getCoolMapView().getSelectedRows() != null && !obj.getCoolMapView().getSelectedRows().isEmpty()) {
            centerTo.y = ((Range<Integer>) (obj.getCoolMapView().getSelectedRows().get(0))).lowerEndpoint();
        }

        CoolMapState state = CoolMapState.createStateColumns("Prepend nodes to column", obj, null);
        obj.insertColumnNodes(0, newNodes, true);
//        obj.getCoolMapView().centerToRegion(centerTo);
        obj.getCoolMapView().centerToSelections();
        StateStorageMaster.addState(state);

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
        _ontologyBrowswer.setActiveCOntology((COntology) _ontologyCombo.getSelectedItem());
        if (ontologies.size() > 0) {
            _updateTable(); //rebuild
        } else {
            _ontologyTable.setModel(new DefaultTableModel());
            nodeToTableRowHash.clear();
        }
    }

    private void _updateOntologiesAndSelect(COntology ontology) {
        List<COntology> ontologies = CoolMapMaster.getLoadedCOntologies();
        DefaultComboBoxModel model = new DefaultComboBoxModel(ontologies.toArray());
        _ontologyCombo.setModel(model);
        _ontologyCombo.setSelectedItem(ontology);
        _ontologyBrowswer.setActiveCOntology(ontology);
        _updateTable();
        _ontologyCombo.setEnabled(true);
    }

}
