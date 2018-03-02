package mandelbrot

import processing.core._
import PConstants._
import PApplet._
import spire.implicits._
import spire.math._
import spire.math.{log => slog, abs => sabs}
import scala.annotation.tailrec

package object mandelbrot {
  type FromRGB = (Float, Float, Float) => Unit
}

/** I wrote a few colours as helpers (also to have some fun). Then I found
  * Colour is already a type in Processing, and used in an interpolator */

sealed trait Colour {
  def apply(method: mandelbrot.FromRGB): Unit
}
final case object Black extends Colour {
  def apply(method: mandelbrot.FromRGB): Unit = method(0, 0, 0)
}
final case object White extends Colour {
  def apply(method: mandelbrot.FromRGB): Unit = method(255, 255, 255)
}

/** Using the colour linear interpolator there is no need to define this */

final case class Gray(levelD: Double) extends Colour {
  private val level = levelD.toFloat
  def apply(method: mandelbrot.FromRGB): Unit = method(level, level, level)
}

case class Parameters(w: Double, h: Double, upper: Complex[Double], delta: Double, iter: Int, width: Int, height: Int) {
  def map(x: Int, y: Int) = Complex(upper.real + x * w / width, upper.imag - y * h / height)
  def zoom(pt: Complex[Double]): Parameters = {
    val _w = w * 0.6
    val _h = _w * height / width
    val _x0 = pt.real - _w / 2
    val _y0 = pt.imag + _h / 2
    val _delta = delta * 0.6
    val _iter = math.floor(iter * 1.4).toInt
    Parameters(_w, _h, Complex(_x0, _y0), _delta, _iter, width, height)
  }
}

object Parameters {
  def empty: Parameters = Parameters(0, 0, Complex(0, 0), 0, 0, 0, 0)
}

object Main extends PApplet {
  def main(args:Array[String]): Unit = {
    PApplet.main("mandelbrot.Main")
  }
}

class Main extends PApplet {
  var params: Parameters = Parameters.empty
  var drawn = false
  var gray = true
  var colour = false

  val INITIAL_ITERS = 100

  private def paint(colour: Colour): Unit = colour.apply(stroke)

  override def setup(): Unit = {
    White.apply(background)
    params = Parameters(2.8, 3.0 * height / width, Complex(-2.1, 3.0 * height / width / 2), 0.01 / width, INITIAL_ITERS, width, height)
    PApplet.println(params)
    noLoop
  }

  override def settings(): Unit = {
    size(800, 600, PConstants.P3D)
  }


  /** Implements the Distance Estimation Method (based on an approximation of the
    * Green potential on the exterior of the Mandelbrot set)
    */

  private def mandelbrot(c: Complex[Double], limit: Int): Double = {
    @tailrec def loop(z: Complex[Double], dz: Complex[Double], n: Int): Double =
      if (n >= limit) -1
      else if (z.abs > 4.0) z.abs * slog(z.abs) / dz.abs
      else loop(z * z + c, 2 * z * dz + 1, n + 1)
    loop(c, 1, 1)
  }

  override def mousePressed: Unit = {
    if(drawn){
      val pt = params.map(mouseX, mouseY)
      params = params.zoom(pt)
      PApplet.println(s"Zooming centered on $pt with ${params.iter} iterations and ${params.delta} precision")
      drawn = false
      loop
    }
  }

  override def keyPressed: Unit = {
    if(key == 'q') {
      exit
    }
    if(key == 'g') {
      gray = !gray
      drawn = false
      loop
    }
    if(key == 'c') {
      colour = true
      drawn = false
      loop
    }
  }

  val ceruleanBlue = color(42, 82, 190)
  val limeGreen = color(50, 205, 50)

  override def draw {
    def plot = {
      for (x <- 0 until width; y <- 0 until height) {
        val distance = mandelbrot(params.map(x, y), params.iter)
        if(colour){
          // We need a few logs to get the Green potential to be a bit less
          // exponential. The absolute value adds a bit of a
          // glitch/discontinuity, and also a "colour cyclic reflection" with
          // respect to the potential
          stroke(lerpColor(ceruleanBlue, limeGreen, (sabs(slog(sabs(slog(sabs(slog(distance))))))).toFloat))
        } else {
          if(gray)
            Gray(-slog(distance) * 10).apply(stroke)
          else {
            if (distance > params.delta)
              White.apply(stroke)
            else
              Black.apply(stroke)
          }
        }
        point(x, y)
      }
      drawn = true
    }
    if(!drawn) {
      plot
    }
    saveFrame("image.png")
  }
}
