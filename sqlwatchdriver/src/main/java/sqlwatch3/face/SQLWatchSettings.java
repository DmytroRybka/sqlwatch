package sqlwatch3.face;

/**
 * Конфигурация утилиты с доступом только для чтения.
 *
 * @author dmitry.mamonov
 */
public interface SQLWatchSettings {
    public SQLListener[] getSQLListeners();

    public long getFlushTimeoutInMillis();
}
