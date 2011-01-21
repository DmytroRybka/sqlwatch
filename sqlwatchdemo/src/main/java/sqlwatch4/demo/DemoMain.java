package sqlwatch4.demo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.transaction.TransactionRolledbackException;
import java.util.Random;

/**
 * @author dmitry.mamonov
 */

public class DemoMain {
    static Random rnd = new Random();

    public static void main(String[] args) {
        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-context.xml");
            final JdbcTemplate jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
            TransactionTemplate transactionTemplate = applicationContext.getBean(TransactionTemplate.class);
            jdbcTemplate.update("CREATE TABLE client(id INT, name VARCHAR(50), age INT, sex CHAR)");

            while (true) {
                try {
                    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                            int loopInsert = rnd.nextInt(5);
                            for (int i = 0; i < 3+loopInsert; i++) {
                                randomInsert(jdbcTemplate);
                                try {
                                    Thread.sleep(40);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            if (rnd.nextBoolean()) {
                                throw new RuntimeException("Opps! Rollback!");
                            }
                        }
                    });
                } catch (RuntimeException oops) {
                    System.out.println(oops.getMessage());
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
