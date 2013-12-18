/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.io.external.interfaces;

import coolmap.data.CoolMapObject;
import coolmap.data.contology.model.COntology;
import java.io.File;
import java.util.Set;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author sugang
 */
public interface ImportData {
    
    /**
     * import data and save imported from file.. it can be from multiple files
     * @param file
     * @throws Exception 
     */
    public void importFromFile(File... file) throws Exception;
    
    /**
     * get display label
     * @return 
     */
    public String getLabel();
        
    public FileNameExtensionFilter getFileNameExtensionFilter();
    
    public Set<CoolMapObject> getImportedCoolMapObjects();
    public Set<COntology> getImportedCOntology();

    public void configure(File... file);
    
    
}
