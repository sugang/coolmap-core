/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.listeners;

import coolmap.data.CoolMapObject;

/**
 *
 * @author gangsu
 */
public interface ActiveCoolMapChangedListener {
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject);
}
