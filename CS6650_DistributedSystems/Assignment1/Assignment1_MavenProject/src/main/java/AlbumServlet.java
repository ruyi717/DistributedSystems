

import java.io.FileOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

@WebServlet("/albums")
@MultipartConfig
public class AlbumServlet extends HttpServlet {
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
//       Handle file upload
//      Part filePart = request.getPart("image");
//      String fileName = UUID.randomUUID().toString() + ".jpg"; // Generate a unique file name
//      InputStream inputStream = filePart.getInputStream();
//      // Specify the directory where you want to save the uploaded files
//      OutputStream outputStream = new FileOutputStream("/Users/jouy/Desktop/DistributedSystems/CS6650_DistributedSystems/Assignment1/" + fileName);
//
//      byte[] buffer = new byte[1024];
//      int bytesRead;
//      long imageSizeInBytes = 0; // Initialize the size counter
//
//      while ((bytesRead = inputStream.read(buffer)) != -1) {
//        outputStream.write(buffer, 0, bytesRead);
//        imageSizeInBytes += bytesRead; // Update the image size
//      }
//      outputStream.close();
//      inputStream.close();

//       Generate a new album key (albumID)
      String albumID = "1";

      // Create the JSON response
      String imageSize = "3475";

      String jsonResponse = String.format("{\"albumID\": \"%s\", \"imageSize\": \"%s\"}", albumID, imageSize);

      // Return a JSON response
      response.setContentType("application/json");
      response.getWriter().write(jsonResponse);
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception e) {
      // Handle any exceptions that may occur during file upload or processing
//      response.setContentType("application/json");
//      String errorMessage = "{\"msg\": \"Error uploading file\"}";
//      response.getWriter().write(errorMessage);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }
//  private String formatSize(long sizeInBytes) {
//    if (sizeInBytes < 1024) {
//      return sizeInBytes + " B";
//    } else if (sizeInBytes < 1024 * 1024) {
//      return String.format("%.2f KB", (float) sizeInBytes / 1024);
//    } else {
//      return String.format("%.2f MB", (float) sizeInBytes / (1024 * 1024));
//    }
//  }
}


//import java.io.FileOutputStream;
//import java.io.IOException;
//import com.google.gson.Gson;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.PrintWriter;
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.Part;
//
//
//@WebServlet(name = "AlbumServlet", value = "/albums/*")
//public class AlbumServlet extends HttpServlet {
//
//  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//    Part filePart = req.getPart("file");
//    String fileName = filePart.getSubmittedFileName();
//    InputStream inputStream = filePart.getInputStream();
//    OutputStream outputStream = new FileOutputStream("/Users/jouy/Desktop/DistributedSystems/CS6650_DistributedSystems/Assignment1/" + fileName);
//    byte[] buffer = new byte[1024];
//    int bytesRead;
//    while ((bytesRead = inputStream.read(buffer)) != -1) {
//      outputStream.write(buffer, 0, bytesRead);
//    }
//    outputStream.close();
//    inputStream.close();
//
//    // Send a response to the client
//    res.setContentType("text/html");
//
//
//    Album albumEx = new Album("Taylor Swift", "Evermore", 2020);
////    res.setContentType("application/json");
////    res.setCharacterEncoding("UTF-8");
//    Gson gson =new Gson();
//    PrintWriter out = res.getWriter();
//    String album = gson.toJson(albumEx);
//    out.print(album);
//    out.flush();
//  }
//
//
//  @Override
//  protected void doGet(HttpServletRequest request, HttpServletResponse response)
//      throws ServletException, IOException {
//    processRequest(request, response);
//  }
//
//  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
//      throws ServletException, IOException {
//    response.setContentType("application/json");
//    Gson gson = new Gson();
//
//    try {
//      StringBuilder sb = new StringBuilder();
//      String s;
//      while ((s = request.getReader().readLine()) != null) {
//        sb.append(s);
//      }
//      System.out.println("sb.toString(): " + sb.toString());
//
//      Album album = (Album) gson.fromJson(sb.toString(), Album.class);
//      Integer key = -1;
//      System.out.println("album: " + sb.toString());
//
//
////      if (album.getArtist().equalsIgnoreCase("Taylor Swift")) {
////        key = 1;
////      } else {
////        key = -2;
////      }
////      response.getOutputStream().print(gson.toJson(key));
////      response.getOutputStream().flush();
//    } catch (Exception ex) {
//      ex.printStackTrace();
//      Integer key = -3;
//      response.getOutputStream().print(gson.toJson(key));
//      response.getOutputStream().flush();
//    }
//  }
//}
