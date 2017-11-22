package server;
import database.DatabaseConnector;
import databaseObjects.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles all database-related actions. Uses singleton design pattern.
 *
 */
public class DatabaseHandler {

    /** A  logger for debugging. */
    private static Logger log = LogManager.getLogger();

    /** Makes sure only one database handler is instantiated. */
    private static DatabaseHandler singleton = new DatabaseHandler();

    /** Get a list of all hotel names in alphabetical order */
    private static final String GET_HOTEL_GENERAL_INFO_SQL =
            "SELECT hotelnames,address,avgRating,hotelId FROM hotel_info order by hotelnames";

    /** Used to register a new user in the database. */
    private static final String REGISTER_SQL =
            "INSERT INTO login_users (username, password, usersalt) " +
                    "VALUES (?, ?, ?);";

    /** Used to determine if a username already exists. */
    private static final String USER_SQL =
            "SELECT username FROM login_users WHERE username = ?";

    /** Used to retrieve the salt associated with a specific user. */
    private static final String SALT_SQL =
            "SELECT usersalt FROM login_users WHERE username = ?";

    /** Used to authenticate a user. */
    private static final String AUTH_SQL =
            "SELECT username FROM login_users " +
                    "WHERE username = ? AND password = ?";

    /** Used to get all reviews for a hotel by hotelId*/
    private static final String GET_HOTEL_REVIEWS_HOTELID_SQL =
            "SELECT * FROM hotel_reviews WHERE hotelId=?;";

    /** Used to get all reviews created by a specific user*/
    private static final String GET_HOTEL_REVIEWS_USERNAME_SQL =
            "SELECT * FROM hotel_reviews WHERE username=?;";

    /*** Used to get a hotel name using hotelid.*/
    private static final String GET_HOTEL_NAME_SQL =
            "SELECT hotelnames FROM hotel_info WHERE hotelId=? ORDER BY hotelId DESC LIMIT 1";

    /*** Used to insert a new review for a hotel.*/
    private static final String INSERT_REVIEW_SQL =
            "INSERT INTO hotel_reviews (hotelId, reviewId, intRating, title, review, submissiondate, username) " +
                    "VALUES (?,?,?,?,?,?,?);";

    /** Used to delete a hotel review.*/
    private static final String REMOVE_HOTEL_REVIEW_SQL =
            "DELETE FROM hotel_reviews WHERE reviewId=?;";

    /** Used to update a review .*/
    private static final String UPDATE_HOTEL_REVIEW_SQL =
            "UPDATE hotel_reviews SET title = ?, review = ?, intRating =?, submissiondate=? WHERE reviewId=?;";

    /** Used to get all review info by primary key*/
    private static final String GET_HOTEL_REVIEW_INFO_BY_KEY_SQL =
            "SELECT * FROM hotel_reviews where reviewID=?;";


    /** Used to get latitude for a hotel.*/
    private static final String GET_HOTEL_LATITUDE_SQL =
            "SELECT latitude FROM hotel_info where hotelId=?;";

    /** Used to get longitude for a hotel.*/
    private static final String GET_HOTEL_LONGITUDE_SQL =
            "SELECT longitude FROM hotel_info where hotelId=?;";

    /** Used to get city for a hotelid*/
    private static final String GET_HOTEL_CITY_SQL =
            "SELECT city FROM hotel_info where hotelId=?;";

    /** Used to check if hotel with id exist*/
    private static final String CHECK_HOTEL_EXIST_SQL =
            "SELECT * FROM hotel_info where hotelId=?;";
    /**
     * Used to get all ratings for a hotel
     */
    private static final String GET_ALL_RATINGS_HOTELID_SQL =
            "SELECT intRating FROM hotel_reviews WHERE hotelId=?";

    /**
     * Used to update average rating for a hotel
     */
    private static final String SET_AVERAGE_RATING_FOR_HOTEL_SQL =
            "UPDATE hotel_info SET avgRating = ? WHERE hotelId=?;";


    /** Used to configure connection to database. */

    private DatabaseConnector db;

    /** Used to generate password hash salt for user. */
    private Random random;

    /**
     * Initializes a database handler for the Login example. Private constructor
     * forces all other classes to use singleton.
     */
    private DatabaseHandler() {
        Status status = Status.OK;
        random = new Random(System.currentTimeMillis());

        try {
            db = new DatabaseConnector("database.properties");
            status = db.testConnection() ? Status.OK : Status.CONNECTION_FAILED;
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
    public static DatabaseHandler getInstance() {
        return singleton;
    }

    /**
     * Checks to see if a String is null or empty.
     * @param text - String to check
     * @return true if non-null and non-empty
     */
    public static boolean isBlank(String text) {
        return (text == null) || text.trim().isEmpty();
    }



    /**
     * Tests if a user already exists in the database. Requires an active
     * database connection.
     *
     * @param connection - active database connection
     * @param user - username to check
     * @return Status.OK if user does not exist in database
     * @throws SQLException
     */
    private Status duplicateUser(Connection connection, String user) {

        assert connection != null;
        assert user != null;

        Status status = Status.ERROR;

        try (
                PreparedStatement statement = connection.prepareStatement(USER_SQL)
        ) {
            statement.setString(1, user);

            ResultSet results = statement.executeQuery();
            status = results.next() ? Status.DUPLICATE_USER : Status.OK;
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
            status = Status.SQL_EXCEPTION;
        }

        return status;
    }


    /**
     * Returns the hex encoding of a byte array.
     *
     * @param bytes - byte array to encode
     * @param length - desired length of encoding
     * @return hex encoded byte array
     */
    public static String encodeHex(byte[] bytes, int length) {
        BigInteger bigint = new BigInteger(1, bytes);
        String hex = String.format("%0" + length + "X", bigint);

        assert hex.length() == length;
        return hex;
    }

    /**
     * Calculates the hash of a password and salt using SHA-256.
     *
     * @param password - password to hash
     * @param salt - salt associated with user
     * @return hashed password
     */
    public static String getHash(String password, String salt) {
        String salted = salt + password;
        String hashed = salted;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salted.getBytes());
            hashed = encodeHex(md.digest(), 64);
        }
        catch (Exception ex) {
            log.debug("Unable to properly hash password.", ex);
        }

        return hashed;
    }

    /**
     * Registers a new user, placing the username, password hash, and
     * salt into the database if the username does not already exist.
     *
     * @param newuser - username of new user
     * @param newpass - password of new user
     * @return Status.OK if registration successful
     */
    private Status registerUser(Connection connection, String newuser, String newpass) {

        Status status = Status.ERROR;

        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);

        String usersalt = encodeHex(saltBytes, 32);
        String passhash = getHash(newpass, usersalt);

        try (
                PreparedStatement statement = connection.prepareStatement(REGISTER_SQL)
        ) {
            statement.setString(1, newuser);
            statement.setString(2, passhash);
            statement.setString(3, usersalt);
            statement.executeUpdate();

            status = Status.OK;
        }
        catch (SQLException ex) {
            status = Status.SQL_EXCEPTION;
            log.debug(ex.getMessage(), ex);
        }

        return status;
    }

    /**
     * Registers a new user, placing the username, password hash, and
     * salt into the database if the username does not already exist.
     *
     * @param newuser - username of new user
     * @param newpass - password of new user
     * @return Status.OK if registration successful
     */
    public Status registerUser(String newuser, String newpass) {
        Status status = Status.ERROR;
        log.debug("Registering " + newuser + ".");

        // make sure we have non-null and non-emtpy values for login
        if (isBlank(newuser) || isBlank(newpass)) {
            status = Status.INVALID_LOGIN;
            log.debug(status);
            return status;
        }

        // try to connect to database and test for duplicate user
        System.out.println(db);

        try (
                Connection connection = db.getConnection()
        ) {
            status = duplicateUser(connection, newuser);

            // if okay so far, try to insert new user
            if (status == Status.OK) {
                status = registerUser(connection, newuser, newpass);
            }
        }
        catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            log.debug(status, ex);
        }

        return status;
    }

    /**
     * Gets the salt for a specific user.
     *
     * @param connection - active database connection
     * @param user - which user to retrieve salt for
     * @return salt for the specified user or null if user does not exist
     * @throws SQLException if any issues with database connection
     */
    private String getSalt(Connection connection, String user) throws SQLException {
        assert connection != null;
        assert user != null;

        String salt = null;

        try (
                PreparedStatement statement = connection.prepareStatement(SALT_SQL)
        ) {
            statement.setString(1, user);

            ResultSet results = statement.executeQuery();

            if (results.next()) {
                salt = results.getString("usersalt");
            }
        }

        return salt;
    }

    /**
     * Checks if the provided username and password match what is stored
     * in the database. Requires an active database connection.
     *
     * @param username - username to authenticate
     * @param password - password to authenticate
     * @return Status.OK if authentication successful
     * @throws SQLException
     */
    private Status authenticateUser(Connection connection, String username,
                                    String password) throws SQLException {

        Status status = Status.ERROR;

        try (
                PreparedStatement statement = connection.prepareStatement(AUTH_SQL)
        ) {
            String usersalt = getSalt(connection, username);
            String passhash = getHash(password, usersalt);

            statement.setString(1, username);
            statement.setString(2, passhash);

            ResultSet results = statement.executeQuery();
            status = results.next() ? status = Status.OK : Status.INVALID_LOGIN;
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
            status = Status.SQL_EXCEPTION;
        }

        return status;
    }

    /**
     * Checks if the provided username and password match what is stored
     * in the database. Must retrieve the salt and hash the password to
     * do the comparison.
     *
     * @param username - username to authenticate
     * @param password - password to authenticate
     * @return Status.OK if authentication successful
     */
    public Status authenticateUser(String username, String password) {
        Status status = Status.ERROR;

        log.debug("Authenticating user " + username + ".");

        try (
                Connection connection = db.getConnection()
        ) {
            status = authenticateUser(connection, username, password);
        }
        catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            log.debug(status, ex);
        }

        return status;
    }

    /**
     * Removes a hotel review for a user.
     *
     * @param username - username connected to review
     * @param hotelId - hotel id that is  connected to review
     * @return Status.OK if removal successful
     */
    public Status removeReview(String username, String hotelId) {
        Status status = Status.ERROR;

        String primkey=username+hotelId;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(REMOVE_HOTEL_REVIEW_SQL)
        ) {
            statement.setString(1,primkey);
            log.debug("Removing review " +primkey+".");
            statement.executeUpdate();
            status=updateAvgRating(hotelId,connection);
        }
        catch (Exception ex) {
            status = Status.CONNECTION_FAILED;
            log.debug(status, ex);
        }
        return status;
    }
    /**
     * Used to fetch information about a users old review for a hotel.
     * @param username - username for the that submitted review
     * @param hotelId - Hotel id connected to the hotel review
     * @return Status.OK if removal successful
     */
    public Map<String, String> getUserReviewForHotel(String username, String hotelId) {
        Status status = Status.ERROR;
        Map<String,String> reviewsmap = new HashMap<String, String>();
        String primkey=username+hotelId;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_REVIEW_INFO_BY_KEY_SQL)
        ) {
            statement.setString(1,primkey);
            log.debug("Editing review " +primkey+".");
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                reviewsmap.put("hotelId",rs.getString(1));
                reviewsmap.put("reviewId",rs.getString(2));
                reviewsmap.put("rating",rs.getString(3));
                reviewsmap.put("title",rs.getString(4));
                reviewsmap.put("review",rs.getString(5));
                reviewsmap.put("subDate",rs.getString(6));
                reviewsmap.put("username",rs.getString(7));
            }
        }
        catch (Exception ex) {
            log.debug(status, ex);
        }
        return reviewsmap;
    }
    /**
     * Method used to display general information about all hotels
     * @return A html table string with hotel name, address and average rating
     * */
    public ArrayList<BasicHotelInfo> hotelInfoDisplayer(){
        ArrayList<BasicHotelInfo> hotelInfoArrayList = new ArrayList<>();
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_GENERAL_INFO_SQL)

        ) {
            ResultSet hotelNames =statement.executeQuery();
            while (hotelNames.next()){
                BasicHotelInfo hInfo = new BasicHotelInfo(hotelNames.getString(1),
                        hotelNames.getString(2),
                        hotelNames.getDouble(3),
                        hotelNames.getString(4));
                hotelInfoArrayList.add(hInfo);

            }
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
        }
        return hotelInfoArrayList;
    }
    /**
     * Tests if a hotel has any reviews
     * @param hotelId id for the hotel
     * @return true if set is not empty
     */
    public Status checkHotelIdReviewSet(String hotelId) {
        Status status = Status.ERROR;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_REVIEWS_HOTELID_SQL)
        ) {
            statement.setString(1,hotelId);
            ResultSet results = statement.executeQuery();
            status = results.next() ? status = Status.OK : Status.NO_HOTELREVIEWS;
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
        }
        return status;
    }
    /**
     * Tests if a user have any reviews
     * @param username username
     * @return true if set is not empty
     */
    public Status checkUsernameReviewSet(String username) {
        Status status = Status.ERROR;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_REVIEWS_USERNAME_SQL)
        ) {
            statement.setString(1,username);
            ResultSet results = statement.executeQuery();
            status = results.next() ? status = Status.OK : Status.NO_USERREVIEWS;
        }
        catch (SQLException e) {
            status=Status.SQL_EXCEPTION;
            log.debug(e.getMessage(), e);
        }
        return status;
    }
    /**
     * Used to get the name of a hotel connected to hotel id
     * @param hotelId id for hotel
     * @return hotel name
     */
    public String getHotelIdName(String hotelId) {
        String hotelName="";
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_NAME_SQL)
        ) {
            statement.setString(1,hotelId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                hotelName = rs.getString("hotelnames");
            }
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
        }
        log.debug(hotelName);
        return hotelName;
    }
    /**
     * Method used to build a html string containing all reviews for a hotel
     * using a hotel id
     * @param hotelId hotel id
     * @return A html table string with hotel name, address and rating
     *
     * */
    public String hotelIdReviewDisplayer(String hotelId) {
        StringBuilder sb = new StringBuilder();
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_REVIEWS_HOTELID_SQL)

        ) {
            statement.setString(1,hotelId);
            ResultSet hotelReviews = statement.executeQuery();
            while (hotelReviews.next()) {
                sb.append(reviewBuilder(
                        hotelReviews.getString(4),
                        hotelReviews.getString(7),
                        hotelReviews.getString(6),
                        hotelReviews.getInt(3),
                        hotelReviews.getString(5),
                        false,
                        hotelReviews.getString(1)
                        ));
            }
        } catch (SQLException e) {
            log.debug(e.getMessage(), e);
        }
        return sb.toString();
    }

    /**
     * Method used to get all reviews for a user, and sends each review to review builder.
     * @param username for currant user
     * @return string of reviews in a html format
     * */
    public String usernameReviewDisplayer(String username) {
        StringBuilder sb = new StringBuilder();

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_REVIEWS_USERNAME_SQL)

            ) {
            statement.setString(1,username);
            ResultSet hotelReviews = statement.executeQuery();
                while (hotelReviews.next()) {
                    sb.append(reviewBuilder(
                            hotelReviews.getString(4),
                            hotelReviews.getString(7),
                            hotelReviews.getString(6),
                            hotelReviews.getInt(3),
                            hotelReviews.getString(5),
                            true,
                    hotelReviews.getString(1)
                    ));
                }
            } catch (SQLException e) {
                log.debug(e.getMessage(), e);
            }
            log.debug("Building string with all reviews");
        return sb.toString();
    }

    /**
     * Builds a html string with all reviews for a hotel or user. If user-> add functionality to remove and edit review
     * @param title review title
     * @param username username for person posting the review
     * @param date date of post
     * @param rating rating for hotel
     * @param review review text
     * @param userReview value to print delete and edit button
     * @param hotelId id for hotel
     * @return  string with all reviews
     */
    private String reviewBuilder(String title, String username, String date, int rating, String review,boolean userReview,String hotelId) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"background-color: #f1f1f1; padding: 0.01em 16px; margin: 20px 0; box-shadow: 0 2px 4px 0 rgba(0,0,0,0.16),0 2px 10px 0 rgba(0,0,0,0.12)\">");
        sb.append("<h4 style=\"margin-bottom: 0px;\">" + title + "</h4>");
        sb.append("<p style=\"margin-top: 0px;\"><small>Submited by " + username);
        sb.append(", on " + date + "</p></small>");
        sb.append("<p> Rating: " + rating);
        sb.append("<div style=\"background-color: #fff;\">");
        sb.append("<p>" + review + "</p>");
        sb.append("</div>");
        if(userReview){
            sb.append("<p style=\"margin-top: 0px;\"><small>Review for hotel: "+getHotelIdName(hotelId)+" </small></p>");
            sb.append(" <form action = \"/myreviews?username="+username+"\" method = \"post\">");
            sb.append("<input type=\"hidden\" value=\""+hotelId+"\" name=\"hotelid\" />\n");
            sb.append("<input type=\"submit\" name=\"edit\" value=\"Edit\" />");
            sb.append("<input type=\"submit\" name=\"delete\" value=\"Delete\" />");
            sb.append("</form>");
        }
        sb.append("</div>");
        return sb.toString();
    }
    // TODO make most methods return status
    /**
     * Used to add a new hotel review
     * @param hotelId hotel id
     * @param rating rating for hotel
     * @param title title for hotel
     * @param review review text
     * @param date date review was posted
     * @param username username
     * @return status.OK if review was added
     */

    public Status addReview(String hotelId,int rating, String title, String review, String date, String username){
        Status status = Status.ERROR;

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_REVIEW_SQL)
            ) {
            statement.setString(1,hotelId);
            statement.setString(2,username+hotelId);
            statement.setInt(3,rating);
            statement.setString(4,title);
            statement.setString(5,review);
            statement.setString(6,date);
            statement.setString(7,username);
            statement.executeUpdate();
            status = updateAvgRating(hotelId,connection);
        }
        catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
            log.debug(e.getMessage(), e);
        }
        log.debug("Review was added status: " + status);
        return status;
    }
    /**
     * Used to update a users hotel review
     * @param hotelId hotel id
     * @param rating new rating
     * @param title new title
     * @param review new review text
     * @param date new post date
     * @param username username
     * @return return Status.OK if edit was success
     */

    public Status editReview(String hotelId, int rating, String title, String review, String date, String username){
        Status status = Status.ERROR;

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_HOTEL_REVIEW_SQL)
        ) {
            statement.setString(1,title);
            statement.setString(2,review);
            statement.setInt(3,rating);
            statement.setString(4,date);
            statement.setString(5,username+hotelId);
            statement.executeUpdate();
            status = updateAvgRating(hotelId,connection);
        }
        catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
            log.debug(e.getMessage(), e);
        }
        log.debug("Review was updated status: " + status);
        return status;
    }

    /**
     *Check if user has a existing review for hotel
     * @param hotelId id for hotel
     * @param username username
     * @return status.ok if has any reviews
     */

    public Status checkForExistingUserReview(String hotelId, String username){
        Status status = Status.ERROR;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_REVIEW_INFO_BY_KEY_SQL)
        ) {
            statement.setString(1,username+hotelId);
            ResultSet results= statement.executeQuery();
            status = results.next() ? status = Status.OK : Status.INVALID_REVIEWID;
        }
        catch (SQLException e) {
            status=Status.SQL_EXCEPTION;
            log.debug(e.getMessage(), e);
        }
        return status;
    }


    /**
     * Get latitude for a hotel using hotel id
     * @param hotelId id for hotel
     * @return latitude
     * */
    public String getHotelLat(String hotelId){
        String latitude="";
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_LATITUDE_SQL)
        ) {
            statement.setString(1,hotelId);
           ResultSet set = statement.executeQuery();
            while(set.next()){
                latitude=set.getString(1);
            }
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
        }
        return latitude;
    }

    /**
     * Get longitude for a hotel using hotel id
     * @param hotelId hotel id
     * @return longitude
     * */
    public String getHotelLon(String hotelId){
        String longitude="";
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_LONGITUDE_SQL)
        ) {
            statement.setString(1,hotelId);
            ResultSet set = statement.executeQuery();
            while(set.next()){
                longitude=set.getString(1);
            }
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
        }
        return longitude;
    }

    /**
     * Get city for a hotel
     * @param hotelId id for hotel
     * @return city
     * */
    public String getHotelCity(String hotelId){
        String city="";
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_CITY_SQL)
        ) {
            statement.setString(1,hotelId);
            ResultSet set = statement.executeQuery();
            while(set.next()){
                city=set.getString(1);
            }
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
        }
        return city;
    }


    /**
     *Check if hotel with hotel id exist
     * @param hotelId id for hotel
     * @return status.OK if hotel exist
     *
     */

    public Status checkIfHotelExist(String hotelId){
        Status status = Status.ERROR;
        Boolean exist=null;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(CHECK_HOTEL_EXIST_SQL)
        ) {
            statement.setString(1,hotelId);
            ResultSet results= statement.executeQuery();
            status = results.next() ? status = Status.OK : Status.INVALID_HOTELID;
            log.debug(status);

        }
        catch (SQLException e) {
            status=Status.SQL_EXCEPTION;
            log.debug(e.getMessage(), e);
        }
        return status;
    }

    /**
     * Gets all review ratings and calculate the new average
     * @param hotelId id for hotel
     * @param connection connection to database
     * @return status.OK if update was success
     */
    private Status updateAvgRating(String hotelId,Connection connection) {
        Status status = Status.ERROR;
        int count = 0;
        Double total = 0.0;
        try (
                PreparedStatement statement = connection.prepareStatement(GET_ALL_RATINGS_HOTELID_SQL)
        ) {
            statement.setString(1,hotelId);
            ResultSet set = statement.executeQuery();

            if (set.next()) {
                do {
                    count++;
                    total=total+set.getDouble(1);
                } while (set.next());
            }
            Double avgRating = total/count;
            if(avgRating.isNaN()){
                setAvgRatingForHotel(hotelId,0.0,connection);
            }else{
                setAvgRatingForHotel(hotelId,avgRating,connection);
            }
            status = Status.OK;
        }
        catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
            log.debug(e.getMessage(), e);
        }
        return status;
    }

    /**
     * Updates the average rating for a hotel
     * @param hotelId id for hotel
     * @param avgRating calculated average rating
     * @param connection database connection
     * @return status.OK if rating was updated for the hotel
     */
    private Status setAvgRatingForHotel(String hotelId, Double avgRating,Connection connection) {
        Status status = Status.ERROR;
        try (
                PreparedStatement statement = connection.prepareStatement(SET_AVERAGE_RATING_FOR_HOTEL_SQL)
        ) {
            log.debug("Updating average rating..");
            log.debug(avgRating);
            statement.setDouble(1,avgRating);
            statement.setString(2,hotelId);
            statement.executeUpdate();
            status = Status.OK;
        }
        catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
            log.debug(e.getMessage(), e);
        }
        log.debug("AvgRating for " +hotelId+" was changed " + status);
        return status;
    }
}
