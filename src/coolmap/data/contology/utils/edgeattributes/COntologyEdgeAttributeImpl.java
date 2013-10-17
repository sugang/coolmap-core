/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.contology.utils.edgeattributes;

import java.util.HashMap;

/**
 *
 * @author gangsu
 */
public class COntologyEdgeAttributeImpl {
    
    private HashMap<String, Object> _addtionalAttributes = new HashMap<String, Object>(5);
    
    public void setAttribute(String name, Object attr){
        if(name == null || name.length() == 0){
            return;
        }
        _addtionalAttributes.put(name, attr);
    }
    
    public Object getAttribute(String name){
        if(name == null || name.length() == 0){
            return null;
        }
        return _addtionalAttributes.get(name);
    }
    
    
    
    private float _length;
    
    private COntologyEdgeAttributeImpl(){
        this(0);
    }
    
    public COntologyEdgeAttributeImpl(float f){
        if(f < 0)
            f = 0;
        _length = f;
    }
    
    public Float getNormalizedLength() {
        return _length;
    }
    
}
