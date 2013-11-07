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
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class PasteRowNodesAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
            Transferable content = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            String data = (String) content.getTransferData(DataFlavor.stringFlavor);
            JSONObject json = new JSONObject(data);

            ArrayList<Range<Integer>> selectedRows = obj.getCoolMapView().getSelectedRows();
//            System.out.println(selectedColumns);
            //if there are selections
            int insertionIndex = 0;
            if (selectedRows != null && !selectedRows.isEmpty()) {
                insertionIndex = selectedRows.get(0).lowerEndpoint();
                //System.out.println(lower);

            } //else
            else {
                //append at beginning

            }

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

            Rectangle centerTo = new Rectangle(0, insertionIndex, 1, 1);
            if (obj.getCoolMapView().getSelectedColumns()!= null && !obj.getCoolMapView().getSelectedColumns().isEmpty()) {
                centerTo.x = ((Range<Integer>) (obj.getCoolMapView().getSelectedColumns().get(0))).lowerEndpoint();
            }

            CoolMapState state = CoolMapState.createStateRows("Insert Row Nodes", obj, null);
            obj.insertRowNodes(insertionIndex, newNodes, true);
            obj.getCoolMapView().centerToRegion(centerTo);
            StateStorageMaster.addState(state);

        } catch (Exception ex) {
            System.err.println("Exception in pasting rows:" + ex);
        }
    }

}
