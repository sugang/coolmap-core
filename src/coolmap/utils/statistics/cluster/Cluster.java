/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.statistics.cluster;

import com.google.common.collect.HashMultimap;
import coolmap.application.CoolMapMaster;
import coolmap.application.state.StateStorageMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.contology.utils.edgeattributes.COntologyEdgeAttributeImpl;
import coolmap.data.state.CoolMapState;
import coolmap.utils.Tools;
import coolmap.utils.graphics.UI;
import edu.ucla.sspace.clustering.Assignments;
import edu.ucla.sspace.clustering.BisectingKMeans;
import edu.ucla.sspace.clustering.CKVWSpectralClustering03;
import edu.ucla.sspace.clustering.ClusteringByCommittee;
import edu.ucla.sspace.clustering.DirectClustering;
import edu.ucla.sspace.clustering.GapStatistic;
import edu.ucla.sspace.clustering.HierarchicalAgglomerativeClustering;
import edu.ucla.sspace.clustering.Merge;
import edu.ucla.sspace.clustering.NeighborChainAgglomerativeClustering;
import edu.ucla.sspace.clustering.Streemer;
import edu.ucla.sspace.clustering.criterion.CriterionFunction;
import edu.ucla.sspace.clustering.seeding.KMeansSeed;
import edu.ucla.sspace.common.Similarity;
import edu.ucla.sspace.matrix.ArrayMatrix;
import edu.ucla.sspace.matrix.SparseHashMatrix;
import edu.ucla.sspace.similarity.SimilarityFunction;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author gangsu This will be another library outside of core.
 */
public class Cluster {

    private static DecimalFormat df = new DecimalFormat("000");

    public static void bisecKRows(CoolMapObject<?, Double> object, int numClusters, boolean nullsAsZero, String resultName) {
        try {
            ArrayMatrix matrix = convertToMatrixForRows(object, nullsAsZero);

            BisectingKMeans k = new BisectingKMeans();

            Assignments assignments = k.cluster(matrix, numClusters, new Properties());

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            clusterRow(object, assignments, resultName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void bisecKColumns(CoolMapObject<?, Double> object, int numClusters, boolean nullsAsZero, String resultName) {
        try {
            ArrayMatrix matrix = convertToMatrixForColumns(object, nullsAsZero);

            BisectingKMeans k = new BisectingKMeans();

            Assignments assignments = k.cluster(matrix, numClusters, new Properties());

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            clusterColumn(object, assignments, resultName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void specCKVW03Row(CoolMapObject<?, Double> object, int numClusters, boolean nullsAsZero, String resultName) {
        try {
            ArrayMatrix matrix = convertToMatrixForRows(object, nullsAsZero);

            CKVWSpectralClustering03 c = new CKVWSpectralClustering03();

            Assignments assignments = c.cluster(matrix, numClusters, new Properties());

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            clusterRow(object, assignments, resultName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void specCKVW03Column(CoolMapObject<?, Double> object, int numClusters, boolean nullsAsZero, String resultName) {
        try {
            ArrayMatrix matrix = convertToMatrixForColumns(object, nullsAsZero);
            CKVWSpectralClustering03 c = new CKVWSpectralClustering03();
            Assignments assignments = c.cluster(matrix, numClusters, new Properties());
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            clusterColumn(object, assignments, resultName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void streemerRow(CoolMapObject<?, Double> object, int numClusters, boolean nullsAsZero, String resultName, double backgroundClusterPerc, double similarityThreshold, int minClusterSize, SimilarityFunction simFunction) {
        try {
            ArrayMatrix matrix = convertToMatrixForRows(object, nullsAsZero);

            Streemer s = new Streemer();

            Assignments assignments = s.cluster(matrix, numClusters, backgroundClusterPerc, similarityThreshold, minClusterSize, simFunction);

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            clusterRow(object, assignments, resultName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void streemerColumn(CoolMapObject<?, Double> object, int numClusters, boolean nullsAsZero, String resultName, double backgroundClusterPerc, double similarityThreshold, int minClusterSize, SimilarityFunction simFunction) {
        try {
            ArrayMatrix matrix = convertToMatrixForColumns(object, nullsAsZero);

            Streemer s = new Streemer();

            Assignments assignments = s.cluster(matrix, numClusters, backgroundClusterPerc, similarityThreshold, minClusterSize, simFunction);

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            clusterColumn(object, assignments, resultName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void ncaRow(CoolMapObject<?, Double> object, int numClusters, boolean nullsAsZero, String resultName, SimilarityFunction similarityFunction, NeighborChainAgglomerativeClustering.ClusterLink method) {
        try {
            ArrayMatrix matrix = convertToMatrixForRows(object, nullsAsZero);

            NeighborChainAgglomerativeClustering nca = new NeighborChainAgglomerativeClustering(method, similarityFunction);

            Assignments assignments = nca.cluster(matrix, numClusters, null);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            clusterRow(object, assignments, resultName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ncaColumns(CoolMapObject<?, Double> object, int numClusters, boolean nullsAsZero, String resultName, SimilarityFunction similarityFunction, NeighborChainAgglomerativeClustering.ClusterLink method) {
        try {
            ArrayMatrix matrix = convertToMatrixForColumns(object, nullsAsZero);

            NeighborChainAgglomerativeClustering nca = new NeighborChainAgglomerativeClustering(method, similarityFunction);

            Assignments assignments = nca.cluster(matrix, numClusters, null);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            clusterColumn(object, assignments, resultName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cbcRow(CoolMapObject<?, Double> object, boolean nullsAsZero, String resultName, Properties properties) {
        //this method allows setting similarity function
        try {
            ArrayMatrix matrix = convertToMatrixForRows(object, nullsAsZero);

            SparseHashMatrix sMatrix = new SparseHashMatrix(matrix.rows(), matrix.columns());

            for (int i = 0; i < matrix.rows(); i++) {
                for (int j = 0; j < matrix.columns(); j++) {
                    sMatrix.set(i, j, matrix.get(i, j));
                }
            }

            ClusteringByCommittee cbc = new ClusteringByCommittee();
            Assignments assignments = cbc.cluster(sMatrix, properties);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            clusterRow(object, assignments, resultName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cbcColumn(CoolMapObject<?, Double> object, boolean nullsAsZero, String resultName, Properties properties) {
        //this method allows setting similarity function
        try {
            ArrayMatrix matrix = convertToMatrixForColumns(object, nullsAsZero);
            SparseHashMatrix sMatrix = new SparseHashMatrix(matrix.rows(), matrix.columns());

            for (int i = 0; i < matrix.rows(); i++) {
                for (int j = 0; j < matrix.columns(); j++) {
                    sMatrix.set(i, j, matrix.get(i, j));
                }
            }

            ClusteringByCommittee cbc = new ClusteringByCommittee();
            Assignments assignments = cbc.cluster(sMatrix, properties);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            clusterColumn(object, assignments, resultName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gapKmeansRow(CoolMapObject<?, Double> object, int maxNumClusters, boolean nullsAsZero, String resultName, CriterionFunction criterionFunction) {
        try {

            ArrayMatrix matrix = convertToMatrixForRows(object, nullsAsZero);
            GapStatistic gap = new GapStatistic();

//            System.err.println(matrix + " " + maxNumClusters);
            Properties prop = new Properties();
            prop.put(GapStatistic.METHOD_PROPERTY, criterionFunction.getClass().getName());

            Assignments assignments = gap.cluster(matrix, maxNumClusters, prop);

            if (Thread.interrupted()) {
                //If clustering canceled
                throw new InterruptedException();
            }
            clusterRow(object, assignments, resultName);

        } catch (InterruptedException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static void gapKmeansColumn(CoolMapObject<?, Double> object, int maxNumClusters, boolean nullsAsZero, String resultName, CriterionFunction criterionFunction) {
        try {
            ArrayMatrix matrix = convertToMatrixForColumns(object, nullsAsZero);
            GapStatistic gap = new GapStatistic();

            Properties prop = new Properties();
            prop.put(GapStatistic.METHOD_PROPERTY, criterionFunction.getClass().getName());

            Assignments assignments = gap.cluster(matrix, maxNumClusters, prop);

            if (Thread.interrupted()) {
                //If clustering canceled
                throw new InterruptedException();
            }
            clusterColumn(object, assignments, resultName);

        } catch (InterruptedException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * params
     *
     * @param object
     * @param numClusters
     * @param nullsAsZero
     * @param resultName
     * @param cFunction
     * @param seedType
     * @param numRepeats
     */
    public static void directKmeansRow(CoolMapObject<?, Double> object, int numClusters, boolean nullsAsZero, String resultName, CriterionFunction cFunction, KMeansSeed seedType, int numRepeats) {

        try {
            ArrayMatrix matrix = convertToMatrixForRows(object, nullsAsZero);

            Assignments assignments = DirectClustering.cluster(matrix, numClusters, numRepeats, seedType, cFunction);

            if (Thread.interrupted()) {
                //If clustering canceled
                throw new InterruptedException();
            }

            clusterRow(object, assignments, resultName);

        } catch (InterruptedException | RuntimeException e) {
        }

    }

    public static void directKmeansColumn(CoolMapObject<?, Double> object, int numClusters, boolean nullsAsZero, String resultName, CriterionFunction cFunction, KMeansSeed seedType, int numRepeats) {
        try {
            ArrayMatrix matrix = convertToMatrixForColumns(object, nullsAsZero);

            Assignments assignments = DirectClustering.cluster(matrix, numClusters, numRepeats, seedType, cFunction);

            if (Thread.interrupted()) {
                //If clustering canceled
                throw new InterruptedException();
            }

            clusterColumn(object, assignments, resultName);

        } catch (InterruptedException | RuntimeException e) {
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private static void clusterRow(CoolMapObject<?, Double> object, Assignments assignments, String resultOntologyName) {
        List<Set<Integer>> clusters = assignments.clusters();
        if (resultOntologyName == null || resultOntologyName.length() == 0) {
            resultOntologyName = "Untitled";
        }

        String ID = Tools.randomID();
        COntology ontology = new COntology(resultOntologyName, null);
        int counter = 0;
        for (Set<Integer> cluster : clusters) {
            String groupName = df.format(counter) + " [" + ID + "]";
            counter++;
            for (Integer i : cluster) {
                ontology.addRelationshipNoUpdateDepth(groupName, object.getViewNodeRow(i).getName());
            }
        }

        //create row nodes to make it work
        HashMultimap<String, String> map = HashMultimap.create();
        for (VNode node : object.getViewNodesRow()) {
            if (node.getCOntologyID() != null) {
                map.put(node.getCOntologyID(), node.getName());
            }
        }
//
//            it should be merged, but why not merged?
        for (String otherOntologyID : map.keySet()) {
            Set<String> termsToMerge = map.get(otherOntologyID);
//            System.out.println("Terms to merge:" + termsToMerge);
            COntology otherOntology = CoolMapMaster.getCOntologyByID(otherOntologyID);
            otherOntology.mergeCOntologyTo(ontology, termsToMerge); //merge over the previous terms
        }

        if (Thread.interrupted()) {
            return;
        }
        CoolMapMaster.addNewCOntology(ontology);

        CoolMapState state = CoolMapState.createStateRows("Cluster rows", object, null);

        //insert root nodes
        List<VNode> rootNodes = ontology.getRootNodesOrdered();
        object.replaceRowNodes(rootNodes, null);

        for (VNode node : rootNodes) {
            node.colorTree(UI.randomColor());
        }

        StateStorageMaster.addState(state);
    }

    private static void clusterColumn(CoolMapObject<?, Double> object, Assignments assignments, String resultOntologyName) {
        List<Set<Integer>> clusters = assignments.clusters();
        if (resultOntologyName == null || resultOntologyName.length() == 0) {
            resultOntologyName = "Untitled";
        }

        String ID = Tools.randomID();
        COntology ontology = new COntology(resultOntologyName, null);

        int counter = 0;
        for (Set<Integer> cluster : clusters) {
            String groupName = df.format(counter) + " [" + ID + "]";
            counter++;
            for (Integer i : cluster) {
                ontology.addRelationshipNoUpdateDepth(groupName, object.getViewNodeColumn(i).getName());
            }
        }

//        HashMultimap<COntology, String> map = HashMultimap.create();
//        for (VNode node : object.getViewNodesColumn()) {
//            if (node.getCOntology() != null) {
//                map.put(node.getCOntology(), node.getName());
//            }
//        }
//
//        for (COntology otherOntology : map.keySet()) {
//            Set<String> termsToMerge = map.get(otherOntology);
////            System.out.println("Terms to merge:" + termsToMerge);
//            otherOntology.mergeCOntologyTo(ontology, termsToMerge); //merge over the previous terms
//        }
        //create row nodes to make it work
        HashMultimap<String, String> map = HashMultimap.create();
        for (VNode node : object.getViewNodesColumn()) {
            if (node.getCOntologyID() != null) {
                map.put(node.getCOntologyID(), node.getName());
            }
        }
//
//            it should be merged, but why not merged?
        for (String otherOntologyID : map.keySet()) {
            Set<String> termsToMerge = map.get(otherOntologyID);
//            System.out.println("Terms to merge:" + termsToMerge);
            COntology otherOntology = CoolMapMaster.getCOntologyByID(otherOntologyID);
            otherOntology.mergeCOntologyTo(ontology, termsToMerge); //merge over the previous terms
        }

        if (Thread.interrupted()) {
            return;
        }
        CoolMapMaster.addNewCOntology(ontology);

        CoolMapState state = CoolMapState.createStateColumns("Cluster columns", object, null);
        List<VNode> rootNodes = ontology.getRootNodesOrdered();
        object.replaceColumnNodes(rootNodes, null);

        for (VNode node : rootNodes) {
            node.colorTree(UI.randomColor());
        }

        StateStorageMaster.addState(state);

    }

//    private static void clusterRow(CoolMapObject<?, Double> object, Clustering clusterAlgorithm, int numClusters, Properties properties, boolean nullsAsZero, String resultName) throws CoolMapObjectInputException, CoolMapViewMissingValueException, InterruptedException {
//        if (object == null || !object.isViewMatrixValid() || object.getViewClass() == null || !Double.class.isAssignableFrom(object.getViewClass()) || clusterAlgorithm == null || numClusters <= 0 || properties == null) {
//            //Some messages here
//            throw new CoolMapObjectInputException();
//        } else {
//
//            ArrayMatrix matrix = convertToMatrixForRows(object, nullsAsZero);
//            if (matrix == null) {
//                throw new CoolMapViewMissingValueException();
//            }
//            if (Thread.interrupted()) {
//                return;
//            }
//
//            List<Set<Integer>> clusters = assignments.clusters();
//            if (resultName == null || resultName.length() == 0) {
//                resultName = "Untitled " + clusterAlgorithm;
//            }
//
//            String ID = Tools.randomID();
//            COntology ontology = new COntology(resultName, null);
//            int counter = 0;
//            for (Set<Integer> cluster : clusters) {
//                String groupName = counter + " [" + ID + "]";
//                counter++;
//                for (Integer i : cluster) {
//                    ontology.addRelationshipNoUpdateDepth(groupName, object.getViewNodeRow(i).getName());
//                }
//            }
//
//            //create row nodes to make it work
//            HashMultimap<COntology, String> map = HashMultimap.create();
//            for (VNode node : object.getViewNodesRowFromTreeNodes()) {
//                if (node.getCOntology() != null) {
//                    map.put(node.getCOntology(), node.getName());
//                }
//            }
////
////            it should be merged, but why not merged?
//            for (COntology otherOntology : map.keySet()) {
//                Set<String> termsToMerge = map.get(otherOntology);
//                System.out.println("Terms to merge:" + termsToMerge);
//                otherOntology.mergeCOntologyTo(ontology, termsToMerge); //merge over the previous terms
//            }
//
//            if (Thread.interrupted()) {
//                return;
//            }
//            CoolMapMaster.addNewCOntology(ontology);
//
//            CoolMapState state = CoolMapState.createStateRows("Cluster using " + clusterAlgorithm, object, null);
//
//            //insert root nodes
//            List<VNode> rootNodes = ontology.getRootNodesOrdered();
//            object.replaceRowNodes(rootNodes, null);
//
//            for (VNode node : rootNodes) {
//                node.colorTree(UI.randomColor());
//            }
//
//            StateStorageMaster.addState(state);
//
//            //merge with previous ontologies
//            //Need more polish here, need to save state
//            //This needs to be updated
//            //object.expandRowNodes(rootNodes);
//        }
//    }
    private synchronized static ArrayMatrix convertToMatrixForRows(CoolMapObject object, boolean nullsAsZero) {
        if (object == null || !object.isViewMatrixValid() || object.getViewClass() == null || !Double.class.isAssignableFrom(object.getViewClass())) {
            return null;
        } else {
            ArrayMatrix matrix = new ArrayMatrix(object.getViewNumRows(), object.getViewNumColumns());
            for (int i = 0; i < object.getViewNumRows(); i++) {
                for (int j = 0; j < object.getViewNumColumns(); j++) {
                    Double data = (Double) object.getViewValue(i, j);
                    if (data == null || data.isInfinite() || data.isNaN()) {
                        if (nullsAsZero) {
                            matrix.set(i, j, 0);
                        } else {
                            matrix.set(i, j, Double.NaN);
                        }

                    } else {
                        matrix.set(i, j, data);
                    }
                }
            }//end of 

            return matrix;
        }
    }

    private synchronized static ArrayMatrix convertToMatrixForColumns(CoolMapObject object, boolean nullsAsZero) {
        if (object == null || !object.isViewMatrixValid() || object.getViewClass() == null || !Double.class.isAssignableFrom(object.getViewClass())) {
            return null;
        } else {
            ArrayMatrix matrix = new ArrayMatrix(object.getViewNumColumns(), object.getViewNumRows());
            for (int i = 0; i < object.getViewNumRows(); i++) {
                for (int j = 0; j < object.getViewNumColumns(); j++) {
                    Double data = (Double) object.getViewValue(i, j);
                    if (data == null || data.isInfinite() || data.isNaN()) {
                        if (nullsAsZero) {
                            matrix.set(j, i, 0);
                        } else {
                            matrix.set(j, i, Double.NaN);
                        }

                    } else {
                        matrix.set(j, i, data);
                    }
                }
            }

            return matrix;
        }
    }

    public synchronized static void hClustRow(CoolMapObject<?, Double> object, HierarchicalAgglomerativeClustering.ClusterLinkage linkage, Similarity.SimType simType, boolean nullsAsZero) {

        if (object == null || !object.isViewMatrixValid() || object.getViewClass() == null || !Double.class.isAssignableFrom(object.getViewClass())) {
            return;
        }

//        System.out.println("Clustering started:" + linkage + " " + simType);
        ArrayMatrix matrix = new ArrayMatrix(object.getViewNumRows(), object.getViewNumColumns());
        for (int i = 0; i < object.getViewNumRows(); i++) {
            for (int j = 0; j < object.getViewNumColumns(); j++) {
                Double data = object.getViewValue(i, j);
                if (data == null || data.isInfinite() || data.isNaN()) {
                    if (nullsAsZero) {
                        matrix.set(i, j, 0);
                    } else {
                        matrix.set(i, j, Double.NaN);
                    }

                } else {
                    matrix.set(i, j, data);
                }
            }
        }

        HierarchicalAgglomerativeClustering hclust = new HierarchicalAgglomerativeClustering();

        String ID = Tools.randomID();
        //can not contain null
        //System.out.println(matrix);

        List<Merge> merges = hclust.buildDendogram(matrix, linkage, simType);

        //need to normalize the height
        if (merges.isEmpty()) {
            return;
        }

        Merge firstMerge = merges.get(0);
        double minSimilarity = firstMerge.similarity();
        double maxSimilarity = firstMerge.similarity();

        for (Merge merge : merges) {
            if (merge.similarity() < minSimilarity) {
                minSimilarity = merge.similarity();
            }
            if (merge.similarity() > maxSimilarity) {
                maxSimilarity = merge.similarity();
            }
            //System.out.println(merge + "||" + merge.mergedCluster() + "||" + merge.remainingCluster());
        }

        //System.out.println("Min sim:" + minSimilarity + " " + "Max sim" + maxSimilarity);
//        double range = maxSimilarity - minSimilarity;
        //Build a map
        HashMap<Integer, Object> trackMapping = new HashMap<Integer, Object>();
        for (Integer i = 0; i < merges.size() + 1; i++) {
            trackMapping.put(i, i);
        }

        HashMultimap<String, Object> parentChildMapping = HashMultimap.create();
        HashMap<String, Double> similarityMap = new HashMap<String, Double>();
        String iNodeName = null;
        Merge merge;
        for (Integer i = 0; i < merges.size(); i++) {
            merge = merges.get(i);
            iNodeName = merge.mergedCluster() + "->" + merge.remainingCluster() + ":" + ID;
            parentChildMapping.put(iNodeName, trackMapping.get(merge.mergedCluster()));
            parentChildMapping.put(iNodeName, trackMapping.get(merge.remainingCluster()));
            trackMapping.put(new Integer(merge.remainingCluster()), iNodeName);
            similarityMap.put(iNodeName, merge.similarity());
            //System.out.println(merge.similarity());
        }

        //now you have a parent child mappin
        COntology ontology = new COntology("Hclust:" + ID, "Result from h-clust row", ID);
        ArrayList<String[]> pairs = new ArrayList<String[]>();

        String childNode, parentNode;
        for (String parent : parentChildMapping.keySet()) {
            Set children = parentChildMapping.get(parent);
            parentNode = parent;
            for (Object child : children) {
                if (child instanceof Integer) {
                    childNode = object.getViewNodeRow((Integer) child).getName();
                } else {
                    childNode = child.toString();
                }
                pairs.add(new String[]{parentNode, childNode});
                ontology.addEdgeAttribute(parentNode, childNode, new COntologyEdgeAttributeImpl(similarityMap.get(parent).floatValue()));
                //System.out.println(parentNode + " " + childNode + " " + similarityMap.get(parent).floatValue());
            }
        }

        ontology.addRelationshipUpdateDepth(pairs);

        //COntologyUtils.printOntology(ontology);
        //List<String> roots = ontology.getRootNamesOrdered();
        //also copy the originals?
        if (Thread.interrupted()) {
//            System.out.println("Clustering Aborted");
            return;
        }

//        System.out.println("Clustering Ended Successfully");
        CoolMapMaster.addNewCOntology(ontology);

        //HashSet<COntology> previousOntologies = new HashSet<COntology>();
//        HashMultimap<COntology, String> map = HashMultimap.create();

//        for (VNode node : object.getViewNodesRow()) {
//            if (node.getCOntology() != null) {
//                map.put(node.getCOntology(), node.getName());
//            }
//        }
//
//        for (COntology otherOntology : map.keySet()) {
//            Set<String> termsToMerge = map.get(otherOntology);
////            System.out.println("Terms to merge:" + termsToMerge);
//            otherOntology.mergeCOntologyTo(ontology, termsToMerge); //merge over the previous terms
//        }
                //create row nodes to make it work
        HashMultimap<String, String> map = HashMultimap.create();
        for (VNode node : object.getViewNodesRow()) {
            if (node.getCOntologyID() != null) {
                map.put(node.getCOntologyID(), node.getName());
            }
        }
//
//            it should be merged, but why not merged?
        for (String otherOntologyID : map.keySet()) {
            Set<String> termsToMerge = map.get(otherOntologyID);
//            System.out.println("Terms to merge:" + termsToMerge);
            COntology otherOntology = CoolMapMaster.getCOntologyByID(otherOntologyID);
            otherOntology.mergeCOntologyTo(ontology, termsToMerge); //merge over the previous terms
        }

//        System.out.println("+++ check point +++");
//        System.out.println(ontology.getRootNamesOrdered());
//        Why this step go it dead completely?
//        System.out.println("attempt to replace nodes:");
        object.replaceRowNodes(ontology.getRootNodesOrdered(), null);
        //It can not be expanded for weird reason
//         System.out.println("attempt to expand nodes:");

        //This is when problem occurs right here!
        object.expandRowNodeToBottom(object.getViewNodeRow(0));

//        System.out.println("+++++++++++++++++++++++Ended Successfully\n\n");
    }

    public synchronized static void hClustColumn(CoolMapObject<?, Double> object, HierarchicalAgglomerativeClustering.ClusterLinkage linkage, Similarity.SimType simType, boolean nullsAsZero) {

        if (object == null || !object.isViewMatrixValid() || object.getViewClass() == null || !Double.class.isAssignableFrom(object.getViewClass())) {
            return;
        }

//        System.out.println("Clustering started:" + linkage + " " + simType);
        ArrayMatrix matrix = new ArrayMatrix(object.getViewNumColumns(), object.getViewNumRows());
        for (int i = 0; i < object.getViewNumRows(); i++) {
            for (int j = 0; j < object.getViewNumColumns(); j++) {
                Double data = object.getViewValue(i, j);
                if (data == null || data.isInfinite() || data.isNaN()) {
                    if (nullsAsZero) {
                        matrix.set(j, i, 0);
                    } else {
                        matrix.set(j, i, Double.NaN);
                    }

                } else {
                    matrix.set(j, i, data);
                }
            }
        }

        HierarchicalAgglomerativeClustering hclust = new HierarchicalAgglomerativeClustering();

        //can not contain null
        //System.out.println(matrix);
        String ID = Tools.randomID();
        List<Merge> merges = hclust.buildDendogram(matrix, linkage, simType);

        //need to normalize the height
        if (merges.isEmpty()) {
            return;
        }

        Merge firstMerge = merges.get(0);
        double minSimilarity = firstMerge.similarity();
        double maxSimilarity = firstMerge.similarity();

//        normalize to 0 ~ 1
        for (Merge merge : merges) {
            if (merge.similarity() < minSimilarity) {
                minSimilarity = merge.similarity();
            }
            if (merge.similarity() > maxSimilarity) {
                maxSimilarity = merge.similarity();
            }
            //System.out.println(merge + "||" + merge.mergedCluster() + "||" + merge.remainingCluster());
        }

        //System.out.println("Min sim:" + minSimilarity + " " + "Max sim" + maxSimilarity);
//        double range = maxSimilarity - minSimilarity;
        //Build a map
        HashMap<Integer, Object> trackMapping = new HashMap<Integer, Object>();
        for (Integer i = 0; i < merges.size() + 1; i++) {
            trackMapping.put(i, i);
        }

        HashMultimap<String, Object> parentChildMapping = HashMultimap.create();
        HashMap<String, Double> similarityMap = new HashMap<String, Double>();
        String iNodeName = null;
        Merge merge;
        for (Integer i = 0; i < merges.size(); i++) {
            merge = merges.get(i);
            iNodeName = merge.mergedCluster() + "->" + merge.remainingCluster() + ":" + ID;
            parentChildMapping.put(iNodeName, trackMapping.get(merge.mergedCluster()));
            parentChildMapping.put(iNodeName, trackMapping.get(merge.remainingCluster()));
            trackMapping.put(new Integer(merge.remainingCluster()), iNodeName);
            similarityMap.put(iNodeName, merge.similarity());
            //System.out.println(merge.similarity());
        }

        //now you have a parent child mappin
        COntology ontology = new COntology("Hclust:" + Tools.randomID(), "Result from h-clust column", ID);
        ArrayList<String[]> pairs = new ArrayList<String[]>();

        String childNode, parentNode;
        for (String parent : parentChildMapping.keySet()) {
            Set children = parentChildMapping.get(parent);
            parentNode = parent;
            for (Object child : children) {
                if (child instanceof Integer) {
                    childNode = object.getViewNodeColumn((Integer) child).getName();
                } else {
                    childNode = child.toString();
                }
                pairs.add(new String[]{parentNode, childNode});
                ontology.addEdgeAttribute(parentNode, childNode, new COntologyEdgeAttributeImpl(similarityMap.get(parent).floatValue()));
                //System.out.println(parentNode + " " + childNode + " " + similarityMap.get(parent).floatValue());
            }
        }

        ontology.addRelationshipUpdateDepth(pairs);

        //COntologyUtils.printOntology(ontology);
        //List<String> roots = ontology.getRootNamesOrdered();
        //also copy the originals?
        if (Thread.interrupted()) {
//            System.out.println("Clustering Aborted");
            return;
        }

//        System.out.println("Clustering Ended Successfully");
        CoolMapMaster.addNewCOntology(ontology);

//        
//        HashSet<COntology> previousOntologies = new HashSet<COntology>();
//
//
//        for (VNode node : object.getViewNodesColumnFromTreeNodes()) {
//            if (node.getCOntology() != null) {
//                //for now just copy everything?
//                //ontology.mergeCOntologyTo(node.getCOntology());
//                previousOntologies.add(node.getCOntology());
//            }
//        }
//
//        //The ontology merge still causes issues
//        for (COntology prevOnto : previousOntologies) {
////            prevOnto.mergeCOntologyTo(ontology);
//        }
//        Node merge -> ensure nodes can be expanded correctly
//        HashMultimap<COntology, String> map = HashMultimap.create();

//        for (VNode node : object.getViewNodesColumn()) {
//            if (node.getCOntology() != null) {
//                map.put(node.getCOntology(), node.getName());
//            }
//        }
//
//        for (COntology otherOntology : map.keySet()) {
//            Set<String> termsToMerge = map.get(otherOntology);
////            System.out.println("Terms to merge:" + termsToMerge);
//            otherOntology.mergeCOntologyTo(ontology, termsToMerge); //merge over the previous terms
//        }
        
                //create row nodes to make it work
        HashMultimap<String, String> map = HashMultimap.create();
        for (VNode node : object.getViewNodesColumn()) {
            if (node.getCOntologyID() != null) {
                map.put(node.getCOntologyID(), node.getName());
            }
        }
//
//            it should be merged, but why not merged?
        for (String otherOntologyID : map.keySet()) {
            Set<String> termsToMerge = map.get(otherOntologyID);
//            System.out.println("Terms to merge:" + termsToMerge);
            COntology otherOntology = CoolMapMaster.getCOntologyByID(otherOntologyID);
            otherOntology.mergeCOntologyTo(ontology, termsToMerge); //merge over the previous terms
        }

        object.replaceColumnNodes(ontology.getRootNodesOrdered(), null);
        object.expandColumnNodeToBottom(object.getViewNodeColumn(0));
    }
}
