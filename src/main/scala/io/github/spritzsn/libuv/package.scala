package io.github.spritzsn

import scala.collection.mutable
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*
import scala.scalanative.libc.stdlib.*

package object libuv:

  import extern.{LibUV => lib}

  implicit class HandleType(val value: lib.uv_handle_type) extends AnyVal

  object HandleType:
    final val UNKNOWN_HANDLE = new HandleType(0)
    final val ASYNC = new HandleType(1)
    final val CHECK = new HandleType(2)
    final val FS_EVENT = new HandleType(3)
    final val FS_POLL = new HandleType(4)
    final val HANDLE = new HandleType(5)
    final val IDLE = new HandleType(6)
    final val NAMED_PIPE = new HandleType(7)
    final val POLL = new HandleType(8)
    final val PREPARE = new HandleType(9)
    final val PROCESS = new HandleType(10)
    final val STREAM = new HandleType(11)
    final val TCP = new HandleType(12)
    final val TIMER = new HandleType(13)
    final val TTY = new HandleType(14)
    final val UDP = new HandleType(15)
    final val SIGNAL = new HandleType(16)
    final val FILE = new HandleType(17)
    final val HANDLE_TYPE_MAX = new HandleType(18)

  implicit class ReqType(val value: lib.uv_req_type) extends AnyVal

  object ReqType:
    final val UNKNOWN_REQ = new ReqType(0)
    final val REQ = new ReqType(1)
    final val CONNECT = new ReqType(2)
    final val WRITE = new ReqType(3)
    final val SHUTDOWN = new ReqType(4)
    final val UDP_SEND = new ReqType(5)
    final val FS = new ReqType(6)
    final val WORK = new ReqType(7)
    final val GETADDRINFO = new ReqType(8)
    final val GETNAMEINFO = new ReqType(9)
    final val REQ_TYPE_MAX = new ReqType(10)

  implicit class RunMode(val value: lib.uv_run_mode) extends AnyVal

  object RunMode:
    final val RUN_DEFAULT = new RunMode(0)
    final val RUN_ONCE = new RunMode(1)
    final val RUN_NOWAIT = new RunMode(2)

  def checkError(v: Int, label: String): Int =
    if v != 0 then sys.error(s"$label error: ${errName(v)}: ${strError(v)}") else v

  def version: Long = lib.uv_version.toLong

  def versionString: String = fromCString(lib.uv_version_string)

  def strError(err: Int): String = fromCString(lib.uv_strerror(err))

  def errName(err: Int): String = fromCString(lib.uv_err_name(err))

  def loopInit: Loop =
    val loop = malloc(lib.uv_loop_size)

    lib.uv_loop_init(loop)
    loop

  implicit class Loop(val loop: lib.uv_loop_t) extends AnyVal:
    def run(mode: RunMode = RunMode.RUN_DEFAULT): Int = lib.uv_run(loop, mode.value)

    def updateTime(): Unit = lib.uv_update_time(loop)

    def now: Long = lib.uv_now(loop)

    def timer: Timer =
      val timer = malloc(lib.uv_handle_size(HandleType.TIMER.value))

      checkError(lib.uv_timer_init(loop, timer), "uv_timer_init")
      timer

    def prepare: Prepare =
      val prepare = malloc(lib.uv_handle_size(HandleType.PREPARE.value))

      checkError(lib.uv_prepare_init(loop, prepare), "uv_prepare_init")
      prepare

    def check: Check =
      val check = malloc(lib.uv_handle_size(HandleType.CHECK.value))

      checkError(lib.uv_check_init(loop, check), "uv_check_init")
      check

    def idle: Idle =
      val idle = malloc(lib.uv_handle_size(HandleType.IDLE.value))

      checkError(lib.uv_idle_init(loop, idle), "uv_idle_init")
      idle

    def tcp: TCP =
      val tcp = malloc(lib.uv_handle_size(HandleType.TCP.value))

      checkError(lib.uv_tcp_init(loop, tcp), "uv_tcp_init")
      tcp

  def defaultLoop: Loop = lib.uv_default_loop

  private val timerCallbacks = new mutable.HashMap[lib.uv_timer_t, Timer => Unit]

  private val timerCallback: lib.uv_timer_cb = (t: lib.uv_timer_t) => timerCallbacks(t)(t)

  implicit class Timer(val handle: lib.uv_timer_t) extends AnyVal:
    def start(callback: Timer => Unit, timeout: Long, repeat: Long = 0): Int =
      timerCallbacks(handle) = callback
      lib.uv_timer_start(handle, timerCallback, timeout, repeat)

    def stop: Int = lib.uv_timer_stop(handle)

    def dispose(): Unit =
      timerCallbacks -= handle
      free(handle)

    // todo: add rest of timer methods

  private val prepareCallbacks = new mutable.HashMap[lib.uv_prepare_t, Prepare => Unit]

  private val prepareCallback: lib.uv_prepare_cb = (t: lib.uv_prepare_t) => prepareCallbacks(t)(t)

  implicit class Prepare(val handle: lib.uv_prepare_t) extends AnyVal:
    def start(callback: Prepare => Unit): Int =
      prepareCallbacks(handle) = callback
      lib.uv_prepare_start(handle, prepareCallback)

    def stop: Int = lib.uv_prepare_stop(handle)

    def dispose(): Unit =
      prepareCallbacks -= handle
      free(handle)

  private val checkCallbacks = new mutable.HashMap[lib.uv_check_t, Check => Unit]

  private val checkCallback: lib.uv_check_cb = (t: lib.uv_check_t) => checkCallbacks(t)(t)

  implicit class Check(val handle: lib.uv_check_t) extends AnyVal:
    def start(callback: Check => Unit): Int =
      checkCallbacks(handle) = callback
      lib.uv_check_start(handle, checkCallback)

    def stop: Int = lib.uv_check_stop(handle)

    def dispose(): Unit =
      checkCallbacks -= handle
      free(handle)

  private val idleCallbacks = new mutable.HashMap[lib.uv_idle_t, Idle => Unit]

  private val idleCallback: lib.uv_idle_cb = (t: lib.uv_idle_t) => idleCallbacks(t)(t)

  implicit class Idle(val handle: lib.uv_idle_t) extends AnyVal:
    def start(callback: Idle => Unit): Int =
      idleCallbacks(handle) = callback
      lib.uv_idle_start(handle, idleCallback)

    def stop: Int = lib.uv_idle_stop(handle)

    def dispose(): Unit =
      idleCallbacks -= handle
      free(handle)

  implicit class Buffer(val buf: lib.uv_buf_t) extends AnyVal:
    def apply(idx: Int): Int = buf._1(idx) & 0xff

    def update(idx: Int, b: Int): Unit = buf._1(idx) = b.toByte

    def alloc(size: Int): Unit =
      val s = size.toUInt

      buf._1 = malloc(s)
      buf._2 = s

    def dispose(): Unit = free(buf._1)

  type ConnectionCallback = (TCP, Int) => Unit

  private val connectionCallbacks = new mutable.HashMap[lib.uv_tcp_t, ConnectionCallback]

  private val connectionCallback: lib.uv_connection_cb = (tcp: lib.uv_tcp_t, status: CInt) =>
    connectionCallbacks(tcp)(tcp, status)

  val ALLOC_SIZE: CUnsignedInt = 1024.toUInt

  type AllocCallback = (TCP, Int, Buffer) => Unit

  private val allocCallbacks = new mutable.HashMap[lib.uv_tcp_t, AllocCallback]

  private val allocCallback: lib.uv_alloc_cb = (tcp: lib.uv_tcp_t, size: CSize, buf: lib.uv_buf_t) =>
    allocCallbacks(tcp)(tcp, size.toInt, buf)

  type ReadCallback = (TCP, Int, Buffer) => Unit

  private val readCallbacks = new mutable.HashMap[lib.uv_tcp_t, ReadCallback]

  private val readCallback: lib.uv_read_cb = (tcp: lib.uv_tcp_t, size: CSize, buf: lib.uv_buf_t) =>
    readCallbacks(tcp)(tcp, size.toInt, buf)

  private val writeCallback: lib.uv_read_cb =
    (req: lib.uv_write_t, status: Int) =>
      val buffer = (!req).asInstanceOf[Ptr[Buffer]]

      free(buffer._1)
      free(buffer.asInstanceOf[Ptr[Byte]])
      free(req.asInstanceOf[Ptr[Byte]])

  private val shutdownCallback: lib.uv_shutdown_cb =
    (req: lib.uv_shutdown_t, status: Int) =>
      val handle = (!req).asInstanceOf[lib.uv_tcp_t]

      lib.uv_close(handle, closeCallback)
      free(req.asInstanceOf[Ptr[Byte]])

  private val closeCallback: lib.uv_close_cb =
    (handle: lib.uv_tcp_t) => free(handle.asInstanceOf[Ptr[Byte]])

  implicit class TCP(val handle: lib.uv_tcp_t) extends AnyVal:
    def bind(ip: String, port: Int, flags: Int): Int = Zone { implicit z =>
      val socketAddress: Ptr[Byte] = stackalloc[Byte](lib.SOCKADDR_IN_SIZE)

      checkError(lib.uv_ip4_addr(fromCString(ip), port, socketAddress), "uv_ip4_addr")
      checkError(lib.uv_tcp_bind(handle, socketAddress, flags), "uv_tcp_bind")
    }

    def listen(backlog: Int, cb: ConnectionCallback): Int =
      connectionCallbacks(handle) = cb
      checkError(lib.uv_listen(handle, backlog, connectionCallback), "uv_tcp_listen")

    def accept(client: TCP): Int = checkError(lib.uv_accept(handle, client), "uv_accept")

    def readStart(alloc_cb: AllocCallback, read_cb: ReadCallback): Int =
      allocCallbacks(handle) = alloc_db
      readCallbacks(handle) = read_cb
      checkError(lib.uv_read_start(handle, allocCallback, readCallback), "uv_read_start")

    def readStop: Int =
      allocCallbacks -= handle
      readCallbacks -= handle

    def write(data: collection.IndexedSeq[Byte]): Int =
      val req = malloc(lib.uv_req_size(ReqType.WRITE)).asInstanceOf[lib.uv_write_t]
      val buffer = malloc(sizeof[lib.uv_buf_t]).asInstanceOf[lib.uv_buf_tp]
      val len = data.length.toUInt
      val base = malloc(len)

      for i <- data.indices do base(i) = data(i)

      buffer._1 = data
      buffer._2 = len
      !req = buffer.asInstanceOf[Ptr[Byte]]
      checkError(lib.uv_write(req, handle, buffer, 1, writeCallback), "uv_write")

    def shutdown: Int =
      val req = malloc(lib.uv_req_size(ReqType.SHUTDOWN)).asInstanceOf[lib.uv_shutdown_t]

      !req = handle
      checkError(lib.uv_shutdown(req, client, shutdownCallback), "uv_shutdown")

    def dispose(): Unit = free(handle)