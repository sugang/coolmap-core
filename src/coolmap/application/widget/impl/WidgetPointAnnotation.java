/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.utils.Messenger;
import coolmap.application.widget.Widget;
import coolmap.canvas.listeners.CViewListener;
import coolmap.canvas.misc.MatrixCell;
import coolmap.data.CoolMapObject;
import coolmap.data.annotation.PointAnnotation;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.listeners.CObjectListener;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author sugang
 */
public class WidgetPointAnnotation extends Widget implements CObjectListener, CViewListener, ActiveCoolMapChangedListener {

    private JTabbedPane _container = new JTabbedPane();
    private JToolBar _toolBar = new JToolBar();
    private PointAnnotationEditor _editor = new PointAnnotationEditor();
    private PointAnnotationBrowser _browser = new PointAnnotationBrowser();

    /**
     * please note that when base matrix changes, all
     */
    public WidgetPointAnnotation() {
        super("Point Annotation", W_MODULE, L_LEFTBOTTOM, UI.getImageIcon(null), "Annotate certain points on a map");
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCObjectListener(this);
        CoolMapMaster.addActiveCoolMapChangedListener(this);
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCViewListener(this);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_container, BorderLayout.CENTER);

        //need a way: 1 display
        _container.addTab("Editor", null, _editor, "Edit point annotations");
        _container.addTab("Browser", null, _browser, "Browse all annotations");

    }

    private class PointAnnotationEditor extends JPanel {

        private VNode currentRowNode;
        private VNode currentColNode;
        private PointAnnotation currentAnnotation;
        private CoolMapObject currentObject;

        private JLabel _textColorLabel;
        private JLabel _fontColorLabel;
        private JTextArea _annotationField;
        private JLabel _informationLabel = new JLabel();

        public PointAnnotationEditor() {
            _textColorLabel = new JLabel("       ");
            _fontColorLabel = new JLabel("       ");
            _annotationField = new JTextArea();
            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            setLayout(new BorderLayout());
            add(new JScrollPane(_annotationField), BorderLayout.CENTER);
            add(toolBar, BorderLayout.SOUTH);

//  worry about these later on            
//            JButton button = new JButton("Text color");
//            toolBar.add(button);
//            toolBar.add(_textColorLabel);
//            button.addActionListener(new ActionListener() {
//
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    
//                }
//            });
//            
//            button = new JButton("Label color");
//            toolBar.add(button);
//            toolBar.add(_fontColorLabel);
            JButton button = new JButton("Save");
            toolBar.add(button);
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    //save to current PA
                    if (currentObject != null && currentRowNode != null && currentColNode != null) {

                        if (currentAnnotation != null) {
                            //update the current one.
                            //must 
                            currentAnnotation.setAnnotation(_annotationField.getText());
                            currentObject.getCoolMapView().updateCanvasEnforceOverlay();

                            //the current annotation exited
                            //no need to update
                        } else {

                            PointAnnotation annotation = new PointAnnotation(currentRowNode, currentColNode, _annotationField.getText());
                            currentObject.getAnnotationStorage().addAnnotation(annotation);
                            currentObject.getCoolMapView().updateCanvasEnforceOverlay();
                            //new one added, update
                            _browser.updateListModel();
                        }
                    }

                }
            });

            button = new JButton("Delete");
            toolBar.add(button);
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    //delete the current one
                    if (currentObject != null && currentRowNode != null && currentColNode != null && currentAnnotation != null) {

                        currentObject.getAnnotationStorage().removeAnnotation(currentRowNode, currentColNode);
                        currentAnnotation = null;
                        _annotationField.setText("");
                        
                        currentObject.getCoolMapView().updateCanvasEnforceOverlay();
                        
                        _browser.updateListModel();
                    }

                }
            });

            add(_informationLabel, BorderLayout.NORTH);
            _informationLabel.setFont(UI.fontMono.deriveFont(12f));
            _informationLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
            _annotationField.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        }

        public void updateActiveAnnotation(CoolMapObject object, VNode rowNode, VNode colNode, PointAnnotation pa) {
            currentRowNode = rowNode;
            currentColNode = colNode;
            currentAnnotation = pa;
            currentObject = object;

            String rowLabel = "";
            if (rowNode != null) {
                rowLabel = rowNode.getViewLabel();
            }

            String colLabel = "";
            if (colNode != null) {
                colLabel = colNode.getViewLabel();
            }

            _informationLabel.setText("<html> Row: <strong>" + rowLabel + "</strong><br/> Col: <strong>" + colLabel + "</strong></html>");

            if (pa == null) {
                _textColorLabel.setBackground(null);
                _fontColorLabel.setBackground(null);
                _annotationField.setText("");
            } else {
//                _informationLabel.setText(TOOL_TIP_TEXT_KEY);
                _annotationField.setText(pa.getAnnotation());
            }
        }

    }

    private class PointAnnotationBrowser extends JPanel {

        //private JList<PointAnnotation> annotations;
        private JTable annotationTable;
        private JTextField filterField;
        private BrowserTableListener listener = new BrowserTableListener();

        public PointAnnotationBrowser() {
            //annotations = new JList(new DefaultListModel<PointAnnotation>());
            annotationTable = new JTable();
            //make annotationTable not editable
            annotationTable.setAutoCreateRowSorter(true);

            setLayout(new BorderLayout());
            add(new JScrollPane(annotationTable), BorderLayout.CENTER);

            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            add(toolBar, BorderLayout.NORTH);
            filterField = new JTextField();
            toolBar.add(filterField);

        }

        public void updateListModel() {
            //annotations.
            try {
//                ArrayList<PointAnnotation> listAnnotations = CoolMapMaster.getActiveCoolMapObject().getAnnotationStorage().getAnnotations();
//                Collections.sort(listAnnotations);
//                DefaultListModel<PointAnnotation> model = new DefaultListModel<>();
//                for(PointAnnotation an : listAnnotations){
//                    model.addElement(an);
//                }
//                annotations.setModel(model);
                //Must store the ontology ID somewhere otherwise
                ArrayList<PointAnnotation> listAnnotations = CoolMapMaster.getActiveCoolMapObject().getAnnotationStorage().getAnnotations();
                Object[][] anno = new Object[listAnnotations.size()][5];
                for (int i = 0; i < listAnnotations.size(); i++) {
                    PointAnnotation pa = listAnnotations.get(i);
                    anno[i][0] = pa.getRowNodeName();
                    if (pa.getRowNodeOntologyID() != null) {
                        try {
                            anno[i][0] += " || " + CoolMapMaster.getCOntologyByID(pa.getRowNodeOntologyID()).getName();
                        } catch (Exception e) {
                            anno[i][0] += " || ";
                        }
                        anno[i][3] = pa.getRowNodeOntologyID();
                    }
                    anno[i][1] = pa.getColumnNodeName();
                    if (pa.getColumnNodeOntologyID() != null) {
                        try {
                            anno[i][1] += " || " + CoolMapMaster.getCOntologyByID(pa.getColumnNodeOntologyID()).getName();
                        } catch (Exception e) {
                            anno[i][1] += " || ";
                        }
                        anno[i][4] = pa.getColumnNodeOntologyID();
                    }
                    anno[i][2] = pa.getAnnotation();

                    //also must store keys
                }

                DefaultTableModel model = new DefaultTableModel(anno, new String[]{"Row Name", "Col Name", "Annotation", "Row Ontology ID", "Col Ontology ID"});

                annotationTable.setModel(model);

                annotationTable.getModel().addTableModelListener(listener);

                //hid the ontology ids
                annotationTable.removeColumn(annotationTable.getColumn("Row Ontology ID"));
                annotationTable.removeColumn(annotationTable.getColumn("Col Ontology ID"));

                //
            } catch (Exception e) {
//                annotations.setModel(new DefaultListModel<PointAnnotation>());
            }
        }

        private class BrowserTableListener implements TableModelListener {

            @Override
            public void tableChanged(TableModelEvent e) {
                //System.out.println(e.getSource());
                try {
                    if (e.getColumn() == 2 && e.getFirstRow() == e.getLastRow() && e.getType() == TableModelEvent.UPDATE) {

                        int row = e.getFirstRow();
                        String rowKey = annotationTable.getModel().getValueAt(row, 0).toString();
                        String colKey = annotationTable.getModel().getValueAt(row, 1).toString();
                        String newAnnotation = annotationTable.getModel().getValueAt(row, e.getColumn()).toString();

                        Object rowOntologyID = annotationTable.getModel().getValueAt(row, 3);
                        Object colOntologyID = annotationTable.getModel().getValueAt(row, 4);

                        System.out.println(rowKey + " " + colKey + " " + newAnnotation);

                        
                        rowKey = rowKey.replaceAll(" || .*$", "");
                        colKey = colKey.replaceAll(" || .*$", "");

                        if (rowOntologyID != null && rowOntologyID.toString() != "") {
                            rowKey += "|" + rowOntologyID;
                        }
                        if (colOntologyID != null && colOntologyID.toString() != "") {
                            colKey += "|" + colOntologyID;
                        }

                        //
                        PointAnnotation pa = CoolMapMaster.getActiveCoolMapObject().getAnnotationStorage().getAnnotation(rowKey, colKey);
                        
                        //This one must exist! must not be null!
                        pa.setAnnotation(newAnnotation);
                        
                        //
                        CoolMapMaster.getActiveCoolMapObject().getCoolMapView().updateCanvasEnforceOverlay();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(); //Use messenger as well
                }
            }

        }

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
        //when this changes, it should be notified.
        //
        Messenger.showWarningMessage("Base matrices are changed. The annotated values may have also changed.", "Data changed");
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

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        //_editor.updateActiveAnnotation(null);
        _browser.updateListModel();

        if (activeCoolMapObject == null) {
            _editor.updateActiveAnnotation(activeCoolMapObject, null, null, null);

        } else {
            Set<Rectangle> selections = activeCoolMapObject.getCoolMapView().getSelections();
            if (selections.size() == 1) {
                Rectangle sel = (Rectangle) selections.toArray()[0];
                if (sel.width > 1 || sel.height > 1) {
                    _editor.updateActiveAnnotation(activeCoolMapObject, null, null, null);
                    return;
                } else {
                    VNode rowNode = activeCoolMapObject.getViewNodeRow(sel.y);
                    VNode colNode = activeCoolMapObject.getViewNodeColumn(sel.x);

//                    System.out.println(rowNode + " " + colNode);
                    PointAnnotation pa = activeCoolMapObject.getAnnotationStorage().getAnnotation(rowNode, colNode);

//                    System.out.println(pa);
                    _editor.updateActiveAnnotation(activeCoolMapObject, rowNode, colNode, pa);
                }
            }
            else{
                _editor.updateActiveAnnotation(oldObject, null, null, null);
            }
        }

    }

    @Override
    public void selectionChanged(CoolMapObject object) {
        //selection changed
//        System.out.println("Selection changed?");

        try {
            Set<Rectangle> selections = object.getCoolMapView().getSelections();
            if (selections.size() == 1) {
                Rectangle sel = (Rectangle) selections.toArray()[0];
                if (sel.width > 1 || sel.height > 1) {
                    _editor.updateActiveAnnotation(object, null, null, null);
                    return;
                } else {
                    VNode rowNode = object.getViewNodeRow(sel.y);
                    VNode colNode = object.getViewNodeColumn(sel.x);

//                    System.out.println(rowNode + " " + colNode);
                    PointAnnotation pa = object.getAnnotationStorage().getAnnotation(rowNode, colNode);

//                    System.out.println(pa);
                    _editor.updateActiveAnnotation(object, rowNode, colNode, pa);
                }
            }

        } catch (Exception e) {
            _editor.updateActiveAnnotation(null, null, null, null);
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

    @Override
    public void gridChanged(CoolMapObject object) {
    }

}
