package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class H2db {
    static final String TEST_TABLE = "test_table";
    public static void main(String[] args) throws Exception {
        //init a sample DB
        Class.forName ("org.h2.Driver");
        try (Connection conn = DriverManager.getConnection ("jdbc:h2:~/test", "sa","")) {
            try (PreparedStatement st = conn.prepareStatement("DROP TABLE IF EXISTS " + TEST_TABLE)) {
                st.execute();
            }
            try (PreparedStatement st = conn.prepareStatement("CREATE TABLE " + TEST_TABLE + "(id int NOT NULL AUTO_INCREMENT, first_name VARCHAR(255), last_name VARCHAR(255))")) {
                st.execute();
            }
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO " + TEST_TABLE + "(first_name, last_name) VALUES(?,?)")) {
                for (int i = 0 ; i < 20 ; i ++) {
                    st.setString(1, String.valueOf((char)('a' + i)));
                    st.setString(2, String.valueOf((char)('A' + i)));
                    st.addBatch();
                }
                st.executeBatch();
            }
        }
    }

}
