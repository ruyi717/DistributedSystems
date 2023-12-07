package albumstore;

import albumstore.entity.ErrorMsg;
import albumstore.exception.BadRequestException;
import albumstore.service.AlbumBodyService;
import albumstore.exception.NotFoundException;
import com.mongodb.*;
import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@MultipartConfig
public class MainServlet extends HttpServlet {
    private MongoClient mongoClient;
    private AlbumBodyService albumBodyService;
    private static final String DB_ADDRESS = "35.160.170.87";
    private static final int DB_PORT = 27017;


    public MainServlet() {
        this.getConnection();
        this.albumBodyService = new AlbumBodyService(this.mongoClient);
//        this.reviewService = new ReviewService(this.mongoClient);
    }


    private void getConnection() {
        ConnectionString connString = new ConnectionString("mongodb://yourNewUser:yourNewPassword@"+ DB_ADDRESS + ":" + DB_PORT);
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyToClusterSettings(builder ->
                builder.hosts(
                    Collections.singletonList(new ServerAddress(connString.getHosts().get(0))))
            )
            .credential(connString.getCredential())
            .build();
        mongoClient = MongoClients.create(settings);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String urlPath = req.getPathInfo();
        int rc = HttpServletResponse.SC_OK;
        String jsonResponse = "";
        Gson gson = new Gson();

        if (urlPath == null || urlPath.isEmpty()) {
            rc = HttpServletResponse.SC_BAD_REQUEST;
            jsonResponse = gson.toJson(new ErrorMsg("invalid request"));
        } else {
            String[] urlParts = urlPath.split("/");

            if (!isGetUrlValid(urlParts)) {
                rc = HttpServletResponse.SC_BAD_REQUEST;
                jsonResponse = gson.toJson(new ErrorMsg("invalid request"));
            } else {
                String id = urlParts[1];
                try {
                    jsonResponse = this.albumBodyService.findAlbumByID(id);
                } catch (NotFoundException e) {
                    rc = HttpServletResponse.SC_NOT_FOUND;
                    jsonResponse = gson.toJson(new ErrorMsg(e.getMessage()));
                }
            }
        }
        this.outputResponse(resp, jsonResponse, rc);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String urlPath = req.getPathInfo();
        int rc = HttpServletResponse.SC_OK;
        String jsonResponse = "";
        Gson gson = new Gson();

        if (urlPath != null) {
            rc = HttpServletResponse.SC_BAD_REQUEST;
            jsonResponse = gson.toJson(new ErrorMsg("urlPath != null, invalid request"));
        } else {
            try {
                byte[] image = req.getPart("image").getInputStream().readAllBytes();
                String profileJson = new BufferedReader(
                        new InputStreamReader(
                                req.getPart("profile").getInputStream(),
                                StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining(""));
                try {
                    jsonResponse = this.albumBodyService.createAlbum(image, profileJson);
//                    this.reviewService.createReview(gson.fromJson(jsonResponse, ImageMetaData.class).getAlbumID());
                } catch (BadRequestException e) {
                    rc = HttpServletResponse.SC_BAD_REQUEST;
                    jsonResponse = gson.toJson(new ErrorMsg("invalid request about create Album"));
                }
            } catch (Exception e) {
                rc = HttpServletResponse.SC_BAD_REQUEST;
                jsonResponse = gson.toJson(new ErrorMsg("invalid request: " + e));
            }
        }
        this.outputResponse(resp, jsonResponse, rc);
    }

    private void outputResponse(HttpServletResponse response, String payload, int status) {
        response.setHeader("Content-Type", "application/json");
        try {
            response.setStatus(status);
            if (payload != null) {
                OutputStream outputStream = response.getOutputStream();
                outputStream.write(payload.getBytes());
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isGetUrlValid(String[] urlParts) {
        return urlParts.length == 2;
    }
}
