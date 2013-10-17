/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils;

import java.io.File;
import java.util.UUID;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author gangsu
 */
public class Tools {

    private static final JFileChooser _fileChooser = new JFileChooser();
    private static final JColorChooser _colorChooser = new JColorChooser();
    private static final JFileChooser _folderChooser = new JFileChooser();

    public static void initialize() {
//        _folderChooser.setFileFilter(new FileFilter() {
//
//            @Override
//            public boolean accept(File file) {
//                if (file != null && file.isDirectory()) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//
//            @Override
//            public String getDescription() {
//                return "Project home folder";
//            }
//        });
        _folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    public static JColorChooser getColorChooser() {
        return _colorChooser;
    }

    public static JFileChooser getFileChooser() {

        _fileChooser.setFileFilter(null);
        return _fileChooser;
    }

    public static JFileChooser getPNGFileChooser() {
//        if(_fileChooser == null){
//            _fileChooser = new JFileChooser();
//        }
        FileNameExtensionFilter f = new FileNameExtensionFilter("PNG image files", "png");
        _fileChooser.setFileFilter(f);
        return _fileChooser;
    }

    public static JFileChooser getTXTFileChooser() {
//        if(_fileChooser == null){
//            _fileChooser = new JFileChooser();
//        }
        FileNameExtensionFilter f = new FileNameExtensionFilter("TXT text files", "txt");
        _fileChooser.setFileFilter(f);
        return _fileChooser;
    }

//    public static JFileChooser getCPWFileChooser() {
//        FileOnlyExtensionFilter f = new FileOnlyExtensionFilter("CoolMap Workspace files", "cpw");
//        _fileChooser.setFileFilter(f);
//        return _fileChooser;
//    }
    public static JFileChooser getFolderChooser() {

        return _folderChooser;
    }

    public static JFileChooser getCustomFileChooser(FileFilter filter) {
        _fileChooser.setFileFilter(filter);
        return _fileChooser;
    }

    public static File appendFileExtension(File f, String sfx) {
        if (f == null || sfx == null || sfx.length() == 0 || sfx.trim().length() == 0) {
            return f;
        } else {
            sfx = sfx.toLowerCase();
            if (!f.getName().toLowerCase().matches("\\." + sfx + "$")) {
                f = new File(f.getAbsolutePath() + "." + sfx);
                return f;
            } else {
                return f;
            }
        }
    }

    public static String randomID() {

        //return UUID.randomUUID().toString();
        return DigestUtils.md5Hex(UUID.randomUUID().toString());
    }

    public static String removeFileExtension(String s) {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1) {
            return filename;
        }

        return filename.substring(0, extensionIndex);
    }

    public static double[][] convertViewToDouble(Object[][] view) {
        if (view == null) {
            return null;
        }
        double[][] m = new double[view.length][view[0].length];
        Object o;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                o = view[i][j];
                if (o == null) {
                    m[i][j] = Double.NaN;
                } else {
                    m[i][j] = ((Double) o).doubleValue();
                }
            }
        }
        return m;
    }
}
