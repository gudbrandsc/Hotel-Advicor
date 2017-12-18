package server;

import databaseObjects.BasicHotelInfo;
import databaseObjects.ExpediaLinkInfo;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class UserProfileServlet extends LoginBaseServlet{

    /**
     * A method that gets executed when a get request is sent to the UserProfileServlet.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (getUsername(request) != null) {
            ArrayList<BasicHotelInfo> hotelInfo = null;
            ArrayList<ExpediaLinkInfo> links = null;

            String savedMessage =null;
            String linkMessage =null;

            int code = 0;
            boolean savedHotels = false;
            boolean visitedLinks = false;


            if(databaseHandler.checkIfUserHasSavedHotels(getUsername(request))==Status.OK){
                hotelInfo = databaseHandler.userLikedHotelsDisplayer(getUsername(request));
            }else{
                code = Status.NO_SAVED_HOTELS.ordinal();
                savedMessage = getStatusMessage(code);
                savedHotels = true;

            }
            if(databaseHandler.checkIfUserHasLinkHistory(getUsername(request))==Status.OK){
                links = databaseHandler.getAllVisitedLinks(getUsername(request));
            }else{
                code = Status.NO_LINK_HISTORY.ordinal();
                linkMessage = getStatusMessage(code);
                visitedLinks = true;

            }

            PrintWriter out = response.getWriter();
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("static/templates/userprofile.html");
            ArrayList<String> cities = databaseHandler.getAllHotelCities();
            String lastLogin = databaseHandler.getLastLogintime(getUsername(request));
            if(lastLogin.equals("null") ){
                context.put("lastLogin","First visit :D");
            }else {
                context.put("lastLogin",lastLogin);
            }
            context.put("username",getUsername(request));
            context.put("linkMessage", linkMessage);
            context.put("visitedLinks", visitedLinks);
            context.put("savedMessage", savedMessage);
            context.put("savedHotels", savedHotels);
            context.put("hotels", hotelInfo);
            context.put("links", links);
            context.put("cities", cities);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());

        }
        else {
            response.sendRedirect("/login");
        }
    }
}
