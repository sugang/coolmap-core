/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author gangsu
 */
public class FileOnlyExtensionFilter extends FileFilter{
    private FileNameExtensionFilter extensionFilter;
    private FileOnlyExtensionFilter(){
        
    }
    
    public FileOnlyExtensionFilter(String description, String...extensions){
        extensionFilter = new FileNameExtensionFilter(description, extensions);
    }

    @Override
    public boolean accept(File file) {
        if(file.isFile() && extensionFilter.accept(file)){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public String getDescription() {
        return extensionFilter.getDescription();
    }
}
