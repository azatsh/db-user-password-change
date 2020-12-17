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

import java.util.Properties;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class Main {

  private static final String IGNORE_ERRORS_PROP = "ignore_errors";

  private static final Pattern HELP_PARAMETER_PATTERN = Pattern.compile("^-+((\\bh\\b)|(\\bhelp\\b))");

  private static final String USAGE = "%nUsage:%n"
      + "  java -cp {classpath} org.azatsh.dbpasswordchange.Main {username} {password} {new_password} [db_names]%n"
      + "Options:%n"
      + "  username - db user name%n"
      + "  password - db user password%n"
      + "  new_password - new password%n"
      + "  db_names - comma separated db names (optional, can be set in settings.properties)%n"
      + "Examples:%n"
      + "  java -cp \"./:./libs/*\" org.azatsh.dbpasswordchange.Main testuser qwerty qwerty123%n"
      + "  java -cp \"./:./libs/*\" org.azatsh.dbpasswordchange.Main testuser qwerty qwerty123 test_db%n";

  private Properties props;

  public static void main(String[] args) {
    new Main().start(args);
  }

  private void start(String[] args) {
    if (args.length < 3 || args.length > 4 || isHelpParameter(args[0])) {
      printUsage();
      System.exit(1);
    }

    boolean isSuccess = true;
    val user = args[0];
    val password = args[1];
    val newPassword = args[2];
    val dbNames = args.length > 3 ? args[3] : getProperty("db_names");
    for (val dbName : dbNames.split(",")) {
      val result = processUser(dbName, user, password, newPassword);
      if (!result) {
        isSuccess = false;
        if (notIgnoreErrors()) {
          break;
        }
      }
    }

    printMessage("");
    if (!isSuccess) {
      printMessage("There were some errors, please check logs.");
      if (notIgnoreErrors()) {
        printMessage("The process stopped due to '" + IGNORE_ERRORS_PROP + "' property is set false");
      }
    }
    if (isSuccess || !notIgnoreErrors()) {
      printMessage(
          "DB password change completed" + (isSuccess ? " successfully" : " with errors") + ".");
    }

  }

  private boolean processUser(String dbName, String user, String password, String newPassword) {
    printMessage(String.format("%nIs about to change password for db '%s' and user '%s'", dbName, user));

    val dbUrl = getProperty(dbName + "_connection_url");
    if (dbUrl == null) {
      log.error(String.format("Connection url for DB '%s' must be specified", dbName));
      return false;
    }

    boolean isSuccess = true;
    val dao = new Dao();
    try {
      dao.updatePass(dbUrl, user, password, newPassword);
    } catch (Exception e) {
      log.error("An error while running change password query", e);
      isSuccess = false;
    }
    return isSuccess;
  }

  private boolean isHelpParameter(String param) {
    return HELP_PARAMETER_PATTERN.matcher(param).matches();
  }

  private void printUsage() {
    System.out.println(
        String.format(USAGE) // to replace OS specific line endings
    );
  }

  private void printMessage(String msg) {
    System.out.println(msg); // print to console
    if (!msg.isEmpty()) {
      log.info(msg); // duplicate the same to log file
    }
  }

  private String getProperty(String name) {
    if (props == null) {
      props = new Properties();
      try (val inputStream = Main.class.getResourceAsStream("/settings.properties")) {
        props.load(inputStream);
      } catch (Exception e) {
        log.error("An error loading properties", e);
      }
    }
    return props.getProperty(name);
  }

  private boolean getBooleanProperty(String name, boolean defaultValue) {
    val prop = getProperty(name);
    return prop == null || prop.isEmpty() ? defaultValue :
        "true".equalsIgnoreCase(prop) || "yes".equalsIgnoreCase(prop) || "y"
            .equalsIgnoreCase(prop);
  }

  private boolean notIgnoreErrors() {
    return !getBooleanProperty(IGNORE_ERRORS_PROP, true);
  }

}
