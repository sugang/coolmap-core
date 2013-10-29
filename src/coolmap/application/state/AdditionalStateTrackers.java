/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.state;

import coolmap.application.listeners.DataStorageListener;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.contology.model.COntology;
import coolmap.data.state.misc.ZoomTracker;

/**
 *
 * @author sugang
 */
public class AdditionalStateTrackers implements DataStorageListener{

    @Override
    public void coolMapObjectAdded(CoolMapObject newObject) {
        //Adds additional trackers
        ZoomTracker tracker = new ZoomTracker(newObject);
        newObject.addCObjectStateRestoreListener(tracker);
    }

    @Override
    public void coolMapObjectToBeDestroyed(CoolMapObject objectToBeDestroyed) {
    }

    @Override
    public void baseMatrixAdded(CMatrix newMatrix) {
    }

    @Override
    public void baseMatrixToBeRemoved(CMatrix matrixToBeRemoved) {
    }

    @Override
    public void contologyAdded(COntology ontology) {
    }

    @Override
    public void contologyToBeDestroyed(COntology ontology) {
    }
    
    
}
