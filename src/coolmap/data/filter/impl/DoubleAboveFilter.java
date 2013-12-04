/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.filter.impl;

import coolmap.data.CoolMapObject;
import coolmap.data.filter.ViewFilter;
import coolmap.utils.graphics.UI;
import java.awt.FlowLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author gangsu
 */
public class DoubleAboveFilter extends ViewFilter<Double> {
    
    private JTextField _field = new JTextField();
    
    private void _setThreshold(){
        
        String text = _field.getText();
        try{
            _threshold = Double.parseDouble(text);
            _field.setBackground(UI.colorWhite);
            
//            System.out.println("New threshold" + _threshold);
        }
        catch(Exception e){
            _threshold = - Double.MAX_VALUE;
            _field.setBackground(UI.colorRedWarning);
        }
        
        notifyViewFilterUpdated();
    }
    
    public DoubleAboveFilter(){
        setName("Value above");
        _field.setColumns(20);
        _field.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent de) {
                _setThreshold();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                _setThreshold();
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                
            }
        });
    }
    

    private double _threshold = 0.0;
    
    @Override
    public boolean canPass(CoolMapObject<?, Double> data, int row, int col) {
        //System.out.println("Double can pass called");
        
        Double value = data.getViewValue(row, col);
        if(value == null || value.isNaN()){
            return false;
        }
        else{
            if(value >= _threshold){
                return true;
            }
            else{
                return false;
            }
        }
    }

    @Override
    public boolean canFilter(Class<?> objectClass) {
        if(objectClass == null){
            return false;
        }
        return Double.class.isAssignableFrom(objectClass);
    }

    @Override
    public JComponent getConfigUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(new JLabel(">"));
        panel.add(_field);
        _field.setText(_threshold+"");
        return panel;
    }
    
    
    
    
}
