/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.contology.utils;

import coolmap.data.contology.model.COntology;
import java.util.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author gangsu
 */
public class COntologyUtils {

    private static final String[] _columnHeaders = new String[]{"Name", "Number of Child Nodes", "Child Nodes", "Number of Parent Nodes", "Parent Nodes", "Depth"};

    public static DefaultTableModel toTableModel(COntology ontology) {

        ArrayList<String> allNodes = new ArrayList<String>(ontology.getAllNodes());
        Collections.sort(allNodes);

        Object[][] data = new Object[allNodes.size()][_columnHeaders.length];
        String node;
        List<String> child, parent;

        for (int i = 0; i < data.length; i++) {
            node = allNodes.get(i);
            child = ontology.getImmediateChildren(node);
            parent = ontology.getImmediateParents(node);
            data[i][0] = node;
            data[i][1] = child.size();
            data[i][2] = Arrays.toString(child.toArray());
            data[i][3] = parent.size();
            data[i][4] = Arrays.toString(parent.toArray());
            data[i][5] = ontology.getMinimalDepthFromLeaves(node);
        }

        DefaultTableModel model = new DefaultTableModel(data, _columnHeaders) {

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        return model;
    }

    public static COntology createSampleOntology() {
        COntology ontology = new COntology("Sample ontology", null);
        ontology.addRelationshipUpdateDepth("RG0", "R0");
        ontology.addRelationshipUpdateDepth("RG0", "R1");
        ontology.addRelationshipUpdateDepth("RG0", "R2");

        ontology.addRelationshipUpdateDepth("RG1", "R3");
        ontology.addRelationshipUpdateDepth("RG1", "R4");
        ontology.addRelationshipUpdateDepth("RG1", "R5");

        ontology.addRelationshipUpdateDepth("RG2", "R6");
        ontology.addRelationshipUpdateDepth("RG2", "R7");
        ontology.addRelationshipUpdateDepth("RG2", "R8");
        ontology.addRelationshipUpdateDepth("RG2", "R9");

        ontology.addRelationshipUpdateDepth("RG00", "RG1");
        ontology.addRelationshipUpdateDepth("RG00", "RG2");

        ontology.addRelationshipUpdateDepth("RG01", "R4");
        ontology.addRelationshipUpdateDepth("RG01", "R4");
        ontology.addRelationshipUpdateDepth("RG01", null); //should not be working
        ontology.addRelationshipUpdateDepth("RG01", "R5");
        ontology.addRelationshipUpdateDepth("RG01", "RG2");

        ontology.addRelationshipUpdateDepth("R3", "RG00");
//        ontology.addRelationshipAndUpdate("R3", "RG01");

        return ontology;
    }

    public static COntology createSampleRowOntology() {
        COntology ontology = new COntology("SRO", null);
        ontology.addRelationshipUpdateDepth("RG0", "R0");
        ontology.addRelationshipUpdateDepth("RG0", "R1");
        ontology.addRelationshipUpdateDepth("RG0", "R2");
        ontology.addRelationshipUpdateDepth("RG1", "R3");
        ontology.addRelationshipUpdateDepth("RG1", "R4");
        ontology.addRelationshipUpdateDepth("RG1", "R5");

        ontology.addRelationshipUpdateDepth("RG00", "RG0");
        ontology.addRelationshipUpdateDepth("RG00", "RG1");
        ontology.addRelationshipUpdateDepth("RG00", "RG2");

        return ontology;
    }

    public static COntology createSampleColOntology() {
        COntology ontology = new COntology("SCO", null);

        ontology.addRelationshipUpdateDepth("CG1", "C2");
        ontology.addRelationshipUpdateDepth("CG1", "C3");
        ontology.addRelationshipUpdateDepth("CG1", "C4");
        ontology.addRelationshipUpdateDepth("CG1", "C6");
        ontology.addRelationshipUpdateDepth("CG0", "C0");
        ontology.addRelationshipUpdateDepth("CG0", "C1");
        ontology.addRelationshipUpdateDepth("CG0", "C2");
        
        

        ontology.addRelationshipUpdateDepth("CG00", "CG1");
        ontology.addRelationshipUpdateDepth("CG00", "CG0");

        ontology.addRelationshipUpdateDepth("CG00", "CG2");


        //ontology.removeLoops();

        return ontology;
    }

    public static synchronized void printOntology(COntology ontology) {
        HashSet<String> roots = ontology.getRootNames();
        HashSet<String> leaves = ontology.getLeafNodes();

        System.out.println("------------");
        System.out.println("     Ontology: " + ontology.getName());

        System.out.print("        Roots: ");
        if (roots != null) {
            Object[] rootArray = roots.toArray();
            Arrays.sort(rootArray);
            System.out.format("%8s", "[" + roots.size() + "]");
            System.out.print(Arrays.toString(rootArray));
        } else {
            System.out.format("%8s", "[0]");
        }
        System.out.println();

        System.out.print("       Leaves: ");
        if (leaves != null) {
            Object[] leafArray = leaves.toArray();
            Arrays.sort(leafArray);
            System.out.format("%8s", "[" + leaves.size() + "]");
            System.out.print(Arrays.toString(leafArray));
        } else {
            System.out.format("%8s", "[0]");
        }
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("    Structure:");
        HashSet<String> rootHash = ontology.getRootNames();
        if (rootHash != null && !rootHash.isEmpty()) {
            Object[] rootArray = rootHash.toArray();
            Arrays.sort(rootArray);
            for (Object root : rootArray) {
                _printNode(ontology, root, 14);
            }
        }

    }

    private static synchronized void _printNode(COntology ontology, Object no, int indent) {
        String node = (String) no;
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
        System.out.println(node + "(" + ontology.getMinimalDepthFromLeaves(node) + ")");
        ArrayList<String> childHash = ontology.getImmediateChildren(node);



        if (childHash != null && !childHash.isEmpty()) {
            Object[] children = childHash.toArray();
            Arrays.sort(children);
            for (Object child : children) {
                _printNode(ontology, child, indent + 2);
            }
        }
    }

    public static void main(String[] args) {
        //Automatically removes loops from ontology. Excellent!
        COntology ontology = COntologyUtils.createSampleOntology();
        System.out.println("Loops spotted?" + ontology.containsLoop());
        COntologyUtils.printOntology(ontology);

        System.out.println("Loops spotted?" + ontology.containsLoop());
        COntologyUtils.printOntology(ontology);
    }
}
