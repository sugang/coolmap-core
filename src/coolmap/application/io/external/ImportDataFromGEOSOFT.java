/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Table;
import coolmap.application.io.external.interfaces.ImportData;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.canvas.datarenderer.renderer.impl.NumberToColor;
import coolmap.canvas.sidemaps.impl.ColumnLabels;
import coolmap.canvas.sidemaps.impl.ColumnTree;
import coolmap.canvas.sidemaps.impl.RowLabels;
import coolmap.canvas.sidemaps.impl.RowTree;
import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.impl.DoubleDoubleMean;
import coolmap.data.cmatrix.impl.DoubleCMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.contology.model.COntologyPreset;
import coolmap.data.snippet.DoubleSnippet1_3;
import coolmap.utils.Tools;
import coolmap.utils.bioparser.geosoft.ArrayTable;
import coolmap.utils.bioparser.geosoft.GeoSOFT;
import coolmap.utils.bioparser.geosoft.Subset;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author sugang
 */
public class ImportDataFromGEOSOFT implements ImportData {

    private HashSet<CoolMapObject> objects = new HashSet<CoolMapObject>();
    private HashSet<COntology> ontologies = new HashSet<COntology>();

    private final String subset_dataset_id = "subset_dataset_id";
    private final String subset_description = "subset_description";
    private final String subset_sample_id = "subset_sample_id";
    private final String subset_type = "subset_type";

    private final String gene_title = "Gene title";
    private final String gene_symbol = "Gene symbol";
    private final String gene_ID = "Gene ID";
    private final String uniGene_title = "UniGene title";
    private final String uniGene_symbol = "UniGene symbol";
    private final String uniGene_ID = "UniGene ID";
    private final String nucleotide_title = "Nucleotide Title";
    private final String gi = "GI";
    private final String genBank_Accession = "GenBank Accession";
    private final String platform_CLONEID = "Platform_CLONEID";
    private final String platform_ORF = "Platform_ORF";
    private final String platform_SPOTID = "Platform_SPOTID";
    private final String chromosome_location = "Chromosome location";
    private final String chromosome_annotation = "Chromosome annotation";
    private final String go_function = "GO:Function";
    private final String go_process = "GO:Process";
    private final String go_component = "GO:Component";
    private final String go_function_ID = "GO:Function ID";
    private final String go_process_ID = "GO:Process ID";
    private final String go_component_ID = "GO:Component ID";

    @Override
    public void importFromFile(File... files) throws Exception {
        for (File file : files) {
            try {
                GeoSOFT geoEntry = GeoSOFT.parse(file);
                Collection<Subset> subsets = geoEntry.getSubsets();

                String namePrefix = Tools.removeFileExtension(file.getName());

//                Import all the subsets
                COntology ontology = new COntology(namePrefix + " Subsets", "All sample subsets in " + namePrefix);
                for (Subset subset : subsets) {
//                    subset.printDetails();
                    String parentTerm = subset.getID();
                    String childTerms = subset.getAttribute(subset_sample_id);
                    String description = subset.getAttribute(subset_description);
                    String type = subset.getAttribute(subset_type);

                    if (parentTerm == null || parentTerm.length() == 0 || childTerms == null || childTerms.length() == 0) {
                        continue;
                    }

                    COntology.setAttribute(parentTerm, "GEO.Description", description);
                    COntology.setAttribute(parentTerm, "GEO.Type", type);

                    String[] cTerms = childTerms.split(",");
                    for (String term : cTerms) {
                        ontology.addRelationshipNoUpdateDepth(parentTerm, term.trim());
                    }

                }//end of all subsets

                //This should be fast enough
                ontology.validate(); //also attempt to remove loops
                ontologies.add(ontology);

//                Import attributes
                ArrayTable arrayTable = geoEntry.getArrayTable();
                String rowNames[] = arrayTable.getRowNames();
                String columnNames[] = arrayTable.getColumnNames();
                String rowSymbols[] = arrayTable.getRowSymbols();
                LinkedHashMultimap<String, String> mapping = arrayTable.getGeneSymbolToProbeIDMapping();
                COntology geneOntology = new COntology(namePrefix + " Genes", "GO terms, Gene Symbols to ProbeID");

                geneOntology.addRelationshipUpdateDepth(mapping); //add all

//              Then need to add GO terms  
                Table<String, String, String> attributes = arrayTable.getAttributes();
//                for (String probeID : attributes.rowKeySet()) {
////                    for(String attrName : attributes.columnKeySet()){
////                        COntology.setAttribute(probeID, "GEO." + attrName, attributes.get(probeID, attrName));
////                    }
//
//                    //Also add:
////                    String symbol = attributes.get(probeID, "Gene symbol");
//                    COntology.setAttribute(probeID, probeID, attributes);
//
//                }
                LinkedHashSet<String> genes = new LinkedHashSet<String>();
                LinkedHashSet<String> goFunctionsPreset = new LinkedHashSet<String>();
                LinkedHashSet<String> goComponentsPreset = new LinkedHashSet<String>();
                LinkedHashSet<String> goProcessesPreset = new LinkedHashSet<String>();

                String probeID, symbol, nucleotideTitleData, geneTitleData, goFunctionIDString, goFunctionString, goComponentIDString, goComponentString, goProcessIDString, goProcessString;
                for (int i = 0; i < rowNames.length; i++) {
                    probeID = rowNames[i];

                    symbol = rowSymbols[i];

                    if (symbol == null || symbol.length() == 0) {
                        continue;
                    }

                    genes.add(symbol);

                    nucleotideTitleData = attributes.get(probeID, nucleotide_title);
                    geneTitleData = attributes.get(probeID, gene_title);

                    if (nucleotideTitleData != null) {
                        COntology.setAttribute(probeID, "GEO." + nucleotide_title, nucleotideTitleData);
                    }
                    if (geneTitleData != null) {
                        COntology.setAttribute(symbol, "GEO." + gene_title, geneTitleData);
                    }

//                  //These attributes may not exist  
                    try {

                        goFunctionIDString = attributes.get(probeID, go_function_ID);
                        goFunctionString = attributes.get(probeID, go_function);

                        if (goFunctionIDString != null) {

                            String[] GOFunctionIDs = goFunctionIDString.split("///", -1);
                            String[] GOFunctions = goFunctionString.split("///", -1);

                            for (int j = 0; j < GOFunctionIDs.length; j++) {

                                COntology.setAttribute(GOFunctionIDs[j], "GEO.Go function", GOFunctions[j]);

                                String parentTerm = GOFunctionIDs[j];
                                if (parentTerm == null || parentTerm.length() == 0) {
                                    continue;
                                }
                                geneOntology.addRelationshipNoUpdateDepth(parentTerm, symbol);

                            }
                        }

                    } catch (Exception e) {

                    }

                    try {

                        goComponentIDString = attributes.get(probeID, go_component_ID);
                        goComponentString = attributes.get(probeID, go_component);

                        if (goComponentIDString != null) {
                            String[] GOComponentIDs = goComponentIDString.split("///", -1);
                            String[] GOComponents = goComponentString.split("///", -1);

                            for (int j = 0; j < GOComponentIDs.length; j++) {
                                COntology.setAttribute(GOComponentIDs[j], "GEO.Go component", GOComponents[j]);
                                String parentTerm = GOComponentIDs[j];
                                if (parentTerm == null || parentTerm.length() == 0) {
                                    continue;
                                }
                                geneOntology.addRelationshipNoUpdateDepth(parentTerm, symbol);

                            }
                        }

                    } catch (Exception e) {

                    }

                    try {
                        goProcessIDString = attributes.get(probeID, go_process_ID);
                        goProcessString = attributes.get(probeID, go_process);

                        String[] GOProcessIDs = goProcessIDString.split("///", -1);
                        String[] GOProcesses = goProcessString.split("///", -1);

                        for (int j = 0; j < GOProcessIDs.length; j++) {
                            COntology.setAttribute(GOProcessIDs[j], "GEO.Go process", GOProcesses[j]);
                            String parentTerm = GOProcessIDs[j];
                            if (parentTerm == null || parentTerm.length() == 0) {
                                continue;
                            }
                            geneOntology.addRelationshipNoUpdateDepth(parentTerm, symbol);
                        }

                    } catch (Exception e) {

                    }

                }

                geneOntology.validate();

                //add presets
                COntologyPreset preset = new COntologyPreset("Genes", "Gene level view", genes, null);

                geneOntology.addPreset(preset);

                ontologies.add(geneOntology);

                DoubleCMatrix matrix = new DoubleCMatrix(Tools.removeFileExtension(file.getName()), rowNames.length, columnNames.length);

                //
//                System.out.println(Arrays.toString(rowNames));
                //If soft file is full then it becomes null??
//                System.out.println(Arrays.toString(columnNames));
                matrix.setColLabels(columnNames);
                matrix.setRowLabels(rowNames);

                for (int i = 0; i < rowNames.length; i++) {
                    for (int j = 0; j < columnNames.length; j++) {
                        matrix.setValue(i, j, arrayTable.getValue(i, j));
                        if (Thread.interrupted()) {
                            objects.clear();
                            ontologies.clear();
                            return;
                        }
                    }
                }

                //replace these with genes later, intialize ontology first
                CoolMapObject object = new CoolMapObject();
                object.setName(Tools.removeFileExtension(file.getName()));
                object.addBaseCMatrix(matrix);

                ArrayList<VNode> nodes = new ArrayList<VNode>();
                for (String label : preset.getLabels()) {
                    nodes.add(new VNode(label.toString(), geneOntology));
                }
                object.insertRowNodes(nodes);
                /////////////////////////////////////////////////////////

                nodes.clear();
                for (Object label : matrix.getColLabelsAsList()) {
                    nodes.add(new VNode(label.toString()));
                }
                object.insertColumnNodes(nodes);

                object.setAggregator(new DoubleDoubleMean());
                object.setSnippetConverter(new DoubleSnippet1_3());
                object.setViewRenderer(new NumberToColor(), true);

                object.getCoolMapView().addColumnMap(new ColumnLabels(object));
                object.getCoolMapView().addColumnMap(new ColumnTree(object));
                object.getCoolMapView().addRowMap(new RowLabels(object));
                object.getCoolMapView().addRowMap(new RowTree(object));

                objects.add(object);

            } catch (Exception e) {
                CMConsole.logError(" failed to import numeric matrix data from: " + file);
                e.printStackTrace();
            }
        }

//
//            if (Thread.interrupted()) {
//                return null;
//            }
//            return matrix;
//        } catch (Exception e) {
//            CMConsole.logError(" failed to import numeric matrix data from: " + file);
//            return null;
//        }
    }

    @Override
    public String getLabel() {
        return "NCBI.GEO soft";
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() {
        return new FileNameExtensionFilter("GEO soft", "soft", "txt");
    }

    @Override
    public Set<CoolMapObject> getImportedCoolMapObjects() {
        return objects;
    }

    @Override
    public Set<COntology> getImportedCOntology() {
        return ontologies;
    }

}
