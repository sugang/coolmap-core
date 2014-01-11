package coolmap.canvas.datarenderer.renderer.model;

import coolmap.canvas.CoolMapView;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.StateSavable;
import coolmap.utils.Tools;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.json.JSONObject;

/**
 *
 * @author gangsu
 */
public abstract class ViewRenderer<VIEW> implements StateSavable {

    private boolean _antiAliasing = true;
    private int _threadNum = 2;
    private final String _ID = Tools.randomID();
    private int _multiThreadThreshold = 75;
    //This may take 2 seconds
    private final static GraphicsConfiguration _graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    //Low def mode
    private final int LD = -1;
    //standard def mode
    private final int SD = 0;
    //high def mode
    private final int HD = 1;
    //preset thresholds. auto adjust
    //or simply use a button.
    private int _ldThreshold = 5;
    private int _hdThreshold = 20;
    private int _globalMode = SD;
    private boolean _modeOverride = false;
    private CoolMapObject _coolMapObject;

    protected static int DEFAULT_LEGEND_WIDTH = 100;
    protected static int DEFAULT_LEGENT_HEIGHT = 25;

    public Image getSubTip(CoolMapObject object, VNode rowNode, VNode colNode, float percentX, float PercentY, int cellWidth, int cellHeight) {
        //System.out.println(activeCell + " " + percentX + " " + PercentY + " " + cellWidth + " " + cellHeight);
        return null;
    }

    public Image getLegend() {
        return null;
    }

    public ViewRenderer() {
        _coolMapObject = null;
    }

    public final void setCoolMapObject(CoolMapObject object, boolean initialize) {
        _coolMapObject = object;
//        System.out.println(object + "===" + initialize);
        if (initialize) {
//            System.out.println("===Initalize is called in setCoolMapObject:" + this);
            initialize();
        }
    }

    public final CoolMapObject getCoolMapObject() {
        return _coolMapObject;
    }
    private String _name = "Untitiled";

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    /**
     * returns the UI for configuration
     *
     * @return
     */
    public JComponent getConfigUI() {
        JLabel label = new JLabel("No configuration needed.");
        label.setHorizontalAlignment(SwingConstants.CENTER);

        return label;
    }

    private String _description = "No description";

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    ;

    public void updateRenderer() {
        updateRendererChanges();
        if (getConfigUI() != null) {
            getConfigUI().repaint();
        }

        try {
            getCoolMapObject().getCoolMapView().updateCanvasEnforceAll();
            getCoolMapObject().notifyViewRendererUpdated();
        } catch (Exception e) {
//            System.err.println("Update renderer failed");
        }
    }

    /**
     * update parameters changes for the render; will be called in the
     * updateRenderer function
     */
    public abstract void updateRendererChanges();

    /**
     * called when this view renderer is assigned to a coolmap object
     */
    protected abstract void initialize();

    public abstract boolean canRender(Class<?> viewClass);

    public final void setAntiAliasing(boolean antiAlias) {
        _antiAliasing = antiAlias;
    }

    public abstract void preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY);

    public abstract void prepareGraphics(Graphics2D g2D);

    public abstract void renderCellLD(VIEW v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight);

    public abstract void renderCellSD(VIEW v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight);

    public abstract void renderCellHD(VIEW v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight);

    /**
     * a built in method to mark Null
     *
     * @param v
     * @param rowNode
     * @param columnNode
     * @param g2D
     * @param anchorX
     * @param anchorY
     * @param cellWidth
     * @param cellHeight
     */
    protected void _markNull(VIEW v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (v == null) {
            g2D.setColor(UI.colorBlack2);
            int anchorXI = Math.round(anchorX);
            int anchorYI = Math.round(anchorY);
            int widthI = Math.round(cellWidth);
            int heightI = Math.round(cellHeight);

            g2D.setStroke(UI.stroke1_5);
            g2D.fillRect(anchorXI, anchorYI, widthI, heightI);
            g2D.setColor(UI.colorSHOJYOHI);
            g2D.drawLine(anchorXI, anchorYI, anchorXI + widthI, anchorYI + heightI);
            g2D.drawLine(anchorXI + widthI, anchorYI, anchorXI, anchorYI + heightI);
        }
    }

//    protected abstract void _renderCellAnnotationLD();
//    
//    protected abstract void _renderCellAnnotationSD(VIEW v, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight);
//    
//    protected abstract void _renderCellAnnotationHD();
    public abstract void postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY);

//    public synchronized BufferedImage getRenderedRadarView(CoolMapObject data, int imageWidth, int imageHeight) throws InterruptedException{
//        //I can't possibly render a map that is overwhelmingly large
//        //int fullImageWidth = data.getCoolMapView().getMapWidth();
//        //int fullImageHeight = data.getCoolMapView().getMapHeight();
//        
//        
//        
//        return null;
//    }
    public BufferedImage getRenderedFullMap(CoolMapObject<?, VIEW> data, float percentage) {
        try {
            if (data == null) {
                return null;
            }
            CoolMapView view = data.getCoolMapView();
            if (view == null) {
                return null;
            }
            float zoomX = view.getZoomX();
            float zoomY = view.getZoomY();

            float mapWidth = view.getMapWidth() * percentage;
            float mapHeight = view.getMapHeight() * percentage;

            if (mapWidth <= 0) {
                mapWidth = 1;
            }

            if (mapHeight <= 0) {
                mapHeight = 1;
            }

            BufferedImage image = _graphicsConfiguration.createCompatibleImage(Math.round(mapWidth), Math.round(mapHeight));

            Graphics2D g2D = image.createGraphics();
            g2D.setColor(Color.BLACK);

            g2D.fillRect(0, 0, image.getWidth(), image.getHeight());

            //anchorage
            float currentAnchorX = 0;
            float currentAnchorY = 0;
            float currentWidth = 0;
            float currentHeight = 0;

//        try{
//            Thread.sleep(5000);
//        }
//        catch(Exception e){
//            
//        }
//        Thread.sleep(1000);
            //then render
            for (int i = 0; i < data.getViewNumRows(); i++) {
                VNode rowNode = data.getViewNodeRow(i);
                currentAnchorY = rowNode.getViewOffset() * percentage;
                currentHeight = rowNode.getViewSizeInMap(zoomY) * percentage;
                if (currentHeight < 1) {
                    currentHeight = 1;
                }

                for (int j = 0; j < data.getViewNumColumns(); j++) {
                    VNode colNode = data.getViewNodeColumn(j);
                    currentAnchorX = colNode.getViewOffset() * percentage;
                    currentWidth = colNode.getViewSizeInMap(zoomX) * percentage;
                    if (currentWidth < 1) {
                        currentWidth = 1;
                    }

                    VIEW v = data.getViewValue(i, j);

                    //use low definition mode
                    renderCellLD(v, rowNode, colNode, g2D, Math.round(currentAnchorX), Math.round(currentAnchorY), Math.round(currentWidth), Math.round(currentHeight));
//                try{
//                    Thread.sleep(50);
//                }
//                
//                catch(InterruptedException e){
//                    throw e;
//                }

                    if (Thread.interrupted()) {
                        return null;
                    }
                }
            }

            if (Thread.interrupted()) {
                return null;
            }

            return image;
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized BufferedImage getRenderedMap(CoolMapObject<?, VIEW> data, int fromRow, int toRow, int fromCol, int toCol, final float zoomX, final float zoomY) throws InterruptedException {
        if (data == null || data.getViewNumColumns() == 0 || data.getViewNumRows() == 0 || fromRow < 0 || fromRow > data.getViewNumRows() || fromCol < 0 || fromCol > data.getViewNumColumns()) {
            //System.out.println("Error occured");
//            System.out.println("Render exception occured. Check render range and data");
            return null;
        } else {

            //System.out.println((int)zoomX);
            if (!canRender(data.getViewClass())) {
                return null;
            };

            //
            if (data.getViewNumColumns() > _multiThreadThreshold
                    && data.getViewNumRows() > 1
                    || data.getViewNumRows() > _multiThreadThreshold
                    && data.getViewNumColumns() > 1) {
                _threadNum = 2;
            } else {
                _threadNum = 1;
            }

//            _threadNum = 1;
            //do something before render
            preRender(fromRow, toRow, fromCol, toCol, zoomX, zoomY);

            //Can process!
            int numRow = toRow - fromRow;
            int numCol = toCol - fromCol;

            VNode rMin = data.getViewNodeRow(fromRow);
            VNode rMax = data.getViewNodeRow(toRow - 1);//Inclusive
            VNode cMin = data.getViewNodeColumn(fromCol);
            VNode cMax = data.getViewNodeColumn(toCol - 1);

            int imageWidth = (int) (cMax.getViewOffset() - cMin.getViewOffset() + cMax.getViewSizeInMap(zoomX));
            int imageHeight = (int) (rMax.getViewOffset() - rMin.getViewOffset() + rMax.getViewSizeInMap(zoomY));

//            System.out.println(imageWidth + " " + imageHeight);
            //This is the bottom map, no need to be transparent.
            BufferedImage viewMap = _graphicsConfiguration.createCompatibleImage(imageWidth, imageHeight, Transparency.OPAQUE);

            int mapRowSectionSize = numRow / _threadNum;
            int mapColSectionSize = numCol / _threadNum;
//        int imageRowSectionSize = (int)( mapRowSectionSize * zoomY );
//        int imageColSectionSize = (int)( mapColSectionSize * zoomX );

            Thread[] threads = new Thread[_threadNum * _threadNum];

            int matrixFromRow;
            int matrixToRow;
            int matrixFromCol;
            int matrixToCol;

            int subImageAnchorX;
            int subImageAnchorY;

            for (int i = 0; i < _threadNum; i++) {
                for (int j = 0; j < _threadNum; j++) {
                    matrixFromRow = fromRow + i * mapRowSectionSize;
                    matrixToRow = fromRow + (i + 1) * mapRowSectionSize;
                    matrixFromCol = fromCol + j * mapColSectionSize;
                    matrixToCol = fromCol + (j + 1) * mapColSectionSize;

                    if (i == _threadNum - 1) {
                        matrixToRow = toRow;
                    }

                    if (j == _threadNum - 1) {
                        matrixToCol = toCol;
                    }

                    subImageAnchorX = (int) (data.getViewNodeColumn(matrixFromCol).getViewOffset() - data.getViewNodeColumn(fromCol).getViewOffset());
                    subImageAnchorY = (int) (data.getViewNodeRow(matrixFromRow).getViewOffset() - data.getViewNodeRow(fromRow).getViewOffset());

                    threads[i * _threadNum + j] = new Thread(new ViewRendererRunner(data, matrixFromRow, matrixToRow, matrixFromCol, matrixToCol, viewMap, subImageAnchorX, subImageAnchorY, zoomX, zoomY));

                }
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            //if thread is interrupted, return immediately.
            if (Thread.currentThread().isInterrupted()) {
                for (Thread thread : threads) {
                    if (thread.isAlive()) {
                        thread.interrupt();
                    }
                }
                throw new InterruptedException();
            }

            //Do something after render
            postRender(fromRow, toRow, fromCol, toCol, zoomX, zoomY);

            return viewMap;
        }
    }//end of get rendered map

    private class ViewRendererRunner implements Runnable {

        private int __matrixFromRow;
        private int __matrixToRow;
        private int __matrixFromCol = -1;
        private int __matrixToCol = -1;
        private int __subImageAnchorX = -1;
        private int __subImageAnchorY = -1;
        private BufferedImage __viewMap = null;
        private float __zoomX = -1;
        private float __zoomY = -1;
        private int __mode = SD;
        private CoolMapObject<?, VIEW> __data;

        private ViewRendererRunner() {
        }

        public ViewRendererRunner(CoolMapObject<?, VIEW> data,
                int matrixFromRow,
                int matrixToRow,
                int matrixFromCol,
                int matrixToCol,
                BufferedImage viewMap,
                int subImageAnchorX,
                int subImageAnchorY,
                float cellWidth,
                float cellHeight) {

            __data = data;
            __matrixFromCol = matrixFromCol;
            __matrixFromRow = matrixFromRow;
            __matrixToCol = matrixToCol;
            __matrixToRow = matrixToRow;
            __viewMap = viewMap;
            __subImageAnchorX = subImageAnchorX;
            __subImageAnchorY = subImageAnchorY;
            __zoomX = cellWidth;
            __zoomY = cellHeight;

        }

        @Override
        public void run() {
            VNode colStartNode = __data.getViewNodeColumn(__matrixFromCol);
            VNode colEndNode = __data.getViewNodeColumn(__matrixToCol - 1);
            VNode rowStartNode = __data.getViewNodeRow(__matrixFromRow);
            VNode rowEndNode = __data.getViewNodeRow(__matrixToRow - 1);

//            System.out.println("Rendering Column:" + __matrixFromCol + " " + __matrixToCol);
//            System.out.println("Rendering Row:" + __matrixFromRow + " " + __matrixToRow);
            //int subMapWidth = (int) (colEndNode.getViewOffset() - colStartNode.getViewOffset() + colEndNode.getViewSize(__zoomX));
            //int subMapHeight = (int) (rowEndNode.getViewOffset() - rowStartNode.getViewOffset() + rowEndNode.getViewSize(__zoomY));
            //System.out.println("zoomX:" + __zoomX);
            int subMapWidth = VNode.distanceInclusive(colStartNode, colEndNode, __zoomX);
            int subMapHeight = VNode.distanceInclusive(rowStartNode, rowEndNode, __zoomY);

            //ensure it's at least 1 px 
            if (subMapWidth <= 0) {
                subMapWidth = 1;
            }

            //ensure it's at least 1 px
            if (subMapHeight <= 0) {
                subMapHeight = 1;
            }

            //System.out.println(subMapWidth + " " + subMapHeight);
            final BufferedImage subMap = _graphicsConfiguration.createCompatibleImage(subMapWidth, subMapHeight, Transparency.OPAQUE);

            Graphics2D g2D = subMap.createGraphics();
            if (_antiAliasing) {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            prepareGraphics(g2D);

            int anchorX;
            int anchorY;
            int cellWidth;
            int cellHeight;
            float offsetX = __data.getViewNodeColumn(__matrixFromCol).getViewOffset();
            float offsetY = __data.getViewNodeRow(__matrixFromRow).getViewOffset();
            VNode rowNode;
            VNode colNode;
            VIEW value;

            for (int i = 0; i < __matrixToRow - __matrixFromRow; i++) {
                for (int j = 0; j < __matrixToCol - __matrixFromCol; j++) {

                    try {
                        rowNode = __data.getViewNodeRow(i + __matrixFromRow);
                        colNode = __data.getViewNodeColumn(j + __matrixFromCol);

                        value = __data.getViewValue(i + __matrixFromRow, j + __matrixFromCol);

                        anchorX = (int) Math.round(colNode.getViewOffset() - offsetX);

                        //sometimes error here.
                        anchorY = (int) Math.round(rowNode.getViewOffset() - offsetY);

                        cellWidth = (int) Math.round(colNode.getViewSizeInMap(__zoomX));
                        cellHeight = (int) Math.round(rowNode.getViewSizeInMap(__zoomY));
                        
                        //make it minimal 1 px for rendering
                        if(cellWidth < 1)cellWidth = 1;
                        if(cellHeight < 1)cellHeight = 1;

                        //System.out.println(cellWidth + " " + cellHeight);
                        //each cell can take a different size. Therefore need to 
                        if (!_modeOverride) {
                            if (cellWidth <= _ldThreshold || cellHeight <= _ldThreshold) {
                                __mode = LD;
                            } else if (cellWidth >= _hdThreshold || cellHeight > +_hdThreshold) {
                                __mode = HD;
                            } else {
                                __mode = SD;
                            }
                        } else {
                            __mode = _globalMode;
                        }

                        //make sure drawing don't go to other cells
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }

                        if (_clipCell) {
                            g2D.setClip((int) anchorX, (int) anchorY, (int) cellWidth, (int) cellHeight);
                        }
                        switch (__mode) {
                            case LD:
                                renderCellLD(value, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                                break;
                            case SD:

                                renderCellSD(value, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                                break;
                            case HD:
                                renderCellHD(value, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                                break;
                            default:
                                renderCellSD(value, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                        }

                    } catch (Exception e) {
                        return;
                    }

                }//end of inner loop
            }//end of outter loop

            g2D.dispose();
//            try {
//                ImageIO.write(subMap, "png", new File("/Users/gangsu/Desktop/subrender.png"));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            Graphics2D g2DM = __viewMap.createGraphics();
            synchronized (g2D) {
                if (_antiAliasing) {
                    g2DM.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2DM.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }

                //System.out.println(__subImageAnchorX + " " + __subImageAnchorY);
                g2DM.drawImage(subMap, __subImageAnchorX, __subImageAnchorY, null);
                g2DM.dispose();
            }

        }
    }

    protected boolean _clipCell = true;

    protected void setClipCell(boolean clip) {
        _clipCell = clip;
    }

    protected final boolean getAntiAliasing() {
        return _antiAliasing;
    }
//    protected final void setAntiAliasing(boolean antiAliasing){
//        _antiAliasing = antiAliasing;
//    }

    public static BufferedImage createToolTipFromJLabel(JLabel label) {

        label.setSize(label.getPreferredSize()); //make sure it is the preferred size.

        BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(label.getWidth(), label.getHeight());
        Graphics2D g2D = (Graphics2D) image.createGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        label.paint(g2D);
        g2D.dispose();

        //return label paint as
        return image;
    }

    public static BufferedImage createToolTipFromString(String tipString, Font font) {
        if (tipString == null || tipString.length() == 0) {
            return null;
        }

        int width = 2, height = 2;
//        Font font = UI.fontMono.deriveFont(12f).deriveFont(Font.BOLD);
        Color fontColor = UI.colorBlack4;
        Color backgroundColor = UI.colorGrey2;
        BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
        Graphics2D g2D = image.createGraphics();
        g2D.setFont(font);
        FontMetrics fMetrics = g2D.getFontMetrics();
        int marginLR = 10;
        int marginTB = 4;

        String[] lines = tipString.split("\\n");

        for (String line : lines) {
            int w = fMetrics.stringWidth(line);
            if (width < w) {
                width = w;
            }
        }

        width += marginLR * 2;

        height = lines.length * (font.getSize() + marginTB * 2);

        image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        g2D = image.createGraphics();
        g2D.setFont(font);
        g2D.setColor(backgroundColor);
        g2D.fillRoundRect(0, 0, width, height, 5, 5);

        g2D.setColor(fontColor);
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int descent = fMetrics.getMaxDescent();

        g2D.translate(marginLR, 0);
        for (String line : lines) {
            g2D.translate(0, marginTB + font.getSize());
            g2D.drawString(line, 0, -font.getSize() / 2 + descent);
            g2D.translate(0, marginTB);
        }

        return image;
    }

    /**
     * override this to save state to JSON
     *
     * @return
     */
    @Override
    public JSONObject getCurrentState() {
        return null;
    }

    /**
     * override this to make this restorable
     *
     * @param savedState
     * @return
     */
    @Override
    public boolean restoreState(JSONObject savedState) {
        return false;
    }

}
