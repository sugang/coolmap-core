/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.aggregator.model;

import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.utils.Tools;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.tools.Tool;
import javax.xml.bind.JAXBElement;

/**
 *
 * @author gangsu
 */
public abstract class CAggregator<BASE, VIEW> {

    public abstract VIEW getAggregation(BASE item, Collection<CMatrix> matrices, Integer rowIndex, Integer columnIndex);

    public abstract VIEW getAggregation(Collection<BASE> item, Collection<CMatrix> matrices, Collection<Integer> rowIndices, Collection<Integer> columnIndices);
    
    private final Class _inClass;
    private final Class _outClass;
    private final String _ID;
    private String _name;
    private String _description;
    private JComponent _ui;
    private CoolMapObject _coolMapObject;
    private String _tipName = null;
    
    public CAggregator() {
        _inClass = Object.class;
        _outClass = Object.class;
        _ID = Tools.randomID();
        _name = null;
        _description = null;
        _coolMapObject = null;
    }
    
//    public CAggregator(CoolMapObject object){
//        this();
//    }
    
    public void setCoolMapObject(CoolMapObject object){
        _coolMapObject = object;
    }

    public CAggregator(String name, String tipName, String description, Class<BASE> inClass, Class<VIEW> outClass, CoolMapObject obj) {
        _ID = Tools.randomID();
        _name = name;
        _inClass = inClass;
        _outClass = outClass;
        _coolMapObject = obj;
        _tipName = tipName;
        _description = description;
    }

    public final String getDescription() {
        return "<html>" + _description + "<br/><hr/>" + "aggregate from type: <br/><b>[" + _inClass + "]</b><br/> to type: <br/><b>[" + _outClass + "]</b></html>";
    }

    public boolean canAggregate(Class<?> cls) {
        if (cls == null || _inClass == null) {
            return false;
        }
//        System.out.println(_name + " " + _inClass);
        return _inClass.isAssignableFrom(cls);
    }

    public Class<BASE> getBaseClass() {
        return (Class<BASE>) _inClass;
    }

    public Class<VIEW> getViewClass() {
        return (Class<VIEW>) _outClass;
    }

    public final String getName() {
        return _name;
    }

    @Override
    public String toString() {
        return _name + " " + _inClass + " " + _outClass;
    }
    
    public JComponent getConfigUI(){
        JLabel label = new JLabel("No configuration needed.");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        return label;
    };
    
    public String getID(){
        return _ID;
    }
    
    public void aggregatorUpdated(){
        if(_coolMapObject != null){
            //fire data updated
            _coolMapObject.notifyAggregatorUpdated();
        }
    }
    
    public String getTipName(){
        return _tipName;
    }
    

    
    
}
