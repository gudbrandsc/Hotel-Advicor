package server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HotelAttractionsServlet extends LoginBaseServlet{
    /**
     * A method that gets executed when the get request is sent to the HotelAttractionsServlet
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (getUsername(request) != null) {
            prepareResponse("Hotel attraction", response);
            printForm(request,response);
            finishResponse(response);
            }

        else {
            response.sendRedirect("/login");
        }
    }
    /** The method that will process the form once it's submitted
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     * */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse("Hotel attractions", response);
        String hotelid = request.getParameter("hotelid");
        String radius = request.getParameter("radius");
        response.sendRedirect(response.encodeRedirectURL("/attractions?hotelId="+hotelid+"&radius="+radius));
    }

    /**
     * Method to print attraction information to response writer. Also handel if get request miss param
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    private void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int radius;
        String hotelId;
        PrintWriter out = response.getWriter();
        //Check that both param are in the GET request
        if(request.getParameter("radius") != null && request.getParameter("hotelId") != null){
            hotelId = request.getParameter("hotelId");
            radius = Integer.parseInt(request.getParameter("radius"));
            Status status = databaseHandler.checkIfHotelExist(hotelId);

            if(status==Status.OK){
                out.printf(hotelapp.TouristAttractionFinder.fetchAttractions(radius, hotelId));
            }else{
                out.println("<p>Hotel does not exist</p>");
            }
        } else{
            out.printf("<p>Invalid request</p>" + System.lineSeparator());
        }
    }
}
