package server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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
     * Method that prints all hotel reviews to a specific hotel.
     * @param request httpservletrequest
     * @param response httpservletrespons
     * @throws IOException IOException

     */
    private void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        //Check if request matches pattern
        String hotelid = request.getParameter("hotelid");
        //Check if there is a hotel with matching hotel id in the db
        if(!databaseHandler.getHotelIdName(request.getParameter("hotelid")).isEmpty()){
            //Check if hotel has any reviews
            if (databaseHandler.checkHotelIdReviewSet(hotelid)==Status.OK){
                if (request.getParameter("success") != null) {
                    out.println("<p>Thank you, your review for hotel ");
                    out.println(databaseHandler.getHotelIdName(request.getParameter("hotelid"))+ " is now submitted.</p>");
                }else if (request.getParameter("error") != null){
                    out.println("<p>We where unfortunately not able to handle your request at this time ");
                    out.println("Pleas try again later</p>");
                }
                if(databaseHandler.checkForExistingUserReview(hotelid,getUsername(request))==Status.OK){
                    out.println("<p><a href=\"/editreview?username="+getUsername(request)+"&hotelid="+ hotelid+"\">Edit your review</a></p>" );
                }else{
                    out.println("<p><a href=\"/addreview?hotelid="+ hotelid+"\">Add review</a></p>" );
                }
                out.println("<h3>Reviews for hotel:  "+databaseHandler.getHotelIdName(hotelid)+" </h3>");
                out.println(" <form action = \"/attractions\" method = \"post\">");
                out.println("<input type=\"hidden\" value=\""+hotelid+"\" name=\"hotelid\" />\n");
                out.println("<input pattern=\"[0-9]{0,4}\" required placeholder=\"Enter radius..\" type=\"text\" name=\"radius\">");
                out.println("<input type=\"submit\" value=\"Show attractions\">");
                out.println("</form>");
                out.println(databaseHandler.hotelIdReviewDisplayer(hotelid));
            } else {
                out.println("<p>There is unfortunately no reviews for " + databaseHandler.getHotelIdName(hotelid)+"</p>");
                out.println("<p>Be the first one to give it a review:<a href=\"/addreview?hotelid="+ hotelid+"\">Add review</a></p>" );
            }
        }else{
            out.println("<p>There is unfortunately no hotel with this id</p>");
        }
    }
}

