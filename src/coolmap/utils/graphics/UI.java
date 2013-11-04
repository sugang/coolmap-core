/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.border.Border;

/**
 *
 * @author gangsu
 */
public class UI {

    //String constants
    public final static char tab = '\t';
    public final static char comma = ',';
    public final static char newLine = '\n';
    //Default Fonts
    public static Font fontPlain = null;               //new Font("serif", Font.PLAIN, 15);
    public static Font fontMono = null;
    //colors
    public static Color colorBlackShadow = new Color(50, 50, 50, 150);
    public static Color colorWhite = Color.WHITE;
    public static Color colorGrey1 = new Color(240, 240, 240);
    public static Color colorGrey2 = new Color(220, 220, 220);
    public static Color colorGrey3 = new Color(200, 200, 200);
    public static Color colorGrey4 = new Color(180, 180, 180);
    public static Color colorGrey5 = new Color(160, 160, 160);
    public static Color colorGrey6 = new Color(140, 140, 140);
    public static Color colorBlack1 = new Color(20, 20, 20);
    public static Color colorBlack2 = new Color(40, 40, 40);
    public static Color colorBlack3 = new Color(60, 60, 60);
    public static Color colorBlack4 = new Color(80, 80, 80);
    public static Color colorBlack5 = new Color(100, 100, 100);
    public static Color colorBlack6 = new Color(120, 120, 120);
    public static Color colorLightYellow = new Color(255, 246, 0);
    public static Color colorLightRed = new Color(252, 142, 114);
    public static Color colorLightGreen0 = new Color(184, 225, 134, 150);
    public static Color colorLightGreen4 = new Color(184, 225, 134, 155);
    public static Color colorLightGreen1 = new Color(161, 217, 155, 200);
    public static Color colorLightGreen2 = new Color(49, 163, 84);
    public static Color colorLightGreen5 = new Color(173, 221, 142);
    public static Color colorLightGreen6 = new Color(247,252,185);
    public static Color colorLightBlue0 = new Color(158, 202, 225);
    public static Color colorLightBlue1 = new Color(50, 130, 190);
    public static Color colorDarkGreen1 = new Color(77, 172, 38);
    public static Color colorOrange0 = new Color(255, 135, 0);
    public static Color colorOrange1 = new Color(255, 160, 0);
    public static Color colorOrange2 = new Color(245, 130, 0);
    public static Color colorRedWarning = new Color(253, 187, 132);
    public static Color colorHighlightLabelSibling = new Color(255, 237, 160, 180);
    public static Color colorDirectBaseline = new Color(217, 95, 14, 200);
    public static Color colorLightPink = new Color(253, 224, 221);
    public static Color colorPink = new Color(250,159,181);
    
    //dash can be quite expensive to redraw for some reason.
    public static BasicStroke strokeDash1_5 = new BasicStroke(1.5f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f, new float[]{2.0f}, 0.0f);
    public static BasicStroke strokeDash2 = new BasicStroke(2f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f, new float[]{4.0f}, 0.0f);
    public static BasicStroke strokeDash3 = new BasicStroke(3.5f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f, new float[]{3.0f}, 0.0f);
    //strokes
    public static BasicStroke stroke1 = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static BasicStroke stroke1_5 = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static BasicStroke stroke2 = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static BasicStroke stroke3 = new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static BasicStroke stroke3_5 = new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static BasicStroke stroke4 = new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static BasicStroke stroke5 = new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static BasicStroke stroke6 = new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static BasicStroke stroke7 = new BasicStroke(7.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static BasicStroke stroke8 = new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final HashMap<String, ImageIcon> _iconStash = new HashMap<String, ImageIcon>();
    public static Image progressBar = null;
    public static Image blockLoader = null;
    public static String stringArrowUp = " \u21E1";
    public static String stringArrowDown = " \u21E3";
    public static DecimalFormat formatDouble3 = new DecimalFormat("#.###");
    public static Border borderPadding10 = BorderFactory.createEmptyBorder(0, 10, 0, 10);
    public static Border borderPadding20 = BorderFactory.createEmptyBorder(0, 20, 0, 20);

    public static Color randomColor() {
        return new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
    }

    public static Color randomHSBColorLight() {
        return Color.getHSBColor((float) Math.random(), (float) (Math.random() * 0.7), (float) (Math.random() * 0.4 + 0.6));
    }

    public static void initialize() {
        System.out.println("Initailizing UI Constants...");
        _loadFonts();
        _loadImages();
        _loadImageIcons();
        System.out.println("Done");
    }

    private static void _loadFonts() {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = e.getAllFonts();
        HashSet<String> fontNames = new HashSet<String>();
        for (Font f : fonts) {
            fontNames.add(f.getName());
        }

        if (fontNames.contains("NanumGothic")) {
            fontPlain = new Font("NanumGothic", Font.PLAIN, 15);
        } else if (fontNames.contains("Verdana")) {
            fontPlain = new Font("Verdana", Font.PLAIN, 15);
        } else if (fontNames.contains("Tahoma")) {
            fontPlain = new Font("Tahoma", Font.PLAIN, 15);
        } else if (fontNames.contains("Helvetica")) {
            fontPlain = new Font("Helvetica", Font.PLAIN, 15);
        } else if (fontNames.contains("Arial")) {
            fontPlain = new Font("Arial", Font.PLAIN, 15);
        } else {
            fontPlain = new Font("plain", Font.PLAIN, 15);
        }

        if (fontNames.contains("Menlo")) {
            fontMono = new Font("Menlo", Font.PLAIN, 15);
        } else if (fontNames.contains("Consolas")) {
            fontMono = new Font("Consolas", Font.PLAIN, 15);
        } else if (fontNames.contains("Monaco")) {
            fontMono = new Font("Monaco", Font.PLAIN, 15);
        } else {
            fontMono = new Font("monospaced", Font.PLAIN, 15);
        }
    }

    public static void main(String args[]) {
        UI.initialize();
    }

    private static void _loadImages() {
        progressBar = Toolkit.getDefaultToolkit().createImage(UI.class.getResource("/resources/ProgressBar.gif"));
        blockLoader = Toolkit.getDefaultToolkit().createImage(UI.class.getResource("/resources/blockLoader.gif"));
    }

    public static ImageIcon getImageIcon(String name) {
        return _iconStash.get(name);
    }

    private static void _loadImageIcons() {
        _iconStash.put("ontologyTop", new ImageIcon(UI.class.getResource("/resources/iconOntologyTop.png")));
        _iconStash.put("ontologyStash", new ImageIcon(UI.class.getResource("/resources/iconContentBox.png")));
        _iconStash.put("console", new ImageIcon(UI.class.getResource("/resources/iconConsole.png")));
        _iconStash.put("textList", new ImageIcon(UI.class.getResource("/resources/iconTextList.png")));
        _iconStash.put("cd", new ImageIcon(UI.class.getResource("/resources/iconCD.png")));
        _iconStash.put("search", new ImageIcon(UI.class.getResource("/resources/iconSearch.png")));
        _iconStash.put("binocular", new ImageIcon(UI.class.getResource("/resources/iconBinocular.png")));
        _iconStash.put("google", new ImageIcon(UI.class.getResource("/resources/iconGoogle.png")));
        _iconStash.put("googleScholar", new ImageIcon(UI.class.getResource("/resources/iconScholar.png")));
        _iconStash.put("pubmed", new ImageIcon(UI.class.getResource("/resources/iconPubmed.png")));
        _iconStash.put("refresh", new ImageIcon(UI.class.getResource("/resources/iconRefresh.png")));
        _iconStash.put("insertColumn", new ImageIcon(UI.class.getResource("/resources/iconDownload2.png")));
        _iconStash.put("insertRow", new ImageIcon(UI.class.getResource("/resources/iconDownload2_rotate.png")));
        _iconStash.put("trashBin", new ImageIcon(UI.class.getResource("/resources/iconTrashbin.png")));
        _iconStash.put("plusSmall", new ImageIcon(UI.class.getResource("/resources/iconPlusSmall.png")));
        _iconStash.put("minusSmall", new ImageIcon(UI.class.getResource("/resources/iconMinusSmall.png")));
        _iconStash.put("plusSmallThin", new ImageIcon(UI.class.getResource("/resources/iconPlusSmallThin.png")));
        _iconStash.put("minusSmallThin", new ImageIcon(UI.class.getResource("/resources/iconMinusSmallThin.png")));
        _iconStash.put("colLabel", new ImageIcon(UI.class.getResource("/resources/iconColLabel.png")));
        _iconStash.put("rowLabel", new ImageIcon(UI.class.getResource("/resources/iconRowLabel.png")));
        _iconStash.put("rowBase", new ImageIcon(UI.class.getResource("/resources/iconRowBase.png")));
        _iconStash.put("columnBase", new ImageIcon(UI.class.getResource("/resources/iconColBase.png")));
        _iconStash.put("iconWarning", new ImageIcon(UI.class.getResource("/resources/bigIconInformation.png")));
        _iconStash.put("dataBlock", new ImageIcon(UI.class.getResource("/resources/iconDataBlock.png")));
        _iconStash.put("contentDrawer", new ImageIcon(UI.class.getResource("/resources/iconContentDrawer.png")));
        _iconStash.put("numeric", new ImageIcon(UI.class.getResource("/resources/iconNumeric.png")));
        _iconStash.put("bool", new ImageIcon(UI.class.getResource("/resources/iconBool.png")));
        _iconStash.put("text", new ImageIcon(UI.class.getResource("/resources/iconText.png")));
        _iconStash.put("filter", new ImageIcon(UI.class.getResource("/resources/iconFilter.png")));
        _iconStash.put("horizontalExtend", new ImageIcon(UI.class.getResource("/resources/iconHorizontalExtend.png")));
        _iconStash.put("grid", new ImageIcon(UI.class.getResource("/resources/iconGrid.png")));
        _iconStash.put("paintRoll", new ImageIcon(UI.class.getResource("/resources/iconPaintRoll.png")));
        _iconStash.put("commentDots", new ImageIcon(UI.class.getResource("/resources/iconCommentDots.png")));
        _iconStash.put("horn", new ImageIcon(UI.class.getResource("/resources/iconHorn.png")));
        _iconStash.put("play", new ImageIcon(UI.class.getResource("/resources/iconPlay.png")));
        _iconStash.put("play2", new ImageIcon(UI.class.getResource("/resources/iconPlay2.png")));
        _iconStash.put("pause", new ImageIcon(UI.class.getResource("/resources/iconPause.png")));
        _iconStash.put("pacman", new ImageIcon(UI.class.getResource("/resources/iconPacman.png")));
        _iconStash.put("grid4", new ImageIcon(UI.class.getResource("/resources/iconGrid4.png")));
        _iconStash.put("pen", new ImageIcon(UI.class.getResource("/resources/iconPen.png")));
        _iconStash.put("zoomIn", new ImageIcon(UI.class.getResource("/resources/iconZoomIn.png")));
        _iconStash.put("zoomOut", new ImageIcon(UI.class.getResource("/resources/iconZoomOut.png")));
        _iconStash.put("dataBlocks", new ImageIcon(UI.class.getResource("/resources/iconDataBlocks.png")));
        _iconStash.put("fileStat", new ImageIcon(UI.class.getResource("/resources/iconFileStat.png")));
        _iconStash.put("layers", new ImageIcon(UI.class.getResource("/resources/iconLayers.png")));
        _iconStash.put("ruler", new ImageIcon(UI.class.getResource("/resources/iconRuler.png")));
        _iconStash.put("error", new ImageIcon(UI.class.getResource("/resources/bigIconCross.png")));
        _iconStash.put("upThin", new ImageIcon(UI.class.getResource("/resources/iconUpThin.png")));
        _iconStash.put("downThin", new ImageIcon(UI.class.getResource("/resources/iconDownThin.png")));
        _iconStash.put("leftThin", new ImageIcon(UI.class.getResource("/resources/iconLeftThin.png")));
        _iconStash.put("rightThin", new ImageIcon(UI.class.getResource("/resources/iconRightThin.png")));
        _iconStash.put("infoBW", new ImageIcon(UI.class.getResource("/resources/infoBW.png")));
        _iconStash.put("dashboard", new ImageIcon(UI.class.getResource("/resources/iconDashboard.png")));
        _iconStash.put("anchor", new ImageIcon(UI.class.getResource("/resources/iconAnchor.png")));
        _iconStash.put("activeCell", new ImageIcon(UI.class.getResource("/resources/iconActiveCell.png")));
        _iconStash.put("selection", new ImageIcon(UI.class.getResource("/resources/iconSelection.png")));
        _iconStash.put("rangeColumn", new ImageIcon(UI.class.getResource("/resources/iconRangeColumn.png")));
        _iconStash.put("rangeRow", new ImageIcon(UI.class.getResource("/resources/iconRangeRow.png")));
        _iconStash.put("powerOn", new ImageIcon(UI.class.getResource("/resources/iconPowerOn.png")));
        _iconStash.put("powerOnSmall", new ImageIcon(UI.class.getResource("/resources/iconPowerOnSmall.png")));
        _iconStash.put("powerOff", new ImageIcon(UI.class.getResource("/resources/iconPowerOff.png")));
        _iconStash.put("keyboard", new ImageIcon(UI.class.getResource("/resources/iconKeyboard.png")));
        _iconStash.put("lock", new ImageIcon(UI.class.getResource("/resources/iconLock.png")));
        _iconStash.put("gear", new ImageIcon(UI.class.getResource("/resources/iconGear.png")));
        _iconStash.put("gearBig", new ImageIcon(UI.class.getResource("/resources/iconGearBig.png")));
        _iconStash.put("gearSmall", new ImageIcon(UI.class.getResource("/resources/iconGearSmall.png")));
        _iconStash.put("zoomInX", new ImageIcon(UI.class.getResource("/resources/iconZoomInX.png")));
        _iconStash.put("zoomInY", new ImageIcon(UI.class.getResource("/resources/iconZoomInY.png")));
        _iconStash.put("zoomOutX", new ImageIcon(UI.class.getResource("/resources/iconZoomOutX.png")));
        _iconStash.put("zoomOutY", new ImageIcon(UI.class.getResource("/resources/iconZoomOutY.png")));
        _iconStash.put("resetX", new ImageIcon(UI.class.getResource("/resources/iconResetX.png")));
        _iconStash.put("resetY", new ImageIcon(UI.class.getResource("/resources/iconResetY.png")));
        _iconStash.put("funnel", new ImageIcon(UI.class.getResource("/resources/iconFunnel.png")));
        _iconStash.put("screen", new ImageIcon(UI.class.getResource("/resources/iconScreen.png")));
        _iconStash.put("emptyPage", new ImageIcon(UI.class.getResource("/resources/iconEmptyPage.png")));
        _iconStash.put("compass", new ImageIcon(UI.class.getResource("/resources/iconCompass.png")));
        _iconStash.put("expand3", new ImageIcon(UI.class.getResource("/resources/iconThreearrows.png")));
        _iconStash.put("duplicate", new ImageIcon(UI.class.getResource("/resources/iconDuplicate.png")));
    }

    public static Color mixOpacity(Color input, float opacity) {
        if (input == null) {
            return null;
        }
        if (opacity < 0) {
            opacity = 0;
        }
        if (opacity > 1.0) {
            opacity = 1.0f;
        }
        return new Color(input.getRed(), input.getGreen(), input.getBlue(), (int) (255 * opacity));
    }

    public static Color mixOpacity(Color input, int opacity) {
        if (input == null) {
            return null;
        }
        if (opacity < 0) {
            opacity = 0;
        }
        if (opacity > 255) {
            opacity = 255;
        }
        return new Color(input.getRed(), input.getGreen(), input.getBlue(), opacity);
    }

    public static Color getTagColor(String key) {
        if (key == null || key.length() == 0) {
            return null;
        } else {
            if (_colorStash.containsKey(key)) {
                return _colorStash.get(key);
            } else {
                Color color = UI.randomHSBColorLight();
                _colorStash.put(key, color);
                return color;
            }
        }
    }
    private final static HashMap<String, Color> _colorStash = new HashMap<String, Color>();
}
