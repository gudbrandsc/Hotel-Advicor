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
 * Servlet that is used to add new reviews for a hotel
 */
public class AddReviewServlet extends LoginBaseServlet {
    /**
     * A method that gets executed when the get request is sent to the AddReviewServlet
     * @param request httpservletrequest
     * @param response httpsservletrespons
     * @throws IOException IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (getUsername(request) != null) {
            if (request.getParameterMap().containsKey("hotelid")) {
                String hotelid = request.getParameter("hotelid");
                if (databaseHandler.checkHotelIdReviewSet(hotelid) == Status.OK) {
                    PrintWriter out = response.getWriter();
                    VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
                    VelocityContext context = new VelocityContext();
                    Template template = ve.getTemplate("templates/addReview.html");
                    String username = getUsername(request);
                    context.put("hotelname",databaseHandler.getHotelIdName(hotelid));
                    context.put("hotelid", hotelid);
                    context.put("username", username);
                    context.put("date",getDate());
                    StringWriter writer = new StringWriter();
                    template.merge(context, writer);
                    out.println(writer.toString());
                }else {

                }

            }else {

            }

        }
        else {
            response.sendRedirect("/login");
        }
    }
    /**
     * A method that is executed when the post request is sent to the AddReviewServlet
     * @param request httpservletrequest
     * @param response httpsservletrespons
     * @throws IOException IOException

     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse("Add hotel review", response);

        String hotelid = request.getParameter("hotelid");
        int rating = Integer.parseInt(request.getParameter("rating"));
        String title = request.getParameter("title");
        String review = request.getParameter("review");
        String date = request.getParameter("date");
        String username = request.getParameter("username");
        log.debug("Sending post request to add review...");
        Status status = databaseHandler.addReview(hotelid,rating,title,review,date,username);

        if(status == Status.OK) {
            log.debug("Review successfully added");
            String url = "/myreviews?username="+username+"&success=true";
            response.sendRedirect(response.encodeRedirectURL(url));
        }
        else {
            log.debug("Not able to add review:" + status);
            String url = "/myreviews?username="+username+"&error=true";
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }

    }

}
