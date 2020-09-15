package advisor.Spotify;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpotifyAuth {
    protected HttpClient client;
    protected HttpServer server;
    protected final Map<String, String> map = new ConcurrentHashMap<>();
    String uri_access = "";
    volatile boolean haveCode = false;


    public SpotifyAuth(String client_id, String client_secret, String spotify_entry_point) {
        try {
            this.server = HttpServer.create();
            this.server.bind(new InetSocketAddress(8080), 1);
            this.server.createContext("/", e -> {
                String query = e.getRequestURI().getQuery() == null ? "" : e.getRequestURI().getQuery();
                getQuery(query);
                String msg = "Got the code. Return back to your program.";
                if (map.containsKey("code")) {
                    this.haveCode = true;
                    e.sendResponseHeaders(200,msg.getBytes().length);
                } else {
                    msg = "Not found authorization code. Try again.";
                    this.haveCode = false;
                    e.sendResponseHeaders(400,msg.getBytes().length);
                }
                e.getResponseBody().write(msg.getBytes());
                e.getResponseBody().close();
            });

            this.server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.map.put("client_id",client_id);
        this.map.put("client_secret",client_secret);
        this.map.put("spotify_entry_point", spotify_entry_point);

        this.client = HttpClient.newBuilder().build();
        this.uri_access = spotify_entry_point + "authorize?client_id=" + client_id +
                "&response_type=code&redirect_uri=http://localhost:8080/";

    }

    public String getOAuthPermissionLink() {
        return this.uri_access;
    }

    public String getAccessToken() {
        while(!this.haveCode);

        String postBody = "grant_type=authorization_code&" +
                "code=" + map.get("code") +"&redirect_uri=http://localhost:8080/&" +
                "client_id=" + map.get("client_id") + "&client_secret=" + map.get("client_secret");

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(map.get("spotify_entry_point") + "api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(postBody))
                .build();
        String ans = "";
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ans = response.body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        this.server.stop(2);
        return ans;
    }

    public void stopService() {
        this.server.stop(2);
    }

    protected void getQuery(String query) {
        String[] queries = query.split("&");
        for (String q: queries) {
            String[] keyValue = q.split("=");
            if (keyValue.length == 2) {
                this.map.put(keyValue[0].trim(),keyValue[1].trim());
            }
        }
    }
}
