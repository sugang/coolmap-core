/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.utils;

import coolmap.application.listeners.DataStorageListener;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.contology.model.COntology;
import java.util.HashSet;

/**
 *
 * @author gangsu
 */
public class DataMaster {

    private DataMaster() {
    }
    private static HashSet<DataStorageListener> _dataStorageListeners = new HashSet<DataStorageListener>();

    public static void addDataStorageListener(DataStorageListener lis) {
        if (lis != null) {
            _dataStorageListeners.add(lis);
        }
    }

    public static void removeDataStorageListener(DataStorageListener lis) {
        if (lis != null) {
            _dataStorageListeners.remove(lis);
        }
    }

    public static void fireCMatrixAdded(CMatrix newMatrix) {
        for (DataStorageListener lis : _dataStorageListeners) {
            lis.baseMatrixAdded(newMatrix);
        }
    }

    public static void fireCMatrixToBeRemoved(CMatrix newMatrix) {
        for (DataStorageListener lis : _dataStorageListeners) {
            lis.baseMatrixToBeRemoved(newMatrix);
        }
    }

    public static void fireCoolMapObjectAdded(CoolMapObject object) {
        for (DataStorageListener lis : _dataStorageListeners) {
            lis.coolMapObjectAdded(object);
        }
    }

    public static void fireCoolMapObjectToBeDestroyed(CoolMapObject object) {
        for (DataStorageListener lis : _dataStorageListeners) {
            lis.coolMapObjectToBeDestroyed(object);
        }
    }

    public static void fireCOntologyAdded(COntology ontology) {
        for (DataStorageListener lis : _dataStorageListeners) {
            lis.contologyAdded(ontology);
        }
    }

    public static void fireCOntologyToBeDestroyed(COntology ontology) {
        for (DataStorageListener lis : _dataStorageListeners) {
            lis.contologyToBeDestroyed(ontology);
        }
    }
}
