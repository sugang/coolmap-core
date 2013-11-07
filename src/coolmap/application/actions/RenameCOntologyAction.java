/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.actions;

import coolmap.utils.graphics.UI;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author sugang
 */
public class RenameCOntologyAction extends AbstractAction{

    public RenameCOntologyAction() {
        super("Rename", UI.getImageIcon("pen"));
    }

    
    
    @Override
    public void actionPerformed(ActionEvent e) {
    }
    
}
