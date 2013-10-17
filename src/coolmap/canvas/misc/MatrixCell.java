/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.misc;

import com.google.common.base.Objects;
import coolmap.data.CoolMapObject;

/**
 *
 * @author gangsu
 */
public class MatrixCell {

    //incase want non-integer cell
    public Float row = null;
    public Float col = null;

    public void confineToValidCell(CoolMapObject object) {
        if (object == null) {
            row = 0f;
            col = 0f;
            return;
        }

        if (row == null || row < 0) {
            row = 0f;
        } else if (row >= object.getViewNumRows()) {
            row = new Float(object.getViewNumRows() - 1);
        }


        if (col == null || col < 0) {
            col = 0f;
        } else if (col >= object.getViewNumColumns()) {
            col = new Float(object.getViewNumColumns() - 1);
        }
    }

    public MatrixCell() {
        row = null;
        col = null;
    }

    public void col(int col) {
        this.col = new Float(col);
    }

    public void row(int row) {
        this.row = new Float(row);
    }

    public void col(Integer col) {
        if (col == null) {
            this.col = null;
        } else {
            this.col = col.floatValue();
        }
    }

    public void colF(Float col) {
        if (col == null) {
            this.col = null;
        } else {
            this.col = col.floatValue();
        }
    }

    public void row(Integer row) {
        if (row == null) {
            this.row = null;
        } else {
            this.row = row.floatValue();
        }
    }

    public void rowF(Float row) {
        if (row == null) {
            this.row = null;
        } else {
            this.row = row.floatValue();
        }
    }

    public MatrixCell(Float row, Float col) {
        this.row = row;
        this.col = col;
    }

    public MatrixCell(Integer row, Integer col) {
        if (row == null) {
            this.row = null;
        } else {
            this.row = new Float(row);
        }
        if (col == null) {
            this.col = null;
        } else {
            this.col = new Float(col);
        }
    }

    @Override
    public Object clone() {
        return new MatrixCell(row, col);
    }

    public MatrixCell duplicate() {
        return new MatrixCell(row, col);
    }

    @Override
    public String toString() {
        return "Row:" + row + " Col:" + col;
    }

    public boolean isValidCell(CoolMapObject object) {
        if (isRowValidCell(object) && isColValidCell(object)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isValidRange(CoolMapObject object) {
        if (isRowValidRange(object) && isColValidRange(object)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRowValidRange(CoolMapObject object) {
        if (row != null && row >= 0 && row <= object.getViewNumRows()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isColValidRange(CoolMapObject object) {
        if (col != null && col >= 0 && col <= object.getViewNumColumns()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRowValidCell(CoolMapObject object) {
        if (row != null && row >= 0 && row < object.getViewNumRows()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isColValidCell(CoolMapObject object) {
        if (col != null && col >= 0 && col < object.getViewNumColumns()) {
            return true;
        } else {
            return false;
        }
    }

    public void setValueTo(MatrixCell cell) {
        if (cell == null) {
            row = null;
            col = null;
        } else {
            row = cell.row;
            col = cell.col;
        }

    }

    @Override
    public boolean equals(Object o) {
        try {
            return valueEquals((MatrixCell) o);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + (this.row != null ? this.row.hashCode() : 0);
        hash = 71 * hash + (this.col != null ? this.col.hashCode() : 0);
        return hash;
    }

    public boolean valueEquals(MatrixCell cell) {
//            if(cell == null){
//                return false;
//            }
//            
//            if(col != null && row != null && cell.col != null && cell.row != null){
//                int thisCol = col.intValue();
//                int thisRow = row.intValue();
//                int cellCol = cell.col.intValue();
//                int cellRow = cell.row.intValue();
//                return(thisCol == cellCol && thisRow == cellRow);
//            }
//            
//           
//            return false;
        if (cell == null) {
            return false;
        }

        return Objects.equal(row, cell.row) && Objects.equal(col, cell.col);
    }

    public Integer getRow() {
        if (row == null) {
            return null;
        } else {
            return row.intValue();
        }
    }
    
    public Integer getCol() {
        if( col == null){
            return null;
        } 
        else {
            return col.intValue();
        }
    }
}
