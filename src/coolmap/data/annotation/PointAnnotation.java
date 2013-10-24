/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.annotation;

import coolmap.application.CoolMapMaster;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.utils.graphics.UI;
import java.awt.Color;

/**
 *
 * @author sugang
 */
public class PointAnnotation implements Comparable<PointAnnotation> {

    private String rowNodeOntologyID = null;
    private String columnNodeOntologyID = null;
    private String rowNodeName = null;
    private String columnNodeName = null;
    private String annotation;

    private Color borderColor = UI.colorWhite;
    private Color backgroundColor = UI.colorLightGreen6;
    private Color fontColor = UI.colorBlack3;

    private int fontSize = 10;

    public String getRowKey() {
        if (rowNodeOntologyID == null) {
            return rowNodeName;
        } else {
            return rowNodeName + "|" + rowNodeOntologyID;
        }
    }

    public String getColumnKey() {
        if (columnNodeOntologyID == null) {
            return columnNodeName;
        } else {
            return columnNodeName + "|" + columnNodeOntologyID;
        }
    }

    public PointAnnotation(VNode rowNode, VNode columnNode, String annotation) {

//        try{
//            rowNodeOntologyID = rowNode.getCOntology().getID();
//        }
//        catch(Exception e){
//            
//        }
//        
//        try{
//            columnNodeOntologyID = columnNode.getCOntology().getID();
//        }
        if (rowNode != null && columnNode != null) {
            COntology conto = columnNode.getCOntology();
            if (conto != null) {
                columnNodeOntologyID = conto.getID();
            }

            COntology ronto = rowNode.getCOntology();
            if (ronto != null) {
                rowNodeOntologyID = ronto.getID();
            }

            rowNodeName = rowNode.getName();
            columnNodeName = columnNode.getName();

            this.annotation = annotation;
        }

    }

    public String getRowNodeOntologyID() {
        return rowNodeOntologyID;
    }

    public String getColumnNodeOntologyID() {
        return columnNodeOntologyID;
    }

    public String getRowNodeName() {
        return rowNodeName;
    }

    public String getColumnNodeName() {
        return columnNodeName;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public Color getFontColor() {
        return fontColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBorderColor(Color bColor) {
        borderColor = bColor;
    }

    public void setBackgroundColor(Color bgColor) {
        backgroundColor = bgColor;
    }

    public boolean isValid() {
        if (rowNodeName != null && columnNodeName != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        //return rowNodeName + " (" + rowNodeOntologyID + ") " + " -- " + columnNodeName + " (" + columnNodeOntologyID + ") " + "\n" + annotation;
        String label = "";
        label+= rowNodeName;
        if(rowNodeOntologyID != null){
            label += "(" + CoolMapMaster.getCOntologyByID(rowNodeOntologyID).getName() + "), ";
        }
        label += columnNodeName;
        if(columnNodeOntologyID != null){
            label += "(" + CoolMapMaster.getCOntologyByID(columnNodeOntologyID).getName() + ")";
        }
        
        return label;
    }

    @Override
    public int compareTo(PointAnnotation o) {
        try {
            String s1 = getRowKey() + " " + getColumnKey();
            String s2 = o.getRowKey() + " " + o.getColumnKey();
            return s1.compareTo(s2);
        } catch (Exception e) {
            return 0;
        }
    }

}
