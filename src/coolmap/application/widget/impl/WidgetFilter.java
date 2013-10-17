/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.widget.Widget;
import coolmap.data.CoolMapObject;
import coolmap.data.filter.CombinationFilter;
import coolmap.data.filter.ViewFilter;
import coolmap.data.filter.ViewFilterUpdatedListener;
import coolmap.data.filter.impl.DoubleAboveFilter;
import coolmap.data.filter.impl.DoubleBelowFilter;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashSet;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 *
 * @author gangsu
 */
public class WidgetFilter extends Widget implements ActiveCoolMapChangedListener, ViewFilterUpdatedListener {

    private JPanel _container = new JPanel();
    private JComboBox _filters = new JComboBox();
    private DefaultComboBoxModel _model = new DefaultComboBoxModel();
    private JScrollPane _scroller = new JScrollPane();
    private final LinkedHashSet<Class> _registeredFilters = new LinkedHashSet<Class>();
    private JButton _addFilter;
    private CoolMapObject _currentObject = null;
    private JToolBar _toolbar = new JToolBar();
    private JButton _executeButton;
    private JComboBox _filterMode = new JComboBox();

    private void _updateAvailableOptionList() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (Class cls : _registeredFilters) {
            try {
                model.addElement(cls.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        _filters.setModel(model);
    }

    public void registerFilter(String className) {
        try {
            Class cls = Class.forName(className);
            if (ViewFilter.class.isAssignableFrom(cls)) {
                _registeredFilters.add(cls);
                _updateAvailableOptionList();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void _initBuiltInFilters() {
        registerFilter(DoubleAboveFilter.class.getName());
        registerFilter(DoubleBelowFilter.class.getName());
    }

    public WidgetFilter() {
        super("Filter", W_MODULE, L_LEFTBOTTOM, UI.getImageIcon("filter"), "Filters");
        CoolMapMaster.addActiveCoolMapChangedListener(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_scroller, BorderLayout.CENTER);

//        _container.setLayout(new BorderLayout());
//        
//        _container.add(_toolbar, BorderLayout.NORTH);
        _toolbar.add(_filterMode);
        DefaultComboBoxModel model = new DefaultComboBoxModel(new String[]{"And", "Or"});
        _filterMode.setModel(model);
        _filterMode.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
                if(_currentObject == null){
                    return;
                }
                
                if (ie.getStateChange() == ItemEvent.SELECTED) {
//                    _applyAggregator();
//                    _aggregators.setToolTipText(((CMatrixAggregator)_aggregators.getSelectedItem()).getDescription());
                    if(_filterMode.getSelectedIndex() == 0){
                       _currentObject.setViewFilterMode(CombinationFilter.AND);
                       _toolbar.setBackground(UI.colorLightGreen0);
                    }else if(_filterMode.getSelectedIndex() == 1){
                        _currentObject.setViewFilterMode(CombinationFilter.OR);
                        _toolbar.setBackground(UI.colorLightGreen0);
                    }
                }
            }
        });


        _toolbar.addSeparator();

        _toolbar.setFloatable(false);
        _toolbar.add(_filters);
        getContentPane().add(_toolbar, BorderLayout.NORTH);
//

        _addFilter = new JButton(UI.getImageIcon("plusSmall"));
        _addFilter.setToolTipText("Add a new filter");
        _toolbar.add(_addFilter);
        _addFilter.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if(_currentObject == null)
                    return;
                try{
                    ViewFilter filter = (ViewFilter)(((ViewFilter)_filters.getSelectedItem()).getClass().newInstance());
                    _currentObject.addViewFilter(filter);
                    _updateList();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                
            }
        });
        
        
        
//        _container.add(_scroller, BorderLayout.CENTER);
//        _scroller.setViewportView(_toolbar);
//
//        _initBuiltInFilters();
//
////        _activeFilters.add(new FilterEntry(new DoubleAboveFilter()));
////        _activeFilters.add(new FilterEntry(new DoubleAboveFilter()));
        _executeButton = new JButton(UI.getImageIcon("refresh"));
        _executeButton.setToolTipText("Apply filter");
        _executeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (_currentObject == null) {
                    return;
                }

                _currentObject.getCoolMapView().updateCanvasEnforceOverlay();

                //if it works
                _toolbar.setBackground(UI.colorLightGreen0);
            }
        });
        _toolbar.add(_executeButton);


        //_updateList();
        _initBuiltInFilters();

        _filters.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
                JLabel label = (JLabel) super.getListCellRendererComponent(jlist, o, i, bln, bln1);
                ViewFilter filter = (ViewFilter) o;
                if (_currentObject == null || !filter.canFilter(_currentObject.getViewClass())) {
                    label.setEnabled(false);
                    label.setFocusable(false);
                    label.setBackground(UI.colorRedWarning);
                } else {
                    label.setEnabled(true);
                    label.setFocusable(true);
                }




                return label;
            }
        });


    }

    private void _updateList() {
        if (_currentObject == null) {
            JPanel panel = new JPanel();
            _scroller.setViewportView(panel);
            panel.setBackground(UI.colorBlack2);
            return;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        for (Object filter : _currentObject.getActiveFilters()) {
            FilterEntry entry = new FilterEntry((ViewFilter) filter);
            ((ViewFilter) filter).addViewFilterUpdatedListener(this);
            panel.add(entry);
        }

        panel.setBackground(UI.colorBlack2);
        _scroller.setViewportView(panel);
    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        _currentObject = activeCoolMapObject;
        if(_currentObject != null){
            int mode = _currentObject.getViewFilterMode();
            if(mode == CombinationFilter.AND){
                _filterMode.setSelectedIndex(0);
            }
            else{
                _filterMode.setSelectedIndex(1);
            }
            
            _filters.repaint();
        }
        _updateList();
    }

    @Override
    public void filterUpdated(ViewFilter filter, CoolMapObject object) {
        //The filter was updated, need to reexecute
        _toolbar.setBackground(UI.colorRedWarning);
    }

    private class FilterEntry extends JPanel {

        private ViewFilter _filter;

        public FilterEntry(ViewFilter filter) {
            _filter = filter;
            JButton remove = new JButton(UI.getImageIcon("trashBin"));
            JToolBar toolBar = new JToolBar();
            toolBar.add(remove);
            remove.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    removeViewFilter(FilterEntry.this);
                }
            });

            if (filter != null) {
                this.setLayout(new BorderLayout());
                JLabel label = new JLabel(_filter.getName());
                label.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 2));
                label.setToolTipText(filter.getDescrpition());
                toolBar.add(label);
                toolBar.setFloatable(false);
                add(toolBar, BorderLayout.NORTH);
                if (filter != null) {
                    add(filter.getConfigUI(), BorderLayout.CENTER);
                }

            }
            setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, UI.colorBlack4));
            //setMaximumSize(new Dimension(20000,200));
        }

        public ViewFilter getFilter() {
            return _filter;
        }
    }

    private void removeViewFilter(FilterEntry entry) {
        ViewFilter filter = entry.getFilter();
        if (_currentObject != null) {
            filter.notifyViewFilterUpdated();
            _currentObject.removeViewFilter(entry.getFilter());
            _toolbar.setBackground(UI.colorLightGreen0);

        }
        _updateList();
    }
}
