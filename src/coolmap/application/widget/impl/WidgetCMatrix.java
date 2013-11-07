/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.DataStorageListener;
import coolmap.application.utils.DataMaster;
import coolmap.application.widget.Widget;
import coolmap.application.widget.WidgetMaster;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

/**
 *
 * @author gangsu
 */
public class WidgetCMatrix extends Widget implements DataStorageListener {

    private JList _baseMatrices = new JList();
    private JToolBar _toolBar = new JToolBar();
//    private JPanel _newCoolMapObjectPanel = new JPanel();
    private JComboBox _aggregators = new JComboBox();
    private JComboBox _renderer = new JComboBox();
    private JTextField _name = new JTextField();

    public WidgetCMatrix() {
        super("Base Matrices", W_MODULE, L_LEFTTOP, UI.getImageIcon(""), "Imported base matrices");
        DataMaster.addDataStorageListener(this);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(_baseMatrices), BorderLayout.CENTER);
        _baseMatrices.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
                JLabel label = (JLabel) super.getListCellRendererComponent(jlist, o, i, bln, bln1);
                label.setText(((CMatrix) o).getDisplayLabel());
                label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                return label;
            }
        });

        _toolBar.setFloatable(false);
        getContentPane().add(_toolBar, BorderLayout.NORTH);
        JButton button = new JButton(UI.getImageIcon("screen"));
        button.setToolTipText("Create view from selected matrices. Selected matrices must be of the same data type and row/column layout.");
        _toolBar.add(button);

        _aggregators.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
//                    DefaultComboBoxModel model = new DefaultComboBoxModel();
                    System.out.println("ItemStatedChanged");
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
                        System.out.println("Renderer set model");
                        _renderer.setModel(model);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


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
                    if (a != null && a.canAggregate(obj.getBaseClass())) {
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
                    if (renderer != null && renderer.canRender(obj.getViewClass())) {
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


        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (_baseMatrices.getSelectedIndices() == null || _baseMatrices.getSelectedIndices().length == 0) {
                    return;
                }
                int[] selectedIndices = _baseMatrices.getSelectedIndices();
                CMatrix[] matrices = new CMatrix[selectedIndices.length];
                Object[] values = _baseMatrices.getSelectedValues();
                int counter = 0;
                for (int i = 0; i < values.length; i++) {
                    matrices[i] = (CMatrix) values[i];
                }

                if (matrices.length > 1) {
                    CMatrix m0 = matrices[0];
                    for (int i = 1; i < matrices.length; i++) {
                        if (!matrices[i].canBeGroupedTogether(m0)) {
                            return;
                        }
                    }
                }

                System.out.println("Create new CoolMapObject");

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
                    e.printStackTrace();
                }


                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), panel);
                CMatrix m0 = matrices[0];
                ArrayList<VNode> nodes = new ArrayList<VNode>();
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
                    object.setViewRenderer((ViewRenderer)(_renderer.getSelectedItem().getClass().newInstance()), true);
                    if(Double.class.isAssignableFrom(object.getViewClass())){
                        object.setSnippetConverter(SnippetMaster.getConverter("D13"));
                    }
                } catch (Exception e) {
                }
                object.setName("Untitiled");

                object.getCoolMapView().addColumnMap(new ColumnLabels(object));
                object.getCoolMapView().addColumnMap(new ColumnTree(object));

                object.getCoolMapView().addRowMap(new RowLabels(object));
                object.getCoolMapView().addRowMap(new RowTree(object));



                CoolMapMaster.addNewCoolMapObject(object);
            }
        });


        button = new JButton(UI.getImageIcon("trashBin"));
        button.setToolTipText("Remove selected CMatrices");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {


                Object[] cmatrices = _baseMatrices.getSelectedValues();
                if (cmatrices == null || cmatrices.length == 0) {
                    return;
                }



                List<CoolMapObject> objs = CoolMapMaster.getCoolMapObjects();
                if (objs != null && !objs.isEmpty()) {

                    for (Object m : cmatrices) {
                        for (CoolMapObject obj : objs) {
                            obj.removeBaseCMatrix((CMatrix) m);
                        }
                    }

                }

                for (Object m : cmatrices) {
                    CoolMapMaster.destroyCMatrix((CMatrix) m);
                }


            }
        });




        _toolBar.add(button);
    }

    @Override
    public void coolMapObjectAdded(CoolMapObject newObject) {
    }

    @Override
    public void coolMapObjectToBeDestroyed(CoolMapObject objectToBeDestroyed) {
    }

    @Override
    public void baseMatrixAdded(CMatrix newMatrix) {
        _updateList();
    }

    @Override
    public void baseMatrixToBeRemoved(CMatrix matrixToBeRemoved) {
        _updateList();
    }

    private void _updateList() {
        List<CMatrix> cMatrices = CoolMapMaster.getLoadedCMatrices();
        DefaultComboBoxModel model = new DefaultComboBoxModel(cMatrices.toArray());
        _baseMatrices.setModel(model);
    }

    @Override
    public void contologyAdded(COntology ontology) {
    }

    @Override
    public void contologyToBeDestroyed(COntology ontology) {
    }
}
