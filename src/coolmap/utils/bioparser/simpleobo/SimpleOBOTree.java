/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.bioparser.simpleobo;

import com.google.common.collect.HashMultimap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * records the relationship between GO terms
 *
 * @author sugang
 */
public class SimpleOBOTree {

    private HashMultimap<String, String> goTree = HashMultimap.create();
    private HashMap<String, SimpleOBOEntry> goTermHash = new HashMap<>();

    public void addEntry(SimpleOBOEntry entry) {
        goTermHash.put(entry.getID(), entry);
    }

    /**
     * returns the go tree - not a copy, for efficiency and economy concerns
     *
     * @return
     */
    public HashMultimap<String, String> getTree() {
        return goTree;
    }

    public void addTreeBranch(String parent, String child) {
        goTree.put(parent, child);
    }

    public static SimpleOBOTree parse(String name, InputStream in) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        SimpleOBOEntry currentEntry = null;
        String currentChildTerm = null;
        SimpleOBOTree simpleOboTree = new SimpleOBOTree();

        while ((line = reader.readLine()) != null) {
            if (Thread.interrupted()) {
                return null;
            }
            line = line.trim();

            if (line.length() == 0) {
                if (currentEntry != null) {
                    simpleOboTree.addEntry(currentEntry); //if it's not null, add a new entry
                    currentEntry = null;
                }
            } else if (line.startsWith("[Term]")) {
                currentEntry = new SimpleOBOEntry();
                //
            } else if (currentEntry != null) {

                if (line.startsWith("id:")) {
                    //the current entry must not be null
                    //try{
                    
                    currentChildTerm = line.substring(3).trim();
                    currentEntry.setID(currentChildTerm);

                } else if (line.startsWith("name:")) {
                    //the current entry must not be null
                    currentEntry.setName(line.substring(5).trim());

                } else if (line.startsWith("namespace:")) {
                    //the current entry must not be null
                    currentEntry.setNamespace(line.substring(11));

                } else if (line.startsWith("is_a:")) {
                    //the current entry must not be null
                    String parent = line.substring(5).trim();
                    simpleOboTree.addTreeBranch(parent, currentChildTerm);
                    currentEntry.addParent(parent);

                } else {
                    //add attributes

                    //System.out.println(line);
                    String ele[] = line.split(": ", 2); //why limit is 2?
                    currentEntry.addAttribute(ele[0], ele[1]);

                }
            }
        }

        return simpleOboTree;
    }

    public void printEntries() {
        System.out.println("Printing " + goTermHash.size() + " entries");
        for (SimpleOBOEntry entry : goTermHash.values()) {
            entry.print();
            if (Thread.interrupted()) {
                System.err.println("print canceled");
                return;
            }
        }
    }

    public void printBranches() {
        System.out.println("Printing tree structure between terms:");
        for (String parent : goTree.keySet()) {
            System.out.print(parent + " -> ");
            Set<String> children = goTree.get(parent);
            System.out.println(children);
            if (Thread.interrupted()) {
                System.err.println("print canceled");
                return;
            }
        }

    }

    /**
     * return all nodeIDs that have children
     *
     * @return
     */
    public Set<String> getParentNodeIDs() {
        return goTree.keySet();
    }

    /**
     * return all nodeIDs that are children of the given parentNodeID
     *
     * @param parentNodeID
     * @return
     */
    public Set<String> getChildNodes(String parentNodeID) {
        return goTree.get(parentNodeID);
    }

    public SimpleOBOEntry getEntry(String id) {
        return goTermHash.get(id);
    }

    public Set<String> getAllNodeIDs() {
        return goTermHash.keySet();
    }

    public Set<SimpleOBOEntry> getAllEntries() {
        HashSet<SimpleOBOEntry> entries = new HashSet<>();
        entries.addAll(goTermHash.values());
        return entries;
    }

}
