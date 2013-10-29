/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.widget.Widget;
import coolmap.canvas.listeners.CViewListener;
import coolmap.canvas.misc.MatrixCell;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.listeners.CObjectListener;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author gangsu
 */
public class WidgetDataMatrix extends Widget implements CObjectListener, CViewListener, ActiveCoolMapChangedListener {

    private final JPanel _container = new JPanel();
    private final JTable _dataTable = new JTable();
//    private DefaultTableCellRenderer _rowCellRenderer = new DefaultTableCellRenderer();

    public WidgetDataMatrix() {
        super("Data Matrix", W_DATA, L_DATAPORT, UI.getImageIcon("grid"), null);
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCObjectListener(this);
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCViewListener(this);
        CoolMapMaster.addActiveCoolMapChangedListener(this);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_container);
        _container.setLayout(new BorderLayout());
        _container.add(new JScrollPane(_dataTable));
        //_rowCellRenderer.setBackground(UI.colorGrey2);
    }

    private void _updateData() {
        CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
        if (object == null) {
            _dataTable.setModel(new DefaultTableModel());
        } else {

//            Rectangle selection = object.getCoolMapPanel().getSelectedRegion();

            Rectangle selection = object.getCoolMapView().getSelectionsUnion();
            if (selection == null) {
                _dataTable.setModel(new DefaultTableModel());
                return;
            }

            Object[][] actualView = new Object[selection.height + 1][selection.width + 1];

            actualView[0][0] = "Rows\\Cols";


            for (int i = selection.y; i < selection.y + selection.height; i++) {
                actualView[i - selection.y + 1][0] = object.getViewNodeRow(i);
                for (int j = selection.x; j < selection.x + selection.width; j++) {
                    //actualView[i - selection.y][j - selection.x + 1] = view[i][j];
                    actualView[i - selection.y + 1][j - selection.x + 1] = object.getViewValue(i, j);
                }
            }

            Object[] colNames = new Object[selection.width + 1];
//            colNames[0] = "Rows\\Cols";

            for (int i = 1; i < colNames.length; i++) {
                actualView[0][i] = object.getViewNodeColumn(i + selection.x - 1);
                colNames[i] = "";
            }
            colNames[0] = "Data";

            DefaultTableModel tableModel = new DefaultTableModel(actualView, colNames);
            _dataTable.setModel(tableModel);

            _dataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

                public Component getTableCellRendererComponent(JTable jtable, Object o, boolean isSelected, boolean bln1, int i, int i1) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(jtable, o, isSelected, bln1, i, i1);
                    if (isSelected) {
                        return label;
                    }


                    if (o != null) {
                        if (o instanceof VNode) {
                            VNode node = (VNode) o;
                            if (node.isGroupNode()) {
                                if (node.getViewColor() != null) {
                                    label.setBackground(node.getViewColor());
                                } else if (node.getCOntology() != null && node.getCOntology().getViewColor() != null) {
                                    label.setBackground(node.getCOntology().getViewColor());
                                } else {
                                    label.setBackground(UI.colorGrey2);
                                }
                            } else {
                                label.setBackground(null);
                            }
                        } else {
                            label.setBackground(null);
                        }
                    } else {
                        label.setBackground(null);
                    }


                    return label;
                }
            });

            _dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            _dataTable.getTableHeader().setReorderingAllowed(false);



//            _dataTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer(){
//
//                @Override
//                public Component getTableCellRendererComponent(JTable jtable, Object o, boolean isSelected, boolean bln1, int i, int i1) {
//                   
//                    
//                    
//                    JLabel label = (JLabel)  super.getTableCellRendererComponent(jtable, o, isSelected, bln1, i, i1);
//                    if(isSelected){
//                        return label;
//                    }
//                    VNode node  = (VNode) o;
//                    if(node.getViewColor() != null){
//                        label.setBackground(node.getViewColor());
//                    }
//                    else if(node.getCOntology() != null && node.getCOntology().getViewColor() != null){
//                        label.setBackground(node.getCOntology().getViewColor());
//                    }
//                    else{
//                        label.setBackground(UI.colorGrey2);
//                    }
//          
//                    return label;
//                }
//                
//            });

//            _dataTable.getTableHeader().setDefaultRenderer(new DefaultTableCellHeaderRenderer(){
//
//                @Override
//                public Component getTableCellRendererComponent(JTable jtable, Object o, boolean bln, boolean bln1, int i, int i1) {
//                    
//                    //System.out.println(o.getClass());
//                    JLabel label = (JLabel)super.getTableCellRendererComponent(jtable, o, bln, bln1, i, i1);
//                    if(o != null && o instanceof VNode){
//                        VNode colNode = (VNode) o;
//                        if(colNode.getViewColor() != null){
//                            label.setBackground(colNode.getViewColor());
//                        }
//                        else if(colNode.getCOntology() != null && colNode.getCOntology().getViewColor() != null){
//                            label.setBackground(colNode.getCOntology().getViewColor());
//                        }
//                        else{
//                            label.setBackground(null);
//                        }
//                    }
//                    return label;
//                }
//            
//                
//            });


        }

    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
        _updateData();
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
        _updateData();
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
        _updateData();
    }

    @Override
    public void baseMatrixChanged(CoolMapObject object) {
        _updateData();
    }

//    @Override
//    public void stateStorageUpdated(CoolMapObject object) {
//    }

    @Override
    public void selectionChanged(CoolMapObject object) {
        _updateData();
    }

    @Override
    public void mapAnchorMoved(CoolMapObject object) {
    }

    @Override
    public void activeCellChanged(CoolMapObject object, MatrixCell oldCell, MatrixCell newCell) {
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
    }

//    @Override
//    public void subSelectionRowChanged(CoolMapObject object) {
//    }
//
//    @Override
//    public void subSelectionColumnChanged(CoolMapObject object) {
//    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        _updateData();
    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    @Override
    public void gridChanged(CoolMapObject object) {
    }
}
