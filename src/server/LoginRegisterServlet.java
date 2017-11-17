package server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// Example of Prof. Engle

@SuppressWarnings("serial")
public class LoginRegisterServlet extends LoginBaseServlet {

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

	private void printForm(PrintWriter out) {
		assert out != null;
		//TODO add exstra input and check that the two input matches before post
		out.println("<h3>Register</h3>");
		out.println("<form action=\"/register\" method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Usename:</td>");
		out.println("\t\t<td><input placeholder=\"Enter username\" type=\"text\" name=\"user\" size=\"30\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Password:</td>");
		out.println("\t\t<td><input placeholder=\"Enter password\" type=\"password\" name=\"pass\" size=\"30\"");
		out.println("pattern=\"(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d])(?=.*[#?!@$%^&*-]).{8,}\"");
		out.println("autofocus required title=\"- At least 8 characters");
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