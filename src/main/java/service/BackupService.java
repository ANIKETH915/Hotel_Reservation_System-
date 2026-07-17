package service;

import database.DatabaseConnection;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BackupService {

    public void backupToFile(Path path) throws Exception {
        Files.createDirectories(path.getParent() != null ? path.getParent() : Path.of("."));

        try (Connection conn = DatabaseConnection.getConnection();
             BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {

            writer.write("-- Hotel Reservation System backup");
            writer.newLine();
            writer.write("-- Generated: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            writer.newLine();
            writer.newLine();

            List<String> tables = listTables(conn);
            for (String table : tables) {
                writeCreateTable(conn, writer, table);
                writer.newLine();
                writeInserts(conn, writer, table);
                writer.newLine();
            }
        }
    }

    private List<String> listTables(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SHOW TABLES")) {
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        }
        return tables;
    }

    private void writeCreateTable(Connection conn, BufferedWriter writer, String table) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SHOW CREATE TABLE `" + table + "`")) {
            if (rs.next()) {
                writer.write(rs.getString(2) + ";");
                writer.newLine();
            }
        }
    }

    private void writeInserts(Connection conn, BufferedWriter writer, String table) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM `" + table + "`")) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder("INSERT INTO `").append(table).append("` VALUES (");
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) {
                        sb.append(", ");
                    }
                    Object value = rs.getObject(i);
                    sb.append(formatSqlValue(value));
                }
                sb.append(");");
                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }

    private String formatSqlValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof byte[]) {
            return "0x" + bytesToHex((byte[]) value);
        }
        String str = value.toString();
        return "'" + str.replace("\\", "\\\\").replace("'", "''") + "'";
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}
