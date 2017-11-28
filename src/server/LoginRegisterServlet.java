package server;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;


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


		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");
		boolean alert = false;

		if(error != null) {
			alert=true;
		}

		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/register.html");
		context.put("alert",alert);
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		out.println(writer.toString());
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
}