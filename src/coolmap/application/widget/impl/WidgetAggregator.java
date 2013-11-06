/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.widget.Widget;
import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.impl.*;
import coolmap.data.aggregator.model.CAggregator;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashSet;
import javax.swing.*;

/**
 *
 * @author gangsu
 */
public class WidgetAggregator extends Widget implements ActiveCoolMapChangedListener {

    private JPanel _container = new JPanel();
    private JComboBox _aggregators = new JComboBox();
    private DefaultComboBoxModel _model = new DefaultComboBoxModel();
    private JScrollPane _scroller = new JScrollPane();
    private final LinkedHashSet<Class> _registeredAggregators = new LinkedHashSet<Class>();

    public LinkedHashSet<CAggregator> getLoadedRenderers() {
        LinkedHashSet<CAggregator> aggrs = new LinkedHashSet<CAggregator>();
        for (Class cls : _registeredAggregators) {
            try {
                aggrs.add((CAggregator) cls.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return aggrs;
    }

    private void _initBuiltInAggregators() {
        registerAggregator(PassThrough.class.getName());
        registerAggregator(DoubleDoubleMax.class.getName());
        registerAggregator(DoubleDoubleMean.class.getName());
        registerAggregator(DoubleDoubleMedian.class.getName());
        registerAggregator(DoubleDoubleMin.class.getName());
        registerAggregator(DoubleDoubleSum.class.getName());
        registerAggregator(DoubleDoubleSD.class.getName());
        registerAggregator(DoubleDoubleVariance.class.getName());
        registerAggregator(DoubleToNetwork.class.getName());

        //registerAggregator(DoubleDoubleMin.class.getName());
        //registerAggregator(DoubleDoubleMean.class.getName());
        _updatetip();
    }

    public WidgetAggregator() {
        super("Aggregator", W_MODULE, L_LEFTCENTER, UI.getImageIcon("grid"), "Aggregators");
        CoolMapMaster.addActiveCoolMapChangedListener(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_container);
        _container.setLayout(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        _container.add(toolbar, BorderLayout.NORTH);
        toolbar.setFloatable(false);
        toolbar.add(_aggregators);
        _container.add(_scroller, BorderLayout.CENTER);

        _aggregators.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {

                    //
                    CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
//                    System.out.println("Active object:" + object.getName());
                    
                    if (object != null) {


                        try {
                            CAggregator aggr = ((CAggregator) _aggregators.getSelectedItem()).getClass().newInstance();
                            if (!aggr.canAggregate(object.getBaseClass())) {
                                CAggregator aggregator = object.getAggregator();
                                if (aggregator != null) {
                                    _scroller.setViewportView(aggregator.getConfigUI());
                                    for (int i = 0; i < _aggregators.getItemCount(); i++) {
                                        if (_aggregators.getItemAt(i).getClass().equals(aggregator.getClass())) {
                                            _aggregators.setSelectedIndex(i);
                                            break;
                                        }
                                    }
                                } else {
                                    _scroller.setViewportView(null);
                                }
                                return;
                            } else {
                                //can aggregate, what the fuck
                                
                                object.setAggregator(aggr);
                                _scroller.setViewportView(object.getAggregator().getConfigUI());
                            }
                        } catch (Exception e) {
                            //Failed
                            _scroller.setViewportView(null);
                            object.notifyAggregatorUpdated();
                            e.printStackTrace();
                        }
                    }

                    _updatetip();
                }
            }
        });

        _aggregators.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
                JLabel label = (JLabel) super.getListCellRendererComponent(jlist, o, i, bln, bln1);
                if(o == null){
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


        _initBuiltInAggregators();

    }

    private void _updatetip() {
        _aggregators.setToolTipText(((CAggregator) _aggregators.getSelectedItem()).getDescription());
    }

    public void registerAggregator(String className) {
        try {
//            System.out.println(className);
            Class cls = Class.forName(className);

            if (CAggregator.class.isAssignableFrom(cls)) {
                _registeredAggregators.add(cls);
                _updateList();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void _updateList() {
        _model = new DefaultComboBoxModel();
//        Object[] registeredAggregators = _registeredAggregators.toArray();
//        Arrays.sort(registeredAggregators);

        for (Class cls : _registeredAggregators) {
            try {
                _model.addElement(cls.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        _aggregators.setModel(_model);

    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        if (activeCoolMapObject != null) {
            CAggregator aggregator = activeCoolMapObject.getAggregator();
            if (aggregator != null) {
                _scroller.setViewportView(aggregator.getConfigUI());
                for (int i = 0; i < _aggregators.getItemCount(); i++) {
                    if (_aggregators.getItemAt(i).getClass().equals(aggregator.getClass())) {
                        _aggregators.setSelectedIndex(i);
                        _updatetip();
                        return;
                    }
                    //register;
                }
                registerAggregator(aggregator.getClass().getName());
                for (int i = 0; i < _aggregators.getItemCount(); i++) {
                    if (_aggregators.getItemAt(i).getClass().equals(aggregator.getClass())) {
                        _aggregators.setSelectedIndex(i);
                        _updatetip();
                        return;
                    }
                    //register;
                }

            } else {
                _scroller.setViewportView(null);
                _updatetip();
            }

        } else {
            _scroller.setViewportView(null);
            _updatetip();
        }



        //check to set the availability


    }
}
