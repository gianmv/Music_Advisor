package advisor.Spotify;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpotifyRepository implements SpotifyCollection<String>{
    SpotifyAuth auth;
    String apiRepository;
    HttpClient client = HttpClient.newBuilder().build();
    String accessToken;
    Map<String, String> map = new HashMap<>();

    public SpotifyRepository(SpotifyAuth authorization, String apiRepository) {
        this.auth = authorization;
        this.apiRepository = apiRepository;
    }

    @Override
    public List<String> getNew() {
        String endPoint = this.apiRepository + "v1/browse/new-releases";
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + this.accessToken)
                .uri(URI.create(endPoint))
                .GET()
                .build();

        JsonObject ans = new JsonObject();

        try {
            HttpResponse<String> response = this.client.send(request,HttpResponse.BodyHandlers.ofString());
            ans = JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        List<String> list = getNew(ans);

        return list;
    }

    private List<String> getNew(JsonObject ans) {
        JsonArray jsonArray  = ans.getAsJsonObject("albums").getAsJsonArray("items");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            String element = "";
            JsonObject temp = jsonArray.get(i).getAsJsonObject();
            element += temp.get("name").getAsString() + "\n";
            JsonArray artists = temp.getAsJsonArray("artists");
            String arts = "[";
            for (int j = 0; j < artists.size(); j++) {
                arts += artists.get(j).getAsJsonObject().get("name").getAsString();
                arts += j == artists.size() - 1 ? "" : ", ";
            }
            arts += "]\n";

            element += arts;
            element += temp.get("external_urls").getAsJsonObject().get("spotify").getAsString() + "\n";
            list.add(element);
        }
        return list;
    }

    @Override
    public List<String> getFeatured() {
        String endPoint = this.apiRepository + "v1/browse/featured-playlists";
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + this.accessToken)
                .uri(URI.create(endPoint))
                .GET()
                .build();

        JsonObject ans = new JsonObject();

        try {
            HttpResponse<String> response = this.client.send(request,HttpResponse.BodyHandlers.ofString());
            ans = JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        List<String> list = getFeatured(ans);

        return list;
    }

    private List<String> getFeatured(JsonObject ans) {
        JsonArray array = ans.get("playlists").getAsJsonObject().get("items").getAsJsonArray();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            String element = "";
            element += array.get(i).getAsJsonObject().get("name").getAsString() + "\n";
            element += array.get(i).getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString();
            element += "\n";
            list.add(element);
        }
        return list;
    }

    @Override
    public List<String> getCategories() {
        String endPoint = this.apiRepository + "v1/browse/categories";
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + this.accessToken)
                .uri(URI.create(endPoint))
                .GET()
                .build();

        JsonObject ans = new JsonObject();

        try {
            HttpResponse<String> response = this.client.send(request,HttpResponse.BodyHandlers.ofString());
            ans = JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        List<String> list = getCategories(ans);

        return list;
    }

    private List<String> getCategories(JsonObject ans) {
        List<String> list = new ArrayList<>();
        JsonArray array = ans.get("categories").getAsJsonObject().get("items").getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            String element = "";
            String name = array.get(i).getAsJsonObject().get("name").getAsString();
            String id = array.get(i).getAsJsonObject().get("id").getAsString();
            map.put(name,id);
            element += name;
            list.add(element);
        }
        return list;
    }

    @Override
    public List<String> getPlaylist(String categoryName) {
        if (map.containsKey(categoryName)) {
            String endPoint = this.apiRepository + "v1/browse/categories/"+ map.get(categoryName) +"/playlists";
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + this.accessToken)
                    .uri(URI.create(endPoint))
                    .GET()
                    .build();

            JsonObject ans = new JsonObject();

            try {
                HttpResponse<String> response = this.client.send(request,HttpResponse.BodyHandlers.ofString());
                ans = JsonParser.parseString(response.body()).getAsJsonObject();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            List<String> list = getPlaylist(ans);
            return list;
        } else {
            List<String> ans = new ArrayList<>();
            ans.add("Unknown category name.");
            return ans;
        }
    }

    private List<String> getPlaylist(JsonObject ans) {
        List<String> list = new ArrayList<>();
        if(ans.has("error")) {
            list.add(ans.get("error").getAsJsonObject().get("message").getAsString());
        } else {
            JsonArray array = ans.get("playlists").getAsJsonObject().get("items").getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                String message = "";
                message += array.get(i).getAsJsonObject().get("name").getAsString() + "\n";
                message += array.get(i).getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString();
                message += "\n";
                list.add(message);

            }
        }
        return list;
    }

    @Override
    public String getOAuthPermissionLink() {
        return this.auth.getOAuthPermissionLink();
    }

    @Override
    public boolean startAuthService() {
        String accessCode = this.auth.getAccessToken();
        JsonObject json = JsonParser.parseString(accessCode).getAsJsonObject();
        this.accessToken = json.get("access_token").getAsString();
        getCategories();
        return !"".equals(this.accessToken);
    }

    public void stopAuthService() {
        this.auth.stopService();
    }
}
