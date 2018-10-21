import scalaz.zio._
import java.io.IOException
import scala.concurrent.duration._
import scalaz.zio.console._

object ZioTest {
  //Pure Values
  val z:IO[Nothing,String] = IO.point("Hello World")
  val z1: IO[Nothing, String] = IO.now("Hello World")
  println(z)
  println(z1)

  //Impure Code
  val z2: IO[Nothing, Long] = IO.sync(System.nanoTime())

  //Mapping and Chaining
  //You can change an IO[E, A] to an IO[E, B] by calling the map method A => B
  val zmap:IO[Nothing,Int] = IO.point(21).map(_*3)
  println(zmap)

  //IO[E, A] into an IO[E2, A] by calling the leftMap method E => E2
  val zLeftMap:IO[Exception,String] = IO.fail("No no!").leftMap(msg=>new Exception(msg))

  //Chaining ,execute two actions in sequence with the flatMap method
  val zFlatMap:IO[Nothing,List[Int]] = IO.point(List(1,2,5)).flatMap { list => IO.point(list.map(_+1)) }
  println(zFlatMap)
  
  //Scala's for
  val l: IO[Nothing, List[Int]] = for {
    list <- IO.point(List(1, 2, 3))
    added <- IO.point(list.map(_ + 1))
  } yield added

  //--------------------------------------------------------------------------------------------
  //You can create IO actions that describe failure with IO.fail

  def sqrt(io: IO[Nothing, Double]): IO[String, Double] =
    IO.absolve(
      io.map(value =>
        if (value < 0.0) Left("Value must be >= 0.0")
        else Right(Math.sqrt(value))
      )
    )

  //Brackets

  //Promise
  val ioPromise: IO[Nothing, Promise[Exception, String]] = Promise.make[Exception, String]
  val ioGet: IO[Exception, String] = ioPromise.flatMap(promise => promise.get)

  //Here is a scenario where we use a Promise to hand-off a value between two Fibers
  val program: IO[IOException, Unit] = for {
    promise         <-  Promise.make[Nothing, String]
    sendHelloWorld  =   (IO.now("hello world") <* IO.sleep(1.second)).flatMap(promise.complete)
    getAndPrint     =   promise.get.flatMap(putStrLn)
    fiberA          <-  sendHelloWorld.fork
    fiberB          <-  getAndPrint.fork
    _               <-  (fiberA zip fiberB).join
  } yield ()

  //Schedule
  val fibonacci = Schedule.fibonacci(10.milliseconds)
  println(fibonacci)

  //Ref
  val ref = for {
    ref <- Ref(100)
    v1 <- ref.get
    v2 <- ref.set(v1 - 50)
  } yield v2

  println(ref)
  //state transformation
  var idCounter = 0
  def freshVar: String = {
    idCounter += 1
    s"var${idCounter}"
  }
  val v1 = freshVar
  val v2 = freshVar
  val v3 = freshVar
  //println(v1 +"  " + v2 + " " +v3)

  def main(args: Array[String]): Unit = {
  }
}
