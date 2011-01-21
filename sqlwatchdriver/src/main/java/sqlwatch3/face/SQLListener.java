package sqlwatch3.face;

/**
 * Интерфейс слушателя SQL запросов поступающих от JDBC драйвера.
 *
 * Запросы разделены по типам.
 * 
 * @author dmitry.mamonov
 */
public interface SQLListener {
    void update(String sql, double millis, QueryType type);

    void select(String sql, double millis);

    void call(String sql, double millis);

    void ddl(String sql, double millis);

    void other(String sql, double millis);

    public static class Adapter implements SQLListener {
        public void update(String sql, double millis, QueryType type) {
            //pass
        }

        public void select(String sql, double millis) {
            //pass
        }

        public void call(String sql, double millis) {
            //pass
        }

        public void ddl(String sql, double millis) {
            //pass
        }

        public void other(String sql, double millis) {
            //pass
        }
    }
    enum QueryType {
        Select,
        Insert,
        Update,
        Delete,
        Call,
        Ddl,
        Other;
    }
}
