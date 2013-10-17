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
public class SelectionFilter<VIEW> implements Filter<VIEW> {
    
    @Override
    public boolean canPass(CoolMapObject<?, VIEW> data, int row, int col) {
        if(data.isCellSelected(row, col)){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public boolean canFilter(Class<?> objectClass) {
        return true;
    }
    
}
