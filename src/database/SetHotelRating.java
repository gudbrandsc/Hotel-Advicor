package database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.sql.*;


public class SetHotelRating {
    /**
     */
    private static Logger log = LogManager.getLogger();

    /**
     * Makes sure only one database handler is instantiated.
     */
    private static SetHotelRating singleton = new SetHotelRating();

    /**
     * Used to get all hotelIds in the database.
     */
    private static final String GET_ALL_HOTELIDS_SQL =
            "SELECT hotelId FROM hotel_info";



    /**
     * Used to get all ratings for a hotel
     */
    private static final String GET_ALL_RATINGS_HOTELID_SQL =
            "SELECT intRating FROM hotel_reviews WHERE hotelId=?";

    /**
     * Used to get all ratings for a hotel
     */
    private static final String SET_AVERAGE_RATING_FOR_HOTEL_SQL =
            "UPDATE hotel_info SET avgRating = ? WHERE hotelId=?;";



    /**
     * Used to configure connection to database.
     */
    private database.DatabaseConnector db;



    /**
     * Initializes a database handler for the Login example. Private constructor
     * forces all other classes to use singleton.
     */
    private SetHotelRating() {
        Status status = Status.OK;
        //List that contains all hotels files that u want in your db

        try {
            db = new database.DatabaseConnector("database.properties");
            status = db.testConnection() ? setAllRatings() : Status.CONNECTION_FAILED;
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
    public static SetHotelRating getInstance() {
        return singleton;
    }


    private Status setAllRatings() {
        Status status = Status.ERROR;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_ALL_HOTELIDS_SQL)
        ) {
            ResultSet set = statement.executeQuery();
            while(set.next()){
                log.debug("Setting rating for hotelid: "+ set.getString(1));
                calculateAvgRating(set.getString(1),connection);
            }
            status = Status.OK;
        }
        catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
            log.debug(e.getMessage(), e);
        }
        log.debug("Average rating was updated: " + status);
        return status;
    }


    private Status calculateAvgRating(String hotelId,Connection connection) {
        Status status = Status.ERROR;
        int count = 0;
        Double total = 0.0;
        try (
                PreparedStatement statement = connection.prepareStatement(GET_ALL_RATINGS_HOTELID_SQL)
        ) {
            statement.setString(1,hotelId);
            ResultSet set = statement.executeQuery();
            if(set.next()){
                while(set.next()){
                    log.debug("Adding values "+ set.getFloat(1));
                    count++;
                    total=total+set.getFloat(1);
                }
                Double avgRating = total/count;
                log.debug("Rating is now : "+avgRating);
                setAvgRatingForHotel(hotelId,avgRating,connection);
            }
            status = Status.OK;
        }
        catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
            log.debug(e.getMessage(), e);
        }
        log.debug("Average rating was updated: " + status);
        return status;
    }

    private Status setAvgRatingForHotel(String hotelId, Double avgRating,Connection connection) {
        Status status = Status.ERROR;
        try (
                PreparedStatement statement = connection.prepareStatement(SET_AVERAGE_RATING_FOR_HOTEL_SQL)
        ) {
            log.debug("Updating average rating..");
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
