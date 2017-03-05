package com.davidbarsky.dag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import com.davidbarsky.dag.models.states.MachineType;
import com.davidbarsky.schedulers.BoundedScheduler;
import org.junit.Test;

import com.davidbarsky.dag.models.TaskQueue;
import com.davidbarsky.schedulers.RoundRobin;

import info.rmarcus.ggen4j.graph.Vertex;

public class DAGGeneratorTest {

	@Test
	public void randomGraph() {
		BoundedScheduler roundRobin = new RoundRobin();
		List<TaskQueue> tqs = roundRobin.generateSchedule(4, MachineType.SMALL);
		assertEquals(4, tqs.size());
	}

	@Test
	public void generateGraphRange() {
		List<Collection<Vertex>> range = DAGGenerator.generateGraphRange(20);

		// minimum number is 15 vertices, so we should get 6 results back.
		assertTrue(range.size() == 6);
		assertTrue(range.stream().noneMatch(Collection::isEmpty));
	}

	@Test
	public void getErdosGMNSources() {
		Collection<Vertex> sources = DAGGenerator.getErdosGNMSources(20);
		assertTrue(sources.size() == 20);

		sources.forEach(vertex -> {
			assertNotNull(vertex);
			assertNotNull(vertex.getParents());
			assertNotNull(vertex.getChildren());
			assertEquals(vertex.hashCode(), Integer.hashCode(vertex.getID()));
		});
	}
}
