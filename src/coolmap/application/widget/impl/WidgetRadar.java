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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;

/**
 *
 * @author sugang
 */
public class WidgetRadar extends Widget implements ActiveCoolMapChangedListener, CViewListener, CObjectListener {

    private final JPanel _container = new JPanel();
    private final RadarPanel _radarPanel = new RadarPanel();
    private final static GraphicsConfiguration _graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    final JRadioButton fitToWindowButton = new JRadioButton();

    // percentage a map should multiply to, at current zoom level
    private float xPercentage = 0.5f;
    private float yPercentage = 0.5f;

    private final Point2D.Float mapAnchor = new Point2D.Float();

    private final int margin = 10;

    public void fitView() {
        //need to compute the optimal percentage for the activecoolMap
        try {
            CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
            if (object == null) {
                bufferedImage = null;
                _radarPanel.repaint();
                region = null;
                return;
            }
            ViewRenderer renderer = object.getViewRenderer();
            if (renderer == null) {
                bufferedImage = null;
                _radarPanel.repaint();
                region = null;
                return;
            }

            float mapWidth = object.getCoolMapView().getMapWidth();
            float mapHeight = object.getCoolMapView().getMapHeight();

            // map dimension 
            float containerWidth = _radarPanel.getWidth();
            float containerHeight = _radarPanel.getHeight();

            containerWidth -= 2 * margin;
            containerHeight -= 2 * margin;

            containerWidth = containerWidth < 0 ? 0 : containerWidth;
            containerHeight = containerHeight < 0 ? 0 : containerHeight;

            // now decide the percentage, and put it @ center
            mapAnchor.x = containerWidth / 2 + margin;
            mapAnchor.y = containerHeight / 2 + margin;

            //
            float containerWtoHRatio = containerWidth / containerHeight;
            float mapWtoHRatio = mapWidth / mapHeight;

            if (fitToWindowButton.isSelected()) {
                xPercentage = (float) containerWidth / mapWidth;
                yPercentage = (float) containerHeight / mapHeight;
            } else { // keep original scale
                float percentage;

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

                xPercentage = percentage;
                yPercentage = percentage;
            }

            int previewWidth = Math.round(mapWidth * xPercentage);
            int previewHeight = Math.round(mapHeight * yPercentage);

            mapAnchor.x -= previewWidth / 2;
            mapAnchor.y -= previewHeight / 2;

            updateBufferedImage();

        } catch (Exception ex) {
            //no exception here
            ex.printStackTrace();
        }

    }

    private boolean busy;

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

        _container.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                fitView();
            }

        });

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        fitToWindowButton.setText("Fit to window");
        fitToWindowButton.setToolTipText("Fit preview to current window");
        fitToWindowButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fitView();
            }
        });
        _container.add(toolBar, BorderLayout.NORTH);
        toolBar.add(fitToWindowButton);

    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
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

            int minCol = view.getCurrentColSearchAll(canvas.x);
            int maxCol = view.getCurrentColSearchAll(canvas.x + canvas.width);

            //after moving, the two functions returned same values
            int minRow = view.getCurrentRowSearchAll(canvas.y);
            int maxRow = view.getCurrentRowSearchAll(canvas.y + canvas.height);

            float zoomX = obj.getCoolMapView().getZoomX();
            float zoomY = obj.getCoolMapView().getZoomY();

            //use map mover
            minCol = minCol < 0 ? 0 : minCol;
            minRow = minRow < 0 ? 0 : minRow;

            maxCol = maxCol >= obj.getViewNumColumns() ? obj.getViewNumColumns() - 1 : maxCol;
            maxRow = maxRow >= obj.getViewNumRows() ? obj.getViewNumRows() - 1 : maxRow;

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

            busy = true;

            _radarPanel.repaint();

            //redraw the buffered image
            if (Thread.interrupted()) {
                return;
            }

            CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
            if (object == null) {
                busy = false;
                return;
            }

            ViewRenderer renderer = object.getViewRenderer();
            if (renderer == null) {
                busy = false;
                return;
            }

            float mapWidth = object.getCoolMapView().getMapWidth();
            float mapHeight = object.getCoolMapView().getMapHeight();

            int previewWidth = Math.round(mapWidth * xPercentage);
            int previewHeight = Math.round(mapHeight * yPercentage);

            //it's a bit interesting here
            if (previewWidth < 1 || previewHeight < 1) {
                bufferedImage = null;
                busy = false;
                return;
            }

            BufferedImage image = null;

            if (Thread.interrupted()) {
                return;
            }

            image = renderer.getRenderedFullMap(object, xPercentage, yPercentage);

            if (Thread.interrupted()) {
                return;
            }

            bufferedImage = image;

            updateRegion();

            busy = false;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void selectionChanged(CoolMapObject object) {
    }

    @Override
    public void mapAnchorMoved(CoolMapObject object) {
        //only needs to repaint
        updateRegion();
    }

    @Override
    public void activeCellChanged(CoolMapObject object, MatrixCell oldCell, MatrixCell newCell) {
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
        fitView();

    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
        fitView();
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
        fitView();
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
        fitView();
    }

    @Override
    public void coolMapObjectBaseMatrixChanged(CoolMapObject object) {
        fitView();
    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
        fitView();
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    @Override
    public void gridChanged(CoolMapObject object) {
        fitView();
    }

    @Override
    public void nameChanged(CoolMapObject object) {
    }

    private class RadarPanel extends JPanel {

        private final Font defaultFont;
        private final String MESSAGE_PREVIEW_NOT_AVAILABLE = "Preview not available";

        public RadarPanel() {
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    //jump to a region

                    if (CoolMapMaster.getActiveCoolMapObject() == null || bufferedImage == null) {
                        return;
                    }

                    float xPercentage = (e.getX() - mapAnchor.x) / bufferedImage.getWidth();
                    float yPercentage = (e.getY() - mapAnchor.y) / bufferedImage.getHeight();

                    //the distance from the anchor
                    //need to determine the location
                    xPercentage = xPercentage < 0 ? 0 : xPercentage;
                    xPercentage = xPercentage > 1 ? 1 : xPercentage;
                    yPercentage = yPercentage < 0 ? 0 : yPercentage;
                    yPercentage = yPercentage > 1 ? 1 : yPercentage;

                    CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                    obj.getCoolMapView().centerToPercentage(xPercentage, yPercentage);
                }

            });
            defaultFont = UI.fontPlain.deriveFont(11f).deriveFont(Font.BOLD);
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
            } else if (!busy) {
//                g2D.setFont(UI);
                g2D.setColor(UI.colorWhite);
                g2D.setFont(defaultFont);
                int stringW = g2D.getFontMetrics().stringWidth(MESSAGE_PREVIEW_NOT_AVAILABLE);
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2D.drawString(MESSAGE_PREVIEW_NOT_AVAILABLE, getWidth() / 2 - stringW / 2, getHeight() / 2 + 5);
                return;
            }

            if (region != null) {
                g2D.setColor(Color.WHITE);
                g2D.setStroke(UI.strokeDash1_5);
                g2D.drawRect((int) (region.x + mapAnchor.x), (int) (region.y + mapAnchor.y), region.width, region.height);
            }

            if (busy) {

                g2D.setColor(UI.mixOpacity(UI.colorWhite, 0.5f));
                g2D.fillRoundRect(getWidth() / 2 - UI.blockLoader.getWidth(_container) / 2 - 10, getHeight() / 2 - UI.blockLoader.getHeight(_container) / 2 - 10, UI.blockLoader.getWidth(_container) + 20, UI.blockLoader.getHeight(_container) + 20, 10, 10);

                try {
                    g2D.drawImage(UI.blockLoader, getWidth() / 2 - UI.blockLoader.getWidth(_container) / 2, getHeight() / 2 - UI.blockLoader.getHeight(_container) / 2, this);
                } catch (Exception e) {
                }

            }
        }

    }
}
