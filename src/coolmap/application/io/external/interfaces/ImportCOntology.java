/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.io.external.interfaces;

import coolmap.data.contology.model.COntology;
import java.io.File;
import java.util.Set;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * This interface must be changed as well, may need to import from complex files like excel that requires config windows
 * @author sugang
 */
public interface ImportCOntology {
    
    
    
    
    public void importFromFiles(File... file) throws Exception;
    public void importFromFile(File file) throws Exception;
    public String getLabel();
    public FileNameExtensionFilter getFileNameExtensionFilter();
    public Set<COntology> getImportedCOntology();
    public void configure(File... file);
    public boolean onlyImportFromSingleFile();
    
}
