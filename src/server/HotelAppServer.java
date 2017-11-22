package server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * Server that handles all servlets.
 */
public class HotelAppServer {
    protected static Logger log = LogManager.getLogger();
    private static int PORT = 5050;

    public static void main(String[] args) {

        Server server = new Server(PORT);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(LoginUserServlet.class,     "/login");
        handler.addServletWithMapping(LoginRegisterServlet.class, "/register");
        handler.addServletWithMapping(HotelsDisplayServlet.class,  "/viewhotels");
        handler.addServletWithMapping(AllReviewsServlet.class,  "/allreviews");
        handler.addServletWithMapping(UserReviewsServlet.class,  "/myreviews");
        handler.addServletWithMapping(AddReviewServlet.class,  "/addreview");
        handler.addServletWithMapping(EditReviewServlet.class,  "/editreview");
        handler.addServletWithMapping(HotelAttractionsServlet.class,  "/attractions");
        handler.addServletWithMapping(LoginRedirectServlet.class, "/*");


        log.info("Starting server on port " + PORT + "...");

        try {
            server.start();
            server.join();

            log.info("Exiting...");
        }
        catch (Exception ex) {
            log.fatal("Interrupted while running server.", ex);
            System.exit(-1);
        }
    }
}
