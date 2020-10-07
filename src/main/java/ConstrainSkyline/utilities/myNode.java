package ConstrainSkyline.utilities;


import RstarTree.Data;
import neo4jTools.connector;
import org.neo4j.graphdb.Transaction;
import tools.configuration.constant;

import java.util.ArrayList;

public class myNode {
    public long id;
    public long node;
    public ArrayList<path> skyPaths;
    //-1 is the exact approach, otherwise the threshold that create paths from the query point
    public double distance_q;
    public double[] locations;
    public boolean inqueue;

    public myNode(Data queryNode, long current_id, double distance_threshold) {
        this.node = this.id = current_id;
        this.locations = new double[2];
//        this.qNode = queryNode;
        skyPaths = new ArrayList<>();
        setLocations(queryNode);
//        System.out.println(this.distance_q+"     "+distance_threshold);

        //Lemma1: the dummy path from q to o must be the skyline path from q to o
        if (distance_threshold != -1 && this.distance_q <= distance_threshold) {
            path dp = new path(this);
            this.skyPaths.add(dp);
        } else if (distance_threshold == -1) {
            path dp = new path(this);
            this.skyPaths.add(dp);
        }
        inqueue = false;
    }


    public myNode(long current_id) {
        this.node = this.id = current_id;
        this.locations = new double[2];
        try (Transaction tx = connector.graphDB.beginTx()) {
            locations[0] = (double) connector.graphDB.getNodeById(this.id).getProperty("lat");
            locations[1] = (double) connector.graphDB.getNodeById(this.id).getProperty("log");
            tx.success();
        }
        inqueue = false;
    }

    public double[] getLocations() {
        return locations;
    }

    public void setLocations(Data queryNode) {
        try (Transaction tx = connector.graphDB.beginTx()) {
            locations[0] = (double) connector.graphDB.getNodeById(this.id).getProperty("lat");
            locations[1] = (double) connector.graphDB.getNodeById(this.id).getProperty("log");
            if (constant.distance_calculation_type.equals("actual")) {
                this.distance_q = constant.distanceInMeters(locations[0], locations[1], queryNode.location[0], queryNode.location[1]);
            } else {
                this.distance_q = Math.sqrt(Math.pow(locations[0] - queryNode.location[0], 2) + Math.pow(locations[1] - queryNode.location[1], 2));

            }
            tx.success();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.node = this.id = id;
    }

    public boolean addToSkyline(path np) {
        int i = 0;
        if (skyPaths.isEmpty()) {
            this.skyPaths.add(np);
            return true;
        } else {
            boolean can_insert_np = true;
            for (; i < skyPaths.size(); ) {
                if (checkDominated(skyPaths.get(i).costs, np.costs)) {
                    can_insert_np = false;
                    break;
                } else {
                    if (checkDominated(np.costs, skyPaths.get(i).costs)) {
                        this.skyPaths.remove(i);
                    } else {
                        i++;
                    }
                }
            }

            if (can_insert_np) {
                this.skyPaths.add(np);
                return true;
            }
        }
        return false;
    }

    private boolean checkDominated(double[] costs, double[] estimatedCosts) {
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] * (1) > estimatedCosts[i]) {
                return false;
            }
        }
        return true;
    }


    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof myNode)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        myNode c = (myNode) o;

        // Compare the data members and return accordingly
        return c.id == this.id;
    }
}
