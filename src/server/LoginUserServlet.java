package server;


import databaseObjects.HotelReview;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class that handles login requests.
 *
 */
@SuppressWarnings("serial")
public class LoginUserServlet extends LoginBaseServlet {
    /**
     * A method that gets executed when the get request is sent to the LoginUserServlet
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException

     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        String error = request.getParameter("error");
        int code = 0;
        boolean newuser=false;
        boolean logout = false;
        boolean erroralert = false;
        String errorMessage =null;

        if (error != null) {
            try {
                code = Integer.parseInt(error);
            } catch (Exception ex) {
                code = -1;
            }

            errorMessage = getStatusMessage(code);
            erroralert = true;
        }

        if (request.getParameter("newuser") != null) {
            newuser=true;
        }

        if (request.getParameter("logout") != null) {
            databaseHandler.setLastLoginTime(getUsername(request));
            clearCookies(request, response);

            logout=true;
        }
        if (getUsername(request) != null){
            response.sendRedirect("/welcome");
        }

        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("static/templates/login.html");
        context.put("newuser",newuser);
        context.put("logout",logout);
        context.put("error",erroralert);
        context.put("errormessage",errorMessage);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        out.println(writer.toString());

    }
    /** The method that will handle post requests sent to LoginUserServlet
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String user = request.getParameter("user");
        String pass = request.getParameter("pass");
        Status status = databaseHandler.authenticateUser(user, pass);

        try {
            if (status == Status.OK) {
                // should eventually change this to something more secure
                response.addCookie(new Cookie("login", "true"));
                response.addCookie(new Cookie("name", user));
                databaseHandler.setLoginTime(getLoginDate(),user);
                response.sendRedirect(response.encodeRedirectURL("/welcome"));
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

}