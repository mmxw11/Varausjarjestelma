package varausjarjestelma.domain.builder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import varausjarjestelma.database.dao.Dao;

/**
 * Mikäli tietokannasta haetaan olioiden tietoka JOIN-kyselyillä,
 * tulee nämä JOIN-kyselyiden kohde tauluja vastaavat muuttujat merkitä tällä annotaatiolla.
 * 
 * @author Matias
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinLuokka {

    /**
     * @return Palauttaa luokkaa vastaavan DAO-luokan
     */
    Class<? extends Dao<?, ?>> value();
}