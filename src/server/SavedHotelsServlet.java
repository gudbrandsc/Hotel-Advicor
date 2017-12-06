package server;

import databaseObjects.BasicHotelInfo;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class SavedHotelsServlet extends LoginBaseServlet {
    /**
     * A method that gets executed when a get request is sent to the HotelsDisplayServlet.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (getUsername(request) != null) {
            ArrayList<BasicHotelInfo> hotelInfo = null;
            String errorMessage =null;
            int code = 0;
            boolean erroralert = false;


            if(databaseHandler.checkIfUserHasSavedHotels(getUsername(request))==Status.OK){
                hotelInfo = databaseHandler.userLikedHotelsDisplayer(getUsername(request));
            }else{
                code = Status.NO_SAVED_HOTELS.ordinal();
                errorMessage = getStatusMessage(code);
                erroralert = true;

            }

            //TODO remove all liked hotels

            PrintWriter out = response.getWriter();
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("static/templates/userprofile.html");
            ArrayList<String> cities = databaseHandler.getAllHotelCities();
            context.put("username",getUsername(request));
            context.put("errorMessage", errorMessage);
            context.put("erroralert", erroralert);
            context.put("hotels", hotelInfo);
            context.put("cities", cities);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());

        }
        else {
            response.sendRedirect("/login");
        }
    }
    /** The method that will handle post request sent to UserReviewsServlet.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     * */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String username=request.getParameter("username");
        Status status = databaseHandler.removeAllSavedHotels(username);

        if(status == Status.OK) {
            log.debug("All saved hotels successfully removed");
            status=Status.REMOVED_ALL_SAVED_HOTELS_SUCCESS;
            String url = "/viewhotels?username="+username+"&success="+status.ordinal();
            response.sendRedirect(response.encodeRedirectURL(url));
        }
        else {
            log.debug("Not able to remove all saved hotels:" + status);
            status=Status.REMOVED_ALL_SAVED_HOTELS_ERROR;
            String url = "/myreviews?username="+username+"&error="+status.ordinal();
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }
    }
}
