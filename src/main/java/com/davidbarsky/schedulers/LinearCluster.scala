package com.davidbarsky.schedulers

import java.util

import collection.JavaConverters._
import com.davidbarsky.dag.models.{Task, TaskQueue}
import com.davidbarsky.dag.models.states.MachineType
import com.davidbarsky.extensions.TaskExtension._

import scala.annotation.tailrec
import collection.mutable.{ListBuffer, Set => MutableSet}

// So here's the plan.
// First, split the graph into two groups: nodes *with* neighbors and *without*.
// The ones without neighbors are shuffled onto their own task queue. Those can be built without worry.
// That leaves us with the rest. Of the rest (the dependent nodes) need to scheduled.
// To do that, we will find the longest path between a source and leaf node. To accomplish this,
// we will use a BFS (with negative weights determine the "shortest" path).
// On each iteration of the BFS, we'll pass a set of visited nodes. We stop iteration when
// the set of visited nodes equals the size of the dependent nodes.

class LinearCluster extends UnboundedScheduler {
  override def generateSchedule(
      graph: util.List[Task],
      machineType: MachineType): util.List[TaskQueue] = {

    // Initialization
    val immutableGraph = graph.asScala.toList
    val independentTasks: List[Task] = immutableGraph.filter(_.isIndependent)
    val sourceTasks: List[Task] = immutableGraph.filter(_.isSource)
    val levels: Map[Task, Int] = GraphProperties.findBottomLevel(immutableGraph, machineType)
    val criticalPaths = sourceTasks.map(t => findCriticalPath(t, levels))
    val adjacentNodes = criticalPaths.map(neighborsOfCriticalPath)

    val scheduled = Set[Task]()
    val buffer = ListBuffer[TaskQueue]()

    buffer += new TaskQueue(machineType, independentTasks.asJava)

//    immutableGraph.map(findPath).foreach { path: List[Task] =>
//      val taskPath = path
//        .filterNot(t => scheduled.contains(t))
//        .sortWith(_.getID < _.getID)
//        .distinct
//      buffer += new TaskQueue(machineType, taskPath.asJava)
//      taskPath.foreach(t => scheduled.add(t))
//    }

    buffer.filterNot(_.getTasks.isEmpty).asJava
  }

  def findCriticalPath(source: Task, levels: Map[Task, Int]): List[Task] = {
    @tailrec def go(t: Task, acc: List[Task]): List[Task] = {
      if (t.isLeaf) {
        acc.reverse
      } else {
        val maxChild = max(t.getChildren)
        go(maxChild, maxChild :: acc)
      }
    }

    @tailrec def max(as: List[Task]): Task = {
      as match {
        case List(t: Task) => t
        case a :: b :: rest =>
          max((if (levels(a) > levels(b)) a else b) :: rest)
      }
    }

    go(source, Nil)
  }

  def neighborsOfCriticalPath(criticalPath: List[Task]): List[Task] = {
    val path = MutableSet[Task]()
    for (member <- criticalPath) {
      val neighbors = member.getChildren ++ member.getParents
      neighbors.map(t => path.add(t))
    }
    path.toList.sortWith(_.getID < _.getID)
  }

//  def longestPath(list: List[Task]): List[Int] = {
//    @tailrec def max(as: List[Int]): Int = as match {
//      case Nil => 0
//      case _ :: Nil => 1
//      case a :: b :: rest => max((if (a > b) a else b) :: rest)
//    }
//
//    def distance(t: Task): Int = {
//      if (t.isLeaf) 0
//      else {
//        max(t.getNegativeDependents.map { entry =>
//          1 + distance(entry._1)
//        }.toList)
//      }
//    }
//
//    list.map(distance)
//  }
//
//  private def findPath(current: Task): List[Task] = {
//    if (current.isLeaf) {
//      current :: Nil
//    } else {
//      current.getNegativeDependents.flatMap { entry =>
//        current :: findPath(entry._1)
//      }.toList
//    }
//  }

//  private def longestPathByWeight(task: Task): List[Edge] = {
//    val edges = task.getDependents.asScala.map {
//      case (task: Task, cost: Integer) => Edge(task, cost)
//    }.toList
//
//    max(edges) match {
//      case None => Nil
//      case Some(edge) => edge :: longestPathByWeight(edge.task)
//    }
//  }
//
//  @tailrec private def max(as: List[Edge]): Option[Edge] = as match {
//    case Nil => None
//    case List(Edge(task: Task, cost: Int)) => Some(Edge(task, cost))
//    case x :: y :: tail =>
//      max((if (x.networkCost > y.networkCost) x else y) :: tail)
//  }

  override def toString: String = {
    "LinearCluster"
  }
}
