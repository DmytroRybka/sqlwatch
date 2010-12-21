package sqlwatch4.driverlistener;

import net.sf.log4jdbc.Spy;
import net.sf.log4jdbc.SpyLogDelegator;
import sqlwatch4.Config;
import sqlwatch4.TraceDispatcher;
import sqlwatch4.model.Trace;

import java.io.PrintWriter;
import java.io.StringWriter;

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
        Trace trace = new Trace();
        trace.setType("Exception");
        trace.setSpy(spy);
        trace.setMethodCall(methodCall);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        trace.setException(sw.toString());
        trace.setSql(sql);
        trace.setExecTime(execTime);
        TraceDispatcher.get().spread(trace);
    }

    @Override
    public final void methodReturned(Spy spy, String methodCall, String returnMsg) {
        if (Config.get().isTraceAll()) {
            Trace trace = new Trace();
            trace.setType("MethodReturned");
            trace.setSpy(spy);
            trace.setMethodCall(methodCall);
            trace.setReturnMessage(returnMsg);
            TraceDispatcher.get().spread(trace);
        }
    }

    @Override
    public final void constructorReturned(Spy spy, String constructionInfo) {
        Trace trace = new Trace();
        trace.setType("ConstructorReturned");
        trace.setSpy(spy);
        trace.setConstructionInfo(constructionInfo);
        TraceDispatcher.get().spread(trace);
    }

    @Override
    public final void sqlOccured(Spy spy, String methodCall, String sql) {
        Trace trace = new Trace();
        trace.setType("Sql");
        trace.setSpy(spy);
        trace.setMethodCall(methodCall);
        trace.setSql(sql);
        TraceDispatcher.get().spread(trace);
    }

    @Override
    public final void sqlTimingOccured(Spy spy, long execTime, String methodCall, String sql) {
        Trace trace = new Trace();
        trace.setType("sqlTiming");
        trace.setSpy(spy);
        trace.setExecTime(execTime);
        trace.setMethodCall(methodCall);
        trace.setSql(sql);
        TraceDispatcher.get().spread(trace);
    }

    @Override
    public final void connectionOpened(Spy spy) {
        Trace trace = new Trace();
        trace.setType("ConnectionOpened");
        trace.setSpy(spy);
        TraceDispatcher.get().spread(trace);
    }

    @Override
    public final void connectionClosed(Spy spy) {
        Trace trace = new Trace();
        trace.setType("ConnectionClosed");
        trace.setSpy(spy);
        TraceDispatcher.get().spread(trace);
    }

    @Override
    public final void debug(String msg) {
        if (Config.get().isTraceAll()) {
            Trace trace = new Trace();
            trace.setType("Debug");
            trace.setMessage(msg);
            TraceDispatcher.get().spread(trace);
        }
    }
}
