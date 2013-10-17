/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.internal.cmatrix;

import coolmap.data.cmatrix.model.CMatrix;
import java.io.File;

/**
 * A CMatrix may be saved into multiple files
 * Therefore, a directory stores all the files
 * @author gangsu
 */
public interface ICMatrixIO<T> {
    
    public CMatrix<T> importFromDirectory (String ID, String name, int numRow, int numColumn, File directory, Class matrixClass) throws Exception;
    public void exportToDirectory(CMatrix<T> matrix, File directory) throws Exception;
    public String getDisplayName();
}
