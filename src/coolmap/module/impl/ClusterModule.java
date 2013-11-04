/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module.impl;

import cern.colt.Arrays;
import coolmap.application.CoolMapMaster;
import coolmap.application.state.StateStorageMaster;
import coolmap.application.utils.LongTask;
import coolmap.application.utils.TaskEngine;
import coolmap.data.CoolMapObject;
import coolmap.data.state.CoolMapState;
import coolmap.module.Module;
import coolmap.utils.cluster.HCluster;
import edu.ucla.sspace.clustering.HierarchicalAgglomerativeClustering;
import edu.ucla.sspace.common.Similarity;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author gangsu
 */
public class ClusterModule extends Module {
    
    private HierarchicalAgglomerativeClustering.ClusterLinkage hClusterLinkage = HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE;
    private Similarity.SimType hClusterSimType = Similarity.SimType.PEARSON_CORRELATION;
    
    

    private class HClusterRowAction extends AbstractAction {

        public HClusterRowAction() {
            super("Cluster Row");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("H - Cluster rows") {

                @Override
                public void run() {
                    try {
//                            Thread.sleep(3000);
                        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                        if (obj == null) {
                            return;
                        }

                        CoolMapState state = CoolMapState.createStateRows("H-Cluster rows", obj, null);
                        HCluster.hclustRow(obj, hClusterLinkage, hClusterSimType);
                        StateStorageMaster.addState(state);

                    } catch (Exception e) {
//                        e.printStackTrace();
                        System.err.println("Cluster row error:" + e);
                    }
                }
            });
        }

    }

    private class HClusterColumnAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("H - Cluster columns") {

                @Override
                public void run() {
                    try {
                        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                        if (obj == null) {
                            return;
                        }

                        //To change body of generated methods, choose Tools | Templates.
                        CoolMapState state = CoolMapState.createStateColumns("H-Cluster columns", obj, null);
                        HCluster.hclustColumn(CoolMapMaster.getActiveCoolMapObject(), hClusterLinkage, hClusterSimType);
                        StateStorageMaster.addState(state);
                        
                    } catch (Exception e) {
                        System.err.println("Cluster columns error:" + e);
                    }
                }
            });
        }
    }

    private class HClusterConfigAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), "Config");
            if(returnVal == JOptionPane.YES_OPTION){
                System.err.println("yes!");
            }
        }

    }

    private class HClustPanel extends JPanel {

        HierarchicalAgglomerativeClustering.ClusterLinkage linkage;
        Similarity.SimType simType;
        Object[] linkages = HierarchicalAgglomerativeClustering.ClusterLinkage.values();
        Object[] simTypes = Similarity.SimType.values();

    }

    private void initHClust() {
        MenuItem item = new MenuItem("H-Cluster Row");
        CoolMapMaster.getCMainFrame().addMenuItem("Cluster/Hierarchical", item, false, false);
        item.addActionListener(new HClusterRowAction());

        item = new MenuItem("H-Cluster Column");
        CoolMapMaster.getCMainFrame().addMenuItem("Cluster/Hierarchical", item, false, false);
        item.addActionListener(new HClusterColumnAction());
        
        item = new MenuItem("Config...");
        CoolMapMaster.getCMainFrame().addMenuItem("Cluster/Hierarchical", item, false, false);
        item.addActionListener(new HClusterConfigAction());

        System.out.println(Arrays.toString(Similarity.SimType.values()));

    }

    public ClusterModule() {

//        item = new MenuItem("KMeans");
//        CoolMapMaster.getCMainFrame().addMenuItem("Analysis", item, false);
//        item.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                KMeans.kMeansClusterRow(CoolMapMaster.getActiveCoolMapObject(), 5);
//            }
//        });
        initHClust();
    }
}
