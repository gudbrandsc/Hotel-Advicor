package server;

import databaseObjects.BasicHotelInfo;
import databaseObjects.HotelReview;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class SortReviewsServlet extends LoginBaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ArrayList<HotelReview> hotelReviews = null;
        PrintWriter out = response.getWriter();
        StringBuilder sb = new StringBuilder();
        String hotelid="";

        if(request.getParameter("hotelid")!=null && request.getParameter("date") !=null) {
            hotelid = request.getParameter("hotelid");
            hotelReviews = databaseHandler.hotelReviewsByDate(hotelid);
            if(hotelReviews.size()==0){
                sb.append(" <div class=\"alert alert-danger alert-dismissable\">");
                sb.append("<a class=\"close\" data-dismiss=\"alert\" aria-label=\"close\">&times;</a>");
                sb.append("<strong>There is no hotel matching your search</strong>");
                sb.append("</div>");
            }else{
                    sb.append("<p>Reviews sorted by date");
                for(HotelReview review:hotelReviews){
                    sb.append("<div class=\"panel panel-info\">");
                    sb.append("<div class=\"panel-heading\">"+review.getTitle()+"</div>");
                    sb.append("<div class=\"panel-body\">");
                    sb.append("<div class=\"card-body text-primary\">");
                    sb.append("<h4 class=\"card-title\">Summary:</h4>");
                    sb.append("<p class=\"card-text\">"+review.getReview()+"</p>");
                    sb.append("<div class=\"border-bottom\"></div>");
                    sb.append("<p>Rating: "+review.getRating()+"</p>");
                    sb.append("<p><small>posted by "+review.getUsername()+" on "+review.getDate()+"</small></p>");
                    sb.append("</div></div></div>");
                }
            }

        }else if(request.getParameter("hotelid")!=null && request.getParameter("rating") !=null) {
            hotelid = request.getParameter("hotelid");
            hotelReviews = databaseHandler.hotelReviewsByRating(hotelid);
            if(hotelReviews.size()==0){
                sb.append(" <div class=\"alert alert-danger alert-dismissable\">");
                sb.append("<a class=\"close\" data-dismiss=\"alert\" aria-label=\"close\">&times;</a>");
                sb.append("<strong>There is no hotel matching your search</strong>");
                sb.append("</div>");
            }else{
                sb.append("<p>Reviews sorted by rating");
                for(HotelReview review:hotelReviews){
                    sb.append("<div class=\"panel panel-info\">");
                    sb.append("<div class=\"panel-heading\">"+review.getTitle()+"</div>");
                    sb.append("<div class=\"panel-body\">");
                    sb.append("<div class=\"card-body text-primary\">");
                    sb.append("<h4 class=\"card-title\">Summary:</h4>");
                    sb.append("<p class=\"card-text\">"+review.getReview()+"</p>");
                    sb.append("<div class=\"border-bottom\"></div>");
                    sb.append("<p>Rating: "+review.getRating()+"</p>");
                    sb.append("<p><small>posted by "+review.getUsername()+" on "+review.getDate()+"</small></p>");
                    sb.append("</div></div></div>");

                }
            }

        }
        out.printf(sb.toString());
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
