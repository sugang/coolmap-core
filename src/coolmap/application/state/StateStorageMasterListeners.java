/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.state;

import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.data.CoolMapObject;

/**
 *
 * @author sugang
 */
public class StateStorageMasterListeners implements ActiveCoolMapChangedListener {

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        StateStorageMaster.activeCoolMapChanged(oldObject, activeCoolMapObject);
    }
    
}
