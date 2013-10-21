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
import coolmap.data.listeners.CObjectListener;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        JButton button = new JButton("Fit");
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
    private final Rectangle region = new Rectangle();
    
    private void updateRegion(){
        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
        if(obj == null){
            return;
        }
        
        CoolMapView view = obj.getCoolMapView();
        if(view == null){
            return;
        }
        
        //canvas
        Rectangle canvas = view.getCanvasDimension();
        int minCol = view.getCurrentCol(canvas.x);
        int maxCol = view.getCurrentCol(canvas.x + canvas.width);
        int minRow = view.getCurrentRow(canvas.y);
        int maxRow = view.getCurrentRow(canvas.y + canvas.height);
        
        
        
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
            if(Thread.interrupted()){
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
            
            if(Thread.interrupted()){
                return;
            }
            
            image = renderer.getRenderedFullMap(object, percentage);
            
            if(Thread.interrupted())
                return;
            
            bufferedImage = image;
            _radarPanel.repaint();

//        } catch (InterruptedException e) {
//            System.out.println("Update preview interrupted..");
//        }
            
            //a new method is needed then
            
        }
        catch(Exception ex){
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
        
    }

    @Override
    public void activeCellChanged(CoolMapObject object, MatrixCell oldCell, MatrixCell newCell) {
        //do nothing
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
        System.out.println("Map zoom changed");
        //repaint
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
        System.out.println("Active rows changed");
        //update image
        updateBufferedImage();
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
        System.out.println("Active cols changed");
        //update image
        updateBufferedImage();
    }

    @Override
    public void baseMatrixChanged(CoolMapObject object) {
        System.out.println("base matrix changed");
        //update image
        updateBufferedImage();
    }

    @Override
    public void stateStorageUpdated(CoolMapObject object) {
        //do nothing
    }

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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D) g;

            //g2D
            g2D.setColor(UI.colorBlack5);
            g2D.fillRect(0, 0, this.getWidth(), this.getHeight());

            //still draws the image at the correct coordinate
            g2D.drawImage(bufferedImage, (int) mapAnchor.x, (int) mapAnchor.y, null);

        }

    }

}
