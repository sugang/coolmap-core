/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.actions;

import coolmap.application.CoolMapMaster;
import coolmap.utils.graphics.UI;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import quicktime.util.QTUtils;

/**
 *
 * @author sugang
 */
public class DeleteCOntologyAction extends AbstractAction {

    public DeleteCOntologyAction(){
        super("Delete Ontology", UI.getImageIcon("trashBin"));
        putValue(SHORT_DESCRIPTION, "Remove current COntology");
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        //remove this COntology from the Combo -> Aslo ask whether remove all node associated with this COntology in view as well
        int returnVal = JOptionPane.showConfirmDialog(CoolMapMaster.getCMainFrame(), "Are you sure you want to delete the current ontology? \nOperation can't be undone", "Confirm Ontology removal", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if(returnVal == JOptionPane.OK_OPTION){
            
        }
    }
    
}
