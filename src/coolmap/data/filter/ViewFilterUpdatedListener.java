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
public interface ViewFilterUpdatedListener {
    
    /*
     * 
     */
    public void filterUpdated(ViewFilter filter, CoolMapObject object);
}
