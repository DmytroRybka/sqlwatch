package sqlwatch4.driverlistener;

import net.sf.log4jdbc.Spy;
import net.sf.log4jdbc.SpyLogDelegator;

/**
 * @author dmitry.mamonov
 */
public class SqlWatch4SpyLogDelegator implements SpyLogDelegator {
    @Override
    public final boolean isJdbcLoggingEnabled() {
        return true;
    }

    @Override
    public final void exceptionOccured(Spy spy, String methodCall, Exception e, String sql, long execTime) {
    }

    @Override
    public final void methodReturned(Spy spy, String methodCall, String returnMsg) {
    }

    @Override
    public final void constructorReturned(Spy spy, String constructionInfo) {
    }

    @Override
    public final void sqlOccured(Spy spy, String methodCall, String sql) {
    }

    @Override
    public final void sqlTimingOccured(Spy spy, long execTime, String methodCall, String sql) {
    }

    @Override
    public final void connectionOpened(Spy spy) {
    }

    @Override
    public final void connectionClosed(Spy spy) {
    }

    @Override
    public final void debug(String msg) {

    }
}
