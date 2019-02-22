package varausjarjestelma.database;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Tämän rajapinnan tarkoitus on kääriä Springin tarjoamalla 
 * JdbcTemplate-oliolla tehdyt tietokantakyselyt.
 * Spring muuntaa kaikki SQLExceptionit DataAccessExceptioneiksi. Nämä Springin poikkeukset ovat
 * runtime-poikkeuksia, joihin ei ole pakko varautua. Ohjelman kannalta on kuitenkin tärkeää,
 * että virhetilanteisiin varaudutaan kunnolla. Tämän rajapinnan tehtävä on siis
 * muuntaa nämä poikkeukset takaisin SQLExceptioneiksi.
 * 
 * @author Matias
 */
@FunctionalInterface
public interface JdbcSpringKysely<T> {

    T query(JdbcTemplate jdbcTemp) throws DataAccessException;
}