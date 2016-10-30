package edu.brandeis.dag;
import java.util.Collection;

import edu.brandeis.dag.models.TaskQueue;

public class Actualizer {
	private Actualizer() { }
	
	public static void actualize(Collection<TaskQueue> tqs) {

		// keep going while any of our tasks are not built
		while (tqs.stream().anyMatch(tq -> tq.hasUnbuiltTask())) {
			// each time, try to build the next task on each task queue.
			boolean builtAny = tqs.stream()
					.map(tq -> tq.buildNextUnbuiltTask())
					.anyMatch(opt -> opt.isPresent());
			
			// if we couldn't build any tasks in the queue, we should be done!
			if (!builtAny) 
				break;
		}
		
		// check invariant: all tasks should now be built
		if (tqs.stream().anyMatch(tq -> tq.hasUnbuiltTask()))
			throw new DAGException("Could not build task! Check input graph for cycles, and make sure all dependencies are in a task queue.");
	}

}