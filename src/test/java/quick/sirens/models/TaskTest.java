package quick.sirens.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import sirens.experiments.GraphGenerator;
import org.junit.Test;

import sirens.dag.DAGException;
import sirens.models.Task;
import sirens.models.TaskQueue;
import sirens.models.states.MachineType;

public class TaskTest {
	@Test
	public void testAddDependency() {
		Task firstTask = new Task(1, new TaskQueue(MachineType.SMALL), new HashMap<>());
		Task secondTask = new Task(2, new TaskQueue(MachineType.SMALL), new HashMap<>());

		firstTask.addDependency(5, secondTask);

		assertTrue(firstTask.getDependencies().containsKey(secondTask));
		assertTrue(secondTask.getDependents().containsKey(firstTask));
		assertEquals(firstTask.getDependencies().get(secondTask), new Integer(5));
		assertEquals(secondTask.getDependents().get(firstTask), new Integer(5));

	}

	@Test
	public void buildable1() {
		Task firstTask = new Task(1, new TaskQueue(MachineType.SMALL), new HashMap<>());
		assertTrue(firstTask.buildable());
	}

	@Test
	public void buildable2() {
		Task firstTask = new Task(1, new TaskQueue(MachineType.SMALL), new HashMap<>());
		Task secondTask = new Task(2, new TaskQueue(MachineType.SMALL), new HashMap<>());
		firstTask.addDependency(5, secondTask);
		assertFalse(firstTask.buildable());
	}

	@Test
	public void testHashCode() {
		Task task = new Task(1, new TaskQueue(MachineType.LARGE), new HashMap<>());
		assertEquals(task.hashCode(), Integer.hashCode(1));
	}

	@Test(expected=DAGException.class)
	public void compareToWithUnbuiltTasks() {
		Task firstTask = new Task(1, new TaskQueue(MachineType.LARGE), new HashMap<>());
		Task secondTask = new Task(2, new TaskQueue(MachineType.LARGE), new HashMap<>());

		firstTask.compareTo(secondTask);
	}

	@Test
	public void isLeaf() {
		Task task = new Task(1, new TaskQueue(MachineType.LARGE), new HashMap<>());
		assertTrue(task.isLeaf());
	}

	@Test
	public void isSource() {
		Task task = new Task(1, new TaskQueue(MachineType.LARGE), new HashMap<>());
		assertTrue(task.isSource());
	}

	@Test
	public void isIndependent() {
		List<Task> graph = GraphGenerator.genericGraph(4);
		long independentTaskSize = graph.stream().filter(Task::isIndependent).count();
		long dependentTaskSize = graph.stream().filter(task -> !task.isIndependent()).count();

		assertEquals(graph.size(), independentTaskSize + dependentTaskSize);
	}
}