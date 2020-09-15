package advisor.Spotify;

import java.util.List;

public interface SpotifyCollection <T> {
    public List<T> getNew();
    public List<T> getFeatured();
    public List<T> getCategories();
    public List<T> getPlaylist(String playlistName);
    public String getOAuthPermissionLink();
    public boolean startAuthService();
    public void stopAuthService();
}
