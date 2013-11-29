/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.statistics.test;

import coolmap.application.widget.impl.console.CMConsole;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.TTest;

/**
 *
 * @author sugang
 */
public class CTest {

    public enum Dimension {

        ROW, COLUMN
    };

    public static double[] extractRow(CoolMapObject<?, Double> object, VNode rowNode) {
        try {
            if (object.getViewNodesRow().contains(rowNode)) {

                double[] data = new double[object.getViewNumColumns()];
                for (int j = 0; j < object.getViewNumColumns(); j++) {
                    data[j] = object.getViewValue(rowNode.getViewIndex().intValue(), j).doubleValue(); //nulls will throw exception
                }

                return data;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static double[] extractColumn(CoolMapObject<?, Double> object, VNode colujmnNode) {
        try {
            if (object.getViewNodesColumn().contains(colujmnNode)) {

                double[] data = new double[object.getViewNumRows()];
                for (int i = 0; i < object.getViewNumColumns(); i++) {
                    data[i] = object.getViewValue(i, colujmnNode.getViewIndex().intValue()).doubleValue(); //nulls will throw exception
                }

                return data;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static void ttest(CoolMapObject obj, Dimension direction, VNode leafNode1, VNode leafNode2) {
        try {
            ArrayList<VNode> leafNodes = new ArrayList<VNode>();
            if (direction == Dimension.ROW) {
                leafNodes.addAll(obj.getViewNodesRow());
            } else if (direction == Dimension.COLUMN) {
                leafNodes.addAll(obj.getViewNodesColumn());
            } else {
                return;
            }

            if (leafNodes.contains(leafNode1) && leafNodes.contains(leafNode2)) {

                double[] data1 = null;
                double[] data2 = null;

                if (direction == Dimension.ROW) {
                    data1 = extractRow(obj, leafNode1);
                    data2 = extractRow(obj, leafNode2);
                } else if (direction == Dimension.COLUMN) {
                    data1 = extractColumn(obj, leafNode1);
                    data2 = extractColumn(obj, leafNode2);
                }

                TTest test = new TTest();
                double pValue = test.pairedTTest(data1, data2);

                TestResult result = new TestResult("Student Paired T-Test", "Two tailed paired t-test with alpha = 0.05.\nData: " + obj.getName() + "\nDirection: " + direction + "\nGroups: " + leafNode1 + ", " + leafNode2, pValue);
                CMConsole.logData(result.toString());

            } else {
                CMConsole.logError("T-test error: group dimension mismatch, must both be row ontology group or column ontology groups.");
            }

        } catch (Exception e) {
            CMConsole.logError("T-test error: " + " Dataset:" + obj + " Direction:" + direction + " Group1:" + leafNode1 + " Group2:" + leafNode2);
        }
    }

    //wilcox - need to convert the statistic to p-value
    //anova is easy to implement
    public static void anova(CoolMapObject obj, Dimension direction, VNode... nodes) {
        try {
            ArrayList<VNode> leafNodes = new ArrayList<VNode>();
            if (direction == Dimension.ROW) {
                leafNodes.addAll(obj.getViewNodesRow());
            } else if (direction == Dimension.COLUMN) {
                leafNodes.addAll(obj.getViewNodesColumn());
            } else {
                return;
            }

            ArrayList<double[]> data = new ArrayList<double[]>();
            if (direction == Dimension.ROW) {
                for(VNode node : nodes){
                    data.add(extractRow(obj, node));
                }
            } else if (direction == Dimension.COLUMN) {
                for(VNode node : nodes){
                    data.add(extractColumn(obj, node));
                }
            }

            OneWayAnova anova = new OneWayAnova();
            double pValue = anova.anovaPValue(data);
            TestResult result = new TestResult("One-way ANOVA", "One way analysis of variance wiht alpha = 0.05. \nData: " + obj.getName() + "\nDirection: " + direction + "\nGroups: " + Arrays.toString(nodes), pValue);
            CMConsole.logData(result.toString());

        } catch (Exception e) {
            CMConsole.logError("ANOVA error: " + " Dataset:" + obj + " Direction:" + direction + " Groups:" + nodes == null ? null : Arrays.toString(nodes));
        }
    }

}
