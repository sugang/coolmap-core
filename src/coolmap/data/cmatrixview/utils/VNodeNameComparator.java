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
public class VNodeNameComparator implements Comparator<VNode>{

    @Override
    public int compare(VNode t1, VNode t2) {
        //throw new UnsupportedOperationException("Not supported yet.");
        if(t1 == null || t1.getName() == null){
            return 1;
        }
        else if(t2 == null || t2.getName() == null){
            return -1;
        }
        else{
            return t1.getName().compareTo(t2.getName());
        }
    }
    
}
