package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles display of user information.
 * Example of Prof. Engle
 * @see LoginServer
 */
@SuppressWarnings("serial")
public class HotelsDisplayServlet extends LoginBaseServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String user = getUsername(request);

        if (user != null) {
            prepareResponse("View hotels", response);
            StringBuilder sb = new StringBuilder();
            PrintWriter out = response.getWriter();
            out.println(databaseHandler.getHotelNames());
            finishResponse(response);
        }
        else {
            response.sendRedirect("/login");
        }
    }
    //TODO can be removed
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        doGet(request, response);
    }
}