/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils;

import com.google.common.collect.Range;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author gangsu
 */
public class Tools {

    private static final JFileChooser _fileChooser = new JFileChooser();
    private static final JColorChooser _colorChooser = new JColorChooser();
    private static final JFileChooser _folderChooser = new JFileChooser();

    public static JLabel createJLabel(String text, Icon icon, String tooltip, Border border) {

        JLabel label = new JLabel();
        if (text != null) {
            label.setText(text);
        }
        if (icon != null) {
            label.setIcon(icon);
        }
        if (border != null) {
            label.setBorder(border);
        }
        if (tooltip != null) {
            label.setToolTipText(tooltip);
        }

        return label;

    }

    public static HashSet<Range<Integer>> createRangesFromIndices(ArrayList<Integer> indices) {
        try {

            Collections.sort(indices);
            int startIndex = indices.get(0);
            int currentIndex = startIndex;

            HashSet<Range<Integer>> selectedRanges = new HashSet<Range<Integer>>();

            for (Integer index : indices) {
                if (index <= currentIndex + 1) {
                    currentIndex = index;
                    continue;
                } else {
                    selectedRanges.add(Range.closedOpen(startIndex, currentIndex + 1));
                    currentIndex = index;
                    startIndex = currentIndex;
                }
            }
            selectedRanges.add(Range.closedOpen(startIndex, currentIndex + 1));

            return selectedRanges;

        } catch (Exception e) {
            return null;
        }
    }

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

    public static JFileChooser getCustomMultiFileChooser(FileFilter filter) {
        FileFilter[] filters = _fileChooser.getChoosableFileFilters();
        _fileChooser.setMultiSelectionEnabled(true);
        for (FileFilter f : filters) {
            _fileChooser.removeChoosableFileFilter(f);
        }

        _fileChooser.setFileFilter(filter);
        return _fileChooser;
    }

    public static JFileChooser getCustomSingleFileChooser(FileFilter filter) {
        FileFilter[] filters = _fileChooser.getChoosableFileFilters();
        _fileChooser.setMultiSelectionEnabled(false);
        for (FileFilter f : filters) {
            _fileChooser.removeChoosableFileFilter(f);
        }

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
        //Probably better way to generate anID
        //The current Unique IDs are way too long
        UUID u = UUID.randomUUID();

        //return DigestUtils.md5Hex(UUID.randomUUID().toString());
        //a shorter UUID
        return toIDString(u.getMostSignificantBits()) + toIDString(u.getLeastSignificantBits()); //This UUID is only 20 key long
    }

    private static String toIDString(long i) {
        char[] buf = new char[32];
        int z = 64; // 1 << 6;
        int cp = 32;
        long b = z - 1;
        do {
            buf[--cp] = DIGITS66[(int) (i & b)];
            i >>>= 6;
        } while (i != 0);
        return new String(buf, cp, (32 - cp));
    }

    private final static char[] DIGITS66 = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '-', '.', '_', '~'
    };

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

    public static String appendPathExtension(String fileName, String extension) {
        if (fileName == null) {
            return null;
        }

        if (fileName.toLowerCase().endsWith(extension.toLowerCase())) {
            return fileName;
        } else {
            return fileName + "." + extension;
        }
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

    public static BufferedImage createStringImage(Graphics g, String s) {
        int w = g.getFontMetrics().stringWidth(s) + 5;
        int h = g.getFontMetrics().getHeight();

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = image.createGraphics();
        imageGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        imageGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        imageGraphics.setColor(Color.BLACK);
        imageGraphics.setFont(g.getFont());
        imageGraphics.drawString(s, 0, h - g.getFontMetrics().getDescent());
        imageGraphics.dispose();

        return image;
    }
}
