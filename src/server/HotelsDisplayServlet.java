package server;

import databaseObjects.BasicHotelInfo;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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
            PrintWriter out = response.getWriter();

            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("templates/basicHotelInfo.html");

            ArrayList<BasicHotelInfo> hotelInfo = databaseHandler.hotelInfoDisplayer();
            context.put("username",getUsername(request));
            context.put("hotels", hotelInfo);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);

            out.println(writer.toString());

        }
        else {
            response.sendRedirect("/login");
        }
    }

}