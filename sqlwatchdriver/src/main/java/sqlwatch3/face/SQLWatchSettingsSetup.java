package sqlwatch3.face;


/**
 * Интерфейс для установки параметров концигурации
 * утилиты SQLWatch.
 *
 * @author dmitry.mamonov
 */
public interface SQLWatchSettingsSetup {
    public void setFlushTimeoutInSeconds(double timeout);
    public void addQueriesListener(SQLListener listener);
}
