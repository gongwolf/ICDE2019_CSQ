package neo4jTools;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import tools.configuration.constant;

import java.io.*;
import java.util.*;

public class generateGraph {
    String DBBase;
    String EdgesPath;
    String NodePath;

    int numberNodes, numberofEdges, numberofDimens;


    public generateGraph(int graphsize, int degree, int dimensions) {
        this.numberNodes = graphsize;
        this.numberofEdges = Math.round(numberNodes * (degree));
        this.numberofDimens = dimensions;

        this.DBBase = constant.data_path + "/" + graphsize + "_" + degree + "_" + dimensions;
        EdgesPath = DBBase + "/SegInfo.txt";
        NodePath = DBBase + "/NodeInfo.txt";

    }


    public static void main(String args[]) {
        int numberNodes = 1000, numberofDegree = 4, numberofDimen = 3;
        generateGraph g = new generateGraph(numberNodes, numberofDegree, numberofDimen);
        g.generateG(true);
    }

    public void generateG(boolean deleteBefore) {
        if (deleteBefore) {
            File dataF = new File(DBBase);
            try {
                FileUtils.deleteDirectory(dataF);
                dataF.mkdirs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HashMap<Pair<String, String>, String[]> Edges = new HashMap<>();
        HashMap<String, String[]> Nodes = new HashMap<>();


        //生成经度和纬度
        for (int i = 0; i < numberNodes; i++) {
            String cost1 = String.valueOf(constant.getRandomNumberInRange(1, 360));
            String cost2 = String.valueOf(constant.getRandomNumberInRange(1, 360));
            Nodes.put(String.valueOf(i), new String[]{cost1, cost2});
        }

        //Create the Edges information.
        for (int i = 0; i < numberofEdges; i++) {
            String startNode = String.valueOf(constant.getRandomNumberInRange_int(0, numberNodes - 1));
            String endNode = String.valueOf(constant.getRandomNumberInRange_int(0, numberNodes - 1));
            while (startNode.equals(endNode)) {
                endNode = String.valueOf(constant.getRandomNumberInRange_int(0, numberNodes - 1));
            }

            double x1 = Double.parseDouble(Nodes.get(startNode)[0]);
            double y1 = Double.parseDouble(Nodes.get(startNode)[1]);
            double x2 = Double.parseDouble(Nodes.get(endNode)[0]);
            double y2 = Double.parseDouble(Nodes.get(endNode)[1]);

            double dist = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));

            //Random Generate the costs
            String[] costs = new String[numberofDimens];
            for (int j = 0; j < numberofDimens; j++) {
                costs[j] = String.valueOf(constant.getGaussian(dist, dist * 0.2));
            }

            Edges.put(new Pair(startNode, endNode), costs);
        }

        //stores all of the start node of the created edges
        HashSet<String> containedNodes = new HashSet<>();
        for (Pair<String, String> p : Edges.keySet()) {
            containedNodes.add(p.getKey());
        }

        //Make sure every node is connected
        for (String node : Nodes.keySet()) {
            //if there is the node does not have any edge start with it
            //Create edge for it
            if (!containedNodes.contains(node)) {
                String startNode = String.valueOf(node);
                String endNode = String.valueOf(constant.getRandomNumberInRange_int(0, numberNodes - 1));
                while (startNode.equals(endNode)) {
                    endNode = String.valueOf(constant.getRandomNumberInRange_int(0, numberNodes - 1));
                }

                double x1 = Double.parseDouble(Nodes.get(startNode)[0]);
                double y1 = Double.parseDouble(Nodes.get(startNode)[1]);
                double x2 = Double.parseDouble(Nodes.get(endNode)[0]);
                double y2 = Double.parseDouble(Nodes.get(endNode)[1]);

                double dist = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));

                String[] costs = new String[numberofDimens];
                for (int j = 0; j < numberofDimens; j++) {
                    costs[j] = String.valueOf(constant.getGaussian(dist * 2, dist * 0.3));
                }

                Edges.put(new Pair(startNode, endNode), costs);
            }
        }

        writeNodeToDisk(Nodes);
        writeEdgeToDisk(Edges);
        System.out.println("Graph is created, there are " + Nodes.size() + " nodes and " + Edges.size() + " edges");


    }

    private void writeEdgeToDisk(HashMap<Pair<String, String>, String[]> edges) {
        try (FileWriter fw = new FileWriter(EdgesPath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            for (Map.Entry<Pair<String, String>, String[]> node : edges.entrySet()) {
//                System.out.println(EdgesPath);
                StringBuffer sb = new StringBuffer();
                String snodeId = node.getKey().getKey();
                String enodeId = node.getKey().getValue();
                sb.append(snodeId).append(" ");
                sb.append(enodeId).append(" ");
                for (String cost : node.getValue()) {
                    sb.append(cost).append(" ");
                }
                out.println(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeNodeToDisk(HashMap<String, String[]> nodes) {
        try (FileWriter fw = new FileWriter(NodePath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
//            System.out.println(NodePath);
            TreeMap<String, String[]> tm = new TreeMap<String, String[]>(new StringComparator());
            tm.putAll(nodes);
            for (Map.Entry<String, String[]> node : tm.entrySet()) {
                StringBuffer sb = new StringBuffer();
                String nodeId = node.getKey();
                sb.append(nodeId).append(" ");
                for (String cost : node.getValue()) {
                    sb.append(cost).append(" ");
                }
                out.println(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
