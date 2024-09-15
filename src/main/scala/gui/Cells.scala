package gui

import java.awt.{Color, Component, FlowLayout}
import javax.swing.{BorderFactory, JLabel, JPanel, SwingConstants}
import scala.swing.Dimension

object Cells:


  private type Panel = JPanel
  type Cell = Panel => Unit


  def empty:Cell = x =>
    x.setBackground(Color.white)
    x.setLayout(new FlowLayout(FlowLayout.CENTER))

  def borderCell:Cell = x =>
    empty(x)
    x.setBorder(BorderFactory.createLineBorder(Color.black))

  def labelCell(s: String, h: Int):Cell = x =>
    x.removeAll()
    empty(x)
    val l = new JLabel(s)
    l.setForeground(Color.BLACK)
    l.setMaximumSize(new Dimension(l.getSize.width, h))
    l.setHorizontalAlignment(SwingConstants.CENTER)
    l.setVerticalAlignment(SwingConstants.CENTER)
    l.setAlignmentY(Component.CENTER_ALIGNMENT)
    l.setFont(l.getFont.deriveFont(15.0f))
    l.setText(s)
    x.add(l)
    x.setMaximumSize(new Dimension(x.getMaximumSize.width, l.getMaximumSize.height))
    x.validate()

  def obstacle: Cell = x =>
    x.setBackground(Color.black)

  def enemy: Cell = x =>
    x.setBackground(Color.red)

  def path: Cell = x =>
    x.setBackground(Color.green)
  def jump(s:String):Cell = x =>
    labelCell(s,20)(x)
    x.setBackground(Color.cyan)

  def robot: Cell = x =>
    x.setBackground(Color.blue)

  def item: Cell = x =>
    labelCell("Item", 20)(x)
    x.setBackground(Color.yellow)
    x.setForeground(Color.black)

  def release:Cell = x =>
    labelCell("Release", 20)(x)
    x.setBackground(Color.orange)

  def borderGrid: (Int,Int) => Cell = (x, y) => borderCell
  def noBorderGrid: (Int,Int) => Cell = (x, y) => empty

  extension (f:(Int,Int) => Cell)
    def showStartPos(s:(Int,Int)): (Int,Int) => Cell =
      (x,y) => (x, y) match
        case `s` => robot
        case _ => f(x,y)
    def showObstacles(m:Set[(Int,Int)]): (Int,Int) => Cell =
      (x,y) => (x, y) match
        case (x, y) if m.contains((x,y)) => obstacle
        case _ => f(x,y)
    def showItems(items: Set[(Int,Int)], rel:Set[(Int,Int)]): (Int, Int) => Cell =
      (x, y) => (x, y) match
        case (x, y) if items.contains((x, y)) => item
        case (x, y) if rel.contains((x, y)) => release
        case c => f(x,y)
    def showEnemies(m:Set[(Int,Int)]): (Int,Int) => Cell =
      (x,y) => (x, y) match
        case (x, y) if m.contains((x, y)) => enemy
        case c => f(x,y)
    def showJumps[A](p:PartialFunction[((Int, Int), A), (Int, Int)], actions:Set[A]): (Int,Int) => Cell =
      (x, y) => (x, y) match
        case (x, y) if actions.exists(a => p.isDefinedAt(((x, y), a))) =>
          jump(p((x,y),actions.find(a => p.isDefinedAt(((x, y), a))).get).toString())
        case c => f(x,y)



