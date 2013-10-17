/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.cmatrixview.utils;

import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author gangsu
 */
public class VNodeRowSorter implements Comparator<VNode> {

    private final CoolMapObject _obj;
    private final int _row;
    private final boolean _descending;
    private final List<Comparable> _data;
    private final List<VNode> _colNodes;

    private VNodeRowSorter() {
        this(null, -1, false);
    }

    public VNodeRowSorter(CoolMapObject obj, int row, boolean descending) {
        _obj = obj;
        _row = row;
        _descending = descending;
        if (obj != null) {
            _data = new ArrayList<Comparable>(obj.getViewNumColumns());
            _colNodes = new ArrayList<VNode>(obj.getViewNumColumns());
            for (int j = 0; j < obj.getViewNumColumns(); j++) {
                _data.add((Comparable)obj.getViewValue(row, j));
                _colNodes.add(obj.getViewNodeColumn(j));
            }
        }
        else{
            _data = null;
            _colNodes = null;
        }
    }

    @Override
    public int compare(VNode t1, VNode t2) {
//        if (t1 == null || t2 == null || t1.getViewIndex() == null || t2.getViewIndex() == null) {
//            return 0;
//        }
        if(t1 == null && t2 != null){
            return -1;
        }
        if(t2 == null && t1 != null){
            return 1;
        }
        if(t1 == null && t2 == null){
            return 0;
        }
        
        if(t1.getViewIndex() == null && t2.getViewIndex() != null){
            return -1;
        }
        if(t2.getViewIndex() == null && t1.getViewIndex() != null){
            return 1;
        }
        if(t1.getViewIndex() == null && t2.getViewIndex() == null ){
           return 0;
        }
        
        

        int colIndex1 = t1.getViewIndex().intValue();
        int colIndex2 = t2.getViewIndex().intValue();

//        if (colIndex1 < 0 || colIndex2 < 0 || colIndex1 >= _data.size() || colIndex2 >= _data.size()) {
//            return 0;
//        }
        
        
        Comparable v1 = _data.get(colIndex1);
        Comparable v2 = _data.get(colIndex2);
        
//        if(v1 == null || v2 == null){
//            return 0;
//        }
//        if(v1 == null && v2 != null){
//            return 
//        }
        if(v1 == null && v2 != null){
            return 1;
        }
        else if(v1 != null && v2 == null){
            return -1;
        }
        else if(v1 == null && v2 == null){
            return 0;
        }
        

        if (!_descending) {
            return v1.compareTo(v2);
        } else {
            return v2.compareTo(v1);
        }
    }
    
}
