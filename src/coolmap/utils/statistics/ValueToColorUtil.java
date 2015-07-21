package coolmap.utils.statistics;

import java.awt.Color;

/**
 *
 * @author Keqiang Li
 */
public class ValueToColorUtil {

    private final double min;
    private final double max;
    private final double factor;

    public ValueToColorUtil(double min, double max) {
        this.min = min;
        this.max = max;
        this.factor = 255 / (max - min);
    }

    public double convertValue(double origin) {
        return origin * factor;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public Color convertToColor(double value) {
        Double convertedValue = convertValue(value - min);
        Color color = new Color(convertedValue.intValue(), 0, 0);
        return color;
    }
}
