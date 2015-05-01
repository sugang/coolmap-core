/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.aggregator.impl;

import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.model.CMatrix;
import java.util.Collection;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

/**
 *
 * @author gangsu
 */
public class DoubleDoubleMean extends CAggregator<Double, Double>{
    
    public DoubleDoubleMean(){
        super("Double.Mean", "Mean", "Returns the mean value from a collection of numeric values", Double.class, Double.class, null);
    }
    
    

    @Override
    public Double getAggregation(Double item, Collection<CMatrix> matrices, Integer rowIndex, Integer columnIndex) {
        return item;
    }

    @Override
    public Double getAggregation(Collection<Double> items, Collection<CMatrix> matrices, Collection<Integer> rowIndices, Collection<Integer> columnIndices) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        Mean mean = new Mean();
        for (Double item : items) {
            if (item != null && !item.isNaN()) {
                mean.increment(item);
            }
        }
        return mean.getResult();
    }
}
