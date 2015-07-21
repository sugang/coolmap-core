package coolmap.canvas.sidemaps.impl;

import coolmap.application.CoolMapMaster;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.misc.MatrixCell;
import coolmap.canvas.misc.ZoomControl;
import coolmap.canvas.sidemaps.ColumnMap;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.Tools;
import coolmap.utils.graphics.CAnimator;
import coolmap.utils.graphics.UI;
import coolmap.utils.statistics.AggregationUtil;
import coolmap.utils.statistics.LabelToColor;
import coolmap.utils.statistics.MatrixUtil;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;

/**
 *
 * @author Keqiang Li
 */
public class ColumnHighlightor extends ColumnMap<Object, Object> implements MouseMotionListener {

    private final HoverTarget _hoverTarget = new HoverTarget();
    private final Animator _hoverAnimator = CAnimator.createInstance(_hoverTarget, 150);

    private final Rectangle _activeRectangle = new Rectangle();

    private AggregationUtil.AggregationType aggregationType;

    private final JPopupMenu _menu = new JPopupMenu();

    private final ZoomControl _zoomControlX;

    private LabelToColor labelToColor;

    @Override
    public String getName() {
        return "Column Highlightor";
    }

    public void setAggregationType(AggregationUtil.AggregationType type) {
        this.aggregationType = type;
        CoolMapMaster.getActiveCoolMapObject().getCoolMapView().updateColumnMapBuffersEnforceAll();
    }

    public ColumnHighlightor(final CoolMapObject object, LabelToColor labelToColor) {
        super(object);
        this.labelToColor = labelToColor;

        this.aggregationType = AggregationUtil.AggregationType.MAX;

        _zoomControlX = getCoolMapView().getZoomControlX();
        initPopupMenu();
        
        getViewPanel().addMouseMotionListener(this);
    }

    private void initPopupMenu() {
        JMenu setAggregationTypeMenu = new JMenu("Set Aggregation Type");
        _menu.add(setAggregationTypeMenu);

        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem maxItem = new JRadioButtonMenuItem("Max");
        maxItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setAggregationType(AggregationUtil.AggregationType.MAX);
            }
        });
        maxItem.setSelected(true);
        group.add(maxItem);
        setAggregationTypeMenu.add(maxItem);

        JRadioButtonMenuItem minItem = new JRadioButtonMenuItem("Min");
        minItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setAggregationType(AggregationUtil.AggregationType.MIN);
            }
        });
        group.add(minItem);
        setAggregationTypeMenu.add(minItem);

        JRadioButtonMenuItem meanItem = new JRadioButtonMenuItem("Mean");
        meanItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setAggregationType(AggregationUtil.AggregationType.MEAN);
            }
        });
        group.add(meanItem);
        setAggregationTypeMenu.add(meanItem);

        JRadioButtonMenuItem medianItem = new JRadioButtonMenuItem("Median");
        medianItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setAggregationType(AggregationUtil.AggregationType.MEDIAN);
            }
        });
        group.add(medianItem);
        setAggregationTypeMenu.add(medianItem);

        getViewPanel().setComponentPopupMenu(_menu);
    }

    public void setLabelToColor(LabelToColor labelToColor) {
        this.labelToColor = labelToColor;
    }

    @Override
    protected void renderColumn(Graphics2D g2D, CoolMapObject<Object, Object> object, VNode node, int anchorX, int anchorY, int cellWidth, int cellHeight) {
        if (node == null || getCoolMapView() == null || node.getCurrentViewMultiplier() == 0f) {
            return;
        }

        String label = node.getViewLabel();
        if (label != null) {

            Color color;
            double value;

            if (node.isGroupNode()) {
                CMatrix matrix = (CMatrix) CoolMapMaster.getActiveCoolMapObject().getBaseCMatrices().get(0);
                if (matrix == null) {
                    return;
                }

                List<Object> leafNames = MatrixUtil.getColumnLeafNodeNames(node, matrix);
                List<Double> allLeafValues = new ArrayList<>();

                for (Object name : leafNames) {
                    String leafName = (String) name;
                    if (!labelToColor.containsLabel(leafName)) {
                        continue; // doesn't have this leaf
                    }
                    allLeafValues.add(labelToColor.getValue(leafName));
                }

                if (allLeafValues.isEmpty()) {
                    return;
                }

                value = AggregationUtil.doAggregation(allLeafValues, aggregationType);
                color = labelToColor.getColor(value);
            } else {

                if (!labelToColor.containsLabel(label)) {
                    return;
                }

                value = labelToColor.getValue(label);
                color = labelToColor.getLabelColor(label);
            }

            g2D.setColor(color);

            if (cellWidth > 6) {
                g2D.setColor(color);
                g2D.fillRoundRect(anchorX + 1, anchorY + cellHeight - 5, cellWidth - 2, 10, 4, 4);
            } else {
                g2D.setColor(color);
                g2D.fillRect(anchorX, anchorY + cellHeight - 5, cellWidth, 10);
            }

            BufferedImage image = Tools.createStringImage(g2D, "" + String.format("%.2f", value));
            g2D.rotate(-Math.PI / 2);
            g2D.drawImage(image, null, -anchorY - cellHeight + 8, anchorX);
            g2D.rotate(Math.PI / 2);
        }
    }

    @Override
    public void prePaint(Graphics2D g2D, CoolMapObject<Object, Object> object, int width, int height) {

        CoolMapView canvas = getCoolMapView();

        if (isEnabled() && canvas != null) {
            if (canvas.getActiveCell().isColValidCell(canvas.getCoolMapObject())) {
                _activeRectangle.height = height;
                g2D.setColor(UI.colorLightGreen5);
                g2D.fillRect(_activeRectangle.x, _activeRectangle.y, _activeRectangle.width, _activeRectangle.height);
            }
        }
    }

    @Override
    public void aggregatorUpdated(CoolMapObject object) {
    }

    @Override
    public void rowsChanged(CoolMapObject object) {
    }

    @Override
    public void columnsChanged(CoolMapObject object) {
    }

    @Override
    public void coolMapObjectBaseMatrixChanged(CoolMapObject object) {
    }

    @Override
    public void mapAnchorMoved(CoolMapObject object) {
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
    }

    @Override
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    @Override
    public void gridChanged(CoolMapObject object) {
    }

    @Override
    public void nameChanged(CoolMapObject object) {
    }

    @Override
    public void postPaint(Graphics2D g2D, CoolMapObject<Object, Object> obj, int width, int height) {

    }

    @Override
    public boolean canRender(CoolMapObject coolMapObject) {
        return true;
    }

    @Override
    public JComponent getConfigUI() {
        return null;
    }

    @Override
    public void justifyView() {
        _updateRectangle(getCoolMapView().getActiveCell());
        getViewPanel().repaint();
    }

    @Override
    public void activeCellChanged(CoolMapObject obj, MatrixCell oldCell, MatrixCell newCell) {
        if (_hoverAnimator.isRunning()) {
            _hoverAnimator.cancel();
        }
        _hoverTarget.setBeginEnd(oldCell, newCell);
        _hoverAnimator.start();
    }

    private void _updateRectangle(MatrixCell activeCell) {
        CoolMapView view = getCoolMapView();
        if (view == null || activeCell == null || activeCell.getCol() == null) {
            return;
        }

        VNode node = getCoolMapView().getCoolMapObject().getViewNodeColumn(activeCell.getCol());

        if (node == null || node.getViewOffset() == null) {
            return;
        }
        _activeRectangle.x = (int) (getCoolMapView().getMapAnchor().x + node.getViewOffset());
        _activeRectangle.y = 0;
        _activeRectangle.width = (int) node.getViewSizeInMap(view.getZoomX());
    }

    @Override
    public void selectionChanged(CoolMapObject obj) {
    }

    @Override
    protected void prepareRender(Graphics2D g2D) {
        g2D.setFont(_zoomControlX.getBoldFont());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        CoolMapView view = getCoolMapView();
        if (view != null) {
            Integer col = view.getCurrentCol(e.getX());
            MatrixCell oldCell = view.getActiveCell();
            MatrixCell newCell = new MatrixCell(oldCell.getRow(), col);
            if (!newCell.valueEquals(oldCell)) {
                view.setActiveCell(view.getActiveCell(), newCell);
            }
        }
    }

    private class HoverTarget implements TimingTarget {

        private final Rectangle _beginRect = new Rectangle();
        private final Rectangle _endRect = new Rectangle();

        public void setBeginEnd(MatrixCell oldCell, MatrixCell newCell) {

            //System.out.println(oldCell + " " + newCell);
            CoolMapView view = getCoolMapView();
            if (view == null) { // || oldCell == null || oldCell.getCol() == null || newCell == null || newCell.getCol() == null) {
                return;
            }

            if (oldCell == null || newCell == null) {
                return;
            }

            if (newCell.col == null) {
                return;
            }

            if (oldCell.col == null && newCell.col != null) {
                oldCell.col = newCell.col;
            }

            VNode oldNode = getCoolMapView().getCoolMapObject().getViewNodeColumn(oldCell.getCol());
            VNode newNode = getCoolMapView().getCoolMapObject().getViewNodeColumn(newCell.getCol());

            if (oldNode == null || oldNode.getViewOffset() == null || newNode == null || newNode.getViewOffset() == null) {
                return;
            }

            _beginRect.x = (int) (getCoolMapView().getMapAnchor().x + oldNode.getViewOffset());
            _beginRect.y = 0;
            _beginRect.width = (int) oldNode.getViewSizeInMap(view.getZoomX());

            _endRect.x = (int) (getCoolMapView().getMapAnchor().x + newNode.getViewOffset());
            _endRect.y = 0;
            _endRect.width = (int) newNode.getViewSizeInMap(view.getZoomX());

            getViewPanel().repaint();
        }

        @Override
        public void begin(Animator source) {
        }

        @Override
        public void end(Animator source) {
        }

        @Override
        public void repeat(Animator source) {
        }

        @Override
        public void reverse(Animator source) {
        }

        @Override
        public void timingEvent(Animator source, double fraction) {
            _activeRectangle.x = (int) (_beginRect.x + (_endRect.x - _beginRect.x) * fraction);
            _activeRectangle.y = 0;
            _activeRectangle.width = (int) (_beginRect.width + (_endRect.width - _beginRect.width) * fraction);
            getViewPanel().repaint();
        }
    }

}
