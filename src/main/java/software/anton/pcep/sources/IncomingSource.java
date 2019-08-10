package software.anton.pcep.sources;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Objects;

import static java.util.stream.Collectors.toCollection;

/**
 * @author Anton Rudacov <anton.rudacov @ gmail.com>
 */
public class IncomingSource extends RichSourceFunction<String> {

    private static final String DATASET = "src/main/resources/dataset/CalIt2.data";

    private long period;
    private boolean isRunning;
    private LinkedList<String> lines;

    public IncomingSource(long period) {
        this.period = period;
        this.isRunning = true;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        lines = Files.lines(Paths.get(DATASET))
                .filter(line -> line.charAt(0) == '9')
                .collect(toCollection(LinkedList::new));
    }

    @Override
    public void run(SourceContext<String> ctx) throws Exception {

        while (isRunning) {
            Thread.sleep(period);

            String[] parts = Objects.requireNonNull(lines.pollFirst()).split(",");
            long timeStamp = System.currentTimeMillis();
            String line = "in," + parts[parts.length - 1] + "," + timeStamp;

            ctx.collectWithTimestamp(line, timeStamp);
            ctx.emitWatermark(new Watermark(timeStamp));
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
