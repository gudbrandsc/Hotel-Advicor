package server;

import databaseObjects.BasicHotelInfo;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;
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
            ArrayList<BasicHotelInfo> hotelInfo = null;
            if(request.getParameter("city")!=null && request.getParameter("hotelname")!=null){
                String city = request.getParameter("city").replaceAll(Pattern.quote("+")," ");
                String hotelname = request.getParameter("hotelname").replaceAll(Pattern.quote("+")," ");
                if(city.equals("--Select city--")){
                    city="";
                }
                hotelInfo = databaseHandler.hotelInfoSearchDisplayer(city,hotelname);

            }else{
                hotelInfo = databaseHandler.hotelInfoDisplayer();
            }

            String error = request.getParameter("error");
            String success = request.getParameter("success");
            String errorMessage =null;
            String successMessage =null;
            boolean successalert = false;
            boolean erroralert = false;
            int code = 0;

            if (error != null) {
                code = integerParser(error);
                errorMessage = getStatusMessage(code);
                erroralert = true;
            } else if (success != null) {
                code = integerParser(success);
                successMessage = getStatusMessage(code);
                successalert = true;
            }


            PrintWriter out = response.getWriter();
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("static/templates/basicHotelInfo.html");
            ArrayList<String> cities = databaseHandler.getAllHotelCities();
            context.put("username",getUsername(request));
            context.put("errorMessage", errorMessage);
            context.put("erroralert", erroralert);
            context.put("successMessage", successMessage);
            context.put("successalert", successalert);
            context.put("hotels", hotelInfo);
            context.put("cities", cities);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());

        }
        else {
            response.sendRedirect("/login");
        }
    }

}