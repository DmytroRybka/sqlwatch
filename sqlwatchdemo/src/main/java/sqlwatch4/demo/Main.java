package sqlwatch4.demo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Random;

/**
 * @author dmitry.mamonov
 */

public class Main {
    static Random rnd = new Random();

    public static void main(String[] args) {
        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-context.xml");
            JdbcTemplate jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
            jdbcTemplate.update("CREATE TABLE client(id INT, name VARCHAR(50), age INT, sex CHAR)");

            while (true) {
                int loopInsert = rnd.nextInt(5);
                for (int i = 0; i < loopInsert; i++) {
                    randomInsert(jdbcTemplate);
                    Thread.sleep(40);
                }
                Thread.sleep(3000);
                int loopSelect = rnd.nextInt(12);
                for (int i = 0; i < loopSelect; i++) {
                    randomSelect(jdbcTemplate);
                    Thread.sleep(120);
                }
                Thread.sleep(2000);
                randomDelete(jdbcTemplate);
                Thread.sleep(1000);
            }
        } catch (Throwable th) {
            th.printStackTrace();
            System.exit(1);
        } finally {
            System.exit(0);
        }
    }

    private static void randomInsert(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("INSERT INTO client (id, name,age,sex) VALUES(?,?,?,?)",
                rnd.nextInt(),
                "name" + rnd.nextInt(),
                rnd.nextInt(100),
                rnd.nextInt(2));

    }

    private static void randomSelect(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.query("SELECT * FROM client where id<" + rnd.nextInt(), new ColumnMapRowMapper());
    }

    private static void randomDelete(JdbcTemplate jdbcTemplate) {
        if (rnd.nextInt(100) == 0) {
            jdbcTemplate.update("DELETE FROM client");
        }
    }
}
