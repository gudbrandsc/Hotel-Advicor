package server;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class that handles login requests.
 *
 */
@SuppressWarnings("serial")
public class LoginUserServlet extends LoginBaseServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        prepareResponse("Login", response);
        PrintWriter out = response.getWriter();
        String error = request.getParameter("error");
        int code = 0;

        if (error != null) {
            try {
                code = Integer.parseInt(error);
            } catch (Exception ex) {
                code = -1;
            }

            String errorMessage = getStatusMessage(code);
            out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
        }

        if (request.getParameter("newuser") != null) {
            out.println("<p>Registration was successful!");
            out.println("Login with your new username and password below.</p>");
        }

        if (request.getParameter("logout") != null) {
            clearCookies(request, response);
            out.println("<p>Successfully logged out.</p>");
        }
        if (getUsername(request) != null){
            response.sendRedirect("/viewhotels");
        }

        printForm(out);
        finishResponse(response);
    }
    /** The method that will process the form once it's submitted
     * @param request
     * @param response
     * */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String user = request.getParameter("user");
        String pass = request.getParameter("pass");

        //
        Status status = databaseHandler.authenticateUser(user, pass);

        try {
            if (status == Status.OK) {
                // should eventually change this to something more secure
                response.addCookie(new Cookie("login", "true"));
                response.addCookie(new Cookie("name", user));
                response.sendRedirect(response.encodeRedirectURL("/viewhotels"));
            }
            else {
                response.addCookie(new Cookie("login", "false"));
                response.addCookie(new Cookie("name", ""));
                response.sendRedirect(response.encodeRedirectURL("/login?error=" + status.ordinal()));
            }
        }
        catch (Exception ex) {
            log.error("Unable to process login form.", ex);
        }
    }

    private void printForm(PrintWriter out) {
        assert out != null;
        out.println("<h3>Login</h3>");
        out.println("<form action=\"/login\" method=\"post\">");
        out.println("<table border=\"0\">");
        out.println("<tr>");
        out.println("<td>Usename:</td>");
        out.println("<td><input placeholder=\"Enter username\" type=\"text\" name=\"user\" size=\"30\"></td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>Password:</td>");
        out.println("<td><input placeholder=\"Enter password\" type=\"password\" name=\"pass\" size=\"30\"></td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("<p><input type=\"submit\" value=\"Login\"></p>");
        out.println("</form>");
        out.println("<p>(<a href=\"/register\">new user? register here.</a>)</p>");
    }
}