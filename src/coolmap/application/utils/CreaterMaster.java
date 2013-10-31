/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.utils;

import com.google.common.collect.Range;
import coolmap.application.CMainFrame;
import coolmap.application.CoolMapMaster;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
import coolmap.canvas.sidemaps.impl.ColumnLabels;
import coolmap.canvas.sidemaps.impl.ColumnTree;
import coolmap.canvas.sidemaps.impl.RowLabels;
import coolmap.canvas.sidemaps.impl.RowTree;
import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sugang
 */
public class CreaterMaster {

    /**
     * must be initialized after CoolMap has been initialized
     */
    public static void initialize() {
        Menu createMenu = new Menu("Create New");
        CMainFrame frame = CoolMapMaster.getCMainFrame();
        frame.addMenuItem("Edit", createMenu, true, false);

        //MenuItem item = frame.findMenuItem("/View/Arrange Views");
        //System.out.println("Found menu item:" + item);
        //createMenu.add(new MenuItem("Pretty"));

        _initNewFromSelection();
    }

    private static void _initNewFromSelection() {
        CMainFrame frame = CoolMapMaster.getCMainFrame();
        Menu menu = (Menu) frame.findMenu("Edit/Create New");

        System.out.println("Menu:" + menu);

        MenuItem createFromSelection = new MenuItem("From Selection");
        menu.add(createFromSelection);

        createFromSelection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //first of all, get the active coolMap object
                
                //Need to create a 'duplicate' function for CoolMap object
                
                
                CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object == null) {
                    return;
                    //must have active object
                }

                ArrayList<Range<Integer>> selectedRows = object.getCoolMapView().getSelectedRows();
                ArrayList<Range<Integer>> selectedColumns = object.getCoolMapView().getSelectedColumns();

                ArrayList<VNode> rowNodes = new ArrayList<VNode>();
                ArrayList<VNode> colNodes = new ArrayList<VNode>();

                VNode node;
                Integer low, high;
                CoolMapView view = object.getCoolMapView();
                for (Range<Integer> range : selectedRows) {
                    low = range.lowerEndpoint();
                    high = range.upperEndpoint();
                    for (int i = low; i < high; i++) {
                        node = object.getViewNodeRow(i).duplicate(); //duplicate a node
                        rowNodes.add(node);
                    }
                }

                for (Range<Integer> range : selectedColumns) {
                    low = range.lowerEndpoint();
                    high = range.upperEndpoint();
                    for (int i = low; i < high; i++) {
                        node = object.getViewNodeColumn(i).duplicate();
                        colNodes.add(node);
                    }
                }

//                System.out.println("Row nodes:" + rowNodes);
//                System.out.println("Col nodes:" + colNodes);

                //row nodes, col nodes
                CoolMapObject newObject = new CoolMapObject();
                List<CMatrix> matrices = object.getBaseCMatrices();

                if (matrices == null || matrices.isEmpty()) {
                    //an exception
                    return;
                }

                CMatrix[] cmatrix = new CMatrix[matrices.size()];

                matrices.toArray(cmatrix);

                newObject.addBaseCMatrix(cmatrix); //set the matrix to be the same base matrices

                newObject.insertRowNodes(rowNodes);
                newObject.insertColumnNodes(colNodes);

                //this should not be a big issue for now
                newObject.setSnippetConverter(object.getSnippetConverter());

                newObject.getCoolMapView().addRowMap(new RowLabels(newObject));
                newObject.getCoolMapView().addRowMap(new RowTree(newObject));
                newObject.getCoolMapView().addColumnMap(new ColumnLabels(newObject));
                newObject.getCoolMapView().addColumnMap(new ColumnTree(newObject));
                
                try{
                    newObject.setAggregator((CAggregator)object.getAggregator().getClass().newInstance());
                    newObject.setViewRenderer((ViewRenderer)object.getViewRenderer().getClass().newInstance());
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                
                CoolMapMaster.addNewCoolMapObject(newObject);
                

            }
        });

    }
}
