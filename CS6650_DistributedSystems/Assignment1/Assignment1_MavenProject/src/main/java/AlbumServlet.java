import java.io.IOException;
import com.google.gson.Gson;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name = "AlbumServlet", value = "/albums/*")
public class AlbumServlet extends HttpServlet {

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    Album albumEx = new Album("Taylor Swift", "Evermore", 2020);
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    Gson gson =new Gson();
    PrintWriter out = res.getWriter();
    String album = gson.toJson(albumEx);
    out.print(album);
    out.flush();
  }


  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();

    try {
      StringBuilder sb = new StringBuilder();
      String s;
      while ((s = request.getReader().readLine()) != null) {
        sb.append(s);
      }

      Album album = (Album) gson.fromJson(sb.toString(), Album.class);
      Integer key = -1;
      System.out.println("album: " + sb.toString());


      if (album.getArtist().equalsIgnoreCase("Taylor Swift")) {
        key = 1;
      } else {
        key = -2;
      }
      response.getOutputStream().print(gson.toJson(key));
      response.getOutputStream().flush();
    } catch (Exception ex) {
      ex.printStackTrace();
      Integer key = -3;
      response.getOutputStream().print(gson.toJson(key));
      response.getOutputStream().flush();
    }
  }
}
