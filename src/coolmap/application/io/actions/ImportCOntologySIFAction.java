/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.actions;

import coolmap.application.CoolMapMaster;
import coolmap.application.io.external.ImportCOntologyFromSimpleTwoColumn;
import coolmap.application.utils.LongTask;
import coolmap.application.utils.TaskEngine;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.data.contology.model.COntology;
import coolmap.utils.Tools;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author sugang
 */
public class ImportCOntologySIFAction extends AbstractAction {

    public ImportCOntologySIFAction() {
        super("from sif(two column)");
        putValue(SHORT_DESCRIPTION, "import ontology from sif (two column file, 'child parent') ");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = Tools.getCustomFileChooser(new FileNameExtensionFilter(".tsv", "txt", "tsv"));
        int returnVal = chooser.showOpenDialog(CoolMapMaster.getCMainFrame());
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final File f = chooser.getSelectedFile();
        if (f != null && f.isFile() && f.exists()) {

            LongTask task = new LongTask("import ontology...") {

                @Override
                public void run() {
                    try {
                        COntology ontology = ImportCOntologyFromSimpleTwoColumn.importFromFile(f);
                        ontology.setName(Tools.removeFileExtension(f.getName()));
                        if(ontology == null)
                            return;
                        
                        CoolMapMaster.addNewCOntology(ontology);

                        CMConsole.logInSuccess("Ontology imported from " + f.getPath());
                    } catch (Exception ex) {
                        CMConsole.logError("Error: failed to import ontology from " + f.getName());
                    }
                }
            };
            
            TaskEngine.getInstance().submitTask(task);

        }
        
        
    }

}
