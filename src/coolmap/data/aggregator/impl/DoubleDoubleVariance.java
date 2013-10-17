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
import org.apache.commons.math3.stat.descriptive.moment.Variance;

/**
 *
 * @author gangsu
 */
public class DoubleDoubleVariance extends CAggregator<Double, Double> {

    public DoubleDoubleVariance() {
        super("Double.Var", "Var", "Returns the variance from a collection of numeric values", Double.class, Double.class, null);
    }

    @Override
    public Double getAggregation(Double item, Collection<CMatrix> matrices, Integer rowIndex, Integer columnIndex) {
        return item;
    }

    @Override
    public Double getAggregation(Collection<Double> items, Collection<CMatrix> matrices, Collection<Integer> rowIndices, Collection<Integer> columnIndices) {

        ArrayList<Double> a = new ArrayList<Double>(items.size());
        for (Double item : items) {
            if (item == null || item.isNaN()) {
                continue;
            }
            a.add(item);
        }

        if (a.isEmpty()) {
            return null;
        }

        double[] b = new double[a.size()];
        for (int i = 0; i < b.length; i++) {
            b[i] = a.get(i);
        }

        Variance variance = new Variance();
        return variance.evaluate(b);

    }
}
