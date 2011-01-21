package sqlwatch4.console;

import org.hibernate.pretty.Formatter;

/**
 * @author dmitry.mamonov
 */
public class TestSQLFormatter {
    public static void main(String[] args) {
        Formatter f = new Formatter("SELECT first_name, l_name, bd as bdate FROM user JOIN address on ad_id=addr_id where id=12 AND name ilike '$person' and (fk1=3 or fk1=4) and exists (select * from topic where sarter=uid)");
        f.setInitialString("");
        f.setIndentString("  ");
        String formatted = f.format();
        System.out.println(formatted);
    }
}
