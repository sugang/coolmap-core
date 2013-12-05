/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.bioparser.gseagmt;

import com.google.common.collect.HashMultimap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sugang
 */
public class GmtEntry {

    public static enum ID_TYPE {

        GENE_SYMBOL, ENTREZ_ID, ORIGINAL_IDENTIFIER, UNKNOWN
    };
    /**
     * data
     */
    private HashMap<String, String> descriptions = new HashMap<>();
    private HashMultimap<String, String> genesets = HashMultimap.create();
    private String name = "Untilted";
    private ID_TYPE idType = ID_TYPE.UNKNOWN;

    public GmtEntry() {
    }

    public GmtEntry(String name, ID_TYPE idType) {
        this.name = name;
        this.idType = idType;
    }

    public void addEntry(String genesetString, String geneString) {
        genesets.put(genesetString, geneString);
    }

    public void clear(String genesetString) {
        genesets.removeAll(genesetString);
    }

    public void clearAll() {
        genesets.clear();
    }

    public void addDescription(String genesetString, String descriptionString) {
        descriptions.put(genesetString, descriptionString);
    }
    
    public String getDescription(String genesetString){
        return descriptions.get(genesetString);
    }

    public void printStructure() {
        System.out.println(name + ":" + idType);
        System.out.println("------------------");

        for (String genesetString : genesets.keySet()) {

            Set<String> genes = genesets.get(genesetString);
            System.out.println(genesetString + "( " + genes.size() + " )" + " : " + descriptions.get(genesetString));
            System.out.println("--- " + Arrays.toString(genes.toArray()));
        }
    }

    public Set<String> getGenesetNames() {
        Set<String> genesetNames = new HashSet<>();
        genesetNames.addAll(genesets.keySet());
        return genesetNames;
    }

    public Set<String> getGenesetGenes(String genesetName) {
        return genesets.get(genesetName);
    }

    /**
     * parse from input stream
     * @param name
     * @param idType
     * @param in
     * @return
     * @throws IOException 
     */
    public static GmtEntry parse(String name, GmtEntry.ID_TYPE idType, InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        String ele[];
        GmtEntry gmtObject = new GmtEntry(name, idType);
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            ele = line.split("\\t", -1);
            if (ele.length <= 2) {
                continue; //only has name and descritpion, no child
            }

            //parse
            gmtObject.addDescription(ele[0], ele[1]);

            for (int i = 2; i < ele.length; i++) {
                gmtObject.addEntry(ele[0], ele[i]);
            }
        }
        reader.close();
        return gmtObject;
    }

    public static GmtEntry parse(InputStream in) throws IOException {
        return parse("untitled", GmtEntry.ID_TYPE.UNKNOWN, in);
    }
}
