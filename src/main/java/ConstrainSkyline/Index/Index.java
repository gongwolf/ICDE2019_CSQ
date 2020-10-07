package ConstrainSkyline.Index;

import ConstrainSkyline.utilities.Skyline;
import ConstrainSkyline.utilities.myNode;
import RstarTree.Data;
import neo4jTools.connector;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.Transaction;
import tools.configuration.constant;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class Index {
    //    private final String base = System.getProperty("user.home") + "/shared_git/bConstrainSkyline/data/index";
    private final String base = System.getProperty("user.home") + "/mydata/DemoProject/data/index";
    public String home_folder;
    private String dataPath;
    private String treePath;
    private String graphPath;
    private String source_data_tree;
    private String neo4j_db;
    //    private final int pagesize_data;
    private int pagesize_list;
    private String node_info_path;
    private long num_nodes;
    private double distance_threshold;

    private int obj_dimension = 3;

    public Index(int graph_size, int degree, int dimension, double range, int num_hotels, int obj_dimension, double distance_thresholds) {
        this.obj_dimension = obj_dimension;
        this.distance_threshold = distance_thresholds;
        String graph_folder_str = graph_size + "_" + degree + "_" + dimension;

        if (distance_thresholds != -1) {
            this.home_folder = constant.index_path + "/" + graph_folder_str + "_" + range + "_" + num_hotels + "_" + (int) distance_thresholds;
            this.source_data_tree = constant.data_path + "/" + graph_folder_str + "/" + graph_folder_str + "_" + range + "_" + num_hotels + ".rtr";
            this.neo4j_db = constant.db_path + "/" + graph_folder_str + "/databases/graph.db";
            this.node_info_path = constant.data_path + "/" + graph_folder_str + "/NodeInfo.txt";
        } else {
            this.home_folder = constant.index_path + "/" + graph_folder_str + "_" + range + "_" + num_hotels + "_all";
            this.source_data_tree = constant.data_path + "/" + graph_folder_str + "/" + graph_folder_str + "_" + range + "_" + num_hotels + ".rtr";
            this.neo4j_db = constant.db_path + "/" + graph_folder_str + "/databases/graph.db";
            this.node_info_path = constant.data_path + "/" + graph_folder_str + "/NodeInfo.txt";
        }

        this.num_nodes = getLineNumbers();
        this.pagesize_list = 1024;

    }


    public Index(String city, double distance_threshold) {
//		this.distance_threshold = 0.0105;
        this.distance_threshold = distance_threshold;
        if (distance_threshold != -1) {
            this.home_folder = base + "/" + city + "_index_" + (int) distance_threshold + "/";
        } else {
            this.home_folder = base + "/" + city + "_index_all/";
        }

        this.graphPath = System.getProperty("user.home") + "/neo4j334/testdb_" + city + "_Random/databases/graph.db";
        this.treePath = System.getProperty("user.home") + "/mydata/DemoProject/data/real_tree_" + city + ".rtr";
        this.dataPath = System.getProperty("user.home") + "/mydata/DemoProject/data/staticNode_real_" + city + ".txt";

        this.source_data_tree = this.treePath;
        this.neo4j_db = this.graphPath;
        this.node_info_path = System.getProperty("user.home") + "/mydata/projectData/testGraph_real_50_Random/data/"
                + city + "_NodeInfo.txt";
        this.num_nodes = getLineNumbers();
        this.pagesize_list = 1024;
    }


    public ArrayList<Data> read_d_list_from_disk(long node_id) {

        String header_name = this.home_folder + "/header.idx";
        String list_name = this.home_folder + "/list.idx";
        String Data_file = this.home_folder + "/data.dat";
        ArrayList<Data> d_list = new ArrayList<>();

        try {

            RandomAccessFile header_f = new RandomAccessFile(header_name, "r");
            header_f.seek((node_id * 8));
            int pagenumber = header_f.readInt();
            int d_size = header_f.readInt();
//            System.out.println(node_id + "  " + d_size + "  " + pagenumber);

            RandomAccessFile list_f = new RandomAccessFile(list_name, "r");
            list_f.seek(pagenumber * pagesize_list);


            RandomAccessFile data_f = new RandomAccessFile(Data_file, "r");


            for (int i = 0; i < d_size; i++) {
                int d_id = list_f.readInt();
                Data d = new Data(obj_dimension);
                data_f.seek(d_id * d.get_size());
                byte[] b_d = new byte[d.get_size()];
                data_f.read(b_d);
                d.read_from_buffer(b_d);
                d_list.add(d);
//                System.out.println(d_id);
            }

            data_f.close();
            header_f.close();
            list_f.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return d_list;
    }

    public void test(int number_of_pois) {
        String Data_file = this.home_folder + "/data.dat";
        System.out.println(Data_file);
        try {

            long d_id = getRandomNumberInRange_int(0, number_of_pois - 1);
            RandomAccessFile data_f = new RandomAccessFile(Data_file, "rw");
            Data d = new Data(3);
            data_f.seek(d_id * d.get_size());
            System.out.println(d.get_size());
            byte[] b_d = new byte[d.get_size()];
            data_f.read(b_d);
            d.read_from_buffer(b_d);
            System.out.println(d);
            System.out.println("===============================================");
            String header_name = this.home_folder + "/header.idx";
            String list_name = this.home_folder + "/list.idx";
            RandomAccessFile header_f = new RandomAccessFile(header_name, "rw");
            long node_id = getRandomNumberInRange_int(0, number_of_pois);
            header_f.seek((node_id * 8));
            RandomAccessFile list_f = new RandomAccessFile(list_name, "rw");
//            list_f.seek(node_id * pagesize_list);

            int pagenumber = header_f.readInt();
            int d_size = header_f.readInt();
            System.out.println(node_id + "  " + d_size + "  " + pagenumber);
            for (int i = 0; i < d_size; i++) {
                System.out.println(list_f.readInt());
            }

            data_f.close();
            header_f.close();
            list_f.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * write the all the objects to .dat file
     *
     * @param allNodes the list of POI objects
     */
    private void writeDataToDisk(ArrayList<Data> allNodes) {
        ArrayList<Data> an = new ArrayList<>(allNodes);
        String Data_file = this.home_folder + "/data.dat";
        try {
            RandomAccessFile data_f = new RandomAccessFile(Data_file, "rw");
            data_f.seek(0);

            Collections.sort(an, Comparator.comparingInt(Data::getPlaceId));

            for (Data d : an) {
                byte[] b_d = new byte[d.get_size()];
                d.write_to_buffer(b_d); //write the information of d to buffer b_d
                data_f.write(b_d);
            }
            data_f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void buildIndex(boolean deleteBeforeBuild) {
        if (deleteBeforeBuild) {
            File dataF = new File(home_folder);
            try {
                FileUtils.deleteDirectory(dataF);
                dataF.mkdirs();
                System.out.println(dataF);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Skyline sk = new Skyline(this.source_data_tree);
        sk.allDatas(); //get all data objects from R-tree
        sk.findSkyline(); //get skyline objects among all the data objects
        System.out.println("number of data objects " + sk.allNodes.size());
        System.out.println("number of skyline data objects:" + sk.sky_hotels.size());
        writeDataToDisk(sk.allNodes);

        String header_name = this.home_folder + "/header.idx";
        String list_name = this.home_folder + "/list.idx";

//        System.out.println(this.num_nodes);


        try {
            connector n = new connector(this.neo4j_db);
            n.startDB();
            RandomAccessFile header_f = new RandomAccessFile(header_name, "rw");
            header_f.seek(0);
            RandomAccessFile list_f = new RandomAccessFile(list_name, "rw");
            list_f.seek(0);


            int page_list_number = 0;
            for (int node_id = 0; node_id <= num_nodes; node_id++) {
                if (node_id % 1000 == 0) {
                    System.out.println("========================" + node_id + "=========================");
                }

                try (Transaction tx = connector.graphDB.beginTx()) {
                    myNode node = new myNode(node_id);

                    ArrayList<Data> d_list;
                    if (this.distance_threshold == -1) {
                        d_list = new ArrayList<>(sk.sky_hotels);
                    } else {
                        d_list = new ArrayList<>();
                        for (Data d : sk.sky_hotels) {
                            double d2;
                            if (constant.distance_calculation_type.equals("actual")) {
                                d2 = constant.distanceInMeters(node.locations[0], node.locations[1], d.location[0], d.location[1]);
                            } else {
                                d2 = Math.sqrt(Math.pow(node.locations[0] - d.location[0], 2) + Math.pow(node.locations[1] - d.location[1], 2));
                            }
                            if (d2 < this.distance_threshold) {
                                d_list.add(d);
                            }
                        }

                    }

                    //if we can find the distance from the bus_stop n to the hotel d is shorter than the distance to one of the skyline hotels s_d
                    //It means the hotel could be a candidate hotel of the bus stop n.
                    for (Data d : sk.allNodes) {

                        if (sk.sky_hotels.contains(d)) {
                            continue;
                        }

                        //distance from node to d
                        double d2;
                        if (constant.distance_calculation_type.equals("actual")) {
                            d2 = constant.distanceInMeters(node.locations[0], node.locations[1], d.location[0], d.location[1]);
                        } else {
                            d2 = Math.sqrt(Math.pow(node.locations[0] - d.location[0], 2) + Math.pow(node.locations[1] - d.location[1], 2));
                        }

                        double min_dist = Double.MAX_VALUE;
                        for (Data s_d : sk.sky_hotels) {
                            //distance from node to the skyline data s_d
                            double d1;
                            if (constant.distance_calculation_type.equals("actual")) {
                                d1 = constant.distanceInMeters(node.locations[0], node.locations[1], s_d.location[0], s_d.location[1]);
                            } else {
                                d1 = Math.sqrt(Math.pow(node.locations[0] - s_d.location[0], 2) + Math.pow(node.locations[1] - s_d.location[1], 2));
                            }

                            if (checkDominated(s_d.getData(), d.getData()) && d1 < min_dist) {
                                if (distance_threshold == -1) {
                                    min_dist = d1;
                                } else {
                                    if (d1 < this.distance_threshold) {
                                        min_dist = d1;
                                    }
                                }
                            }
                        }

                        if (this.distance_threshold != -1) {
                            if (min_dist > d2 && this.distance_threshold > d2) {
                                d_list.add(d);
                            }
                        } else {
                            if (min_dist > d2) {
                                d_list.add(d);
                            }
                        }

                    }

                    int d_size = d_list.size();


                    header_f.writeInt(page_list_number); //start page of the list file
                    header_f.writeInt(d_size); //the size of the list of current node

                    int records = 0;
                    for (Data d : d_list) {

                        list_f.writeInt(d.PlaceId);

                        records++;
                        //if page is full, page number ++, the records is set to 0
                        if ((this.pagesize_list / 4) < (records + 1)) {
                            page_list_number++;
                            records = 0;
                        }
                    }

                    //fill the remainning page with -1.
                    long list_end = list_f.getFilePointer();
                    for (long i = list_end; i < (page_list_number + 1) * this.pagesize_list; i++) {
                        list_f.writeByte(-1);
                    }

                    page_list_number++;

                    tx.success();
                }
            }

            header_f.close();
            list_f.close();
            n.shutdownDB();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * @return number of nodes on the graph
     */
    public long getLineNumbers() {
        long lines = 0;
        try {
            File file = new File(this.node_info_path);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                long l = Long.parseLong(line.split(" ")[0]);
                if (l > lines) {
                    lines = l;
                }
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    private boolean checkDominated(double[] costs, double[] estimatedCosts) {
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] * (1.0) > estimatedCosts[i]) {
                return false;
            }
        }
        return true;
    }

    private int getRandomNumberInRange_int(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
