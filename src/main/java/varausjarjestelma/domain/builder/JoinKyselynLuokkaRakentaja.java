package varausjarjestelma.domain.builder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * Rakentaa tietokannan palauttamasta tietueestaa olion.
 * Käytetään JOIn-kyselyiden kanssa, kun kohdeluokka sisältää oliomuuttujia.
 * 
 * @author Matias
 */
public class JoinKyselynLuokkaRakentaja<T> implements RowMapper<T> {

    private Class<T> resultClass;

    public JoinKyselynLuokkaRakentaja(Class<T> resultClass) {
        this.resultClass = resultClass;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T instance = BeanUtils.instantiateClass(resultClass);
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(instance);
        wrapper.setAutoGrowNestedPaths(true);
        ResultSetMetaData meta = rs.getMetaData();
        try {
            for (int i = 0; i < meta.getColumnCount(); i++) {
                Class<?> rclass = Class.forName(meta.getColumnClassName(i + 1));
                wrapper.setPropertyValue(JdbcUtils.lookupColumnName(meta, i + 1),
                        JdbcUtils.getResultSetValue(rs, i + 1, rclass));
            }
        } catch (Exception e) {
            // Kääri SQL-virheeksi. koska ne hoidetaan asian mukaisesti.
            throw new SQLException(e);
        }
        return instance;
    }
}
