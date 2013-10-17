/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.exceptions;

import java.io.File;

/**
 *
 * @author gangsu
 */
public class IOExceptionMaster {

    public static void newCOntologyDirectionException(int direction) throws Exception{
        throw new IllegalArgumentException("COntology argument:" + direction);
    }
    
    public static void newEntryFolderException(File entryFolder) throws Exception{
        throw new IllegalArgumentException("Entry folder:" + entryFolder.getAbsolutePath() + " does not exist or is not a folder");
    }
}
