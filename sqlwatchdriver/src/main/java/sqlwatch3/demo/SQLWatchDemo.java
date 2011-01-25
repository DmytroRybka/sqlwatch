package sqlwatch3.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Random;
import sqlwatch3.tool.QueriesProfilter;

/**
 * Демонстрационное приложение,
 * показывает функции профайлинга и отлдаки в SQLWatch.
 * @author dmitry.mamonov
 */
public class SQLWatchDemo {
    public static void main(String[] args) throws Exception {
        //load JDBC driver
            Class.forName("net.sf.log4jdbc.DriverSpy").newInstance();

        Connection connection = null;

        //"sakila" database creation script is avalilable
        //on mysql site: http://downloads.mysql.com/docs/sakila-db.zip
        connection = DriverManager.getConnection(
                "jdbc:log4jdbc:mysql://localhost:3306/sakila",
                "demo", "secret");

        //run profiler demo
        unefficiendDataLoadExample(connection);
        slowQueryExmple(connection);
    }

    static void unefficiendDataLoadExample(Connection connection) throws Exception {
        String[] tables = {
            "actor",
            "address",
            "category",
            "city",
            "country",
            "customer",
            "film",
            "inventory",
            "language",
            "payment",
            "rental",
            "staff",
            "store",  
        };

        Random rnd = new Random();
        Statement stmt = connection.createStatement();
        int stagesCount = 2;
        for(int i=0;i<stagesCount;i++){
            System.out.println("\n\nProfiling select queries, stage "+(i+1));
            //run 1000 queries
            for(int queryNo=0;queryNo<10000;queryNo++){
                String query = String.format("SELECT * FROM %1$s WHERE %1$s_id = %2$s;",
                        tables[rnd.nextInt(tables.length)],
                        1+rnd.nextInt(10));
                stmt.executeQuery(query).close();
            }
            System.out.printf("Stage %d is done, see it as the end of processing http request\n",(i+1));


            //sleep for a while so "sql profiler" will figure out that
            //it's time to display collected data.
            System.out.println("sleep");
            Thread.sleep(6*1000);
        }
        stmt.close();
        QueriesProfilter.getInstance().flushResultsImmediately();
    }

    static void slowQueryExmple(Connection connection) throws Exception {
        //slow query
        Statement stmt = connection.createStatement();
        System.out.println("\n\nProfiling SLOW select queries");
        long random = System.currentTimeMillis(); //random is used to miss queries cache
        stmt.executeQuery("SELECT (SELECT count(distinct a.first_name) FROM actor a, actor b, actor c) FROM actor z WHERE "+random+"="+random).close();
        System.out.println("sleep");
        Thread.sleep(6*1000);
        stmt.close();
    }
}
