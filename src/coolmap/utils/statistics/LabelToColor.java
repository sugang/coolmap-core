package coolmap.utils.statistics;

import java.awt.Color;
import java.util.Map;

/**
 *
 * @author Keqiang Li
 */
public class LabelToColor {

    private final Map<String, Double> labelToValue;
    private final ValueToColorUtil valueToColorUtil;

    public LabelToColor(Map<String, Double> labelToValue, double min, double max) {
        this.labelToValue = labelToValue;
        valueToColorUtil = new ValueToColorUtil(min, max);
    }

    public Color getLabelColor(String label) {
        return valueToColorUtil.convertToColor(labelToValue.get(label));
    }

    public boolean containsLabel(String label) {
        return labelToValue.containsKey(label);
    }

    public double getValue(String label) {
        return labelToValue.get(label);
    }

    public Color getColor(double value) {
        return valueToColorUtil.convertToColor(value);
    }
    
    public Map<String, Double> getLabelToValue() {
        return labelToValue;
    }
}
