package com.hrishabh.problemservice.dailychallenge.scheduler;

import com.hrishabh.problemservice.dailychallenge.config.PotdSchedulerProperties;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class PotdMysqlSchedulerLock {

    private final DataSource dataSource;
    private final PotdSchedulerProperties properties;

    public PotdMysqlSchedulerLock(DataSource dataSource, PotdSchedulerProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
    }

    /**
     * Acquires a MySQL named lock on a dedicated connection. Caller must {@link #release} on the same connection.
     */
    public Connection tryAcquire() throws SQLException {
        Connection connection = dataSource.getConnection();
        try (PreparedStatement ps = connection.prepareStatement("SELECT GET_LOCK(?, ?)")) {
            ps.setString(1, properties.getMysqlLockName());
            ps.setInt(2, properties.getLockAcquisitionTimeoutSeconds());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 1) {
                    return connection;
                }
            }
        }
        connection.close();
        return null;
    }

    public void release(Connection connection) {
        if (connection == null) {
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            ps.setString(1, properties.getMysqlLockName());
            ps.executeQuery();
        } catch (SQLException ignored) {
            // best-effort release
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
