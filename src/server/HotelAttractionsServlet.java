package server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HotelAttractionsServlet extends LoginBaseServlet{
    /**
     * A method that gets executed when the get request is sent to the HotelAttractionsServlet
     * @param request
     * @param response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (getUsername(request) != null) {
            PrintWriter out = response.getWriter();
            prepareResponse("Hotel attraction", response);
            out.println("<button><a href=\"/login?logout\">Logout</a></button>");
            printForm(request,response);
            finishResponse(response);
            }

        else {
            response.sendRedirect("/login");
        }
    }

    /**
     * Method to print attraction information to response writer. Also handel if get request miss param
     * @param request
     * @param response
     */
    private void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int radius;
        String hotelId;
        PrintWriter out = response.getWriter();


        if(request.getParameter("radius") != null && request.getParameter("hotelId") != null){ //Check that both param are in GET request
            hotelId = request.getParameter("hotelId");
            radius = Integer.parseInt(request.getParameter("radius"));
            if(databaseHandler.checkIfHotelExist(hotelId)){
                out.printf(hotelapp.TouristAttractionFinder.fetchAttractions(radius, hotelId));
            }else{
                out.println("<p>Hotel does not exist</p>");
            }
        } else{
            out.printf("<p>Invalid request</p>" + System.lineSeparator());
        }
    }
}
//TODO: add average rating