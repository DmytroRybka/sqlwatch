Provides ability to monitor changes in database
and catch them in Java debugging mode.

Will help to find malicious database changing code.

Live pattern:
  1. Operation "Register user" inserts 1 row into table "User" in simple
  1. While operation execution 1 row is also inserted into table "Account"
  1. But we see no explicit code working with "Account" table
  1. Let's try to watch database with query: `SELECT count(*) FROM "Accounts"`
  1. Gotcha! Account is also created from AspectJ code, that is why we missed it before!

What is it mean - watch database? Here is SQLWatch concepts:
  1. SQLWatch is not a library but code template.
  1. SQLWatch is a mysql JDBC driver listener.
  1. When it receives log message contains INSERT/UPDATE/DELETE/CALL it starts to act.
  1. Action stage is a check of result sets of custom queries.
  1. If a query result is different from previous one => monitoring data is changed.
  1. When change found, custom code starts to work, it's ready for debug and logging.
  1. Tip: this way indirect changes, via trigger for example, will also be detected.

See SQLWatch and Demo classes source:
  * http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch/src/com/mysql/jdbc/log/SQLWatch.java
  * http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch/src/sqlwatch/Demo.java

Example with real code:

We are working with _sakila_ database which is available from http://dev.mysql.com/doc/
```
line 101: statement.executeUpdate("INSERT INTO country (country, last_update) VALUES('Albonia', NOW());");
line 102: statement.executeUpdate("INSERT INTO city (city, country_id, last_update) VALUES('Catch me if you can!', 1, NOW());");
```
Now we planning to catch `INSERT INTO city`, we will use special query to monitor
table "city" state, here is it: `SELECT max(city_id) from city;`

SQLWatch works as sql logger so we need to associate it with
working connection:
```
       connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila"+
                "?profileSQL=true" + //enable logging of SQL queries into java logger
                "&logger=SQLWatch", //SQLWatch logger
                "demo", "secret");
```


The rest is what we need is to add `watch` with specified query,
we need to modify SQLWatch class as next:
```
package com.mysql.jdbc.log;

...

public class SQLWatch extends StandardLogger {
...
    //TODO: please change connection options to appropriate values
    private final String connectionUrl = "jdbc:mysql://localhost:3306/sakila";
    private final String connectionUser = "demo";
    private final String connectionPassword = "secret";

...

    protected void monitor(Object objMessage) {
...
                //NOTE: all queries executed here in READ_UNCOMMITTED mode,
                //so changes will be detected even through they are provided
                //within transaction.
                if (watch("SELECT max(city_id) from city;")) { //<<place watch query here
/*breakpoint*/      System.out.println("Changes detected!");
                }
...
        }
    }
```

Now done, run code in debug, it will stop at `/*breakpoint*/` position,
passing down by stack you will find `line 102` which actually changes "city" table.

Here is nothing to download in jar/or zip file in this project,
just get actual sources from repository and place them into
your project.
  * http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch/src/com/mysql/jdbc/log/SQLWatch.java
  * http://code.google.com/p/sqlwatch/source/browse/trunk/sqlwatch/src/sqlwatch/Demo.java

Have fun!