package io.github.spritzsn

import scala.collection.mutable
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*
import scala.scalanative.libc.stdlib.*
import java.util.IdentityHashMap

package object libuv:

  import extern.{LibUV => lib}

  def eof: Int = lib.uv_eof

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

  val O_RDONLY = 0
  val O_WRONLY = 1
  val O_RDWR = 2

  val O_CREAT: Int = if sys.props("os.name") == "Mac OS X" then 512 else 64
  val O_APPEND = 1024

  private def o(n: Int): Int = Integer.parseInt(n.toString, 8)

  val S_IRWXU: Int = o(700)

  val S_IRUSR: Int = o(00400) //  user has read permission

  val S_IWUSR: Int = o(00200) //  user has write permission

  val S_IXUSR: Int = o(00100) //  user has execute permission

  val S_IRWXG: Int = o(00070) //  group has read, write, and execute permission

  val S_IRGRP: Int = o(00040) //  group has read permission

  val S_IWGRP: Int = o(00020) //  group has write permission

  val S_IXGRP: Int = o(00010) //  group has execute permission

  val S_IRWXO: Int = o(00007) //  others have read, write, and execute permission

  val S_IROTH: Int = o(00004) //  others have read permission

  val S_IWOTH: Int = o(00002) //  others have write permission

  val S_IXOTH: Int = o(00001) //  others have execute permission

  val S_ISUID: Int = o(0004000) //  set-user-ID bit

  val S_ISGID: Int = o(0002000) //  set-group-ID bit (see inode(7)).

  val S_ISVTX: Int = o(0001000) //  sticky bit (see inode(7)).

  def checkError(v: Int, label: String): Int =
    if v != 0 then sys.error(s"$label error: ${errName(v)}: ${strError(v)}") else v

  def version: Long = lib.uv_version.toLong

  def versionString: String = fromCString(lib.uv_version_string)

  def strError(err: Int): String = fromCString(lib.uv_strerror(err))

  def errName(err: Int): String = fromCString(lib.uv_err_name(err))

  def hrTime: Long = lib.uv_hrtime.toLong

  def getHostname: String =
    val buffer = stackalloc[CChar](256.toUInt)
    val size = stackalloc[CSize]()

    lib.uv_os_gethostname(buffer, size)
    buffer(255) = 0.toByte
    fromCString(buffer)

  def loopInit: Loop =
    val loop = malloc(lib.uv_loop_size)

    lib.uv_loop_init(loop)
    loop

  private val exitCallbacks = new mutable.HashMap[lib.uv_process_t, ExitCallback]

  private val closeCallbackProcess: lib.uv_close_cb =
    (handle: lib.uv_process_t) =>
      val options: lib.uv_process_options_tp = !(handle.asInstanceOf[Ptr[lib.uv_process_options_tp]])
      val args: Ptr[CString] = options._3
      var i = 0

      while !(args + i) != null do
        free((!(args + i)).asInstanceOf[Ptr[Byte]])
        i += 1

      free(args.asInstanceOf[Ptr[Byte]])
      free(options.asInstanceOf[Ptr[Byte]])
      free(handle)

  private val exitCallback: lib.uv_exit_cb =
    (handle: lib.uv_process_t, exit_status: CLong, term_signal: CInt) =>
      exitCallbacks(handle)(exit_status.toInt, term_signal)
      exitCallbacks -= handle
      lib.uv_close(handle, closeCallbackProcess)

  private val fileCallbacks = new mutable.HashMap[lib.uv_fs_t, FileReq => Unit]

  private def fileCallback(req: lib.uv_fs_t): Unit =
    fileCallbacks(req)(req)
    fileCallbacks -= req
    lib.uv_fs_req_cleanup(req)
    free(req.asInstanceOf[Ptr[Byte]])

  implicit class Loop(val loop: lib.uv_loop_t) extends AnyVal:
    def run(mode: RunMode = RunMode.RUN_DEFAULT): Int = lib.uv_run(loop, mode.value)

    def isAlive: Boolean = lib.uv_loop_alive(loop) > 0

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

    def spawn(program: String, args: IndexedSeq[String], exit_cb: ExitCallback): Int =
      val handle = malloc(lib.uv_handle_size(HandleType.PROCESS.value))
      val options = malloc(sizeof[lib.uv_process_options_t]).asInstanceOf[lib.uv_process_options_tp]

      !handle.asInstanceOf[Ptr[lib.uv_process_options_tp]] = options

      for i <- 0 until sizeof[lib.uv_process_options_t].toInt do !(options.asInstanceOf[Ptr[Byte]] + i) = 0.toByte

      val argsArray = malloc((args.length + 2).toUInt * sizeof[CString]).asInstanceOf[Ptr[CString]]
      val file = allocString(program)

      argsArray(0) = file

      for i <- 1 to args.length do argsArray(i) = allocString(args(i - 1))

      argsArray(args.length + 1) = null
      options._1 = exitCallback
      options._2 = file
      options._3 = argsArray
      exitCallbacks(handle) = exit_cb

      lib.uv_spawn(loop, handle, options)

    def open(
        path: String,
        flags: Int,
        mode: Int,
        cb: FileReq => Unit,
    ): Int =
      val req = allocfs

      Zone { implicit z =>
        fileCallbacks(req) = cb
        checkError(lib.uv_fs_open(loop, req, toCString(path), flags, mode, fileCallback), "uv_fs_open")
      }

  end Loop

  private def allocfs = malloc(lib.uv_req_size(ReqType.FS.value)).asInstanceOf[lib.uv_fs_t]

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

  implicit class Buffer(val buf: lib.uv_buf_tp) extends AnyVal:
    def apply(idx: Int): Int = !(buf._1 + idx) & 0xff

    def update(idx: Int, b: Int): Unit = buf._1(idx) = b.toByte

    def alloc(size: Int): Unit =
      val s = size.toUInt

      buf._1 = malloc(s)
      buf._2 = s

    def size: Int = buf._2.toInt

    def dispose(): Unit =
      if buf._1 != null then free(buf._1)

    def data(len: Int = size): Seq[Int] = for i <- 0 until (len min size) yield apply(i)

    def string(len: Int = size): String = data(len) map (_.toChar) mkString

  type ConnectionCallback = (TCP, Int) => Unit

  private val connectionCallbacks = new mutable.LongMap[ /*lib.uv_tcp_t,*/ ConnectionCallback]

  private val connectionCallback: lib.uv_connection_cb = (tcp: lib.uv_tcp_t, status: CInt) =>
    connectionCallbacks(tcp.toLong)(tcp, checkError(status, "uv_connection_cb"))

//  val ALLOC_SIZE: CUnsignedInt = 1024.toUInt
//
//  type AllocCallback = (TCP, Int, Buffer) => Unit
//
//  private val allocCallbacks = new mutable.HashMap[lib.uv_tcp_t, AllocCallback]

  private val allocCallback: lib.uv_alloc_cb = (tcp: lib.uv_tcp_t, size: CSize, buf: lib.uv_buf_tp) =>
//    allocCallbacks(tcp)(tcp, size.toInt, buf)
    buf.alloc(size.toInt)

//  type ReadCallback = (TCP, Int, Buffer) => Unit
  type ReadCallback = (TCP, Int, Buffer) => Unit

  private val readCallbacks = new mutable.HashMap[lib.uv_tcp_t, ReadCallback]

  private val readCallback: lib.uv_read_cb = (stream: lib.uv_stream_t, size: CSSize, buf: lib.uv_buf_tp) =>
    readCallbacks(stream)(stream, size.toInt, buf)
    if buf._1 != null then free(buf._1)

  private val writeCallback: lib.uv_write_cb =
    (req: lib.uv_write_t, status: Int) =>
      val buf = (!req).asInstanceOf[lib.uv_buf_tp]

      free(buf._1)
      free(buf.asInstanceOf[Ptr[Byte]])
      free(req.asInstanceOf[Ptr[Byte]])

  type ShutdownCallback = TCP => Unit

  private val shutdownCallbacks = new mutable.HashMap[lib.uv_shutdown_t, ShutdownCallback]

  private val shutdownCallback: lib.uv_shutdown_cb =
    (req: lib.uv_shutdown_t, status: Int) =>
      val handle = (!req).asInstanceOf[lib.uv_tcp_t]

      shutdownCallbacks(req)(handle)
      shutdownCallbacks -= req
      free(req.asInstanceOf[Ptr[Byte]])

//  type CloseCallbackTCP = TCP => Unit
//
//  private val closeCallbacksTCP = new mutable.HashMap[lib.uv_tcp_t, CloseCallbackTCP]

  private val closeCallbackTCP: lib.uv_close_cb =
    (handle: lib.uv_tcp_t) =>
//      closeCallbacksTCP(handle)(handle)
//      closeCallbacksTCP -= handle
      free(handle)

  implicit class TCP(val handle: lib.uv_tcp_t) extends AnyVal:
    def bind(ip: String, port: Int, flags: Int): Int = Zone { implicit z =>
      val socketAddress = stackalloc[Byte](extern.SOCKADDR_IN_SIZE).asInstanceOf[lib.sockaddr_inp]

      checkError(lib.uv_ip4_addr(toCString(ip), port, socketAddress), "uv_ip4_addr")
      checkError(lib.uv_tcp_bind(handle, socketAddress, flags), "uv_tcp_bind")
    }

    def listen(backlog: Int, cb: ConnectionCallback): Int =
      connectionCallbacks(handle.toLong) = cb
      checkError(lib.uv_listen(handle, backlog, connectionCallback), "uv_tcp_listen")

    def accept(client: TCP): Int = checkError(lib.uv_accept(handle, client.handle), "uv_accept")

    def readStart( /*alloc_cb: AllocCallback,*/ read_cb: ReadCallback): Int =
//      allocCallbacks(handle) = alloc_cb
      readCallbacks(handle) = read_cb
      checkError(lib.uv_read_start(handle, allocCallback, readCallback), "uv_read_start")

    def readStop: Int =
//      allocCallbacks -= handle
      readCallbacks -= handle
      checkError(lib.uv_read_stop(handle), "uv_read_start")

    def isReadable: Boolean = lib.uv_is_readable(handle) > 0

    def isWritable: Boolean = lib.uv_is_writable(handle) > 0

    def write(data: collection.IndexedSeq[Byte]): Int =
      val req = malloc(lib.uv_req_size(ReqType.WRITE.value)).asInstanceOf[lib.uv_write_t]
      val buffer = malloc(sizeof[lib.uv_buf_t]).asInstanceOf[lib.uv_buf_tp]
      val len = data.length.toUInt
      val base = malloc(len)

      for i <- data.indices do base(i) = data(i)

      buffer._1 = base
      buffer._2 = len
      !req = buffer.asInstanceOf[Ptr[Byte]]
      checkError(lib.uv_write(req, handle, buffer, 1.toUInt, writeCallback), "uv_write")

    def shutdown(cb: ShutdownCallback): Int =
      val req = malloc(lib.uv_req_size(ReqType.SHUTDOWN.value)).asInstanceOf[lib.uv_shutdown_t]

      !req = handle
      shutdownCallbacks(req) = cb
      checkError(lib.uv_shutdown(req, handle, shutdownCallback), "uv_shutdown")

    def close( /*cb: CloseCallbackTCP*/ ): Unit =
//      closeCallbacksTCP(handle) = cb
      lib.uv_close(handle, closeCallbackTCP)

    def isClosing: Boolean = lib.uv_is_closing(handle) > 0

    def dispose(): Unit = free(handle)

  type ExitCallback = (Int, Int) => Unit

  implicit class Process(val proc: lib.uv_process_t) extends AnyVal

  def allocString(s: String): CString =
    val bytes = s.getBytes(scala.io.Codec.UTF8.charSet)
    val cstr = malloc((bytes.length + 1).toULong)
    var c = 0

    while c < bytes.length do
      !(cstr + c) = bytes(c)
      c += 1

    !(cstr + c) = 0.toByte
    cstr
  end allocString

  implicit class File(val file: lib.uv_file) extends AnyVal

  implicit class FileReq(val req: lib.uv_fs_t) extends AnyVal:
    def getResult: Int = lib.uv_fs_get_result(req).toInt
