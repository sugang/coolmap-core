/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.listeners;

import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.contology.model.COntology;

/**
 *
 * @author gangsu
 */
public interface DataStorageListener {
    public void coolMapObjectAdded(CoolMapObject newObject);
    public void coolMapObjectToBeDestroyed(CoolMapObject objectToBeDestroyed);
    public void baseMatrixAdded(CMatrix newMatrix);
    public void baseMatrixToBeRemoved(CMatrix matrixToBeRemoved);
    public void contologyAdded(COntology ontology);
    public void contologyToBeDestroyed(COntology ontology);
}
