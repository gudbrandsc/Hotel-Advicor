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
            //Check that request contains hotel id
            if (request.getParameterMap().containsKey("hotelid")) {
                String hotelid = request.getParameter("hotelid");
                //Check that hotel id exist in db
                if(databaseHandler.checkIfHotelExist(hotelid)==Status.OK){
                    boolean hotelReviews=false;
                    ArrayList<HotelReview> reviews=null;
                    //Check if hotel has any reviews.
                    if(databaseHandler.checkHotelIdReviewSet(hotelid)==Status.OK){
                        hotelReviews=true;
                        reviews = databaseHandler.hotelIdReviewDisplayer(hotelid);
                    }

                    PrintWriter out = response.getWriter();
                    VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
                    VelocityContext context = new VelocityContext();
                    Template template = ve.getTemplate("static/templates/allreviews.html");
                    Boolean existingReview  = false;
                    //Check if the user already have an review.
                    if (databaseHandler.checkForExistingUserReview(hotelid,getUsername(request))==Status.OK){
                        existingReview = true;
                    }
                    context.put("existingReview",existingReview);
                    context.put("hotelReviews",hotelReviews);
                    context.put("hotelid", hotelid);
                    context.put("username", getUsername(request));
                    context.put("reviews", reviews);
                    StringWriter writer = new StringWriter();
                    template.merge(context, writer);
                    out.println(writer.toString());
                }else {
                    Status status = Status.INVALID_HOTELID;
                    response.sendRedirect("/viewhotels?error="+status.ordinal());
                    log.debug("There is no hotel with matching hotel id");
                }
            }else{
                Status status = Status.MISSING_HOTELID;
                response.sendRedirect("/viewhotels?error="+status.ordinal());
                log.debug("The request was missing hotel id");

            }
        } else {
            response.sendRedirect("/login");
        }
    }
}

