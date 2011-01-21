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
        trace.setKind(Trace.Kind.Exception);
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
        if (Config.get().isTraceMethods()) {
            if (methodCall.startsWith("set")||methodCall.startsWith("get")){
                return;
            }
            if (methodCall.startsWith("clearParameters()")){
                return;
            }
            if (methodCall.startsWith("execute")){
                return;
            }
            if (methodCall.startsWith("wasNull")){
                return;
            }
            if (methodCall.startsWith("next")){
                return;
            }
            if (methodCall.startsWith("new ")){
                return;
            }
            if (methodCall.startsWith("clearWarnings")){
                return;
            }
            if (methodCall.startsWith("isClosed")){
                return;
            }
            if (methodCall.startsWith("close")){
                return;
            }
            if (methodCall.startsWith("create")){
                return;
            }
            if (methodCall.startsWith("prepare")){
                return;
            }
            Trace trace = new Trace();
            trace.setKind(Trace.Kind.MethodReturned);
            trace.setSpy(spy);
            trace.setMethodCall(methodCall);
            trace.setReturnMessage(returnMsg);
            TraceDispatcher.get().spread(trace);
        }
    }

    @Override
    public final void constructorReturned(Spy spy, String constructionInfo) {
        Trace trace = new Trace();
        trace.setKind(Trace.Kind.ConstructorReturned);
        trace.setSpy(spy);
        trace.setConstructionInfo(constructionInfo);
        TraceDispatcher.get().spread(trace);
    }

    @Override
    public final void sqlOccured(Spy spy, String methodCall, String sql) {
        Trace trace = new Trace();
        trace.setKind(Trace.Kind.Sql);
        trace.setSpy(spy);
        trace.setMethodCall(methodCall);
        trace.setSql(sql);
        TraceDispatcher.get().spread(trace);
    }

    @Override
    public final void sqlTimingOccured(Spy spy, long execTime, String methodCall, String sql) {
        Trace trace = new Trace();
        trace.setKind(Trace.Kind.SqlTiming);
        trace.setSpy(spy);
        trace.setExecTime(execTime);
        trace.setMethodCall(methodCall);
        trace.setSql(sql);
        TraceDispatcher.get().spread(trace);
    }

    @Override
    public final void connectionOpened(Spy spy) {
        Trace trace = new Trace();
        trace.setKind(Trace.Kind.ConnectionOpened);
        trace.setSpy(spy);
        TraceDispatcher.get().spread(trace);
    }

    @Override
    public final void connectionClosed(Spy spy) {
        Trace trace = new Trace();
        trace.setKind(Trace.Kind.ConnectionClosed);
        trace.setSpy(spy);
        TraceDispatcher.get().spread(trace);
    }

    @Override
    public final void debug(String msg) {
        if (Config.get().isTraceAll()) {
            Trace trace = new Trace();
            trace.setKind(Trace.Kind.Debug);
            trace.setMessage(msg);
            TraceDispatcher.get().spread(trace);
        }
    }
}
