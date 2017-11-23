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
 * Servlet class to handle server get requests for hotelReviews
 */
public class AllReviewsServlet extends LoginBaseServlet{


    /**
     * A method that gets executed when the get request is sent to the AllReviewsServlet
     * @param request httpservletrequest
     * @param response httpservletrespons
     * @throws IOException IOException

     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (getUsername(request) != null) {
            PrintWriter out = response.getWriter();
            //Check that request contains hotel id
            if (request.getParameterMap().containsKey("hotelid")) {
                String hotelid = request.getParameter("hotelid");
                //Chack that hotel exist in database
                if (databaseHandler.checkHotelIdReviewSet(hotelid) == Status.OK) {
                    VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
                    VelocityContext context = new VelocityContext();
                    Template template = ve.getTemplate("templates/allreviews.html");
                    ArrayList<HotelReview> reviews = databaseHandler.hotelIdReviewDisplayer(hotelid);
                    context.put("reviews", reviews);
                    StringWriter writer = new StringWriter();
                    template.merge(context, writer);
                    out.println(writer.toString());
                } else {
                    //send some alert
                }
            }else{
                //Send some alert
                log.debug("Did not find it");
            }
        } else {
            response.sendRedirect("/login");
        }
    }
}


