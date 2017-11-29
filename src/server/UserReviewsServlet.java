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
            if(request.getParameter("username")!=getUsername(request)){
                String username = request.getParameter("username");
                boolean userReviews = false;
                ArrayList<HotelReview> reviews=null;
                if(databaseHandler.checkUsernameReviewSet(username) == Status.OK){
                    userReviews=true;
                    reviews = databaseHandler.usernameReviewDisplayer(username);
                }
                PrintWriter out = response.getWriter();
                VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
                VelocityContext context = new VelocityContext();
                Template template = ve.getTemplate("templates/myReviews.html");
                context.put("reviews", reviews);
                context.put("userReviews",userReviews);
                StringWriter writer = new StringWriter();
                template.merge(context, writer);
                out.println(writer.toString());
                //response.sendRedirect("myreviews?username="+getUsername(request));
            }else {
                //response.sendRedirect("myreviews?username="+getUsername(request));

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
            prepareResponse("Delete hotel review", response);
            Status status = databaseHandler.removeReview(username,request.getParameter("hotelid"));

            if(status == Status.OK) {
                log.debug("Review successfully removed");
                String url = "/myreviews?username="+username+"&delete=true";
                response.sendRedirect(response.encodeRedirectURL(url));
            }
            else {
                log.debug("Not able to remove review:" + status);
                String url = "/myreviews?username="+username+"&delete=true";
                url = response.encodeRedirectURL(url);
                response.sendRedirect(url);
            }
        } else if(request.getParameter("edit")!=null){
            String url = "/editreview?username="+username+"&hotelid="+request.getParameter("hotelid");
            response.sendRedirect(response.encodeRedirectURL(url));
        }

    }
}
