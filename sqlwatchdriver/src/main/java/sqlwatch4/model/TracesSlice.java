package sqlwatch4.model;

import java.util.List;

/**
 * @author dmitry.mamonov
 */
public class TracesSlice {
    List<Trace> traces;

    public List<Trace> getTraces() {
        return traces;
    }

    public void setTraces(List<Trace> traces) {
        this.traces = traces;
    }
}
