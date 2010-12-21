package sqlwatch4.model;

import sqlwatch4.rebase.com.google.gson.Gson;

import java.util.List;

/**
 * @author dmitry.mamonov
 */
public class TracesSlice {
    private static Gson gson = new Gson();
    List<Trace> traces;

    public List<Trace> getTraces() {
        return traces;
    }

    public void setTraces(List<Trace> traces) {
        this.traces = traces;
    }

    public String toJson(){
        return gson.toJson(this);
    }
}
