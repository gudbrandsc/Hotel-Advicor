package database;

import server.DatabaseConnector;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for creating a table for users.
 * Checks if database exist, and if it does deletes it and creates a new one
 *
 *
 */
public class UserHandler {

    /** A {@link org.apache.log4j.Logger log4j} logger for debugging. */
    private static Logger log = LogManager.getLogger();

    /** Makes sure only one database handler is instantiated. */
    private static UserHandler singleton = new UserHandler();

    /** Used to determine if necessary tables are provided. */
    private static final String CHECK_LOGIN_USER_TABLE_SQL =
            "SHOW TABLES LIKE 'login_users';";

    /** Used to drop user table */
    private static final String DROP_USER_TABLE_SQL =
            "DROP TABLE login_users;";

    /** Create table for users */
    private static final String CREATE_SQL_LOGIN_USERS =
            "CREATE TABLE login_users (" +
                    "userid INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(32) NOT NULL UNIQUE, " +
                    "password CHAR(64) NOT NULL, " +
                    "usersalt CHAR(32) NOT NULL);";


    /** Used to configure connection to database. */
    private DatabaseConnector db;

    /** Used to generate password hash salt for user. */
    private Random random;

    /**
     * Initializes a database handler for the Login example. Private constructor
     * forces all other classes to use singleton.
     */
    private UserHandler() {
        Status status = Status.OK;
        random = new Random(System.currentTimeMillis());

        try {
            // TODO Change to "database.properties" or whatever your file is called
            db = new DatabaseConnector("database.properties");
            status = db.testConnection() ? setupTables() : Status.CONNECTION_FAILED;
        }
        catch (FileNotFoundException e) {
            status = Status.MISSING_CONFIG;
        }
        catch (IOException e) {
            status = Status.MISSING_VALUES;
        }

        if (status != Status.OK) {
            log.fatal(status.message());
        }
    }

    /**
     * Gets the single instance of the database handler.
     *
     * @return instance of the database handler
     */
    public static UserHandler getInstance() {
        return singleton;
    }

    /**
     * Checks if necessary table exists in database, and if it does deletes it and creates a new.
     * If it does not exist just create new table
     *
     * @return {@link Status.OK} if table exists or create is successful
     */
    private Status setupTables() {
        Status status = Status.ERROR;

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement();
        ) {
            if (!statement.executeQuery(CHECK_LOGIN_USER_TABLE_SQL).next()) {
                // Table missing, must create
                log.debug("Creating user table...");
                statement.executeUpdate(CREATE_SQL_LOGIN_USERS);

            } else {
                log.debug("User table found.");
                statement.executeUpdate(DROP_USER_TABLE_SQL);
                log.debug("Deleting existing user table.");
                log.debug("Creating new user table...");
                statement.executeUpdate(CREATE_SQL_LOGIN_USERS);
            }
            // Check if create was successful
            if (!statement.executeQuery(CHECK_LOGIN_USER_TABLE_SQL).next()) {
                log.debug("User table failed to be created");
                status = Status.CREATE_FAILED;
            }
            else {
                log.debug("User table created");
                status = Status.OK;
            }
        }
        catch (Exception ex) {
            status = Status.CREATE_FAILED;
            log.debug(status, ex);
        }

        return status;
    }

}
