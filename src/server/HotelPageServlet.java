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
            String error = request.getParameter("error");
            String errorMessage =null;
            boolean erroralert = false;
            int code = 0;
            if (error != null) {
                code = integerParser(error);
                errorMessage = getStatusMessage(code);
                erroralert = true;
            }

            PrintWriter out = response.getWriter();

            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("static/templates/hotelInfo.html");
            String hotelId = request.getParameter("hotelid");
            boolean saved = databaseHandler.checkIfHotelIsSaved(getUsername(request)+hotelId);
            String name = databaseHandler.getHotelIdName(hotelId);
            String address = databaseHandler.getHotelIdAddress(hotelId);
            String city = databaseHandler.getHotelCity(hotelId);
            Double rating = databaseHandler.getHotelIdRating(hotelId);

            String expedia = "https://www.expedia.com/"+city+"-hotels-"+name+".h"+hotelId+".Hotel-Information";
            expedia = expedia.replaceAll(" ","-");
            String lastLogin = databaseHandler.getLastLogintime(getUsername(request));
            if(lastLogin == null ){
                context.put("lastLogin","First visit :D");
            }else {
                context.put("lastLogin",lastLogin);
            }
            context.put("errorMessage",errorMessage);
            context.put("erroralert",erroralert);
            context.put("saved",saved);
            context.put("name",name);
            context.put("address", address);
            context.put("rating", rating);
            context.put("hotelid",hotelId);
            context.put("expedia",expedia);

            StringWriter writer = new StringWriter();
            template.merge(context, writer);

            out.println(writer.toString());

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
        Status status = Status.ERROR;
        String hotelid = request.getParameter("hotelid");
        String username = getUsername(request);
        String key= username+hotelid;
        log.debug(request.getParameter("save"));
        log.debug(request.getParameter("unsave"));


        if(request.getParameter("save")!=null){
             status = databaseHandler.saveHotel(hotelid,username);
        }else if(request.getParameter("unsave")!=null){
             status = databaseHandler.unsaveHotel(key);

        }

        if(status == Status.OK) {
            response.sendRedirect(response.encodeRedirectURL("/hotel?hotelid="+hotelid));
        }
        else {
            String url = "/viewhotels?error=" + status.ordinal();
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }
    }

}
