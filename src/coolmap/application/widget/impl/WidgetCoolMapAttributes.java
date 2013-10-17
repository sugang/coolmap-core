/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.listeners.DataStorageListener;
import coolmap.application.widget.Widget;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.contology.model.COntology;
import coolmap.data.listeners.CObjectListener;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author gangsu
 */
public class WidgetCoolMapAttributes extends Widget implements ActiveCoolMapChangedListener, CObjectListener {

    private JPanel _container = new JPanel();
    private JToolBar _toolBar = new JToolBar();
    private JScrollPane _scroller = new JScrollPane();
    private JList _cmatrixList = new JList();
    private JList _allCMatrixList = new JList();

    public WidgetCoolMapAttributes() {
        super("Active CoolMap Matrices", W_MODULE, L_LEFTBOTTOM, UI.getImageIcon("commentDots"), "Comments in the current view");
        CoolMapMaster.addActiveCoolMapChangedListener(this);
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCObjectListener(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_container);
        _container.setLayout(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        _container.add(toolbar, BorderLayout.NORTH);
        toolbar.setFloatable(false);

        JButton button = new JButton(UI.getImageIcon("upThin"));
        toolbar.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {


                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object == null) {
                    _updateBaseCMatrixList();
                } else {
                    int selectedIndex = _cmatrixList.getSelectedIndex();
                    if (selectedIndex < 0) {
                        return;
                    }

                    List<CMatrix> matrices = object.getBaseCMatrices();
                    CMatrix matrix = matrices.get(selectedIndex);
                    matrices.remove(matrix);
                    selectedIndex--;
                    if (selectedIndex < 0) {
                        selectedIndex = 0;
                    }
                    matrices.add(selectedIndex, matrix);
                    CMatrix[] mat = new CMatrix[matrices.size()];
                    matrices.toArray(mat);
                    object.clearBaseCMatrices();
                    object.addBaseCMatrix(mat);
                    _updateBaseCMatrixList();
                    _cmatrixList.setSelectedIndex(selectedIndex);
                }

            }
        });

        button = new JButton(UI.getImageIcon("downThin"));
        toolbar.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object == null) {
                    _updateBaseCMatrixList();
                } else {
                    int selectedIndex = _cmatrixList.getSelectedIndex();
                    if (selectedIndex < 0) {
                        return;
                    }

                    List<CMatrix> matrices = object.getBaseCMatrices();
                    CMatrix matrix = matrices.get(selectedIndex);
                    matrices.remove(matrix);
                    selectedIndex++;
                    if (selectedIndex > matrices.size()) {
                        selectedIndex = matrices.size();
                    }
                    matrices.add(selectedIndex, matrix);
                    CMatrix[] mat = new CMatrix[matrices.size()];
                    matrices.toArray(mat);
                    object.clearBaseCMatrices();
                    object.addBaseCMatrix(mat);
                    _updateBaseCMatrixList();
                    _cmatrixList.setSelectedIndex(selectedIndex);
                }

            }
        });

        button = new JButton(UI.getImageIcon("plusSmall"));
        toolbar.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object == null) {
                    return;
                }


                List<CMatrix> allMatrices = CoolMapMaster.getLoadedCMatrices();
                if (allMatrices.isEmpty()) {
                    return;
                }

                DefaultListModel model = new DefaultListModel();
                for (CMatrix matrix : allMatrices) {
                    model.addElement(matrix);
                }
                _allCMatrixList.setModel(model);
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), new JScrollPane(_allCMatrixList), "Choose CMatrices to Add", JOptionPane.INFORMATION_MESSAGE);

                Object[] matrix = _allCMatrixList.getSelectedValues();
                System.out.println("To be added:" + Arrays.toString(matrix));
                for (Object m : matrix) {
                    try {
                        CMatrix mat = (CMatrix) m;
                        object.addBaseCMatrix(mat);
                    } catch (Exception e) {
                        System.out.println(m + " was not added.");
                    }
                }


            }
        });



        button = new JButton(UI.getImageIcon("minusSmall"));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object == null) {
                    _updateBaseCMatrixList();
                } else {
                    int selectedIndex = _cmatrixList.getSelectedIndex();
                    if (selectedIndex < 0) {
                        return;
                    }

                    List<CMatrix> matrices = object.getBaseCMatrices();
                    CMatrix matrix = matrices.get(selectedIndex);
                    matrices.remove(matrix);
                    CMatrix[] mat = new CMatrix[matrices.size()];
                    matrices.toArray(mat);
                    object.clearBaseCMatrices();
                    object.addBaseCMatrix(mat);
                    _updateBaseCMatrixList();
                }

            }
        });
        toolbar.add(button);




        _container.add(_scroller, BorderLayout.CENTER);
        _scroller.setViewportView(_cmatrixList);

        _cmatrixList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
                JLabel label = (JLabel) super.getListCellRendererComponent(jlist, o, i, bln, bln1);
                CMatrix matrix = (CMatrix) o;

                try {
                    label.setText("<html><strong>" + matrix.getName() + "</strong> [" + matrix.getMemberClass().getSimpleName() + "] [ Rows:" + matrix.getNumRows() + ", Columns: " + matrix.getNumColumns() + "]</html>");
                } catch (Exception e) {
                }
                label.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 2));
                return label;

            }
        });

        _allCMatrixList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
                JLabel label = (JLabel) super.getListCellRendererComponent(jlist, o, i, bln, bln1);
                CMatrix matrix = (CMatrix) o;

                try {
                    label.setText("<html><strong>" + matrix.getName() +  "</strong> [" + matrix.getMemberClass().getSimpleName() + "] [ Rows:" + matrix.getNumRows() + ", Columns: " + matrix.getNumColumns() + "]</html>");
                } catch (Exception e) {
                }
                label.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 2));
                return label;

            }
        });

        _cmatrixList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        _updateBaseCMatrixList();
    }

    private void _updateBaseCMatrixList() {
        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
        if (obj == null) {
            _cmatrixList.setModel(new DefaultListModel());
            return;
        }


        List<CMatrix> cmatrices = obj.getBaseCMatrices();
        DefaultListModel model = new DefaultListModel();
        for (CMatrix matrix : cmatrices) {
            model.addElement(matrix);
        }

        System.out.println("Model updated:" + model);

        _cmatrixList.setModel(model);
    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
    }

    @Override
    public void baseMatrixChanged(CoolMapObject object) {
        _updateBaseCMatrixList();
    }

    @Override
    public void stateStorageUpdated(CoolMapObject object) {
    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }
}
