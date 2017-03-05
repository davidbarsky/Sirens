package com.davidbarsky.dag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.davidbarsky.schedulers.BoundedScheduler;
import org.junit.Test;

import com.davidbarsky.dag.models.Task;
import com.davidbarsky.dag.models.TaskQueue;
import com.davidbarsky.dag.models.states.MachineType;
import com.davidbarsky.schedulers.RoundRobin;

public class ActualizerTest {
	@SuppressWarnings("null")
	@Test
	public void actualize() {
		BoundedScheduler roundRobin = new RoundRobin();
		ArrayList<TaskQueue> tqs = (ArrayList<TaskQueue>) roundRobin.generateSchedule(2, MachineType.SMALL);

		final List<TaskQueue> tasks = Actualizer.actualize(tqs);
		List<TaskQueue> copied = new ArrayList<>(tasks);

		if (tasks == null) {
			fail("Actualizer returned null!");
			return;
		}
		
		copied.forEach(t -> Collections.sort(t.getTasks()));

		assertEquals(copied, tasks);
		assertTrue(tasks.stream().map(TaskQueue::getTasks).allMatch(t -> t.stream().allMatch(Task::isBuilt)));
		assertTrue(tasks.stream().map(TaskQueue::getTasks).allMatch(t -> t.stream().allMatch(Task::buildable)));

	}

	@SuppressWarnings("null")
	public void impossibleActualize() {
		TaskQueue tq = new TaskQueue(MachineType.SMALL);

		Map<MachineType, Integer> latencies = new EnumMap<MachineType, Integer>(MachineType.class);
		latencies.put(MachineType.SMALL, 10);
		latencies.put(MachineType.LARGE, 10);

		Task t1 = new Task(0, tq, latencies);
		Task t2 = new Task(1, tq, latencies);

		t2.addDependency(10, t1);

		tq.add(t2);
		tq.add(t1);

		assertEquals(Actualizer.actualize(Collections.singletonList(tq)), null);
	}
}
