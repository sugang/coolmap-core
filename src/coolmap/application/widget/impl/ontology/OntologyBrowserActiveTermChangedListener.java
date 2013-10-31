/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.widget.impl.ontology;

import coolmap.data.contology.model.COntology;

/**
 *
 * @author sugang
 */
public interface OntologyBrowserActiveTermChangedListener {
    
    public void activeTermChanged(String term, COntology ontology);
}
