package server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 * Servlet class to handle server get requests for hotelReviews
 */
public class AllReviewsServlet extends LoginBaseServlet{


    /**
     * A method that gets executed when the get request is sent to the HotelAttractionsServlet
     * @param request
     * @param response
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (getUsername(request) != null) {
            if(request.getParameter("hotelid")==null){
                log.debug("Missing hotelid in get request");
                response.sendRedirect("/viewhotels");
            }

            prepareResponse("Reviews", response);
            PrintWriter out = response.getWriter();
            out.println("<button><a href=\"/login?logout\">Logout</a></button>");
            printForm(request,response);
            finishResponse(response);
        }
        else {
            response.sendRedirect("/login");
        }
    }

    /**
     * Method that prints hotel reviews to a specific hotel. Also handeles if get request miss param
     * @param request
     * @param response
     *
     */
    private void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        //Check if request matches pattern
        if(request.getParameter("hotelid")!=null){
            String hotelid = request.getParameter("hotelid");
            //Check if there is a hotel with matching hotel id in the db
            if(!databaseHandler.getHotelIdName(request.getParameter("hotelid")).isEmpty()){
                //Check if hotel has any reviews
                if (databaseHandler.checkHotelIdReviewSet(hotelid)){
                    if (request.getParameter("success") != null) {
                        out.println("<p>Thank you, your review for hotel ");
                        out.println(databaseHandler.getHotelIdName(request.getParameter("hotelid"))+ " is now submitted.</p>");
                    }else if (request.getParameter("error") != null){
                        out.println("<p>We where unfortunately not able to add your review at this time.");
                        out.println(databaseHandler.getHotelIdName("Please try again later</p>"));

                    }
                    out.println(databaseHandler.hotelIdReviewDisplayer(hotelid));
                } else {
                    out.println("<p>There is unfortunately no reviews for " + databaseHandler.getHotelIdName(hotelid)+"</p>");
                    out.println("<p>Be the first one to give it a review:<a href=\"/addreview?hotelid="+ hotelid+"\">Add review</a></p>" );
                }
            }else{
                out.println("<p>There is unfortunately no hotel with this id</p>");
            }
        }else{
            out.println("Invalid request");
        }
    }
}
