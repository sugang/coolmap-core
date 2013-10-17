/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.aggregator.impl;

import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrix.model.NetworkCMatrix;
import coolmap.utils.network.LNetwork;
import java.util.Collection;

/**
 * it's some difficulty here
 *
 * @author gangsu
 */
public class DoubleToNetwork extends CAggregator<Double, LNetwork> {

    public DoubleToNetwork() {
        super("Double.Network", "Ntwk", "Returns a network from sparse matrix", Double.class, LNetwork.class, null);
    }

    @Override
    public LNetwork getAggregation(Double item, Collection<CMatrix> matrices, Integer rowIndex, Integer columnIndex) {
        //single item, return null.
        //no need to aggregate
        return null;
    }

    @Override
    public LNetwork getAggregation(Collection<Double> item, Collection<CMatrix> matrices, Collection<Integer> rowIndices, Collection<Integer> columnIndices) {
        //supports only 1 network
        //System.out.println("Get aggregation");
        
        if (matrices == null || matrices.isEmpty()) {
            return null;
        }

        CMatrix matrix = matrices.iterator().next();
        if (!(matrix instanceof NetworkCMatrix)) {
            return null;
        }
        
        //System.out.println(rowIndices + " " + columnIndices);

        try {
            return ((NetworkCMatrix) matrix).getLNetwork(rowIndices, columnIndices);
        } catch (Exception e) {
            return null;
        }
    }
}
