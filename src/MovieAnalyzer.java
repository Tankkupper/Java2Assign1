import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovieAnalyzer {
    static class MovieBean{
        String seriesTitle;
        int releasedYear;
        String certificate;
        int runtime; // "120 min"
        String genre; // type of movie
        double rateInIMDB;
        String overview;
        int metaScore;
        String director;
        String[] stars;
        long noOfVotes;
        long gross;

        @Override
        public String toString() {
            return "MovieBean{" +
                    "seriesTitle='" + seriesTitle + '\'' +
                    ", releasedYear=" + releasedYear +
                    ", certificate='" + certificate + '\'' +
                    ", runtime=" + runtime +
                    ", genre='" + genre + '\'' +
                    ", rateInIMDB=" + rateInIMDB +
                    ", overview='" + overview + '\'' +
                    ", metaScore=" + metaScore +
                    ", director='" + director + '\'' +
                    ", stars=" + Arrays.toString(stars) +
                    ", noOfVotes=" + noOfVotes +
                    ", gross=" + gross +
                    '}';
        }
    }
    List<MovieBean> list;
    Supplier<Stream<MovieBean>> streamSupplier;

    public static void main(String[] args) {
        MovieAnalyzer test = new MovieAnalyzer("resources/imdb_top_500.csv");
    }

    private static void buildMovieBean(String[] temp, MovieBean bean) {
        bean.seriesTitle = temp[1];
        bean.releasedYear = Integer.parseInt(temp[2]);
        bean.certificate = temp[3];
        bean.runtime = Integer.parseInt(temp[4].split(" ")[0]);
        bean.genre = temp[5];
        bean.rateInIMDB = Double.parseDouble(temp[6]);
        bean.overview = temp[7];
        bean.metaScore = Integer.parseInt(temp[8]);
        bean.director = temp[9];
        bean.stars = new String[4];
        bean.stars[0] = temp[10];
        bean.stars[1] = temp[11];
        bean.stars[2] = temp[12];
        bean.stars[3] = temp[13];
        bean.noOfVotes = Long.parseLong(temp[14]);
        bean.gross = Long.parseLong(temp[15].replace("\"", "").replace(",", ""));
    }

    public MovieAnalyzer(String datasetPath) {
        list = new ArrayList<>();
        streamSupplier = () -> list.stream();
        File dataset = new File(datasetPath);
        Supplier<MovieBean> movieBeanSupplier = MovieBean::new;
        String inLine;
        try {
            BufferedReader reader =
                    new BufferedReader
                            (new FileReader
                                    (dataset, StandardCharsets.UTF_8));
            inLine = reader.readLine();
            while((inLine = reader.readLine()) != null){
                String[] temp = inLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                //if (temp.length < 13) continue;
                MovieBean bean = movieBeanSupplier.get();
                buildMovieBean(temp, bean);
                list.add(bean);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Integer> getMovieCountByYear() {
        //System.out.println(list.size());
        Stream<MovieBean> stream = streamSupplier.get();
        return stream.collect(Collectors.groupingBy(t -> t.releasedYear, Collectors.summingInt(x -> 1)));
    }

    public Map<String, Integer> getMovieCountByGenre() {
        return null;
    }

    public Map<List<String>, Integer> getCoStarCount() {
        return null;
    }

    public List<String> getTopMovies(int top_k, String by) {
        return null;
    }

    public List<String> getTopStars(int top_k, String by) {
        return null;
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
        return null;
    }
}