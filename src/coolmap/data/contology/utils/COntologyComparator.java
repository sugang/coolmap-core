/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.contology.utils;

import coolmap.data.contology.model.COntology;
import java.util.Comparator;

/**
 *
 * @author gangsu
 */
public final class COntologyComparator implements Comparator<COntology> {

    @Override
    public int compare(COntology t1, COntology t2) {
        return(t1.getName().compareTo(t2.getName()));
    }
    
}
