package ConstrainSkyline.utilities;

import neo4jTools.Line;
import neo4jTools.connector;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import tools.configuration.constant;

import java.util.ArrayList;
import java.util.Iterator;


public class path {
    public double[] costs;
    public boolean expaned;
    public long startNode, endNode;

    public ArrayList<Long> nodes;
    public ArrayList<Long> rels;
    public ArrayList<String> propertiesName;


    //Create the dummpy path from q to current node
    public path(myNode current) {
        this.costs = new double[constant.path_dimension];
        costs[0] = current.distance_q;
        costs[1] = costs[2] = costs[3] = 0;
//        constants.print(costs);
        this.startNode = current.node;
        this.endNode = current.node;
        this.expaned = false;

        this.nodes = new ArrayList<>();
        this.rels = new ArrayList<>();
        this.propertiesName = new ArrayList<>();

        this.setPropertiesName();

        //store the Long Objects
//        this.nodes.add(getLongObject_Node(this.endNode));
    }

    //Create new path based on given path and a new relationship
    public path(path old_path, Relationship rel) {

        this.costs = new double[constant.path_dimension];
        this.startNode = old_path.startNode;
        this.endNode = rel.getOtherNodeId(old_path.endNode);


        this.nodes = new ArrayList<>();
        this.nodes.addAll(old_path.nodes);

        this.rels = new ArrayList<>();
        this.rels.addAll(old_path.rels);

        this.propertiesName = new ArrayList<>(old_path.propertiesName);


        expaned = false;

        this.nodes.add(this.endNode);
        this.rels.add(rel.getId());

        System.arraycopy(old_path.costs, 0, this.costs, 0, this.costs.length);

        calculateCosts(rel);
    }


    public ArrayList<path> expand() {
        ArrayList<path> result = new ArrayList<>();

        try (Transaction tx = connector.graphDB.beginTx()) {
            Iterable<Relationship> rels = connector.graphDB.getNodeById(this.endNode).getRelationships(Line.Linked, Direction.OUTGOING);
            Iterator<Relationship> rel_Iter = rels.iterator();
            while (rel_Iter.hasNext()) {
                Relationship rel = rel_Iter.next();
                path nPath = new path(this, rel);
                result.add(nPath);
            }
            tx.success();
        }
        return result;
    }


    private void calculateCosts(Relationship rel) {
//        System.out.println(this.propertiesName.size());
        if (this.startNode != this.endNode) {
            int i = 1;
            for (String pname : this.propertiesName) {
//                System.out.println(i+" "+this.costs[i]+"  "+Double.parseDouble(rel.getProperty(pname).toString()));

                this.costs[i] = this.costs[i] + (double) rel.getProperty(pname);
                i++;
            }
        }
    }


    public void setPropertiesName() {
        this.propertiesName = connector.propertiesName;
    }

    public String toString() {
//        System.out.println("dasdasd:   "+this.nodes.size()+"  "+this.rels.size());
        StringBuffer sb = new StringBuffer();
        if (this.rels.isEmpty()) {
            sb.append("(").append(this.startNode).append(")");
        } else {
            int i;
            for (i = 0; i < this.nodes.size() - 1; i++) {
                sb.append("(").append(this.nodes.get(i)).append(")");
                // sb.append("-[Linked," + this.relationships.get(i).getId() +
                // "]->");
                sb.append("-[").append(this.rels.get(i)).append("]-");
            }
            sb.append("(").append(this.nodes.get(i)).append(")");
        }

        sb.append(",[");
        for (double d : this.costs) {
            sb.append(" " + d);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null && this == null) {
            return true;
        } else if ((obj == null && this != null) || (obj != null && this == null)) {
            return false;
        }

        if (obj == this)
            return true;
        if (!(obj instanceof path))
            return false;


        path o_path = (path) obj;
        if (o_path.endNode != endNode || o_path.startNode != startNode) {
            return false;
        }

        for (int i = 0; i < costs.length; i++) {
            if (o_path.costs[i] != costs[i]) {
                return false;
            }
        }

//        if (!o_path.nodes.equals(this.nodes) || !o_path.rels.equals(this.rels)) {
//            return false;
//        }
        return true;
    }

    public boolean hasCycle() {
        for (int i = 0; i < rels.size() - 2; i++) {
            if (this.endNode == rels.get(i)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDummyPath() {
        for (int i = 1; i < this.costs.length; i++) {
            if (this.costs[i] != 0) {
                return false;
            }
        }
        return true;
    }
}
