package varausjarjestelma.domain.builder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.Dao;
import varausjarjestelma.domain.serialization.MuuttujaSerializer;

/**
 * Rakentaa tietokannan palauttamasta tietueestaa olion.
 * Voidaan käyttää JOIN-kyselyiden kanssa, kun kohdeluokka sisältää muihin tauluihin viittaavia olioita.
 * 
 * @author Matias
 */
public class TulosLuokkaRakentaja<T> implements RowMapper<T> {

    private Dao<T, ?> dao;
    private Tietokantahallinta thallinta;

    public TulosLuokkaRakentaja(Dao<T, ?> dao, Tietokantahallinta thallinta) {
        this.dao = dao;
        this.thallinta = thallinta;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T instance = BeanUtils.instantiateClass(dao.getResultClass());
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(instance);
        wrapper.setAutoGrowNestedPaths(true);
        ResultSetMetaData meta = rs.getMetaData();
        try {
            for (int i = 0; i < meta.getColumnCount(); i++) {
                Class<?> rclass = Class.forName(meta.getColumnClassName(i + 1));
                String columnName = JdbcUtils.lookupColumnName(meta, i + 1);
                // System.out.println("SAMA LUOKKA EIO " + columnName);
                if (columnName.indexOf("\\.") == -1) {
                    // String uusinim = columnName.substring(0, columnName.indexOf("."));//
                    // .split(".")[0];//.equalsIgnoreCase(resultClass.getSimpleName()))
                    // {
                    // if (uusinim.equalsIgnoreCase(dao.getResultClass().getSimpleName())) {
                    MuuttujaSerializer<ResultSet> deserializer = dao.getSerializer().getDeserializer(columnName);
                   System.out.println("SAMA LUOKKA " + columnName);
                    if (deserializer != null) {
                        wrapper.setPropertyValue(columnName, deserializer.serializeField(rs));
                        continue;
                    }
                    // }
                } else {
                    System.out.println("ERI  LUOKKA " + columnName);
                    String[] parts = columnName.split("\\.");
                    for (String  part : parts) {
                        System.out.println("part " +part);
                    }
                    Dao<?,?> tdao = thallinta.getDaoByResultClassName(parts[0]);
                    System.out.println("tdao " + tdao + " | " + parts[0]);
                    MuuttujaSerializer<ResultSet> deserializer = tdao.getSerializer().getDeserializer(parts[1]);
           
                    if (deserializer != null) {
                        wrapper.setPropertyValue(columnName, deserializer.serializeField(rs));
                        continue;
                    }
                    
                    // substring(0, columnName.indexOf("."));
                }
                // System.out.println("COLUMN SET NAME: " + JdbcUtils.lookupColumnName(meta, i +
                // 1));
                /** if (rclass == Timestamp.class) {
                    Timestamp tstamp = (Timestamp) JdbcUtils.getResultSetValue(rs, i + 1, rclass);
                    wrapper.setPropertyValue(columnName,
                            tstamp.toLocalDateTime());
                } else {*/
                wrapper.setPropertyValue(columnName,
                        JdbcUtils.getResultSetValue(rs, i + 1, rclass));
            }
        } catch (Exception e) {
            // Kääri SQL-virheeksi. koska ne hoidetaan asian mukaisesti.
            throw new SQLException(e);
        }
        return instance;
    }
}