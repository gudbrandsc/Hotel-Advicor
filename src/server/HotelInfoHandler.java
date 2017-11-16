package server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static server.LoginBaseServlet.log;

/**
 * Handles all database-related actions. Uses singleton design pattern.
 * Example of Prof. Engle
 *
 * @see LoginServer
 */
public class HotelInfoHandler {

    /**
     * A {@link org.apache.log4j.Logger log4j} logger for debugging.
     */
    private static Logger log = LogManager.getLogger();

    /**
     * Makes sure only one database handler is instantiated.
     */
    private static HotelInfoHandler singleton = new HotelInfoHandler();

    /**
     * Used to determine if necessary tables are provided.
     */
    private static final String CHECK_HOTEL_INFO_TABLE_SQL =
            "SHOW TABLES LIKE 'hotel_info';";

    /**
     * Create table for users
     */
    private static final String CREATE_SQL_HOTEL_INFO =
            "CREATE TABLE hotel_info (" +
                    "hotelId VARCHAR(32) PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL, " +
                    "city VARCHAR(20) NOT NULL, " +
                    "state VARCHAR(10) NOT NULL, " +
                    "address VARCHAR(50) NOT NULL," +
                    "latitude FLOAT(32) NOT NULL," +
                    "longitude FLOAT(32) NOT NULL);";

    /**
     * Used to insert a new user into the database.
     */
    private static final String INSERT_HOTEL_SQL =
            "INSERT INTO hotel_info (hotelId, username, city, state, address, latitude, longitude) " +
                    "VALUES (?,?,?,?,?,?,?);";


    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;

    /**
     * Used to generate password hash salt for user.
     */
    private Random random;

    /**
     * Initializes a database handler for the Login example. Private constructor
     * forces all other classes to use singleton.
     */
    private HotelInfoHandler() {
        Status status = Status.OK;
        random = new Random(System.currentTimeMillis());

        try {
            // TODO Change to "database.properties" or whatever your file is called
            db = new DatabaseConnector("database.properties");
            status = db.testConnection() ? setupHotelInfoTables() : Status.CONNECTION_FAILED;
        } catch (FileNotFoundException e) {
            status = Status.MISSING_CONFIG;
        } catch (IOException e) {
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
    public static HotelInfoHandler getInstance() {
        return singleton;
    }

    /**
     * Checks to see if a String is null or empty.
     *
     * @param text - String to check
     * @return true if non-null and non-empty
     */
    public static boolean isBlank(String text) {
        return (text == null) || text.trim().isEmpty();
    }

    /**
     * Checks if necessary table exists in database, and if not tries to
     * create it.
     *
     * @return {@link Status.OK} if table exists or create is successful
     */
    private Status setupHotelInfoTables() {
        Status status = Status.ERROR;

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement();
        ) {
            if (!statement.executeQuery(CHECK_HOTEL_INFO_TABLE_SQL).next()) {
                // Table missing, must create
                log.debug("Creating user table...");
                System.out.println("Create table");
                statement.executeUpdate(CREATE_SQL_HOTEL_INFO);

                // Check if create was successful
                if (!statement.executeQuery(CHECK_HOTEL_INFO_TABLE_SQL).next()) {
                    System.out.println("table fail");
                    status = Status.CREATE_FAILED;
                } else {
                    status = Status.OK;
                    System.out.println("Hello");
                    Path curr = Paths.get("input/hotels4.json");
                    System.out.println(curr.toAbsolutePath());
                    System.out.println();
                    loadHotelInfo(curr.toAbsolutePath().toString());

                }
            } else {
                log.debug("Usertable found.");
                status = Status.OK;
            }
        } catch (Exception ex) {
            status = Status.CREATE_FAILED;
            log.debug(status, ex);
        }

        return status;
    }


    /**
     * Read the given json file with information about the hotels (check hotels.json to see the expected format)
     * and load it into the appropriate data structure(s).
     * Do not hardcode the name of the file! the could should work on any json file in the same format.
     * You may use JSONSimple library for parsing a JSON file.
     */
    public void loadHotelInfo(String jsonFilename) {
        Status status = Status.ERROR;
        JSONParser parser = new JSONParser();
        try {
            Connection connection = db.getConnection();

            try (
                    PreparedStatement statement = connection.prepareStatement(INSERT_HOTEL_SQL);
            ) {
                statement.setString(1, "hello");
                statement.setString(2, "heo");
                statement.setString(3, "sdsd");
                statement.setString(4, "ffdf");
                statement.setString(5, "sdfsdf");
                statement.setFloat(6, 123);
                statement.setFloat(7, 12344.123f);
                statement.executeUpdate();
                System.out.println("Done success");
                status = Status.OK;
            }
            catch (SQLException ex) {
                status = Status.SQL_EXCEPTION;
                log.debug(ex.getMessage(), ex);
            }


            JSONObject obj = (JSONObject) parser.parse(new FileReader(jsonFilename));
            JSONArray arr = (JSONArray) obj.get("sr");
            Iterator<JSONObject> iterator = arr.iterator();
            while (iterator.hasNext()) {
                JSONObject res = iterator.next();
                JSONObject coords = (JSONObject) res.get("ll");
                String hotelId= (String) res.get("id");
                String name = (String) res.get("f");//TODO SUBSTRING
                String city = (String) res.get("ci");
                String state = (String) res.get("pr");
                String address= (String) res.get("id");
                float lat = Float.parseFloat((String) coords.get("lat"));
                float lng = Float.parseFloat((String) coords.get("lng"));
                System.out.println(hotelId+","+ name+","+city+","+state+","+address+lat+lng);
                try (
                        PreparedStatement statement = connection.prepareStatement(INSERT_HOTEL_SQL);
                ) {

                    statement.setString(1, hotelId);
                    statement.setString(2, name);
                    statement.setString(3, city);
                    statement.setString(4, state);
                    statement.setString(5, address);
                    statement.setFloat(6, lat);
                    statement.setFloat(7, lng);
                    System.out.println("before");

                    statement.executeUpdate();
                    System.out.println("after");
                    status = Status.OK;
                }
                catch (SQLException ex) {
                    System.out.println("catch");
                    System.out.println(ex.getMessage());
                    status = Status.SQL_EXCEPTION;
                    log.debug(ex.getMessage(), ex);
                }

            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}

