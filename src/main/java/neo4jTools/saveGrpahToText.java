package neo4jTools;

import org.neo4j.graphdb.GraphDatabaseService;
import tools.configuration.constant;

public class saveGrpahToText {
    public static void main(String args[]) {
        String city = "LA";
        String DB_PATH = constant.db_path + "/" + city + "_db" + "/databases/graph.db";
        connector nconn = new connector(DB_PATH);
        nconn.startBD_without_getProperties();
        long pre_n = nconn.getNumberofNodes();
        long pre_e = nconn.getNumberofEdges();
        System.out.println("deal with the database (" + city + ") graph at " + nconn.DB_PATH + "  " + pre_n + " nodes and " + pre_e + " edges");

        String textFilePath = constant.data_path + "/" + city+"/";
        nconn.saveGraphToTextFormation(textFilePath, city);
        nconn.shutdownDB();
    }
}
