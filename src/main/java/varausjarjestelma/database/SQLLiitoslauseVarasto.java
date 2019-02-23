package varausjarjestelma.database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tallentaa taulujen väliset liitokset
 * 
 * @author Matias
 */
public class SQLLiitoslauseVarasto {

    private Map<String, List<String>> joinClauses;

    public SQLLiitoslauseVarasto() {
        this.joinClauses = new LinkedHashMap<>(); // Järjestys on erittäin olennainen asia.
    }

    /**
     * Lisää uuden liitoslauseen.
     * @param joinedTableName Taulun nimi, johon joinataan
     * @param join
     */
    public void addSQLJoinClause(String joinedTableName, String sql) {
        List<String> clauses = joinClauses.get(joinedTableName);
        if (clauses == null) {
            clauses = new ArrayList<>();
            joinClauses.put(joinedTableName, clauses);
        }
        clauses.add(sql);
    }

    public Map<String, List<String>> getJoinClauses() {
        return joinClauses;
    }
}