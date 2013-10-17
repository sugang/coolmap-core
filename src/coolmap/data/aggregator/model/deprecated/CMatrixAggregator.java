/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.aggregator.model.deprecated;

import coolmap.data.cmatrix.model.CMatrix;
import coolmap.utils.Tools;
import java.util.UUID;
import javax.swing.JComponent;

/**
 * Make sure the CMatrix supplied is not null!
 *
 * @author gangsu
 */
public abstract class CMatrixAggregator<INCLASS, OUTCLASS> {

    private final Class _inClass;
    private final Class _outClass;
    private final String _ID;
    private String _name;
    private String _description;
    private JComponent _ui;

    private CMatrixAggregator() {
//        _inClass = Object.class;
//        _outClass = Object.class;
//        _ID = Tools.generateRandomID();
//        _name = null;
//        _description = null;
        this(null, null, null, null);
    }

    public CMatrixAggregator(String name, String description, Class<INCLASS> inClass, Class<OUTCLASS> outClass) {
        _ID = Tools.randomID();
        _name = name;
        _inClass = inClass;
        _outClass = outClass;
        _description = description;
    }

    public final String getDescription() {
        return "<html>" + _description + "<br/><hr/>" + "aggregate from type: <br/><b>[" + _inClass + "]</b><br/> to type: <br/><b>[" + _outClass + "]</b></html>";
    }

    public boolean canAggregate(CMatrix cMatrix) {
        if (cMatrix == null || _inClass == null) {
            return false;
        }
        return _inClass.isAssignableFrom(cMatrix.getMemberClass());
    }

//    //place holder here. Maybe for a hybrid cMatrix, choose aggregate methods
//    public boolean canAggregateRows(CMatrix cMatrix, int[] rows) {
//
//        for (int i = 0; i < rows.length; i++) {
//            if (!_inClass.isAssignableFrom(cMatrix.getRowClass(rows[i]))) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public boolean canAggregateCols(CMatrix cMatrix, int[] cols) {
//
//        for (int i = 0; i < cols.length; i++) {
//            if (!_inClass.isAssignableFrom(cMatrix.getRowClass(cols[i]))) {
//                return false;
//            }
//        }
//        return true;
//    }

    public Class<OUTCLASS> getExportClass() {
        return (Class<OUTCLASS>) _outClass;
    }

    public Class<INCLASS> getImportClass() {
        return (Class<INCLASS>) _inClass;
    }

    public final String getName() {
        return _name;
    }

    @Override
    public String toString() {
        return _name + " " + _inClass + " " + _outClass;
    }

    public abstract JComponent getConfigUI();

    public abstract OUTCLASS getAggregation(Integer row, Integer[] cols, CMatrix<INCLASS>... m);
    public abstract OUTCLASS getAggregation(Integer[] rows, Integer col, CMatrix<INCLASS>... m);
    public abstract OUTCLASS getAggregation(Integer row, Integer col, CMatrix<INCLASS>... m);
    public abstract OUTCLASS getAggregation(Integer[] rows, Integer[] cols, CMatrix<INCLASS>... m);
     
}
