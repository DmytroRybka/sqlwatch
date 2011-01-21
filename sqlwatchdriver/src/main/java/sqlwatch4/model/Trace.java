package sqlwatch4.model;

import net.sf.log4jdbc.Spy;

/**
 * @author dmitry.mamonov
 */
public class Trace {
    public enum Kind {
        Debug(false),
        ConnectionClosed(true),
        ConnectionOpened(true),
        SqlTiming(true),
        Sql(false),
        ConstructorReturned(false),
        MethodReturned(true),
        Exception(true);
        private boolean ui;

        Kind(boolean ui) {
            this.ui = ui;
        }

        public boolean isUi() {
            return ui;
        }
    }

    Kind kind;
    long when = System.currentTimeMillis();
    long thread = Thread.currentThread().getId();
    String classType;
    Integer connectionNumber;
    String methodCall;
    String exception;
    String sql;
    Long execTime;
    String returnMessage;
    String constructionInfo;
    String message;

    public long getWhen() {
        return when;
    }

    public long getThread() {
        return thread;
    }

    public Integer getConnectionNumber() {
        return connectionNumber;
    }

    public String getConstructionInfo() {
        return constructionInfo;
    }

    public void setConstructionInfo(String constructionInfo) {
        this.constructionInfo = constructionInfo;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public Long getExecTime() {
        return execTime;
    }

    public void setExecTime(Long execTime) {
        this.execTime = execTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMethodCall() {
        return methodCall;
    }

    public void setMethodCall(String methodCall) {
        this.methodCall = methodCall;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }

    public String getClassType() {
        return classType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public void setSpy(Spy spy) {
        this.classType = spy.getClassType();
        this.connectionNumber = spy.getConnectionNumber();
    }
}
