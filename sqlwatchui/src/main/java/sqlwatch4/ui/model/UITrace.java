package sqlwatch4.ui.model;

import sqlwatch4.model.Trace;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author dmitry.mamonov
 */

public class UITrace extends Trace {
    /**
     * Note: Yes, I know it is not thread-safe to use SimpleDateFormatter but it is used only
     * in UI which is single-threaded, so it should be fine.
     */
    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("[HH:mm:ss:SSS]");

    public String getWhenTime() {
        return timeFormatter.format(new Date(getWhen()));
    }

    public String getDurationMs() {
        if (getExecTime() != null) {
            return Math.round((double) getExecTime() / 1000000.0) + "ms";
        } else {
            return "";
        }
    }

    public String getQueryOrCommand() {
        if (getSql() != null) {
            return getSql();
        } else {
            if (getKind() == Kind.MethodReturned) {
                return String.valueOf(getMethodCall());
            } else {
                return String.valueOf(getKind());
            }
        }
    }

}
