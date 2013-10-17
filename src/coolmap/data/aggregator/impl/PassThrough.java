/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.aggregator.impl;

import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.model.CMatrix;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author gangsu
 */
public class PassThrough extends CAggregator<Object, String> {
    
    public PassThrough(){
        super("Object.Text", "Text", "Converts data objects to text using 'toString()'", Object.class, String.class, null);
    }

    @Override
    public String getAggregation(Object item, Collection<CMatrix> matrices, Integer rowIndex, Integer columnIndex) {
        if( item == null )return null;
        return item.toString();
    }

    @Override
    public String getAggregation(Collection<Object> item, Collection<CMatrix> matrices, Collection<Integer> rowIndices, Collection<Integer> columnIndices) {
        if(item == null)return null;
        return "[Group of:" + item.size() + "]";
    }
    
}
