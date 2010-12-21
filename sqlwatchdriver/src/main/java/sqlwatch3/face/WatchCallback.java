package sqlwatch3.face;

/**
 * Слушатель изменений в базе денных. Вызывается каждый раз когда
 * данные по SQL запросу слушателя были измененны. При этом в слушатель
 * передаётся SQL запрос клиента который непосредственно изменил 
 * данные в watch.
 *
 *
 * @author dmitry.mamonov
 */
public interface WatchCallback {
    /**
     * Запрос для мониторинга данных, как только выборка запроса измениться
     * будет вызван метод {@link #catchChange(java.lang.String, java.lang.String, java.lang.Object)}
     * @return
     */
    String getWatchQuery();
    /**
     * Метод вызывается каждый раз когда происходят изменения в области данных
     * над которой осуществляется мониторинг. Область данных задаётся запросом
     * {@link  #getWatchQuery()}.
     * 
     * @param watchQuery watch запрос для мониторинга изменений в области данных.
     * @param userQuery клиентский запрос изменивший область данных над которой осущетсвляется мониторинг.
     * @param extra не используется.
     */
    void catchChange(String watchQuery, String userQuery, Object extra);
}
