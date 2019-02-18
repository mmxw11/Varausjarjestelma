package varausjarjestelma.database;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Rajapinnan tarkoitus on kääriä Springin tarjoamalla 
 * JdbcTemplate-oliolla tehdyt tietokantakyselyt.
 * Spring muuntaa kaikki SQLExceptionit DataAccessExceptioneiksi. Nämä Springin poikkeukset ovat
 * runtime-poikkeuksia, joihin ei ole pakko varautua. Tämän rajapinann tehtävä on siis
 * muuntaa nämä poikkeukset takaisin SQLExceptioneiksi.
 */
@FunctionalInterface
public interface JdbcSpringKysely<T> {

    T query(JdbcTemplate jdbcTemplate) throws DataAccessException;
}