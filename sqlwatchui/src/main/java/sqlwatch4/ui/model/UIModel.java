package sqlwatch4.ui.model;

import com.google.inject.internal.Preconditions;
import org.apache.pivot.collections.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * @author dmitry.mamonov
 */
public class UIModel {
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    org.apache.pivot.collections.List<Slice> slices = new org.apache.pivot.collections.ArrayList<Slice>();
    org.apache.pivot.collections.List<UITrace> traces = new org.apache.pivot.collections.ArrayList<UITrace>();
    Set<Slice> selectedSlices = new LinkedHashSet<Slice>();

    public org.apache.pivot.collections.List<Slice> getSlices() {
        return slices;
    }

    public org.apache.pivot.collections.List<UITrace> getTraces() {
        return traces;
    }

    public void setSelectedSlices(Sequence<Slice> nowSelectedSlices) {
        for (Slice slice : selectedSlices) {
            slice.setSelected(false);
        }
        selectedSlices.clear();
        final int length = nowSelectedSlices.getLength();
        for (int i = 0; i < length; i++) {
            Slice slice = nowSelectedSlices.get(i);
            slice.setSelected(true);
            selectedSlices.add(slice);
        }
        traceInit();
    }

    protected void traceInit() {
        traces.clear();
        for (Slice slice : selectedSlices) {
            for (UITrace trace : slice.getTraces()) {
                traces.add(trace);
            }
        }
        System.out.println("Traces view reinitizlied");
    }

    protected void tracesAdd(UITrace trace) {
        traces.add(trace);
    }

    public class Slice {
        boolean selected = false;
        List<UITrace> traces = new ArrayList<UITrace>();
        List<UITrace> newlyAddedTraces = new ArrayList<UITrace>();

        public Slice(UITrace initialTrace) {
            update(initialTrace);

        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public List<UITrace> getTraces() {
            return traces;
        }

        @SuppressWarnings({"PointlessBooleanExpression"})
        private void refreshAfterUpdate() {
            slices.update(slices.indexOf(this), this);
            if (newlyAddedTraces.isEmpty() == false) {
                if (isSelected()) {
                    for (UITrace newTrace : newlyAddedTraces) {
                        tracesAdd(newTrace);
                    }
                }
                newlyAddedTraces.clear();
            }
        }

        public void update(UITrace newTrace) {
            Preconditions.checkNotNull(newTrace);
            traces.add(newTrace);
            if (isSelected()) {
                newlyAddedTraces.add(newTrace);
            }
        }

        public UITrace getLatestTrace() {
            return traces.get(traces.size() - 1);
        }

        public UITrace getFirstTraceOrNull() {
            return traces.get(0);
        }

        @Override
        public String toString() {
            UITrace first = getFirstTraceOrNull();
            UITrace last = getLatestTrace();
            return timeFormat.format(new Date(first.getWhen())) + " .. " +
                    timeFormat.format(new Date(last.getWhen())) + String.format(" [queries=%d] %6dms", traces.size(), last.getWhen() - first.getWhen());
        }
    }

    protected Slice getLatestSlice() {
        if (slices.getLength() == 0) {
            return null;
        }
        return slices.get(slices.getLength() - 1);
    }

    protected Slice cutNewSlice(UITrace initialTrace) {
        Slice latest = getLatestSlice();
        if (latest != null) {
            latest.refreshAfterUpdate();
        }
        System.out.println("cut new slice");
        Slice newSlice = new Slice(initialTrace);
        slices.add(newSlice);
        return newSlice;
    }

    protected boolean isCutNewSlice(UITrace prev, UITrace next) {
        if (prev != null && next != null) {
            if (next.getWhen() - prev.getWhen() > 3000L) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"PointlessBooleanExpression"})
    public void insert(List<UITrace> newTraces) {
        if (newTraces.isEmpty() == false) {
            Slice latest = getLatestSlice();
            UITrace prev = latest != null ? latest.getLatestTrace() : null;
            for (UITrace next : newTraces) {
                UITrace.Kind kind = next != null ? next.getKind() : null;
                if (kind != null && kind.isUi()) {
                    if (isCutNewSlice(prev, next) || latest == null) {
                        latest = cutNewSlice(next);
                    } else {
                        latest.update(next);
                    }
                    prev = next;
                }
            }
            if (latest != null) {
                latest.refreshAfterUpdate();
            }
            System.out.println("added: " + newTraces.size());
        }
    }
}
