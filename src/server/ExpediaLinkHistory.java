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

public class ExpediaLinkHistory  extends LoginBaseServlet {

    /** The method that will handle post request sent to UserReviewsServlet.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     * */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Status status = Status.ERROR;

        if(request.getParameter("visit")!=null){
            String hotelid = request.getParameter("hotelid");
            String username = getUsername(request);
            String hotelname = request.getParameter("hotelname");
            String link = request.getParameter("link");
            String key= username+hotelid;
            //Check if user has link already
            status=databaseHandler.checkIfUserHasSavedLink(key);
            if(status==Status.OK){
                response.sendRedirect(link);
            }else{
                status = databaseHandler.addVisitedExpedialink(hotelid,username,link,hotelname);
            }
            if(status == Status.OK) {

                response.sendRedirect(link);
            }
        }else if(request.getParameter("clearhistory")!=null){
            status= databaseHandler.clearUserLinkHistory(getUsername(request));
            if(status==Status.OK){
                log.debug("All link history successfully removed");
                status=Status.REMOVED_ALL_LINK_HISTORY_SUCCESS;
                String url = "/viewhotels?success="+status.ordinal();
                response.sendRedirect(response.encodeRedirectURL(url));
            }else{
                log.debug("All link history successfully removed");
                status=Status.REMOVED_ALL_LINK_HISTORY_ERROR;
                String url = "/viewhotels?error="+status.ordinal();
                response.sendRedirect(response.encodeRedirectURL(url));
            }
        }

        else {
            String url = "/viewhotels?error=" + status.ordinal();
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }





        //remove all links for a user
        // Create username and hotelid
        // Check if user has clicked this link before if true redirect
        // else add username+hotelId primkey and store the url and username(to get all visited by user)
        // then redirect
    }
}
