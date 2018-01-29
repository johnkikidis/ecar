package org.kikidis;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@Scope("request")
public class PowerController {
    private static final DateTimeFormatter sqlPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    private final Connection conn;

    public PowerController(Connection conn) {
        this.conn = conn;
    }


    @RequestMapping(value = "/provider", method = RequestMethod.POST, produces = "application/json")
    public String producer(@RequestBody JsonNode json) throws Exception {

        if (json.get("name") == null || json.get("name").asText().isEmpty()) {
            return "{\"error\": \"missing name\"}";
        }

        if (json.get("power") == null || !json.get("power").isArray() || json.get("power").size() != 24) {
            return "{\"error\": \"power property should an array of 24 items\"}";
        }

        for (JsonNode item : json.get("power")) {
            if (!item.isNumber()) {
                return "{\"error\": \"all power per hour must be numbers\"}";
            }
        }

        Long insertId = 0L;
        conn.setAutoCommit(false);
        try (PreparedStatement st = conn.prepareStatement("INSERT INTO providers (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, json.get("name").asText());
            st.executeUpdate();
            ResultSet cursor = st.getGeneratedKeys();
            if (cursor.next()) {
                insertId = cursor.getLong(1);
            }
        }

        try (PreparedStatement st = conn.prepareStatement("INSERT INTO hourly_power (fk_provider_id, hour, power) VALUES (?,?,?)")) {
            int i = 0;
            for (JsonNode power : json.get("power")) {
                st.setLong(1, insertId);
                st.setLong(2, i++);
                st.setBigDecimal(3, power.decimalValue());
                st.addBatch();
            }
            st.executeBatch();
        }

        conn.commit();
        return "{\"status\": \"OK\", \"id\": " + insertId + "}";
    }

    @RequestMapping(value = "/available", method = RequestMethod.GET, produces = "application/json")
    public String ask(@RequestParam("power") BigDecimal val) throws Exception {
        return "{\"available\": " + available(val) + "}";
    }

    @RequestMapping(value = "/book", method = RequestMethod.POST, produces = "application/json")
    public String book(@RequestParam("power") BigDecimal val, @RequestParam("name") String name) throws Exception {
//        Long insertId = 0L;
        BigDecimal available = available(val);
        if (available.subtract(val).compareTo(BigDecimal.ZERO) >= 0) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO client_charges (name, booking_date, power) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, name);
                st.setString(2, LocalDateTime.now().format(sqlPattern));
                st.setBigDecimal(3, val);
                st.executeUpdate();
//                ResultSet cursor = st.getGeneratedKeys();
////                if (cursor.next()) {
////                    insertId = cursor.getLong(1);
////                }
            }
            return "{\"success\": true}";
        } else {
            return "{\"success\": false, \"message\": \"There is no available power. Only " + available + " remaining.\"}";
        }
    }

    private BigDecimal available(BigDecimal val) throws Exception {
        LocalDateTime start = LocalDate.now().atTime(LocalDateTime.now().getHour(), 0);
        LocalDateTime end = start.plusHours(1);
        String sqlStart = start.format(sqlPattern);
        String sqlEnd = end.format(sqlPattern);
        try (PreparedStatement st = conn.prepareStatement("SELECT (SELECT IFNULL(SUM(p.power), 0) FROM hourly_power AS p WHERE p.hour = ?) - (SELECT IFNULL(SUM(a.power), 0) FROM client_charges AS a WHERE a.booking_date BETWEEN ? AND ?)")) {
            st.setInt(1, start.getHour());
            st.setString(2, sqlStart);
            st.setString(3, sqlEnd);
            ResultSet rows = st.executeQuery();
            rows.next();
            return new BigDecimal(rows.getString(1)).min(val).max(BigDecimal.ZERO);
        }
    }

}
