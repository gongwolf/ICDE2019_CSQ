package tools.configuration;

import RstarTree.Data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Random;

public class constant {

    public static final int path_dimension = 4; //1(edu_dis)+3(road net work attrs)+3(static node attrs);

    public static HashMap<Long, Long> accessedNodes = new HashMap<>();
    public static HashMap<Long, Long> accessedEdges = new HashMap<>();
    public static String distance_calculation_type = "euclidean";
    public static boolean index_enabled = false;


    static Random r = new Random(System.nanoTime());

    public static final String home_dir = System.getProperty("user.home");
    public static final String data_path = home_dir + "/shared_git/ICDE_ConstrainSkylineQuery/Data";
    public static final String db_path = data_path + "/Neo4jDB_files";
    public static final String index_path = data_path + "/index";

    public static float randomFloatInRange(float min, float max) {
        float random = min + r.nextFloat() * (max - min);
        return random;
    }

    public static double getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextDouble() * (max - min) + min;
    }


    public static int getRandomNumberInRange_int(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static double getGaussian(double mean, double sd) {
        Random r = new Random();
        double value = r.nextGaussian() * sd + mean;

        while (value <= 0) {
            value = r.nextGaussian() * sd + mean;
        }

        return value;
    }

    public static double distanceInMeters(double lat1, double lng1, double lat2, double lng2) {
        long R = 6371000;
        double d;

        double r_lat1 = Math.PI / 180 * lat1;
        double r_lat2 = Math.PI / 180 * lat2;
//        double delta_lat = Math.PI / 180 * (lat2 - lat1);
        double delta_long = Math.PI / 180 * (lng2 - lng1);
//        double a = Math.sin(delta_lat / 2) * Math.sin(delta_lat / 2) + Math.cos(r_lat1) * Math.cos(r_lat2) * Math.sin(delta_long / 2) * Math.sin(delta_long / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        d = R * c;
//        System.out.println(d);
//        double x = Math.PI / 180 * (long2 - long1) * Math.cos(Math.PI / 180 * (lat1 + lat2) / 2);
//        double y = Math.PI / 180 * (lat2 - lat1);
//        d = Math.sqrt(x * x + y * y) * R;
//        System.out.println(d);
        d = Math.acos(Math.sin(r_lat1) * Math.sin(r_lat2) + Math.cos(r_lat1) * Math.cos(r_lat2) * Math.cos(delta_long)) * R;
//        System.out.println(d);
        return d;
    }

    public static Data getDataById(int placeId, int poi_dimension, String pathpois) {
        BufferedReader br = null;
        int linenumber = 0;

        Data queryD = new Data(poi_dimension);


        try {
            br = new BufferedReader(new FileReader(pathpois));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (linenumber == placeId) {
//                    System.out.println(line);
                    String[] infos = line.split(",");
                    Double lat = Double.parseDouble(infos[1]);
                    Double log = Double.parseDouble(infos[2]);


                    Float c1 = Float.parseFloat(infos[3]);
                    Float c2 = Float.parseFloat(infos[4]);
                    Float c3 = Float.parseFloat(infos[5]);


                    queryD.setPlaceId(placeId);
                    queryD.setLocation(new double[]{lat, log});
                    queryD.setData(new float[]{c1, c2, c3});
                    break;
                } else {
                    linenumber++;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Can not open the file, please check it. ");
        }

        return queryD;

    }
}
