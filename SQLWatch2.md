# SQL Watch 2.0: Watch and Profile your queries! #



<a href='http://sqlwatch.googlecode.com/files/sqlwatch2.jar'>SQLWatch 2.0</a> - утилита для монитороинга и профайлинга SQL запросов в
приложении на уровне JDBC драйвера.

В настоящий момент поддерживает MySQL, но добавление поддежрки
других JDBC драйверов не должо быть проблемой.



## Профайлинг ##

  * Сколько раз одни и те же данные выбираются из базы?
  * Как много времени занимает выполнение SQL запросов?
  * Все ли SQL запросы эффективны?

Решение SQLWatch/QueriesProfiler: профайлер слушает все сообщения о поступающих SQL запросах
от JDBC драйвера и собирает их. Когда с момента последнего запроса
проходит больше 5 сек. профалер печатает таблицу с собранной статистикой.

Это удобно для Web-приложений, - запрос поступает, сервер работает, результат вернули
браузеру, сервер не выполнят запросов, статистика печатается автоматически.

```
----------- >> begin >> queries profiling report >> -----------
Table Name         Requests (of total %) /Unique Requests | Useless % |  Time  (of total %)|
staff            =       88 (        9%) /             10 |       89% |         32ms ( 16%)|
film             =       87 (        9%) /             10 |       89% |          0ms (  0%)|
address          =       87 (        9%) /             10 |       89% |         31ms ( 15%)|
rental           =       86 (        9%) /             10 |       88% |          0ms (  0%)|
payment          =       85 (        9%) /             10 |       88% |         31ms ( 15%)|
actor            =       76 (        8%) /             10 |       87% |         16ms (  8%)|
language         =       73 (        7%) /             10 |       86% |         62ms ( 31%)|
category         =       72 (        7%) /             10 |       86% |         15ms (  7%)|
store            =       72 (        7%) /             10 |       86% |          0ms (  0%)|
customer         =       72 (        7%) /             10 |       86% |         16ms (  8%)|
inventory        =       72 (        7%) /             10 |       86% |          0ms (  0%)|
country          =       69 (        7%) /             10 |       86% |          0ms (  0%)|
city             =       61 (        6%) /             10 |       84% |          0ms (  0%)|
TOTAL 468 ms >>> =     1000 (      100%) /            130 |       87% |        203ms (100%)|
----------- >> end >> [Memory    3M Free/    4M Total] >> -----------

Легенда:
1. Table Name      - название таблицы из FROM раздела SQL запроса
2. Requests        - количество запросов с этой таблице в разделе FROM
3. (of total %)    - доля этих запросов от общего числа запросов
4. Unique Requests - количество уникальных (с учётом значений параметров) запросов к этой заблице
5. Useless %       - доля запросов для получения данных которые уже были выбраны ранее
6. Time            - время потраченное на выполнения запросов
7. (of total %)    - доля потраченного времени от времени потраченного на выполнения всех запросов
TOTAL XXXX ms - общее количество времени прошедшее с момента получения первого запроса
    в списке, до момента получения последнего (включает время изспонения запросов
    и сумму промежутков времени между запросами) - время работы Бизнес-уровня
    приложения (most probably).
```

## Отладка ##
  * Как найти строчку кода которая меняет конкретные данны?

Пример: большое приложение, незнакомый код, при регистрации пользователя
через интерфейс создаётся лишняя запись в таблице счетов.

Как найти, где в проекте добавляется лишний счёт?

Решение `SQLWatch/ChangesMonitor`:
  1. Нужно добавить `Watch` по запросу `SELECT count(*) FROM Accounts`
  1. В `WatchCallback` распечатать стектрейс (Thread.dumpStack())
  1. Запустить приложени и выполнить операцию
  1. Найти "вражеский" код в стектрейсе

Как это работает. `ChangesMonitor` так же как и профайлер слушает все
SQL запросы от JDBC драйвера.

Когда поступает запрос на изменение данных (INSERT/UPDATE/DELETE)
монитор выполнять все `Watch` запросы и сравнивает результат их
выполнения. Если количество `count(*)` изменилось
по сравнению с предыдущим результатом `SELECT count(*) FROM Accounts`
значит полученный запрос изменил отслеживаемые данные.

В этом случае слушатель оповещается, и распечатав стектрейс он
может показать в каком месте кода был выполнен пользвательский
SQL запрос.
```
>>> Query result set is changed <<<
Watch Query: SELECT count(*) FROM country
java.lang.Exception: Stack trace to find user code
        at sqlwatch2.demo.SQLWatchDemoSetup$1$1$1.catchChange(SQLWatchDemoSetup.java:46) <<< слушателй Watch запроса напечатавший это сообщение
        at sqlwatch2.tool.ChangesMonitor.update(ChangesMonitor.java:45) <<< обработчик сообщений о поступающих DML запросах, проверяющий наличие изменений
        at sqlwatch2.SQLWatch$1.query(SQLWatch.java:31) <<< вспомогательный код разделяющий SQL запросы по типам
        at com.mysql.jdbc.log.SQLWatchForMySQL.processMessage(SQLWatchForMySQL.java:78) <<< MySQL JDBC Driver
        at com.mysql.jdbc.log.SQLWatchForMySQL.logInfo(SQLWatchForMySQL.java:52) <<< MySQL JDBC Driver
        at com.mysql.jdbc.profiler.LoggingProfilerEventHandler.consumeEvent(LoggingProfilerEventHandler.java:46) <<< MySQL JDBC Driver
        at com.mysql.jdbc.MysqlIO.sqlQueryDirect(MysqlIO.java:2209) <<< MySQL JDBC Driver
        at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2536) <<< MySQL JDBC Driver
        at com.mysql.jdbc.StatementImpl.executeUpdate(StatementImpl.java:1564) <<< MySQL JDBC Driver
        at com.mysql.jdbc.StatementImpl.executeUpdate(StatementImpl.java:1485) <<< MySQL JDBC Driver
        at sqlwatch2.demo.SQLWatchDemo.watchChangesExample(SQLWatchDemo.java:80) <<< !!! строчка кода в которой были изменены отслеживаемые нами данные !!!
        at sqlwatch2.demo.SQLWatchDemo.main(SQLWatchDemo.java:30) <<< точка входа в приложение
```


## Настройка ##

**1.** Скачать <a href='http://sqlwatch.googlecode.com/files/sqlwatch2.jar'>sqlwatch2.jar</a> и добавить его в classpath приложения

**2.** В стоке соединения JDBC драйвера с MySQL прописать логгер `SQLWatchForMySQL` и
включить профайлинг SQL запросов:
```
DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila?profileSQL=true&logger=SQLWatchForMySQL",
                "demo", "secret");  
```

**3.** Добавить в системный свойства приложения имя класса, статический
инициализатор которого задаст конфигурацию для SQLWatch:
```
  java -DSQLWatchSetup=sqlwatch2.demo.SQLWatchDemoSetup ...
```

**4.** Задать требуемую конфигурацию:
```
package sqlwatch2.demo;

import java.sql.DriverManager;
import java.sql.SQLException;
import sqlwatch2.SQLWatch;
import sqlwatch2.face.SQLWatchSettingsSetup;
import sqlwatch2.face.SQLWatchSetup;
import sqlwatch2.face.WatchCallback;
import sqlwatch2.tool.ChangesMonitor;
import sqlwatch2.tool.QueriesProfilter;

/**
 * This class is user as "configuration file" for SQLWatch tool.
 *
 * "-DSQLWatchConfig=sqlwatch2.demo.SQLWatchConfig" system option
 * should be set apply this class. It is called from JDBC driver.
 *
 * @author dmitry.mamonov
 */
public class SQLWatchDemoSetup {
    static {
        //Print some message to be sure that configuration is applyed.
        System.out.println("Configuring SQLWatch...");
        SQLWatch.getInstance().setSetup(new SQLWatchSetup() {
            public void setupSettings(SQLWatchSettingsSetup setup) {
                //QueriesProfiler will flush statistics after idle of 3 seconds
                setup.setFlushTimeoutInSeconds(3.0);

                //enable QueriesProfiler (it's singleton)
                setup.addQueriesListener(QueriesProfilter.getInstance());

                //enable ChangeMonitor to watch and catch changes in database
                setup.addQueriesListener(new ChangesMonitor() {
                    {
                        try {
                            //ChangesMonitor is required an extra database connector
                            //to not interfer with main apllication database connection.
                            //
                            //Also it works in Connection.TRANSACTION_READ_UNCOMMITTED mode
                            //so it will be able to catch changes provided within
                            //transaction instantly, without wating for commit.
                            setConnection(DriverManager.getConnection(
                                    "jdbc:mysql://localhost:3306/sakila",
                                    "demo",
                                    "secret"));
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                        //Add some callback to catch changes provided over
                        //important data sets.
                        addWatch(new WatchCallback() {
                            //Return query which selects a data set for monitoring.
                            //SELECT count(*)... query will help to catch INSERTs and DELETEs,
                            //SELET * FROM ... WHERE id=... wiil help to catch changes on particular data row.
                            public String getWatchQuery() {
                                //with fire on insert or delete
                                return "SELECT count(*) FROM country";
                            }

                            public void catchChange(String watchQuery, String userQuery, Object extra) {
                                //when change is catched, this method is called.
                                System.out.println(">>> Query result set is changed <<<");
                                System.out.println("Watch Query: "+watchQuery);
                                //you may print stack trace to find user code,
                                //or even set breakpopint here to cacth changes in debugger!
                                new Exception("Stack trace to find user code").printStackTrace(System.out);
                            }
                        });
                    }
                });
            }
        });
        System.out.println("...Done");
    }
}


```

**5.** Готово, SQL Watch настроен, можно запускать прилжение.

## Пример ##
  * Для запуска примера нужна схема демонстрационая БД <a href='http://downloads.mysql.com/docs/sakila-db.zip'>sakila</a>
  * demo/secret - прописанные в коде логин и пароль пользователя БД
  * <a href='http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch2/src/sqlwatch2/demo/SQLWatchDemo.java'>Код демонстрационного приложения</a>
  * <a href='http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch2/src/sqlwatch2/demo/SQLWatchDemoSetup.java'>Требуемы код для конфигурирвония SQLWatch<a />