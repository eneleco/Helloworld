/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import static javax.measure.unit.SI.KILOGRAM;
import javax.measure.quantity.Mass;
import org.jscience.physics.model.RelativisticModel;
import org.jscience.physics.amount.Amount;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Controller
@RestController
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  private static final String template = "Full Name from DB: %s";
  private final AtomicLong counter = new AtomicLong();


@RequestMapping("/hello")
String hello(Map<String, Object> model) {
    RelativisticModel.select();
    String energy = System.getenv().get("ENERGY");
    if (energy == null) {
       energy = "12 GeV";
    }
    Amount<Mass> m = Amount.valueOf(energy).to(KILOGRAM);
    model.put("science", "E=mc^2: " + energy + " = "  + m.toString());
    return "hello";
}

  @RequestMapping("/")
  String index() {
    return "index";
  }


  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      //stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      //stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      //ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
      ResultSet rs = stmt.executeQuery("SELECT id, firstname, lastname, sfid FROM salesforce.contact");
      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        //output.add("Result From DB: " + rs.getTimestamp("tick"));
        output.add("Result From DB: " + rs.getString("id") + " " + rs.getString("firstname") + " " + rs.getString("lastname") + " " + rs.getString("sfid"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }


  @RequestMapping("/fullname")
  public Fullname fullname(@RequestParam(value="recordId", defaultValue="1") String recordId) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String queryStr = "SELECT id, firstname, lastname, sfid FROM salesforce.contact WHERE id = " + recordId;
      String outStr = new String("no result");
      ResultSet rs = stmt.executeQuery(queryStr);
      while (rs.next()) {
        outStr = rs.getString("firstname") + " " + rs.getString("lastname");
      }
      //return outStr;
      return new Fullname(counter.incrementAndGet(),
                            String.format(template, outStr));
    }catch (Exception e) {
      return new Fullname(counter.incrementAndGet(),
                            String.format(template, "error:" + e.getMessage()));
    }
  } 



  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}
