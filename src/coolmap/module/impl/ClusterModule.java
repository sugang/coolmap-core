/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module.impl;

import coolmap.application.CoolMapMaster;
import coolmap.module.Module1;
import coolmap.utils.cluster.HCluster;
import edu.ucla.sspace.clustering.HierarchicalAgglomerativeClustering;
import edu.ucla.sspace.common.Similarity;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author gangsu
 */
public class ClusterModule extends Module1 {

    public ClusterModule() {

        MenuItem item = new MenuItem("H-Cluster Row");
        CoolMapMaster.getCMainFrame().addMenuItem("Analysis", item, false, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                HCluster.hclustRow(CoolMapMaster.getActiveCoolMapObject(), HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.COSINE);
            }
        });

        item = new MenuItem("H-Cluster Column");
        CoolMapMaster.getCMainFrame().addMenuItem("Analysis", item, false, false);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                HCluster.hclustColumn(CoolMapMaster.getActiveCoolMapObject(), HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.COSINE);
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
