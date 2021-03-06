package software.anton.pcep.maps;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import software.anton.pcep.data.KeyedDataPoint;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Anton Rudacov <anton.rudacov @ gmail.com>
 */
public class KeyedDataPointMap implements FlatMapFunction<String, KeyedDataPoint<Double>> {

    @Override
    public void flatMap(String value, Collector<KeyedDataPoint<Double>> out) throws Exception {

        if (isNotBlank(value)) {

            String[] parts = value.split(",");
            out.collect(new KeyedDataPoint<>(parts[0], Double.valueOf(parts[1].trim()), Long.parseLong(parts[2])));
        }
    }
}
