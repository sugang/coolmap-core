/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.cmatrix.impl;

import coolmap.data.cmatrix.model.Model2DCMatrix;
import java.text.DecimalFormat;

/**
 *
 * @author gangsu
 */
public class DoubleCMatrix extends Model2DCMatrix<Double> {
    
//    private final DecimalFormat _format = new DecimalFormat("#.###");
    
    private DoubleCMatrix(String name, Double[][] data, String[] rowLabels, String[] colLabels) {
        super(name, data, Double.class, rowLabels, colLabels);
    }

    public DoubleCMatrix(String name, int numRow, int numCol) {
        super(name, numRow, numCol, Double.class);
    }
    
    public DoubleCMatrix(String name, int numRow, int numCol, String ID){
        super(name, numRow, numCol, Double.class, ID);
    }

    private DoubleCMatrix() {
        super(null, null, null, null);
    }

    /**
     *  returns a three digit format for snippet, used in tooltip
     * @param row
     * @param col
     * @return 
     */
//    @Override
//    public String getValueAsSnippet(int row, int col) {
//        Double b = getValue(row, col);
//        if(b==null){
//            return null;
//        }
//        else{
//            return _format.format(b);
//        }
//    }
    
    

    
}
