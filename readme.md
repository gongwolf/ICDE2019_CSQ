# Constrain Sekyline Query
The java implementation of the [Skyline Queries Constrained by Multi-cost Transportation Networks
](https://ieeexplore.ieee.org/abstract/document/8731518). Given a multi-cost transportation network (MCTN), a list of POI objects **D** (could be on/off the MCTN) and a query POI object **q** in **D**, constrain skyline queries return the skyline solutions from **q** to objects **o** in **D**, where one solution is consists of the costs of the walking distance, the cost of the road network paths and the cost of the reached object **o**. 
<p align="center">
  <img src="Figs/MCNexample-small.png">
</p>
The code implements four methods that is used solve the constrain skyline query with/without index, and the goodness score function. Moreover, related pre-processes are implemented as well, include generating synthetic road network, POIs information data, and index building.  

## Preliminary 
- Neo4j,[https://neo4j.com/](https://neo4j.com/), our code embedded Neo4j ([https://neo4j.com/docs/java-reference/current/java-embedded/](https://neo4j.com/docs/java-reference/current/java-embedded/)) to our JAVA code. So there is no need to install the neo4j physically. 
- [Apache Maven](https://maven.apache.org/)
- [Apache commons-io](http://commons.apache.org/proper/commons-io/)
- [Apache commons-math3](https://commons.apache.org/proper/commons-math/)
- [Apache commons-cli](https://commons.apache.org/proper/commons-cli/)

## Compile
Execute the maven command, ```mvn clean compile assembly:single```, The executable jar file is placed under the *'target'* folder. 

## Usage
```
usage: java -jar constrainSkylineQuery.jar
Run the code of the constrain skyline path query :
 -d,--distance_threshold <arg>   distance threshold that is used in the
                                 approximation methods, the default value
                                 is '30'.
 -e,--measure <arg>              the measure is used to calculate the
                                 distance between two points, the default
                                 value is 'euclidean'.
 -gd,--grahpdegree <arg>         degree of the graph, the default value is
                                 '4'.
 -gm,--grahpdimension <arg>      dimension of the graph, the default value
                                 is '3'.
 -gs,--grahpsize <arg>           number of nodes in the graph, the default
                                 value is '1000'.
 -h,--help                       print the help of this command
 -hd,--poidimension <arg>        dimension of the poi objects, the default
                                 value is '3'.
 -hn,--poinumber <arg>           number of the poi objects, the default
                                 value is '200'.
 -i,--index <arg>                index enable/disable, the default value
                                 is 'false'.
 -id,--index_threshold <arg>     the distance range that is used to build
                                 the index, the default value is '-1'.
 -m,--method <arg>               method to execute, the default value is
                                 'exact_improved'.
 -q,--query <arg>                query by a given object ID or a random
                                 generated POI object ID), the default
                                 value is '-1'.
 -r,--range <arg>                the range parameter to generate the
                                 synthetic data, the default value is
                                 '20'.
 -u,--poinumber <arg>            the maximum number of POI objects that
                                 are within given range, the default value
                                 is '60'.
 -v,--verbose <arg>              calculate the goodness score while
                                 executing the approximate methods, the
                                 default value is 'false'.
```

## Details and examples
1. Generate synthetic road network data. Given number of graph nodes, degree and the dimension of the cost on each edge.  
>```java -jar constrainSkylineQuery.jar -m GenerateSynethicRoadNetwork -gs 1000 -gd 4 -gm 3```  
2. Create the Neo4j DataBase based on given graph information.  
>```java -jar constrainSkylineQuery.jar -m CreateRoadNetworkDB -gs 1000 -gd 4 -gm 3```  
3. Generate synthetic POI object based given graph information, number of objects and the number of dimension.  
>```java -jar constrainSkylineQuery.jar -m GenerateSynethicPOIsData -gs 1000 -gd 4 -gm 3 -hn 200 -hd 3 -r 20 -u 60```  
```r``` is the range that is used to count # of bus stops are within ```r``` of a generated object.  
```u``` is the threshold of the max number of bus stops are within ```r``` of a generated object  
4. Build the index for a given graph and a list of POI objects. The ```id``` is the range that is used to calculate the index from graph nodes to POI objects. The default value of ```id``` is **-1** which means no index range threshold. The details calculation can be found in the paper. 
> * ```java -jar constrainSkylineQuery.jar -m IndexBuilding -gs 1000 -gd 4 -gm 3 -hn 200 -hd 3 -r 20 -u 60 -id 500```  
> * ```java -jar constrainSkylineQuery.jar -m IndexBuilding -gs 1000 -gd 4 -gm 3 -hn 200 -hd 3 -r 20 -u 60 ```  
> * ```java -jar constrainSkylineQuery.jar -m IndexBuilding -gs 1000 -gd 4 -gm 3 -hn 200 -hd 3 -r 20 -u 60 -id -1```  
5. Execute the exact methods (*ExactBaseline* and *ExactImproved*)with given graph information and a query POI object. If the query object Id is not given, a random generated object will be used to conduct the query. 
> * ```java -jar constrainSkylineQuery.jar -m ExactBaseline -gs 1000 -gd 4 -gm 3 -hn 200 -hd 3 -r 20```  
> * ```java -jar constrainSkylineQuery.jar -m ExactImproved -gs 1000 -gd 4 -gm 3 -hn 200 -hd 3 -r 20 -i true -v true```
6. Execute the approximate methods (*ApproxRange* and *ApproxMixed*)with given graph information and a query POI object. If the query object Id is not given, a random generated object will be used to conduct the query. Compared with the exact methods, one extra parameter, distance_threshold, ```d``` is used to constrains the range of search from a graph node to POI objects. **NOTICE:Before executing the approximate methods with the given distance threshold by using index, a corresponding index with same index_threshold, ```id```, as distance_threshold needs to be created before.**
> * ```java -jar constrainSkylineQuery.jar -m ApproxMixed -gs 1000 -gd 4 -gm 3 -hn 200 -hd 3 -r 20 -d 30```  
> * ```java -jar constrainSkylineQuery.jar -m ApproxRange -gs 1000 -gd 4 -gm 3 -hn 200 -hd 3 -r 20 -d 40 -i true -v true```

#
- Graph information is identified by the graph size, degree and dimension
- POI object information is identified by the given graph with the range, the number of POIs and the POI object cost dimension. 
- The **index version** of queries is enabled by using the ```-i true```, when ```-m``` is equals to ```ExactBaseline```, ```ExactImproved```, ```ExactBaseline``` and ```ApproxMixed```.
- The **Goodness score** of the approximate method is calculated and displayed by using ```-v true```
- The **measurement of the distance** could be calculated by the **‘haversine’ formula** (```-e actual```) and the **L-2** distance (```-e euclidean```).
- The graph nodes and edges information are stored in the ```NodeInfo.txt``` and ```SegInfo.txt``` under the corresponding graph folder in the ```Data``` folder the ```Data```.
- The neo4j database are stored under the folder ```Data\Neo4jDB_files```.
- The index files are stored under the folder ```Data\index```.

## output
The performance results are displayed like  
>```181|195 14| running time(ms):3,51,471,0,1747| overall:3153   1217(789+125+1+0+269),355|result size:2584 109|984,547,0.5558943089430894|695389,13126,14110,8318```  
>```181|195 14| running time(ms):11,52,165,89,281| overall:1845   41(28+6+0+0+4),113|result size:382 95|984,346,0.3516260162601626|16965,3533,3545,4026```

Where are ```[query object ID], [# of POI objects don't dominated by the query object], [# of skyline POI objects in ], [Best First Search Time], [Nearest Graph Node search Time], [Search on the Graph Time], [Index Search Time], [Query time without Index], [Overall Query Time], [Time used to form the final result and add the result set] (Components: [Add to results time], [condition checking time], [Map operation time], [Empty checking time], [POI object Reading Time], [Path Expansion Time]), [# of Skyline Solutions], [# of distinct POI objects], [# of visited graph nodes], [# of graph nodes in the final solutions], [visited ratio], [# of times form the candidate solutions], [# of prefix skyline paths from query object to each graph nodes], [# of pop-up times from the queue].``` from left to right. 

## Acknowledgements
pecial thanks to java implementatioan fo the [R*-tree](http://chorochronos.datastories.org/?q=node/43)\[1\].  
[1] Beckmann, N., H. Kriegel, R. Schneider and B. Seeger. “The R*-tree: an efficient and robust access method for points and rectangles.” SIGMOD '90 (1990).