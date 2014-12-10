/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.CMatrixListener;
import coolmap.application.listeners.DataStorageListener;
import coolmap.application.utils.DataMaster;
import coolmap.application.utils.Messenger;
import coolmap.application.widget.Widget;
import coolmap.application.widget.WidgetMaster;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.canvas.sidemaps.impl.ColumnLabels;
import coolmap.canvas.sidemaps.impl.ColumnTree;
import coolmap.canvas.sidemaps.impl.RowLabels;
import coolmap.canvas.sidemaps.impl.RowTree;
import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.snippet.SnippetMaster;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author gangsu
 */
public class WidgetCMatrix extends Widget implements DataStorageListener, CMatrixListener {

    private JToolBar _toolBar = new JToolBar();
//    private JPanel _newCoolMapObjectPanel = new JPanel();
    private JComboBox _aggregators = new JComboBox();
    private JComboBox _renderer = new JComboBox();
    private JTextField _name = new JTextField();

    private JTable matrixTable = new JTable();

    public List<CMatrix> getSelectedMatrices() {

        int[] selectedRows = matrixTable.getSelectedRows();
        ArrayList<CMatrix> matrices = new ArrayList<>();
        for (int row : selectedRows) {

            int index = matrixTable.convertRowIndexToModel(row);
            try {
                String ID = matrixTable.getModel().getValueAt(index, 0).toString();
                CMatrix mx = CoolMapMaster.getCMatrixByID(ID);
                if (mx != null) {
                    matrices.add(mx);
                }
            } catch (Exception e) {

            }
        }
        return matrices;
    }

    private void renameSelected() {
        List<CMatrix> selectedMatrices = getSelectedMatrices();
        //need a OptionPane for new names - and other widgets need to be updated ugh
        String returnVal = JOptionPane.showInputDialog(CoolMapMaster.getCMainFrame(), "Please provide a new name:");
        if (returnVal == null || returnVal.length() == 0) {
            returnVal = "Untitled";
        }

//        System.out.println(returnVal);
        int counter = 0;
        for (CMatrix matrix : selectedMatrices) {
            if (counter == 0) {
                CoolMapMaster.renameCMatrix(matrix.getID(), returnVal);
            } else {
                CoolMapMaster.renameCMatrix(matrix.getID(), returnVal + "_" + counter);
            }
            counter++;
        }
    }

    private void removeSelected() {
        List<CMatrix> selectedMatrices = getSelectedMatrices();
        CoolMapMaster.destroyCMatrices(selectedMatrices);
    }





    private boolean canBeGrouped(List<CMatrix> matrices) {
        if (matrices == null || matrices.isEmpty()) {
            return false;
        } else if (matrices.size() == 1) {
            return true;
        } else {
            CMatrix matrix1 = matrices.get(0);

            for (int i = 1; i < matrices.size(); i++) {
                CMatrix mx = matrices.get(i);
                if (!matrix1.canBeGroupedTogether(mx)) {
                    return false;
                }
            }
            return true;
        }
    }

    private void createView() {
        List<CMatrix> selectedMatrices = getSelectedMatrices();
        if (selectedMatrices.isEmpty()) {
            Messenger.showWarningMessage("Please select matrices to continue.", "Empty selection");
            return;
        }

        CMatrix[] matrices = new CMatrix[selectedMatrices.size()];
        for (int i = 0; i < selectedMatrices.size(); i++) {
            matrices[i] = selectedMatrices.get(i);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 5;
        c.ipady = 5;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("CoolMap Name:"), c);
        c.gridx = 1;
        panel.add(_name, c);
        c.gridy++;
        c.gridx = 0;
        panel.add(new JLabel("Aggregator:"), c);
        c.gridx = 1;
        panel.add(_aggregators, c);
        c.gridy++;
        c.gridx = 0;

        panel.add(new JLabel("Renderer:"), c);
        c.gridx = 1;
        panel.add(_renderer, c);

        _name.setText("Untitiled");
        _name.setColumns(20);

        CoolMapObject object = new CoolMapObject();
        object.addBaseCMatrix(matrices);

        try {
            WidgetAggregator aggrWidget = (WidgetAggregator) WidgetMaster.getWidget(WidgetAggregator.class.getName());
            LinkedHashSet<CAggregator> aggrs = aggrWidget.getLoadedRenderers();
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (CAggregator aggr : aggrs) {
                if (aggr.canAggregate(object.getBaseClass())) {
                    model.addElement(aggr);
                }
            }
            _aggregators.setModel(model);

        } catch (Exception e) {

        }

//        JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), panel);
        int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), panel, "Config View", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (returnVal == JOptionPane.OK_OPTION) {

            CMatrix m0 = matrices[0];
            ArrayList<VNode> nodes = new ArrayList<>();
            for (int i = 0; i < m0.getNumRows(); i++) {
                nodes.add(new VNode(m0.getRowLabel(i)));
            }
            object.insertRowNodes(0, nodes, false);

            nodes.clear();
            for (int i = 0; i < m0.getNumColumns(); i++) {
                nodes.add(new VNode(m0.getColLabel(i)));
            }
            object.insertColumnNodes(0, nodes, false);

            //need a dialog
            try {
                object.setAggregator((CAggregator) (_aggregators.getSelectedItem().getClass().newInstance()));
                object.setViewRenderer((ViewRenderer) (_renderer.getSelectedItem().getClass().newInstance()), true);
                if (Double.class.isAssignableFrom(object.getViewClass())) {
                    object.setSnippetConverter(SnippetMaster.getConverter("D13"));
                }
            } catch (InstantiationException | IllegalAccessException e) {
                CMConsole.logError(e.getMessage());
            }
            object.setName("Untitiled");

            object.getCoolMapView().addColumnMap(new ColumnLabels(object));
            object.getCoolMapView().addColumnMap(new ColumnTree(object));

            object.getCoolMapView().addRowMap(new RowLabels(object));
            object.getCoolMapView().addRowMap(new RowTree(object));

            CoolMapMaster.addNewCoolMapObject(object);

        }
    }



    private void initPopupmenu() {
        JPopupMenu menu = new JPopupMenu();
        matrixTable.setComponentPopupMenu(menu);

        JMenuItem rename = new JMenuItem("Rename selected");
        menu.add(rename);
        rename.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                renameSelected();
            }
        });

        JMenuItem view = new JMenuItem("View selected");
        menu.add(view);
        view.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                view();
            }
        });

        JMenuItem delete = new JMenuItem("Remove selected");
        menu.add(delete);
        delete.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelected();
            }
        });

        JMenuItem createView = new JMenuItem("Create view from selected");
        menu.add(createView);
        createView.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                createView();
            }
        });

        //These should not be putted here. move to plugin.
        
//        menu.addSeparator();
//        JMenu create = new JMenu("Create transformed from selected");
//        menu.add(create);

        
//        JMenuItem createLog2 = new JMenuItem("log2");
//        create.add(createLog2);
//        createLog2.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                createLogTransform(2.0);
//            }
//        });
//
//        JMenuItem createLog10 = new JMenuItem("log10");
//        create.add(createLog10);
//        createLog10.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                createLogTransform(10.0);
//            }
//        });

//        JMenuItem createZ = new JMenuItem("z");
//        create.add(createZ);
//        createZ.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                createZTransform();
//            }
//        });

//        JMenuItem createRange = new JMenuItem("0-1 range");
//        create.add(createRange);
//        createRange.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                createRangeTransform();
//            }
//        });

//        JMenu aggregate = new JMenu("Create aggregate from selected");
//        menu.add(aggregate);

//        JMenuItem sum = new JMenuItem("sum");
//        aggregate.add(sum);
//        sum.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                sum();
//            }
//        });

//        JMenuItem avg = new JMenuItem("average");
//        aggregate.add(avg);
//        avg.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                average();
//            }
//        });

//        JMenuItem diff = new JMenuItem("difference (1-2)");
//        aggregate.add(diff);
//        diff.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                difference();
//            }
//        });

    }





    private void view() {
        List<CMatrix> selectedMatrices = getSelectedMatrices();
        if (selectedMatrices.isEmpty()) {
            Messenger.showWarningMessage("Please select datasets to continue.", "Invalid selection");
            return;
        }

        for (final CMatrix mx : selectedMatrices) {
            //
            Object[][] data = new Object[mx.getNumRows()][mx.getNumColumns() + 1];
            for (int i = 0; i < mx.getNumRows(); i++) {
                data[i][0] = mx.getRowLabel(i);
                for (int j = 0; j < mx.getNumColumns(); j++) {
                    data[i][j + 1] = mx.getValue(i, j);
                }
            }

            String[] header = new String[mx.getNumColumns() + 1];
            header[0] = "Row/Column";

            for (int i = 0; i < mx.getNumColumns(); i++) {
                header[i + 1] = mx.getColLabel(i);
            }

            final DefaultTableModel model = new DefaultTableModel(data, header) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; //To change body of generated methods, choose Tools | Templates.
                }

            };

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {

                    JTable matrixPreviewTable = new JTable();
                    matrixPreviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    matrixPreviewTable.getTableHeader().setReorderingAllowed(false);
                    matrixPreviewTable.setModel(model);
                    matrixPreviewTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){

                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            JLabel l = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
                            
                            if(isSelected){
                                return l;
                            }
                            
                            if(column == 0){
                                l.setBackground(UI.colorLightBlue0);
                            }
                            else{
                                l.setBackground(UI.colorWhite);
                            }
                            //To change body of generated methods, choose Tools | Templates.
                            return l;
                        }
                        
                    });
                    JOptionPane.showMessageDialog(
                            CoolMapMaster.getCMainFrame(),
                            new JScrollPane(matrixPreviewTable),
                            "Preview of: " + mx,
                            JOptionPane.PLAIN_MESSAGE
                    );
                }
            });
        }

    }


    public WidgetCMatrix() {
        super("Imported data", W_MODULE, L_LEFTTOP, UI.getImageIcon(""), "Imported data");
        DataMaster.addDataStorageListener(this);
        DataMaster.addCMatrixListener(this);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(matrixTable), BorderLayout.CENTER);
        matrixTable.setAutoCreateRowSorter(true);
        matrixTable.getTableHeader().setReorderingAllowed(false);

        initPopupmenu();
//        _toolBar.setFloatable(false);
//        getContentPane().add(_toolBar, BorderLayout.NORTH);
//
//
        _aggregators.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
//                    DefaultComboBoxModel model = new DefaultComboBoxModel();
//                    System.out.println("ItemStatedChanged");
                    if (_aggregators.getSelectedItem() == null || !(_aggregators.getSelectedItem() instanceof CAggregator)) {
                        _renderer.setModel(new DefaultComboBoxModel());
                        return;
                    }
                    try {
                        //WidgetMaster.getWidget("coolmap.application.widget.impl.WidgetCMatrix");
                        DefaultComboBoxModel model = new DefaultComboBoxModel();
                        WidgetViewRenderer rendererWidget = (WidgetViewRenderer) WidgetMaster.getWidget(WidgetViewRenderer.class.getName());
                        LinkedHashSet<ViewRenderer> renderers = rendererWidget.getLoadedRenderers();
                        for (ViewRenderer renderer : renderers) {
                            if (renderer.canRender(((CAggregator) _aggregators.getSelectedItem()).getViewClass())) {
                                model.addElement(renderer);
                            }
                        }
//                        System.out.println("Renderer set model");
                        _renderer.setModel(model);
                    } catch (Exception e) {
                    }
                }
            }
        });
//
        _aggregators.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
                JLabel label = (JLabel) super.getListCellRendererComponent(jlist, o, i, bln, bln1);
                if (o == null) {
                    return label;
                }
                if (CoolMapMaster.getActiveCoolMapObject() != null) {
                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    //CAggregator a = obj.getAggregator();
                    //System.out.println("label disable:" + a.canAggregate(obj.getBaseClass()) + " " + obj.getBaseClass());
                    CAggregator a = (CAggregator) o;
                    if (a.canAggregate(obj.getBaseClass())) {
//                        System.out.println("pass:" + o.getClass() + obj.getBaseClass());
                        label.setEnabled(true);
                        label.setFocusable(true);
                    } else {
                        label.setEnabled(false);
                        label.setFocusable(false);
                        label.setBackground(UI.colorRedWarning);
                    }
                }

                try {
                    String displayName = ((CAggregator) o).getName();
                    label.setText(displayName);
                } catch (Exception e) {
                    label.setText(o.getClass().getSimpleName());
                }
                return label;
            }
        });
//
        _renderer.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
                JLabel label = (JLabel) super.getListCellRendererComponent(jlist, o, i, bln, bln1);
                if (o == null) {
                    return label;
                }

                if (CoolMapMaster.getActiveCoolMapObject() != null) {
                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    ViewRenderer renderer = (ViewRenderer) o;
                    if (renderer.canRender(obj.getViewClass())) {
                        label.setEnabled(true);
                        label.setFocusable(true);
                    } else {
                        label.setEnabled(false);
                        label.setFocusable(false);
                        label.setBackground(UI.colorRedWarning);
                    }
                }
                try {
                    String displayName = ((ViewRenderer) o).getName();
                    label.setText(displayName);
                } catch (Exception e) {
                    label.setText(o.getClass().getSimpleName());
                }

                return label;

            }
        });
//        
//        
//        JButton button = new JButton(UI.getImageIcon("screen"));
//        button.setToolTipText("Create view from selected matrices. Selected matrices must be of the same data type and row/column layout.");
//        _toolBar.add(button);
//
//        button.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                if (_baseMatrices.getSelectedIndices() == null || _baseMatrices.getSelectedIndices().length == 0) {
//                    return;
//                }
//                int[] selectedIndices = _baseMatrices.getSelectedIndices();
//                CMatrix[] matrices = new CMatrix[selectedIndices.length];
//                Object[] values = _baseMatrices.getSelectedValues();
//                int counter = 0;
//                for (int i = 0; i < values.length; i++) {
//                    matrices[i] = (CMatrix) values[i];
//                }
//
//                if (matrices.length > 1) {
//                    CMatrix m0 = matrices[0];
//                    for (int i = 1; i < matrices.length; i++) {
//                        if (!matrices[i].canBeGroupedTogether(m0)) {
//                            return;
//                        }
//                    }
//                }
//
////                System.out.println("Create new CoolMapObject");
//                JPanel panel = new JPanel();
//                panel.setLayout(new GridBagLayout());
//                panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//                GridBagConstraints c = new GridBagConstraints();
//                c.fill = GridBagConstraints.HORIZONTAL;
//                c.ipadx = 5;
//                c.ipady = 5;
//
//                c.gridx = 0;
//                c.gridy = 0;
//                panel.add(new JLabel("CoolMap Name:"), c);
//                c.gridx = 1;
//                panel.add(_name, c);
//                c.gridy++;
//                c.gridx = 0;
//                panel.add(new JLabel("Aggregator:"), c);
//                c.gridx = 1;
//                panel.add(_aggregators, c);
//                c.gridy++;
//                c.gridx = 0;
//
//                panel.add(new JLabel("Renderer:"), c);
//                c.gridx = 1;
//                panel.add(_renderer, c);
//
//                _name.setText("Untitiled");
//                _name.setColumns(20);
//
//                CoolMapObject object = new CoolMapObject();
//                object.addBaseCMatrix(matrices);
//
//                try {
//                    WidgetAggregator aggrWidget = (WidgetAggregator) WidgetMaster.getWidget(WidgetAggregator.class.getName());
//                    LinkedHashSet<CAggregator> aggrs = aggrWidget.getLoadedRenderers();
//                    DefaultComboBoxModel model = new DefaultComboBoxModel();
//                    for (CAggregator aggr : aggrs) {
//                        if (aggr.canAggregate(object.getBaseClass())) {
//                            model.addElement(aggr);
//                        }
//                    }
//                    _aggregators.setModel(model);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), panel);
//                CMatrix m0 = matrices[0];
//                ArrayList<VNode> nodes = new ArrayList<VNode>();
//                for (int i = 0; i < m0.getNumRows(); i++) {
//                    nodes.add(new VNode(m0.getRowLabel(i)));
//                }
//                object.insertRowNodes(0, nodes, false);
//
//                nodes.clear();
//                for (int i = 0; i < m0.getNumColumns(); i++) {
//                    nodes.add(new VNode(m0.getColLabel(i)));
//                }
//                object.insertColumnNodes(0, nodes, false);
//
//                //need a dialog
//                try {
//                    object.setAggregator((CAggregator) (_aggregators.getSelectedItem().getClass().newInstance()));
////                    object.setViewRenderer((ViewRenderer)(_renderer.getSelectedItem().getClass().newInstance()), true);
//                    if (Double.class.isAssignableFrom(object.getViewClass())) {
//                        object.setSnippetConverter(SnippetMaster.getConverter("D13"));
//                    }
//                } catch (Exception e) {
//                }
//                object.setName("Untitiled");
//
//                object.getCoolMapView().addColumnMap(new ColumnLabels(object));
//                object.getCoolMapView().addColumnMap(new ColumnTree(object));
//
//                object.getCoolMapView().addRowMap(new RowLabels(object));
//                object.getCoolMapView().addRowMap(new RowTree(object));
//
//                CoolMapMaster.addNewCoolMapObject(object);
//            }
//        });

//        button = new JButton(UI.getImageIcon("trashBin"));
//        button.setToolTipText("Remove selected CMatrices");
//        button.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//
//                Object[] cmatrices = _baseMatrices.getSelectedValues();
//                if (cmatrices == null || cmatrices.length == 0) {
//                    return;
//                }
//
//                List<CoolMapObject> objs = CoolMapMaster.getCoolMapObjects();
//                if (objs != null && !objs.isEmpty()) {
//
//                    for (Object m : cmatrices) {
//                        for (CoolMapObject obj : objs) {
//                            obj.removeBaseCMatrix((CMatrix) m);
//                        }
//                    }
//
//                }
//
//                for (Object m : cmatrices) {
//                    CoolMapMaster.destroyCMatrix((CMatrix) m);
//                }
//
//            }
//        });
//        _toolBar.add(button);
    }

    @Override
    public void coolMapObjectAdded(CoolMapObject newObject) {
    }

    @Override
    public void coolMapObjectToBeDestroyed(CoolMapObject objectToBeDestroyed) {
    }

    @Override
    public void baseMatrixAdded(CMatrix newMatrix) {
        updateTableModel();
    }

    @Override
    public void baseMatrixToBeRemoved(CMatrix matrixToBeRemoved) {
        updateTableModel();
    }

    private final String[] tableHeaders = new String[]{"ID", "Name", "Rows", "Columns", "Type"};

    private void updateTableModel() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
//                List<CMatrix> cMatrices = CoolMapMaster.getLoadedCMatrices();
//                DefaultComboBoxModel model = new DefaultComboBoxModel(cMatrices.toArray());
//                _baseMatrices.setModel(model);
                List<CMatrix> cMatrices = CoolMapMaster.getLoadedCMatrices();

//                System.out.println(cMatrices);
                if (!cMatrices.isEmpty()) {

                    Object[][] data = new Object[cMatrices.size()][5];
                    for (int i = 0; i < cMatrices.size(); i++) {
                        CMatrix mx = cMatrices.get(i);
                        data[i][0] = mx.getID();
                        data[i][1] = mx.getName();
                        data[i][2] = mx.getNumRows();
                        data[i][3] = mx.getNumColumns();
                        data[i][4] = mx.getMemberClass().getSimpleName();
                    }

                    final DefaultTableModel model = new DefaultTableModel(data, tableHeaders) {

                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }

                    };

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                matrixTable.setModel(model);
                                matrixTable.getColumnModel().removeColumn(matrixTable.getColumnModel().getColumn(0));
                            } catch (Exception e) {

                            }
                        }
                    });

                } else {

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                matrixTable.setModel(new DefaultTableModel());
                            } catch (Exception e) {

                            }
                        }
                    });
                }

            }
        });

    }

    @Override
    public void contologyAdded(COntology ontology) {

    }

    @Override
    public void contologyToBeDestroyed(COntology ontology) {
    }

    @Override
    public void cmatrixNameChanged(CMatrix mx) {
        updateTableModel();
    }

    @Override
    public void cmatrixValueUpdated(CMatrix mx) {
        updateTableModel();
    }
}
