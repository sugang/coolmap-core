/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.listeners;

import coolmap.data.CoolMapObject;

/**
 *
 * @author gangsu
 */
public interface CObjectListener {
    /**
     * possible cause of data update: change base matrix, change view matrix, change rows/columns insert, remove, expand...
     * @param object 
     */
    public void aggregatorUpdated(CoolMapObject object);
    
    public void rowsChanged(CoolMapObject object);
    
    public void columnsChanged(CoolMapObject object);
    
    public void baseMatrixChanged(CoolMapObject object);
    
//    public void stateStorageUpdated(CoolMapObject object);
    
    public void viewRendererChanged(CoolMapObject object);
    
    public void viewFilterChanged(CoolMapObject object);
    
    public void nameChanged(CoolMapObject object);

}
