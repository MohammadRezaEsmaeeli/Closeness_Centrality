import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Closeness {

    // Config
    static String FILE_PATH = "sample-graph.txt";
    static int NUMBER_OF_MOST_CENTRAL_NODES = 10;
    static int START_NODE_INDEX = 1;
    static int END_NODE_INDEX = 30_000_000;
    // ------

    public static ArrayList<Integer>[] adj;
    public static HashMap<Integer,Double> scors;
    public static HashMap<Integer,HashMap<Integer,Integer>> info;

    public static void readLine_saveGraph() {
        Path path = Paths.get(FILE_PATH);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> {
                String[] temp = s.split(" ");
                int src = Integer.parseInt(temp[0]);
                int des = Integer.parseInt(temp[1]);
                adj[src].add(des);
            } );
        }
        catch (IOException ex) {
            System.out.println();
        }catch (OutOfMemoryError e){
            System.err.println("Max JVM memory: " + Runtime.getRuntime().maxMemory());
        }
    }

    public static int navBFS(int s,int d) {
        if (adj[s].size() == 0) {
            return -1;
        }
        int src = s;
        boolean[] visited = new boolean[END_NODE_INDEX + 1];
        LinkedList<Integer> queue = new LinkedList<>();
        visited[s] = true;
        int level = 0;
        queue.add(s);
        queue.add(-1);
        while (queue.size() != 0) {
            s = queue.poll();
            if (s == -1) {
                queue.add(-1);
                if (queue.size() == 1 && queue.get(0) == -1) {
                    return -1;
                }
                level++;
                continue;
            }
            if (s == d) {
                return level;
            }
            Iterator<Integer> i = adj[s].listIterator();
            while (i.hasNext()) {
                int n = i.next();
                if (!visited[n]) {
                    visited[n] = true;
                    queue.add(n);
                    int dist = findDistFromInfo(n,d);
                    if (dist != -1) {
                        return dist + level + 1;
                    }
                    addToInfo(src,n,level + 1);
                }
            }
        }
        return -1;
    }

    public static void addToInfo(int src,int des,int dist) {
        if (dist <= 5) {
            if (!info.containsKey(src)) {
                info.put(src, new HashMap<>());
            }
            info.get(src).put(des,dist);
        }
    }

    public static int findDistFromInfo(int src,int des) {
        int dist = -1;
        if (info.containsKey(src) && info.get(src).containsKey(des)) {
            dist = info.get(src).get(des);
        }
        return dist;
    }

    public static HashMap<Integer, Double> sortByValue(HashMap<Integer, Double> hm) {
        List<Map.Entry<Integer, Double> > list =
            new LinkedList<Map.Entry<Integer, Double> >(hm.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double> >() {
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        HashMap<Integer, Double> temp = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public static void printResult(HashMap<Integer, Double> nodes) {
        System.out.println("The most central Nodes (Score is sum of distanses): ");
        int index = 0;
        for(Map.Entry<Integer, Double> entry : nodes.entrySet()) {
            int key = entry.getKey();
            double value = entry.getValue();
            System.out.println("Node " + key + " => Score: " + value);
            index++;
            if (index >= NUMBER_OF_MOST_CENTRAL_NODES) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        info = new HashMap<>();
        adj = new ArrayList[END_NODE_INDEX + 1];
        scors = new HashMap<Integer,Double>();
        for (int i = 0; i < adj.length; i++) {
            adj[i] = new ArrayList<>();
        }
        long step1 = System.currentTimeMillis();
        readLine_saveGraph();
        long step2 = System.currentTimeMillis();
        System.out.println("Read File and Save Graph: " + (step2 - step1) + " ms");
        long startAlgorithm = System.currentTimeMillis();
        for (int i = START_NODE_INDEX; i < adj.length; i++) {
            double sumDistanse = 0;
            for (int j = START_NODE_INDEX; j < adj.length; j++) {
                if (i != j) {
                    int d = navBFS(i,j);
                    if (d != -1) {
                        sumDistanse += d;
                    }
                }
            }
            scors.put(i, sumDistanse);
        }
        long endAlgorithm = System.currentTimeMillis();
        System.out.println("Run time: " + (endAlgorithm - startAlgorithm) + " ms");
        scors = sortByValue(scors);
        printResult(scors);
    }

}
