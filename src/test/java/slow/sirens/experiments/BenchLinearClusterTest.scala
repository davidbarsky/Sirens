package slow.sirens.experiments

import java.util

import org.junit.Test
import sirens.experiments.{ExperimentRunner, GraphGenerator}
import sirens.models.Task
import sirens.models.data.{LatencyBounds, NetworkingBounds}
import sirens.models.states.MachineType
import sirens.schedulers.LinearCluster

class BenchLinearClusterTest {
  val latencyBounds: LatencyBounds = LatencyBounds(10, 60)
  val networkingBounds: NetworkingBounds = NetworkingBounds(10, 60)

  def run(graphs: util.List[util.List[Task]]): Unit = {
    val results = ExperimentRunner.runSeries(
      scheduler = new LinearCluster,
      graphs = graphs,
      machineType = MachineType.SMALL,
      networkingBounds = networkingBounds,
      latencyBounds = latencyBounds
    )
    results.map(_.toCSV).foreach(println)
  }

  def cholskey(): Unit = {
    val graphs = GraphGenerator.cholesky(latencyBounds, networkingBounds)
    run(graphs)
  }

  @Test
  def erdosGNM(): Unit = {
    val graphs = GraphGenerator.erdos(latencyBounds, networkingBounds)
    run(graphs)
  }

//  @Test
//  def erdosGNP(): Unit = {
//    val graphs = GraphGenerator.erdosGNP(latencyBounds, networkingBounds)
//    run(graphs)
//  }

  @Test
  def forkJoin(): Unit = {
    val graphs = GraphGenerator.forkJoin(latencyBounds, networkingBounds)
    run(graphs)
  }

  @Test
  def poisson2D(): Unit = {
    val graphs = GraphGenerator.poisson(latencyBounds, networkingBounds)
    run(graphs)
  }

  @Test
  def sparseLU(): Unit = {
    val graphs = GraphGenerator.sparseLU(latencyBounds, networkingBounds)
    run(graphs)
  }
}
