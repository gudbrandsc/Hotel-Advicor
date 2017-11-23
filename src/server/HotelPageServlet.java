package server;

import databaseObjects.BasicHotelInfo;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class HotelPageServlet extends LoginBaseServlet {
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
            Template template = ve.getTemplate("templates/hotelInfo.html");
            String hotelId = request.getParameter("hotelid");
            String name = databaseHandler.getHotelIdName(hotelId);
            String address = databaseHandler.getHotelIdAddress(hotelId);
            String city = databaseHandler.getHotelCity(hotelId);
            Double rating = databaseHandler.getHotelIdRating(hotelId);

            String expedia = "https://www.expedia.com/"+city+"-hotels-"+name+".h"+hotelId+".Hotel-Information";
            expedia = expedia.replaceAll(" ","-");

            context.put("name",name);
            context.put("address", address);
            context.put("rating", rating);
            context.put("hotelid",hotelId);
            context.put("expedia",expedia);
            System.out.println(expedia);


            StringWriter writer = new StringWriter();
            template.merge(context, writer);

            out.println(writer.toString());

        }
        else {
            response.sendRedirect("/login");
        }
    }
}
