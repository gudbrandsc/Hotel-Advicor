package server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginRedirectServlet extends LoginBaseServlet {

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
    //TODO: Check if we need this
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        doGet(request, response);
    }
}
