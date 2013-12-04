/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import com.google.common.collect.Range;
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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import temp.RowHeader;

/**
 *
 * @author gangsu
 */
public class WidgetDataMatrix extends Widget implements CObjectListener, CViewListener, ActiveCoolMapChangedListener {

    private final JPanel _container = new JPanel();
    private final DataTable _dataTable;
//    private DefaultTableCellRenderer _rowCellRenderer = new DefaultTableCellRenderer();
    private CoolMapObject _activeObject;

    private JScrollPane scrollPane;
    private JTable rowHeader;

    @Override
    public void nameChanged(CoolMapObject object) {
    }

    //built in actions will not be used elsewhere
    private class CenterSelectionAction extends AbstractAction {

        public CenterSelectionAction() {
            super("Center selected cells in canvas", UI.getImageIcon("anchor"));
            putValue(AbstractAction.SHORT_DESCRIPTION, "Center selected cells in view");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (_activeObject == null) {
                return;
            }

            int[] cols = _dataTable.getSelectedColumns();
            int[] rows = _dataTable.getSelectedRows();

            if (cols.length == 0 || rows.length == 0) {
                return;
            }

            Arrays.sort(cols);
            Arrays.sort(rows);

            Rectangle sel = new Rectangle();
            sel.x = cols[0] - 1;
            sel.y = rows[0];

            sel.width = cols[cols.length - 1] - cols[0] + 1;
            sel.height = rows[rows.length - 1] - rows[0] + 1;

            _activeObject.getCoolMapView().setSelection(sel);
            _activeObject.getCoolMapView().centerToSelections();
        }

    }

    private class CopyToClipboardAction extends AbstractAction {

        public CopyToClipboardAction() {
            super("Copy selected data w/ headers to clipboard", UI.getImageIcon("duplicate"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Clipboard clpBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringBuilder content = new StringBuilder();

            if (_dataTable.getSelectedRowCount() == 0 || _dataTable.getSelectedColumnCount() == 0) {
                return;
            }

            int[] columns = _dataTable.getSelectedColumns();
            int[] rows = _dataTable.getSelectedRows();

            //these are the column and row indices
            System.out.println(Arrays.toString(columns));
            System.out.println(Arrays.toString(rows));

            Arrays.sort(columns);
            Arrays.sort(rows);

            //inclusive
            content.append("Row Nodes/Column Nodes");
            for (int j = columns[0] - 1; j <= columns[columns.length - 1] - 1; j++) {
                content.append("\t");
                content.append(_activeObject.getViewNodeColumn(j));
            }
            content.append("\n");

            for (int i = rows[0]; i <= rows[rows.length - 1]; i++) {
                content.append(_activeObject.getViewNodeRow(i));
                for (int j = columns[0] - 1; j <= columns[columns.length - 1] - 1; j++) {
                    content.append("\t");
                    content.append(_activeObject.getViewValue(i, j));
                }
                content.append("\n");
            }

            StringSelection str = new StringSelection(content.toString());
            clpBoard.setContents(str, null);
        }

    }

    public WidgetDataMatrix() {
        super("Data Matrix", W_DATA, L_DATAPORT, UI.getImageIcon("grid"), null);
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCObjectListener(this);
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCViewListener(this);
        CoolMapMaster.addActiveCoolMapChangedListener(this);

        _dataTable = new DataTable();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_container);
        _container.setLayout(new BorderLayout());

        JPopupMenu menu = new JPopupMenu();
        _dataTable.setComponentPopupMenu(menu);

        menu.add(new CenterSelectionAction());

        menu.addSeparator();

        menu.add(new CopyToClipboardAction());

//        JToolBar toolBar = new JToolBar();
//        toolBar.setFloatable(false);
//        _container.add(toolBar, BorderLayout.NORTH);
//        
//        JButton button = new JButton(UI.getImageIcon("anchor"));
//        toolBar.add(button);
//        button.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                
//                if(_activeObject == null){
//                    return;
//                }
//                
//                int[] cols = _dataTable.getSelectedColumns();
//                int[] rows = _dataTable.getSelectedRows();
//                
//                if(cols.length == 0 || rows.length == 0){
//                    return;
//                }
//                
//                Arrays.sort(cols);
//                Arrays.sort(rows);
//                
//                Rectangle sel = new Rectangle();
//                sel.x = cols[0]-1;
//                sel.y = rows[0];
//                
//                sel.width = cols[cols.length-1] - cols[0]+1;
//                sel.height = rows[rows.length-1] - rows[0]+1;
//                
//                _activeObject.getCoolMapView().setSelection(sel);
//                _activeObject.getCoolMapView().centerToSelections();
//                
//            }
//        });
        scrollPane = new JScrollPane(_dataTable);
        rowHeader = new RowHeader(_dataTable);
        scrollPane.setRowHeaderView(rowHeader);
        scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, rowHeader.getTableHeader());

        scrollPane.getViewport().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                System.out.println("Scroll changed");
                Rectangle firstCellRectangle = _dataTable.getCellRect(0, 0, true);
                int viewPosition = firstCellRectangle.x + firstCellRectangle.width - scrollPane.getViewport().getViewPosition().x;

                if (viewPosition < 0) {
//                    scrollPane.setRowHeaderView(rowHeader);
                    rowHeader.setEnabled(true);
                } else {
//                    scrollPane.setRowHeaderView(null);
                    rowHeader.setEnabled(false);
                }
            }
        });

        _container.add(scrollPane);

        //_container.add(new JScrollPane(new RowNumberTable(_dataTable)));
        //_rowCellRenderer.setBackground(UI.colorGrey2);
        //_dataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
    }

    /**
     * use a background thread to copy data over. and that thread is
     */
    private void _updateData() {
//        if (_workerThread != null && _workerThread.isAlive()) {
//            _workerThread.interrupt();
//        }
//
//        _workerThread = new UpdateDataThread();
//        _workerThread.start();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //_replaceTableModel(); //Put replace table model in swing utilies
//                        if (_workerThread != null && _workerThread.isAlive()) {
//                    _workerThread.interrupt();
//                }
//
//                _workerThread = new UpdateDataThread();
//                _workerThread.start();
                _replaceTableModel(); //Let it call. Fuck. sutpid jTable
            }
        });
    }

    private void _replaceTableModel() {
        try {
            CoolMapObject object = _activeObject;
            if (object == null) {
                _dataTable.setModel(new DataTableModel());
                return;
            }
            //need to make sure it's sortable
            //The selections will be

            //secure column labels
            Object[] columnLabels = new Object[object.getViewNumColumns() + 1];
//            _dataTable.clearColumnClasses();
            columnLabels[0] = "Row Nodes";
            for (int i = 0; i < object.getViewNumColumns(); i++) {
                columnLabels[i + 1] = object.getViewNodeColumn(i);
//                if(Double.class.isAssignableFrom(object.getViewClass())){
//                    _dataTable.setColumnClass(i+1, Double.class);
//                }
                if (Thread.interrupted()) {
                    return;
                }
            }

            //create
            Object[][] data = new Object[object.getViewNumRows()][object.getViewNumColumns() + 1];

            for (int i = 0; i < object.getViewNumRows(); i++) {
                data[i][0] = object.getViewNodeRow(i);
                for (int j = 0; j < object.getViewNumColumns(); j++) {
//                    data[i][j+1]
                    data[i][j + 1] = object.getViewValue(i, j);
                    if (Thread.interrupted()) {
                        return;
                    }
                }
            }

//            DataTableModel model = new DataTableModel();
            if (Thread.interrupted()) {
                return;
            }

            //Then set table model
            DataTableModel model = new DataTableModel(data, columnLabels);

            //
            for (int i = 1; i < columnLabels.length; i++) {
                if (Double.class.isAssignableFrom(object.getViewClass())) {
                    model.setColumnClass(i, null);
                }
            }
            if (Thread.interrupted()) {
                return;
            }

            _dataTable.setModel(model);

            //Remove
//        _dataTable.getColumnModel().removeColumn(_dataTable.getColumn("Row Nodes"));
            //add
            _dataTable.getRowSorter().addRowSorterListener(new RowSorterListener() {

                @Override
                public void sorterChanged(RowSorterEvent e) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    if (e.getType() == RowSorterEvent.Type.SORTED) {
                        System.out.println("sort changed");

                        rowHeader.repaint();

                        //replace nodes
                        if (_activeObject == null) {
                            return;
                        }
                        List treeNodes = _activeObject.getViewTreeNodesRow();
                        ArrayList<VNode> nodes = new ArrayList<VNode>();

                        for (int i = 0; i < _dataTable.getRowCount(); i++) {
                            nodes.add((VNode) _dataTable.getValueAt(i, 0));
                        }
                        //System.out.println(nodes);
                        _sorterTrigger = true;
                        _activeObject.replaceRowNodes(nodes, treeNodes);

                    }

                }
            });
        } catch (Exception e) {
            System.err.println("Minor issue when attempting to update table model. Possibly due to render cancelation");
        }
    }

    private boolean _sorterTrigger = false;

    private Thread _workerThread;

    private class UpdateDataThread extends Thread {

        @Override
        public void run() {
            super.run();
            CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
            if (object == null) {
                _dataTable.setModel(new DataTableModel());
                return;
            }
            //need to make sure it's sortable
            //The selections will be

            //secure column labels
            Object[] columnLabels = new Object[object.getViewNumColumns() + 1];
//            _dataTable.clearColumnClasses();
            columnLabels[0] = "Row Nodes";
            for (int i = 0; i < object.getViewNumColumns(); i++) {
                columnLabels[i + 1] = object.getViewNodeColumn(i);
//                if(Double.class.isAssignableFrom(object.getViewClass())){
//                    _dataTable.setColumnClass(i+1, Double.class);
//                }
                if (Thread.interrupted()) {
                    return;
                }
            }

            //create
            Object[][] data = new Object[object.getViewNumRows()][object.getViewNumColumns() + 1];

            for (int i = 0; i < object.getViewNumRows(); i++) {
                data[i][0] = object.getViewNodeRow(i);
                for (int j = 0; j < object.getViewNumColumns(); j++) {
//                    data[i][j+1]
                    data[i][j + 1] = object.getViewValue(i, j);
                    if (Thread.interrupted()) {
                        return;
                    }
                }
            }

//            DataTableModel model = new DataTableModel();
            if (Thread.interrupted()) {
                return;
            }

            //Then set table model
            DataTableModel model = new DataTableModel(data, columnLabels);

            //
            for (int i = 1; i < columnLabels.length; i++) {
                if (Double.class.isAssignableFrom(object.getViewClass())) {
                    model.setColumnClass(i, null);
                }
            }
            if (Thread.interrupted()) {
                return;
            }

            _dataTable.setModel(model);
        }
    }

    private class DataTableModel extends DefaultTableModel {

        public DataTableModel() {
        }

        public DataTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }

        private final HashMap<Integer, Class> columnClass = new HashMap();

        public void setColumnClass(int index, Class cls) {
            columnClass.put(new Integer(index), cls);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            try {
                Class cls = columnClass.get(new Integer(columnIndex));
                if (cls == null) {
                    return super.getColumnClass(columnIndex);
                } else {
                    return cls;
                }
            } catch (Exception e) {
                return super.getColumnClass(columnIndex); //To change body of generated methods, choose Tools | Templates.
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
//            return super.isCellEditable(row, column); //To change body of generated methods, choose Tools | Templates.
            return false;
        }

    }

    private class DataTable extends JTable {

//        private final HashMap<Integer, Class> columnClassMap = new HashMap<Integer, Class>();
        private boolean columnDragging = false;
        private DecimalFormat format = new DecimalFormat("#.###");

        public DataTable() {

            setAutoCreateRowSorter(true);
            setRowSelectionAllowed(true);
            setColumnSelectionAllowed(true);
            setAutoResizeMode(AUTO_RESIZE_OFF);

            setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.

                    if (value != null && Double.class.isAssignableFrom(value.getClass())) {
                        label.setText(format.format(value));
                    }

                    if (!isSelected) {
                        int modelIndex = table.convertColumnIndexToModel(column);

                        if (modelIndex == 0) {
                            label.setBackground(UI.colorLightGreen0);
                            label.setFont(_dataTable.getTableHeader().getFont());
                        } else {
                            label.setBackground(UI.colorWhite);
                            label.setFont(UIManager.getFont("Table.font"));
                        }
                    }

                    return label;
                }

            });

            getColumnModel().addColumnModelListener(new TableColumnModelListener() {

                @Override
                public void columnAdded(TableColumnModelEvent e) {
                }

                @Override
                public void columnRemoved(TableColumnModelEvent e) {
                }

                @Override
                public void columnMoved(TableColumnModelEvent e) {
                    columnDragging = true;
                    if (columnValue == -1) {
                        columnValue = e.getFromIndex();
                    }

                    columnNewValue = e.getToIndex();

                    //System.out.println(columnValue + " " + columnNewValue);
                }

                @Override
                public void columnMarginChanged(ChangeEvent e) {
                }

                @Override
                public void columnSelectionChanged(ListSelectionEvent e) {
                }
            });

            getTableHeader().addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {

                    if (columnValue != -1 && (columnValue == 0 || columnNewValue == 0)) {
                        _dataTable.moveColumn(columnNewValue, columnValue);
                    }

                    if (columnValue >= 1 && columnNewValue >= 1 && columnValue != columnNewValue) {

                        //To change body of generated methods, choose Tools | Templates.
                        if (columnDragging) {
//                        System.out.println("Drag completed");
                            reorderColumns(columnValue, columnNewValue);
                        }
                        columnDragging = false;
                    }

                    //reset them both
                    columnValue = -1;
                    columnNewValue = -1;
                }

                @Override
                public void mouseExited(MouseEvent e) {
//                    super.mouseExited(e); //To change body of generated methods, choose Tools | Templates.
//                    mouseReleased(e);
                }

            });
        }

        private int columnValue = -1;
        private int columnNewValue = -1;

        private void reorderColumns(int fromIndex, int toIndex) {
            //Note the state here must be saved
            //System.out.println(_dataTable.getColumnModel().getColumn(0).getHeaderValue());
//            if(_activeObject == null){
//                return;
//            }
//            
//            ArrayList<VNode> columnNodes = new ArrayList<VNode>(_activeObject.getViewNumColumns());
//            
//            for(int i=0; i< _dataTable.getColumnModel().getColumnCount(); i++){
//                try{
//                    //columnNodes.add((VNode)_dataTable.getColumnModel().getColumn(i).getHeaderValue());
//                    System.out.println(_dataTable.getColumnModel().getColumn(i).getIdentifier().getClass());
//                }
//                catch(Exception e){
//                    e.printStackTrace();
//                }
//            }
//            
//            System.out.println(columnNodes);
            fromIndex = fromIndex - 1;
            toIndex = toIndex - 1;

            if (toIndex > fromIndex) {
                toIndex = toIndex + 1;
            }

//            System.out.println(fromIndex + " " + toIndex);
            if (_activeObject == null) {
                return;
            }

            ArrayList<Range<Integer>> selectedColumns = new ArrayList<>(1);
            selectedColumns.add(Range.closedOpen(fromIndex, fromIndex + 1));
            _activeObject.multiShiftColumns(selectedColumns, toIndex);

        }

//        public void setColumnClass(int columnIndex, Class cls){
//            columnClassMap.put(columnIndex, cls);
//        }
//        
//        public void clearColumnClasses(){
//            columnClassMap.clear();
//        }
//
//        @Override
//        public void setModel(TableModel dataModel) {
//            super.setModel(dataModel); //To change body of generated methods, choose Tools | Templates.
//        }
//        
//        
//        
//        @Override
//        public Class<?> getColumnClass(int column) {
//            try {
//                Class cls = columnClassMap.get(column);
//                if(cls != null){
//                    return cls;
//                }
//                else{
//                    return super.getColumnClass(column);
//                }
//            } catch (Exception e) {
//
//                return super.getColumnClass(column); //To change body of generated methods, choose Tools | Templates.
//            }
//        }
//        @Override
//        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
//
////            Component rendererCp = super.prepareRenderer(renderer, row, column); //To change body of generated methods, choose Tools | Templates.
////
////            if (column == 0) {
////                //row labels
////                //but however - > 
////                rendererCp.setBackground(UI.colorLightGreen0);
////            }
////            else {
////                
////                rendererCp.setBackground(UI.colorWhite);
////            }
////
////            return rendererCp;
//            return super.prepareRenderer(renderer, row, column);
//        }
    }

    //This is where it was broken!
    private void _updateDataOld() {

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

            System.out.println("Selection in data matrix widget" + selection);

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

            System.out.println("Finished loading actual view");

            DefaultTableModel tableModel = new DefaultTableModel(actualView, colNames);
            _dataTable.setModel(tableModel);

            System.out.println("End of setting table model");

            System.out.println("End of setting table renderer");

            _dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            _dataTable.getTableHeader().setReorderingAllowed(false);

            System.out.println("End of updating data table");

        }

    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
        _updateData();
    }

    @Override
    public void rowsChanged(CoolMapObject object) {

        if (_activeObject == null || _activeObject != object) {
            return;
        }

//        System.out.println("Was row change because of sort?" + _sorterTrigger);
        if (_sorterTrigger) {
            _sorterTrigger = false;
        } else {
            _updateData();
        }
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
        //_updateData();
        _updateData();
    }

    @Override
    public void baseMatrixChanged(CoolMapObject object) {
        if (_activeObject != null && _activeObject == object) {
            _updateData();
        }
    }

//    @Override
//    public void stateStorageUpdated(CoolMapObject object) {
//    }
    @Override
    public void selectionChanged(CoolMapObject object) {
//        _updateData();
        //But instead, select
        if (_activeObject != null && _activeObject == object) {
            Rectangle selection = _activeObject.getCoolMapView().getSelectionsUnion();

            if (selection == null || selection.isEmpty()) {

                _dataTable.clearSelection();
                return;
            } else {
//                _dataTable.getSelectionModel().

//                System.err.println("Selection:" + selection);
                _dataTable.setColumnSelectionInterval(selection.x + 1, selection.x + selection.width);

                try {
                    _dataTable.setRowSelectionInterval(selection.y, selection.y + selection.height - 1);

//                _dataTable.getCellRect(W_DATA, W_DATA, true)
                    Rectangle rect = _dataTable.getCellRect((selection.y * 2 + selection.height) / 2, (selection.x * 2 + selection.width) / 2, true);
                    rect.x += 100;

                    _dataTable.scrollRectToVisible(rect);
                } catch (Exception e) {
                    _dataTable.clearSelection();
                }

            }
        }

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
        _activeObject = activeCoolMapObject;
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

                //This guy got an issue
    //issue is here, still don't know why
//            _dataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
//
//                public Component getTableCellRendererComponent(JTable jtable, Object o, boolean isSelected, boolean bln1, int i, int i1) {
////                    try{
////                    JLabel label = (JLabel) super.getTableCellRendererComponent(jtable, o, isSelected, bln1, i, i1);
////                    if (isSelected) {
////                        return label;
////                    }
////
////
////                    if (o != null) {
////                        if (o instanceof VNode) {
////                            VNode node = (VNode) o;
////                            if (node.isGroupNode()) {
////                                if (node.getViewColor() != null) {
////                                    label.setBackground(node.getViewColor());
////                                } else if (node.getCOntology() != null && node.getCOntology().getViewColor() != null) {
////                                    label.setBackground(node.getCOntology().getViewColor());
////                                } else {
////                                    label.setBackground(UI.colorGrey2);
////                                }
////                            } else {
////                                label.setBackground(null);
////                            }
////                        } else {
////                            label.setBackground(null);
////                        }
////                    } else {
////                        label.setBackground(null);
////                    }
////
////
////                    return label;
////                    }
////                    catch(Exception e){
////                        return super.getTableCellRendererComponent(jtable, o, isSelected, bln1, i1, i1);
////                    }
//                    
//                    
//                    return super.getTableCellRendererComponent(jtable, o, isSelected, bln1, i1, i1);
//                }
//            });
}
