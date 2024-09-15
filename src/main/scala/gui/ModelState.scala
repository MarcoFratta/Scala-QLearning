package gui

import model.QLearning.Pos2d

case class ModelState[S <: Pos2d[S]](step: Int, episode: Int, path: Seq[S]):
  private type History[Q] = Seq[Seq[(S, Q)]]

  def next[Q](hist: History[Q]):ModelState[S] =
    val maxStep = hist(episode).length
    copy(step = if episode == hist.length-1 && step == maxStep - 1 then step else (step + 1) % maxStep,
      episode = if step == maxStep - 1 then (episode + 1) min hist.length - 1 else episode,
      path = if step == maxStep - 1 then List() else path :+ hist(episode)(step)._1)

  def prev[Q](hist: History[Q]):ModelState[S] =
    val newEpisode = if step == 0 then (episode - 1) max 0 else episode
    copy(step = (step, episode) match
      case (0,0) => 0
      case (0,_) => hist(newEpisode).length - 1
      case (a,_) =>  a - 1,
      episode = newEpisode,
      path = if step == 0 then hist(newEpisode).map(_._1).toList else path.drop(1).toList)

  def getState[Q](hist: History[Q]): (Int, Int) = (hist(episode)(step)._1.x, hist(episode)(step)._1.y)
  def getQ[Q](hist: History[Q]): Q = hist(episode)(step)._2
