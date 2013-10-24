/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.annotation;

import com.google.common.collect.HashBasedTable;
import coolmap.data.cmatrixview.model.VNode;
import java.util.ArrayList;

/**
 *
 * @author sugang
 */
public class AnnotationStorage {

    //need a hash structure with row name + ontology ID, col name + 
    //private ArrayList<PointAnnotation> pointAnnotationStorage = new ArrayList<>();
    private HashBasedTable<String, String, PointAnnotation> pointAnnotationStorage = HashBasedTable.create();

    public AnnotationStorage() {

    }
    
    public void clearAllAnnotations(){
        pointAnnotationStorage.clear();
    }

    public void removeAnnotation(VNode rowNode, VNode colNode) {
        String rowKey = rowNode.getName();
        String columnKey = colNode.getName();

        if (rowNode.getCOntology() != null) {
            rowKey += "|" + rowNode.getCOntology().getID();
        }

        if (colNode.getCOntology() != null) {
            columnKey += "|" + colNode.getCOntology().getID();
        }
        
        pointAnnotationStorage.remove(rowKey, columnKey);
    }

    public void addAnnotation(PointAnnotation pa) {
        if (pa.isValid()) {

            pointAnnotationStorage.put(pa.getRowKey(), pa.getColumnKey(), pa);
        }
    }

    /**
     * get all existing annotation
     *
     * @return
     */
    public ArrayList<PointAnnotation> getAnnotations() {
        ArrayList<PointAnnotation> pas = new ArrayList<>();

        pas.addAll(pointAnnotationStorage.values());

        return pas;
    }

    public PointAnnotation getAnnotation(VNode rowNode, VNode columnNode) {
        if (rowNode == null || columnNode == null) {
            return null;
        }

        String rowKey = rowNode.getName();
        String columnKey = columnNode.getName();

        if (rowNode.getCOntology() != null) {
            rowKey += "|" + rowNode.getCOntology().getID();
        }

        if (columnNode.getCOntology() != null) {
            columnKey += "|" + columnNode.getCOntology().getID();
        }

        return pointAnnotationStorage.get(rowKey, columnKey);
    }

}
