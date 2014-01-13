/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.listeners;

import coolmap.data.cmatrix.model.CMatrix;

/**
 *
 * @author sugang
 */
public interface CMatrixListener {
    
    public void cmatrixNameChanged(CMatrix mx);
    public void cmatrixValueUpdated(CMatrix mx);
    
}
