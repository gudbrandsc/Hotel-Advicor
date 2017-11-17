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
            "SHOW TABLES LIKE 'hotel_reviews';";

    /** Used to drop hotel_review table */
    private static final String DROP_HOTEL_REVIEW_TABLE_SQL =
            "DROP TABLE hotel_reviews;";

    /**
     * Create table for hotel reviews
     */
    private static final String CREATE_HOTEL_REVIEW_TABLE_SQL =
            "CREATE TABLE hotel_reviews (hotelId VARCHAR(50) NOT NULL," +
                    "                    reviewId VARCHAR(100) PRIMARY KEY," +
                    "                    intRating INTEGER (50) NOT NULL, " +
                    "                    title VARCHAR(50) NOT NULL," +
                    "                    review VARCHAR(3000) NOT NULL," +
                    "                    submissiondate VARCHAR (255) NOT NULL," +
                    "                    username VARCHAR(50) NOT NULL);";

    /**
     * Used to insert a new user into the database.
     */
    private static final String INSERT_HOTEL_SQL =
            "INSERT INTO hotel_reviews (hotelId, reviewId, intRating, title, review, submissiondate, username) " +
                    "VALUES (?,?,?,?,?,?,?);";


    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;



    /**
     * Initializes a database handler for the Login example. Private constructor
     * forces all other classes to use singleton.
     */
    private HotelReviewBuilder() {
        Status status = Status.OK;
        //List that contains all hotels files that u want in your db
        ArrayList<Path> list = new ArrayList<Path>();
        list.add(Paths.get("input/reviews"));

        try {
            db = new DatabaseConnector("database.properties");
            status = db.testConnection() ? setupTable(list) : Status.CONNECTION_FAILED;
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
    public static HotelReviewBuilder getInstance() {
        return singleton;
    }

    /**
     * Checks if necessary table exists in database, and if it does deletes it and creates a new.
     * If it does not exist just create new table
     *
     * @return {@link Status.OK} if table exists or create is successful
     */
    private Status setupTable(ArrayList<Path> hotelFiles) {
        Status status = Status.ERROR;

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement();
        ) {
            if (!statement.executeQuery(CHECK_HOTEL_REVIEW_TABLE_SQL).next()) {
                log.debug("Creating hotel review table...");
                statement.executeUpdate(CREATE_HOTEL_REVIEW_TABLE_SQL);
            } else {
                log.debug("Hotel review table found.");
                statement.executeUpdate(DROP_HOTEL_REVIEW_TABLE_SQL);
                log.debug("Deleting existing hotel review table.");
                log.debug("Creating new hotel review table...");
                statement.executeUpdate(CREATE_HOTEL_REVIEW_TABLE_SQL);
            }
            // Check if create was successful
            if (!statement.executeQuery(CHECK_HOTEL_REVIEW_TABLE_SQL).next()) {
                log.debug("Hotel review table failed to be created");
                status = Status.CREATE_FAILED;
            }
            else {
                log.debug("Hotel review table created");
                status = Status.OK;
                for (Path p: hotelFiles) {
                    loadReviews(p);
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
        Status status = Status.ERROR;

        try {
            Connection connection = db.getConnection();
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
                String date =(String) res.get("reviewSubmissionTime");
                String username = (String) res.get("userNickname");
                try (
                        PreparedStatement statement = connection.prepareStatement(INSERT_HOTEL_SQL);
                ) {
                    statement.setString(1, hotelId);
                    statement.setString(2, reviewId);
                    statement.setInt(3, intRating);
                    statement.setString(4, title);
                    statement.setString(5, review);
                    statement.setString(6, date);
                    statement.setString(7, username);
                    statement.executeUpdate();
                    status = Status.OK;
                    log.debug("Review added");
                }
                catch (SQLException ex) {
                    status = Status.SQL_EXCEPTION;
                    log.debug(ex.getMessage(), ex);
                }
            }

        } catch  (FileNotFoundException ex) {
            log.debug("Could not find file: " + jsonFilename, ex.getMessage());
        } catch (ParseException e) {
            log.debug("Can not parse a given json file. " + jsonFilename, e.getMessage());
        } catch (IOException e) {
            log.debug("General IO Exception in readJSON ", e.getMessage());
        } catch (SQLException e) {
            log.debug(e.getMessage());
        }
        return true;
    }

}

