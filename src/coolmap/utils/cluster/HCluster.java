/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.cluster;

import coolmap.application.CoolMapMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.contology.utils.edgeattributes.COntologyEdgeAttributeImpl;
import coolmap.utils.Tools;
import edu.ucla.sspace.clustering.HierarchicalAgglomerativeClustering;
import edu.ucla.sspace.clustering.Merge;
import edu.ucla.sspace.common.Similarity;
import edu.ucla.sspace.matrix.ArrayMatrix;
import edu.ucla.sspace.util.HashMultiMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gangsu This will be another library outside of core.
 */
public class HCluster {

    public synchronized static void hclustRow(CoolMapObject<?, Double> object, HierarchicalAgglomerativeClustering.ClusterLinkage linkage, Similarity.SimType simType) {

        if (object == null || !object.isViewValid() || object.getViewClass() == null || !Double.class.isAssignableFrom(object.getViewClass())) {
            return;
        }

        System.out.println("Clustering started");

        ArrayMatrix matrix = new ArrayMatrix(object.getViewNumRows(), object.getViewNumColumns());
        for (int i = 0; i < object.getViewNumRows(); i++) {
            for (int j = 0; j < object.getViewNumColumns(); j++) {
                Double data = object.getViewValue(i, j);
                if (data == null || data.isInfinite() || data.isNaN()) {
                    matrix.set(i, j, Double.NaN);
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

        HashMultiMap<String, Object> parentChildMapping = new HashMultiMap<String, Object>();
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
            System.out.println("Clustering Aborted");
            return;
        }

        System.out.println("Clustering Ended Successfully");
        CoolMapMaster.addNewCOntology(ontology);

        HashSet<COntology> previousOntologies = new HashSet<COntology>();



        for (VNode node : object.getViewNodesRow()) {
            if (node.getCOntology() != null) {
                //for now just copy everything?
                //ontology.mergeCOntologyTo(node.getCOntology());
                previousOntologies.add(node.getCOntology());
            }
        }

        System.out.println("+++" + previousOntologies + "+++");

        //this merge ontology part got trouble

        for (COntology prevOnto : previousOntologies) {
            prevOnto.mergeCOntologyTo(ontology);
        }

        System.out.println("+++ check point +++");
//        System.out.println(ontology.getRootNamesOrdered());

//        Why this step go it dead completely?
        System.out.println("attempt to replace nodes:");
        object.replaceRowNodes(ontology.getRootNodesOrdered());
        //It can not be expanded for weird reason
         System.out.println("attempt to expand nodes:");
        
        //This is when problem occurs 
        object.expandRowNodeToBottom(object.getViewNodeRow(0));

        System.out.println("+++++++++++++++++++++++Ended Successfully\n\n");
        
    }

    public synchronized static void hclustColumn(CoolMapObject<?, Double> object, HierarchicalAgglomerativeClustering.ClusterLinkage linkage, Similarity.SimType simType) {

        if (object == null || !object.isViewValid() || object.getViewClass() == null || !Double.class.isAssignableFrom(object.getViewClass())) {
            return;
        }

        //System.out.println("Clustering started");

        ArrayMatrix matrix = new ArrayMatrix(object.getViewNumColumns(), object.getViewNumRows());
        for (int i = 0; i < object.getViewNumRows(); i++) {
            for (int j = 0; j < object.getViewNumColumns(); j++) {
                Double data = object.getViewValue(i, j);
                if (data == null || data.isInfinite() || data.isNaN()) {
                    matrix.set(j, i, Double.NaN);
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

        HashMultiMap<String, Object> parentChildMapping = new HashMultiMap<String, Object>();
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
            System.out.println("Clustering Aborted");
            return;
        }

        System.out.println("Clustering Ended Successfully");
        CoolMapMaster.addNewCOntology(ontology);

        HashSet<COntology> previousOntologies = new HashSet<COntology>();


        for (VNode node : object.getViewNodesColumn()) {
            if (node.getCOntology() != null) {
                //for now just copy everything?
                //ontology.mergeCOntologyTo(node.getCOntology());
                previousOntologies.add(node.getCOntology());
            }
        }

        //The ontology merge still causes issues
        for (COntology prevOnto : previousOntologies) {
            prevOnto.mergeCOntologyTo(ontology);
        }

        object.replaceColumnNodes(ontology.getRootNodesOrdered());
        object.expandColumnNodeToBottom(object.getViewNodeColumn(0));
    }
}
