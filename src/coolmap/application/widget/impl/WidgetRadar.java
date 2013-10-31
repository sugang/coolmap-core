/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.application.widget.Widget;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.canvas.listeners.CViewListener;
import coolmap.canvas.misc.MatrixCell;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.listeners.CObjectListener;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 *
 * @author sugang
 */
public class WidgetRadar extends Widget implements ActiveCoolMapChangedListener, CViewListener, CObjectListener {

    private final JPanel _container = new JPanel();
    private final RadarPanel _radarPanel = new RadarPanel();
    private final static GraphicsConfiguration _graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    //How it should be doing? 
    //percentage a map should multiply to, at current zoom level
    private float percentage = 0.5f;
    private final Point2D.Float mapAnchor = new Point2D.Float();

    private int margin = 10;

    private void updatePercentage() {

    }

    public void fitView() {
        //need to compute the optimal percentage for the activecoolMap
        try {
            CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
            if (object == null) {
                return;
            }
            ViewRenderer renderer = object.getViewRenderer();
            if (renderer == null) {
                return;
            }
            //need to determine the actual dimension of the map, when the size is set to 1
            //rowStart, colStart, rowEnd, colEnd
//        VNode rowStartNode = object.getViewNodeRow(0);
//        VNode colStartNode = object.getViewNodeColumn(0);
//        VNode rowEndNode = object.getViewNodeRow(object.getViewNumRows() - 1);
//        VNode colEndNode = object.getViewNodeColumn(object.getViewNumColumns() - 1);
//
//        float zoomX = object.getCoolMapView().getZoomX();
//        float zoomY = object.getCoolMapView().getZoomY();
//
//        int fullMapHeight = Math.round(VNode.distanceInclusive(rowStartNode, rowEndNode, zoomY) / zoomY);
//        int fullMapWidth = Math.round(VNode.distanceInclusive(colStartNode, colEndNode, zoomX) / zoomX);
//
//        //
//        System.out.println("Map dimension @ 1px:" + fullMapWidth + " " + fullMapHeight);
            float zoomX = object.getCoolMapView().getZoomX();
            float zoomY = object.getCoolMapView().getZoomY();
            float mapWidth = object.getCoolMapView().getMapWidth();
            float mapHeight = object.getCoolMapView().getMapHeight();

//                  
//            map dimension 
            float containerWidth = _radarPanel.getWidth();
            float containerHeight = _radarPanel.getHeight();

            containerWidth -= 2 * margin;
            containerHeight -= 2 * margin;

            containerWidth = containerWidth < 0 ? 0 : containerWidth;
            containerHeight = containerHeight < 0 ? 0 : containerHeight;

            System.out.println("Map dimensions:" + mapWidth + " " + mapHeight);

//           by the time it is loaded, container may not have been added yet; this could be avoided by loading  
            System.out.println("Container Dimension:" + _container.getWidth() + " " + _container.getHeight());

            //now decide the percentage, and put it @ center
            mapAnchor.x = containerWidth / 2 + margin;
            mapAnchor.y = containerHeight / 2 + margin;

            //
            float containerWtoHRatio = containerWidth / containerHeight;
            float mapWtoHRatio = mapWidth / mapHeight;

            //
            if (containerWtoHRatio > 1) {
                //
                if (mapWtoHRatio > containerWtoHRatio) {
                    //mapWidth should be container width
                    percentage = (float) containerWidth / mapWidth;
                } else {
                    //mapHeight should be container height
                    percentage = (float) containerHeight / mapHeight;
                }
            } else {
                if (mapWtoHRatio < containerWtoHRatio) {
                    //mapHeight should be container height
                    percentage = (float) containerHeight / mapHeight;
                } else {
                    //mapWidth should be container width
                    percentage = (float) containerWidth / mapWidth;
                }
            }

            int previewWidth = Math.round(mapWidth * percentage);
            int previewHeight = Math.round(mapHeight * percentage);

            mapAnchor.x -= previewWidth / 2;
            mapAnchor.y -= previewHeight / 2;

            System.out.println(percentage);
            updateBufferedImage();

//            _radarPanel.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public WidgetRadar() {
        super("Radar", W_MODULE, L_LEFTBOTTOM, UI.getImageIcon("compass"), "Radar");
        CoolMapMaster.addActiveCoolMapChangedListener(this);

        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCObjectListener(this);
        CoolMapMaster.getActiveCoolMapObjectListenerDelegate().addCViewListener(this);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_container);

        //Add container
        _container.setLayout(new BorderLayout());

        _container.add(_radarPanel, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        
        
        JButton button = new JButton(UI.getImageIcon("expand3"));
        button.setToolTipText("Fit preview to current window");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fitView();
            }
        });
        _container.add(toolBar, BorderLayout.NORTH);
        toolBar.add(button);

    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        //Re render
        fitView();
    }

    //The radar panel needs a custom painter
    private BufferedImage bufferedImage;
    private Thread workerThread;
    private Rectangle region = null;

    private void updateRegion() {
        try {

            CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
            if (obj == null) {
                return;
            }

            CoolMapView view = obj.getCoolMapView();
            if (view == null) {
                return;
            }

            //canvas
            Rectangle canvas = view.getCanvasDimension();

        //may cause the 
//        System.out.println("");
//        System.out.println(canvas.x + " " + (canvas.x + canvas.width));
//            System.out.println("Map Anchor in listener:" + view.getMapAnchor());
//            System.out.println("======= ======");
            //no problem in mapAnchor

            //The bug:
            //while searching, the subMapIndex not updated yet.
            //The search was not a good one - the search should occur AFTER the update
            int minCol = view.getCurrentColSearchAll(canvas.x);
            int maxCol = view.getCurrentColSearchAll(canvas.x + canvas.width);

            //after moving, the two functions returned same values
            int minRow = view.getCurrentRowSearchAll(canvas.y);
            int maxRow = view.getCurrentRowSearchAll(canvas.y + canvas.height);

            float zoomX = obj.getCoolMapView().getZoomX();
            float zoomY = obj.getCoolMapView().getZoomY();
//            there are problems with getCurrentRow and getCurrentCol
//            can't figure it out!! fuck fuck 
        //System.out.println(canvas);
//        System.out.println("minCol: " + minCol + " maxCol: " + maxCol + " -- " + " minRow: " +  minRow + " maxRow: " + maxRow);
//        System.out.println("");
//        System.out.println("");
            //use map mover
            minCol = minCol < 0 ? 0 : minCol;
            minRow = minRow < 0 ? 0 : minRow;

            maxCol = maxCol >= obj.getViewNumColumns() ? obj.getViewNumColumns() - 1 : maxCol;
            maxRow = maxRow >= obj.getViewNumRows() ? obj.getViewNumRows() - 1 : maxRow;

//            System.out.println(minCol + "<" + maxCol + ":" + minRow + "<" + maxRow);
            //Then find the percentage
            VNode minRowNode = obj.getViewNodeRow(minRow);
            VNode minColNode = obj.getViewNodeColumn(minCol);
            VNode maxRowNode = obj.getViewNodeRow(maxRow);
            VNode maxColNode = obj.getViewNodeColumn(maxCol);

            float mapWidth = view.getMapWidth();
            float mapHeight = view.getMapHeight();

            float r1 = minRowNode.getViewOffset() / mapHeight;
            float r2 = maxRowNode.getViewOffset(zoomY) / mapHeight;
            float c1 = minColNode.getViewOffset() / mapWidth;
            float c2 = maxColNode.getViewOffset(zoomX) / mapWidth;

            //Then get the actual
//            if(bufferedImage!= null){
//                int previewWidth = Math.round(mapWidth * percentage);
//                int previewHeight = Math.round(mapHeight * percentage);
//            }
            int previewWidth = bufferedImage.getWidth();
            int previewHeight = bufferedImage.getHeight();
            //region measurement still have issues

            //then I would know
            region = new Rectangle();
            region.width = Math.round(previewWidth * (c2 - c1));
            region.height = Math.round(previewHeight * (r2 - r1));

            //do do do
            //relative to map anchor
            region.x = Math.round(previewWidth * c1);
            region.y = Math.round(previewHeight * r1);

        } catch (Exception e) {
            //do nothing
            region = null;
        }

        _radarPanel.repaint();

    }

    private void updateBufferedImage() {
        if (CoolMapMaster.getActiveCoolMapObject() == null || CoolMapMaster.getActiveCoolMapObject().getViewRenderer() == null) {
            return;
        }

        try {
            workerThread.interrupt();
        } catch (NullPointerException ne) {

        }

        workerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                //trun
                if (Thread.interrupted()) {
                    return;
                }
                updateBufferedImageTask();
            }
        });
        workerThread.start();
    }

    private void updateBufferedImageTask() {
        //only do for the active CoolMapObject
        try {

//            renderer.getRenderedRadarView(object, L_VIEWPORT, L_LEFTTOP);
            //redraw the buffered image
            if (Thread.interrupted()) {
                return;
            }
            CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
            if (object == null) {
                return;
            }
            ViewRenderer renderer = object.getViewRenderer();
            if (renderer == null) {
                return;
            }
            float zoomX = object.getCoolMapView().getZoomX();
            float zoomY = object.getCoolMapView().getZoomY();
            float mapWidth = object.getCoolMapView().getMapWidth();
            float mapHeight = object.getCoolMapView().getMapHeight();

            int previewWidth = Math.round(mapWidth * percentage);
            int previewHeight = Math.round(mapHeight * percentage);

            if (previewWidth < margin * 2 || previewHeight < margin * 2) {
                bufferedImage = null;
                return;
            }

            //bufferedImage = _graphicsConfiguration.createCompatibleImage( previewWidth, 
            //        previewHeight);
            //Test image
            //Graphics2D g2D = bufferedImage.createGraphics();
            //g2D.setColor(Color.RED);
            //g2D.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
            //g2D.dispose();
//            float zoomX = object.getCoolMapView().getZoomX();
//            float zoomY = object.getCoolMapView().getZoomY();
            //percentage 
            zoomX = zoomX * percentage;
            zoomY = zoomY * percentage;

            //Fork... the offsets are not adjusted ...
            //could be quite slow to generate preview
            //BufferedImage image = renderer.getRenderedMap(object, 0, object.getViewNumRows(), 0, object.getViewNumColumns(), zoomX, zoomY);
            BufferedImage image = null;

            if (Thread.interrupted()) {
                return;
            }

            image = renderer.getRenderedFullMap(object, percentage);

            if (Thread.interrupted()) {
                return;
            }

            bufferedImage = image;
//            _radarPanel.repaint();

//        } catch (InterruptedException e) {
//            System.out.println("Update preview interrupted..");
//        }
            //a new method is needed then
            updateRegion();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void selectionChanged(CoolMapObject object) {
        //do nothing
    }

    @Override
    public void mapAnchorMoved(CoolMapObject object) {
        //only needs to repaint
//        System.out.println("map anchor moved in listener");
        updateRegion();
    }

    @Override
    public void activeCellChanged(CoolMapObject object, MatrixCell oldCell, MatrixCell newCell) {
        //do nothing
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
//        System.out.println("Map zoom changed");
        //repaint
        //fitView();
        updateRegion();
    }

//    @Override
//    public void subSelectionRowChanged(CoolMapObject object) {
//        //
//    }
//
//    @Override
//    public void subSelectionColumnChanged(CoolMapObject object) {
//    }
    @Override
    public void aggregatorUpdated(CoolMapObject object) {
        //update image -> this can be very slow
        updateBufferedImage();
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
//        System.out.println("Active rows changed");
        //update image
        updateBufferedImage();
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
//        System.out.println("Active cols changed");
        //update image
        updateBufferedImage();
    }

    @Override
    public void baseMatrixChanged(CoolMapObject object) {
        System.out.println("base matrix changed");
        //update image
        updateBufferedImage();
    }

//    @Override
//    public void stateStorageUpdated(CoolMapObject object) {
//        //do nothing
//    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
        System.out.println("View renderer changed");
        //update image
        updateBufferedImage();
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
        //do nothing for now
    }

    @Override
    public void gridChanged(CoolMapObject object) {
        System.out.println("Grid changed");
        updateBufferedImage();
    }

    private class RadarPanel extends JPanel {
        
        public RadarPanel(){
            MouseTracker tracker = new MouseTracker();                    
            addMouseListener(tracker);
            addMouseMotionListener(tracker);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D) g;

            //g2D
            g2D.setColor(UI.colorBlack5);
            g2D.fillRect(0, 0, this.getWidth(), this.getHeight());

            //still draws the image at the correct coordinate
            g2D.setColor(UI.colorBlack4);
            g2D.setStroke(UI.stroke8);

            //
            if (bufferedImage != null) {
                g2D.drawRoundRect((int) mapAnchor.x, (int) mapAnchor.y, bufferedImage.getWidth(), bufferedImage.getHeight(), 5, 5);
                g2D.drawImage(bufferedImage, (int) mapAnchor.x, (int) mapAnchor.y, null);
            }

            if (region != null) {
                g2D.setColor(Color.WHITE);
                g2D.setStroke(UI.strokeDash1_5);
                g2D.drawRect((int) (region.x + mapAnchor.x), (int) (region.y + mapAnchor.y), region.width, region.height);
            }
        }

    }
    
    /**
     * Mouse Tracker
     */
    private class MouseTracker implements MouseListener, MouseMotionListener {

        @Override
        public void mouseClicked(MouseEvent e) {
//            System.out.println("Mouse clicked");
            //jump to a region
            
            if(CoolMapMaster.getActiveCoolMapObject() == null  || bufferedImage == null)
                return;
            
            float xPercentage =  (e.getX() - mapAnchor.x)/bufferedImage.getWidth() ;
            float yPercentage =  (e.getY() - mapAnchor.y)/bufferedImage.getHeight() ;
            
            //System.out.println(xPercentage + "--" + xPercentage);
            
            //the distance from the anchor
            //need to determine the location
            
            
            xPercentage  = xPercentage < 0 ? 0 : xPercentage;
            xPercentage = xPercentage > 1 ? 1 : xPercentage;
            yPercentage = yPercentage < 0 ? 0 : yPercentage;
            yPercentage = yPercentage > 1 ? 1 : yPercentage;
            
            CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
            obj.getCoolMapView().centerToPercentage(xPercentage, yPercentage);
            
            
            
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
        
    }

}
