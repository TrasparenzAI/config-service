/*
 * Copyright (C) 2024 Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.cnr.anac.transparency.configserver.data;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository delle proprietà, esposto tramite Spring Data REST al path /properties.
 * I metodi di ricerca sono disponibili al path /properties/search.
 */
public interface PropertyRepository extends ListCrudRepository<Property, Long>{

  /**
   * Proprietà con la chiave indicata, per tutte le applicazioni e i profili.
   */
  List<Property> findByKey(@Param("key") String key);

  /**
   * Proprietà di una applicazione e di un profilo con la chiave indicata.
   * Possono essere presenti più proprietà con la stessa chiave e label diverse.
   */
  List<Property> findByApplicationAndProfileAndKey(
      @Param("application") String application,
      @Param("profile") String profile,
      @Param("key") String key);
}