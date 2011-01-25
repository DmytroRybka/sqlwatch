package sqlwatch4.ui.model;

import com.google.inject.internal.Preconditions;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import sqlwatch4.model.Trace;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author dmitry.mamonov
 */

public class StatByTransaction {
    /**
     * Note: Yes, I know it is not thread-safe to use SimpleDateFormatter but it is used only
     * in UI which is single-threaded, so it should be fine.
     */
    //TODO [DM] merge logic.
    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("[HH:mm:ss:SSS]");

    final long thread;
    final int connectionNumber;
    final long startTime;
    long totalDuration = 0;
    long finishTime;
    int queriesCount =0;
    String resolution = "Default";
    final List<UITrace> traces = new ArrayList<UITrace>();

    public StatByTransaction(UITrace trace) {
        super();
        this.thread = trace.getThread();
        this.connectionNumber = trace.getConnectionNumber();
        this.startTime = trace.getWhen();
        this.finishTime = this.startTime;
        add(trace);
    }

    public void add(UITrace trace) {
        Preconditions.checkNotNull(trace);
        Preconditions.checkArgument(trace.getThread() == thread);
        Preconditions.checkNotNull(trace.getConnectionNumber());
        Preconditions.checkArgument(trace.getConnectionNumber() == connectionNumber);
        this.finishTime = Math.max(this.finishTime, trace.getWhen());
        if (trace.getExecTime()!=null){
            this.queriesCount++;
            this.totalDuration += trace.getExecTime();
        }
        if (trace.getKind()== Trace.Kind.MethodReturned){
            resolution = trace.getMethodCall();
        }
        traces.add(trace);
    }

    public int getConnectionNumber() {
        return connectionNumber;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public String getFinishTimeStr() {
        return timeFormatter.format(new Date(finishTime));
    }

    public long getStartTime() {
        return startTime;
    }

    public String getStartTimeStr() {
        return timeFormatter.format(new Date(startTime));
    }

    public long getThread() {
        return thread;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public String getTotalDurationStr() {
        return String.format("%6.1fms", (double) totalDuration / 1000000.0);
    }

    public List<UITrace> getTraces() {
        return traces;
    }

    public int getQueriesCount() {
        return queriesCount;
    }

    public String getResolution(){
        return resolution;
    }
}
