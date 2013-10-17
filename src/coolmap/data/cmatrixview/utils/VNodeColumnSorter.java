/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.cmatrixview.utils;

import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VMatrix;
import coolmap.data.cmatrixview.model.VNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author gangsu
 */
public class VNodeColumnSorter implements Comparator<VNode> {

    private final CoolMapObject _obj;
    private final int _column;
    private final boolean _descending;
    private final List<Comparable> data;
    private final List<VNode> rowNodes;

    private VNodeColumnSorter() {
        this(null, -1, false);
    }

    public VNodeColumnSorter(CoolMapObject obj, int column, boolean descending) {
        _obj = obj;
        _column = column;
        _descending = descending;
        if (obj != null) {
            data = new ArrayList<Comparable>(obj.getViewNumRows());
            rowNodes = new ArrayList<VNode>(obj.getViewNumRows());
            for (int i = 0; i < obj.getViewNumRows(); i++) {
                data.add((Comparable)obj.getViewValue(i, column));
                rowNodes.add(obj.getViewNodeRow(i));
            }
        }
        else{
            data = null;
            rowNodes = null;
        }
    }

    @Override
    public int compare(VNode t1, VNode t2) {
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
        

        int rowIndex1 = t1.getViewIndex().intValue();
        int rowIndex2 = t2.getViewIndex().intValue();

//        if (rowIndex1 < 0 || rowIndex2 < 0 || rowIndex1 >= data.size() || rowIndex2 >= data.size()) {
//            return 0;
//        }

        Comparable v1 = data.get(rowIndex1);
        Comparable v2 = data.get(rowIndex2);
        
        
        
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
