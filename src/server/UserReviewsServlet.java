package server;

import databaseObjects.HotelReview;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Servlet that handles display of user reviews, and lets user edit and delete there reviews.
 */
public class UserReviewsServlet extends LoginBaseServlet {

    /**
     * A method that gets executed when the get request is sent to the UserReviewsServlet
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (getUsername(request) != null) {
            if(request.getParameter("username").equals(getUsername(request))){
                String error = request.getParameter("error");
                String success = request.getParameter("success");
                String errorMessage =null;
                String successMessage =null;
                boolean successalert = false;
                boolean erroralert = false;
                int code = 0;
                log.debug("inside");
                if (error != null) {
                    code = integerParser(error);
                    errorMessage = getStatusMessage(code);
                    erroralert = true;
                } else if (success != null) {
                    code = integerParser(success);
                    successMessage = getStatusMessage(code);
                    successalert = true;
                }

                boolean userHasReviews = false;
                String username = request.getParameter("username");
                ArrayList<HotelReview> reviews=null;
                if(databaseHandler.checkUsernameReviewSet(username) == Status.OK){
                    userHasReviews=true;
                    reviews = databaseHandler.usernameReviewDisplayer(username);
                }
                log.debug(userHasReviews);
                PrintWriter out = response.getWriter();
                VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
                VelocityContext context = new VelocityContext();
                Template template = ve.getTemplate("static/templates/myReviews.html");
                context.put("errorAlert", erroralert);
                context.put("errorMessage", errorMessage);
                context.put("successAlert", successalert);
                context.put("successMessage", successMessage);
                context.put("reviews", reviews);
                context.put("userReviews",userHasReviews);
                StringWriter writer = new StringWriter();
                template.merge(context, writer);
                out.println(writer.toString());
            }else {
                log.debug("redirect");
                response.sendRedirect("myreviews?username="+getUsername(request));

            }

        } else {
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
        if(request.getParameter("delete")!=null){
            Status status = databaseHandler.removeReview(username,request.getParameter("hotelid"));

            if(status == Status.OK) {
                log.debug("Review successfully removed");
                status=Status.REMOVE_REVIEW_SUCCESS;
                String url = "/myreviews?username="+username+"&success="+status.ordinal();
                response.sendRedirect(response.encodeRedirectURL(url));
            }
            else {
                log.debug("Not able to remove review:" + status);
                status=Status.REMOVE_REVIEW_ERROR;
                String url = "/myreviews?username="+username+"&error="+status.ordinal();
                url = response.encodeRedirectURL(url);
                response.sendRedirect(url);
            }
        } else if(request.getParameter("edit")!=null){
            String url = "/editreview?username="+username+"&hotelid="+request.getParameter("hotelid");
            response.sendRedirect(response.encodeRedirectURL(url));
        }

    }
}
