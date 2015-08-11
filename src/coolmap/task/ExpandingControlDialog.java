package coolmap.task;

import coolmap.application.CoolMapMaster;
import coolmap.canvas.sidemaps.util.SideTreeUtil;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

/**
 *
 * @author Keqiang Li
 */
public class ExpandingControlDialog extends javax.swing.JDialog implements SideTreeNodeListExpandingListener {

    private final CoolMapObject coolMapObject;

    private final Queue<Set<String>> nodeListsToExpand;

    private SideTreeNodeExpandingTask expandingTask;

    private final boolean isToRow;

    private boolean isExpanding;

    /**
     * Creates new form ExpandingControlDialog
     *
     * @param parent
     * @param modal
     * @param isToRow
     * @param nodeLists
     * @param coolMapObject
     */
    public ExpandingControlDialog(java.awt.Frame parent, boolean modal, boolean isToRow, List<Set<String>> nodeLists, CoolMapObject coolMapObject) {
        super(parent, modal);

        this.isToRow = isToRow;

        this.coolMapObject = coolMapObject;

        nodeListsToExpand = new LinkedList<>();
        for (Set<String> nodeList : nodeLists) {
            nodeListsToExpand.add(nodeList);
        }

        this.isExpanding = false;

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                if (expandingTask != null) {
                    expandingTask.stop();
                }
            }

        });

        initComponents();

        setLocation(parent.getSize().width / 2, parent.getSize().height / 2);

        if (!nodeListsToExpand.isEmpty()) {
            startExpandingNextList();
        } else {
            dispose();
        }
    }
    
    public boolean isExpanding() {
        return isExpanding;
    }

    private synchronized void setIsExpanding(boolean isExpanding) {
        this.isExpanding = isExpanding;

        if (isExpanding) {
            startAndPauseButton.setText("Pause Expanding");
            previousStateButton.setVisible(false);
            nextStateButton.setVisible(false);
            expandingStatus.setText("Expanding");
            setVisible(false);
            setModal(true);
            setVisible(true);
        } else {
            startAndPauseButton.setText("Resume Expanding");
            previousStateButton.setVisible(true);
            nextStateButton.setVisible(true);
            expandingStatus.setText("Paused");
            setModalityType(Dialog.ModalityType.MODELESS);
            setVisible(false);
            setModal(false);
            setVisible(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        expandingNodeList = new javax.swing.JList();
        startAndPauseButton = new javax.swing.JButton();
        nextStateButton = new javax.swing.JButton();
        previousStateButton = new javax.swing.JButton();
        expandingStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Expanding Control");

        expandingNodeList.setBorder(javax.swing.BorderFactory.createTitledBorder("Current node list"));
        jScrollPane1.setViewportView(expandingNodeList);

        startAndPauseButton.setText("Start/Pause");
        startAndPauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startAndPauseButtonActionPerformed(evt);
            }
        });

        nextStateButton.setText("Next");
        nextStateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextStateButtonActionPerformed(evt);
            }
        });

        previousStateButton.setText("Previous");
        previousStateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousStateButtonActionPerformed(evt);
            }
        });

        expandingStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        expandingStatus.setText("Not Started");
        expandingStatus.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(expandingStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(startAndPauseButton, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                    .addComponent(previousStateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nextStateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(expandingStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startAndPauseButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previousStateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextStateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startAndPauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startAndPauseButtonActionPerformed
        if (isExpanding) {
            if (expandingTask != null) {
                expandingTask.pause();
                setIsExpanding(false);
            }
        } else {
            if (expandingTask == null) {
                startExpandingNextList();
            } else {
                expandingTask.resume();
                setIsExpanding(true);
            }
        }
    }//GEN-LAST:event_startAndPauseButtonActionPerformed

    private void previousStateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousStateButtonActionPerformed
        if (isExpanding) {
            return;
        }

        if (expandingTask != null) {
            expandingTask.previous();
        }
    }//GEN-LAST:event_previousStateButtonActionPerformed

    private void nextStateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextStateButtonActionPerformed
        if (isExpanding) {
            return;
        }

        if (expandingTask != null) {
            expandingTask.next();
        }
    }//GEN-LAST:event_nextStateButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList expandingNodeList;
    private javax.swing.JLabel expandingStatus;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton nextStateButton;
    private javax.swing.JButton previousStateButton;
    private javax.swing.JButton startAndPauseButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void nodeListFinished(SideTreeExpandingFinishedEvent event) {
        
        if (isToRow) {
            coolMapObject.collapseRowNodes(event.getExpandedNodes(), false);
        } else {
            coolMapObject.collapseColumnNodes(event.getExpandedNodes(), false);
        }
        
        nodeListsToExpand.poll();
        
        if (!nodeListsToExpand.isEmpty()) {
            startExpandingNextList();
        } else {
            JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), "Expanding finished");
            dispose();
        }
    }

    private void updateList(Set<String> nodes) {
        DefaultListModel model = new DefaultListModel();

        for (String node : nodes) {
            model.addElement(node);
        }

        expandingNodeList.setModel(model);
    }

    private void startExpandingNextList() {
        if (!nodeListsToExpand.isEmpty()) {
            Set<String> nextListToExpand = nodeListsToExpand.peek();

            if (startExpanding(coolMapObject, nextListToExpand)) {
                updateList(nextListToExpand);
                setIsExpanding(true);
            } else {
                nodeListsToExpand.poll();
                startExpandingNextList();
            }

        } else {
            expandingTask = null;
            expandingStatus.setText("Expanding finished");
            setIsExpanding(false);
        }
    }

    private boolean startExpanding(CoolMapObject object, Set<String> names) {
        return isToRow ? startRowExpandingTask(object, names) : startColumnExpandingTask(object, names);
    }

    private boolean startRowExpandingTask(CoolMapObject object, Set<String> names) {

        List<VNode> rootNodes;

        Map<VNode, Integer> countedMapping = new HashMap<>();

        List<VNode> allNodes = object.getViewTreeNodesRow();

        List<VNode> returnedNodes;
        if (allNodes.isEmpty()) {
            rootNodes = object.getViewNodesRow();
            returnedNodes = SideTreeUtil.DFSFindRootNodesContainingNameNumber(rootNodes, names, countedMapping);

        } else {
            List<VNode> activeRowNodes = object.getViewNodesRow();

            rootNodes = new LinkedList<>();
            for (VNode node : allNodes) {
                if (node.getParentNode() == null) {
                    rootNodes.add(node);
                }
            }

            SideTreeUtil.DFSFindRootNodesContainingNameNumber(rootNodes, names, countedMapping);

            activeRowNodes.retainAll(countedMapping.keySet());

            returnedNodes = new LinkedList<>();
            for (VNode node : activeRowNodes) {
                if (node.isGroupNode()) {
                    returnedNodes.add(node);
                }
            }
        }

        if (countedMapping.isEmpty()) {
            return false;
        }

        expandingTask = new RowTreeNodeExpandingTaskImpl(returnedNodes, countedMapping, 3000, object);
        expandingTask.addSideTreeNodeListExpandingListener(this);
        expandingTask.start();

        return true;
    }

    private boolean startColumnExpandingTask(CoolMapObject object, Set<String> names) {
        List<VNode> rootNodes;

        Map<VNode, Integer> countedMapping = new HashMap<>();

        List<VNode> allNodes = object.getViewTreeNodesColumn();

        List<VNode> returnedNodes;

        if (allNodes.isEmpty()) {
            rootNodes = object.getViewNodesColumn();
            returnedNodes = SideTreeUtil.DFSFindRootNodesContainingNameNumber(rootNodes, names, countedMapping);

        } else {
            List<VNode> activeColumnNodes = object.getViewNodesColumn();

            rootNodes = new LinkedList<>();
            for (VNode node : allNodes) {
                if (node.getParentNode() == null) {
                    rootNodes.add(node);
                }
            }

            SideTreeUtil.DFSFindRootNodesContainingNameNumber(rootNodes, names, countedMapping);

            activeColumnNodes.retainAll(countedMapping.keySet());

            returnedNodes = new LinkedList<>();
            for (VNode node : activeColumnNodes) {
                if (node.isGroupNode()) {
                    returnedNodes.add(node);
                }
            }
        }

        if (countedMapping.isEmpty()) {
            return false;
        }

        expandingTask = new ColumnTreeNodeExpandingTaskImpl(returnedNodes, countedMapping, 3000, object);
        expandingTask.addSideTreeNodeListExpandingListener(this);
        expandingTask.start();

        return true;
    }
}
