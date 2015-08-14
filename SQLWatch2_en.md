# SQL Watch 2.0: Watch and Profile your queries! #



<a href='http://code.google.com/p/sqlwatch/downloads/list'>SQLWatch 2.0</a> - tool
for monitoring and profiling SQL queries in application on JDBC Driver layer.

Currenly supported JDBC Drivers:
  1. MySQL - <a href='http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch2/src/sqlwatch2/demo/SQLWatchDemo.java'>direct supported</a>
  1. Other (including MySQL) - <a href='http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch2/src/sqlwatch2/demo/SQLWatchProxyDemo.java'>supported</a> through <a href='http://code.google.com/p/log4jdbc/'>log4jdbc driver proxy</a>

## Profiling ##
  * How many times **same data** is selected from database _per operation_?
  * How long it take to perform all SQL queries _per operation_?
  * Is SQL queries efficient?

`SQLWatch-QueriesProfiler`: listens all SQL queries from JDBC driver and aggregates them.
Whan no queries is received within 5 sec (configurable) then queries statistics
is printed to stdout.

This way is good enough to perform profiling of web-applications (not only). Work pattern:
  1. Some HTTP request is passed to server
  1. While processing request `Profiler` aggregate quereies data
  1. When response is returned to client nothing is happened on server
  1. Now statistics report will be displayed:
Report example:
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

Legend:
1. Table Name      - name of a table in FROM section of SQL query
2. Requests        - count of querties with this table name in FROM section
3. (of total %)    - percentage for such queries over total count of queries in reprot
4. Unique Requests - count of unique request (requests with different arguments like `WHERE X=?` counted as different)
5. Useless %       - percentage of queries which requests data which was already requested in this report
6. Time            - total time spend to perform this queries
7. (of total %)    - percentage of total time
TOTAL XXXX ms -  while time elapsed from first to last query in report,
                 this time includes not inly SQL execution time, but 
                 whole application logic either.
```

## Monitoring ##
  * How to find a line of code which midifies concrete data element?

Usecase: big application, unknown code, within register user operation
one extra record is created in `Accounts` table.

How to find line of code which actually does this unexpected operation?

Solution:
  1. `SELECT count(*) FROM Accounts` result of this query will be different is some row will be added or removed from `Accounts` table
  1. If after each INSERT/UPDATE/DELETE we will check result of query above
  1. Then if result set of query is different then current query actually added (or removed) one row into `Accounts` table
  1. When this situation is catched stack trace may be printed
  1. Analysing stack trace we will definitely find line of code which performed INSERT INTO Account operation
  1. It is even possible to set a BREAKPOINT instead of printing stack trace so we will be able to catch changed in debugger!

Only problem is a transaction, it may hide changes from observing.
But all changes monitored in Connection.TRANSACTION\_READ\_UNCOMMITTED mode,
so transaction is not a problem!

For MySQL demo database `sakila` one example <a href='http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch2/src/sqlwatch2/demo/SQLWatchDemoSetup.java'>is presented</a>. It will catch any INSERT/DELETE into table `country`.

```
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
```

Program output will be:
```
>>> Query result set is changed <<<
Watch Query: SELECT count(*) FROM country
java.lang.Exception: Stack trace to find user code
        at sqlwatch2.demo.SQLWatchDemoSetup$1$1$1.catchChange(SQLWatchDemoSetup.java:46) <<< Watch listener printed this message
        at sqlwatch2.tool.ChangesMonitor.update(ChangesMonitor.java:45) <<< DML quereis handler which catched modification in "watch" query result set
        at sqlwatch2.SQLWatch$1.query(SQLWatch.java:31) <<< SQL Watch infrastructure code
        at com.mysql.jdbc.log.SQLWatchForMySQL.processMessage(SQLWatchForMySQL.java:78) <<< MySQL JDBC Driver
        at com.mysql.jdbc.log.SQLWatchForMySQL.logInfo(SQLWatchForMySQL.java:52) <<< MySQL JDBC Driver
        at com.mysql.jdbc.profiler.LoggingProfilerEventHandler.consumeEvent(LoggingProfilerEventHandler.java:46) <<< MySQL JDBC Driver
        at com.mysql.jdbc.MysqlIO.sqlQueryDirect(MysqlIO.java:2209) <<< MySQL JDBC Driver
        at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2536) <<< MySQL JDBC Driver
        at com.mysql.jdbc.StatementImpl.executeUpdate(StatementImpl.java:1564) <<< MySQL JDBC Driver
        at com.mysql.jdbc.StatementImpl.executeUpdate(StatementImpl.java:1485) <<< MySQL JDBC Driver
        at sqlwatch2.demo.SQLWatchDemo.watchChangesExample(SQLWatchDemo.java:80) <<< !!! line of code which actually performed "insert into country..." !!!
        at sqlwatch2.demo.SQLWatchDemo.main(SQLWatchDemo.java:30) <<< application antry point
```


## Configuration (for MySQL) ##
(Example of SQLWatch configuration with log4jdbc proxy driver
is presented <a href='http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch2/src/sqlwatch2/demo/SQLWatchProxyDemo.java'>here</>)<br>
<br>
<b>1.</b> <a href='http://code.google.com/p/sqlwatch/downloads/list'>Download</a> the latest version of SQLWatch and add it into classpath of an application.<br>
<br>
<b>2.</b> Modify MySQL JDBC Driver URL, - add logger <code>SQLWatchForMySQL</code> and anable<br>
profiling of SQL querires:<br>
<pre><code>DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila?profileSQL=true&amp;logger=SQLWatchForMySQL",<br>
                "demo", "secret");  <br>
</code></pre>

<b>3.</b> Add next option into application system properties:<br>
<pre><code>  java -DSQLWatchSetup=sqlwatch2.demo.SQLWatchDemoSetup ...<br>
</code></pre>
Sure, custom configuration class (<code>sqlwatch2.demo.SQLWatchDemoSetup</code>) may be different.<br>
<br>
<b>4.</b> Setup configuration for SQLWatch (<code>sqlwatch2.demo.SQLWatchDemoSetup</code>):<br>
<pre><code>package sqlwatch2.demo;<br>
<br>
import java.sql.DriverManager;<br>
import java.sql.SQLException;<br>
import sqlwatch2.SQLWatch;<br>
import sqlwatch2.face.SQLWatchSettingsSetup;<br>
import sqlwatch2.face.SQLWatchSetup;<br>
import sqlwatch2.face.WatchCallback;<br>
import sqlwatch2.tool.ChangesMonitor;<br>
import sqlwatch2.tool.QueriesProfilter;<br>
<br>
/**<br>
 * This class is user as "configuration file" for SQLWatch tool.<br>
 *<br>
 * "-DSQLWatchConfig=sqlwatch2.demo.SQLWatchConfig" system option<br>
 * should be set apply this class. It is called from JDBC driver.<br>
 *<br>
 * @author dmitry.mamonov<br>
 */<br>
public class SQLWatchDemoSetup {<br>
    static {<br>
        //Print some message to be sure that configuration is applyed.<br>
        System.out.println("Configuring SQLWatch...");<br>
        SQLWatch.getInstance().setSetup(new SQLWatchSetup() {<br>
            public void setupSettings(SQLWatchSettingsSetup setup) {<br>
                //QueriesProfiler will flush statistics after idle of 3 seconds<br>
                setup.setFlushTimeoutInSeconds(3.0);<br>
<br>
                //enable QueriesProfiler (it's singleton)<br>
                setup.addQueriesListener(QueriesProfilter.getInstance());<br>
<br>
                //enable ChangeMonitor to watch and catch changes in database<br>
                setup.addQueriesListener(new ChangesMonitor() {<br>
                    {<br>
                        try {<br>
                            //ChangesMonitor is required an extra database connector<br>
                            //to not interfer with main apllication database connection.<br>
                            //<br>
                            //Also it works in Connection.TRANSACTION_READ_UNCOMMITTED mode<br>
                            //so it will be able to catch changes provided within<br>
                            //transaction instantly, without wating for commit.<br>
                            setConnection(DriverManager.getConnection(<br>
                                    "jdbc:mysql://localhost:3306/sakila",<br>
                                    "demo",<br>
                                    "secret"));<br>
                        } catch (SQLException ex) {<br>
                            ex.printStackTrace();<br>
                        }<br>
<br>
                        //Add some callback to catch changes provided over<br>
                        //important data sets.<br>
                        addWatch(new WatchCallback() {<br>
                            //Return query which selects a data set for monitoring.<br>
                            //SELECT count(*)... query will help to catch INSERTs and DELETEs,<br>
                            //SELET * FROM ... WHERE id=... wiil help to catch changes on particular data row.<br>
                            public String getWatchQuery() {<br>
                                //with fire on insert or delete<br>
                                return "SELECT count(*) FROM country";<br>
                            }<br>
<br>
                            public void catchChange(String watchQuery, String userQuery, Object extra) {<br>
                                //when change is catched, this method is called.<br>
                                System.out.println("&gt;&gt;&gt; Query result set is changed &lt;&lt;&lt;");<br>
                                System.out.println("Watch Query: "+watchQuery);<br>
                                //you may print stack trace to find user code,<br>
                                //or even set breakpopint here to cacth changes in debugger!<br>
                                new Exception("Stack trace to find user code").printStackTrace(System.out);<br>
                            }<br>
                        });<br>
                    }<br>
                });<br>
            }<br>
        });<br>
        System.out.println("...Done");<br>
    }<br>
}<br>
<br>
<br>
</code></pre>

<b>5.</b> Done, SQL Watch us configured, now run application and see results.<br>
<br>
<h2>Example</h2>
<ul><li>Demo database  <a href='http://downloads.mysql.com/docs/sakila-db.zip'>sakila</a> is required to run <a href='http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch2/src/sqlwatch2/demo/SQLWatchDemo.java'>bundled demo application</a>
</li><li>demo/secret - default login and password hardcoded in demo application.<br>
</li><li><a href='http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch2/src/sqlwatch2/demo/SQLWatchDemo.java'>Demo application</a>
</li><li><a href='http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch2/src/sqlwatch2/demo/SQLWatchDemoSetup.java'>Demo configuration for SQLWatch<a />