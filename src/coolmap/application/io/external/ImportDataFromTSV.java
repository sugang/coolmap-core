/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

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
import coolmap.data.snippet.DoubleSnippet1_3;
import coolmap.utils.Tools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author gangsu
 */
public class ImportDataFromTSV implements ImportData {

    private HashSet<CoolMapObject> objects = new HashSet<>();

    public void importFromFiles(File... files) throws Exception {

//        System.out.println("importing from files");
        for (File file : files) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                ArrayList<String> dataLines = new ArrayList<>();

                String header = reader.readLine();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    dataLines.add(line);
                }

                String[] ele = header.split("\t", -1);

                DoubleCMatrix matrix = new DoubleCMatrix(Tools.removeFileExtension(file.getName()), dataLines.size(), ele.length - 1);

                String[] colLabels = new String[ele.length - 1];
                for (int i = 1; i < ele.length; i++) {
                    colLabels[i - 1] = ele[i].trim();
                }
                matrix.setColLabels(colLabels);

                int counter = 0;
                Double value;
                for (String row : dataLines) {

                    ele = row.split("\t");
                    matrix.setRowLabel(counter, ele[0]);

                    for (int i = 1; i < ele.length; i++) {
                        try {
                            value = Double.parseDouble(ele[i]);
                        } catch (Exception e) {
                            value = null;
                        }
                        matrix.setValue(counter, i - 1, value);
                        if (Thread.interrupted()) {
                            objects.clear();
                            return;
                        }
                    }

                    counter++;
                }

                reader.close();

                if (Thread.interrupted()) {
                    objects.clear();
                    return;
                }

                //return matrix;
                //obtained matrix from here
                CoolMapObject object = new CoolMapObject();
                object.setName(Tools.removeFileExtension(file.getName()));
                object.addBaseCMatrix(matrix);

                ArrayList<VNode> nodes = new ArrayList<>();
                for (Object label : matrix.getRowLabelsAsList()) {
                    nodes.add(new VNode(label.toString()));
                }
                object.insertRowNodes(nodes);

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
                return;
            }
        }

    }

    @Override
    public String getLabel() {
        return "Numeric tsv";
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() {
        return new FileNameExtensionFilter("Numeric tsv", "tsv", "txt");
    }

    @Override
    public Set<CoolMapObject> getImportedCoolMapObjects() {
        return objects;
    }

    @Override
    public Set<COntology> getImportedCOntology() {
        return null;
    }

    @Override
    public void configure(File... file) {
    }

    @Override
    public boolean onlyImportFromSingleFile() {
        return false;
    }

    @Override
    public void importFromFile(File file) throws Exception {
    }
    
    

}
