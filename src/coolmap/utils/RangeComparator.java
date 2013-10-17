/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import java.util.Comparator;

/**
 * all ranges here must all be closed - open type
 * @author gangsu
 */
public class RangeComparator implements Comparator<Range<Integer>>{

    /**
     * all ranges here are in the form of closed-open type
     * @param t
     * @param t1
     * @return 
     */
    @Override
    public int compare(Range<Integer> t, Range<Integer> t1) {
        if(t == null){
            return -1;
        }
        if(t1 == null){
            return 1;
        }
        
        if(t.lowerEndpoint() < t1.lowerEndpoint()){
            return -1;
        }
        if(t1.lowerEndpoint() < t.lowerEndpoint()){
            return 1;
        }
        else{
            //lower end point equal
            if(t.upperEndpoint() < t1.upperEndpoint()){
                return -1;
            }
            if(t1.upperEndpoint() < t.upperEndpoint()){
                return 1;
            }
            else{
                //both lower and upper are equal
                return 0;
            }
        }
    }
    
}
