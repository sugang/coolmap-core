/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module.impl;

import cern.colt.Arrays;
import coolmap.application.CoolMapMaster;
import coolmap.application.utils.LongTask;
import coolmap.application.utils.TaskEngine;
import coolmap.module.Module;
import coolmap.utils.cluster.HCluster;
import edu.ucla.sspace.clustering.HierarchicalAgglomerativeClustering;
import edu.ucla.sspace.common.Similarity;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPanel;

/**
 *
 * @author gangsu
 */
public class ClusterModule extends Module {

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
                        HCluster.hclustRow(CoolMapMaster.getActiveCoolMapObject(), HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.COSINE);
                    } catch (Exception e) {
                        e.printStackTrace();
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

                    //To change body of generated methods, choose Tools | Templates.
                    HCluster.hclustColumn(CoolMapMaster.getActiveCoolMapObject(), HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.COSINE);
                }
            });
        }
    }
    
    private class HClusterConfig extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            
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
