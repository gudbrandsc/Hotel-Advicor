package database;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//TODO CHECK JAVADOC FOR ALL BUILDER FILES
/**
 * Handles all database-related actions. Uses singleton design pattern.
 * Example of Prof. Engle
 *
 * @see LoginServer
 */
public class HotelReviewBuilder {

    /**
     * A {@link org.apache.log4j.Logger log4j} logger for debugging.
     */
    private static Logger log = LogManager.getLogger();

    /**
     * Makes sure only one database handler is instantiated.
     */
    private static HotelReviewBuilder singleton = new HotelReviewBuilder();

    /**
     * Used to determine if hotel_review  table exist.
     */
    private static final String CHECK_HOTEL_REVIEW_TABLE_SQL =
            "SHOW TABLES LIKE 'hotel_review';";

    /** Used to drop hotel_review table */
    private static final String DROP_HOTEL_REVIEW_TABLE_SQL =
            "DROP TABLE hotel_review;";

    /**
     * Create table for hotel reviews
     */
    private static final String CREATE_HOTEL_REVIEW_TABLE_SQL =
            "CREATE TABLE hotel_review (" +
                    "hotelId VARCHAR(50) PRIMARY KEY," +
                    "reviewId VARCHAR(100) NOT NULL, " +
                    "intRating INTEGER (50) NOT NULL, " +
                    "title VARCHAR(50) NOT NULL, " +
                    "review VARCHAR(255) NOT NULL," +
                    "submissiondate FLOAT(50) NOT NULL," +
                    "username FLOAT(50) NOT NULL);";

    /**
     * Used to insert a new user into the database.
     */
    private static final String INSERT_HOTEL_SQL =
            "INSERT INTO hotel_info (hotelId, hotelnames, city, state, address, latitude, longitude) " +
                    "VALUES (?,?,?,?,?,?,?);";


    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;



    /**
     * Initializes a database handler for the Login example. Private constructor
     * forces all other classes to use singleton.
     */
    private HotelInfoBuilder() {
        Status status = Status.OK;
        //List that contains all hotels files that u want in your db
        ArrayList<String> list = new ArrayList<String>();
        list.add("hotels.json");

        try {
            db = new DatabaseConnector("database.properties");
            status = db.testConnection() ? setupTables(list) : Status.CONNECTION_FAILED;
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
    public static HotelInfoBuilder getInstance() {
        return singleton;
    }

    /**
     * Checks if necessary table exists in database, and if it does deletes it and creates a new.
     * If it does not exist just create new table
     *
     * @return {@link Status.OK} if table exists or create is successful
     */
    private Status setupTables(ArrayList<String> hotelFiles) {
        Status status = Status.ERROR;

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement();
        ) {
            if (!statement.executeQuery(CHECK_HOTEL_INFO_TABLE_SQL).next()) {
                log.debug("Creating hotel info table...");
                statement.executeUpdate(CREATE_HOTEL_REVIEW_TABLE_SQL);

            } else {
                log.debug("Hotel info table found.");
                statement.executeUpdate(DROP_HOTEL_INFO_TABLE_SQL);
                log.debug("Deleting existing hotel info table.");
                log.debug("Creating new hotel info table...");
                statement.executeUpdate(CREATE_HOTEL_REVIEW_TABLE_SQL);
            }
            // Check if create was successful
            if (!statement.executeQuery(CHECK_HOTEL_INFO_TABLE_SQL).next()) {
                log.debug("Hotel info table failed to be created");
                status = Status.CREATE_FAILED;
            }
            else {
                log.debug("Hotel info table created");
                status = Status.OK;
                for (String f: hotelFiles) {
                    Path curr = Paths.get("input/"+f);
                    loadHotelInfo(curr.toAbsolutePath().toString());
                }


            }
        }
        catch (Exception ex) {
            status = Status.CREATE_FAILED;
            log.debug(status, ex);
        }

        return status;
    }


    /**
     * Read the given json file with information about the hotels (check hotels.json to see the expected format)
     * and load it into the appropriate data structure(s).
     */
    public void loadHotelInfo(String jsonFilename) {
        Status status = Status.ERROR;
        JSONParser parser = new JSONParser();
        try {
            Connection connection = db.getConnection();
            JSONObject obj = (JSONObject) parser.parse(new FileReader(jsonFilename));
            log.debug("Started loading data from " + jsonFilename);
            JSONArray arr = (JSONArray) obj.get("sr");
            Iterator<JSONObject> iterator = arr.iterator();
            while (iterator.hasNext()) {
                JSONObject res = iterator.next();
                JSONObject coords = (JSONObject) res.get("ll");
                String hotelId= (String) res.get("id");
                String name = (String) res.get("f");
                String city = (String) res.get("ci");
                String state = (String) res.get("pr");
                String address= (String) res.get("id");
                float lat = Float.parseFloat((String) coords.get("lat"));
                float lng = Float.parseFloat((String) coords.get("lng"));
                //TODO Check if id is duplicate
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
                    statement.executeUpdate();
                    status = Status.OK;
                    log.debug("Hotel added");
                }
                catch (SQLException ex) {
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
    /**
     * Find all review files in the given path (including in subfolders and subsubfolders etc),
     * read them, parse them using JSONSimple library, and
     * load review info to the TreeMap that contains a TreeSet of Review-s for each hotel id (you should
     * have defined this instance variable above)
     * @param path
     */
    public void loadReviews(Path path){
        final List<Path> files = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    loadReviews(entry);
                }else {
                    files.add(entry);
                    if(entry.toString().endsWith(".json")){
                        readJson(entry.toString());
                    }
                }
            }
        }catch (ParseException p) {
            p.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean readJson(String jsonFilename) throws ParseException, IOException {
        JSONParser parser = new JSONParser();

        try {

            JSONObject obj = (JSONObject)parser.parse(new FileReader(jsonFilename));
            JSONObject rewD = (JSONObject)obj.get("reviewDetails");
            JSONObject rewC = (JSONObject)rewD.get("reviewCollection");
            JSONArray arr= (JSONArray)rewC.get("review");
            Iterator<JSONObject> iterator = arr.iterator();

            while (iterator.hasNext()) {
                JSONObject res = iterator.next();
                boolean isRecom;
                if (res.get("isRecommended") == ("YES")) {
                    isRecom = true;
                } else {
                    isRecom = false;

                }
                String hotelId = (String) res.get("hotelId");
                String reviewId = (String) res.get("reviewId");
                String rating = res.get("ratingOverall").toString();
                int intRating = Integer.parseInt(rating);
                String title = (String) res.get("title");
                String review = (String) res.get("reviewText");
                String data =(String) res.get("reviewSubmissionTime");
                String username = (String) res.get("userNickname");
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
                    statement.executeUpdate();
                    status = Status.OK;
                    log.debug("Hotel added");
                }
                catch (SQLException ex) {
                    status = Status.SQL_EXCEPTION;
                    log.debug(ex.getMessage(), ex);
                }
            }

        } catch  (FileNotFoundException ex) {
            System.out.println("Could not find file: " + jsonFilename );
        } catch (ParseException e) {
            System.out.println("Can not parse a given json file. " + jsonFilename);
        } catch (IOException e) {
            System.out.println("General IO Exception in readJSON");
        }
        return true;
    }

}

