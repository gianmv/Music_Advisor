package advisor;


import advisor.Spotify.SpotifyAuth;
import advisor.Spotify.SpotifyCollection;
import advisor.Spotify.SpotifyRepository;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean run = true;
        boolean authorized = false;

        String spotify_entry_point = "https://accounts.spotify.com/";
        String apiEndPoint = "https://api.spotify.com/";
        int page = 5;

        for (int i = 0; i < args.length; i++) {
            if ("-access".equals(args[i])) {
                spotify_entry_point = args[i+1] + "/";
            }
            if ("-resource".equals(args[i]))  {
                apiEndPoint = args[i+1] + "/";
            }

            if ("-page".equals(args[i])) {
                page = Integer.valueOf(args[i+1]);
            }
        }

        String client_id = "574055a6878647619dd4278fb9322231";
        String client_secret = "859695bc2a964281886c8b394a3732d5";
        SpotifyAuth spotifyAuth = new SpotifyAuth(client_id,client_secret, spotify_entry_point);
        SpotifyCollection<String> library = new SpotifyRepository(spotifyAuth, apiEndPoint);

        Paginator<String> featuredPag, newPag, categoryPag, playlistPag;

        while (run) {
            System.out.print("> ");
            String command = scanner.nextLine();

            if (command.contains("featured") && authorized) {
                featuredPag = new Paginator<>(page,library.getFeatured());
                featuredPag.start();

            } else if (command.contains("new") && authorized) {
                newPag = new Paginator<>(page,library.getNew());
                newPag.start();

            } else if (command.contains("categories") && authorized) {
                categoryPag = new Paginator<>(page,library.getCategories());
                categoryPag.start();
            } else if (command.contains("playlists") && authorized) {
                String playlistName = command.replace("playlists","").trim();
                if (!playlistName.isBlank()) {
                    playlistPag = new Paginator<>(page,library.getPlaylist(playlistName));
                    playlistPag.start();
                } else {
                    System.out.println("Error in command");
                }

            } else if (command.contains("exit")){
                System.out.println("---GOODBYE!---");
                run = false;
                library.stopAuthService();
            } else if (command.contains("auth")) {
                System.out.println("use this link to request the access code:");
                System.out.println(library.getOAuthPermissionLink());
                System.out.println("waiting for code...");
                authorized = library.startAuthService();
                if (authorized) {
                    System.out.println("---SUCCESS---");
                }
            } else if(!authorized){
                System.out.println("Please, provide access for application.");
            }
        }
    }
}

class Paginator <T> {
    protected List<T> library;
    protected int actualPage;
    protected int pageNumber;
    Map<Integer,List<Integer>> paginator = new HashMap<>();

    Paginator(int itemPerPage, List<T> library) {
        this.library = library;
        this.actualPage = 1;
        int temp = 1;
        for (int i = 0; i < library.size(); i+= itemPerPage) {
            List<Integer> numbers = new ArrayList<>();
            int limit = Math.min(i + itemPerPage, library.size());
            for (int j = i; j < limit; j++) {
                numbers.add(j);
            }
            this.paginator.put(temp++,numbers);
        }
        this.pageNumber = this.paginator.size();
    }

    public void start() {
        for (Integer idx : paginator.get(this.actualPage)) {
            System.out.println(library.get(idx));
        }
        System.out.printf("---PAGE %d OF %d---\n", this.actualPage, this.pageNumber);

        System.out.print("> ");
        Scanner sc = new Scanner(System.in);
        String command = sc.nextLine();
        while (!"exit".equals(command)) {
            if ("prev".equals(command)) {
                showNext(false);
            } else if ("next".equals(command)) {
                showNext(true);
            }
            System.out.print("> ");
            command = sc.nextLine();
        }

    }

    protected void showNext(boolean next) {
        this.actualPage = next ? this.actualPage + 1 : this.actualPage - 1;

        if (this.actualPage >= 1  && this.actualPage  <= this.pageNumber) {
            for (Integer idx : paginator.get(this.actualPage)) {
                System.out.println(library.get(idx));
            }
            System.out.printf("---PAGE %d OF %d---\n", this.actualPage, this.pageNumber);
        } else {
            System.out.println("No more pages.");
            this.actualPage = next ? this.actualPage - 1 : this.actualPage + 1;
        }


    }
}