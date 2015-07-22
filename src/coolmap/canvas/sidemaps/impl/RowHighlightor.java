package coolmap.canvas.sidemaps.impl;

import coolmap.application.CoolMapMaster;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.misc.MatrixCell;
import coolmap.canvas.misc.ZoomControl;
import coolmap.canvas.sidemaps.RowMap;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.Tools;
import coolmap.utils.graphics.CAnimator;
import coolmap.utils.graphics.UI;
import coolmap.utils.statistics.AggregationUtil;
import coolmap.utils.statistics.AggregationUtil.AggregationType;
import coolmap.utils.statistics.LabelToColor;
import coolmap.utils.statistics.MatrixUtil;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
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

public class RowHighlightor extends RowMap<Object, Object> implements MouseMotionListener {
    
    private final HoverTarget _hoverTarget = new HoverTarget();
    private final Animator _hoverAnimator = CAnimator.createInstance(_hoverTarget, 150);

    private AggregationType aggregationType;

    private final JPopupMenu _menu = new JPopupMenu();

    private final ZoomControl _zoomControlY;
    private LabelToColor labelToColor;

    @Override
    public String getName() {
        return "Row Highlightor";
    }

    @Override
    public void nameChanged(CoolMapObject object) {
    }

    @Override
    public void justifyView() {
        _updateRectangle(getCoolMapView().getActiveCell());
        getViewPanel().repaint();
    }

    public void setAggregationType(AggregationType type) {
        this.aggregationType = type;
        CoolMapMaster.getActiveCoolMapObject().getCoolMapView().updateRowMapBuffersEnforceAll();
    }

    public RowHighlightor(final CoolMapObject obj, LabelToColor labelToColor) {
        super(obj);
        this.labelToColor = labelToColor;
        this.aggregationType = AggregationType.MAX;

        _zoomControlY = getCoolMapView().getZoomControlY();

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
                setAggregationType(AggregationType.MAX);
            }
        });
        maxItem.setSelected(true);
        group.add(maxItem);
        setAggregationTypeMenu.add(maxItem);

        JRadioButtonMenuItem minItem = new JRadioButtonMenuItem("Min");
        minItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setAggregationType(AggregationType.MIN);
            }
        });
        group.add(minItem);
        setAggregationTypeMenu.add(minItem);

        JRadioButtonMenuItem meanItem = new JRadioButtonMenuItem("Mean");
        meanItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setAggregationType(AggregationType.MEAN);
            }
        });
        group.add(meanItem);
        setAggregationTypeMenu.add(meanItem);

        JRadioButtonMenuItem medianItem = new JRadioButtonMenuItem("Median");
        medianItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setAggregationType(AggregationType.MEDIAN);
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
    public void viewRendererChanged(CoolMapObject object) {
    }

    @Override
    public void viewFilterChanged(CoolMapObject object) {
    }

    @Override
    public void mapZoomChanged(CoolMapObject object) {
    }

    @Override
    public JComponent getConfigUI() {
        return null;
    }

    @Override
    protected void prePaint(Graphics2D g2D, CoolMapObject<Object, Object> obj, int width, int height) {
        if (!isDataViewValid()) {
            return;
        }

        CoolMapView canvas = getCoolMapView();
        
        if (isEnabled() && canvas != null) {
            if (canvas.getActiveCell().isRowValidCell(canvas.getCoolMapObject())) {
                _activeRectangle.width = width;
                g2D.setColor(UI.colorLightGreen5);
                g2D.fillRect(_activeRectangle.x, _activeRectangle.y, _activeRectangle.width, _activeRectangle.height);
            }
        }
    }

    @Override
    protected void prepareRender(Graphics2D g2D) {
        g2D.setFont(_zoomControlY.getBoldFont());
    }

    @Override
    protected void postPaint(Graphics2D g2D, CoolMapObject<Object, Object> canvas, int width, int height) {
    }

    @Override
    public boolean canRender(CoolMapObject coolMapObject) {
        return true;
    }

    @Override
    protected void renderRow(Graphics2D g2D, CoolMapObject<Object, Object> object, VNode node, int anchorX, int anchorY, int cellWidth, int cellHeight) {
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

                List<Object> leafNames = MatrixUtil.getRowLeafNodeNames(node, matrix);
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

            if (cellHeight > 6) {
                g2D.fillRoundRect(anchorX - 4, anchorY + 1, 10, cellHeight - 2, 4, 4);
            } else {
                g2D.fillRect(anchorX - 4, anchorY, 10, cellHeight);
            }

            BufferedImage image = Tools.createStringImage(g2D, "" + String.format("%.2f", value));

            g2D.drawImage(image, null, anchorX + 10, anchorY);

        }
    }

    @Override
    public void activeCellChanged(CoolMapObject obj, MatrixCell oldCell, MatrixCell newCell) {
        if (_hoverAnimator.isRunning()) {
            _hoverAnimator.cancel();
        }
        _hoverTarget.setBeginEnd(oldCell, newCell);
        _hoverAnimator.start();
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
    public void gridChanged(CoolMapObject object) {
    }

    @Override
    public void selectionChanged(CoolMapObject obj) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        CoolMapView view = getCoolMapView();
        if (view != null) {
            Point mouse = translateToCanvas(e.getX(), e.getY());

            Integer row = view.getCurrentRow(mouse.y);
            MatrixCell oldCell = view.getActiveCell();
            MatrixCell newCell = new MatrixCell(row, oldCell.getCol());
            if (!newCell.valueEquals(oldCell)) {
                view.setActiveCell(view.getActiveCell(), newCell);
            }
        }
    }

    private class HoverTarget implements TimingTarget {

        private final Rectangle _beginRect = new Rectangle();
        private final Rectangle _endRect = new Rectangle();

        public void setBeginEnd(MatrixCell oldCell, MatrixCell newCell) {

            CoolMapView view = getCoolMapView();
//            if (view == null || oldCell == null || oldCell.getRow() == null || newCell == null || newCell.getRow() == null) {
//                return;
//            }
            if (view == null) { // || oldCell == null || oldCell.getCol() == null || newCell == null || newCell.getCol() == null) {
                return;
            }

            if (oldCell == null || newCell == null) {
                return;
            }

            if (newCell.row == null) {
                return;
            }

            if (oldCell.row == null && newCell.row != null) {
                oldCell.row = newCell.row;
            }

            VNode oldNode = getCoolMapView().getCoolMapObject().getViewNodeRow(oldCell.getRow());
            VNode newNode = getCoolMapView().getCoolMapObject().getViewNodeRow(newCell.getRow());

            if (oldNode == null || oldNode.getViewOffset() == null || newNode == null || newNode.getViewOffset() == null) {
                return;
            }

            _beginRect.x = 0;
            _beginRect.y = (int) (getCoolMapView().getMapAnchor().y + oldNode.getViewOffset() - getAnchorY());
            _beginRect.height = (int) oldNode.getViewSizeInMap(view.getZoomY());

            _endRect.x = 0;
            _endRect.y = (int) (getCoolMapView().getMapAnchor().y + newNode.getViewOffset() - getAnchorY());
            _endRect.height = (int) newNode.getViewSizeInMap(view.getZoomY());
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
            _activeRectangle.x = 0;
            _activeRectangle.y = (int) (_beginRect.y + (_endRect.y - _beginRect.y) * fraction);
            _activeRectangle.height = (int) (_beginRect.height + (_endRect.height - _beginRect.height) * fraction);
            getViewPanel().repaint();
        }
        
    }
    private final Rectangle _activeRectangle = new Rectangle();

    private void _updateRectangle(MatrixCell activeCell) {
        CoolMapView view = getCoolMapView();
        if (view == null || activeCell == null || activeCell.getRow() == null) {
            return;
        }

        VNode node = getCoolMapView().getCoolMapObject().getViewNodeRow(activeCell.getRow());

        if (node == null || node.getViewOffset() == null) {
            return;
        }
        _activeRectangle.x = 0; //(int) (getCoolMapView().getMapAnchor().x + node.getViewOffset());
        _activeRectangle.y = (int) (getCoolMapView().getMapAnchor().y + node.getViewOffset() - getAnchorY());
        _activeRectangle.height = (int) node.getViewSizeInMap(view.getZoomY());
    }
}

