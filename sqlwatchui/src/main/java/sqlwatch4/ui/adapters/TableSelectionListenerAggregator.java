package sqlwatch4.ui.adapters;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;

/**
 * @author dmitry.mamonov
 */

public abstract class TableSelectionListenerAggregator implements TableViewSelectionListener {
    @Override
    public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
        onSelect(tableView);
    }

    @Override
    public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
        onSelect(tableView);
    }

    @Override
    public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
        onSelect(tableView);
    }

    protected abstract void onSelect(TableView tableView);
}
