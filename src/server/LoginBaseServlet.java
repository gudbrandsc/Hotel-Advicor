package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides base functionality to all servlets.
 *
 */
@SuppressWarnings("serial")
public class LoginBaseServlet extends HttpServlet {

	protected static Logger log = LogManager.getLogger();
	//Creates a single instance of the database handler that is accessible by all child classes
	protected static final DatabaseHandler databaseHandler = DatabaseHandler.getInstance();



	/**
	 * Used to return a date.
	 * @return date
	 */
	protected String getDate() {

		String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
		DateFormat dateFormat = new SimpleDateFormat(format);
		System.out.println(dateFormat.format(Calendar.getInstance().getTime()));

		return dateFormat.format(Calendar.getInstance().getTime());
	}
	/**
	 * Used to return a date.
	 * @return date
	 */
	protected String getLoginDate() {

		String format = "yyyy-MM-dd HH:mm:ss";
		DateFormat dateFormat = new SimpleDateFormat(format);
		System.out.println(dateFormat.format(Calendar.getInstance().getTime()));

		return dateFormat.format(Calendar.getInstance().getTime());
	}

	protected Map<String, String> getCookieMap(HttpServletRequest request) {
		HashMap<String, String> map = new HashMap<String, String>();

		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				map.put(cookie.getName(), cookie.getValue());
			}
		}

		return map;
	}

	/**
	 * Used to clear all cookies.
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 */
	protected void clearCookies(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();

		if(cookies == null) {
			return;
		}

		for(Cookie cookie : cookies) {
			cookie.setValue("");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}


	/**
	 * Used to get a status message using a integer code
	 * @param code status code number
	 * @return with error message
	 */
	protected String getStatusMessage(int code) {
		Status status = null;

		try {
			status = Status.values()[code];
		}
		catch (Exception ex) {
			log.debug(ex.getMessage(), ex);
			status = Status.ERROR;
		}

		return status.toString();
	}

	/**
	 * Used to get the username of the person in the session, using cookies.
	 * @param request HttpServletRequest
	 * @return string containing username
	 */
	protected String getUsername(HttpServletRequest request) {
		Map<String, String> cookies = getCookieMap(request);

		String login = cookies.get("login");
		String user  = cookies.get("name");

		if ((login != null) && login.equals("true") && (user != null)) {
			// this is not safe!
			return user;
		}

		return null;
	}
	protected int integerParser(String value){
		int num=0;
		try {
			num = Integer.parseInt(value);
		} catch (Exception ex) {
			num = -1;
		}
		return num;
	}

}