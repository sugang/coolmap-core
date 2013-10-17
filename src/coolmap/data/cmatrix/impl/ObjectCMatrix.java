/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.cmatrix.impl;

import coolmap.data.cmatrix.model.Model2DCMatrix;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author gangsu
 */
public class ObjectCMatrix extends Model2DCMatrix<Object> {

//    private final HashMap<Integer, Class> _rowClasses = new HashMap<Integer, Class>();
//    private final HashMap<Integer, Class> _colClasses = new HashMap<Integer, Class>();

    private ObjectCMatrix() {
        super(null, null, null, null);
    }

    public ObjectCMatrix(String name, Integer numRow, Integer numCol) {
        super(name, numRow, numCol, Object.class);
    }

    public ObjectCMatrix(String name, Object[][] data, String[] rowLabels, String[] colLabels) {
        super(name, data, Object.class, rowLabels, colLabels);
    }

//    public void setRowClass(Integer index, Class classObj) {
//        if (index >= 0 && index < getNumRows()) {
//            _rowClasses.put(index, classObj);
//        }
//    }
//
//    public void setColClass(Integer index, Class classObj) {
//        if (index >= 0 && index < getNumCols()) {
//            _colClasses.put(index, classObj);
//        }
//    }
//
//    //getRow class
//    @Override
//    public Class<?> getRowClass(int index) {
//        //return super.getRowClass(index);
//        Class cls = _rowClasses.get(index);
//        if (cls != null) {
//            return cls;
//        } else {
//            return super.getRowClass(index);
//        }
//    }
//
//    //get column class
//    @Override
//    public Class<?> getColClass(int index) {
//        Class cls = _colClasses.get(index);
//        if (cls != null) {
//            return cls;
//        } else {
//            return super.getColClass(index);
//        }
//    }

    //get member class
    /**
     * return a specific class of a member, if this cell's class is different
     * from either column class or row class it's quite rare actually, forget
     * about it now.
     *
     * @return
     */
//    @Override
//    public Class<?> getMemberClass(int row, int col){
//
//        Class cls = _rowClasses.get(row);
//        if (cls != null) {
//            return cls;
//        }
//        
//        cls = _colClasses.get(col);
//        if (cls != null) {
//            return cls;
//        }
//        
//        //if row and column classes are not assigned, use super member class
//        return super.getMemberClass();
//        
//    }
}
