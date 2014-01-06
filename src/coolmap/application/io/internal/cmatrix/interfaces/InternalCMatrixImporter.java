/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.io.internal.cmatrix.interfaces;

import coolmap.data.cmatrix.model.CMatrix;
import de.schlichtherle.truezip.file.TFile;
import org.json.JSONObject;

/**
 * import data to a certain class
 * @author sugang
 */
public interface InternalCMatrixImporter {
    
    public CMatrix loadData(TFile matrixFolder, JSONObject properties) throws Exception;
    
    /**
     * returns what kind of importer this data is imported to; for example double matrix.
     * @return 
     */
    public Class<? extends CMatrix> getImportedClass();
}
