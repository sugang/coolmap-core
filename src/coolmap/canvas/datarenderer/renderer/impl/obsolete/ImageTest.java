/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl.obsolete;

import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.cmatrixview.model.VNode;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;

/**
 *
 * @author sugang
 */
public class ImageTest extends ViewRenderer<Object> {

    public ImageTest() {
        setName("Image Test");
        setDescription("A Test to render images");
    }
    private final HashMap<String, BufferedImage> maps = new HashMap<String, BufferedImage>();

    @Override
    public void updateRendererChanges() {
    }

    @Override
    public void initialize() {
    }

    @Override
    public boolean canRender(Class<?> viewClass) {
        return true;
    }

    @Override
    public void preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
        //load the images from these rows
        maps.clear();
        for (int i = fromRow; i < toRow; i++) {
            for (int j = fromCol; j < toCol; j++) {
                try {
                    BufferedImage image = ImageIO.read(new File("/Users/sugang/Documents/Dev - Local/WorldMap/800X800/" + i + "-" + j + ".jpg"));
                    if (image != null) {
                        maps.put(i + "-" + j, image);
                    }
                } catch (Exception e) {
                }
            }
        }

        System.out.println("Preloaded map size: " + maps.size());
    }

    @Override
    public void prepareGraphics(Graphics2D g2D) {
    }

    @Override
    public void renderCellLD(Object v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
    }

    @Override
    public void renderCellSD(Object v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        //need the names of row nodes and column nodes
        try {
            int y = rowNode.getViewIndex().intValue();
            int x = columnNode.getViewIndex().intValue();

            BufferedImage img = maps.get(x + "-" + y);

            g2D.drawImage(
                    img.getScaledInstance((int) cellWidth, (int) cellHeight, BufferedImage.SCALE_FAST),
                    (int) anchorX,
                    (int) anchorY,
                    null);

        } catch (Exception e) {
        }
    }

    @Override
    public void renderCellHD(Object v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
    }

    @Override
    public void postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }
}
