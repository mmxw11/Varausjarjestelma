package varausjarjestelma.domain.serialization;

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

/**
 * Rakentaa tietokannan palauttamasta tietueestaa olion.
 * Voidaan käyttää myös JOIN-kyselyiden kanssa, kun kohdeluokka sisältää muihin tauluihin viittaavia olioita.
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
                mapField(wrapper, meta, rs, i + 1);
            }
        } catch (Exception e) {
            // Kääri SQL-virheeksi. koska ne hoidetaan asian mukaisesti.
            throw new SQLException(e);
        }
        return instance;
    }

    private void mapField(BeanWrapper wrapper, ResultSetMetaData meta, ResultSet rs, int i) throws SQLException, ClassNotFoundException {
        String columnName = JdbcUtils.lookupColumnName(meta, i);
        String[] columnNameParts = columnName.split("\\.");
        MuuttujaSerializer<ResultSet> deserializer;
        if (columnNameParts.length > 1) {
            // Sisäinen luokka (oliomuuttuja tai vastaava)
            Dao<?, ?> tdao = thallinta.getDaoByResultClassName(columnNameParts[0]);
            deserializer = tdao != null ? tdao.getSerializer().getDeserializer(columnNameParts[1]) : null;
        } else {
            deserializer = dao.getSerializer().getDeserializer(columnName);
        }
        if (deserializer != null) {
            wrapper.setPropertyValue(columnName, deserializer.serializeField(rs));
            return;
        }
        Class<?> rclass = Class.forName(meta.getColumnClassName(i));
        wrapper.setPropertyValue(columnName, JdbcUtils.getResultSetValue(rs, i, rclass));
    }
}