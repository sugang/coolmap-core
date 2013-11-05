/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.cluster;

import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.utils.graphics.UI;
import edu.ucla.sspace.clustering.Assignments;
import edu.ucla.sspace.clustering.BisectingKMeans;
import edu.ucla.sspace.matrix.ArrayMatrix;
import java.awt.Color;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author gangsu
 */
public class KMeans {

    public synchronized static void kMeansClusterRow(CoolMapObject<?, Double> object, int numClusters) {
        if (object == null || !object.isViewMatrixValid() || object.getViewClass() == null || !Double.class.isAssignableFrom(object.getViewClass())) {
            return;
        }

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
        
        BisectingKMeans bisectingKMeans = new BisectingKMeans();
        Assignments assignments = bisectingKMeans.cluster(matrix, numClusters, new Properties());
        List<Set<Integer>> clusters = assignments.clusters();
        COntology ontology = new COntology("TestOntology", null);
        int counter = 0;
        for(Set<Integer> cluster : clusters){
            String groupName = "GRP:" + counter;
            counter++;
            for(Integer i : cluster){
                ontology.addRelationshipNoUpdateDepth(groupName, object.getViewNodeRow(i).getName());
            }
        }
        
        ontology.validate();
        List<VNode> rootNodes = ontology.getRootNodesOrdered();
        object.insertRowNodes(0, rootNodes, false);
        
        for(VNode node : rootNodes){
            node.colorTree(UI.randomColor());
            //object.expandRowNode(node);//Map updated multiple times? NO should only once.
        }
        
        object.expandRowNodes(rootNodes);
        
        
        
    }
}
