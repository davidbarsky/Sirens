package edu.brandeis.dag.models;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import edu.brandeis.dag.DAGException;
import edu.brandeis.dag.models.states.BuildStatus;
import edu.brandeis.dag.models.states.MachineType;

public class Task implements Comparable<Task> {
	private Integer id;
	private BuildStatus buildStatus;
	private Optional<StartEndTime> startEndTime;

	private Map<Task, Integer> dependencies;

	private Map<Task, Integer> dependents;

	private Map<MachineType, Integer> latencies;
	private TaskQueue tq;

	public Task(Integer id, TaskQueue tq, Map<MachineType, Integer> latencies) {
		this.id = id;
		this.buildStatus = BuildStatus.NOT_BUILT;
		this.startEndTime = Optional.empty();
		this.dependencies = new HashMap<>();
		this.dependents = new HashMap<>();
		this.latencies = latencies;
		this.tq = tq;
	}

	public void addDependency(int networkCost, Task t) {
		dependencies.put(t, networkCost);
		t.addDependent(networkCost, this);
	}

	private void addDependent(int networkCost, Task t) {
		dependents.put(t, networkCost);
	}

	public TaskQueue getTaskQueue() {
		return tq;
	}

	public boolean isBuilt() {
		if (buildStatus == BuildStatus.BUILT && startEndTime.isPresent())
			return true;

		if (buildStatus == BuildStatus.BUILT && !startEndTime.isPresent())
			throw new DAGException("Invariant violated: should not be able to have a built task without a start and end time!");

		return false;
	}

	public boolean buildable() {
		return dependencies.keySet()
				.stream()
				.allMatch(t -> t.isBuilt());
	}

	public Optional<StartEndTime> getStartEndTime() {
		return startEndTime;
	}

	public Optional<StartEndTime> build() {
		if (!buildable())
			return Optional.empty();

		// find the latest ending dependency
		int latestDep = this.dependencies
				.keySet().stream()
				.max(Comparator.comparingInt(a -> a.getStartEndTime().get().getEnd()))
				.map(t -> t.getStartEndTime().get().getEnd())
				.orElse(0);

		// find the latest starting task on my machine currently
		int latestStart = tq.getLatestStartTime();
		
		// figure out how much time we will need to spend doing networking...
		// (time we will use to write our data to other VMs with our dependents)
		int networkingTime = dependents.entrySet().stream()
				.filter(e -> e.getKey().getTaskQueue() != this.getTaskQueue())
				.mapToInt(e -> e.getValue())
				.sum();

		int myStart = Math.max(latestStart, latestDep);
		int myEnd = myStart + latencies.get(tq.getMachineType()) + networkingTime;

		this.startEndTime = Optional.of(new StartEndTime(myStart, myEnd, myEnd - networkingTime));
		this.buildStatus = BuildStatus.BUILT;

		return startEndTime;
	}

	public Map<Task, Integer> getDependencies() {
		return dependencies;
	}

	public Map<Task, Integer> getDependents() {
		return dependents;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Task))
			return false;

		return this.id == ((Task)o).id;		
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(id);
	}
	
	@Override
	public String toString() {
		return "Task " + id + " start: " + startEndTime.map(t -> String.valueOf(t.getStart())).orElse("<not built>")
				+ " end: " + startEndTime.map(t -> String.valueOf(t.getEnd())).orElse("<not built>");
	}

	@Override
	public int compareTo(Task that) {
	    if (!this.startEndTime.isPresent() || !that.startEndTime.isPresent())
	    	throw new DAGException("Tasks have not been built; there is no logical way to compare them.");

		if (this.startEndTime.get().getEnd() <= that.startEndTime.get().getStart() &&
				this.startEndTime.get().getStart() < this.startEndTime.get().getStart()) {
			return -1;
		} else if (this.startEndTime.get().getStart() >= that.startEndTime.get().getEnd() &&
				this.startEndTime.get().getEnd() >= that.startEndTime.get().getEnd()) {
			return 1;
		} else {
			return 0;
		}
	}
}
