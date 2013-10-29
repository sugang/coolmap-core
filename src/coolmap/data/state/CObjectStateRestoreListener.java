/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.data.state;

import coolmap.data.state.CoolMapState;

/**
 *
 * @author sugang
 */
public interface CObjectStateRestoreListener {
    
    public abstract void stateToBeRestored(CoolMapState state);
}
