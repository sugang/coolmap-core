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
import coolmap.utils.Tools;
import coolmap.utils.cluster.Cluster;
import coolmap.utils.graphics.UI;
import edu.ucla.sspace.clustering.ClusteringByCommittee;
import edu.ucla.sspace.clustering.HierarchicalAgglomerativeClustering;
import edu.ucla.sspace.clustering.NeighborChainAgglomerativeClustering;
import edu.ucla.sspace.clustering.criterion.CriterionFunction;
import edu.ucla.sspace.clustering.criterion.H1Function;
import edu.ucla.sspace.clustering.criterion.H2Function;
import edu.ucla.sspace.clustering.criterion.I1Function;
import edu.ucla.sspace.clustering.criterion.I2Function;
import edu.ucla.sspace.clustering.seeding.GeneralizedOrssSeed;
import edu.ucla.sspace.clustering.seeding.KMeansPlusPlusSeed;
import edu.ucla.sspace.clustering.seeding.KMeansSeed;
import edu.ucla.sspace.clustering.seeding.OrssSeed;
import edu.ucla.sspace.clustering.seeding.RandomSeed;
import edu.ucla.sspace.common.Similarity;
import edu.ucla.sspace.similarity.AverageCommonFeatureRank;
import edu.ucla.sspace.similarity.CosineSimilarity;
import edu.ucla.sspace.similarity.DotProduct;
import edu.ucla.sspace.similarity.EuclideanSimilarity;
import edu.ucla.sspace.similarity.GaussianKernel;
import edu.ucla.sspace.similarity.JaccardIndex;
import edu.ucla.sspace.similarity.KLDivergence;
import edu.ucla.sspace.similarity.KendallsTau;
import edu.ucla.sspace.similarity.LinSimilarity;
import edu.ucla.sspace.similarity.OneSimilarity;
import edu.ucla.sspace.similarity.PearsonCorrelation;
import edu.ucla.sspace.similarity.PolynomialKernel;
import edu.ucla.sspace.similarity.SimilarityFunction;
import edu.ucla.sspace.similarity.SpearmanRankCorrelation;
import edu.ucla.sspace.similarity.TanimotoCoefficient;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author gangsu
 */
public class ClusterModule extends Module {

    private class CBCRowsAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("CBC - Cluster rows") {

                @Override
                public void run() {
                    Properties properties = new Properties();
                    properties.put(ClusteringByCommittee.AVERGAGE_LINK_MERGE_THRESHOLD_PROPERTY, cbcPanel.avgLinkMergeThresh);
                    properties.put(ClusteringByCommittee.COMMITTEE_SIMILARITY_THRESHOLD_PROPERTY, cbcPanel.maxCommitteeSimThresh);
                    properties.put(ClusteringByCommittee.RESIDUE_SIMILARITY_THRESHOLD_PROPERTY, cbcPanel.residueSimThresh);
                    properties.put(ClusteringByCommittee.SOFT_CLUSTERING_SIMILARITY_THRESHOLD_PROPERTY, cbcPanel.softClusteringThresh);
                    properties.put(ClusteringByCommittee.HARD_CLUSTERING_PROPERTY, cbcPanel.useHardClustering);
                    Cluster.cbcRow(CoolMapMaster.getActiveCoolMapObject(), cbcPanel.nullsAsZero, "CBC rows(" + Tools.randomID() + ")", properties);

                }
            });
        }

    }

    private class CBCColumnsAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("CBC - Cluster columns") {

                @Override
                public void run() {
                    Properties properties = new Properties();
                    properties.put(ClusteringByCommittee.AVERGAGE_LINK_MERGE_THRESHOLD_PROPERTY, cbcPanel.avgLinkMergeThresh);
                    properties.put(ClusteringByCommittee.COMMITTEE_SIMILARITY_THRESHOLD_PROPERTY, cbcPanel.maxCommitteeSimThresh);
                    properties.put(ClusteringByCommittee.RESIDUE_SIMILARITY_THRESHOLD_PROPERTY, cbcPanel.residueSimThresh);
                    properties.put(ClusteringByCommittee.SOFT_CLUSTERING_SIMILARITY_THRESHOLD_PROPERTY, cbcPanel.softClusteringThresh);
                    properties.put(ClusteringByCommittee.HARD_CLUSTERING_PROPERTY, cbcPanel.useHardClustering);
                    Cluster.cbcColumn(CoolMapMaster.getActiveCoolMapObject(), cbcPanel.nullsAsZero, "CBC columns(" + Tools.randomID() + ")", properties);
                }
            });
        }

    }

    private class DirectKmeansRowAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("DKmeans - Cluster rows") {

                @Override
                public void run() {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    //Default: I1, random seed
//                    System.err.println("num clusters" + dKmeansPanel.numClusters); //why this number is one???
                    Cluster.directKmeansRow(CoolMapMaster.getActiveCoolMapObject(), dKmeansPanel.numClusters, dKmeansPanel.nullsAsZero, "Direct Kmeans Row (" + Tools.randomID() + ")", dKmeansPanel.criterionFunction, dKmeansPanel.seedType, dKmeansPanel.numIterations);

                }
            });
        }

    }

    private class NCARowsAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("NCA - Cluster rows") {
                @Override
                public void run() {
                    Cluster.ncaRow(CoolMapMaster.getActiveCoolMapObject(), ncaPanel.numClusters, ncaPanel.nullsAsZero, "NCA Row (" + Tools.randomID() + ")", ncaPanel.similarityFunction, ncaPanel.link);
                }
            });
        }
    }

    private class NCAColumnsAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("NCA - Cluster columns") {
                @Override
                public void run() {
                    Cluster.ncaColumns(CoolMapMaster.getActiveCoolMapObject(), ncaPanel.numClusters, ncaPanel.nullsAsZero, "NCA Column (" + Tools.randomID() + ")", ncaPanel.similarityFunction, ncaPanel.link);
                }
            });
        }
    }

    private class DirectKmeansColumnAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("DKmean - Cluster rows") {

                @Override
                public void run() {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    //Default: I1, random seed
                    //System.err.println("num clusters" + dKmeansPanel.numClusters); //why this number is one???
                    Cluster.directKmeansColumn(CoolMapMaster.getActiveCoolMapObject(), dKmeansPanel.numClusters, dKmeansPanel.nullsAsZero, "Direct Kmeans Column (" + Tools.randomID() + ")", dKmeansPanel.criterionFunction, dKmeansPanel.seedType, dKmeansPanel.numIterations);

                }
            });
        }

    }

    private class GapKmeansRowAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("Gap Kmeans - Cluster rows") {

                @Override
                public void run() {

                    Cluster.gapKmeansRow(CoolMapMaster.getActiveCoolMapObject(), gapPanel.maxClusters, gapPanel.nullsAsZero, "Gap Kmeans Row (" + Tools.randomID() + ")", gapPanel.criterionFunction);

                }
            });
        }

    }

    private class StreemerRowAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("Streemer - Cluster rows") {

                @Override
                public void run() {

                    Cluster.streemerRow(CoolMapMaster.getActiveCoolMapObject(), strePanel.numClusters, strePanel.nullsAsZero, "Streemer Row (" + Tools.randomID() + ")", strePanel.backgroundClusterPerc, strePanel.similarityThreshold, (int) strePanel.minClusterSize, strePanel.similarityFunction);

                }
            });
        }

    }

    private class StreemerColumnAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("Streemer - Cluster columns") {

                @Override
                public void run() {

                    Cluster.streemerColumn(CoolMapMaster.getActiveCoolMapObject(), strePanel.numClusters, strePanel.nullsAsZero, "Streemer Column (" + Tools.randomID() + ")", strePanel.backgroundClusterPerc, strePanel.similarityThreshold, (int) strePanel.minClusterSize, strePanel.similarityFunction);

                }
            });
        }

    }

    private class GapKmeansColumnAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("Gap Kmeans - Cluster columns") {

                @Override
                public void run() {

                    Cluster.gapKmeansColumn(CoolMapMaster.getActiveCoolMapObject(), gapPanel.maxClusters, gapPanel.nullsAsZero, "Gap Kmeans Column (" + Tools.randomID() + ")", gapPanel.criterionFunction);

                }
            });
        }

    }

    private class SpecRowAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("Spectral - Cluster rows") {

                @Override
                public void run() {

                    Cluster.specCKVW03Row(CoolMapMaster.getActiveCoolMapObject(), specPanel.numClusters, specPanel.nullsAsZero, "Spectral Row (" + Tools.randomID() + ")");

                }
            });
        }
    }

    private class BisecRowAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("Bisec Kmeans - Cluster rows") {

                @Override
                public void run() {

                    Cluster.bisecKRows(CoolMapMaster.getActiveCoolMapObject(), specPanel.numClusters, specPanel.nullsAsZero, "Bisec Kmeans Row (" + Tools.randomID() + ")");

                }
            });
        }
    }

    private class SpecColumnAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("Spectral - Cluster columns") {

                @Override
                public void run() {

                    Cluster.specCKVW03Column(CoolMapMaster.getActiveCoolMapObject(), specPanel.numClusters, specPanel.nullsAsZero, "Spectral Column (" + Tools.randomID() + ")");

                }
            });
        }
    }

    private class BisecColumnAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("Bisec Kmeans - Cluster columns") {

                @Override
                public void run() {

                    Cluster.bisecKColumns(CoolMapMaster.getActiveCoolMapObject(), specPanel.numClusters, specPanel.nullsAsZero, "Bisec Kmeans Column (" + Tools.randomID() + ")");

                }
            });
        }
    }

    private class GapConfigAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), gapPanel, "Gap Kmeans Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, UI.getImageIcon("gearSmall"));
            if (returnVal == JOptionPane.OK_OPTION) {
//                System.err.println("yes!");
                gapPanel.setParameters();
            }
        }

    }

    private class StreemerConfigAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), strePanel, "DirectKMeans Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, UI.getImageIcon("gearSmall"));
            if (returnVal == JOptionPane.OK_OPTION) {
//                System.err.println("yes!");
                strePanel.setParameters();
            }
        }

    }

    private class DirectKMeansConfigAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), dKmeansPanel, "DirectKMeans Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, UI.getImageIcon("gearSmall"));
            if (returnVal == JOptionPane.OK_OPTION) {
//                System.err.println("yes!");
                dKmeansPanel.setParameters();
            }
        }

    }

    private class SpecConfigAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), specPanel, "Spectral Clustering Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, UI.getImageIcon("gearSmall"));
            if (returnVal == JOptionPane.OK_OPTION) {
//                System.err.println("yes!");
                specPanel.setParameters();
            }
        }
    }

    private class BisecConfigurationAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), bisecPanel, "Bisec Kmeans Clustering Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, UI.getImageIcon("gearSmall"));
            if (returnVal == JOptionPane.OK_OPTION) {
//                System.err.println("yes!");
                bisecPanel.setParameters();
            }
        }
    }

    private class NCAConfigAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), ncaPanel, "Neighbor Chain Agglo (NCA) Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, UI.getImageIcon("gearSmall"));
            if (returnVal == JOptionPane.OK_OPTION) {
//                System.err.println("yes!");
                ncaPanel.setParameters();
            }
        }

    }

    private class CBCConfigAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), cbcPanel, "Clustering by Committee Config", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, UI.getImageIcon("gearSmall"));
            if (returnVal == JOptionPane.OK_OPTION) {
//                System.err.println("yes!");
                cbcPanel.setParameters();
            }
        }

    }

    private class HClusterRowAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskEngine.getInstance().submitTask(new LongTask("HAC - Cluster rows") {

                @Override
                public void run() {
                    try {
//                            Thread.sleep(3000);
                        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                        if (obj == null) {
                            return;
                        }

                        CoolMapState state = CoolMapState.createStateRows("H-Cluster rows", obj, null);
                        Cluster.hClustRow(obj, hClustPanel.hClusterLinkage, hClustPanel.hClusterSimType, hClustPanel.nullsAsZero);
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
            TaskEngine.getInstance().submitTask(new LongTask("HAC - Cluster columns") {

                @Override
                public void run() {
                    try {
                        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
                        if (obj == null) {
                            return;
                        }

                        //To change body of generated methods, choose Tools | Templates.
                        CoolMapState state = CoolMapState.createStateColumns("H-Cluster columns", obj, null);
                        Cluster.hClustColumn(CoolMapMaster.getActiveCoolMapObject(), hClustPanel.hClusterLinkage, hClustPanel.hClusterSimType, hClustPanel.nullsAsZero);
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

//    private class 
    private class EnumRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
            String text = value.toString().toLowerCase().replaceAll("_", " ");
            try {
                text = text.substring(0, 1).toUpperCase() + text.substring(1);
            } catch (Exception e) {

            }
            label.setText(text);

            return label;
        }

    }

    private class ClassRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            try {
                label.setText(value.getClass().getSimpleName());
            } catch (Exception e) {

            }

            return label;
        }

    }

    private class GapPanel extends JPanel {

        public int maxClusters = 20;
        public boolean nullsAsZero = false;

        private final JComboBox criterionCombo;
        private final JTextField maxClustersField;
        private final JCheckBox nullsAsZeroCheck = new JCheckBox();
        public CriterionFunction criterionFunction = new H2Function();

        public GapPanel() {

            criterionCombo = new JComboBox(new CriterionFunction[]{new H2Function(), new H1Function(), new I1Function(), new I2Function()});
            maxClustersField = new JTextField(Integer.toString(maxClusters));
            criterionCombo.setRenderer(new ClassRenderer());
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            setLayout(new GridBagLayout());
            c.insets = new Insets(5, 5, 5, 5);

            c.gridx = 0;
            add(new JLabel("Max clusters:"), c);
            c.gridx = 1;
            add(maxClustersField, c);

            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Criteria Function"), c);
            c.gridx = 1;
            add(criterionCombo, c);

            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Missing values as 0:"), c);
            c.gridx = 1;
            add(nullsAsZeroCheck, c);
        }

        public void setParameters() {
            try {
//                System.err.println(numClustersField.getText());
                maxClusters = Integer.parseInt(maxClustersField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid cluster number: " + maxClustersField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }
            nullsAsZero = nullsAsZeroCheck.isSelected();

            try {
                criterionFunction = (CriterionFunction) (criterionCombo.getSelectedItem());
            } catch (Exception e) {
                System.err.println("Parameters error");
            }
        }

    }

    private DKmeansPanel dKmeansPanel = new DKmeansPanel();
    private GapPanel gapPanel = new GapPanel();
    private CBCPanel cbcPanel = new CBCPanel();
    private NCAPanel ncaPanel = new NCAPanel();
    private StreemerPanel strePanel = new StreemerPanel();
    private SpecPanel specPanel = new SpecPanel();
    private BisecPanel bisecPanel = new BisecPanel();

    private class BisecPanel extends JPanel {

        public int numClusters = 10;
        private final JTextField numClustersField = new JTextField("10");
        public boolean nullsAsZero = false;
        private final JCheckBox nullsAsZeroCheck = new JCheckBox();

        public BisecPanel() {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            setLayout(new GridBagLayout());
            c.insets = new Insets(5, 5, 5, 5);

            c.gridx = 0;
            add(new JLabel("Num Clusters:"), c);
            c.gridx = 1;
            add(numClustersField, c);

            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Missing values as 0:"), c);
            c.gridx = 1;
            add(nullsAsZeroCheck, c);
        }

        public void setParameters() {
            try {
                numClusters = Integer.parseInt(numClustersField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid cluster number: " + numClustersField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }
            nullsAsZero = nullsAsZeroCheck.isSelected();
        }

    }

    private class SpecPanel extends JPanel {

        public int numClusters = 10;
        private final JTextField numClustersField = new JTextField("10");
        public boolean nullsAsZero = false;
        private final JCheckBox nullsAsZeroCheck = new JCheckBox();

        public SpecPanel() {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            setLayout(new GridBagLayout());
            c.insets = new Insets(5, 5, 5, 5);

            c.gridx = 0;
            add(new JLabel("Num Clusters:"), c);
            c.gridx = 1;
            add(numClustersField, c);

            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Missing values as 0:"), c);
            c.gridx = 1;
            add(nullsAsZeroCheck, c);
        }

        public void setParameters() {
            try {
                numClusters = Integer.parseInt(numClustersField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid cluster number: " + numClustersField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }
            nullsAsZero = nullsAsZeroCheck.isSelected();
        }

    }

    private class StreemerPanel extends JPanel {

        public int numClusters = 10;
        private final JTextField numClustersField = new JTextField("10");

        public double backgroundClusterPerc = 0.25;
        private final JTextField backgroundClusterPercField = new JTextField("0.25");

        public double similarityThreshold = 0.75;
        private final JTextField similarityThresholdField = new JTextField("0.75");

        public double minClusterSize = 5;
        private final JTextField minClusterSizeField = new JTextField("5");

        public boolean nullsAsZero = false;
        private final JCheckBox nullsAsZeroCheck = new JCheckBox();

        public SimilarityFunction similarityFunction = new CosineSimilarity();
        private final JComboBox simCombo = new JComboBox(new SimilarityFunction[]{new AverageCommonFeatureRank(), new CosineSimilarity(),
            new DotProduct(),
            new EuclideanSimilarity(),
            new GaussianKernel(),
            new JaccardIndex(),
            new KendallsTau(),
            new KLDivergence(),
            new LinSimilarity(),
            new OneSimilarity(),
            new PearsonCorrelation(),
            new PolynomialKernel(),
            new SpearmanRankCorrelation(),
            new TanimotoCoefficient()
        });

        ;
        
        public StreemerPanel() {
            simCombo.setSelectedIndex(1);
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            setLayout(new GridBagLayout());
            c.insets = new Insets(5, 5, 5, 5);

            c.gridx = 0;
            add(new JLabel("Num Clusters:"), c);
            c.gridx = 1;
            add(numClustersField, c);

            c.gridy++;
            c.gridx = 0;
            add(new JLabel("Background Cluster %:"), c);
            c.gridx = 1;
            add(backgroundClusterPercField, c);

            c.gridy++;
            c.gridx = 0;
            add(new JLabel("Similarity Cutoff:"), c);
            c.gridx = 1;
            add(similarityThresholdField, c);

            c.gridy++;
            c.gridx = 0;
            add(new JLabel("Min Cluster Size:"), c);
            c.gridx = 1;
            add(minClusterSizeField, c);

            c.gridy++;
            c.gridx = 0;
            add(new JLabel("Similarity Function:"), c);
            c.gridx = 1;
            add(simCombo, c);

            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Missing values as 0:"), c);
            c.gridx = 1;
            add(nullsAsZeroCheck, c);

            simCombo.setRenderer(new ClassRenderer());
        }

        public void setParameters() {

            try {
                numClusters = Integer.parseInt(numClustersField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid cluster number: " + numClustersField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }

            try {
                backgroundClusterPerc = Double.parseDouble(backgroundClusterPercField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid number: " + backgroundClusterPercField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }

            try {
                similarityThreshold = Double.parseDouble(similarityThresholdField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid number: " + similarityThresholdField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }

            try {
                minClusterSize = Double.parseDouble(minClusterSizeField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid number: " + minClusterSizeField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }

            similarityFunction = (SimilarityFunction) simCombo.getSelectedItem();
            nullsAsZero = nullsAsZeroCheck.isSelected();

        }

    }

    private class NCAPanel extends JPanel {

        public NeighborChainAgglomerativeClustering.ClusterLink link = NeighborChainAgglomerativeClustering.ClusterLink.MEAN_LINK;
        public SimilarityFunction similarityFunction = new CosineSimilarity();
        public boolean nullsAsZero = false;
        public int numClusters = 10;

        private final JComboBox linkCombo;
        private final JComboBox simCombo;
        private final JCheckBox nullsAsZeroCheck = new JCheckBox();
        private final JTextField numClustersField;

        public NCAPanel() {
            numClustersField = new JTextField(Integer.toString(numClusters));
            linkCombo = new JComboBox(NeighborChainAgglomerativeClustering.ClusterLink.values());
            simCombo = new JComboBox(new SimilarityFunction[]{new AverageCommonFeatureRank(), new CosineSimilarity(),
                new DotProduct(),
                new EuclideanSimilarity(),
                new GaussianKernel(),
                new JaccardIndex(),
                new KendallsTau(),
                new KLDivergence(),
                new LinSimilarity(),
                new OneSimilarity(),
                new PearsonCorrelation(),
                new PolynomialKernel(),
                new SpearmanRankCorrelation(),
                new TanimotoCoefficient()
            });

            linkCombo.setSelectedItem(NeighborChainAgglomerativeClustering.ClusterLink.MEAN_LINK);
            simCombo.setSelectedIndex(1);

            linkCombo.setRenderer(new EnumRenderer());
            simCombo.setRenderer(new ClassRenderer());

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            setLayout(new GridBagLayout());
            c.insets = new Insets(5, 5, 5, 5);

            c.gridx = 0;
            add(new JLabel("Num Clusters:"), c);
            c.gridx = 1;
            add(numClustersField, c);

            c.gridy++;
            c.gridx = 0;
            add(new JLabel("Link Method:"), c);
            c.gridx = 1;
            add(linkCombo, c);

            c.gridy++;
            c.gridx = 0;
            add(new JLabel("Similarity Function:"), c);
            c.gridx = 1;
            add(simCombo, c);

            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Missing values as 0:"), c);
            c.gridx = 1;
            add(nullsAsZeroCheck, c);

        }

        public void setParameters() {
            link = (NeighborChainAgglomerativeClustering.ClusterLink) linkCombo.getSelectedItem();
            similarityFunction = (SimilarityFunction) simCombo.getSelectedItem();
            nullsAsZero = nullsAsZeroCheck.isSelected();
            try {
                numClusters = Integer.parseInt(numClustersField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid cluster number: " + numClustersField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }

        }

    }

    private class CBCPanel extends JPanel {

        public double avgLinkMergeThresh = 0.25;
        public double maxCommitteeSimThresh = 0.35;
        public double residueSimThresh = 0.25;
        public double softClusteringThresh = 0.25;
        public boolean useHardClustering = true;
        public boolean nullsAsZero = false;

        private final JTextField avgLinkMergeThreshField = new JTextField("0.25");
        private final JTextField maxCommitteeSimThreshField = new JTextField("0.35");
        private final JTextField residueSimThreshField = new JTextField("0.25");
        private final JTextField softClusteringThreshField = new JTextField("0.25");
        private final JCheckBox useHardClusteringCheck = new JCheckBox();
        private final JCheckBox nullsAsZeroCheck = new JCheckBox();

        public CBCPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            setLayout(new GridBagLayout());
            c.insets = new Insets(5, 5, 5, 5);

            c.gridx = 0;
            add(new JLabel("Average Link:"), c);
            c.gridx = 1;
            add(avgLinkMergeThreshField, c);

            c.gridy++;
            c.gridx = 0;
            add(new JLabel("Max Committee Similarity:"), c);
            c.gridx = 1;
            add(maxCommitteeSimThreshField, c);

            c.gridy++;
            c.gridx = 0;
            add(new JLabel("Residue Similarity:"), c);
            c.gridx = 1;
            add(residueSimThreshField, c);

            c.gridy++;
            c.gridx = 0;
            add(new JLabel("Soft Clustering Cutoff:"), c);
            c.gridx = 1;
            add(softClusteringThreshField, c);

            c.gridy++;
            c.gridx = 0;
            add(new JLabel("Use Hard Clustering:"), c);
            c.gridx = 1;
            add(useHardClusteringCheck, c);

            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Missing values as 0:"), c);
            c.gridx = 1;
            add(nullsAsZeroCheck, c);

            useHardClusteringCheck.setSelected(true);

        }

        public void setParameters() {
            try {
                avgLinkMergeThresh = Double.parseDouble(avgLinkMergeThreshField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid parameter: " + avgLinkMergeThreshField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }

            try {
                maxCommitteeSimThresh = Double.parseDouble(maxCommitteeSimThreshField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid parameter: " + maxCommitteeSimThreshField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }

            try {
                residueSimThresh = Double.parseDouble(residueSimThreshField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid parameter: " + residueSimThreshField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }

            try {
                softClusteringThresh = Double.parseDouble(softClusteringThreshField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid parameter: " + softClusteringThreshField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }

            useHardClusteringCheck.setSelected(true);

            useHardClustering = useHardClusteringCheck.isSelected();
            nullsAsZero = nullsAsZeroCheck.isSelected();
        }

    }

    private class DKmeansPanel extends JPanel {

        public int numClusters = 5;
        public int numIterations = 1;
        public CriterionFunction criterionFunction = new I1Function();
        public KMeansSeed seedType = new RandomSeed();
        public boolean nullsAsZero = false;

        private final JComboBox criterionCombo;
        private final JComboBox seedCombo;
        private final JTextField numClustersField;
        private final JTextField numIterationsField;
        private final JCheckBox nullsAsZeroCheck = new JCheckBox();

        public DKmeansPanel() {
            numClustersField = new JTextField(Integer.toString(numClusters));
            numIterationsField = new JTextField(Integer.toString(numIterations));
            criterionCombo = new JComboBox(new CriterionFunction[]{new I1Function(), new I2Function(), new H1Function(), new H2Function()});
            seedCombo = new JComboBox(new KMeansSeed[]{new RandomSeed(), new KMeansPlusPlusSeed(), new OrssSeed(), new GeneralizedOrssSeed(new PearsonCorrelation())});

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            setLayout(new GridBagLayout());
            c.insets = new Insets(5, 5, 5, 5);

            //row one
            c.gridx = 0;
            add(new JLabel("Num clusters:"), c);
            c.gridx = 1;
            add(numClustersField, c);

            //row two
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Num Iterations:"), c);
            c.gridx = 1;
            add(numIterationsField, c);

            //row three
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Criteria Function"), c);
            c.gridx = 1;
            add(criterionCombo, c);

            //row four
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Seed Function:"), c);
            c.gridx = 1;
            add(seedCombo, c);

            //row five
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Missing values as 0:"), c);
            c.gridx = 1;
            add(nullsAsZeroCheck, c);

            criterionCombo.setRenderer(new ClassRenderer());
            seedCombo.setRenderer(new ClassRenderer());

        }

        public void setParameters() {
            try {
//                System.err.println(numClustersField.getText());
                numClusters = Integer.parseInt(numClustersField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid cluster number: " + numClustersField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }

            try {
                numIterations = Integer.parseInt(numIterationsField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Invalid iteration number: " + numIterationsField.getText(), "Parameter Error", JOptionPane.ERROR_MESSAGE);
            }

            try {
                criterionFunction = (CriterionFunction) (criterionCombo.getSelectedItem());
                seedType = (KMeansSeed) (seedCombo.getSelectedItem());
            } catch (Exception e) {
                System.err.println("Parameters error");
            }

            nullsAsZero = nullsAsZeroCheck.isSelected();

//            System.out.println("Number of clusters is set to:" + numClusters);
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
        public boolean nullsAsZero = false;

        private final JComboBox linkageCombo;
        private final JComboBox similarityCombo;
        private final JCheckBox nullsAsZeroCheck = new JCheckBox();

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
            c.gridy++;
            add(new JLabel("Similarity metric:"), c);
            c.gridx = 1;
            add(similarityCombo, c);

            //row three
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Missing values as 0:"), c);
            c.gridx = 1;
            add(nullsAsZeroCheck, c);

            linkageCombo.setSelectedItem(HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE);
            similarityCombo.setSelectedItem(Similarity.SimType.PEARSON_CORRELATION);

            linkageCombo.setRenderer(new EnumRenderer());
            similarityCombo.setRenderer(new EnumRenderer());

        }

        public void setParameter() {
            hClusterLinkage = (HierarchicalAgglomerativeClustering.ClusterLinkage) linkageCombo.getSelectedItem();
            hClusterSimType = (Similarity.SimType) similarityCombo.getSelectedItem();
            nullsAsZero = nullsAsZeroCheck.isSelected();
        }

    }

    private void initHClust() {

        //bisec kmeans
        addClusterMenuItem("Cluster Row (BK)", "Cluster/Bisec Kmeans", new BisecRowAction());
        addClusterMenuItem("Cluster Column (BK)", "Cluster/Bisec Kmeans", new BisecColumnAction());
        addClusterMenuItem("Config...", "Cluster/Bisec Kmeans", new BisecConfigurationAction());

        //CBC
        addClusterMenuItem("Cluster Row (CBC)", "Cluster/Clustering by Committee", new CBCRowsAction());
        addClusterMenuItem("Cluster Column (CBC)", "Cluster/Clustering by Committee", new CBCColumnsAction());
        addClusterMenuItem("Config...", "Cluster/Clustering by Committee", new CBCConfigAction());

        //Direct Kmeans
        addClusterMenuItem("Cluster Row (DKmeans)", "Cluster/Direct Kmeans", new DirectKmeansRowAction());
        addClusterMenuItem("Cluster Column (DKmeans)", "Cluster/Direct Kmeans", new DirectKmeansColumnAction());
        addClusterMenuItem("Config...", "Cluster/Direct Kmeans", new DirectKMeansConfigAction());

        //Gap Kmeans
        addClusterMenuItem("Cluster Row (Gap Kmeans)", "Cluster/Gap Kmeans", new GapKmeansRowAction());
        addClusterMenuItem("Cluster Column (Gap Kmeans)", "Cluster/Gap Kmeans", new GapKmeansColumnAction());
        addClusterMenuItem("Config...", "Cluster/Gap Kmeans", new GapConfigAction());

        //HAC
        addClusterMenuItem("Cluster Row (HAC)", "Cluster/Hierarchical", new HClusterRowAction());
        addClusterMenuItem("Cluster Column (HAC)", "Cluster/Hierarchical", new HClusterColumnAction());
        addClusterMenuItem("Config...", "Cluster/Hierarchical", new HClusterConfigAction());

        //NCA
        addClusterMenuItem("Cluster Row (NCA)", "Cluster/Neighbor Chain", new NCARowsAction());
        addClusterMenuItem("Cluster Column (NCA)", "Cluster/Neighbor Chain", new NCAColumnsAction());
        addClusterMenuItem("Config...", "Cluster/Neighbor Chain", new NCAConfigAction());

        //spectral
        addClusterMenuItem("Cluster Row (Spectral)", "Cluster/Spectral", new SpecRowAction());
        addClusterMenuItem("Cluster Column (Spectral)", "Cluster/Spectral", new SpecColumnAction());
        addClusterMenuItem("Config...", "Cluster/Spectral", new SpecConfigAction());

        //Streemer
        addClusterMenuItem("Cluster Row (Streemer)", "Cluster/Streemer", new StreemerRowAction());
        addClusterMenuItem("Cluster Column (Streemer)", "Cluster/Streemer", new StreemerColumnAction());
        addClusterMenuItem("Config...", "Cluster/Streemer", new StreemerConfigAction());

    }

    private void addClusterMenuItem(String label, String path, ActionListener actionListener) {
        MenuItem item = new MenuItem(label);
        CoolMapMaster.getCMainFrame().addMenuItem(path, item, false, false);
        item.addActionListener(actionListener);
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
