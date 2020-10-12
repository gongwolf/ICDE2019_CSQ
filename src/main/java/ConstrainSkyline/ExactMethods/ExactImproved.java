package ConstrainSkyline.ExactMethods;

import ConstrainSkyline.Index.Index;
import ConstrainSkyline.utilities.*;
import RstarTree.Data;
import neo4jTools.connector;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import tools.configuration.constant;

import java.util.*;

public class ExactImproved {
    private String city;
    public double nn_dist;
    public ArrayList<path> qqqq = new ArrayList<>();
    public ArrayList<Result> skyPaths = new ArrayList<>();
    public GraphDatabaseService graphdb;
    Random r = new Random(System.nanoTime());
    String treePath;
    String dataPath;
    int graph_size;
    int degree;
    int dimension;
    String graphPath;
    long add_oper = 0;
    long check_add_oper = 0;
    long map_operation = 0;
    long checkEmpty = 0;
    long read_data = 0;
    HashMap<Integer, Double> dominated_checking = new HashMap<>();
    private int hotels_num;
    private double range;
    private HashMap<Long, myNode> tmpStoreNodes = new HashMap();
    private ArrayList<Data> sNodes = new ArrayList<>(); //the POIs that are not dominated by the query object
    private ArrayList<Data> sky_hotel; //skyline pois among the POIs data
    private HashSet<Integer> finalDatas = new HashSet<>(); //number of distinct objects in the final result set
    private long add_counter; // how many times call the addtoResult function
    private long pro_add_result_counter; // how many path + hotel combination of the results are generated
    private long sky_add_result_counter; // how many results are taken the addtoskyline operation
    private Data queryD;
    private long d_list_num = 0;
    private int obj_dimension;

    public ExactImproved(int graph_size, int degree, int dimension, double range, int hotels_num, int obj_dimension) {
        this.range = range;
        this.hotels_num = hotels_num;
        r = new Random(System.nanoTime());
        this.graph_size = graph_size;
        this.degree = degree;
        this.dimension = dimension;
        this.obj_dimension = obj_dimension;
        this.graphPath = constant.db_path + "/" + this.graph_size + "_" + this.degree + "_" + dimension + "/databases/graph.db";
        this.treePath = constant.data_path + "/" + this.graph_size + "_" + this.degree + "_" + dimension + "/" + this.graph_size + "_" + this.degree + "_" + dimension + "_" + range + "_" + hotels_num + ".rtr";
        this.dataPath = constant.data_path + "/" + this.graph_size + "_" + this.degree + "_" + dimension + "/" + this.graph_size + "_" + this.degree + "_" + dimension + "_" + range + "_" + hotels_num + ".txt";

        System.out.println("graph db: " + graphPath);
        System.out.println("Rtree files: " + treePath);
        System.out.println("POI objects: " + dataPath);
    }

    public ExactImproved(String city, int obj_dimension) {
        this.obj_dimension = obj_dimension;
        this.city = city;

        this.graphPath = constant.db_path + "/" + city + "_db" + "/databases/graph.db";
        this.treePath = constant.data_path + "/" + city + "/" + "real_tree_" + this.city + ".rtr";
        this.dataPath = constant.data_path + "/" + city + "/" + "staticNode_real_" + this.city + ".txt";
        System.out.println("graph db: " + graphPath);
        System.out.println("Rtree files: " + treePath);
        System.out.println("POI objects: " + dataPath);

    }

    public ArrayList<Result> Query(Data queryD) {
        this.queryD = queryD;
        StringBuffer sb = new StringBuffer();
        sb.append(queryD.getPlaceId() + "|");

        Skyline sky = new Skyline(treePath);

        //find the skyline hotels of the whole dataset.
        sky.findSkyline();
        this.sky_hotel = new ArrayList<>(sky.sky_hotels); //must be in the result set

        long s_sum = System.currentTimeMillis();
        long index_s = 0;
        int sk_counter = 0; //the number of total candidate hotels of each bus station

        long r1 = System.currentTimeMillis();
        sky.BBS(queryD);         //Find the hotels that aren't dominated by the query point
        long bbs_rt = System.currentTimeMillis() - r1;
        sNodes = sky.skylineStaticNodes;
        sb.append(this.sNodes.size() + " " + this.sky_hotel.size() + "|");

        //Lemma 1: Assumes the POIs that can be reached from the query point, included the skyline POIs
        for (Data d : sNodes) {
            double[] c = new double[constant.path_dimension + this.dimension];
            c[0] = d.distance_q;
            double[] d_attrs = d.getData();
            for (int i = 4; i < c.length; i++) {
                c[i] = d_attrs[i - 4];
            }
            Result r = new Result(queryD, d, c, null);
            addToSkyline(r);
        }


        //find the minimum distance from query point to the skyline hotel that dominate non-skyline hotel cand_d
        for (Data cand_d : sNodes) {
            double h_to_h_dist = Double.MAX_VALUE;
            if (!sky_hotel.contains(cand_d)) {
                for (Data s_h : sky_hotel) {
                    if (checkDominated(s_h.getData(), cand_d.getData())) {
                        double tmep_dist = s_h.distance_q;
                        if (tmep_dist < h_to_h_dist) {
                            h_to_h_dist = tmep_dist;
                        }
                    }
                }
            }
            dominated_checking.put(cand_d.getPlaceId(), h_to_h_dist);
        }

//        System.out.println("==========" + this.skyPaths.size());


        long db_time = System.currentTimeMillis();
        connector.graphDB = null;
        connector n = new connector(graphPath);
        n.startDB();
        this.graphdb = n.getDBObject();

        long counter = 0; // queue operation counter
        long addResult_rt = 0;
        long expasion_rt = 0;


        try (Transaction tx = this.graphdb.beginTx()) {
            db_time = System.currentTimeMillis() - db_time;
            r1 = System.currentTimeMillis();
            Node startNode = nearestNetworkNode(queryD); //find the nearest road network nodes from q
            long nn_rt = System.currentTimeMillis() - r1; // time that is to find the nearest grahp node

            long rt = System.currentTimeMillis();

            myNode s = new myNode(queryD, startNode.getId(), -1);

            myNodePriorityQueue mqueue = new myNodePriorityQueue();
            mqueue.add(s);

            this.tmpStoreNodes.put(s.id, s);

            while (!mqueue.isEmpty()) {

                myNode v = mqueue.pop();
                v.inqueue = false;

                counter++;

                for (int i = 0; i < v.skyPaths.size(); i++) {
                    path p = v.skyPaths.get(i);
                    if (!p.expaned) {
                        p.expaned = true;

                        long ee = System.nanoTime();
                        ArrayList<path> new_paths = p.expand();
                        expasion_rt += (System.nanoTime() - ee);
                        for (path np : new_paths) {
                            if (!np.hasCycle()) {
                                myNode next_n;
                                if (this.tmpStoreNodes.containsKey(np.endNode)) {
                                    next_n = tmpStoreNodes.get(np.endNode);
                                } else {
                                    next_n = new myNode(queryD, np.endNode, -1);
                                    this.tmpStoreNodes.put(next_n.id, next_n);
                                }

                                //Lemma 2
                                if (!(this.tmpStoreNodes.get(np.startNode).distance_q > next_n.distance_q)) {
                                    if (next_n.addToSkyline(np) && !next_n.inqueue) {
                                        mqueue.add(next_n);
                                        next_n.inqueue = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            long exploration_rt = System.currentTimeMillis() - rt; // time that is used to search on the graph
            System.out.println("expansion finished, using " + exploration_rt+" ms");
            long tt_sl = 0;
//            hotels_scope = new HashMap<>();
            Index idx = null;
            if (constant.index_enabled) {
                if (this.city.equals("")) {
                    idx = new Index(this.graph_size, this.degree, this.dimension, this.range, this.hotels_num, this.obj_dimension, -1);
                } else {
                    idx = new Index(this.city, this.obj_dimension, -1);
                }
            }

            for (Map.Entry<Long, myNode> entry : tmpStoreNodes.entrySet()) {
                sk_counter += entry.getValue().skyPaths.size();
                myNode my_n = entry.getValue();

                long t_index_s = System.nanoTime();
                ArrayList<Data> d_list;
                if (constant.index_enabled) {
                    d_list = idx.read_d_list_from_disk(my_n.id);
                } else {
                    d_list = sNodes;
                }
                index_s += (System.nanoTime() - t_index_s);
                for (path p : my_n.skyPaths) {
                    if (!p.rels.isEmpty()) {
                        long ats = System.nanoTime();
                        boolean f = addToSkylineResult(p, d_list);
                        addResult_rt += System.nanoTime() - ats;
                    }
                }


            }

            //time that is used to find the candicated objects, find the nearest objects,
            sb.append(" running time(ms):" + bbs_rt + "," + nn_rt + "," + exploration_rt + "," + (index_s / 1000000) + ",");
            tx.success();
        }

        long shut_db_time = System.currentTimeMillis();
        n.shutdownDB();
        shut_db_time = System.currentTimeMillis() - shut_db_time;

        s_sum = System.currentTimeMillis() - s_sum;
        sb.append((s_sum - db_time - shut_db_time - (index_s / 1000000)) + "| overall:" + (s_sum) + "   ");
        sb.append(addResult_rt / 1000000 + "(" + (this.add_oper / 1000000) + "+" + (this.check_add_oper / 1000000)
                + "+" + (this.map_operation / 1000000) + "+" + (this.checkEmpty / 1000000) + "+" + (this.read_data / 1000000) + "),");
        sb.append(expasion_rt / 1000000 + "|");
        sb.append("result size:" + this.skyPaths.size());

//        sb.append("\nadd_to_Skyline_result " + this.add_counter + "  " + this.pro_add_result_counter + "  " + this.sky_add_result_counter + " ");
//        sb.append((double) this.sky_add_result_counter / this.pro_add_result_counter);

        List<Result> sortedList = new ArrayList(this.skyPaths);
        Collections.sort(sortedList);

        HashSet<Long> final_bus_stops = new HashSet<>();

        for (Result r : sortedList) {
            this.finalDatas.add(r.end.getPlaceId());
            if (r.p != null) {
                for (Long nn : r.p.nodes) {
                    final_bus_stops.add(nn);
                }
            }
        }


        sb.append(" " + finalDatas.size() + "|");

        int visited_bus_stop = this.tmpStoreNodes.size();
        int bus_stop_in_result = final_bus_stops.size();

        sb.append(visited_bus_stop + "," + bus_stop_in_result + "," + (double) bus_stop_in_result / visited_bus_stop + "|" + this.sky_add_result_counter);

        sb.append("," + add_counter + "," + sk_counter + "," + counter);

        System.out.println(sb.toString());

//        System.out.println("====================");
//        for (Map.Entry<Integer, HashSet<Long>> e : hotels_scope.entrySet()) {
//            System.out.println(e.getKey() + "  " + e.getValue().size());
//        }
//        System.out.println("====================");

//        System.out.println(finalDatas.size() + " " + this.skyPaths.size());
//        System.out.println(addResult_rt + "/" + add_counter + "=" + (double) addResult_rt / add_counter / 1000000);
//        System.out.println(sky_add_result_counter + "/" + add_counter + "=" + (double) sky_add_result_counter / add_counter);
        ArrayList<Result> fianl_result_set = new ArrayList<Result>(this.skyPaths);
        return fianl_result_set;
    }

    public boolean addToSkyline(Result r) {
        int i = 0;
//        if (r.end.getPlaceId() == checkedDataId) {
//            System.out.println(r);
//        }
        if (skyPaths.isEmpty()) {
            this.skyPaths.add(r);
        } else {
            boolean can_insert_np = true;
            for (; i < skyPaths.size(); ) {
                if (checkDominated(skyPaths.get(i).costs, r.costs)) {
                    can_insert_np = false;
                    break;
                } else {
                    if (checkDominated(r.costs, skyPaths.get(i).costs)) {
                        this.skyPaths.remove(i);
                    } else {
                        i++;
                    }
                }
            }

            if (can_insert_np) {
                this.skyPaths.add(r);
                return true;
            }
        }

        return false;
    }

    private boolean checkDominated(double[] costs, double[] estimatedCosts) {
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] * (1.0) > estimatedCosts[i]) {
                return false;
            }
        }
        return true;
    }

    public Node nearestNetworkNode(Data queryD) {
        Node nn_node = null;
        double distz = Float.MAX_VALUE;
        try (Transaction tx = this.graphdb.beginTx()) {
            ResourceIterable<Node> iter = this.graphdb.getAllNodes();
            for (Node n : iter) {
                double lat = (double) n.getProperty("lat");
                double log = (double) n.getProperty("log");

                double temp_distz = (Math.pow(lat - queryD.location[0], 2) + Math.pow(log - queryD.location[1], 2));
                if (distz > temp_distz) {
                    nn_node = n;
                    distz = temp_distz;
                    this.nn_dist = distz;
                }
            }
            tx.success();
        }

        this.nn_dist = distz;
        return nn_node;
    }

    /**
     * Given a prefix skyline paths from q to one node v_i and a candidate list of v_i, the list of candidate paths
     * The search space is reduced by applying several lemmas.
     *
     * @param np     the prefix skyline path from q to one graph node
     * @param d_list candidate list of
     * @return a list of cadidate paths
     */
    private boolean addToSkylineResult(path np, ArrayList<Data> d_list) {
        this.add_counter++; // number of time to form the candidate results

        long rr = System.nanoTime();
        myNode my_endNode = this.tmpStoreNodes.get(np.endNode);
        this.map_operation += System.nanoTime() - rr;

        long dsad = System.nanoTime();
        long d1 = 0, d2 = 0;
        boolean flag = false;

        for (Data d : d_list) {
            if (!this.dominated_checking.containsKey(d.getPlaceId()) || d.getPlaceId() == queryD.getPlaceId()) {
                continue;
            }

            this.pro_add_result_counter++;
            long rrr = System.nanoTime();

            if (d.getPlaceId() == queryD.getPlaceId()) {
                continue;
            }

            double[] final_costs = new double[np.costs.length + dimension];
            System.arraycopy(np.costs, 0, final_costs, 0, np.costs.length);
            double end_distance; // distance from v_i to o in d_list
            if (constant.distance_calculation_type.equals("actual")) {
                end_distance = constant.distanceInMeters(my_endNode.locations[0], my_endNode.locations[1], d.location[0], d.location[1]);
            } else {
                end_distance = Math.sqrt(Math.pow(my_endNode.locations[0] - d.location[0], 2) + Math.pow(my_endNode.locations[1] - d.location[1], 2));
            }

            if (constant.index_enabled) {
                if (constant.distance_calculation_type.equals("actual")) {
                    d.distance_q = Math.sqrt(Math.pow(d.location[0] - queryD.location[0], 2) + Math.pow(d.location[1] - queryD.location[1], 2));
                } else {
                    d.distance_q = Math.sqrt(Math.pow(queryD.location[0] - d.location[0], 2) + Math.pow(queryD.location[1] - d.location[1], 2));
                }
            }

            final_costs[0] += end_distance;
            //Lemma3 & Lemma4
            if (final_costs[0] < d.distance_q && final_costs[0] < this.dominated_checking.get(d.getPlaceId())) {
                double[] d_attrs = d.getData();
                for (int i = 4; i < final_costs.length; i++) {
                    final_costs[i] = d_attrs[i - 4];
                }

                Result r = new Result(this.queryD, d, final_costs, np);

                this.check_add_oper += System.nanoTime() - rrr;
                d1 += System.nanoTime() - rrr;
                long rrrr = System.nanoTime();
                this.sky_add_result_counter++;
                boolean t = addToSkyline(r);

                this.add_oper += System.nanoTime() - rrrr;
                d2 += System.nanoTime() - rrrr;

                if (!flag && t) {
                    flag = true;
                }
            }
        }

        this.read_data += (System.nanoTime() - d1 - d2 - dsad);
        return flag;
    }

}
