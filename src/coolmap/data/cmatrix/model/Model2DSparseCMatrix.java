/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.cmatrix.model;

import cern.colt.matrix.impl.SparseObjectMatrix2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author gangsu
 */
public class Model2DSparseCMatrix<T> extends CMatrix<T> {

    protected final ArrayList<String> _rowLabels;
    protected final ArrayList<String> _colLabels;
    protected final HashMap<String, Integer> _rowLabelToIndexMap;
    protected final HashMap<String, Integer> _colLabelToIndexMap;
    protected SparseObjectMatrix2D _matrix;
    protected String _description = null;

    
        @Override
    public void destroy() {
        _rowLabelToIndexMap.clear();
        _rowLabels.clear();
        _colLabelToIndexMap.clear();
        _colLabels.clear();
        _matrix = null;
        _isDestroyed = true;
    }
    
    @Override
    public boolean isDestroyed(){
        return _isDestroyed;
    }
    
    private boolean _isDestroyed = false;
    
    private Model2DSparseCMatrix() {
        super(null);
        _rowLabels = null;
        _colLabels = null;
        _rowLabelToIndexMap = null;
        _colLabelToIndexMap = null;
        _matrix = null;
    }

    /**
     * create a new matrix, with homogeneous class type
     *
     * @param name
     * @param numRow
     * @param numCol
     * @param objectClass
     */
    public Model2DSparseCMatrix(String name, Integer numRow, Integer numCol, Class<T> objectClass) {
        super(objectClass);
        if (numRow == null || numCol == null || objectClass == null) {
            _rowLabels = null;
            _colLabels = null;
            _rowLabelToIndexMap = null;
            _colLabelToIndexMap = null;
            _matrix = null;
        } else {

            if (numRow < 0) {
                numRow = 0;
            }
            if (numCol < 0) {
                numCol = 0;
            }

            setName(name);
            
            _matrix = new SparseObjectMatrix2D(numRow, numCol);
            _rowLabels = new ArrayList<String>(numRow);
            _colLabels = new ArrayList<String>(numCol);
            _rowLabelToIndexMap = new HashMap<String, Integer>(numRow);
            _colLabelToIndexMap = new HashMap<String, Integer>(numCol);

            //The default labels are needed as place holders.
            _initDefaultLabels();
        }
    }

    private void _initDefaultLabels() {
        for (int i = 0; i < getNumRows(); i++) {
            //setRowLabel(i, "R" + i);
            _rowLabels.add("R" + i);
        }
        for (int j = 0; j < getNumColumns(); j++) {
            //setColLabel(j, "C" + j);
            _colLabels.add("C" + j);
        }
    }

    public Model2DSparseCMatrix(String name, T[][] data, Class<T> objectClass, String[] rowLabels, String[] colLabels) {
        //generate runtime exception if data has issues
        this(name, data.length, data[0].length, objectClass);
        setRowLabels(rowLabels);
        setColLabels(colLabels);

    }

    @Override
    public T getValue(int row, int col) {
        if (row < 0 || col < 0 || row >= getNumRows() || col >= getNumColumns() || _matrix == null) {
            return null;
        } else {
            return (T)_matrix.getQuick(row, col);
        }
    }

//    @Override
//    public String getValueAsSnippet(int row, int col) {
//        T obj = getValue(row, col);
//        if (obj != null) {
//            return obj.toString();
//        } else {
//            return null;
//        }
//    }

    @Override
    public Integer getIndexOfRowName(String label) {
        return _rowLabelToIndexMap.get(label);
    }

    @Override
    public Integer getIndexOfColName(String label) {
        return _colLabelToIndexMap.get(label);
    }

    @Override
    public Integer getNumRows() {
        if (_matrix != null) {
            return _matrix.rows();
        } else {
            return null;
        }
    }

    @Override
    public Integer getNumColumns() {
        if (_matrix != null) {
            return _matrix.columns();
        } else {
            return null;
        }
    }

    @Override
    public void setValue(int row, int col, T value) {
        if (row < 0 || row >= getNumRows() || col < 0 || col >= getNumColumns()) {
            //Do nothing.
        } else {
            _matrix.setQuick(row, col, value);
        }
    }

    @Override
    public List<String> getRowLabelsAsList(Integer[] rowIndices) {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(_rowLabels);
        return list;
    }

    @Override
    public List<String> getColLabelsAsList(Integer[] colIndices) {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(_colLabels);
        return list;
    }

    @Override
    public void setRowLabel(int index, String label) {
        //label actually should be able to be null.
        //but then the mapping will have issues
        //This can be enforced at the loader.
        if (index < 0 || index >= getNumRows()) {
            return;
        } else {
            _rowLabels.set(index, label);
            _rowLabelToIndexMap.remove(_rowLabels.get(index));
            _rowLabelToIndexMap.put(label, index);
        }
    }

    @Override
    public void setColLabel(int index, String label) {
        if (index < 0 || index >= getNumColumns()) {
            return;
        } else {
            _colLabels.set(index, label);
            _colLabelToIndexMap.remove(_colLabels.get(index));
            _colLabelToIndexMap.put(label, index);
        }
    }

    @Override
    public String getColLabel(int index) {
        if (index < 0 || index >= getNumColumns()) {
            return null;
        } else {
            return _colLabels.get(index);
        }
    }

    @Override
    public String getRowLabel(int index) {
        if (index < 0 || index >= getNumRows()) {
            return null;
        } else {
            return _rowLabels.get(index);
        }
    }

//    @Override
//    public Class<T> getRowClass(int index) {
//        return getMemberClass();
//    }

//    @Override
//    public Class<T> getColClass(int index) {
//        return getMemberClass();
//    }
    

    @Override
    public String getDescription() {
        return _description;
    }

    public void setDescription(String desciption) {
        _description = desciption;
    }

//    @Override
//    public Class<T> getMemberClass(int row, int col) {
//        return getMemberClass();
//    }

//    @Override
//    public T[][] getMatrixCopy(Integer[] rowIndices, Integer[] colIndices) {
//        if (colIndices == null || rowIndices == null || colIndices.length == 0 || rowIndices.length == 0) {
//            return null;
//        }        
//        //T[][] returnVal = (T[][]) (new Object[rowIndices.length][colIndices.length]);
//        T[][] returnVal = (T[][]) Array.newInstance(_objectClass, rowIndices.length, colIndices.length);
//        
//        for (int i = 0; i < rowIndices.length; i++) {
//            for (int j = 0; j < colIndices.length; j++) {
//                returnVal[i][j] = getValue(rowIndices[i], colIndices[j]);
//            }
//        }
//        return returnVal;        
//    }
}
