package tools;

import ConstrainSkyline.utilities.Result;
import javafx.util.Pair;
import neo4jTools.connector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class statistic {

    String graphPath;
    String infoPath;
    String dataPath;

    int graph_size;
    int degree;
    double range;

    connector conn;
    HashMap<Integer, Pair<Double, Double>> buses;
    HashMap<Integer, Pair<Double, Double>> hotels;

    public static double goodnessAnalyze(ArrayList<Result> all, ArrayList<Result> approx, String dist_measure) {

        HashSet<Integer> distinct_all = new HashSet<>();

        for (Result r : all) {
            distinct_all.add(r.end.getPlaceId());
        }

        int n_all = distinct_all.size();


        double[] all_max_array = getBoundsArray(all, "max");
        double[] all_min_array = getBoundsArray(all, "min");
        double[] approx_max_array = getBoundsArray(approx, "max");
        double[] approx_min_array = getBoundsArray(approx, "min");
        double max = Math.sqrt(7);


        //find common objects
        HashSet<Integer> common_set = new HashSet<>();
        for (Result r : all) {
            for (Result r1 : approx) {
                if (r.end.getPlaceId() == r1.end.getPlaceId()) {
                    if (r1.p != null & r.p != null) {
                        common_set.add(r1.end.getPlaceId());
                    }
                }
            }
        }


        ArrayList<Integer> commonList = new ArrayList<>(common_set);

        double sum_up = 0;
        for (int idx : commonList) {
            double min_distance = Double.POSITIVE_INFINITY;
            for (Result r : all) {
                if (r.end.getPlaceId() == idx) {
                    for (Result r1 : approx) {
                        if (r1.end.getPlaceId() == idx) {
                            if (r1.p != null & r.p != null) {
                                double d = 0;

                                switch (dist_measure) {
                                    case "edu":
                                        d = EduclidianDist(r, r1, all_max_array, all_min_array, approx_max_array, approx_min_array);
                                        break;
                                    case "cos":
                                        d = CosineSimilarity(r, r1, all_max_array, all_min_array, approx_max_array, approx_min_array);
                                        break;
                                }

                                if (d < min_distance) {
                                    min_distance = d;
                                }
                            }
                        }
                    }
                }
            }

            switch (dist_measure) {
                case "edu":
                    sum_up += (max - min_distance);
                    break;
                case "cos":
                    sum_up += (1 - min_distance);
                    break;
            }
        }

        return sum_up / n_all;
    }


    private static double[] getBoundsArray(ArrayList<Result> all, String type) {
        int costs_length = 0;

        if (all.isEmpty()) {
            System.out.println("the result is empty");
        } else {
            costs_length = all.get(0).costs.length;
        }

        double result[] = new double[costs_length];
        for (int i = 0; i < result.length; i++) {
            if (type.equals("max")) {
                result[i] = Double.NEGATIVE_INFINITY;
            } else if (type.equals("min")) {
                result[i] = Double.POSITIVE_INFINITY;
            }
        }

        for (Result r : all) {
            for (int i = 0; i < costs_length; i++) {
                if (type.equals("max") && result[i] < r.costs[i]) {
                    result[i] = r.costs[i];
                } else if (type.equals("min") && result[i] > r.costs[i]) {
                    result[i] = r.costs[i];
                }
            }
        }

        return result;
    }


    public static double EduclidianDist(Result r, Result r1, double[] all_max_array, double[] all_min_array, double[] approx_max_array, double[] approx_min_array) {
        int cost_length = r.costs.length;
        double d = 0;
        for (int i = 0; i < cost_length; i++) {
            double i_max = all_max_array[i] > approx_max_array[i] ? all_max_array[i] : approx_max_array[i];
            double i_min = all_min_array[i] < approx_min_array[i] ? all_min_array[i] : approx_min_array[i];
            double v1 = (r.costs[i] - i_min) / (i_max - i_min);
            double v2 = (r1.costs[i] - i_min) / (i_max - i_min);

            if (v2 > 1 || v2 < 0) {
                System.out.println("Normalization error !!!!!!!");
                d = cost_length;
                break;
//                System.exit(0);
            }

            d += Math.pow(v1 - v2, 2);
        }
        d = Math.sqrt(d);
        return d;
    }

    private static double CosineSimilarity(Result r1, Result r, double[] all_max_array, double[] all_min_array, double[] approx_max_array, double[] approx_min_array) {
        int cost_length = r.costs.length;
        double d = 0;
        double d1 = 0;
        double d2 = 0;
        double d3 = 0;
        for (int i = 0; i < cost_length; i++) {
            double i_max = all_max_array[i] > approx_max_array[i] ? all_max_array[i] : approx_max_array[i];
            double i_min = all_min_array[i] < approx_min_array[i] ? all_min_array[i] : approx_min_array[i];
            double v1 = (r.costs[i] - i_min) / (i_max - i_min);
            double v2 = (r1.costs[i] - i_min) / (i_max - i_min);

            if (v2 > 1 || v2 < 0) {
                System.out.println("Normalization error !!!!!!!");
                d = 25 * cost_length;
                break;
//                System.exit(0);
            }

            d1 += v1 * v2;
            d2 += v1 * v1;
            d3 += v2 * v2;
//            d += Math.pow(v1 - v2, 2);
        }
        d = d1 / (Math.sqrt(d2) * Math.sqrt(d3));
        return (1 - d);
    }


    public void shutdown() {
        conn.shutdownDB();
    }
}
