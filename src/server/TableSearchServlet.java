package server;

import databaseObjects.BasicHotelInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class TableSearchServlet extends LoginBaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ArrayList<BasicHotelInfo> hotelInfo = null;
        PrintWriter out = response.getWriter();
        StringBuilder sb = new StringBuilder();

        if(request.getParameter("city")!=null && request.getParameter("key")!=null) {
            String city = request.getParameter("city").replaceAll(Pattern.quote("+"), " ");
            String key = request.getParameter("key").replaceAll(Pattern.quote("+"), " ");
            hotelInfo = databaseHandler.hotelInfoSearchDisplayer(city, key);

            if(hotelInfo.size()==0){
                sb.append(" <div class=\"alert alert-danger alert-dismissable\">");
                sb.append("<a class=\"close\" data-dismiss=\"alert\" aria-label=\"close\">&times;</a>");
                sb.append("<strong>There is no hotel matching your search</strong>");
                sb.append("</div>");
            }else{
                sb.append("<table id=\"hotelsTable\" class=\"table table-striped\">");
                sb.append("<thead class=\"thead-dark\">");
                sb.append("<tr><th>Hotel name</th><th>City</th><th>Address</th><th>Rating</th></tr></thead><tbody>");

                for(BasicHotelInfo bh:hotelInfo){
                    sb.append("<tr><td><a href=\"/hotel?hotelid="+bh.getHotelId()+"\">"+bh.getName()+"</a></td>");
                    sb.append("<td>"+bh.getCity()+"</td>");
                    sb.append("<td>"+bh.getAddress()+"</td>");
                    sb.append("<td>"+bh.getRating()+"</td></tr>");
                }
                sb.append("</tbody></table>");
            }
            out.printf(sb.toString());

        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

}

//TODO parse