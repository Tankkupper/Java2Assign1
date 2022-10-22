import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovieAnalyzer {
    static class MovieBean{
        String seriesTitle;
        int releasedYear;
        String certificate;
        int runtime; // "120 min"
        String[] genre; // type of movie
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
        bean.seriesTitle = temp[1].charAt(0) == '\"' && temp[1].charAt(temp[1].length()-1) == '\"' ? temp[1].substring(1, temp[1].length()-1) :  temp[1];
        bean.releasedYear = setYear(temp);
        bean.certificate = temp[3].replace("\"", "");
        bean.runtime = Integer.parseInt(temp[4].split(" ")[0]);
        bean.genre = temp[5].replace("\"", "").replace(" ", "").split(",");
        bean.rateInIMDB = Double.parseDouble(temp[6]);
        bean.overview = temp[7].charAt(0) == '\"' && temp[7].charAt(temp[7].length()-1) == '\"' ? temp[7].substring(1, temp[7].length()-1) :  temp[7];
        bean.metaScore = setScore(temp);
        bean.director = temp[9].replace("\"", "");
        bean.stars = new String[4];
        bean.stars[0] = temp[10].replace("\"", "");
        bean.stars[1] = temp[11].replace("\"", "");
        bean.stars[2] = temp[12].replace("\"", "");
        bean.stars[3] = temp[13].replace("\"", "");
        bean.noOfVotes = Long.parseLong(temp[14]);
        bean.gross = setGross(temp);
    }

    private static int setYear(String[] temp) {
        try {
            return Integer.parseInt(temp[2]);
        } catch (Exception e) {
            return -1;
        }
    }

    private static int setScore(String[] temp) {
        try {
            return Integer.parseInt(temp[8]);
        } catch (Exception e) {
            return -1;
        }
    }

    private static long setGross(String[] temp) {
        try {
             return Long.parseLong(temp[15].replace("\"", "").replace(",", ""));
        } catch (Exception e) {
            return  -1;
        }
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
//                if (bean.seriesTitle.equals("Indiana Jones and the Last Crusade")) {
//                    System.out.println(bean.overview);
//                    System.out.println(bean.overview.length());
//                }
                list.add(bean);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Integer> getMovieCountByYear() {
        Stream<MovieBean> stream = streamSupplier.get();
        Supplier<Map<Integer, Integer>> mapFactory = () -> new TreeMap<>((o1, o2) -> Integer.compare(o2, o1));
        return stream.collect(Collectors.groupingBy(t -> t.releasedYear, mapFactory,Collectors.summingInt(x -> 1)));
    }

    public Map<String, Integer> getMovieCountByGenre() {
        Stream<MovieBean> stream = streamSupplier.get();
        Map<String, Integer> noOrderedMap =
                stream.map(bean -> Arrays.stream(bean.genre)).reduce(Stream::concat).get().collect(Collectors.groupingBy(a -> a, Collectors.summingInt(x -> 1)));
        return noOrderedMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
    }

    public Map<List<String>, Integer> getCoStarCount() {
        return null;
    }

    public List<String> getTopMovies(int top_k, String by) {
        Stream<MovieBean> stream = streamSupplier.get();
        List<String> list = stream.sorted(by.equals("runtime") ?
                (o1, o2) -> {if (o2.runtime == o1.runtime) return o1.seriesTitle.compareTo(o2.seriesTitle);
                                else return o2.runtime - o1.runtime;} :
                (o1, o2) -> {if (o2.overview.length() == o1.overview.length()) return o1.seriesTitle.compareTo(o2.seriesTitle);
                                else return o2.overview.length() - o1.overview.length();} )
                .limit(top_k).map(bean -> bean.seriesTitle).collect(Collectors.toList());
        return list;
    }

    public List<String> getTopStars(int top_k, String by) {
        return null;
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
        return null;
    }
}