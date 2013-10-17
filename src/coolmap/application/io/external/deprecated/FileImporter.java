/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external.deprecated;

import coolmap.application.io.external.deprecated.CoolMapImporter;
import coolmap.data.CoolMapObject;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author gangsu
 */
public abstract class FileImporter implements CoolMapImporter {

    public CoolMapObject importFromFile(File file) {
        if (file == null || file.isDirectory()) {
            return null;
        }
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
            return importFromStream(stream, file.getName());
        } catch (Exception e) {
            return null;
        }
    }

    public abstract FileFilter getFileFilter();
}
