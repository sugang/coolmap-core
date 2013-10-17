/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.aggregator.impl;

import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.model.CMatrix;
import java.util.Collection;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.rank.Min;

/**
 *
 * @author gangsu
 */
public class DoubleDoubleMin extends CAggregator<Double, Double> {
        public DoubleDoubleMin(){
        super("Double.Min", "Min", "Returns the min value from a collection of numeric values", Double.class, Double.class, null);
        //System.out.println("The tip name is:" + getTipName());
    }

    @Override
    public Double getAggregation(Double item, Collection<CMatrix> matrices, Integer rowIndex, Integer columnIndex) {
        return item;
    }

    @Override
    public Double getAggregation(Collection<Double> items, Collection<CMatrix> matrices, Collection<Integer> rowIndices, Collection<Integer> columnIndices) {
        if(items == null || items.isEmpty()){
            return null;
        }
        
        Double value;
        Min min = new Min();
        for(Double item : items){
            if(item != null && !item.isNaN()){
                min.increment(item);
            }
        }
        return min.getResult();
        
    }
}
