package varausjarjestelma.database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tallentaa taulujen väliset liitokset käytettäväksi kyselyyn.
 * 
 * @author Matias
 */
public class SQLJoinVarasto {

    private Map<String, List<String>> joinClauses;

    public SQLJoinVarasto() {
        this.joinClauses = new LinkedHashMap<>(); // Järjestys on erittäin olennainen asia.
    }

    public SQLJoinVarasto(Map<String, List<String>> joinClauses) {
        this.joinClauses = new LinkedHashMap<>(joinClauses);
    }

    /**
     * Lisää uuden liitoslausekkeen.
     * @param joinedTableName Taulun nimi, johon joinataan
     * @param join Lauseke
     * @return this
     */
    public SQLJoinVarasto addSQLJoinClause(String joinedTableName, String sql) {
        List<String> clauses = joinClauses.get(joinedTableName);
        if (clauses == null) {
            clauses = new ArrayList<>();
            joinClauses.put(joinedTableName, clauses);
        }
        clauses.add(sql);
        return this;
    }

    public Map<String, List<String>> getJoinClauses() {
        return joinClauses;
    }
}