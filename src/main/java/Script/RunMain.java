package Script;

import ConstrainSkyline.Approximate.mixed.ApproxMixed;
import ConstrainSkyline.Approximate.range.ApproxRange;
import ConstrainSkyline.ExactMethods.ExactBaseline;
import ConstrainSkyline.ExactMethods.ExactImproved;
import ConstrainSkyline.Index.Index;
import ConstrainSkyline.utilities.Result;
import RstarTree.Data;
import neo4jTools.CreateDB;
import neo4jTools.generateGraph;
import org.apache.commons.cli.*;
import tools.SyntheticData;
import tools.configuration.constant;

import java.util.ArrayList;

import static tools.statistic.goodnessAnalyze;

public class RunMain {
    private String method;

    private int graph_size;
    private int graph_degree;
    private int graph_dimension;
    private int hotel_number;
    private int hotel_dimension;
    private double range;
    private int upper;
    private double distance_threshold;
    private int index_threshold;
    private boolean verbose;
    private int query_id;
    private String city;

    public static void main(String[] args) {
        RunMain r = new RunMain();
        if (r.readParameters(args)) {
            r.execute();
        }
    }

    private void execute() {
        ArrayList<Result> exact_solutions;
        Data queryD;

        switch (this.method) {
            case "GenerateSynethicRoadNetwork":
                generateGraph g = new generateGraph(this.graph_size, this.graph_degree, this.graph_dimension);
                g.generateG(true);
                break;
            case "CreateRoadNetworkDB":
                if (this.city.equals("")) {
                    CreateDB db = new CreateDB(this.graph_size, this.graph_degree, this.graph_dimension);
                    db.createDatabase();
                } else {
                    CreateDB db = new CreateDB(this.city);
                    db.createDatabaseForCity();
                }
                break;
            case "GenerateSynethicPOIsData":
                if (this.city.equals("")) {
                    SyntheticData syn_creator = new SyntheticData(this.hotel_number, this.hotel_dimension, this.graph_size, this.graph_degree, this.graph_dimension, this.range, this.upper);
                    syn_creator.createStaticNodes_betaDistribution();
                } else {
                    SyntheticData syn_creator = new SyntheticData(this.city, this.hotel_dimension);
                    syn_creator.createTreeForCity();
                }
                break;
            case "IndexBuilding":
                if (this.city.equals("")) {
                    Index index = new Index(this.graph_size, this.graph_degree, this.graph_dimension, this.range, this.hotel_number, this.hotel_dimension, index_threshold);
                    index.buildIndex(true);
                } else {
                    Index index = new Index(this.city, this.hotel_dimension, this.index_threshold);
                    index.buildIndex(true);

                }
                break;
            case "ExactBaseline":
                queryD = getQueryDByID(this.query_id);
                ExactBaseline exact_baseline;
                if (city.equals("")) {
                    exact_baseline = new ExactBaseline(this.graph_size, this.graph_degree, this.graph_dimension, this.range, this.hotel_number, this.hotel_dimension);
                } else {
                    exact_baseline = new ExactBaseline(this.city, this.hotel_dimension);
                }
                exact_baseline.Query(queryD);
                break;
            case "ExactImproved":
                queryD = getQueryDByID(this.query_id);
                ExactImproved exact_improved;
                if (city.equals("")) {
                    exact_improved = new ExactImproved(this.graph_size, this.graph_degree, this.graph_dimension, this.range, this.hotel_number, this.hotel_dimension);
                } else {
                    exact_improved = new ExactImproved(this.city, this.hotel_dimension);
                }
                exact_improved.Query(queryD);
                break;
            case "ApproxRange":
                if (city.equals("")) {
                    queryD = getQueryDByID(this.query_id);
                    ApproxRange apprx_range = new ApproxRange(this.graph_size, this.graph_degree, this.graph_dimension, this.range, this.hotel_number, this.hotel_dimension, this.distance_threshold);
                    ArrayList<Result> apprx_range_results = apprx_range.Query(queryD);
                    if (verbose) {
                        exact_improved = new ExactImproved(this.graph_size, this.graph_degree, this.graph_dimension, this.range, this.hotel_number, this.hotel_dimension);
                        exact_solutions = exact_improved.Query(queryD);
                        System.out.println("GOODNESS SCORE: " + goodnessAnalyze(exact_solutions, apprx_range_results, "cos"));
                    }
                } else {
                    queryD = getQueryDByID(this.query_id, city);
                    ApproxRange apprx_range = new ApproxRange(this.city, this.hotel_dimension, this.distance_threshold);
                    ArrayList<Result> apprx_range_results = apprx_range.Query(queryD);
                    if (verbose) {
                        exact_improved = new ExactImproved(this.city, this.hotel_dimension);
                        exact_solutions = exact_improved.Query(queryD);
                        System.out.println("GOODNESS SCORE: " + goodnessAnalyze(exact_solutions, apprx_range_results, "cos"));
                    }
                }
                break;
            case "ApproxMixed":
                if (this.city.equals("")) {
                    queryD = getQueryDByID(this.query_id);
                    ApproxMixed apprx_mixed = new ApproxMixed(this.graph_size, this.graph_degree, this.graph_dimension, this.range, this.hotel_number, this.hotel_dimension, this.distance_threshold);
                    ArrayList<Result> apprx_mixed_results = apprx_mixed.Query(queryD);
                    if (verbose) {
                        exact_improved = new ExactImproved(this.graph_size, this.graph_degree, this.graph_dimension, this.range, this.hotel_number, this.hotel_dimension);
                        exact_solutions = exact_improved.Query(queryD);
                        System.out.println("GOODNESS SCORE: " + goodnessAnalyze(exact_solutions, apprx_mixed_results, "cos"));
                    }
                } else {
                    queryD = getQueryDByID(this.query_id, city);
                    ApproxMixed apprx_mixed = new ApproxMixed(this.city, this.hotel_dimension, this.distance_threshold);
                    ArrayList<Result> apprx_mixed_results = apprx_mixed.Query(queryD);
                    if (verbose) {
                        exact_improved = new ExactImproved(this.city, this.hotel_dimension);
                        exact_solutions = exact_improved.Query(queryD);
                        System.out.println("GOODNESS SCORE: " + goodnessAnalyze(exact_solutions, apprx_mixed_results, "cos"));
                    }
                }
                break;
        }
    }


    public Data getQueryDByID(int obj_id) {
        int place_id;
        if (obj_id == -1) {
            place_id = constant.getRandomNumberInRange_int(0, this.hotel_number - 1);
        } else {
            place_id = obj_id;
        }
        String poi_path = constant.data_path + "/" + this.graph_size + "_" + this.graph_degree + "_" + this.graph_dimension + "/" + this.graph_size + "_" + this.graph_degree + "_" + this.graph_dimension + "_" + this.range + "_" + this.hotel_number + ".txt";
        Data queryD = constant.getDataById(place_id, this.hotel_dimension, poi_path);
        return queryD;
    }

    public Data getQueryDByID(int obj_id, String city) {
        String poi_path = constant.data_path + "/" + city + "/staticNode_real_" + this.city + ".txt";
        hotel_number = constant.getNumberOfLines(poi_path);
        int place_id;
        if (obj_id == -1) {
            place_id = constant.getRandomNumberInRange_int(0, hotel_number - 1);
        } else {
            place_id = obj_id;
        }
        Data queryD = constant.getDataById(place_id, this.hotel_dimension, poi_path);
        return queryD;
    }


    private boolean readParameters(String[] args) {
        Options options = new Options();
        try {

            /************* execution related parameters ******************/
            options.addOption("m", "method", true, "method to execute, the default value is 'exact_improved'.");
            options.addOption("i", "index", true, "index enable/disable, the default value is 'false'.");
            options.addOption("e", "measure", true, "the measure is used to calculate the distance between two points, the default value is 'euclidean'.");
            options.addOption("v", "verbose", true, "calculate the goodness score while executing the approximate methods, the default value is 'false'.");


            /************ Query related Parameters **************/
            options.addOption("gs", "grahpsize", true, "number of nodes in the graph, the default value is '1000'.");
            options.addOption("gd", "grahpdegree", true, "degree of the graph, the default value is '4'.");
            options.addOption("gm", "grahpdimension", true, "dimension of the graph, the default value is '3'.");
            options.addOption("hn", "poinumber", true, "number of the poi objects, the default value is '200'.");
            options.addOption("hd", "poidimension", true, "dimension of the poi objects, the default value is '3'.");
            options.addOption("r", "range", true, "the range parameter to generate the synthetic data, the default value is '20'.");
            options.addOption("u", "poinumber", true, "the maximum number of POI objects that are within given range, the default value is '60'.");
            options.addOption("d", "distance_threshold", true, "distance threshold that is used in the approximation methods, the default value is '30'.");
            options.addOption("id", "index_threshold", true, "the distance range that is used to build the index, the default value is '-1'.");
            options.addOption("q", "query", true, "query by a given object ID or a random generated POI object ID), the default value is '-1'.");
            options.addOption("c", "city", true, "the city name (NY, LA and SF) of the real-world dataset. ");

            options.addOption("h", "help", false, "print the help of this command");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = null;
            cmd = parser.parse(options, args);


            String gs_str = cmd.getOptionValue("gs");
            String gd_str = cmd.getOptionValue("gd");
            String gm_str = cmd.getOptionValue("gm");
            String hn_str = cmd.getOptionValue("hn");
            String hd_str = cmd.getOptionValue("hd");
            String r_str = cmd.getOptionValue("r");
            String u_str = cmd.getOptionValue("u");
            String d_str = cmd.getOptionValue("d");
            String id_str = cmd.getOptionValue("id");
            String q_str = cmd.getOptionValue("q");
            String m_str = cmd.getOptionValue("m");
            String i_str = cmd.getOptionValue("i");
            String e_str = cmd.getOptionValue("e");
            String v_str = cmd.getOptionValue("v");
            String c_str = cmd.getOptionValue("c");


            if (cmd.hasOption("h")) {
                return printHelper(options);
            } else {
                if (m_str == null) {
                    this.method = "exact_improved";
                } else if (m_str.equals("GenerateSynethicPOIsData") || m_str.equals("CreateRoadNetworkDB")
                        || m_str.equals("GenerateSynethicRoadNetwork") || m_str.equals("ExactBaseline") || m_str.equals("IndexBuilding") ||
                        m_str.equals("ExactImproved") || m_str.equals("ApproxRange") ||
                        m_str.equals("ApproxMixed")) {
                    this.method = m_str;
                } else {
                    return printHelper(options);
                }


                if (i_str == null) {
                    constant.index_enabled = false;
                } else {
                    constant.index_enabled = Boolean.parseBoolean(i_str);
                }

                if (e_str == null) {
                    constant.distance_calculation_type = "euclidean";
                } else if (e_str.equals("euclidean") || e_str.equals("actual")) {
                    constant.distance_calculation_type = e_str;
                } else {
                    return printHelper(options);
                }

                if (v_str == null) {
                    this.verbose = false;
                } else {
                    this.verbose = Boolean.parseBoolean(v_str);
                }

                if (gs_str == null) {
                    this.graph_size = 1000;
                } else {
                    this.graph_size = Integer.parseInt(gs_str);
                }


                if (gd_str == null) {
                    this.graph_degree = 4;
                } else {
                    this.graph_degree = Integer.parseInt(gd_str);
                }

                if (gm_str == null) {
                    this.graph_dimension = 3;
                } else {
                    this.graph_dimension = Integer.parseInt(gm_str);
                }

                if (hn_str == null) {
                    this.hotel_number = 200;
                } else {
                    this.hotel_number = Integer.parseInt(hn_str);
                }

                if (hd_str == null) {
                    this.hotel_dimension = 3;
                } else {
                    this.hotel_dimension = Integer.parseInt(hd_str);
                }

                if (r_str == null) {
                    this.range = 20;
                } else {
                    this.range = Double.parseDouble(r_str);
                }

                if (u_str == null) {
                    this.upper = 60;
                } else {
                    this.upper = Integer.parseInt(u_str);
                }

                if (d_str == null) {
                    this.distance_threshold = 30;
                } else {
                    this.distance_threshold = Double.parseDouble(d_str);
                }

                if (id_str == null) {
                    this.index_threshold = -1;
                } else {
                    this.index_threshold = Integer.parseInt(id_str);
                }

                if (q_str == null || q_str.equals("r")) {
                    this.query_id = -1;
                } else {
                    this.query_id = Integer.parseInt(q_str);
                    if (this.query_id > this.hotel_number || this.query_id < 0) {
                        System.out.println("The query ID must be 'r', less than or equals to the number of POI objects, or greater than 0.");
                        return printHelper(options);
                    }
                }

                if (c_str == null) {
                    this.city = "";
                } else {
                    this.city = c_str.toUpperCase();
                    if (!constant.cityList.contains(this.city)) {
                        return printHelper(options);
                    }
                }
            }

        } catch (ParseException | NumberFormatException e) {
            return printHelper(options);
        }

        return true;
    }

    private boolean printHelper(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        String header = "Run the code of the constrain skyline path query :";
        formatter.printHelp("java -jar constrainSkylineQuery.jar", header, options, "", false);
        return false;
    }

    private void runningExample() {
        int numberNodes = 3000, numberofDegree = 4, numberofDimen = 3;
        int numberPOIs = 200;
        int poi_dimension = 3;
        double range = 20;
        int upper = 60;

//        /*** Generate the synthetic road network/graph  **********/
//        generateGraph g = new generateGraph(numberNodes, numberofDegree, numberofDimen);
//        g.generateG(true);
//        System.out.println("=========================================================");
//
//        /*** Save the graph to neo4j DB  **********/
//        CreateDB db = new CreateDB(numberNodes, numberofDegree, numberofDimen);
//        db.createDatabase();
//        System.out.println("=========================================================");
//
//        /*** Generate of the synthetic POIs *******/
//        SyntheticData syn_creator = new SyntheticData(numberPOIs, poi_dimension, numberNodes, numberofDegree, numberofDimen, range, upper);
//        syn_creator.createStaticNodes_betaDistribution();
//        System.out.println("=========================================================");
//
//        /**** generate query poi object ***/
//        int random_place_id = constant.getRandomNumberInRange_int(0, numberPOIs - 1);
//        String poi_path = constant.data_path + "/" + numberNodes + "_" + numberofDegree + "_" + numberofDimen + "/" + numberNodes + "_" + numberofDegree + "_" + numberofDimen + "_" + range + "_" + numberPOIs + ".txt";
//        Data queryD = constant.getDataById(random_place_id, poi_dimension, poi_path);
//        System.out.println("=========================================================");
//
//        /*** exact improved method ***/
//        ExactImproved exact_improve = new ExactImproved(numberNodes, numberofDegree, numberofDimen, range, numberPOIs, poi_dimension);
//        ArrayList<Result> exact_solutions = exact_improve.Query(queryD);
//        System.out.println("=========================================================");

        /*** exact improved method ***/
//        ExactBaseline exact_baseline = new ExactBaseline(numberNodes, numberofDegree, numberofDimen, range, numberPOIs, poi_dimension);
//        exact_baseline.Query(queryD);
//        System.out.println("=========================================================");

//        Index index = new Index(numberNodes, numberofDegree, numberofDimen, range, numberPOIs, poi_dimension, -1);
//        index.buildIndex(true);
//        constant.index_enabled=true;
//        exact_improve = new ExactImproved(numberNodes, numberofDegree, numberofDimen, range, numberPOIs, poi_dimension);
//        exact_improve.Query(queryD);
//        System.out.println("=========================================================");


//        constant.index_enabled = false;
////        constant.distance_calculation_type = "actual";
//        double distance_threshold = 30;
//        Index index = new Index(numberNodes, numberofDegree, numberofDimen, range, numberPOIs, poi_dimension, distance_threshold);
////        index.buildIndex(true);
//        ApproxRange apprx_range = new ApproxRange(numberNodes, numberofDegree, numberofDimen, range, numberPOIs, poi_dimension, distance_threshold);
//        apprx_range.Query(queryD);
//        System.out.println("=========================================================");
//        constant.index_enabled = true;
//        apprx_range = new ApproxRange(numberNodes, numberofDegree, numberofDimen, range, numberPOIs, poi_dimension, distance_threshold);
//        ArrayList<Result> apprx_range_results = apprx_range.Query(queryD);
//        System.out.println("=========================================================");
//        constant.index_enabled = false;
//        ApproxMixed apprx_mixed = new ApproxMixed(numberNodes, numberofDegree, numberofDimen, range, numberPOIs, poi_dimension, distance_threshold);
//        apprx_mixed.Query(queryD);
//        System.out.println("=========================================================");
//        constant.index_enabled = true;
//        apprx_mixed = new ApproxMixed(numberNodes, numberofDegree, numberofDimen, range, numberPOIs, poi_dimension, distance_threshold);
//        ArrayList<Result> apprx_mixed_results = apprx_mixed.Query(queryD);
//        System.out.println("=========================================================");
//        System.out.println(goodnessAnalyze(exact_solutions, apprx_mixed_results, "cos"));
//        System.out.println(goodnessAnalyze(exact_solutions, apprx_range_results, "cos"));
    }


}
