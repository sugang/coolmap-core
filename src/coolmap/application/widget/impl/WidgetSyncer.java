/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import com.google.common.collect.Range;
import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.DataStorageListener;
import coolmap.application.utils.DataMaster;
import coolmap.application.widget.Widget;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.listeners.CViewListener;
import coolmap.canvas.misc.MatrixCell;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.listeners.CObjectListener;
import coolmap.data.state.StateSnapshot;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

/**
 *
 * @author gangsu
 */
public final class WidgetSyncer extends Widget implements CViewListener, CObjectListener, DataStorageListener {

//    private boolean _isSyncingAnchor = true;
//    private boolean _isSyncingZoom = true;
//    private boolean _isSyncingSelection = false;
//    private boolean _isSyncingRows = false;
//    private boolean _isSyncingColumns = false;
//    private boolean _isSynchingActiveCell = true;
    private final JToggleButton _anchor, _zoom, _activeCell, _selectionRow, _selectionColumn, _rowLayout, _columnLayout;
    private final JList _list = new JList();

    private void _updateSyncStatus(CoolMapObject object) {
        if (object == null) {
            return;
        }

        CoolMapView view = object.getCoolMapView();

        if (_anchor.isSelected()) {
            view.setSyncMapAnchor(true);
        } else {
            view.setSyncMapAnchor(false);
        }

        if (_zoom.isSelected()) {
            view.setSyncZoom(true);
        } else {
            view.setSyncZoom(false);
        }

        if (_activeCell.isSelected()) {
            view.setSyncActiveCell(true);
        } else {
            view.setSyncActiveCell(false);
        }

        if (_selectionRow.isSelected()) {
            view.setSyncRowSelection(true);
        } else {
            view.setSyncRowSelection(false);
        }

        if (_selectionColumn.isSelected()) {
            view.setSyncColumnSelection(true);
        } else {
            view.setSyncColumnSelection(false);
        }

        if (_rowLayout.isSelected()) {
            view.setSyncRowLayout(true);
        } else {
            view.setSyncRowLayout(false);
        }

        if (_columnLayout.isSelected()) {
            view.setSyncColumnLayout(true);
        } else {
            view.setSyncColumnLayout(false);
        }

       view.redrawCanvas();
    }

    private void _updateSyncStatusAll() {
        for (int i = 0; i < _list.getModel().getSize(); i++) {
            CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
            if(item.isSelected()){
                _updateSyncStatus(item.getCoolMapObject());
            }
            else{
                item.getCoolMapObject().prepareDesync();
            }
        }
    }

    public WidgetSyncer() {
        super("Syncer", W_MODULE, L_LEFTCENTER, UI.getImageIcon("lock"), null);
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCObjectListener(this);
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCViewListener(this);
        DataMaster.addDataStorageListener(this);

        JComponent contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        _anchor = new JToggleButton(UI.getImageIcon("anchor"));
        _anchor.setToolTipText("Sync map anchor");
        _anchor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                mapAnchorMoved(CoolMapMaster.getActiveCoolMapObject());
                _updateSyncStatusAll();
            }
        });

        _activeCell = new JToggleButton(UI.getImageIcon("activeCell"));
        _activeCell.setToolTipText("Sync active cell");
        _activeCell.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                activeCellChanged(CoolMapMaster.getActiveCoolMapObject(), null, null);
                _updateSyncStatusAll();
            }
        });

        _zoom = new JToggleButton(UI.getImageIcon("search"));
        _zoom.setToolTipText("Sync zoom");
        _zoom.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                mapZoomChanged(CoolMapMaster.getActiveCoolMapObject());
                _updateSyncStatusAll();
            }
        });

        _selectionRow = new JToggleButton(UI.getImageIcon("rangeRow"));
        _selectionRow.setToolTipText("Sync row selection by node label identity");
        _selectionRow.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                selectionChanged(CoolMapMaster.getActiveCoolMapObject());
                _updateSyncStatusAll();
            }
        });

        _selectionColumn = new JToggleButton(UI.getImageIcon("rangeColumn"));
        _selectionColumn.setToolTipText("Sync column selection by node label identity");
        _selectionColumn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                selectionChanged(CoolMapMaster.getActiveCoolMapObject());
                _updateSyncStatusAll();
            }
        });

        _rowLayout = new JToggleButton(UI.getImageIcon("rowLabel"));
        _rowLayout.setToolTipText("Sync row layout");
        _rowLayout.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                rowsChanged(CoolMapMaster.getActiveCoolMapObject());
                _updateSyncStatusAll();
            }
        });

        _columnLayout = new JToggleButton(UI.getImageIcon("colLabel"));
        _columnLayout.setToolTipText("Sync column layout");
        _columnLayout.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                columnsChanged(CoolMapMaster.getActiveCoolMapObject());
                _updateSyncStatusAll();
            }
        });

        toolBar.add(_anchor);
        toolBar.add(_zoom);
        toolBar.add(_activeCell);
        toolBar.add(_selectionRow);
        toolBar.add(_selectionColumn);
        toolBar.add(_rowLayout);
        toolBar.add(_columnLayout);

        contentPane.add(toolBar, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(_list), BorderLayout.CENTER);

        //update list
        _list.setCellRenderer(new CheckListRenderer());
        _list.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        _list.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent event) {
                JList list = (JList) event.getSource();

                // Get index of item clicked

                int index = list.locationToIndex(event.getPoint());
                CheckListItem item = (CheckListItem) list.getModel().getElementAt(index);

                // Toggle selected state

                item.setSelected(!item.isSelected());


                // Repaint cell

                list.repaint(list.getCellBounds(index, index));




            }
        });
    }

    @Override
    public void selectionChanged(CoolMapObject object) {
        if (object == null) {
            return;
        }

        for (int i = 0; i < _list.getModel().getSize(); i++) {
            CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
            if (item.getCoolMapObject() == object && !item.isSelected()) {
                return;//if the active coolmap object is not selected
            }
        }

        //figure out the selected node names
        if (_selectionColumn.isSelected()) {
            //find the selected anchor names
            //if itself is not checked, return


            HashSet<String> nodeNames = new HashSet<String>();
            ArrayList<Range<Integer>> selectedColumns = object.getCoolMapView().getSelectedColumns();
            VNode node;
            for (Range<Integer> range : selectedColumns) {
                for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                    node = object.getViewNodeColumn(i);
                    if (node != null && node.getName() != null) {
                        nodeNames.add(node.getName());
                    }
                }
            }

            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.isSelected()) {
                    CoolMapObject otherObject = item.getCoolMapObject();
                    if (object == otherObject) {
                        continue;
                    }


                    TreeSet<Integer> toBeSelectedIndices = new TreeSet<Integer>();

                    //iterate over all nodes associated with the name
                    for (String name : nodeNames) {
                        List<VNode> nodes = otherObject.getViewNodesColumn(name);
                        if (nodes == null || nodes.isEmpty()) {
                            continue;
                        } else {
                            for (VNode node1 : nodes) {
                                if (node1 != null && node1.getViewIndex() != null) {
                                    toBeSelectedIndices.add(node1.getViewIndex().intValue());
                                }
                            }
                        }
                    }

                    //now you have a good idea of to be selected
                    //System.out.println(toBeSelected);

                    //now merge them!
                    if (toBeSelectedIndices.isEmpty()) {
                        otherObject.getCoolMapView().setSelectionsColumn(null);
                    } else {
                        int startIndex = toBeSelectedIndices.first();
                        int currentIndex = startIndex;
                        HashSet<Range<Integer>> selectedColumnsRanges = new HashSet<Range<Integer>>();

                        for (Integer index : toBeSelectedIndices) {
                            if (index <= currentIndex + 1) {
                                currentIndex = index;
                                continue;
                            } else {
                                selectedColumnsRanges.add(Range.closedOpen(startIndex, currentIndex + 1));
                                currentIndex = index;
                                startIndex = currentIndex;
                            }
                        }
                        selectedColumnsRanges.add(Range.closedOpen(startIndex, currentIndex + 1));

//                        System.out.println(otherObject.getName());
                        otherObject.getCoolMapView().setSelectionsColumn(selectedColumnsRanges);
                        
//                        ////////////////////////////////////////////////////////////////////////
//                        ////////////////////////////////////////////////////////////////////////
//                        otherObject.getCoolMapView().setSubSelectionColumns(nodeNames);
                        
                        
                        
                    }
                    //end of changing selection operation
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
                    //ontology 
                    

                }
            }
        }//selection column done



        //figure out the selected node names
        if (_selectionRow.isSelected()) {

            HashSet<String> nodeNames = new HashSet<String>();
            ArrayList<Range<Integer>> selectedRows = object.getCoolMapView().getSelectedRows();
            VNode node;
            for (Range<Integer> range : selectedRows) {
                for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                    node = object.getViewNodeRow(i);
                    if (node != null && node.getName() != null) {
                        nodeNames.add(node.getName());
                    }
                }
            }

            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.isSelected()) {
                    CoolMapObject otherObject = item.getCoolMapObject();
                    if (object == otherObject) {
                        continue;
                    }

                    TreeSet<Integer> toBeSelectedIndices = new TreeSet<Integer>();

                    //iterate over all nodes associated with the name
                    for (String name : nodeNames) {
                        List<VNode> nodes = otherObject.getViewNodesRow(name);
                        if (nodes == null || nodes.isEmpty()) {
                            continue;
                        } else {
                            for (VNode node1 : nodes) {
                                if (node1 != null && node1.getViewIndex() != null) {
                                    toBeSelectedIndices.add(node1.getViewIndex().intValue());
                                }
                            }
                        }
                    }

                    if (toBeSelectedIndices.isEmpty()) {
                        otherObject.getCoolMapView().setSelectionsRow(null);

                    } else {
                        int startIndex = toBeSelectedIndices.first();
                        int currentIndex = startIndex;
                        HashSet<Range<Integer>> selectedRowRanges = new HashSet<Range<Integer>>();

                        for (Integer index : toBeSelectedIndices) {
                            if (index <= startIndex + 1) {
                                currentIndex = index;
                                continue;
                            } else {
                                selectedRowRanges.add(Range.closedOpen(startIndex, currentIndex + 1));
                                currentIndex = index;
                                startIndex = currentIndex;
                            }
                        }
                        selectedRowRanges.add(Range.closedOpen(startIndex, currentIndex + 1));

                        //System.out.println(otherObject.getName());
                        otherObject.getCoolMapView().setSelectionsRow(selectedRowRanges);
                        
//                        otherObject.getCoolMapView().setSubSelectionRows(nodeNames); //remove subselection functions
                    }

                }
            }
        }//selection row done



    }

    @Override
    public void mapAnchorMoved(CoolMapObject object) {
        Point mapAnchor = object.getCoolMapView().getMapAnchor();
        if (_anchor.isSelected()) {
            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.getCoolMapObject() == object && !item.isSelected()) {
                    return;//if the active coolmap object is not selected
                }
            }

            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.isSelected()) {
                    CoolMapObject otherObject = item.getCoolMapObject();
                    if (object == otherObject) {
                        continue;
                    }
                    otherObject.getCoolMapView().moveMapTo(mapAnchor.x, mapAnchor.y);
                }
            }
        }



    }

    @Override
    public void activeCellChanged(CoolMapObject object, MatrixCell oldCell, MatrixCell newCell) {
        if (object == null) {
            return;
        }

        Point mouse = object.getCoolMapView().getMouseXY();
        if (newCell != null && !newCell.isRowValidCell(object)) {
            newCell.row = null;
        }

        if (newCell != null && !newCell.isColValidCell(object)) {
            newCell.col = null;
        }


//        if (_activeCell.isSelected()) {
//            List<CoolMapObject> objects = CoolMapMaster.getActiveCoolMapObjects();
//            for (CoolMapObject otherObject : objects) {
//                if (object == otherObject) {
//                    continue;
//                }
//                MatrixCell oldCellDup = null;
//                MatrixCell newCellDup = null;
//                if (oldCell != null) {
//                    oldCellDup = oldCell.duplicate();
//                }
//                if (newCell != null) {
//                    newCellDup = newCell.duplicate();
//                }
//
//                otherObject.getCoolMapView().setActiveCell(oldCell, newCell);
//                //otherObject.getCoolMapView().setMouseXYtoActiveCell();
//            }
//        }
        String nodeNameColumn = null;
        String nodeNameRow = null;

        if (newCell != null && newCell.isValidRange(object)) {
            nodeNameColumn = object.getViewNodeColumn(newCell.col.intValue()).getName();
            nodeNameRow = object.getViewNodeRow(newCell.row.intValue()).getName();
        }


        if (_activeCell.isSelected()) {
            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.getCoolMapObject() == object && !item.isSelected()) {
                    return;//if the active coolmap object is not selected
                }
            }

            MatrixCell oldCellDup = null;
            MatrixCell newCellDup = null;
            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.isSelected()) {
                    CoolMapObject otherObject = item.getCoolMapObject();
                    if (object == otherObject) {
                        object.prepareDesync();
                        continue;
                    }

                    oldCellDup = otherObject.getCoolMapView().getActiveCell();

                    if (nodeNameColumn == null || nodeNameRow == null) {
                        newCellDup = null;
                    } else {
                        List<VNode> rowNodes = otherObject.getViewNodesRow(nodeNameRow);
                        List<VNode> columnNodes = otherObject.getViewNodesColumn(nodeNameColumn);
                        if (rowNodes == null || rowNodes.isEmpty()) {
                            newCellDup = null;
                        } else if (columnNodes == null || columnNodes.isEmpty()) {
                            newCellDup = null;
                        } else {
                            //only fetch the first one
                            newCellDup = new MatrixCell(rowNodes.get(0).getViewIndex(), columnNodes.get(0).getViewIndex());
                        }
                    }

                    otherObject.prepareSync();
                    otherObject.getCoolMapView().setActiveCell(oldCellDup, newCellDup);
                }
            }
        }
    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
        if (object == null) {
            return;
        }

        System.out.println("Rows changed");
        if (_rowLayout.isSelected()) {
            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.getCoolMapObject() == object && !item.isSelected()) {
                    return;//if the active coolmap object is not selected
                }
            }

            StateSnapshot snapshot = new StateSnapshot(object, COntology.ROW, StateSnapshot.STATESET);

            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.isSelected()) {
                    CoolMapObject otherObject = item.getCoolMapObject();
                    if (object == otherObject) {
                        continue;
                    }

                    //start here
                    otherObject.restoreSnapshot(snapshot.duplicate(), true);
                    if (_selectionRow.isSelected()) {
                        otherObject.getCoolMapView().setSelectionsRow(object.getCoolMapView().getSelectedRows());
                    }
                }
            }
        }
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
        if (object == null) {
            return;
        }

        System.out.println("Columns changed");
        if (_columnLayout.isSelected()) {
            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.getCoolMapObject() == object && !item.isSelected()) {
                    return;//if the active coolmap object is not selected
                }
            }

            StateSnapshot snapshot = new StateSnapshot(object, COntology.COLUMN, StateSnapshot.STATESET);

            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.isSelected()) {
                    CoolMapObject otherObject = item.getCoolMapObject();
                    if (object == otherObject) {
                        continue;
                    }

                    //start here
                    otherObject.restoreSnapshot(snapshot.duplicate(), true);
                    if (_selectionColumn.isSelected()) {
                        otherObject.getCoolMapView().setSelectionsColumn(object.getCoolMapView().getSelectedColumns());
                    }
                }
            }
        }


    }

    @Override
    public void baseMatrixChanged(CoolMapObject object) {
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
        if (object == null) {
            return;
        }

        int zIndexX = object.getCoolMapView().getZoomControlX().getCurrentZoomIndex();
        int zIndexY = object.getCoolMapView().getZoomControlY().getCurrentZoomIndex();

        if (_zoom.isSelected()) {
            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.getCoolMapObject() == object && !item.isSelected()) {
                    return;//if the active coolmap object is not selected
                }
            }

            for (int i = 0; i < _list.getModel().getSize(); i++) {
                CheckListItem item = (CheckListItem) _list.getModel().getElementAt(i);
                if (item.isSelected()) {
                    CoolMapObject otherObject = item.getCoolMapObject();
                    if (object == otherObject) {
                        continue;
                    }

                    System.out.println(otherObject + " " + zIndexX + " " + zIndexY);
                    otherObject.getCoolMapView().setZoomIndices(zIndexX, zIndexY);
                }
            }
        }

    }

    @Override
    public void coolMapObjectAdded(CoolMapObject newObject) {
        _updateList();
    }

    @Override
    public void coolMapObjectToBeDestroyed(CoolMapObject objectToBeDestroyed) {
        _updateList();
    }

    @Override
    public void baseMatrixAdded(CMatrix newMatrix) {
        //base matrix added to this object
    }

    @Override
    public void baseMatrixToBeRemoved(CMatrix matrixToBeRemoved) {
        //base matrix removed to this object
    }

    private void _updateList() {
        DefaultListModel model = new DefaultListModel();
        for (CoolMapObject object : CoolMapMaster.getActiveCoolMapObjects()) {
            model.addElement(new CheckListItem(object));
        }
        _list.setModel(model);
    }

    @Override
    public void stateStorageUpdated(CoolMapObject object) {
        //do nothing
    }

    @Override
    public void contologyAdded(COntology ontology) {
    }

    @Override
    public void contologyToBeDestroyed(COntology ontology) {
    }

//    @Override
//    public void subSelectionRowChanged(CoolMapObject object) {
//    }
//
//    @Override
//    public void subSelectionColumnChanged(CoolMapObject object) {
//        //not quite useful. No operation directly conducts subselection
//    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
        //maybe can sync renderer as well
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    @Override
    public void gridChanged(CoolMapObject object) {
    }

    private class CheckListItem {

        private CoolMapObject _object;
        private boolean _isSelected = false;

        public CheckListItem(CoolMapObject object) {
            _object = object;
        }

        public boolean isSelected() {
            return _isSelected;
        }

        public void setSelected(boolean isSelected) {
            _isSelected = isSelected;
            if (_isSelected) {
                //update actually itself..
                CoolMapObject activeObject = CoolMapMaster.getActiveCoolMapObject();
                mapAnchorMoved(activeObject);
                selectionChanged(activeObject);
                mapZoomChanged(activeObject);
                rowsChanged(activeObject);
                columnsChanged(activeObject);
                activeCellChanged(activeObject, null, null);
                _updateSyncStatus(_object);
            } else {
                System.out.println("Deselect item:" + _object);
                if (_object != null) {
                    _object.getCoolMapView().deSyncAll();
                }
            }
        }

        @Override
        public String toString() {
            return _object.getName();
        }

        public CoolMapObject getCoolMapObject() {
            return _object;
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
}
