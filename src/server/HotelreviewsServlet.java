package server;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet class to handle server get requests for hotelReviews
 */
public class HotelreviewsServlet extends LoginBaseServlet{


    /**
     * A method that gets executed when the get request is sent to the HotelAttractionsServlet
     * @param request
     * @param response
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
     * Method that prints hotel reviews to a specific hotel. Also handeles if get request miss param
     * @param request
     * @param response
     */
    private void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String hotelid = request.getParameter("hotelId");
        PrintWriter out = response.getWriter();

        if (hotelid != null) {
            if (databaseHandler.checkHotelReviewSet(hotelid)){
                out.println(databaseHandler.hotelReviewDisplayer(hotelid));
            } else {
                out.println("<p>There is unfortunately no reviews for " + databaseHandler.getHotelName(hotelid));
            }
        }
    }
}
