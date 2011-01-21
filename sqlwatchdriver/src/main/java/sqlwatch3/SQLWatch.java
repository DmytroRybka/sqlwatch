package sqlwatch3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sqlwatch3.face.QueryListener;
import sqlwatch3.face.SQLListener;
import sqlwatch3.face.SQLWatchSettings;
import sqlwatch3.face.SQLWatchSettingsSetup;
import sqlwatch3.face.SQLWatchSetup;
import sqlwatch3.tool.QueriesProfilter;

/**
 * "Общее место" утлииты, класс является синглтоном,
 * содержит данные конфигурации и является посредником
 * между логгером JDBC драйвера и конкретными
 * слушателями SQL запросов.
 *
 * @author dmitry.mamonov
 */
public class SQLWatch {
    private static volatile SQLWatch INSTANCE;
    private SQLWatchSetup setup = null;
    private LocalConfig config = new LocalConfig();
    private SQLWatchSettings settings = config;
    private SQLWatchSettingsSetup settingsSetup = config;
    private QueryListener queryListenerProxy = new QueryListener() {
        public void query(String sql, double ms) {
            String lower = sql.toLowerCase();
            if (lower.startsWith("select")) {
                for (SQLListener lst : settings.getSQLListeners()) {
                    lst.select(sql, ms);
                }
            } else if (lower.startsWith("insert")) {
                for (SQLListener lst : settings.getSQLListeners()) {
                    lst.update(sql, ms, SQLListener.QueryType.Insert);
                }
            } else if (lower.startsWith("update")) {
                for (SQLListener lst : settings.getSQLListeners()) {
                    lst.update(sql, ms, SQLListener.QueryType.Update);
                }
            } else if (lower.startsWith("delete")) {
                for (SQLListener lst : settings.getSQLListeners()) {
                    lst.update(sql, ms, SQLListener.QueryType.Delete);
                }
            } else if (lower.startsWith("create") || lower.startsWith("drop") || lower.startsWith("alter")) {
                for (SQLListener lst : settings.getSQLListeners()) {
                    lst.ddl(sql, ms);
                }
            } else if (lower.startsWith("call") || lower.startsWith("exec")) {
                for (SQLListener lst : settings.getSQLListeners()) {
                    lst.call(sql, ms);
                }
            } else {
                for (SQLListener lst : settings.getSQLListeners()) {
                    lst.other(sql, ms);
                }
            }
        }
    };

    private SQLWatch() {
        super();
    }

    public static synchronized SQLWatch getInstance() {
        if (INSTANCE==null){
             INSTANCE = new SQLWatch();
             INSTANCE.init();
        }
        return INSTANCE;
    }

    public void setSetup(SQLWatchSetup setup) {
        this.setup = setup;
        reloadConfiguration();
    }

    private void reloadConfiguration() {
        if (this.setup != null) {
            this.setup.setupSettings(settingsSetup);
        }
    }

    public SQLWatchSettings getSettings() {
        return settings;
    }

    public QueryListener getQueryListenerProxy() {
        return queryListenerProxy;
    }

    private void init() {
        String setupClassName = System.getProperty("SQLWatchSetup");
        if (setupClassName != null) {
            try {
                Class.forName(setupClassName).newInstance();
            } catch (Exception ex) {
                System.err.printf("%s configuration is failed! (see exception below)", getClass().getSimpleName());
                ex.printStackTrace();
            }
        } else {
            SQLWatch.getInstance().setSetup(new SQLWatchSetup() {
                public void setupSettings(SQLWatchSettingsSetup setup) {
                    //QueriesProfiler will flush statistics after idle of 3 seconds
                    setup.setFlushTimeoutInSeconds(5.0);

                    //enable QueriesProfiler (it's singleton)
                    setup.addQueriesListener(QueriesProfilter.getInstance());
                }
            });
        }
    }

    private class LocalConfig implements SQLWatchSettings, SQLWatchSettingsSetup {
        long flushTimeoutMillis = 5000;
        SQLListener[] listeners = new SQLListener[0];

        public SQLListener[] getSQLListeners() {
            return listeners;
        }

        public long getFlushTimeoutInMillis() {
            return flushTimeoutMillis;
        }

        public void setFlushTimeoutInSeconds(double timeout) {
            flushTimeoutMillis = (long) (timeout * 1000);
        }

        public void addQueriesListener(SQLListener listener) {
            List<SQLListener> listenersList = new ArrayList<SQLListener>(Arrays.asList(listeners));
            listenersList.add(listener);
            listeners = listenersList.toArray(new SQLListener[listenersList.size()]);
        }
    }
}
