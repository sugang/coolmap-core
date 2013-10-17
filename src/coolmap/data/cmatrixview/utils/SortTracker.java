/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.cmatrixview.utils;

import coolmap.data.cmatrixview.model.VNode;

/**
 *
 * @author gangsu
 */
public class SortTracker {

    public VNode lastSortedRow = null;
    public boolean lastSortedRowDescending = false;
    public VNode lastSortedColumn = null;
    public boolean lastSortedColumnDescending = false;

    public void clear() {
        lastSortedRow = null;
        lastSortedRowDescending = false;
        lastSortedColumn = null;
        lastSortedColumnDescending = false;
    }

    public void clearSortedRow() {
        lastSortedRow = null;
        lastSortedRowDescending = false;
    }

    public void clearSortedColumn() {
        lastSortedColumn = null;
        lastSortedColumnDescending = false;
    }
}
