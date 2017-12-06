package server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Server that handles all servlets.
 */
public class HotelAppServer {
    protected static Logger log = LogManager.getLogger();
    private static int PORT = 8080;

    public static void main(String[] args) {

        Server server = new Server(PORT);



        String dir = System.getProperty("user.dir"); //path
        ServletContextHandler sh = new ServletContextHandler(ServletContextHandler.SESSIONS);
        sh.setResourceBase(dir); // map your path into servlet





        // a handler for serving static html pages from the static/ folder
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setResourceBase("static");


        sh.addServlet(LoginUserServlet.class,     "/login");
        sh.addServlet(LoginRegisterServlet.class, "/register");
        sh.addServlet(WelcomeServlet.class,"/welcome");
        sh.addServlet(HotelsDisplayServlet.class,  "/viewhotels");
        sh.addServlet(HotelPageServlet.class,  "/hotel");
        sh.addServlet(AllReviewsServlet.class,  "/reviews");
        sh.addServlet(UserReviewsServlet.class,  "/myreviews");
        sh.addServlet(AddReviewServlet.class,  "/addreview");
        sh.addServlet(EditReviewServlet.class,  "/editreview");
        sh.addServlet(HotelAttractionsServlet.class,  "/attractions");
        sh.addServlet(LoginRedirectServlet.class, "/*");
        sh.addServlet(SavedHotelsServlet.class,"/savedhotels");
        sh.addServlet(ExpediaLinkHistory.class,"/expedialinks");
        sh.addServlet(TableSearchServlet.class,"/searchtable");





        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, sh });




        // initialize Velocity
        VelocityEngine velocity = new VelocityEngine();
        velocity.init();

        // set velocity as an attribute of the context so that we can access it
        // from servlets
        sh.setContextPath("/");
        sh.setAttribute("templateEngine", velocity);

        server.setHandler(handlers);

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
