import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/albums/*")
public class AlbumInfoServlet extends HttpServlet {

  @Override
  protected void doGet(
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    String albumID = request.getPathInfo().substring(1); // Extract albumID from the request path

    // You can return a constant JSON response for the given albumID
    if ("1".equals(albumID)) {
      String jsonResponse = "{\"artist\": \"Sex Pistols\", \"title\": \"Never Mind The Bollocks!\", \"year\": \"1977\"}";
      response.setContentType("application/json");
      response.getWriter().write(jsonResponse);
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      // Handle the case when the requested albumID is not found
      String errorMessage = "{\"msg\": \"Album not found\"}";
      response.setContentType("application/json");
      response.getWriter().write(errorMessage);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
