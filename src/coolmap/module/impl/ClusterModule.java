/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module.impl;

import coolmap.application.CoolMapMaster;
import coolmap.application.utils.LongTask;
import coolmap.application.utils.TaskEngine;
import coolmap.module.Module;
import coolmap.utils.cluster.HCluster;
import edu.ucla.sspace.clustering.HierarchicalAgglomerativeClustering;
import edu.ucla.sspace.common.Similarity;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 *
 * @author gangsu
 */
public class ClusterModule extends Module {

    public ClusterModule() {

        MenuItem item = new MenuItem("H-Cluster Row", new MenuShortcut(KeyEvent.VK_R));
        CoolMapMaster.getCMainFrame().addMenuItem("Analysis", item, false, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                //HCluster.hclustRow(CoolMapMaster.getActiveCoolMapObject(), HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.COSINE);
                TaskEngine.getInstance().submitTask(new LongTask("Cluster rows") {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                            HCluster.hclustRow(CoolMapMaster.getActiveCoolMapObject(), HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.COSINE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
//
//                TaskEngine.getInstance().submitTask(new LongTask("Cluster columns") {
//
//                    @Override
//                    public void run() {
//                        try{
//                        Thread.sleep(3000);
//                        HCluster.hclustColumn(CoolMapMaster.getActiveCoolMapObject(), HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.COSINE);
//                        return;
//                        }
//                        catch(Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//
//                });
            }

        });

        item = new MenuItem("H-Cluster Column");
        CoolMapMaster.getCMainFrame().addMenuItem("Analysis", item, false, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                //HCluster.hclustColumn(CoolMapMaster.getActiveCoolMapObject(), HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.COSINE);
            }
        });

//        item = new MenuItem("KMeans");
//        CoolMapMaster.getCMainFrame().addMenuItem("Analysis", item, false);
//        item.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                KMeans.kMeansClusterRow(CoolMapMaster.getActiveCoolMapObject(), 5);
//            }
//        });
    }
}
