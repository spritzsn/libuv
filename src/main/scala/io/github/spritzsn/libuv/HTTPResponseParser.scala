package io.github.spritzsn.libuv

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class HTTPResponseParser extends Machine:
  val start: State = versionState

  var method: String = null
  var version: String = null
  var status: String = null
  val headers =
    new mutable.TreeMap[String, String]()(scala.math.Ordering.comparatorToOrdering(String.CASE_INSENSITIVE_ORDER))
  var key: String = _
  val buf = new StringBuilder
  val body = new ArrayBuffer[Byte]

  def badRequest: Nothing = sys.error("bad request")

  abstract class AccState extends State:
    override def enter(): Unit = buf.clear()

    def acc(b: Int): Unit = buf += b.toChar

  abstract class NonEmptyAccState extends AccState:
    override def exit(): Unit = if buf.isEmpty then badRequest

  case object versionState extends NonEmptyAccState:
    def on = {
      case ' ' =>
        method = buf.toString
        transition(statusState)
      case '\r' | '\n' => badRequest
      case b           => acc(b)
    }

  case object statusState extends NonEmptyAccState:
    def on = {
      case ' ' =>
        status = buf.toString
        transition(reasonState)
      case '\r' | '\n' => badRequest
      case b           => acc(b)
    }

  case object reasonState extends AccState:
    def on = {
      case '\r' =>
        version = buf.toString
        transition(value2keyState)
      case '\n' => badRequest
      case b    => acc(b)
    }

  case object headerValueState extends AccState:
    def on = {
      case '\r' =>
        headers(key) = buf.toString
        transition(value2keyState)
      case '\n' => badRequest
      case b    => acc(b)
    }

  case object value2keyState extends State:
    def on = {
      case '\n' => transition(headerKeyState)
      case _    => badRequest
    }

  case object headerKeyState extends NonEmptyAccState:
    def on = {
      case '\r' if buf.nonEmpty => badRequest
      case '\r'                 => directTransition(blankState)
      case ':' =>
        key = buf.toString
        transition(key2valueState)
      case '\n' => badRequest
      case b    => acc(b)
    }

  case object blankState extends State:
    def on = {
      case '\n' if headers contains "Content-Length" => transition(bodyState)
      case '\n'                                      => transition(FINAL)
      case _                                         => badRequest
    }

  case object bodyState extends State:
    var len: Int = 0

    override def enter(): Unit =
      len = headers("Content-Length").toInt

      if len == 0 then transition(FINAL)

    def on = { case b =>
      body += b.toByte

      if body.length == len then transition(FINAL)
    }

  case object key2valueState extends State:
    def on = {
      case ' '         =>
      case '\r' | '\n' => badRequest
      case v =>
        pushback(v)
        transition(headerValueState)
    }

  override def toString: String =
    s"${super.toString}, response line: [$version $version $version], headers: $headers, body: $body, length: ${body.length}"
end HTTPResponseParser
