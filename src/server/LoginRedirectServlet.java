package server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginRedirectServlet extends LoginBaseServlet {
    /**
     * A method that gets executed when a get request is sent to the LoginRedirectServlet.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (getUsername(request) != null) {
            response.sendRedirect("/welcome");
        }
        else {
            response.sendRedirect("/login");
        }
    }
}
