package sqlwatch3.face;

import java.io.Serializable;

/**
 * Callback интерфей, клиентский код конфигурирующий SQLWatch
 * должен реализовывать этот интерфейс.
 *
 * Идея в том что бы дыть SQLWatch возможность динамически
 * обновлять текущую конфигурацию по своему успотрению
 * (например при изменении клиентом кода конфигурации в
 * рантайме через HotSwap или JRabel).
 *
 * @author dmitry.mamonov
 */
public interface SQLWatchSetup extends Serializable {
    void setupSettings(SQLWatchSettingsSetup setup);
}
