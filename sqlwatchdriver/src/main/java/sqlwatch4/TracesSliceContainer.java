package sqlwatch4;

import sqlwatch4.model.Trace;
import sqlwatch4.model.TracesSlice;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author dmitry.mamonov
 */
public class TracesSliceContainer implements TraceDispatcher.Listener {
    private static class Holder {
        private static TracesSliceContainer INSTANCE = new TracesSliceContainer();
    }

    private TracesSliceContainer() {
    }

    public static TracesSliceContainer get() {
        return Holder.INSTANCE;
    }

    private BlockingQueue<Trace> runningTraces = new ArrayBlockingQueue<Trace>(Config.get().getMaximumTracesToKeep());

    @Override
    public void takeTrace(Trace trace) {
        eliminateOverflow();
        runningTraces.add(trace);
    }

    public TracesSlice cutSlice() {
        TracesSlice result = new TracesSlice();
        int runningSize = runningTraces.size();
        result.setTraces(new ArrayList<Trace>(runningSize));
        try {
            while (runningSize > 0) {
                Trace trace = runningTraces.take();
                if (trace != null) {
                    result.getTraces().add(trace);
                    runningSize -= 1;
                } else {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();  //TODO [DM] handle exceptions properly
        }
        return result;
    }


    private void eliminateOverflow() {
        try {
            while (runningTraces.remainingCapacity() == 0) {
                runningTraces.take();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();  //TODO [DM] handle this.
        }
    }
}
