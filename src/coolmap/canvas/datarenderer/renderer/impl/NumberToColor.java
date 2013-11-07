/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.datarenderer.renderer.impl;

import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.CImageGradient;
import coolmap.utils.graphics.GradientEditorPanel;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author sugang
 */
public class NumberToColor extends ViewRenderer<Double> {

    private GradientEditorPanel editor = new GradientEditorPanel();

    private JTextField minValueField = new JTextField();
    private JTextField maxValueField = new JTextField();

    public NumberToColor() {
        setName("Number to Color");
        setDescription("Use color to represent numeric values");

        //initialize UI
        configUI.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 5;
        c.ipady = 5;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridwidth = 2;
//        c.weightx = 0.8;

        configUI.add(editor, c);

        
        
//        c.weightx = 0.2;
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        configUI.add(new JLabel("Min value: "), c);
        c.gridx = 1;
//        c.weightx = 0.3;
        configUI.add(minValueField, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        configUI.add(new JLabel("Max valuee: "), c);
        c.gridx = 1;
        configUI.add(maxValueField, c);
        
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        
        JButton button = new JButton("Update", UI.getImageIcon("refresh"));
        configUI.add(button, c);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateParameters();
            }
        });

        
    }
    
    private void updateParameters(){
        
    }
    

    private double _minValue;
    private double _maxValue;
    private Color _minColor = new Color(127, 205, 187);
    private Color _maxColor = new Color(252, 146, 114);
    private CImageGradient _gradient = new CImageGradient(10000);
    private Color[] _gradientColors = null;

    @Override
    public void initialize() {
        CoolMapObject obj = getCoolMapObject();
        if (!canRender(obj.getViewClass())) {
            return;
        }

    }

    @Override
    public boolean canRender(Class<?> viewClass) {
        try {
            return Double.class.isAssignableFrom(viewClass);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void _preRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    @Override
    protected void _prepareGraphics(Graphics2D g2D) {
    }

    @Override
    protected void _renderCellLD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight) {
    }

    @Override
    protected void _renderCellSD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight) {

    }

    @Override
    protected void _renderCellHD(Double v, VNode rowNode, VNode columnNode, Graphics2D g2D, float anchorX, float anchorY, float cellWidth, float cellHeight) {
    }

    @Override
    protected void _postRender(int fromRow, int toRow, int fromCol, int toCol, float zoomX, float zoomY) {
    }

    private JPanel configUI = new JPanel();

    @Override
    public JComponent getConfigUI() {
        return configUI;
    }

}
