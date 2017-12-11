package server;
import databaseObjects.*;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

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
            if (request.getParameterMap().containsKey("hotelid") && request.getParameterMap().containsKey("radius")) {
                String hotelid = request.getParameter("hotelid");
                int radius = Integer.parseInt(request.getParameter("radius"));
                Status status = databaseHandler.checkIfHotelExist(hotelid);
                if(status==Status.OK){
                    ArrayList<HotelAttractions> attractions = hotelapp.TouristAttractionFinder.fetchAttractions(radius, hotelid);
                    PrintWriter out = response.getWriter();
                    VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
                    VelocityContext context = new VelocityContext();
                    Template template = ve.getTemplate("static/templates/hotelAttractions.html");
                    String lastLogin = databaseHandler.getLastLogintime(getUsername(request));
                    if(lastLogin.equals("null") ){
                        context.put("lastLogin","First visit :D");
                    }else {
                        context.put("lastLogin",lastLogin);
                    }
                    context.put("hotelname",databaseHandler.getHotelIdName(hotelid));
                    context.put("attractions", attractions);
                    context.put("radius",radius);
                    context.put("username",getUsername(request));
                    StringWriter writer = new StringWriter();
                    template.merge(context, writer);
                    out.println(writer.toString());
                }else{
                    log.debug("Hotel does not exist");
                }
            } else {
                log.debug("redirect");
            }
        } else {
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
        String hotelid = request.getParameter("hotelid");
        String radius = request.getParameter("radius");
        response.sendRedirect(response.encodeRedirectURL("/attractions?hotelid="+hotelid+"&radius="+radius));
    }
}
