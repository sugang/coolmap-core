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
public class VNodeHeightComparator implements Comparator<VNode> {

    @Override
    public int compare(VNode t1, VNode t2) {
        
        if(t1 == null && t2 == null){
            return 0;
        }
        if (t1 != null && t1.getViewIndex() == null && t2 != null && t2.getViewIndex() == null){
            return 0;
        }
        
        if (t1 == null || t1.getViewHeightInTree() == null) {
            return -1;
        } else if (t2 == null || t2.getViewHeightInTree() == null) {
            return 1;
        } else {
            return t1.getViewHeightInTree().compareTo(t2.getViewHeightInTree());
        }
    }
}
