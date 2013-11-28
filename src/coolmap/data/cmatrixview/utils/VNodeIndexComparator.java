/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.cmatrixview.utils;

import coolmap.data.cmatrixview.model.VNode;
import java.util.Comparator;

/**
 *
 * @author gangsu
 */
public class VNodeIndexComparator implements Comparator<VNode> {

    @Override
    public int compare(VNode t1, VNode t2) {

        if(t1 == null && t2 == null){
            return 0;
        }
        if (t1 != null && t1.getViewIndex() == null && t2 != null && t2.getViewIndex() == null){
            return 0;
        }
        
        if (t1 == null || t1.getViewIndex() == null) {
            return 1;
        } else if (t2 == null || t2.getViewIndex() == null) {
            return -1;
        } else {
            return t1.getViewIndex().compareTo(t2.getViewIndex());
        }
    }

}
