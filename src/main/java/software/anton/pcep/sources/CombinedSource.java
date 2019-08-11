package software.anton.pcep.sources;

import static software.anton.pcep.configs.Configuration.*;

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
public class CombinedSource extends RichSourceFunction<String> {

    private long rate;
    private boolean isRunning;
    private LinkedList<String> linesIn;
    private LinkedList<String> linesOut;

    public CombinedSource(long rate) {
        this.rate = rate;
        this.isRunning = true;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        linesIn = Files.lines(Paths.get(DATASET))
                .filter(line -> line.charAt(0) == '9')
                .collect(toCollection(LinkedList::new));

        linesOut = Files.lines(Paths.get(DATASET))
                .filter(line -> line.charAt(0) == '7')
                .collect(toCollection(LinkedList::new));
    }

    @Override
    public void run(SourceContext<String> ctx) throws Exception {

        while (isRunning) {
            Thread.sleep(rate);

            String[] partsIn = Objects.requireNonNull(linesIn.pollFirst()).split(",");
            String[] partsOut = Objects.requireNonNull(linesOut.pollFirst()).split(",");

            long timeStamp = System.currentTimeMillis();
            String lineIn = "in," + partsIn[partsIn.length - 1] + "," + timeStamp;
            String lineOut = "out," + partsOut[partsOut.length - 1] + "," + timeStamp;

            ctx.collectWithTimestamp(lineIn, timeStamp);
            ctx.collectWithTimestamp(lineOut, timeStamp);
            ctx.emitWatermark(new Watermark(timeStamp));
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}