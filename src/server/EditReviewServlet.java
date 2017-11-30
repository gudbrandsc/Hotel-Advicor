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
import java.util.Map;

/**
 * Servlet that lets a user edit their existing review for a hotel.
 */
public class EditReviewServlet extends LoginBaseServlet{

    /**
     * A method that gets executed when the get request is sent to the EditReviewServlet
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (getUsername(request) != null) {
            if (request.getParameterMap().containsKey("hotelid") && request.getParameterMap().containsKey("username")) {
                String hotelid = request.getParameter("hotelid");
                String username = request.getParameter("username");
                if (databaseHandler.checkForExistingUserReview(hotelid, username) == Status.OK) {
                    PrintWriter out = response.getWriter();
                    VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
                    VelocityContext context = new VelocityContext();
                    Template template = ve.getTemplate("static/templates/editReview.html");
                    HotelReview review = databaseHandler.getUserReviewForHotel(username,hotelid);
                    context.put("hotelname",databaseHandler.getHotelIdName(hotelid));
                    context.put("review", review);
                    context.put("date",getDate());
                    StringWriter writer = new StringWriter();
                    template.merge(context, writer);
                    out.println(writer.toString());
                } else {
                    response.sendRedirect("/addreview?hotelid="+hotelid);
                }
            }else {
                Status status = Status.MISSING_PARAM;
                response.sendRedirect("/viewhotels?error="+status.ordinal());
            }
        } else {
            response.sendRedirect("/login");
        }
    }
    /**
     * A method that is executed when the post request is sent to the AddReviewServlet
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String hotelid = request.getParameter("hotelid");
        int rating = Integer.parseInt(request.getParameter("rating"));
        String title = request.getParameter("title");
        String review = request.getParameter("review");
        String date = request.getParameter("date");
        String username = request.getParameter("username");
        log.debug("Sending post request to edit review...");

        Status status = databaseHandler.editReview(hotelid,rating,title,review,date,username);

        if(status == Status.OK) {
            log.debug("Review successfully edited");
            status=Status.UPDATE_REVIEW_SUCCESS;
            String url = "/myreviews?username="+username+"&success="+status.ordinal();
            response.sendRedirect(response.encodeRedirectURL(url));
        }
        else {
            log.debug("Not able to edit review:" + status);
            status=Status.UPDATE_REVIEW_ERROR;
            String url = "/myreviews?username="+username+"&error="+status.ordinal();
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }

    }
}
