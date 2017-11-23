package server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet that is used to add new reviews for a hotel
 */
public class AddReviewServlet extends LoginBaseServlet {
    /**
     * A method that gets executed when the get request is sent to the AddReviewServlet
     * @param request httpservletrequest
     * @param response httpsservletrespons
     * @throws IOException IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (getUsername(request) != null) {
            String hotelId = request.getParameter("hotelid");
            //Check if user have a existing review and redirect, else print add review form
            if(databaseHandler.checkForExistingUserReview(hotelId,getUsername(request))==Status.OK){
                response.sendRedirect("/editreview?username="+getUsername(request)+"&hotelid="+hotelId);
            }else{
                prepareResponse("Add review", response);
                PrintWriter out = response.getWriter();
                printForm(request,out);
                finishResponse(response);
            }
        }
        else {
            response.sendRedirect("/login");
        }
    }
    /**
     * A method that is executed when the post request is sent to the AddReviewServlet
     * @param request httpservletrequest
     * @param response httpsservletrespons
     * @throws IOException IOException

     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse("Add hotel review", response);

        String hotelid = request.getParameter("hotelid");
        int rating = Integer.parseInt(request.getParameter("rating"));
        String title = request.getParameter("title");
        String review = request.getParameter("review");
        String date = request.getParameter("date");
        String username = request.getParameter("username");
        log.debug("Sending post request to add review...");

        Status status = databaseHandler.addReview(hotelid,rating,title,review,date,username);

        if(status == Status.OK) {
            log.debug("Review successfully added");
            String url = "/myreviews?username="+username+"&success=true";
            response.sendRedirect(response.encodeRedirectURL(url));
        }
        else {
            log.debug("Not able to add review:" + status);
            String url = "/myreviews?username="+username+"&error=true";
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }

    }

    /**
     * Method to print html for a review form
     * @param request httpservletrequest
     * @param out printwriter from getmethod
     * */
    private void printForm(HttpServletRequest request,PrintWriter out) {
        assert out != null;
        String hotelid = request.getParameter("hotelid");
        out.println("<div style=\"background-color: #f1f1f1; padding: 0.01em 16px; margin: 0 auto; width:fit-content; box-shadow: 0 2px 4px 0 rgba(0,0,0,0.16),0 2px 10px 0 rgba(0,0,0,0.12)\">");
        out.println("<h3 style=\"text-align: center;\">Write review for hotel:<br></h3>");
        out.println("<h6 style=\"text-align: center;text-align: center; border-bottom: solid; border-bottom-color: #b7b7b7;");
        out.println("border-width: thin;\">"+databaseHandler.getHotelIdName(hotelid)+"</h6>");
        out.println("<form action=\"/addreview?hotelid="+hotelid+"\" method=\"post\">");
        out.println("<p style=\"text-align: center;\">Title:</p>");
        out.println("<input required autofocus placeholder=\"Title\" type=\"text\" name=\"title\" size=\"50\">");
        out.println("<p style=\"text-align: center;\">Rating:</p>");
        out.println("<input required placeholder=\"Enter your rating from 1-5...\" type=\"text\" name=\"rating\" size=\"50\"pattern=\"[0-5]{1}\" title=\"- Must be a number between 1-5\">");
        out.println("<p style=\"text-align: center;\">Review:</p>");
        out.println("<textarea required placeholder=\"Write your review...\" rows=\"5\" type=\"text\" name=\"review\" maxlength=\"3000\" ");
        out.println("style=\"width:-webkit-fill-available; padding: 6px 12px;border: 1px solid #ccc; border-radius: 4px;\"></textArea>");
        out.println("<input type=\"hidden\" value=\""+hotelid+"\" name=\"hotelid\" />\n");
        out.println("<input type=\"hidden\" value=\""+getDate()+"\" name=\"date\" />\n");
        out.println("<input type=\"hidden\" value=\""+getUsername(request)+"\" name=\"username\" />\n");
        out.println("<p><input type=\"submit\" value=\"Submit\"></p>");
        out.println("</form>");
        out.println("</div>");
    }
}
