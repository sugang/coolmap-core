/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.action;

import com.google.common.collect.Range;
import coolmap.application.CoolMapMaster;
import coolmap.application.state.StateStorageMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.state.CoolMapState;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class PasteColumnNodesAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
            Transferable content = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            String data = (String) content.getTransferData(DataFlavor.stringFlavor);
            JSONObject json = new JSONObject(data);

            System.out.println(json);

            //System.out.println(((JMenuItem)(e.getSource())).getParent());
            ArrayList<Range<Integer>> selectedColumns = obj.getCoolMapView().getSelectedColumns();
//            System.out.println(selectedColumns);
            //if there are selections
            int insertionIndex = 0;
            if (selectedColumns != null && !selectedColumns.isEmpty()) {
                insertionIndex = selectedColumns.get(0).lowerEndpoint();

            } //else

            System.out.println(insertionIndex);

            JSONArray terms = json.getJSONArray("Terms");
            String ontologyID = json.getString("OntologyID");

            COntology ontology = CoolMapMaster.getCOntologyByID(ontologyID);

            System.out.println("Ontology:" + ontology);

            if (ontology == null) {
                return;
            }

            ArrayList<VNode> newNodes = new ArrayList<VNode>();

            for (int i = 0; i < terms.length(); i++) {
                VNode node = new VNode(terms.getString(i), ontology);
                newNodes.add(node);
                System.out.println(node);
            }

            Rectangle centerTo = new Rectangle(insertionIndex, 0, 1, 1);
            if (obj.getCoolMapView().getSelectedRows() != null && !obj.getCoolMapView().getSelectedRows().isEmpty()) {
                centerTo.y = ((Range<Integer>) (obj.getCoolMapView().getSelectedRows().get(0))).lowerEndpoint();
            }

            CoolMapState state = CoolMapState.createStateColumns("Insert Column Nodes", obj, null);
            obj.insertColumnNodes(insertionIndex, newNodes, true);
            //need to center to inserted
            obj.getCoolMapView().centerToRegion(centerTo);

            StateStorageMaster.addState(state);

        } catch (Exception ex) {
            //Return
            System.err.println("Exception in pasting columns:" + ex);
        }
    }

}
