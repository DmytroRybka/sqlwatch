package sqlwatch3.tool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import sqlwatch3.face.SQLListener;
import sqlwatch3.face.SQLListener.QueryType;
import sqlwatch3.face.WatchCallback;

/**
 * Монитор изменений в базе данных, отслеживает области данных по SQL запросам,
 * как только результат запроса изменяется - вызывает соответствующего callback
 * слушателя.
 * 
 * @author dmitry.mamonov
 */
public class ChangesMonitor extends SQLListener.Adapter {
    Connection connection = null;
    Set<WatchCallback> watches = new LinkedHashSet<WatchCallback>();
    Map<String, DataContainer> watchesMap = new ConcurrentHashMap();

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void addWatch(WatchCallback watchCallback) {
        watches.add(watchCallback);
        refreshCurrentState();
    }

    @Override
    public void update(String sql, double millis, QueryType type) {
        for (WatchCallback watch : watches) {
            String watchQuery = watch.getWatchQuery();
            if (watchQuery != null) {
                DataContainer lastResult = watchesMap.get(watchQuery);
                DataContainer currentResult = collectDataFromQuery(watchQuery);
                if (currentResult.equals(lastResult)==false){
                    watch.catchChange(watchQuery, sql, null);
                    watchesMap.put(watchQuery, currentResult);
                }
            }
        }
    }

    protected DataContainer collectDataFromQuery(String query) {
        DataContainer result = new DataContainer();
        if (connection != null) {
            try {
                //force connection to read uncommitted data to monitor changes within transaction
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                //create statement
                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    ResultSet rs = null;
                    try {
                        rs = stmt.executeQuery(query);
                        result.addFromResultSet(rs);
                    } finally {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                } finally {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void refreshCurrentState() {
        for (WatchCallback watch : watches) {
            String watchQuery = watch.getWatchQuery();
            if (watchQuery != null) {
                DataContainer currentResult = collectDataFromQuery(watchQuery);
                watchesMap.put(watchQuery, currentResult);
            }
        }
    }

    protected static class DataContainer extends ArrayList<DataCountainerItem> {
        public DataContainer() {
            super();
        }

        public void addFromResultSet(ResultSet data) throws SQLException {
            int columnCount = data.getMetaData().getColumnCount();
            while (data.next() == true) {
                DataCountainerItem row = new DataCountainerItem(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    row.set(i - 1, data.getObject(i));
                }
                add(row);
            }
        }
    }

    protected static class DataCountainerItem {
        private Object[] cells;

        public DataCountainerItem(int cellsCount) {
            cells = new Object[cellsCount];
        }

        public Object get(int index) {
            return cells[index];
        }

        public void set(int index, Object value) {
            cells[index] = value;
        }

        public int length() {
            return cells.length;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DataCountainerItem other = (DataCountainerItem) obj;
            if (!Arrays.deepEquals(this.cells, other.cells)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Arrays.deepHashCode(this.cells);
            return hash;
        }
    }
}
