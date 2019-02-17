package varausjarjestelma.database;

public class SQLKyselyAsetukset {

    private int rowLimit;

    private SQLKyselyAsetukset() {
        this.rowLimit = -1;
    }

    public static SQLKyselyAsetukset newOptions() {
        return new SQLKyselyAsetukset();
    }

    public SQLKyselyAsetukset setRowLimit(int rowLimit) {
        this.rowLimit = rowLimit;
        return this;
    }

    public int getRowLimit() {
        return rowLimit;
    }
}