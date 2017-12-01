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
            ArrayList<ExpediaLinkInfo> links = null;
            String errorMessage =null;
            int code = 0;
            boolean erroralert = false;


            if(databaseHandler.checkIfUserHasLinkHistory(getUsername(request))==Status.OK){
                links = databaseHandler.getAllVisitedLinks(getUsername(request));
            }else{
                code = Status.NO_LINK_HISTORY.ordinal();
                errorMessage = getStatusMessage(code);
                erroralert = true;

            }


            PrintWriter out = response.getWriter();
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("static/templates/allLinks.html");
            context.put("errorMessage", errorMessage);
            context.put("erroralert", erroralert);
            context.put("links", links);
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
