import com.google.gson.Gson;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

public class SkierServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Invalid URL path\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 8) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Invalid URL path format\"}");
            return;
        }

        int resortID, seasonID, dayID, skierID;
        try {
            resortID = Integer.parseInt(pathParts[1]);
            seasonID = Integer.parseInt(pathParts[3]);
            dayID = Integer.parseInt(pathParts[5]);
            skierID = Integer.parseInt(pathParts[7]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Invalid URL path parameters\"}");
            return;
        }

        String jsonBody = request.getReader().lines().collect(Collectors.joining("\n"));


        LiftRide liftRide;
        try {
            liftRide = gson.fromJson(jsonBody.toString(), LiftRide.class);
        } catch (com.google.gson.JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Malformed JSON payload\"}");
            return;
        }


        if (liftRide == null || liftRide.getLiftID() <= 0 || liftRide.getTime() <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Invalid JSON payload\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_CREATED);
        String responseJson = String.format(
                "{\"message\": \"Lift ride recorded\", \"resortID\": %d, \"seasonID\": %d, \"dayID\": %d, \"skierID\": %d, \"liftID\": %d, \"time\": %d}",
                resortID, seasonID, dayID, skierID, liftRide.getLiftID(), liftRide.getTime()
        );
        response.getWriter().write(responseJson);
    }
}

