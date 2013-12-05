/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.actions;

import coolmap.application.CoolMapMaster;
import coolmap.application.io.external.ImportCOntologyFromGMT;
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
public class ImportCOntologyGMTAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = Tools.getCustomFileChooser(new FileNameExtensionFilter(".gmt", "gmt", "txt"));
        int returnVal = chooser.showOpenDialog(CoolMapMaster.getCMainFrame());
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final File f = chooser.getSelectedFile();
        if (f == null || !f.exists() || !f.isFile()) {
            CMConsole.logError("Failed to open file: " + f.getName());
            return;
        }

        //
        LongTask task = new LongTask("Import ontology...") {

            @Override
            public void run() {
                try {
                    COntology ontology = ImportCOntologyFromGMT.importFromFile(f);
                    if(ontology == null)
                        return;
                    
                    CoolMapMaster.addNewCOntology(ontology);
                    
                    CMConsole.logInSuccess("Ontology imported from " + f.getPath());
                } catch (Exception e) {
                    CMConsole.logError("Error: failed to import ontology from " + f );
                }
            }
        };
        
        TaskEngine.getInstance().submitTask(task);

    }

}
