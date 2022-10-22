
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * The MovieAnalyzer class has one constructor that reads a dataset file from a given path.
 * This class also has 6 other methods that perform data analyses.
 * You'll implement these methods in the MovieAnalyzer class.
 * Method details are described below.
*/

public class MovieAnalyzer {
    static class MovieBean {
        String seriesTitle;
        int releasedYear;
        String certificate;
        int runtime; // "120 min"
        String[] genre; // type of movie
        float rateInIMDB;
        String overview;
        int metaScore;
        String director;
        String[] stars;
        long noOfVotes;
        long gross;
    }

    List<MovieBean> list;
    Supplier<Stream<MovieBean>> streamSupplier;

    private void buildMovieBean(String[] temp, MovieBean bean) {
        bean.seriesTitle = temp[1].charAt(0) == '\"' && temp[1].charAt(temp[1].length() - 1) == '\"'
                ? temp[1].substring(1, temp[1].length() - 1) : temp[1];
        bean.releasedYear = setYear(temp);
        bean.certificate = temp[3].replace("\"", "");
        bean.runtime = Integer.parseInt(temp[4].split(" ")[0]);
        bean.genre = temp[5].replace("\"", "").replace(" ", "").split(",");
        bean.rateInIMDB = Float.parseFloat(temp[6]);
        bean.overview = temp[7].charAt(0) == '\"' && temp[7].charAt(temp[7].length() - 1) == '\"'
                ? temp[7].substring(1, temp[7].length() - 1) : temp[7];
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
            return -1;
        }
    }

    /**The constructor of MovieAnalyzer takes the path of
     * the dataset file and reads the data.
     *
     * @param datasetPath The constructor of MovieAnalyzer takes
     *                    the path of the dataset file and reads the data.
     */
    public MovieAnalyzer(String datasetPath) {
        list = new ArrayList<>();
        streamSupplier = () -> list.stream();
        File dataset = new File(datasetPath);
        Supplier<MovieBean> movieBeanSupplier = MovieBean::new;
        String inLine;
        try {
            BufferedReader reader =
                    new BufferedReader(
                            new FileReader(
                                    dataset, StandardCharsets.UTF_8));
            inLine = reader.readLine();
            while ((inLine = reader.readLine()) != null) {
                String[] temp = inLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                MovieBean bean = movieBeanSupplier.get();
                buildMovieBean(temp, bean);
                list.add(bean);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The map should be sorted by descending order of year (i.e., from the latest to the earliest).
     *
     * @return This method returns a map,
     *          where the key is the year while the value
     *          is the number of movies released in that year.
     */
    public Map<Integer, Integer> getMovieCountByYear() {
        Stream<MovieBean> stream = streamSupplier.get();
        Supplier<Map<Integer, Integer>> mapFactory =
                () -> new TreeMap<>((o1, o2) -> Integer.compare(o2, o1));
        return stream.collect(Collectors.groupingBy(
                t -> t.releasedYear, mapFactory, Collectors.summingInt(x -> 1)));
    }


    /**
     * The map should be sorted by descending order of count
     * (i.e., from the most frequent genre to the least
     * frequent genre). If two genres have the same count,
     * then they should be sorted by the alphabetical order of
     * the genre names.
     *
     * @return returns a map,where the key is the genre while
     *          the value is the number of movies in that genre.
     */
    public Map<String, Integer> getMovieCountByGenre() {
        Stream<MovieBean> stream = streamSupplier.get();
        Map<String, Integer> noOrderedMap =
                stream.map(bean -> Arrays.stream(bean.genre)).reduce(Stream::concat).get()
                        .collect(Collectors.groupingBy(a -> a, Collectors.summingInt(x -> 1)));
        return noOrderedMap.entrySet().stream()
                .sorted((o1, o2) -> {
                    int cmp = o2.getValue().compareTo(o1.getValue());
                    return cmp != 0 ? cmp : o1.getKey().compareTo(o2.getKey());
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
    }

    /**
     * If two people are the stars for the same movie, then the
     * number of movies that they co-starred increases by 1.
     *
     * @return where the key is a list of names of the stars while the
     *      value is the number of movies that they have co-starred in
     */
    public Map<List<String>, Integer> getCoStarCount() {
        List<List<String>> cpsList = new ArrayList<>();
        for (MovieBean bean : list) {
            for (int i = 0; i < 4; i++) {
                for (int j = i + 1; j < 4; j++) {
                    List<String> cpList = new ArrayList<>();
                    cpList.add(bean.stars[i]);
                    cpList.add(bean.stars[j]);
                    Collections.sort(cpList);
                    cpsList.add(cpList);
                }
            }
        }
        return cpsList.stream().collect(
                Collectors.groupingBy(e -> e, Collectors.summingInt(x -> 1)));
    }

    /**
     * Note that the results should be a list of movie titles.
     * If two movies have the same runtime or overview length,
     * then they should be sorted by alphabetical order of their titles.
     *
     * @param top_k  the top K movies
     * @param by  given criterion
     * @return list
     */
    public List<String> getTopMovies(int top_k, String by) {
        Stream<MovieBean> stream = streamSupplier.get();
        List<String> list = stream.sorted(by.equals("runtime")
                        ? (o1, o2) -> {
            if (o2.runtime == o1.runtime) {
                return o1.seriesTitle.compareTo(o2.seriesTitle);
            } else {
                return o2.runtime - o1.runtime;
            }
        } :
                        (o1, o2) -> {
            if (o2.overview.length() == o1.overview.length()) {
                return o1.seriesTitle.compareTo(o2.seriesTitle);
            } else {
                return o2.overview.length() - o1.overview.length();
            }
        })
                .limit(top_k).map(bean -> bean.seriesTitle).collect(Collectors.toList());
        return list;
    }

    /**
     * Note that the results should be a list of star names.
     * If two stars have the same average rating or gross, then
     * they should be sorted by the alphabetical order of their names.
     *
     * @param top_k top K stars
     * @param by given criterion
     * @return list
     */
    public List<String> getTopStars(int top_k, String by) {
        Stream<MovieBean> stream = streamSupplier.get();
        List<String> list = stream.flatMap(x -> {
            Map<String, Double> map = new LinkedHashMap<>();
            for (String star : x.stars) {
                map.put(star, by.equals("rating") ? (double) x.rateInIMDB : x.gross);
            }
            return map.entrySet().stream();
        }).filter(e -> e.getValue() >= 0)
                .collect(
                Collectors.groupingBy(
                        Map.Entry::getKey, Collectors.averagingDouble(Map.Entry::getValue)))
                .entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    int cmp = o2.getValue().compareTo(o1.getValue());
                    return cmp != 0 ? cmp : o1.getKey().compareTo(o2.getKey());
                })
                .limit(top_k)
                .map(Map.Entry::getKey).collect(Collectors.toList());
        return list;
    }

    /**
     * Note that the results should be a list of movie titles that meet
     * the given criteria, and sorted by alphabetical order of the titles.
     *
     * @param genre genre of the movie
     * @param min_rating the rating of the movie should >= min_rating
     * @param max_runtime the runtime (min) of the movie should <= max_runtime
     * @return list
     */
    public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
        Stream<MovieBean> stream = streamSupplier.get();
        return stream.filter(x -> x.runtime <= max_runtime)
                .filter(x -> x.rateInIMDB >= min_rating)
                .filter(x -> Arrays.asList(x.genre).contains(genre))
                .map(x -> x.seriesTitle)
                .sorted((String::compareTo))
                .collect(Collectors.toList());
    }
}