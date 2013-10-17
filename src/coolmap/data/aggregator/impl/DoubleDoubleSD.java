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
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Min;

/**
 *
 * @author gangsu
 */
public class DoubleDoubleSD extends CAggregator<Double, Double> {

    public DoubleDoubleSD() {
        super("Double.SD", "SD", "Returns the standard deviation from a collection of numeric values", Double.class, Double.class, null);
        //System.out.println("The tip name is:" + getTipName());
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

        ArrayList<Double> a = new ArrayList<Double>(items.size());
        for (Double item : items) {
            if (item == null || item.isNaN()) {
                continue;
            }
            a.add(item);
        }
        
        if(a.isEmpty()){
            return null;
        }

        double[] b = new double[a.size()];
        for (int i = 0; i < b.length; i++) {
            b[i] = a.get(i);
        }
        
        StandardDeviation standardDeviation = new StandardDeviation();
        return standardDeviation.evaluate(b);
    }
}
