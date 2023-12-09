package albumstore;

import albumstore.entity.ErrorMsg;
import albumstore.exception.NotFoundException;
import albumstore.service.ReviewService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

@MultipartConfig
public class ReviewServlet extends HttpServlet {
    private MongoClient mongoClient;
    private ReviewService reviewService;
    private static final String DB_ADDRESS = "54.188.34.71";
    private static final int DB_PORT = 27017;
    private final static String QUEUE_NAME = "album_review_queue";
    private ObjectPool<Channel> pool;

    public ReviewServlet() {
        this.getConnection();
        this.reviewService = new ReviewService(this.mongoClient);
    }


    public void init() {
        this.pool = new GenericObjectPool<Channel>(new ConnectionPool());
    }

    private void getConnection() {
        ConnectionString connString = new ConnectionString("mongodb://yourNewUser:yourNewPassword@"+ DB_ADDRESS + ":" + DB_PORT);
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyToClusterSettings(builder ->
                builder.hosts(
                    Collections.singletonList(new ServerAddress(connString.getHosts().get(0))))
            )
//            .credential(connString.getCredential())
            .build();
        this.mongoClient = MongoClients.create(settings);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String urlPath = req.getPathInfo();
        int rc = HttpServletResponse.SC_OK;
        String jsonResponse = "";
        Gson gson = new Gson();

        if (urlPath == null || urlPath.isEmpty()) {
            rc = HttpServletResponse.SC_BAD_REQUEST;
            jsonResponse = gson.toJson(new ErrorMsg("urlPath, invalid request"));
        } else {
            String[] urlParts = urlPath.split("/");

            if (!isGetUrlValid(urlParts)) {
                rc = HttpServletResponse.SC_BAD_REQUEST;
                jsonResponse = gson.toJson(new ErrorMsg("isGetUrlValid, invalid request"));
            } else {
                String id = urlParts[1];
                try {
                    jsonResponse = this.reviewService.findReviewByID(id);
                } catch (NotFoundException e) {
                    rc = HttpServletResponse.SC_NOT_FOUND;
                    jsonResponse = gson.toJson(new ErrorMsg(e.getMessage()));
                }
            }
        }
        this.outputResponse(resp, jsonResponse, rc);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String urlPath = req.getPathInfo();
        int rc = HttpServletResponse.SC_CREATED;
        String jsonResponse = "";
        Gson gson = new Gson();

        if (urlPath == null || urlPath.isEmpty()) {
            rc = HttpServletResponse.SC_BAD_REQUEST;
            jsonResponse = gson.toJson(new ErrorMsg("Invalid inputs"));
        } else {
            String[] urlParts = urlPath.split("/");

            if (!isPostUrlValid(urlParts)) {
                rc = HttpServletResponse.SC_BAD_REQUEST;
                jsonResponse = gson.toJson(new ErrorMsg("Invalid inputs"));
            } else {
                try {
                    boolean like = urlParts[1].equals("like");
                    String id = urlParts[2];

//                    if(like) {
//                        try {
//                            jsonResponse = this.reviewService.addLike(id);
//                        } catch (Exception e) {
//                            rc = HttpServletResponse.SC_NOT_FOUND;
//                            jsonResponse = gson.toJson(new ErrorMsg(e.getMessage()));
//                        }
//                    } else {
//                        try {
//                            jsonResponse = this.reviewService.addDislike(id);
//                        } catch (Exception e) {
//                            rc = HttpServletResponse.SC_NOT_FOUND;
//                            jsonResponse = gson.toJson(new ErrorMsg(e.getMessage()));
//                        }
//                    }

                    JsonObject reviewItem = new JsonObject();
                    reviewItem.addProperty("albumID", id);
                    reviewItem.addProperty("ifLike", like);
                    Channel channel = null;
                    try {
                        channel = pool.borrowObject();
                        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                        channel.basicPublish("", QUEUE_NAME, null, reviewItem.toString().getBytes());
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to borrow from pool" + e.toString());
                    } finally {
                        try {
                            if (channel != null) {
                                System.out.println("Channel return Done");
                                pool.returnObject(channel);
                            }
                        } catch (Exception e) {
                            System.out.println("Error when returning channel");
                        }
                    }
                } catch (Exception ex) {
                    rc = HttpServletResponse.SC_NOT_FOUND;
                }
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
        if (urlParts.length != 2) return false;
        return true;
    }

    private boolean isPostUrlValid(String[] urlParts) {
        if (urlParts.length != 3) return false;
        if (!(urlParts[1].equals("like") || urlParts[1].equals("dislike"))) return false;
        return true;
    }
}
