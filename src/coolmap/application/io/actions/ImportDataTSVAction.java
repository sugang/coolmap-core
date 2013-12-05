/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.actions;

import coolmap.application.CoolMapMaster;
import coolmap.application.io.external.ImportDoubleCMatrixFromFile;
import coolmap.application.utils.LongTask;
import coolmap.application.utils.TaskEngine;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.canvas.datarenderer.renderer.impl.NumberToColor;
import coolmap.canvas.sidemaps.impl.ColumnLabels;
import coolmap.canvas.sidemaps.impl.ColumnTree;
import coolmap.canvas.sidemaps.impl.RowLabels;
import coolmap.canvas.sidemaps.impl.RowTree;
import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.impl.DoubleDoubleMean;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.snippet.DoubleSnippet1_3;
import coolmap.utils.Tools;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author sugang
 */
public class ImportDataTSVAction extends AbstractAction {

    public ImportDataTSVAction() {
        super("from .tsv");
        putValue(SHORT_DESCRIPTION, "import data from tab delimited table");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        JFileChooser chooser = Tools.getCustomFileChooser(new FileNameExtensionFilter(".tsv", "txt", "tsv"));
        int returnVal = chooser.showOpenDialog(CoolMapMaster.getCMainFrame());
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final File f = chooser.getSelectedFile();
        if (f == null || !f.exists() || !f.isFile()) {
            CMConsole.logError("Failed to open file: " + f.getName());
            return;
        }

        LongTask task = new LongTask("import data...") {

            @Override
            public void run() {
                try {
                    CMatrix matrix = ImportDoubleCMatrixFromFile.importFromFile(f);
                    if(matrix == null){
                        return;
                    }
                    
                    
                    CoolMapObject object = new CoolMapObject();
                    object.addBaseCMatrix(matrix);
                    ArrayList<VNode> nodes = new ArrayList<VNode>();
                    for (Object label : matrix.getRowLabelsAsList()) {
                        nodes.add(new VNode(label.toString()));
                    }
                    object.insertRowNodes(nodes);

                    nodes.clear();
                    for (Object label : matrix.getColLabelsAsList()) {
                        nodes.add(new VNode(label.toString()));
                    }
                    object.insertColumnNodes(nodes);

//            
                    object.setAggregator(new DoubleDoubleMean());
                    object.setSnippetConverter(new DoubleSnippet1_3());

                    object.setViewRenderer(new NumberToColor(), true); //This must be added after aggregator

                    object.getCoolMapView().addRowMap(new RowLabels(object));
                    object.getCoolMapView().addRowMap(new RowTree(object));
                    object.getCoolMapView().addColumnMap(new ColumnLabels(object));
                    object.getCoolMapView().addColumnMap(new ColumnTree(object));

                    object.setName(Tools.removeFileExtension(f.getName()));
                    
                    if(Thread.interrupted()){
                        return;
                    }
                    
                    
                    CoolMapMaster.addNewBaseMatrix(matrix);
                    CoolMapMaster.addNewCoolMapObject(object);

                    CMConsole.logInSuccess("Data imported from: " + f.getPath());

                } catch (Exception ex) {
                    CMConsole.logError("Failed to open file: " + f.getName());
                }
            }
        };
        
        TaskEngine.getInstance().submitTask(task);

    }

}
