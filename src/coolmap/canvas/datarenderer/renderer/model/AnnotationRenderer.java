/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.model;

import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.filter.SelectionFilter;
import coolmap.data.filter.Filter;
import coolmap.utils.Tools;
import java.awt.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.util.UUID;
import javax.swing.JComponent;

/**
 *
 * @author gangsu
 */
public abstract class AnnotationRenderer<BASE, VIEW> {

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
    private String _name = null;
    private SelectionFilter<VIEW> _selectionFilter = new SelectionFilter<VIEW>();

    protected String getName() {
        return _name;
    }

    protected void setName(String name) {
        _name = name;
    }

    /**
     * returns the UI for configuration
     *
     * @return
     */
    public abstract JComponent getUI();

    /**
     * called before adding to a new CoolMapInstance for renderer specific
     * intialization.
     */
    protected abstract void _initialize();

    public abstract boolean canRender(Class<?> viewClass);

    public final void setAntiAliasing(boolean antiAlias) {
        _antiAliasing = antiAlias;
    }

    protected abstract void _preRender(CoolMapObject<?, VIEW> data, Filter<VIEW> filter, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY);

    protected abstract void _prepareGraphics(Graphics2D g2D);

    protected abstract void _renderCellLD(VIEW v, CoolMapObject<BASE, VIEW> data, VNode rowNode, VNode colNode, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight);

    protected abstract void _renderCellSD(VIEW v, CoolMapObject<BASE, VIEW> data, VNode rowNode, VNode colNode, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight);

    protected abstract void _renderCellHD(VIEW v, CoolMapObject<BASE, VIEW> data, VNode rowNode, VNode colNode, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight);

//    protected abstract void _renderCellAnnotationLD();
//    
//    protected abstract void _renderCellAnnotationSD(VIEW v, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight);
//    
//    protected abstract void _renderCellAnnotationHD();
    public abstract Image getAnnotationTip(VIEW v, List<Object[][]> baseValues, List<CMatrix<?>> baseMatrices, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight, float xRel, float yRel);

    protected abstract void _postRender(CoolMapObject<?, VIEW> data, Filter<VIEW> filter, int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY);

    public synchronized BufferedImage getRenderedMap(CoolMapObject<BASE, VIEW> data, Filter<VIEW> filter, int fromRow, int toRow, int fromCol, int toCol, final float zoomX, final float zoomY) throws InterruptedException {
        if (data == null || data.getViewNumColumns() == 0 || data.getViewNumRows() == 0 || fromRow < 0 || fromRow > data.getViewNumRows() || fromCol < 0 || fromCol > data.getViewNumColumns()) {
            //System.out.println("Error occured");
            System.out.println("Render exception occured. Check render range and data");
            return null;
        } else {

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
            _preRender(data, filter, fromRow, toRow, fromCol, toCol, zoomX, zoomY);

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

                    threads[i * _threadNum + j] = new Thread(new AnnotationRendererRunner(data, filter, matrixFromRow, matrixToRow, matrixFromCol, matrixToCol, viewMap, subImageAnchorX, subImageAnchorY, zoomX, zoomY));

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
            _postRender(data, filter, fromRow, toRow, fromCol, toCol, zoomX, zoomY);

            return viewMap;
        }
    }//end of get rendered map

    private class AnnotationRendererRunner implements Runnable {

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
        private CoolMapObject<BASE, VIEW> __data;
        private Filter<VIEW> __filter;

        private AnnotationRendererRunner() {
        }

        public AnnotationRendererRunner(CoolMapObject<BASE, VIEW> data,
                Filter<VIEW> filter,
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
            __filter = filter;
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


            //int subMapWidth = (int) (colEndNode.getViewOffset() - colStartNode.getViewOffset() + colEndNode.getViewSize(__zoomX));
            //int subMapHeight = (int) (rowEndNode.getViewOffset() - rowStartNode.getViewOffset() + rowEndNode.getViewSize(__zoomY));

            int subMapWidth = VNode.distanceInclusive(colStartNode, colEndNode, __zoomX);
            int subMapHeight = VNode.distanceInclusive(rowStartNode, rowEndNode, __zoomY);

            //System.out.println(subMapWidth + " " + subMapHeight);

            final BufferedImage subMap = _graphicsConfiguration.createCompatibleImage(subMapWidth, subMapHeight, Transparency.OPAQUE);

            Graphics2D g2D = subMap.createGraphics();
            if (_antiAliasing) {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            _prepareGraphics(g2D);

            float anchorX;
            float anchorY;
            float cellWidth;
            float cellHeight;
            float offsetX = __data.getViewNodeColumn(__matrixFromCol).getViewOffset();
            float offsetY = __data.getViewNodeRow(__matrixFromRow).getViewOffset();
            VNode rowNode;
            VNode colNode;

            int row, col;
            VIEW value;

            for (int i = 0; i < __matrixToRow - __matrixFromRow; i++) {
                for (int j = 0; j < __matrixToCol - __matrixFromCol; j++) {

                    row = i + __matrixFromRow;
                    col = j + __matrixFromCol;

                    if (_selectionFilter.canPass(__data, row, col) && (__filter == null || __filter.canPass(__data, row, col))) {

                        //value
                        value = __data.getViewValue(i, j);
                        
                        //row node and col node
                        rowNode = __data.getViewNodeRow(row);
                        colNode = __data.getViewNodeColumn(col);
                        
                        //Provide the base matrix, prob only need their names; but provide access to full
                        //List<CMatrix<BASE>> cMatrices = __data.getBaseMatrices();
                        
                        anchorX = colNode.getViewOffset() - offsetX;
                        anchorY = rowNode.getViewOffset() - offsetY;
                        cellWidth = colNode.getViewSizeInMap(__zoomX);
                        cellHeight = rowNode.getViewSizeInMap(__zoomY);

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
                            //throw new InterruptedException();
                            //simply stop rendering
                            //System.out.println("Interrupted?");
                            return;
                        }

//                    g2D.setClip(anchorX, anchorY, cellWidth, cellHeight);
//                    System.out.println("Render SD row:" + i + " col:" + j);



                        switch (__mode) {
                            case LD:
                                _renderCellLD(value, __data, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                                break;
                            case SD:

                                _renderCellSD(value, __data, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                                break;
                            case HD:
                                _renderCellHD(value, __data, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                                break;
                            default:
                                _renderCellSD(value, __data, rowNode, colNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
                        }
                        
                    }//end of pass filter
                    
                    
                    

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

    protected final boolean getAntiAliasing() {
        return _antiAliasing;
    }
//    protected final void setAntiAliasing(boolean antiAliasing){
//        _antiAliasing = antiAliasing;
//    }
}
