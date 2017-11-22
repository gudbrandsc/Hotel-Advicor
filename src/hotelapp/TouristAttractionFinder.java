package hotelapp;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.DatabaseHandler;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.Iterator;

public class TouristAttractionFinder {
    private static final String host = "maps.googleapis.com";
    private static final String path = "/maps/api/place/textsearch/json";
    private static final String GOOGLE_API_KEY = "AIzaSyCBYUGa21uU2cRH8tksayshXs63HS-FpLk";
    private static final DatabaseHandler databaseHandler = DatabaseHandler.getInstance();

    /** Takes a host and a string containing path/resource/query and creates a
     * string of the HTTP GET request
     * @param pathResourceQuery string containing path and request query
     * @return a string with built api request string
     * */
    private static String getRequest(String pathResourceQuery) {
        String request = "GET " + pathResourceQuery + " HTTP/1.1" + System.lineSeparator() // GET
                // request
                + "Host: " + host + System.lineSeparator() // Host header required for HTTP/1.1
                + "Connection: close" + System.lineSeparator() // make sure the server closes the
                // connection after we fetch one page
                + System.lineSeparator();
        return request;
    }

    /**
     * Creates a secure socket to communicate with googleapi's server that
     * provides Places API, sends a GET request (to find attractions close to
     * the hotel within a given radius), and gets a response as a string.
     * Removes headers from the response string and parses the remaining json to
     * get Attractions info. Returns a html formated string for attractions table
     *
     * @param radiusInMiles radius in miles
     * @param hId hotelid
     * @return html with all attractions name and address
     */
    public static String fetchAttractions(int radiusInMiles, String hId) {
        String resp = "";
        PrintWriter out = null;
        BufferedReader in = null;
        SSLSocket socket = null;
        int radiusInMeters = radiusInMiles * 1609;

        try{
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) factory.createSocket(host, 443);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

            String lat = databaseHandler.getHotelLat(hId);
            String lon = databaseHandler.getHotelLon(hId);
            String city = databaseHandler.getHotelCity(hId);

            String query = "query=tourist%20attractions+in+"+city+"&location=" +lat +","+ lon+"&radius="+radiusInMeters+"&key="+GOOGLE_API_KEY ;
            //Replace space from query
            String fixedQuery = query.replaceAll(" ", "%20");

            String header = getRequest(path + "?" + fixedQuery);
            out.println(header); // send a request to the server
            out.flush();

            // input stream for the secure socket.
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // use input stream to read server's response
            String line;

            StringBuffer sb = new StringBuffer();
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            resp = sb.toString();
            String jsonString = resp.substring(resp.indexOf("{"));
            resp = "<table><tr><th>Name</th><th>Address</th></tr>" + jsonReader(jsonString)+"</table>";

            in.close();
            out.close();
            socket.close();

        }catch (IOException e){
            System.out.println(e);
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
        return resp;
    }

    /**Creates a Json object. Iterates trough the json file and adds
     * add information to table
     * @param jsonfile String containing Json
     * @return A html string with all the attractions related to a hotel
     * */
    private static String jsonReader(String jsonfile) {
        JSONParser parser = new JSONParser();
        StringBuilder sb = new StringBuilder();

        try {
            JSONObject json = (JSONObject) parser.parse(jsonfile);
            JSONArray results = (JSONArray) json.get("results");
            Iterator<JSONObject> iterator = results.iterator();
            while (iterator.hasNext()) {

                JSONObject res = iterator.next();
                String name = (String) res.get("name");
                String address= (String) res.get("formatted_address");
                sb.append("<tr>");
                sb.append("<td>"+name+"</td>");
                sb.append("<td>"+address+"</td>");
                sb.append("</tr>");
            }
        } catch (ParseException e) {
            System.out.println("Can not parse a given json file. ");
        }
        return sb.toString();
    }
}
