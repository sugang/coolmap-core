/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module.impl;

import com.google.common.collect.Range;
import coolmap.application.CoolMapMaster;
import coolmap.application.widget.WidgetMaster;
import coolmap.application.widget.impl.WidgetViewport;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.module.Module;
import coolmap.utils.statistics.test.TestResult;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.apache.commons.math3.stat.inference.OneWayAnova;

/**
 *
 * @author sugang
 */
public class QuickStatModule extends Module {

    public QuickStatModule() {

        WidgetViewport viewport = WidgetMaster.getViewport();
        viewport.addPopupMenuSeparator(null);

        JMenuItem item = new JMenuItem("ANOVA(t-test)");
        item.addActionListener(new RowTest());
        viewport.addPopupMenuItem("Quick stat selected.../Row", item, false);

        item = new JMenuItem("ANOVA(t-test)");
        item.addActionListener(new ColumnTest());
        viewport.addPopupMenuItem("Quick stat selected.../Column", item, false);

    }

    private class RowTest extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                //now you have selectedrows and selected columns, add indices
                ArrayList<Range<Integer>> selectedRows = obj.getCoolMapView().getSelectedRows();
                ArrayList<Integer> rowIndices = new ArrayList<Integer>();

                for (Range<Integer> range : selectedRows) {
                    for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                        rowIndices.add(i);
                    }
                }
                ArrayList<Integer> columnIndices = new ArrayList<Integer>();
                ArrayList<Range<Integer>> selectedColumns = obj.getCoolMapView().getSelectedColumns();

                for (Range<Integer> range : selectedColumns) {
                    for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                        columnIndices.add(i);
                    }
                }

                //this is for comparing rows, though this is unlikely
//                if(selectedColumns.isEmpty()){
//                    for(int i=0; i<obj.getViewNumColumns();i++){
//                        columnIndices.add(i);
//                    }
//                }
                if (rowIndices.isEmpty() || columnIndices.isEmpty() || rowIndices.size() <= 1) {
                    return;
                }

                ArrayList<double[]> data = new ArrayList<double[]>();

                ArrayList<VNode> nodes = new ArrayList<VNode>();
                for (Integer i : rowIndices) {
                    double[] rowData = new double[columnIndices.size()];
                    int c = 0;
                    for (Integer j : columnIndices) {
                        try {
                            rowData[c] = (Double) obj.getViewValue(i, j);
                        } catch (Exception ex2) {
                        }
                        c++;
                    }
                    data.add(rowData);
                    nodes.add(obj.getViewNodeRow(i));
                }

                OneWayAnova anova = new OneWayAnova();
                double pValue = anova.anovaPValue(data);

                String title = "One-way ANOVA";
                if (rowIndices.size() == 2) {
                    title = "Student Paired T-Test";
                }

                TestResult result = new TestResult(title, "One way analysis of variance wiht alpha = 0.05. \nData: " + obj.getName()
                        + "\nDirection: " + "Row" + "\nTest Groups: " + Arrays.toString(nodes.toArray())
                        + "\nData size: " + columnIndices.size(), pValue);

                CMConsole.logData(result.toString());

                //Then extract these values
            } catch (Exception ex) {
            }
        }

    }

    private class ColumnTest extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                if (obj == null) {
                    return;
                }

                //now you have selectedrows and selected columns, add indices
                ArrayList<Range<Integer>> selectedRows = obj.getCoolMapView().getSelectedRows();
                ArrayList<Integer> rowIndices = new ArrayList<Integer>();

                for (Range<Integer> range : selectedRows) {
                    for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                        rowIndices.add(i);
                    }
                }
                ArrayList<Integer> columnIndices = new ArrayList<Integer>();
                ArrayList<Range<Integer>> selectedColumns = obj.getCoolMapView().getSelectedColumns();

                for (Range<Integer> range : selectedColumns) {
                    for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
                        columnIndices.add(i);
                    }
                }

                //this is for comparing rows, though this is unlikely
//                if(selectedColumns.isEmpty()){
//                    for(int i=0; i<obj.getViewNumColumns();i++){
//                        columnIndices.add(i);
//                    }
//                }
                if (rowIndices.isEmpty() || columnIndices.isEmpty() || columnIndices.size() <= 1) {
                    return;
                }

                ArrayList<double[]> data = new ArrayList<double[]>();

                ArrayList<VNode> nodes = new ArrayList<VNode>();

                for (Integer i : columnIndices) {
                    double[] columnData = new double[rowIndices.size()];

                    int c = 0;
                    for (Integer j : rowIndices) {
                        try {
                            columnData[c] = (Double) obj.getViewValue(j, i);
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                        }
                        c++;
                    }
                    data.add(columnData);
                    nodes.add(obj.getViewNodeColumn(i));
                }

                OneWayAnova anova = new OneWayAnova();
                double pValue = anova.anovaPValue(data);

                String title = "One-way ANOVA";
                if (columnIndices.size() == 2) {
                    title = "Student Paired T-Test";
                }

                TestResult result = new TestResult(title, "One way analysis of variance wiht alpha = 0.05. \nData: " + obj.getName()
                        + "\nDirection: " + "Column" + "\nTest Groups: " + Arrays.toString(nodes.toArray())
                        + "\nData size: " + rowIndices.size(), pValue);

                CMConsole.logData(result.toString());

                //Then extract these values
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}
