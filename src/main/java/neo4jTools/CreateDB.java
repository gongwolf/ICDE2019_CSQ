package neo4jTools;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import tools.configuration.constant;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CreateDB {
    String city;
    String DBBase;
    String DB_PATH;
    String NodesPath;
    String SegsPath;
    private GraphDatabaseService graphdb = null;

    public CreateDB(int graphsize, int degree, int dimension) {
        this.DBBase = constant.data_path + "/" + graphsize + "_" + degree + "_" + dimension + "/";
        this.DB_PATH = constant.db_path + "/" + graphsize + "_" + degree + "_" + dimension + "/databases/graph.db";
        NodesPath = DBBase + "NodeInfo.txt";
        SegsPath = DBBase + "SegInfo.txt";
        System.out.println("Road Network Data : " + DBBase);
        System.out.println("DB path : " + DB_PATH);
    }


    public CreateDB(String city) {
        this.city = city;
        this.DBBase = constant.data_path + "/" + this.city + "/";
        this.DB_PATH = constant.db_path + "/" + city + "_db" + "/databases/graph.db";
        NodesPath = DBBase + city + "_NodeInfo.txt";
        SegsPath = DBBase + city + "_SegInfo.txt";
        System.out.println("Road Network Data : " + DBBase);
        System.out.println("DB path : " + DB_PATH);
    }


    public static void main(String args[]) {
        CreateDB db = new CreateDB(1000, 4, 3);
        db.createDatabase();
    }

    public void createDatabase() {
        //System.out.println(DB_PATH);
        //System.out.println(DBBase);
        //System.out.println("=============");

        connector nconn = new connector(DB_PATH);
        //delete the data base at first
        nconn.deleteDB();
//        nconn.startDB();
        nconn.startBD_without_getProperties();
        this.graphdb = nconn.getDBObject();

        int num_node = 0, num_edge = 0;


        try (Transaction tx = this.graphdb.beginTx()) {
            BufferedReader br = new BufferedReader(new FileReader(NodesPath));
            String line = null;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                String[] attrs = line.split(" ");

                String id = attrs[0];
                double lat = Double.parseDouble(attrs[1]);
                double log = Double.parseDouble(attrs[2]);
                Node n = createNode(id, lat, log);
                num_node++;
                if (num_node % 10000 == 0) {
                    System.out.println(num_node + " nodes was created");
                }
            }
            tx.success();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nconn.shutdownDB();


        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(SegsPath));
            String line = null;


            ArrayList<String> ss = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                ss.add(line);
                num_edge++;

                if (num_edge % 100000 == 0) {
                    process_batch_edges(ss);
                    ss.clear();
                    System.out.println(num_edge + " edges were created");
                }
            }
            process_batch_edges(ss);
            ss.clear();
            System.out.println(num_edge + " edges were created");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Database is created, the location of the db file is " + this.DB_PATH);
        System.out.println("there are total " + num_node + " nodes and " + num_edge + " edges");
    }


    public void createDatabaseForCity() {
        connector nconn = new connector(DB_PATH);
        //delete the data base at first
        nconn.deleteDB();
        nconn.startBD_without_getProperties();
        this.graphdb = nconn.getDBObject();

        int num_node = 0, num_edge = 0;


        try (Transaction tx = this.graphdb.beginTx()) {
            BufferedReader br = new BufferedReader(new FileReader(NodesPath));
            String line = null;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                String[] attrs = line.split(" ");

                String id = attrs[0];
                double lat = Double.parseDouble(attrs[1]);
                double log = Double.parseDouble(attrs[2]);
                Node n = createNode(id, lat, log);
                num_node++;
                if (num_node % 10000 == 0) {
                    System.out.println(num_node + " nodes was created");
                }
            }
            tx.success();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nconn.shutdownDB();


        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(SegsPath));
            String line = null;


            ArrayList<String> ss = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                ss.add(line);
                num_edge++;

                if (num_edge % 100000 == 0) {
                    process_batch_edges(ss);
                    ss.clear();
                    System.out.println(num_edge + " edges were created");
                }
            }
            process_batch_edges(ss);
            ss.clear();
            System.out.println(num_edge + " edges were created");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Database is created, the location of the db file is " + this.DB_PATH);
        System.out.println("there are total " + num_node + " nodes and " + num_edge + " edges");
    }

    private void process_batch_edges(ArrayList<String> ss) {
        connector nconn = new connector(DB_PATH);
        nconn.startBD_without_getProperties();
        this.graphdb = nconn.getDBObject();
        try (Transaction tx = this.graphdb.beginTx()) {
            for (String line : ss) {
                String attrs[] = line.split(" ");
                String src = attrs[0];
                String des = attrs[1];
                double EDistence = Double.parseDouble(attrs[2]);
                double MetersDistance = Double.parseDouble(attrs[3]);
                double RunningTime = Double.parseDouble(attrs[4]);
                createRelation(src, des, EDistence, MetersDistance, RunningTime);
            }
            tx.success();
        }
        nconn.shutdownDB();
    }

    private void createRelation(String src, String des, double eDistence, double metersDistance, double runningTime) {
        try {
            //Node srcNode = this.graphdb.findNode(BNode.BusNode, "name", src);
            //Node desNode = this.graphdb.findNode(BNode.BusNode, "name", des);
            Node srcNode = this.graphdb.getNodeById(Long.valueOf(src));
            Node desNode = this.graphdb.getNodeById(Long.valueOf(des));

            Relationship rel = srcNode.createRelationshipTo(desNode, Line.Linked);
            rel.setProperty("EDistence", eDistence);
            rel.setProperty("MetersDistance", metersDistance);
            rel.setProperty("RunningTime", runningTime);
        } catch (Exception e) {
            System.out.println(src + "-->" + des);
            e.printStackTrace();
            System.exit(0);
        }
    }

    private Node createNode(String id, double lat, double log) {
        Node n = this.graphdb.createNode(BNode.BusNode);
        n.setProperty("name", id);
        n.setProperty("lat", lat);
        n.setProperty("log", log);
        if (n.getId() != Long.valueOf(id)) {
            System.out.println("id not match  " + n.getId() + "->" + id);
        }
        return n;


    }

    public void restartDB() {

    }
}