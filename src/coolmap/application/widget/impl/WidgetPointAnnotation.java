/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
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
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

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
        
        private JLabel _textColorLabel;
        private JLabel _fontColorLabel;
        private JTextArea _annotationField;
        
        
        
        
        public PointAnnotationEditor(){
            _textColorLabel = new JLabel("       ");
            _fontColorLabel = new JLabel("       ");
            _annotationField = new JTextArea();
            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            setLayout(new BorderLayout());
            add(new JScrollPane(_annotationField), BorderLayout.CENTER);
            add(toolBar, BorderLayout.NORTH);
            JButton button = new JButton("Text color");
            toolBar.add(button);
            toolBar.add(_textColorLabel);
            
            button = new JButton("Label color");
            toolBar.add(button);
            toolBar.add(_fontColorLabel);
        }
        
        
        public void updateActiveAnnotation(PointAnnotation pa){
            if(pa == null){
                _textColorLabel.setBackground(null);
                _fontColorLabel.setBackground(null);
                _annotationField.setText("");
            }
            else{
                
                _annotationField.setText(pa.getAnnotation());
            }
        }

    }

    private class PointAnnotationBrowser extends JPanel {

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
        _editor.updateActiveAnnotation(null);
    }

    @Override
    public void selectionChanged(CoolMapObject object) {
        //selection changed
        System.out.println("Selection changed?");
        
        
        try {
            Set<Rectangle> selections = object.getCoolMapView().getSelections();
            if(selections.size() == 1){
                Rectangle sel = (Rectangle)selections.toArray()[0];
                if(sel.width > 1 || sel.height > 1){
                    return;
                }
                else{
                    VNode rowNode = object.getViewNodeRow(sel.y);
                    VNode colNode = object.getViewNodeColumn(sel.x);
                    
//                    System.out.println(rowNode + " " + colNode);
                    
                    PointAnnotation pa = object.getAnnotationStorage().getAnnotation(rowNode, colNode);
                    
//                    System.out.println(pa);
                    _editor.updateActiveAnnotation(pa);
                }
            }
            
            
            
        } catch (Exception e) {
            
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
