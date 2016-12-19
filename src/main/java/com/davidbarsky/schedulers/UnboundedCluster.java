package com.davidbarsky.schedulers;

import com.davidbarsky.dag.DAGGenerator;
import com.davidbarsky.dag.TopologicalSorter;
import com.davidbarsky.dag.models.Task;
import com.davidbarsky.dag.models.TaskQueue;
import info.rmarcus.ggen4j.graph.Vertex;

import java.util.*;

public class UnboundedCluster {

    // The goal is to find the longest critical path, and place it onto
    // a processor or machine. We'll track visitation state using a set.
    public ArrayList<TaskQueue> linearCluster(int numQueues) {
        Collection<Vertex> graph = DAGGenerator.getErdosGNMSources(20);
        List<TaskQueue> linearizedDag = topologicalSorter.invoke(graph);

//        List<ArrayList<Task>> paths = longestPath(linearizedDag.getTasks());
        ArrayList<TaskQueue> result = new ArrayList<>();
//        for (ArrayList<Task> path : paths) {
//            result.add(new TaskQueue(MachineType.SMALL, path));
//        }

        return result;
    }

    // Returns a list of the longest paths
    public ArrayList<ArrayList<Task>> longestPath(List<Task> linearizedDAG) {
        return longestPathHelper(linearizedDAG,
                                new HashSet<>(),
                                new ArrayList<>());
    }

    private ArrayList<ArrayList<Task>> longestPathHelper(List<Task> linearizedDag,
                                                         Set<Task> visited,
                                                         List<Task> path) {
        Task current = linearizedDag.get(0);
        visited.add(current);
        path.add(current);

        for (Task child : current.getDependents().keySet()) {
            if (!visited.contains(child) && !path.contains(child)) {
                return longestPathHelper(linearizedDag, visited, path);
            }
        }

        return null; // TODO
    }
}
