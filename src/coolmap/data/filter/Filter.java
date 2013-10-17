/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.filter;

import coolmap.data.CoolMapObject;

/**
 *
 * @author gangsu
 */
public interface Filter<VIEW> {
    
    public boolean canPass(CoolMapObject<?, VIEW> data, int row, int col);
    public boolean canFilter(Class<?> objectClass);
    
}
