package varausjarjestelma.domain.serialization;

import java.sql.SQLException;

/**
 * Määrittää miten muuttuja pitäisi käsitellä.
 * @see varausjarjestelma.domain.serialization.LuokkaSerializer
 * 
 * @author Matias
 */
@FunctionalInterface
public interface MuuttujaSerializer<T> {

    Object serializeField(T data) throws SQLException;
}