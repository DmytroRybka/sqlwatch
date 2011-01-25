package sqlwatch4.ui.model;

import sqlwatch4.model.Trace;
import sqlwatch4.rebase.com.google.gson.Gson;

import java.util.List;

/**
 * @author dmitry.mamonov
 */

public class UITracesSlice {
    private static Gson gson = new Gson();
    List<UITrace> traces;

    public List<UITrace> getTraces() {
        return traces;
    }
}
