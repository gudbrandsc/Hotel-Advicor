package server;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that displays all hotel names, address and average rating.
 *
 */
@SuppressWarnings("serial")
public class HotelsDisplayServlet extends LoginBaseServlet {
    /**
     * A method that gets executed when a get request is sent to the HotelsDisplayServlet.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (getUsername(request) != null) {
            prepareResponse("View hotels", response);
            PrintWriter out = response.getWriter();
            out.println(databaseHandler.hotelInfoDisplayer());
            finishResponse(response);
        }
        else {
            response.sendRedirect("/login");
        }
    }

}