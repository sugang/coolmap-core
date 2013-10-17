/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.cmatrix.model;

import com.google.common.base.Objects;
import coolmap.utils.Tools;
import java.lang.reflect.Array;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author gangsu
 */
public abstract class CMatrix<T> {

    protected final String _ID;
    protected String _name;
    protected final Class<T> _objectClass;

    public final String getID() {
        return _ID;
    }

    public abstract void destroy();

    public abstract boolean isDestroyed();

    /**
     * create a default CMatrix with no name
     */
    private CMatrix() {
//        _ID = UUID.randomUUID().toString();
//        _name = null;
//        _objectClass = null;
        this(null, null);
    }

    public CMatrix(Class<T> objectClass) {
        this(objectClass, null);
    }

    public CMatrix(Class<T> objectClass, String ID) {
        if (ID == null || ID.length() == 0) {
            _ID = Tools.randomID();
        } else {
            _ID = ID;
        }
        _name = null;
        _objectClass = objectClass;
    }

    /**
     * returns the value at row and col
     *
     * @param row
     * @param col
     * @return
     */
    public abstract T getValue(int row, int col);

    /**
     * get a snippet version of the value
     *
     * @param row
     * @param col
     * @return
     */
    //public abstract String getValueAsSnippet(int row, int col);
    /**
     * returns a subRegion of the matrix inclusive, exclusive
     *
     * @param fromRow
     * @param toRow
     * @param fromCol
     * @param toCol
     * @return
     */
    public final T[][] getMatrixCopy(int fromRow, int toRow, int fromCol, int toCol) {
        if (toRow <= fromRow || toCol <= fromCol) {
            return null;
        }

        Integer[] rowI = new Integer[toRow - fromRow];
        Integer[] colI = new Integer[toCol - fromCol];

        for (int i = 0; i < rowI.length; i++) {
            rowI[i] = i + fromRow;
        }

        for (int i = 0; i < colI.length; i++) {
            colI[i] = i + fromCol;
        }

        return getMatrixCopy(rowI, colI);
    }

    public final T[][] getMatrixCopy() {
        Integer nRow = getNumRows();
        Integer nCol = getNumColumns();
        if (nRow == null || nCol == null || nRow <= 0 || nCol <= 0) {
            return null;
        }
//
//        //This may not work...
//        //actually it doesn't work.
//        T[][] objects = (T[][]) (new Object[nRow][nCol]);
//
//        for (int i = 0; i < nRow; i++) {
//            for (int j = 0; j < nCol; j++) {
//                objects[i][j] = getValue(i, j);
//            }
//        }
        return getMatrixCopy(0, nRow, 0, nCol);
    }

    /**
     * get a matrix copy from the integers
     *
     * @param rowIndices
     * @param colIndices
     * @return
     */
    public T[][] getMatrixCopy(Integer[] rowIndices, Integer[] colIndices) {

        if (colIndices == null || rowIndices == null || colIndices.length == 0 || rowIndices.length == 0) {
            return null;
        }

        //T[][] returnVal = (T[][]) (new Object[rowIndices.length][colIndices.length]);
        T[][] returnVal = (T[][]) Array.newInstance(_objectClass, rowIndices.length, colIndices.length);

        for (int i = 0; i < rowIndices.length; i++) {
            for (int j = 0; j < colIndices.length; j++) {
                returnVal[i][j] = getValue(rowIndices[i], colIndices[j]);
            }
        }
        return returnVal;
    }

    /**
     * returns the index for the row label
     *
     * @param label
     * @return
     */
    public abstract Integer getIndexOfRowName(String name);

    /**
     * returns the index for the col label
     *
     * @param label
     * @return
     */
    public abstract Integer getIndexOfColName(String name);

    /**
     * get the number of rows
     *
     * @return
     */
    public abstract Integer getNumRows();

    /**
     * get the number of columns
     *
     * @return
     */
    public abstract Integer getNumColumns();

    /**
     * this could result in Array Index of of bounds exception
     *
     * @param row
     * @param col
     * @param value
     */
    public abstract void setValue(int row, int col, T value);

    /**
     * get the class of the member
     *
     * @return
     */
    public Class<T> getMemberClass() {
        return (Class<T>) _objectClass;
    }

    ;

//    public abstract Class<?> getRowClass(int index);
//
//    public abstract Class<?> getColClass(int index);
//
//    public abstract Class<?> getMemberClass(int row, int col);

    public abstract String getDescription();

    /**
     * should ensure that it always returns a copy
     *
     * @param rowIndices
     * @return
     */
    public abstract List<String> getRowLabelsAsList(Integer[] rowIndices);

    /**
     * should ensure that it always returns a copy
     *
     * @param colIndices
     * @return
     */
    public abstract List<String> getColLabelsAsList(Integer[] colIndices);

    public abstract void setRowLabel(int index, String label);

    public abstract void setColLabel(int index, String label);

    public final void setRowLabels(String[] labels) {
        if (labels != null && labels.length == getNumRows()) {
            for (int i = 0; i < labels.length; i++) {
                setRowLabel(i, labels[i]);
            }
        }
    }

    public final void setColLabels(String[] labels) {
        if (labels != null && labels.length == getNumColumns()) {
            for (int i = 0; i < labels.length; i++) {
                setColLabel(i, labels[i]);
            }
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    /*
     * Auxilary functions that can't be overriden.
     */
    public final void printMatrix(int rowCap, int colCap) {
        if (rowCap > getNumRows()) {
            rowCap = getNumRows();
        }
        if (colCap > getNumColumns()) {
            colCap = getNumColumns();
        }

        System.out.println("---------------------");
        System.out.println("CMatrix name: " + getName());
        System.out.println("NumRows: " + getNumRows() + " NumCols: " + getNumColumns());

        System.out.println("");
        System.out.format("%10s", "Row\\Col");
        for (int j = 0; j < getNumColumns(); j++) {
            System.out.format("%10s", getColLabel(j));
        }
        System.out.println();


        for (int i = 0; i < rowCap; i++) {
            System.out.format("%10s", getRowLabel(i));
            for (int j = 0; j < colCap; j++) {
                System.out.format("%10s", getValue(i, j));
            }
            System.out.println();
        }
        System.out.println("---------------------");
    }

    public abstract String getColLabel(int index);

    public abstract String getRowLabel(int index);

    public final void printMatrix() {
        printMatrix(getNumRows(), getNumColumns());
    }

    /**
     * get Row labels as list - full
     *
     * @return
     */
    public final List<String> getRowLabelsAsList() {
        Integer[] indices = new Integer[getNumRows()];
        for (int i = 0; i < getNumRows(); i++) {
            indices[i] = i;
        }
        return getRowLabelsAsList(indices);
    }

    /**
     * get Col labels as list - full
     *
     * @return
     */
    public final List<String> getColLabelsAsList() {
        Integer[] indices = new Integer[getNumColumns()];
        for (int i = 0; i < getNumColumns(); i++) {
            indices[i] = i;
        }
        return getColLabelsAsList(indices);
    }

    public final String getName() {
        return _name;
    }

//    public final String getID() {
//        return _ID;
//    }
    public final void setName(String name) {
        if (name != null) {
            _name = name;
        }
    }

    public final boolean containsRowLabel(String label) {
        if (getIndexOfRowName(label) != null) {
            return true;
        } else {
            return false;
        }
    }

    public final boolean containsColLabel(String label) {
        if (getIndexOfColName(label) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * only true when two matrices are same class, and same row/column layout
     *
     * @param matrix2
     * @return
     */
    public final boolean canBeGroupedTogether(CMatrix<T> matrix2) {

        //System.out.println("Check1");
        //System.out.println(matrix2 == null);
        //System.out.println(this.getClass() != matrix2.getClass());
        //System.out.println(this.getNumColumns() != matrix2.getNumColumns());
        //System.out.println(this.getNumRows().intValue() != matrix2.getNumRows().intValue());
        //System.out.println(Objects.equal(this.getNumRows(), matrix2.getNumRows()));

        //System.out.println(this.getID() + " " + this.getNumRows());
        //System.out.println(matrix2.getID() + " " + matrix2.getNumRows());
        if (this == matrix2) {
            return true;
        }

        if (matrix2 == null
                || !Objects.equal(this.getClass(), matrix2.getClass())
                || !Objects.equal(this.getNumRows(), matrix2.getNumRows())
                || !Objects.equal(this.getNumColumns(), matrix2.getNumColumns())) {
            return false;
        }

//        System.out.println("Check2");
//        System.out.println("Check3");
        
        for (int i = 0; i < matrix2.getNumRows(); i++) {
            if (!Objects.equal(this.getRowLabel(i), matrix2.getRowLabel(i))) {
                return false;
            }
        }

//        System.out.println("Check4");
        for (int j = 0; j < matrix2.getNumColumns(); j++) {
            if (!Objects.equal(this.getColLabel(j), matrix2.getColLabel(j))) {
                return false;
            }
        }
        
        return true;
    }
    
    public String getDisplayLabel(){
        return "<html><strong>" + getName() +  "</strong> [" + getMemberClass().getSimpleName() + "] [ Rows:" + getNumRows() + ", Columns: " + getNumColumns() + "]</html>";
    }
}
