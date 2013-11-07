/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.widget.Widget;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.CoolMapObject;
import coolmap.utils.Config;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author gangsu
 */
public class WidgetViewRenderer extends Widget implements ActiveCoolMapChangedListener {

    private JPanel _container = new JPanel();
    private JComboBox _viewRenderers = new JComboBox();
    private DefaultComboBoxModel _model = new DefaultComboBoxModel();
    private JScrollPane _scroller = new JScrollPane();
    private final LinkedHashSet<Class> _registeredRenderers = new LinkedHashSet<Class>();

    public LinkedHashSet<ViewRenderer> getLoadedRenderers() {
        LinkedHashSet<ViewRenderer> renderers = new LinkedHashSet<ViewRenderer>();
        for (Class cls : _registeredRenderers) {
            try {
                renderers.add((ViewRenderer) cls.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return renderers;
    }

    public WidgetViewRenderer() {
        super("View Render", W_MODULE, L_LEFTBOTTOM, UI.getImageIcon("paintRoll"), "View Renderers");
        CoolMapMaster.addActiveCoolMapChangedListener(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_container);
        _container.setLayout(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        _container.add(toolbar, BorderLayout.NORTH);
        toolbar.setFloatable(false);

        _container.add(_scroller, BorderLayout.CENTER);
        _viewRenderers.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                    if (object == null) {
                        return;
                    }
                    try {
                        ViewRenderer renderer = ((ViewRenderer) _viewRenderers.getSelectedItem()).getClass().newInstance();
                        if (!renderer.canRender(object.getViewClass())) {
                            ViewRenderer r = object.getViewRenderer();
                            if (r != null) {
                                _scroller.setViewportView(r.getConfigUI());
                                for (int i = 0; i < _viewRenderers.getItemCount(); i++) {
                                    if (_viewRenderers.getItemAt(i).getClass().equals(r.getClass())) {
                                        _viewRenderers.setSelectedIndex(i);
                                        break;
                                    }
                                }
                            }
                        } else {
                            object.setViewRenderer(renderer);
                            _scroller.setViewportView(object.getViewRenderer().getConfigUI());
                        }
                    } catch (Exception e) {
                        _scroller.setViewportView(null);
                        object.notifyViewRendererUpdated();
                    }

                    _updatetip();
                }
            }
        });

        _viewRenderers.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
                JLabel label = (JLabel) super.getListCellRendererComponent(jlist, o, i, bln, bln1);
                if (o == null) {
                    return label;
                }
                if (CoolMapMaster.getActiveCoolMapObject() != null) {
                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    ViewRenderer renderer = (ViewRenderer) o;
                    if (renderer != null && obj != null && renderer.canRender(obj.getViewClass())) {
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

        _initBuiltInViewRenderers();
        toolbar.add(_viewRenderers);
    }

    private void _initBuiltInViewRenderers() {

//        registerViewRenderer(DoubleToColor.class.getName());
//        registerViewRenderer(DoubleToBar.class.getName());
//        registerViewRenderer(DoubleToShape.class.getName());
//        registerViewRenderer(DoubleToNumber.class.getName());
//        registerViewRenderer(DoubleToBoxPlot.class.getName());
//        registerViewRenderer(DoubleToSortedLines.class.getName());
//        registerViewRenderer(NetworkToForceLayout.class.getName());
//        registerViewRenderer(ImageTest.class.getName());
//        registerViewRenderer(Politics.class.getName());
//        
        if (Config.isInitialized()) {
            try{
                
                JSONArray rendererToLoad = Config.getJSONConfig().getJSONObject("widget").getJSONObject("config").getJSONObject("coolmap.application.widget.impl.WidgetViewRenderer").getJSONArray("load");
                for(int i=0; i<rendererToLoad.length(); i++){
                    try{
                        String rendererClass = rendererToLoad.getString(i);
                        System.err.println(rendererClass);
                        registerViewRenderer(rendererClass);
                    }
                    catch(JSONException exception){
                        System.out.println("parsing error");
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
                        
            

        }

    }
    
    
    private void _updateList() {
        _model = new DefaultComboBoxModel();
        for (Class cls : _registeredRenderers) {
            try {
                _model.addElement(cls.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        System.out.println("Model:" + _model);
        _viewRenderers.setModel(_model);
    }

    public void registerViewRenderer(String className) {
        try {
            Class cls = Class.forName(className);
            if (ViewRenderer.class.isAssignableFrom(cls)) {
                _registeredRenderers.add(cls);
                _updateList();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        if (activeCoolMapObject != null) {
            ViewRenderer renderer = activeCoolMapObject.getViewRenderer();
            if (renderer != null) {
                _scroller.setViewportView(renderer.getConfigUI());
                for (int i = 0; i < _viewRenderers.getItemCount(); i++) {
                    if (_viewRenderers.getItemAt(i).getClass().equals(renderer.getClass())) {
                        _viewRenderers.setSelectedIndex(i);
                        _updatetip();
                        return;
                    }
                    //register;
                }
                registerViewRenderer(renderer.getClass().getName());
                for (int i = 0; i < _viewRenderers.getItemCount(); i++) {
                    if (_viewRenderers.getItemAt(i).getClass().equals(renderer.getClass())) {
                        _viewRenderers.setSelectedIndex(i);
                        _updatetip();
                        return;
                    }
                    //register;
                }
            }
        } else {
            _scroller.setViewportView(null);
        }
    }

    private void _updatetip() {
        _viewRenderers.setToolTipText(((ViewRenderer) _viewRenderers.getSelectedItem()).getDescription());
    }
}
