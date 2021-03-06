package sirens.schedulers

import java.util
import java.util.Comparator

import collection.JavaConverters._
import sirens.models.Task
import sirens.models.states.MachineType
import sirens.typeclasses.TaskExtension._

import scala.annotation.tailrec
import scala.collection.mutable

object GraphProperties {

  def constructALAP(graph: util.List[Task]): util.Map[Task, Integer] = {
    val alap = new util.HashMap[Task, Integer]()

    graph.forEach { task: Task =>
      var min_ft = GraphProperties.lengthOfLongestCP(graph)
      task.getDependents.forEach { (child: Task, networkCost: Integer) =>
        if (alap.getOrDefault(child, 0) - task.getCostTo(child) < min_ft) {
          min_ft = alap.getOrDefault(child, 0) - task.getCostTo(child)
        }
      }
      alap.put(task, min_ft - 1)
    }
    alap
  }

  def countCriticalPaths(graph: util.List[Task]): Int = {
    val immutableGraph = graph.asScala.toList
    val bottomLevels = findBottomLevel(immutableGraph, machineType = MachineType.SMALL)

    immutableGraph.filter(_.isSource).map { task =>
      findCriticalPathWithLevels(task, levels = bottomLevels)
    }.size
  }

  def findCriticalPathWithLevels(source: Task, levels: Map[Task, Int]): List[Task] = {
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

  def lengthOfLongestCP(graph: util.List[Task]): Integer = {
    findCriticalPath(graph)
      .stream()
      .sorted(Comparator.comparingInt(_.size()))
      .findFirst()
      .get()
      .size()
  }

  def findCriticalPath(graph: util.List[Task]): util.List[util.List[Task]] = {
    val visited = new util.HashSet[Task]()
    val paths = new util.ArrayList[util.ArrayList[Task]]()

    def helper(task: Task, path: util.ArrayList[Task]): Unit = {
      visited.add(task)
      task.getDependents.forEach { (child: Task, _) =>
        if (!visited.contains(child)) {
          path.add(child)
          paths.add(path)
          helper(child, path)
        }
      }
    }

    helper(graph.get(0), new util.ArrayList[Task]())
    paths
      .stream()
      .distinct()
      .collect(util.stream.Collectors.toList[util.List[Task]])
  }

  // So far, cost of all tasks is either 1 or 2, depending if they're scheduled
  // on a small/large machine. Since we don't know the *cost* of a task until its scheduled
  // on a machine, tLevel  & bLevel will assume MachineType.SMALL, with a default value of 1.
  def findTopLevel(graph: util.List[Task], machineType: MachineType): util.HashMap[Task, Integer] = {
    val levels = new util.HashMap[Task, Integer]()

    graph.forEach { task: Task =>
      val computationCost = task.getLatencies.get(machineType)
      var max = 0
      task.getDependencies.keySet.forEach { parent: Task =>
        if (levels.getOrDefault(parent, 0)
              + computationCost
              + parent.getCostTo(task) > max) {
          max = (levels.getOrDefault(parent, 0)
            + computationCost
            + parent.getCostTo(task))
        }
      }
      levels.put(task, max)
    }
    levels
  }

  def findBottomLevel(graph: List[Task], machineType: MachineType): Map[Task, Int] = {
    val levels = mutable.Map[Task, Int]().withDefaultValue(0)
    graph.foreach { task: Task =>
      val computationCost = task.getLatencies.get(machineType)
      var max = 0
      task.getDependents.keySet.forEach { child: Task =>
        if (child.getCostTo(task) + levels(task) > max) {
          max = levels(child) + child.getCostTo(task)
        }
      }
      levels.put(task, max + computationCost)
    }

    levels.toMap
  }
}
