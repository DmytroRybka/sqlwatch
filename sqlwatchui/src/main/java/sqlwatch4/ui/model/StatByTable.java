package sqlwatch4.ui.model;

import com.google.inject.internal.Preconditions;
import sqlwatch4.model.Trace;
import sqlwatch4.ui.adapters.CountingMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author dmitry.mamonov
 */

public class StatByTable implements Comparable<StatByTable> {
    String queryType;
    String baseTable;
    CountingMap<String> uniqueSql = new CountingMap<String>();
    CountingMap<String> uniqueStructureSql = new CountingMap<String>();
    List<Trace> traces = new ArrayList<Trace>(1);
    long totalDuration = 0L;
    long globalTotalCount = 1;
    long globalTotalDuration = 0L;

    public StatByTable(Trace trace) {
        Preconditions.checkArgument(trace.getKind() == Trace.Kind.SqlTiming);
        final String[] sqlSplit = trace.getSql().trim().toLowerCase().split("\\s");
        this.queryType = extractQueryType(sqlSplit);
        this.baseTable = extractBaseTable(sqlSplit);
        this.totalDuration = trace.getExecTime();
        this.traces.add(trace);
        this.uniqueSql.put(trace.getSql());
        this.uniqueStructureSql.put(trace.getSql()
                .trim()
                .replaceAll("'[^']*'", "?")
                .replaceAll("-?[0-9]+", "?"));
    }

    public void merge(StatByTable merge) {
        Preconditions.checkArgument(getQueryType().equals(merge.getQueryType()));
        Preconditions.checkArgument(getBaseTable().equals(merge.getBaseTable()));
        this.uniqueSql.putAll(merge.getUniqueSql());
        this.uniqueStructureSql.putAll(merge.getUniqueStructureSql());
        this.traces.addAll(merge.getTraces());
        this.totalDuration += merge.getTotalDuration();
    }

    public void setGlobalTotalCount(long globalTotalCount) {
        Preconditions.checkArgument(globalTotalCount > 0);
        this.globalTotalCount = globalTotalCount;
    }

    public void setGlobalTotalDuration(long globalTotalDuration) {
        this.globalTotalDuration = globalTotalDuration;
    }


    protected long getGlobalTotalDuration() {
        if (globalTotalDuration <= 0) {
            return totalDuration;
        } else {
            return globalTotalDuration;
        }
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public CountingMap<String> getUniqueSql() {
        return uniqueSql;
    }

    public CountingMap<String> getUniqueStructureSql() {
        return uniqueStructureSql;
    }

    private String extractBaseTable(String[] sqlSplit) {
        for (int i = 0; i < sqlSplit.length - 1; i++) {
            String sqlItem = sqlSplit[i];
            if ("from".equals(sqlItem) || "into".equals(sqlItem) || "update".equals(sqlItem)) {
                return sqlSplit[i + 1].replaceAll("[(]].*","()");
            }
        }
        return "Unknown";
    }

    private String extractQueryType(String[] sqlSplit) {
        if (sqlSplit.length > 0) {
            return sqlSplit[0];
        } else {
            return "EmptyQuery";
        }
    }


    public List<Trace> getTraces() {
        return traces;
    }

    public String getBaseTable() {
        return baseTable;
    }

    public String getQueryType() {
        return queryType;
    }

    public int getRequestsCount() {
        return traces.size();
    }

    public int getUniqueRequestsCount() {
        return uniqueSql.size();
    }

    public int getUniqueStructureRequestsCount() {
        return uniqueStructureSql.size();
    }

    public double getUselessSqlPercent() {
        return (traces.size() - uniqueSql.size()) / (double) traces.size();
    }

    public String getUselessSqlPercentStr() {
        return String.format("%5.2f%%", getUselessSqlPercent() * 100.0);
    }

    public double getUselessStructureSqlPercent() {
        return (traces.size() - uniqueStructureSql.size()) / (double) traces.size();
    }

    public String getUselessStructureSqlPercentStr() {
        return String.format("%5.2f%%", getUselessStructureSqlPercent() * 100.0);
    }

    public double getRequestCountOfTotalPercent() {
        return traces.size() / (double) globalTotalCount;
    }

    public String getRequestCountOfTotalPercentStr() {
        return String.format("%5.2f%%", getRequestCountOfTotalPercent() * 100.0);
    }

    public String getDurationMs() {
        return Math.round((double) totalDuration / 1000000.0) + "ms";
    }

    public double getDurationPercent() {
        return getTotalDuration() / (double) getGlobalTotalDuration();
    }

    public String getDurationPercentStr() {
        return String.format("%5.2f%%", getDurationPercent() * 100.0);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatByTable that = (StatByTable) o;

        if (baseTable != null ? !baseTable.equals(that.baseTable) : that.baseTable != null) return false;
        if (queryType != null ? !queryType.equals(that.queryType) : that.queryType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = queryType != null ? queryType.hashCode() : 0;
        result = 31 * result + (baseTable != null ? baseTable.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(StatByTable other) {
        return (int) Math.signum(this.getTraces().size() - other.getTraces().size());
    }
}
