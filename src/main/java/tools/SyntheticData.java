package tools;

import RstarTree.Constants;
import RstarTree.Data;
import RstarTree.RTree;
import javafx.util.Pair;
import org.apache.commons.math3.distribution.BetaDistribution;
import tools.configuration.constant;

import java.io.*;
import java.util.HashSet;
import java.util.Random;

import static tools.configuration.constant.randomFloatInRange;

public class SyntheticData {

    private final int numberOfNodes; // number of POIs
    private final int dimension; //Costs of each POI

    /**** graph information that is used to store the POIs tree and data information ****/
    private final int grahsize;
    private final int degree;
    private final int upper;
    private final int graph_dimension;

    private final String info_path;
    Random r = new Random(System.nanoTime());
    HashSet<Pair<Double, Double>> busLocation = new HashSet<>();
    BetaDistribution bt = new BetaDistribution(2, 19);
    private double range = 0;

    public SyntheticData(int numberOfNodes, int poi_dimension, int graphsize, int degree, int graph_dimension, double range, int upper) {
        this.numberOfNodes = numberOfNodes;
        this.dimension = poi_dimension;

        this.grahsize = graphsize;
        this.degree = degree;
        this.graph_dimension = graph_dimension;

        this.info_path = constant.data_path + "/" + this.grahsize + "_" + this.degree + "_" + this.graph_dimension;
        System.out.println("read the graph information from :" + this.info_path);
        this.upper = upper;
        this.range = range;
    }

    public void createStaticNodes_betaDistribution() {
        //read the latitude and longitude of the node
        readNodeInfo();

        //location of the R-tree
        String treePath = this.info_path + "/" + grahsize + "_" + degree + "_" + graph_dimension + "_" + range + "_" + numberOfNodes + ".rtr";
        System.out.println("The POIs are stored in the rtree file :" + treePath);
        File fp = new File(treePath);

        if (fp.exists()) {
            fp.delete();
        }

        //list of the information of the object
        String infoPath = this.info_path + "/" + grahsize + "_" + degree + "_" + graph_dimension + "_" + range + "_" + numberOfNodes + ".txt";
        System.out.println("The POIs information are stored in the file :" + treePath);
        File file = new File(infoPath);
        if (file.exists()) {
            file.delete();
        }

        HashSet<Data> result = new HashSet<>();

        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            RTree rt = new RTree(treePath, Constants.BLOCKLENGTH, Constants.CACHESIZE, dimension);

            int i = 0;
            while (i < this.numberOfNodes) {
                int numberOfBusStopInRange = (int) Math.floor(bt.sample() * upper); //number of bus stops within given range
                int counter = 0;

//                System.out.println(i + "  " + numberOfBusStopInRange);

                Data d = new Data(this.dimension);
                d.setPlaceId(i);
                int tries = 0;

                float latitude, longitude;
                do {

                    latitude = randomFloatInRange(0f, 360f);
                    longitude = randomFloatInRange(0f, 360f);


                    for (Pair<Double, Double> p : this.busLocation) {
                        double distance = Math.sqrt(Math.pow(latitude - p.getKey(), 2) + Math.pow(longitude - p.getValue(), 2));
                        if (distance <= range) {
                            counter++;
                        }
                    }

                    if (counter != numberOfBusStopInRange) {
                        counter = 0;
                        tries++;
                    }
                } while (counter != numberOfBusStopInRange || tries == 500);


                d.setLocation(new double[]{latitude, longitude});


                float priceLevel = randomFloatInRange(0f, 5f);
                float Rating = randomFloatInRange(0f, 5f);
                float other = randomFloatInRange(0f, 5f);

                d.setData(new float[]{priceLevel, Rating, other});

                bw.write(i + "," + latitude + "," + longitude + "," + priceLevel + "," + Rating + "," + other + "\n");

//                System.out.println(d + "  - # bus stops within range(" + numberOfBusStopInRange + ")");

                result.add(d);

                rt.insert(d);
                i++;
            }

            rt.delete();

            System.out.println("There are " + result.size() + " POIs are generated !!!!!");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {

                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();
//                System.out.println("Done!! See MCP_Results.csv for MCP of each cow for each day.");

            } catch (IOException ex) {

                ex.printStackTrace();

            }
        }
    }

    /**
     * Read the bus station location information that is used to make sure the generated synthetic POIs follow given
     * Beta Distribution
     */
    private void readNodeInfo() {
        String bus_data = this.info_path + "/NodeInfo.txt";
        try {
            File f = new File(bus_data);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";
//            System.out.println("Reading file using Buffered Reader");
            while (((readLine = b.readLine()) != null)) {

                String[] infos = readLine.trim().split(" ");
                double latitude = Double.valueOf(infos[1]);
                double longitude = Double.valueOf(infos[2]);
//                    System.out.println(LatAndLong+" "+latitude+" "+longitude);
                this.busLocation.add(new Pair<>(latitude, longitude));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("there are " + this.busLocation.size() + " bus stops");
    }
}
