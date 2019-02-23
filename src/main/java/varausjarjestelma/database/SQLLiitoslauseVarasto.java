package varausjarjestelma.database;

import java.util.ArrayList;
import java.util.List;

public class SQLLiitoslauseVarasto {

    private List<String> joinLiitokset;

    public SQLLiitoslauseVarasto() {
        this.joinLiitokset = new ArrayList<>();
    }

    public void addSQLJoin(String sql) {
        joinLiitokset.add(sql);
    }

    public List<String> getJoinLiitokset() {
        return joinLiitokset;
    }
}
