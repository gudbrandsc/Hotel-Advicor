package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles all database-related actions. Uses singleton design pattern.
 * Example of Prof. Engle
 *
 *
 */
public class DatabaseHandler {

    /** A {@link org.apache.log4j.Logger log4j} logger for debugging. */
    private static Logger log = LogManager.getLogger();

    /** Makes sure only one database handler is instantiated. */
    private static DatabaseHandler singleton = new DatabaseHandler();

    /** Get a list of all hotel names in alphabetical order */
    private static final String GET_HOTEL_GENERAL_INFO_SQL =
            "SELECT DISTINCT hotelnames,address,intRating, hotel_info.hotelId " +
                    "FROM hotel_info " +
                    "LEFT JOIN hotel_reviews ON hotel_info.hotelId = hotel_reviews.hotelId " +
                    "ORDER BY hotelnames;";

    /** Used to insert a new user into the database. */
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

    /** Used to remove a user from the database. */
    private static final String DELETE_SQL =
            "DELETE FROM login_users WHERE username = ?";

    /** Used to get all reviews for a hotel by hotelId*/
    private static final String GET_HOTEL_REVIEWS_HOTELID_SQL =
            "SELECT * FROM hotel_reviews WHERE hotelId=?;";

    /** Used to get all reviews created by user*/
    private static final String GET_HOTEL_REVIEWS_USERNAME_SQL =
            "SELECT * FROM hotel_reviews WHERE username=?;";


    private static final String GET_HOTEL_NAME_SQL =
            "SELECT hotelnames FROM hotel_info WHERE hotelId=? ORDER BY hotelId DESC LIMIT 1";

    /**
     * Used to insert a new user into the database.
     */
    private static final String INSERT_REVIEW_SQL =
            "INSERT INTO hotel_reviews (hotelId, reviewId, intRating, title, review, submissiondate, username) " +
                    "VALUES (?,?,?,?,?,?,?);";
    /**
     * Used to insert a new user into the database.
     */
    private static final String REMOVE_HOTEL_REVIEW_SQL =
            "DELETE FROM hotel_reviews WHERE reviewId=?;";


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
        //Change so that u can use all SQL statments from databse dir

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
                PreparedStatement statement = connection.prepareStatement(USER_SQL);
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
     * @return {@link Status.OK} if registration successful
     */
    private Status registerUser(Connection connection, String newuser, String newpass) {

        Status status = Status.ERROR;

        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);

        String usersalt = encodeHex(saltBytes, 32);
        String passhash = getHash(newpass, usersalt);

        try (
                PreparedStatement statement = connection.prepareStatement(REGISTER_SQL);
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
     * @return {@link Status.OK} if registration successful
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
                Connection connection = db.getConnection();
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
                PreparedStatement statement = connection.prepareStatement(SALT_SQL);
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
     * @return {@link Status.OK} if authentication successful
     * @throws SQLException
     */
    private Status authenticateUser(Connection connection, String username,
                                    String password) throws SQLException {

        Status status = Status.ERROR;

        try (
                PreparedStatement statement = connection.prepareStatement(AUTH_SQL);
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
     * @return {@link Status.OK} if authentication successful
     */
    public Status authenticateUser(String username, String password) {
        Status status = Status.ERROR;

        log.debug("Authenticating user " + username + ".");

        try (
                Connection connection = db.getConnection();
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
     * Removes a user from the database if the username and password are
     * provided correctly.
     *
     * @param username - username to remove
     * @param password - password of user
     * @return {@link Status.OK} if removal successful
     */
    private Status removeUser(Connection connection, String username, String password) {
        Status status = Status.ERROR;

        try (
                PreparedStatement statement = connection.prepareStatement(DELETE_SQL);
        ) {
            statement.setString(1, username);

            int count = statement.executeUpdate();
            status = (count == 1) ? Status.OK : Status.INVALID_USER;
        }
        catch (SQLException ex) {
            status = Status.SQL_EXCEPTION;
            log.debug(status, ex);
        }

        return status;
    }

    /**
     * Removes a user from the database if the username and password are
     * provided correctly.
     *
     * @param username - username to remove
     * @param password - password of user
     * @return {@link Status.OK} if removal successful
     */
    public Status removeUser(String username, String password) {
        Status status = Status.ERROR;

        log.debug("Removing user " + username + ".");

        try (
                Connection connection = db.getConnection();
        ) {
            status = authenticateUser(connection, username, password);

            if(status == Status.OK) {
                status = removeUser(connection, username, password);
            }
        }
        catch (Exception ex) {
            status = Status.CONNECTION_FAILED;
            log.debug(status, ex);
        }

        return status;
    }
    /**
     * Removes a user from the database if the username and password are
     * provided correctly.
     *
     * @param username - username to remove
     * @param hotelId - password of user
     * @return {@link Status.OK} if removal successful
     */
    public Status removeReview(String username, String hotelId) {
        Status status = Status.ERROR;

        String primkey=username+hotelId;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(REMOVE_HOTEL_REVIEW_SQL);
        ) {
            statement.setString(1,primkey);
            log.debug("Removing review " +primkey+".");
            statement.executeUpdate();
            status=status.OK;
        }
        catch (Exception ex) {
            status = Status.CONNECTION_FAILED;
            log.debug(status, ex);
        }
        return status;
    }
    /**
     * Method that returns info about hotel names
     * @return ResultSet*/
    public String hotelInfoDisplayer(){
        Status status = Status.ERROR;

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_GENERAL_INFO_SQL)

        ) {
            StringBuilder sb = new StringBuilder();
            ResultSet hotelNames =statement.executeQuery();
            sb.append("<table>" +
                    "<tr>" +
                    "<th><b>Hotel name</b></th>" +
                    "<th><b>Address</b></th>" +
                    "<th><b>Rating</b></th>" +
                    "</tr>");
            while (hotelNames.next()){
                sb.append("<tr>");
                sb.append("<td><a href=\"/allreviews?hotelid="+hotelNames.getString(4)+"\">");
                sb.append(hotelNames.getString(1)+"</a></td>");
                sb.append("<td>"+hotelNames.getString(2)+"</td>");
                sb.append("<td>"+hotelNames.getInt(3)+"</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            return sb.toString();
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
            status = Status.SQL_EXCEPTION;
            return null;
        }

    }
    /**
     * Tests if a hotel has any reviews
     *
     * @return true if set is not empty
     * @throws SQLException
     */
    public boolean checkHotelIdReviewSet(String hotelId) {
        Boolean exist=null;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_REVIEWS_HOTELID_SQL);
        ) {
            statement.setString(1,hotelId);
             exist= statement.executeQuery().next();
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
        }
        log.debug(exist);
        return exist;
    }
    /**
     * Tests if a user have any reviews
     *
     * @return true if set is not empty
     * @throws SQLException
     */
    public boolean checkUsernameReviewSet(String username) {
        Boolean exist=null;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_HOTEL_REVIEWS_USERNAME_SQL);
        ) {
            statement.setString(1,username);
            exist= statement.executeQuery().next();
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
        }
        return exist;
    }
    /**
     * Tests if a hotel has any reviews
     *
     * @return true if set is not empty
     * @throws SQLException
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
     * Method that returns hotel reviews using hotelid
     * @return ResultSet*/
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
     * Method that returns hotel reviews using hotelid
     * @return ResultSet*/
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
        return sb.toString();
    }
    /**
     * Builds a html string of a review
     * @return html string*/
    public String reviewBuilder(String title, String username, String date, int rating, String review,boolean userReview,String hotelId) {
        StringBuilder sb = new StringBuilder();
        log.debug("this is your hotelID: "+ hotelId+ " val:" + userReview);
        sb.append("<div style=\"background-color: #f1f1f1; padding: 0.01em 16px; margin: 20px 0; box-shadow: 0 2px 4px 0 rgba(0,0,0,0.16),0 2px 10px 0 rgba(0,0,0,0.12)\">");
        sb.append("<h4 style=\"margin-bottom: 0px;\">" + title + "</h4>");
        sb.append("<p style=\"margin-top: 0px;\"><small>Submited by " + username);
        sb.append(", on " + date + "</p></small>");
        sb.append("<p> Rating: " + rating);
        sb.append("<div style=\"background-color: #fff;\">");
        sb.append("<p>" + review + "</p>");
        sb.append("</div>");
        if(userReview){
            log.debug(title+ ": " +hotelId);
            //TODO make inline
            sb.append(" <form action = \"/myreviews?username="+username+"\" method = \"post\">");
            sb.append("<input type=\"hidden\" value=\""+hotelId+"\" name=\"hotelid\" />\n");
            sb.append("<input type=\"submit\" name=\"edit\" value=\"Edit\" />");
            sb.append("<input type=\"submit\" name=\"delete\" value=\"Delete\" />");
            sb.append("</form>");
        }
        sb.append("</div>");
        return sb.toString();
    }


    public Status addReview(String hotelId,int rating, String title, String review, String date, String username){
        //TODO check if user all ready have review for that hotel
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
            status = Status.OK;
        }
        catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
            log.debug(e.getMessage(), e);
        }
        return status;
    }

}

