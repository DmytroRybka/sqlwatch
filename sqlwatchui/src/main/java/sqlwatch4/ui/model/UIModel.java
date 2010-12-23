package sqlwatch4.ui.model;

import sqlwatch4.model.Trace;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dmitry.mamonov
 */
public class UIModel {
    List<Slice> slices = new ArrayList<Slice>();
    List<Trace> traces = new ArrayList<Trace>();

    //public org.apache.pivot.collections.List<Trace>

    public class Slice {

    }

    public void insert(List<Trace> newTraces){
        traces.addAll(newTraces);
    }
}
