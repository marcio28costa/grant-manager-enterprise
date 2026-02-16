package db;

import config.ConfigLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    public static Connection getConnection() throws SQLException {
        String host = ConfigLoader.get("db.host");
        String port = ConfigLoader.get("db.port");
        String db   = ConfigLoader.get("db.name");
        String user = ConfigLoader.get("db.user");
        String pass = ConfigLoader.get("db.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db +
                "?useSSL=false&serverTimezone=UTC";

        return DriverManager.getConnection(url, user, pass);
    }
}
