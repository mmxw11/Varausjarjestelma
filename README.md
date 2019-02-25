# Tietokantojen perusteet 2019 - Ohjelmointiprojekti 
### Hotellihuoneiden Varausjärjestelmä
https://github.com/mmxw11/Varausjarjestelma

Sovelluksen rakenne:
 * varausjarjestelma.database ->Sisältää tietokannan kanssa kommunikoimiseen tarvittavat koodit.
 * varausjarjestelma.database.dao.* -> Näiden luokkien kautta tietokantaa käytetään.
 * varausjarjestelma.domain -> Sisältää kaikki POJO-oliot.
 * varausjarjestelma.domain.serialization.* -> Automaattinen POJO-luokkien ja niistä SQL-kyselyjen luonti.
 * varausjarjestelma.ui.* -> Sisältää käyttöliittymä koodin.
