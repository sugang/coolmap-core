/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.io.internal.cmatrix.interfaces;

import coolmap.data.cmatrix.model.CMatrix;
import de.schlichtherle.truezip.file.TFile;

/**
 * write matrices to files
 * @author sugang
 */
public interface InternalCMatrixExporter {
    
    public void dumpData(CMatrix matrix, TFile zipFolder) throws Exception;
}
