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
