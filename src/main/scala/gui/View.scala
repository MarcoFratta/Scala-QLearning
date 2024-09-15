package gui

import cats.data.State

import java.awt.{Color, GridLayout}
import javax.swing.*
import scala.collection.immutable.Queue
import scala.swing.Dimension

import gui.Cells.*
import scala.util.Random


object View:


  case class Window(rows:Int, cols:Int, name:String = "",
                    w:Int = 200, h:Int= 200) extends JFrame:
    private var comp: Map[String,Int] = Map()
    private val menu = new JPanel()
    private var queue:Queue[String] = Queue()
    private var autoplay:Boolean = false

    menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS))
    setContentPane(menu)
    setSize(w, h)
    setTitle(name)
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    def isAutoplay:Boolean = autoplay
    def changeAutoplay(): Unit = autoplay = !autoplay
    def queueEvent(f: String): Unit =
      queue = queue enqueue f
    def nextEvent: Option[String] =
      val v = if queue.nonEmpty then Some(queue.dequeue) else None
      queue = v.fold(queue)(_._2)
      v.map(_._1)

    def addElement(n:String, c:JComponent): Unit =
      if comp.contains(n) then
        menu.remove(comp(n))
        menu.add(c, comp(n))
        menu.validate()
      else
        menu.add(c)
        menu.validate()
        comp += (n -> (menu.getComponentCount - 1))



  extension [A](a:A)
    private def >>(f:A => Unit): A =
      f(a)
      a
  private def createCell(size:Int):JPanel =
    new JPanel() >> {p =>
      p.setBackground(Color.white)
      p.setMinimumSize(new Dimension(size, size))
      p.setMaximumSize(new Dimension(size, size))
      //p.setBorder(BorderFactory.createLineBorder(Color.BLACK))
    }
  private val r = Random()


  def defaultWindow(title:String): Window = Window(0,0,title)
  def withSize(width: Int, height: Int): State[Window, Unit] =
    State.modify{_.copy(w = width, h = height)}
  def addGrid(w: Int, h: Int)(c:Int): State[Window, Unit] =
    State.modify{_.copy(rows = h, cols = w) >> {_.add(new JPanel(new GridLayout(h, w)) >>
      {g => g.setSize(w*c, h*c)
        0 until h*w foreach {_ => g.add(createCell(c))}})}}
  def select(x: Int, y: Int)(t:Cell): State[Window, Unit] =
    State.modify{_ >> {s => SwingUtilities.invokeLater(() => {
      selectCell(getGrid(s), s)(x,y)(t)})}}

  def updateLabel(n:String, s0:String)(using h:Int): State[Window, Unit] =
    State.modify{_ >> {s => SwingUtilities.invokeLater(() => {
      s.addElement(n, new JPanel() >> {labelCell(s0, h)(_)})
    })}}
  def addButton(n:String, s0:String)(using h:Int): State[Window, Unit] =
    State.modify{_ >> {s => SwingUtilities.invokeLater(() => {
     s.addElement(n, new JButton(s0) >> {b =>
       b.addActionListener(_ => s.queueEvent(n))
       //b.setMaximumSize(new Dimension(b.getSize.width, h))
       b.setAlignmentX(0.5f)
     })
    })}}

  def eventStream[S](): State[(S,Window), List[String]] =
    State(w => (w, LazyList.continually(w._2.nextEvent).takeWhile(_.isDefined).map(_.get).toList))

  def handle[S](l:Seq[String])(f:PartialFunction[String, State[(S,Window), Unit]]): State[(S,Window), Unit] =
    State.modify{s => l.foldLeft(s)((a, s2) => f(s2).run(a).value._1)}

  def pressButton(n:String): State[Window, Unit] =
    State.modify{_ >> {s => SwingUtilities.invokeLater(() => {
      Thread.sleep(5)
      s.queueEvent(n)
    })}}


  def autoplay()(using d:Int): State[Window, Unit] =
    State.modify(_ >> {s => SwingUtilities.invokeLater(() => {
      s.changeAutoplay()
      updateLabel("autoplay", "Autoplay: " +
        (if s.isAutoplay then "on" else "off")).run(s).value._1
      })})


  private def selectCell(p:JPanel, s:Window)(x:Int, y:Int)(c:Cell): Unit = {
    SwingUtilities.invokeLater(() => {
        c(p.getComponent(y * s.cols + x).asInstanceOf[JPanel])
    })
  }

  def selectN(pos :Set[(Int,Int)])(c:Cell): State[Window, Unit] =
    State.modify{s => s >> {_ => SwingUtilities.invokeLater(() => {
      val panel = getGrid(s)
      s >> {_ => pos.foreach{case (x,y) => selectCell(panel, s)(x,y)(c)}}
    })}}

  def reset(t:Cell) : State[Window, Unit] =
    State.modify{_ >> {p => SwingUtilities.invokeLater(() =>{
        getGrid(p).getComponents.foreach(x => t(x.asInstanceOf[JPanel]))
        p.repaint()}
      )}
      }
  def updateAll(f:(Int,Int) => Cell):State[Window, Unit] =
    State.modify{_ >> {s =>
      SwingUtilities.invokeLater(() => {
        val panel = getGrid(s)
        for
          row <- 0 until s.rows
          col <- 0 until s.cols
        do
          selectCell(panel, s)(col, row)(f(col, row))
          panel.validate()
      })}
    }

  private def getGrid(s: Window) = {
    val panel = s.getContentPane.getComponent(0).asInstanceOf[JPanel]
    panel
  }

  def show(): State[Window, Window] =
    State{s => SwingUtilities.invokeLater(() => {
        s.setVisible(true) // Example usage with width 800 and height 600
        s.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      })
      s -> s
    }



  @main
  def windowExample():Unit =
    val v = for
      _ <- withSize(800, 600)
      _ <- addGrid(7, 3)(30)
      _ <- select(0, 2)(robot)
      _ <- show()
    yield ()

    v.run(defaultWindow("Q-Learning")).value._1

