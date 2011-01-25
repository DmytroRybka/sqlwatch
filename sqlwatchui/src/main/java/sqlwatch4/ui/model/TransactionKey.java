package sqlwatch4.ui.model;

import com.google.common.base.Preconditions;

/**
 * @author dmitry.mamonov
 */

public class TransactionKey {
    int connectionNumber;

    public TransactionKey(int connectionNumber) {
        this.connectionNumber = connectionNumber;
    }

    public TransactionKey(UITrace trace) {
        this(Preconditions.checkNotNull(trace.getConnectionNumber()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionKey that = (TransactionKey) o;

        if (connectionNumber != that.connectionNumber) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return connectionNumber;
    }

    @Override
    public String toString() {
        return "TransactionKey{" +
                "connectionNumber=" + connectionNumber +
                '}';
    }
}

