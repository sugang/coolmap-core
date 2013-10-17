/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.aggregator.impl;

import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.model.CMatrix;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.rank.Median;

/**
 *
 * @author gangsu
 */
public class DoubleDoubleMedian extends CAggregator<Double, Double>{
    
    public DoubleDoubleMedian(){
        super("Double.Median", "Median", "Returns the mean value from a collection of numeric values", Double.class, Double.class, null);
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

        Double value;
        Median median = new Median();
        ArrayList<Double> data = new ArrayList<Double>();
        for (Double item : items) {
            if (item != null && !item.isNaN()) {
                data.add(item);
            }
        }
        if(data.isEmpty()){
            return null;
        }
        
        double[] data2 = new double[data.size()];
        for(int i=0; i<data.size(); i++){
            data2[i] = data.get(i);
        }
        return median.evaluate(data2);
    }
}
