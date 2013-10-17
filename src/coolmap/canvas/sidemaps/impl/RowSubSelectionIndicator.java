/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.sidemaps.impl;

import coolmap.canvas.misc.MatrixCell;
import coolmap.canvas.misc.ZoomControl;
import coolmap.canvas.sidemaps.RowMap;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;

/**
 *
 * This is still experimental. Can be quite confusing to use -> better develop a
 * specific module for it
 *
 * @author gangsu
 */
public class RowSubSelectionIndicator extends RowMap<Object, Object> {

    private ZoomControl _zoomControlY;

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    public RowSubSelectionIndicator(CoolMapObject object) {
        super(object);
        setName("Row Child Match Guage");
        setDescription("blah....");
        _zoomControlY = getCoolMapView().getZoomControlY();
    }

    @Override
    public JComponent getConfigUI() {
        return null;
    }

    @Override
    protected void prePaint(Graphics2D g2D, CoolMapObject<Object, Object> object, int width, int height) {
    }
    private int _maxNumOfChildren = 0;

    @Override
    protected void prepareRender(Graphics2D g2D) {
        g2D.setFont(_zoomControlY.getBoldFont());
        _maxDescent = g2D.getFontMetrics().getMaxDescent();
        _fontSize = _zoomControlY.getBoldFont().getSize();

//        for(VNode node : getCoolMapObject().getViewNodesRow()){
//            if(!node.isGroupNode()){
//                continue;
//            }
//            else{
//                if(_maxNumOfChildren < node.getCOntology().getHeightDifference(.., null).)
//            }
//        }

    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    protected void postPaint(Graphics2D g2D, CoolMapObject<Object, Object> object, int width, int height) {
    }

    @Override
    public boolean canRender(CoolMapObject coolMapObject) {
        return true;
    }

    @Override
    public void justifyView() {
    }
    private int _marginSize = 8;
    private int _maxDescent = 0;
    private int _fontSize = 0;

    @Override
    protected void renderRow(Graphics2D g2D, CoolMapObject<Object, Object> object, VNode node, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (node == null) {
            return;
        }

        //a new copy
        Set<String> subSelection = object.getCoolMapView().getSubSelectedRows();
        if (subSelection == null || subSelection.isEmpty()) {
            return;
        }

        String name = node.getName();

        Set<String> baseNodeNames;
        COntology ontology = null;
        if (!node.isGroupNode()) {
            baseNodeNames = new HashSet<String>();
            baseNodeNames.add(node.getName());
        } else {
            ontology = node.getCOntology();
            baseNodeNames = ontology.getAllLeafChildren(name);
        }



        //baseNodeNames.add(node.getName());
        //System.out.println(baseNodeNames);

        subSelection.retainAll(baseNodeNames);

        //System.err.println(subSelection.size() + "/" + baseNodeNames.size());
        String label = subSelection.size() + "/" + baseNodeNames.size();


        if (label != null) {
            if (node.getViewColor() != null) {
                g2D.setColor(node.getViewColor());
            } else if (ontology != null && ontology.getViewColor() != null) {
                g2D.setColor(ontology.getViewColor());
            } else {
                g2D.setColor(UI.colorRedWarning);
            }

            g2D.fillRect(anchorX, anchorY, (int) (1.0 * cellWidth * subSelection.size() / baseNodeNames.size()), cellHeight);


            g2D.setColor(UI.colorBlack5);
            g2D.drawString(label, anchorX + _marginSize, anchorY + cellHeight - _maxDescent - (cellHeight - _fontSize) / 2 + 1);
        }



    }

    @Override
    public void selectionChanged(CoolMapObject object) {
    }

    @Override
    public void mapAnchorMoved(CoolMapObject object) {
    }

    @Override
    public void activeCellChanged(CoolMapObject object, MatrixCell oldCell, MatrixCell newCell) {
    }

    //No need to update buffer as buffer will be updated globally anyway
    @Override
    public void mapZoomChanged(CoolMapObject object) {
    }

    @Override
    public void subSelectionRowChanged(CoolMapObject object) {
        //updateBuffer();
        updateBuffer();
    }

    @Override
    public void subSelectionColumnChanged(CoolMapObject object) {
    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
        //updateBuffer();
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
    }

    @Override
    public void baseMatrixChanged(CoolMapObject object) {
    }

    @Override
    public void stateStorageUpdated(CoolMapObject object) {
        //updateBuffer();
    }
}
