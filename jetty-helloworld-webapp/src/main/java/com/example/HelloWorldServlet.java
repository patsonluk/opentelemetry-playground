//
//  ========================================================================
//  Copyright (c) Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/test-servlet"}, loadOnStartup = 1)
public class HelloWorldServlet extends HttpServlet
{
  @Override 
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException
  {
      try {
          doHttpGet("https://www.fullstory.com/");
          doHttpGet("https://developers.google.com/apis-explorer");
          String sleepUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + "/sleep";
          doHttpGet(sleepUrl);
          doDb();
      } catch (Exception e) {
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          response.getOutputStream().print("Some internal server error...");
          throw new RuntimeException(e);
      }
      response.getOutputStream().print("Hello World");
  }

    private void doHttpGet(String uri) throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .GET()
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void doDb() throws SQLException {
        try {
            Class.forName ("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try (Connection conn = DriverManager.getConnection ("jdbc:h2:~/test", "sa","")) {
            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM " + H2db.TEST_TABLE)) {
                try (ResultSet resultSet = st.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.println(resultSet.getString("first_name"));
                    }
                }
            }

            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM unknown_table")) { //an unknown table
                try (ResultSet resultSet = st.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.println(resultSet.getString("first_name"));
                    }
                }
            }
        }

    }

}
