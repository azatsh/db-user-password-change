/*
 * Copyright (C) 2020 Azat Sharipov <azatsh@yandex.ru>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.azatsh.dbpasswordchange;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

@Slf4j
public class Dao {

  private static final String ORACLE_QUERY = "alter user %s identified by \"%s\" replace \"%s\"";
  private static final String POSTGRESQL_QUERY = "alter user %s with password \"%s\"";
  private static final String SQLSERVER_QUERY = "alter login %s with password = \"%s\"";
  private static final String MYSQL_QUERY = "alter user %s identified by \"%s\"";

  public void updatePass(String dbUrl, String dbUser, String password, String newPassword) {
    var query = "";
    val dbms = getDbmsName(dbUrl);
    switch (dbms) {
      case "oracle":
        query = ORACLE_QUERY;
        break;
      case "postgresql":
        query = POSTGRESQL_QUERY;
        break;
      case "sqlserver":
        query = SQLSERVER_QUERY;
        break;
      case "mysql":
        query = MYSQL_QUERY;
        break;
      default:
        throw new IllegalArgumentException("Unsupported dbms: " + dbms);
    }

    try (Connection con = DriverManager.getConnection(dbUrl, dbUser, password);
        CallableStatement stmt = con.prepareCall(String.format(query, dbUser, newPassword, password))) {
      con.setAutoCommit(true);
      stmt.execute();
    } catch (SQLException e) {
      log.error("SQL Error occurred", e);
      throw new IllegalStateException(e);
    }
  }

  private String getDbmsName(String dbUrl) {
    val parts = dbUrl.split(":");
    if (parts.length < 3 || !"jdbc".equalsIgnoreCase(parts[0])) {
      throw new IllegalArgumentException(String.format("The jdbc url is incorrect: '%s'", dbUrl));
    }
    return parts[1].toLowerCase();
  }

}
