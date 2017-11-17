package server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class UserReviewsServlet extends LoginBaseServlet {

    /**
     * A method that gets executed when the get request is sent to the HotelAttractionsServlet
     * @param request
     * @param response
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (getUsername(request) != null) {
            if(request.getParameter("username")==null){
                log.debug("Missing username in get request");
                response.sendRedirect("myreviews?username="+getUsername(request));
            }
            prepareResponse("My reviews", response);
            PrintWriter out = response.getWriter();
            out.println("<button><a href=\"/login?logout\">Logout</a></button>");
            printForm(request,response);
            finishResponse(response);
        }
        else {
            response.sendRedirect("/login");
        }
    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse("Add hotel review", response);
        log.debug(request.getParameter("edit"));
        log.debug(request.getParameter("hotelid"));
        String hotelid= request.getParameter("hotelid");
        if (request.getParameter("edit").equals("edit")) {
            log.debug("edit review");
        }
        else if(request.getParameter("delete").equals("delete")){
            databaseHandler.removeReview(getUsername(request),hotelid);
        }


    }


    /**
     * Method that prints hotel reviews to a specific hotel. Also handeles if get request miss param
     * @param request
     * @param response
     */
    private void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        if(request.getQueryString().matches("^username=[a-zA-Z0-9_-]{5,20}$")){
            log.debug(getUsername(request));
            String username = request.getParameter("username");
            //Check that request username is the same as the currant user.
            if(username.equalsIgnoreCase(getUsername(request))){
                log.debug(getUsername(request));
                //Check if currant user has any reviews
                if(databaseHandler.checkUsernameReviewSet(getUsername(request))){
                    //TODO get hotelId somehow
                    out.println(databaseHandler.hotelReviewDisplayer(username,"username","123434"));
                    out.println("<p><input type=\"submit\" value=\"Register user\"></p>");
                }else {
                    out.println("<p>You don't have any reviews yet</p>");
                }
            }else{
                out.println("<p>Username is invalid</p>");
            }
        }else{
            out.println("Invalid request");
        }
    }
}
