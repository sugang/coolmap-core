/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils;

import coolmap.application.CoolMapMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author sugang
 */
public class DoubleMatrix {

    private final String name;
    private final double[][] data;
    private final String[] rowLabels;
    private final String[] columnLabels;
    
    public String[] getRowLabels(){
        return rowLabels;
    }
    
    public String[] getColumnLabels(){
        return columnLabels;
    }
    
    public static void initTest(){
        MenuItem menu = new MenuItem("Test");
        menu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                DoubleMatrix mx = DoubleMatrix.createFromCoolMapObject(CoolMapMaster.getActiveCoolMapObject(), true);
                mx.printMatrix();
            }
        });
        
        CoolMapMaster.getCMainFrame().addMenuItem("Test", menu, false, false);
    }

    public DoubleMatrix(String n, double[][] d, String[] rl, String[] cl) {
        name = n;
        data = d;
        rowLabels = rl;
        columnLabels = cl;
    }

    public double getValue(int i, int j) {
        return data[i][j];
    }

    public String getName() {
        return name;
    }

    public int getNumRows() {
        return data.length;
    }

    public int getNumColumns() {
        return data[0].length;
    }

    public String getRowLabel(int i) {
        return rowLabels[i];
    }

    public String getColumnLabel(int i) {
        return columnLabels[i];
    }

    public static DoubleMatrix createFromCoolMapObject(CoolMapObject obj, boolean selected) {
        if (obj == null) {
            return null;
        }

        if (!Double.class.isAssignableFrom(obj.getViewClass())) {
            return null;
        }

        if (selected) {
            List<VNode> vrs = obj.getViewNodesRowSelected();
            List<VNode> vcs = obj.getViewNodesColumnSelected();

            if (vrs.isEmpty() || vcs.isEmpty()) {
                return null;
            }

            double[][] data = new double[vrs.size()][vcs.size()];
            String name = obj.getName() + "_SELECTED";

            String[] rl = new String[vrs.size()];

            for (int i = 0; i < rl.length; i++) {
                rl[i] = obj.getViewNodeRow(vrs.get(i).getViewIndex().intValue()).getViewLabel();
            }

            String[] cl = new String[vcs.size()];

            for (int j = 0; j < cl.length; j++) {
                cl[j] = obj.getViewNodeColumn(vcs.get(j).getViewIndex().intValue()).getViewLabel();
            }

            try {
                for (int i = 0; i < rl.length; i++) {
                    for (int j = 0; j < cl.length; j++) {
                        Double v = (Double) obj.getViewValue(vrs.get(i).getViewIndex().intValue(), vcs.get(j).getViewIndex().intValue());
                        if (v == null || v.isInfinite() || v.isNaN()) {
                            return null;
                        } else {
                            data[i][j] = v;
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }

            DoubleMatrix mx = new DoubleMatrix(name, data, rl, cl);
            return mx;

        } else {
            double[][] data = new double[obj.getViewNumRows()][obj.getViewNumColumns()];
            String name = obj.getName();

            String[] rl = new String[obj.getViewNumRows()];
            for (int i = 0; i < obj.getViewNumRows(); i++) {
                rl[i] = obj.getViewNodeRow(i).getViewLabel();
            }

            String[] cl = new String[obj.getViewNumColumns()];
            for (int i = 0; i < obj.getViewNumColumns(); i++) {
                cl[i] = obj.getViewNodeColumn(i).getViewLabel();
            }

            try {
                for (int i = 0; i < rl.length; i++) {
                    for (int j = 0; j < cl.length; j++) {
                        Double v = (Double) obj.getViewValue(i, j);
                        if (v == null || v.isInfinite() || v.isNaN()) {
                            return null;
                        } else {
                            data[i][j] = v;
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }

            //data not assigned yet.
            DoubleMatrix mx = new DoubleMatrix(name, data, rl, cl);
            return mx;

        }

    }

    public void printMatrix() {
        System.out.println("Row labels: " + Arrays.toString(rowLabels));
        System.out.println("Col labels:" + Arrays.toString(columnLabels));
        

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                System.out.print(data[i][j] + " ");
            }
            System.out.println("");
        }

    }
    
    public double[][] getData(){
        return data;
    }

}
