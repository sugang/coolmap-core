package coolmap.utils.statistics;

import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Keqiang Li
 */
public class MatrixUtil {
    
    public static List<Object> getRowLeafNodeNames(VNode rowNode, CMatrix matrix) {
        LinkedList<Object> rowNames = new LinkedList<>();
        Integer[] rowIndices;

        if (rowNode.isGroupNode()) {
            rowIndices = rowNode.getBaseIndicesFromCOntology(matrix, COntology.ROW);
            for (int rowIndex : rowIndices) {
                rowNames.add(matrix.getRowLabel(rowIndex));
            }
        } else {
            rowNames.add(rowNode.getName());
        }

        return rowNames;
    }

    public static List<Object> getColumnLeafNodeNames(VNode columnNode, CMatrix matrix) {
        LinkedList<Object> columnNames = new LinkedList<>();
        Integer[] colIndices;
        if (columnNode.isGroupNode()) {
            colIndices = columnNode.getBaseIndicesFromCOntology(matrix, COntology.COLUMN);
            for (int colIndex : colIndices) {
                columnNames.add(matrix.getColLabel(colIndex));
            }
        } else {
            columnNames.add(columnNode.getName());
        }
        return columnNames;
    }
}
