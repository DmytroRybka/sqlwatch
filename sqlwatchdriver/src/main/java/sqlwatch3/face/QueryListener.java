package sqlwatch3.face;

/**
 *
 * @author dmitry.mamonov
 */
public interface QueryListener {
    void query(String sql, double millis);
}
