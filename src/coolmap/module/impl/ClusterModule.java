/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.module.impl;

//import cern.colt.Arrays;
import coolmap.application.CoolMapMaster;
import coolmap.application.state.StateStorageMaster;
import coolmap.application.utils.LongTask;
import coolmap.application.utils.TaskEngine;
import coolmap.data.CoolMapObject;
import coolmap.data.state.CoolMapState;
import coolmap.module.Module;
import coolmap.utils.cluster.Cluster;
import coolmap.utils.graphics.UI;
import edu.ucla.sspace.clustering.HierarchicalAgglomerativeClustering;
import edu.ucla.sspace.common.Similarity;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
                        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                        if (obj == null) {
                            return;
                        }

                        CoolMapState state = CoolMapState.createStateRows("H-Cluster rows", obj, null);
                        Cluster.hClustRow(obj, hClustPanel.hClusterLinkage, hClustPanel.hClusterSimType);
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
                        Cluster.hClustColumn(CoolMapMaster.getActiveCoolMapObject(), hClustPanel.hClusterLinkage, hClustPanel.hClusterSimType);
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
            int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), hClustPanel, "HClust Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, UI.getImageIcon("gearSmall"));
            if (returnVal == JOptionPane.OK_OPTION) {
//                System.err.println("yes!");
                hClustPanel.setParameter();                
            }
        }

    }
    
    private class ObjectSorter implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
        
    }
    
    

    private HClustPanel hClustPanel = new HClustPanel();

    private class HClustPanel extends JPanel {

//        HierarchicalAgglomerativeClustering.ClusterLinkage linkage;
//        Similarity.SimType simType;
        
        Object[] linkages; //= HierarchicalAgglomerativeClustering.ClusterLinkage.values();
        Object[] simTypes; // = Similarity.SimType.values();
        
        public HierarchicalAgglomerativeClustering.ClusterLinkage hClusterLinkage = HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE;
        public Similarity.SimType hClusterSimType = Similarity.SimType.PEARSON_CORRELATION;
        
        private final JComboBox linkageCombo;
        private final JComboBox similarityCombo;

        public HClustPanel() {
            
            linkages = HierarchicalAgglomerativeClustering.ClusterLinkage.values();
            simTypes = Similarity.SimType.values();
            
            Arrays.sort(linkages, new ObjectSorter());
            Arrays.sort(simTypes, new ObjectSorter());
//            Arrays.sort(simTypes);
            
            linkageCombo = new JComboBox(linkages);
            similarityCombo = new JComboBox(simTypes);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            setLayout(new GridBagLayout());
            c.insets = new Insets(5, 5, 5, 5);
            
            //row one
            c.gridx = 0;
            add(new JLabel("Linkage metric:"), c);
            c.gridx = 1;
            add(linkageCombo, c);
            
            //row two
            c.gridx = 0;
            c.gridy ++;
            add(new JLabel("Similarity metric:"), c);
            c.gridx = 1;
            add(similarityCombo, c);
            
            
            linkageCombo.setSelectedItem(HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE);
            similarityCombo.setSelectedItem(Similarity.SimType.PEARSON_CORRELATION);
            
        }
        
        public void setParameter(){
            hClusterLinkage = (HierarchicalAgglomerativeClustering.ClusterLinkage)linkageCombo.getSelectedItem();
            hClusterSimType = (Similarity.SimType)similarityCombo.getSelectedItem();
        }

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
