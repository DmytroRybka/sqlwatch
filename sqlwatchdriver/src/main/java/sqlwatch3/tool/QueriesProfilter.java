package sqlwatch3.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import sqlwatch3.SQLWatch;
import sqlwatch3.face.SQLListener;

/**
 * Профайлер SQL запросов, агрегирует данные о поступающих SQL запросах
 * и по печатает таблицу со статистикой по таймауту.
 *
 * @author dmitry.mamonov
 */
public class QueriesProfilter extends SQLListener.Adapter {
    static QueriesProfilter INSTANCE;
    boolean printAllQueries = false;
    long lastAccessTime = System.currentTimeMillis();
    String printStackTraceFilter = null;
    final AtomicReference<Session> session = new AtomicReference<Session>(new Session());
    boolean shutdownHookAlreadyAdded = false;
    final Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            flushResults();
        }
    };

    public boolean isPrintAllQueries() {
        return printAllQueries;
    }

    public void setPrintAllQueries(boolean printAllQueries) {
        this.printAllQueries = printAllQueries;
    }

    public String getPrintStackTraceFilter() {
        return printStackTraceFilter;
    }

    public void setPrintStackTraceFilter(String printStackTraceFilter) {
        this.printStackTraceFilter = printStackTraceFilter;
    }

    private QueriesProfilter() {
        super();
        QueriesProfilterDaemonThread.getInstace().startOnce();
        synchronized (shutdownHook) {
            if (shutdownHookAlreadyAdded == false) {
                shutdownHookAlreadyAdded = true;
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            }
        }
    }

    public static synchronized QueriesProfilter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new QueriesProfilter();
        }
        return INSTANCE;
    }

    @Override
    public void call(String sql, double millis) {
        this.select(sql, millis);
    }

    @Override
    public void ddl(String sql, double millis) {
        this.select(sql, millis);
    }

    @Override
    public void other(String sql, double millis) {
        this.select(sql, millis);
    }

    @Override
    public void update(String sql, double millis, QueryType type) {
        this.select(sql, millis);
    }

    @Override
    public void select(String sql, double millis) {
        //update last access time
        lastAccessTime = System.currentTimeMillis();
//        if (printAllQueries==true){
//            System.out.println("SQL: "+sql);
//        }

        //process query data
        String baseTable = "UNKNOWN";
        try {
            if (sql.toLowerCase().trim().startsWith("select")){
                String[] lowerWords = sql.toLowerCase().split("\\s+");
                for (int i = 0; i < lowerWords.length - 1; i++) {
                    if (lowerWords[i].equals("from")) {
                        baseTable = lowerWords[i + 1];
                        break;
                    }
                }
            }
        } catch (Exception pass) {

        }
        if (baseTable.equals("UNKNOWN")){
            String sqlLine = sql.replace("\n", "").replace("\r", "");
            if (sqlLine.length()>30){
                baseTable = sqlLine.substring(0, 30);
            } else {
                baseTable = sqlLine;
            }
        }

        Session currentSession = session.get();
        currentSession.addLogLine(baseTable, sql, millis);
    }

    public void flushResults() {
        if (System.currentTimeMillis() - lastAccessTime > SQLWatch.getInstance().getSettings().getFlushTimeoutInMillis()) {
            flushResultsImmediately();
        }
    }

    public void flushResultsImmediately() {
        Session currentSession = session.getAndSet(new Session());
        currentSession.print();
    }
}

class QueriesProfilterDaemonThread extends Thread {
    private static volatile QueriesProfilterDaemonThread INSTANCE = new QueriesProfilterDaemonThread();

    private QueriesProfilterDaemonThread() {
        super(QueriesProfilter.class.getSimpleName() + "-daemon");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (true) {
                QueriesProfilter.getInstance().flushResults();
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            System.out.println(getName() + " is interrupted");
        }
    }

    public static QueriesProfilterDaemonThread getInstace() {
        return INSTANCE;
    }

    public synchronized void startOnce() {
        if (isAlive() == false) {
            this.start();
        }
    }
}

class Session {
    private static final double slowQueryTimeLimitMillis = 20;
    private Set<String> slowQueries = new LinkedHashSet<String>();
    long firstQueryTime = 0;
    long lastQueryTime = 0;
    Map<String, SessionItem> items = new ConcurrentHashMap<String, SessionItem>();

    public synchronized void addLogLine(String _table, String _line, double millis) {
        lastQueryTime = System.currentTimeMillis();
        if (firstQueryTime == 0) {
            firstQueryTime = lastQueryTime;
        }

        if (!items.containsKey(_table)) {
            items.put(_table, new SessionItem(_table));
        }
        SessionItem item = items.get(_table);

        item.addQuery(_line);
        item.setCountQueries(item.getCountQueries() + 1);
        item.setTotalTime(item.getTotalTime() + millis);

        if (millis > slowQueryTimeLimitMillis) {
            slowQueries.add(String.format("[%5.0fms] %s", millis, _line));
        }
    }

    public synchronized void print() {
        if (items.isEmpty() == true) {
            return;
        }

        long totalTime = lastQueryTime - firstQueryTime;
        List<SessionItem> itemsList = new ArrayList<SessionItem>(items.values());

        Collections.sort(itemsList, new Comparator<SessionItem>() {
            public int compare(SessionItem _l, SessionItem _r) {
                return -Integer.valueOf(_l.getCountQueries()).compareTo(_r.getCountQueries());
            }
        });

        System.out.println("----------- >> begin >> queries profiling report >> -----------");
        int wholeQueries = 0;
        int wholeDifferentQueris = 0;
        double wholeTime = 0;
        for (SessionItem it : itemsList) {
            wholeQueries += it.getCountQueries();
            wholeDifferentQueris += it.getCountDifferentQueries();
            wholeTime += it.getTotalTime();
        }
        SessionItem total = new SessionItem("TOTAL " + totalTime + " ms >>>");
        total.setCountQueries(wholeQueries);
        total.setTotalTime(wholeTime);
        for (int i = 0; i < wholeDifferentQueris; i++) {
            total.addQuery("" + i);
        }
        itemsList.add(total);

        int maxTableNameLength = 12;
        for (SessionItem it : itemsList) {
            if (it.getBaseTable().length() > maxTableNameLength) {
                maxTableNameLength = it.getBaseTable().length();
            }
        }

        System.out.println(String.format("%-" + maxTableNameLength + "s   %s (of total %%) /%s | %s | %s (of total %%)|",
                "Table Name", "  Requests", "Unique Requests", "Useless %", " Time "));
        for (SessionItem it : itemsList) {
            System.out.println(String.format("%-" + maxTableNameLength + "s =     %6d (      %3.0f%%) /         %6d |      %3.0f%% | %10.0fms (%3.0f%%)|",
                    it.getBaseTable(),
                    it.getCountQueries(),
                    it.getCountQueries() * 100.0 / wholeQueries,
                    it.getCountDifferentQueries(),
                    (it.getCountQueries() - it.getCountDifferentQueries()) * 100.0 / it.getCountQueries(),
                    it.getTotalTime(),
                    it.getTotalTime() * 100.0 / wholeTime));
        }

        for (SessionItem it : itemsList) {
            boolean duplicates = (it.getCountDifferentQueries() <= 5) && (it.getCountQueries() > 100);
            if (duplicates == true) {
                List<String> lQueries = it.getQueriesList();
                Collections.sort(lQueries);
                System.out.println("FROM " + it.getBaseTable());
                for (String query : lQueries) {
                    System.out.println(">>> " + query);
                }
            }
        }
        if (slowQueries.size() > 0) {
            System.out.println("### slow queries ###");
            for (String query : slowQueries) {
                System.out.println(query);
            }
        }

        Runtime runtime = Runtime.getRuntime();
        System.out.println(String.format("----------- >> end >> [Memory %4dM Free/ %4dM Total] >> -----------",
                runtime.freeMemory() / (1024 * 1024),
                runtime.totalMemory() / (1024 * 1024)));
        System.out.flush();
    }
}

class SessionItem {
    private String baseTable;
    private double totalTime = 0;
    private int countQueries = 0;
    private Set<String> queries = new LinkedHashSet<String>();

    public SessionItem(String baseTable) {
        super();
        this.baseTable = baseTable;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    public int getCountQueries() {
        return countQueries;
    }

    public int getCountDifferentQueries() {
        return queries.size();
    }

    public void setCountQueries(int _countQueries) {
        countQueries = _countQueries;
    }

    public String getBaseTable() {
        return baseTable;
    }

    public void addQuery(String _qeury) {
        queries.add(_qeury);
    }

    public List<String> getQueriesList() {
        return new ArrayList<String>(queries);
    }
}
