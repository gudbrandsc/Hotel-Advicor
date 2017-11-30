package server;


/**
 * Creates a Status enum type for tracking errors. Each Status enum type
 * will use the ordinal as the error code, and store a message describing
 * the error.
 *
 */
public enum Status {

	/*
	 * Creates several Status enum types. The Status name and message is
	 * given in the NAME(message) format below. The Status ordinal is
	 * determined by its position in the list. (For example, OK is the
	 * first element, and will have ordinal 0.)
	 */

	OK("No errors occured."),
	ERROR("Unknown error occurred."),
	MISSING_CONFIG("Unable to find configuration file."),
	MISSING_VALUES("Missing values in configuration file."),
	CONNECTION_FAILED("Failed to establish a database connection."),
	CREATE_FAILED("Failed to create necessary tables."),
	INVALID_LOGIN("Invalid username and/or password."),
	INVALID_USER("User does not exist."),
	DUPLICATE_USER("User with that username already exists."),
	SQL_EXCEPTION("Unable to execute SQL statement."),
	INVALID_REVIEWID("There is no review with that reviewId."),
	NO_USERREVIEWS("The user does not have any reviews yet."),
	NO_HOTELREVIEWS("Hotel does not have any reviews yet."),
	MISSING_PARAM("One of the required parameters for your request was missing."),
	INVALID_HOTELID("No hotel related to hotelid."),
	REMOVE_REVIEW_ERROR("Not able to remove your review, please try again."),
	REMOVE_REVIEW_SUCCESS("Your review was removed."),
	SUBMIT_REVIEW_SUCCESS("Your review was submitted."),
	UPDATE_REVIEW_SUCCESS("Your review was updated."),
	SUBMIT_REVIEW_ERROR("Failed to submit your review, please try again."),
	UPDATE_REVIEW_ERROR("Failed to update your review, please try again."),
	FAILED_TO_POST("Failed to submit review, please try again."),
	MISSING_HOTELID("There is no hotelid in your request.");


	private final String message;

	Status(String message) {
		this.message = message;
	}

	public String message() {
		return message;
	}

	@Override
	public String toString() {
		return this.message;
	}
}