/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.aggregator.impl;

import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.model.CMatrix;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author gangsu
 */
public class DoubleDoubleSum extends CAggregator<Double, Double> {

    public DoubleDoubleSum(){
        super("Double.Sum", "Sum", "Returns the sum a collection of numeric values", Double.class, Double.class, null);
    }
    
    @Override
    public Double getAggregation(Double item, Collection<CMatrix> matrices, Integer rowIndex, Integer columnIndex) {
        return item;
    }

    @Override
    public Double getAggregation(Collection<Double> items, Collection<CMatrix> matrices, Collection<Integer> rowIndices, Collection<Integer> columnIndices) {
        
        //items.removeAll(Collections.singletonList(null));
        
        Double sum = null;
        for (Double item : items) {
            if (item == null || item.isNaN()) {
                continue;
            } else if (sum == null || sum.isNaN()) {
                sum = item;
            } else {
                sum += item;
            }
        }
        return sum;
    }
}
