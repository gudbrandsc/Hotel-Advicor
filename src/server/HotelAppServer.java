package server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * Server that handles all servlets.
 */
public class HotelAppServer {
    protected static Logger log = LogManager.getLogger();
    private static int PORT = 8080;

    public static void main(String[] args) {

        Server server = new Server(PORT);
        ServletContextHandler context = new ServletContextHandler();

        context.addServlet(LoginUserServlet.class,     "/login");
        context.addServlet(LoginRegisterServlet.class, "/register");
        context.addServlet(HotelsDisplayServlet.class,  "/viewhotels");
        context.addServlet(HotelPageServlet.class,  "/hotel");
        context.addServlet(AllReviewsServlet.class,  "/reviews");
        context.addServlet(UserReviewsServlet.class,  "/myreviews");
        context.addServlet(AddReviewServlet.class,  "/addreview");
        context.addServlet(EditReviewServlet.class,  "/editreview");
        context.addServlet(HotelAttractionsServlet.class,  "/attractions");
        context.addServlet(LoginRedirectServlet.class, "/*");

        // initialize Velocity
        VelocityEngine velocity = new VelocityEngine();
        velocity.init();

        // set velocity as an attribute of the context so that we can access it
        // from servlets
        context.setContextPath("/");
        context.setAttribute("templateEngine", velocity);
        server.setHandler(context);

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
//TODO database table with hotelid and links to all photos fro expedia
