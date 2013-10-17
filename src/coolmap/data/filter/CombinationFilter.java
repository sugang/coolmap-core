/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.filter;

import coolmap.data.CoolMapObject;
import java.util.*;

/**
 *
 * @author gangsu
 */
public class CombinationFilter implements Filter {

    public static final int OR = 1;
    public static final int AND = 0;
    
    
    public int getFilterMode(){
        return _mode;
    }
    
    private LinkedHashSet<ViewFilter> _filters = new LinkedHashSet<ViewFilter>();
    private int _mode = AND;
    
    private CoolMapObject _coolMapObject;
    
    private CombinationFilter(){
        _coolMapObject = null;
    }
    
    public CombinationFilter(CoolMapObject object){
        _coolMapObject = object;
    }
    
    public void addFilters(Collection<ViewFilter> filters){
        filters.removeAll(Collections.singletonList(null));
        _filters.addAll(filters);
    }
    
    public void addFilter(ViewFilter filter){
        if(filter != null){
            _filters.add(filter);
            filter.setParentObject(_coolMapObject);
        }
    }
    
    public void removeFilter(ViewFilter filter){
        if(filter != null){
            _filters.remove(filter);
            filter.destroy();
        }
    }
    
    public void setMode(int mode){
        _mode = mode;
    } 
    
    public void clearFilters(){
        for(ViewFilter filter : _filters){
            filter.destroy();
        }
        _filters.clear();
    }
    
    public ArrayList<ViewFilter> getCurrFilters(){
        return new ArrayList<ViewFilter>(_filters);
    }
    
    @Override
    public boolean canPass(CoolMapObject data, int row, int col) {
        if(_filters.isEmpty()){
            return true;
        }
                
        if(_mode == OR){
            for(Filter filter : _filters){
                if(filter.canPass(data, row, col)){
                    return true;
                }
            }
            return false;
        }
        else if(_mode == AND){
            for(Filter filter : _filters){
                if(!filter.canPass(data, row, col)){
                    return false;
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public boolean canFilter(Class objectClass) {
        if(objectClass == null)
            return false;
        else if(Object.class.isAssignableFrom(objectClass)){
            return true;
        }
        else{
            return false;
        }
    }
    
}
