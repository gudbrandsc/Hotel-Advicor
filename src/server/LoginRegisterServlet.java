package server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Servlet that handles all user registration requests
 */
@SuppressWarnings("serial")
public class LoginRegisterServlet extends LoginBaseServlet {

	/**
	 * A method that gets executed when a get request is sent to the LoginRegisterServlet
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws IOException IOException
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Register New User", response);

		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");

		if(error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		printForm(out);
		finishResponse(response);
	}

	/** The method that will process the form once it's submitted
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws IOException IOException
	 * */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Register New User", response);

		String newuser = request.getParameter("user");
		String newpass = request.getParameter("pass");
		Status status = databaseHandler.registerUser(newuser, newpass);

		if(status == Status.OK) {
			response.sendRedirect(response.encodeRedirectURL("/login?newuser=true"));
		}
		else {
			String url = "/register?error=" + status.name();
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url);
		}
	}
	/**
	 * A method that is used to print the registration form.
	 * @param out printwriter from HttpServletResponse
	 */
	private void printForm(PrintWriter out) {
		assert out != null;
		out.println("<h3>Register</h3>");
		out.println("<form action=\"/register\" method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("<tr>");
		out.println("<td>Usename:</td>");
		out.println("<td><input autofocus required placeholder=\"Enter username\" type=\"text\" name=\"user\"");
		out.println("pattern=\"[a-zA-Z0-9_-]{5,20}\" title=\"- Length 5-20 characters ");
		out.println("- Upper and lowercase letters");
		out.println("- numbers and special character: _- \"></td></tr>");
		out.println("<tr>");
		out.println("<td>Password:</td>");
		out.println("<td><input placeholder=\"Enter password\" type=\"password\" name=\"pass\" size=\"30\"");
		out.println("pattern=\"(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d])(?=.*[#?!@$%^&*-]).{8,}\"");
		out.println(" required title=\"- At least 8 characters");
		out.println("- One upper- and one lowercase letter");
		out.println("- One number from 0-9");
		out.println("- One special character  #?!@$%^&*-\"");
		out.println("></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Register user\"></p>");
		out.println("</form>");
	}
}