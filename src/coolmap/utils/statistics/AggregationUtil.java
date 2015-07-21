package coolmap.utils.statistics;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Keqiang Li
 */
public class AggregationUtil {
    
    public static enum AggregationType {
        MAX,
        MIN,
        MEAN,
        MEDIAN
    }
    
    public static Double getMean(Collection<Double> values) {
        if (values.isEmpty()) {
            return null;
        }

        return getSum(values) / values.size();
    }

    public static Double getSum(Collection<Double> values) {
        if (values.isEmpty()) {
            return null;
        }

        Double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }

        return sum;
    }

    public static Double getMax(Collection<Double> values) {
        if (values.isEmpty()) {
            return null;
        }

        Double max = Double.MIN_VALUE;
        for (Double value : values) {
            if (value > max) {
                max = value;
            }
        }

        return max;
    }

    public static Double getMin(Collection<Double> values) {
        if (values.isEmpty()) {
            return null;
        }

        Double min = Double.MAX_VALUE;
        for (Double value : values) {
            if (value < min) {
                min = value;
            }
        }

        return min;
    }

    public static Double getMedian(List<Double> values) {
        Collections.sort(values);
        if (values.isEmpty()) {
            return null;
        }
        
        int middle = values.size() / 2;
        if (values.size() % 2 == 1) {
            return values.get(middle);
        } else {
            return (values.get(middle - 1) + values.get(middle)) / 2.0;
        }
    }
    
    public static double doAggregation(List<Double> values, AggregationType aggregationType) {
        switch (aggregationType) {
            case MAX:
                return AggregationUtil.getMax(values);
            case MIN:
                return AggregationUtil.getMin(values);
            case MEAN:
                return AggregationUtil.getMean(values);
            case MEDIAN:
                return AggregationUtil.getMedian(values);
            default:
                return values.get(0);
        }
    }
    
}


