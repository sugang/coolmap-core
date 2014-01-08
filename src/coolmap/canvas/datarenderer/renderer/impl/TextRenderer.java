/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl;

import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;

/**
 *
 * @author sugang
 */
public class TextRenderer extends ViewRenderer<Object> {

    public TextRenderer() {
        setName("Text");
        setDescription("Render data as text");
        font = UI.fontPlain.deriveFont(12f);
        foregroundColor = UI.colorBlack2;
        backgroundColor = UI.colorGrey1;

        //Can be very inefficient
    }

//    private final JLabel renderer;
    private Font font;
    private Color foregroundColor;
    private Color backgroundColor;

    @Override
    public void updateRendererChanges() {
    }

    @Override
    protected void initialize() {
    }

    @Override
    public boolean canRender(Class<?> viewClass) {
        try {
            return Object.class.isAssignableFrom(viewClass);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    public void prepareGraphics(Graphics2D g2D) {
    }

    @Override
    public void renderCellLD(Object v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        g2D.setColor(UI.colorGrey1);
        g2D.fillRect(anchorX, anchorY, cellWidth, cellHeight);
        g2D.setColor(foregroundColor);
        g2D.setFont(font);
        g2D.setColor(UI.colorBlack2);
        g2D.drawString(v.toString(), anchorX+1, anchorY + font.getSize() + 1);
    }

    @Override
    public void renderCellSD(Object v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {

        g2D.setColor(UI.colorGrey2);
        g2D.fillRect(anchorX, anchorY, cellWidth, cellHeight);

        JLabel renderer = new JLabel();
        renderer.setForeground(foregroundColor);
        renderer.setFont(font);

        renderer.setText(v.toString());
        renderer.setSize(renderer.getPreferredSize());
        BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(
                renderer.getWidth(), renderer.getHeight());
        Graphics2D g2DR = (Graphics2D) image.createGraphics();
        g2DR.setColor(UI.colorGrey1);
        g2DR.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2DR.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderer.paint(g2DR);
        g2DR.dispose();

        g2D.drawImage(image, anchorX + 1, anchorY + 1, null);
    }

    @Override
    public void renderCellHD(Object v, VNode rowNode, VNode columnNode, Graphics2D g2D, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        renderCellSD(v, rowNode, columnNode, g2D, anchorX, anchorY, cellWidth, cellHeight);
    }

    @Override
    public void postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

}
