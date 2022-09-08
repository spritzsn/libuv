package io.github.spritzsn

import scala.collection.mutable
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*
import scala.scalanative.libc.stdlib
import java.util.IdentityHashMap
import scala.collection.mutable.ListBuffer
import scala.io.Codec
import scala.scalanative.posix.fcntl
import scala.scalanative.posix.netdb

package object libuv:

  enum Platform:
    case Nix, Mac, Win, Other

  val platform: Platform =
    val os = sys.props("os.name").toLowerCase

    if os.startsWith("mac") then Platform.Mac
    else if os.startsWith("windows") then Platform.Win
    else if os.startsWith("linux") | os.contains("unix") | os.contains("bsd") then Platform.Nix
    else Platform.Other

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

  implicit class RunMode(val value: lib.uv_run_mode) extends AnyVal

  object RunMode:
    final val RUN_DEFAULT = new RunMode(0)
    final val RUN_ONCE = new RunMode(1)
    final val RUN_NOWAIT = new RunMode(2)

  val O_RDONLY: Int = fcntl.O_RDONLY
  val O_WRONLY: Int = fcntl.O_WRONLY
  val O_RDWR: Int = fcntl.O_RDWR

  val O_CREAT: Int = if sys.props("os.name") == "Mac OS X" then 0x200 else 0x40
  val O_TRUNC: Int = fcntl.O_TRUNC
  val O_APPEND: Int = fcntl.O_APPEND
  val O_SYNC: Int = fcntl.O_SYNC
  val O_DSYNC: Int = 0x1000
  val O_EXCL: Int = fcntl.O_EXCL

  private def o(n: Int): Int = Integer.parseInt(n.toString, 8)

  val ALLRW: Int = o(666)
  val S_IRWXU: Int = o(700)
  val S_IRUSR: Int = o(400) //  user has read permission
  val S_IWUSR: Int = o(200) //  user has write permission
  val S_IXUSR: Int = o(100) //  user has execute permission
  val S_IRWXG: Int = o(70) //  group has read, write, and execute permission
  val S_IRGRP: Int = o(40) //  group has read permission
  val S_IWGRP: Int = o(20) //  group has write permission
  val S_IXGRP: Int = o(10) //  group has execute permission
  val S_IRWXO: Int = 7 //  others have read, write, and execute permission
  val S_IROTH: Int = 4 //  others have read permission
  val S_IWOTH: Int = 2 //  others have write permission
  val S_IXOTH: Int = 1 //  others have execute permission
  val S_ISUID: Int = o(4000) //  set-user-ID bit
  val S_ISGID: Int = o(2000) //  set-group-ID bit (see inode(7)).
  val S_ISVTX: Int = o(1000) //  sticky bit (see inode(7)).

  val UV_READABLE = 1
  val UV_WRITABLE = 2
  val UV_DISCONNECT = 4
  val UV_PRIORITIZED = 8

  def errorMessage(v: Int, label: String): String = s"$label error: ${errName(v)}: ${strError(v)}"

  def checkError(v: Int, label: String): Int = if v < 0 then sys.error(errorMessage(v, label)) else v

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

  def getTimeOfDay: (Long, Int) =
    val tv = stackalloc[lib.uv_timeval64_t]()

    lib.uv_gettimeofday(tv)
    (tv._1, tv._2)

  def loopInit: Loop =
    val loop = stdlib.malloc(lib.uv_loop_size)

    lib.uv_loop_init(loop)
    loop

  private def free(p: Ptr[_]): Unit = stdlib.free(p.asInstanceOf[Ptr[Byte]])

  private inline def malloc[T](inline n: CSize = 1.toULong)(using Tag[T]): Ptr[T] =
    stdlib.malloc(sizeof[T] * n.toULong).asInstanceOf[Ptr[T]]

  private def mallocReq[T](typ: ReqType): T = stdlib.malloc(lib.uv_req_size(typ.value)).asInstanceOf[T]

  private def mallocHandle[T](typ: HandleType): T = stdlib.malloc(lib.uv_handle_size(typ.value)).asInstanceOf[T]

  private val exitCallbacks = new mutable.HashMap[lib.uv_process_t, ExitCallback]

  private val closeCallbackProcess: lib.uv_close_cb =
    (handle: lib.uv_process_t) =>
      val options: lib.uv_process_options_tp = !(handle.asInstanceOf[Ptr[lib.uv_process_options_tp]])
      val args: Ptr[CString] = options._3
      var i = 0

      while !(args + i) != null do
        free((!(args + i)))
        i += 1

      free(args)
      free(options)
      free(handle)

  private val exitCallback: lib.uv_exit_cb =
    (handle: lib.uv_process_t, exit_status: CLong, term_signal: CInt) =>
      exitCallbacks get handle foreach { cb =>
        cb(exit_status.toInt, term_signal)
        exitCallbacks -= handle
      }
      lib.uv_close(handle, closeCallbackProcess)

  private val fileCallbacks = new mutable.HashMap[lib.uv_fs_t, FileReq => Unit]

  private def fileCallback(req: lib.uv_fs_t): Unit =
    fileCallbacks get req foreach { cb =>
      cb(req)
      fileCallbacks -= req
    }
    lib.uv_fs_req_cleanup(req)
    free(req)

  private def fileCallbackWithDispose(req: lib.uv_fs_t): Unit =
    val buf = req.buffer

    fileCallback(req)
    buf.dispose()

  type PollCallback = (Poll, Int, Int) => Unit

  private val pollCallbacks = new mutable.HashMap[lib.uv_poll_t, PollCallback]

  private def pollCallback(handle: lib.uv_poll_t, status: CInt, events: CInt): Unit =
    pollCallbacks get handle foreach (_(handle, status, events))

  type GetAddrInfoCallback = (Int, List[AddrInfo]) => Unit
  case class AddrInfo(family: Int, ip: String, canonicalName: String)

  private val getaddrinfoCallbacks = new mutable.HashMap[lib.uv_getaddrinfo_t, GetAddrInfoCallback]

  private def getaddrinfoCallback(req: lib.uv_getaddrinfo_t, status: CInt, res: lib.addrinfop): Unit =
    val buf = new ListBuffer[AddrInfo]

    getaddrinfoCallbacks get req foreach (_(status, buf.toList))
    free(req)

  implicit class Loop(val loop: lib.uv_loop_t) extends AnyVal:
    def run(mode: RunMode = RunMode.RUN_DEFAULT): Int = lib.uv_run(loop, mode.value)

    def isAlive: Boolean = lib.uv_loop_alive(loop) > 0

    def updateTime(): Unit = lib.uv_update_time(loop)

    def now: Long = lib.uv_now(loop)

    def timer: Timer =
      val timer = mallocHandle[lib.uv_timer_t](HandleType.TIMER)

      checkError(lib.uv_timer_init(loop, timer), "uv_timer_init")
      timer

    def prepare: Prepare =
      val prepare = mallocHandle[lib.uv_prepare_t](HandleType.PREPARE)

      checkError(lib.uv_prepare_init(loop, prepare), "uv_prepare_init")
      prepare

    def check: Check =
      val check = mallocHandle[lib.uv_check_t](HandleType.CHECK)

      checkError(lib.uv_check_init(loop, check), "uv_check_init")
      check

    def idle: Idle =
      val idle = mallocHandle[lib.uv_idle_t](HandleType.IDLE)

      checkError(lib.uv_idle_init(loop, idle), "uv_idle_init")
      idle

    def tcp: TCP =
      val tcp = mallocHandle[lib.uv_tcp_t](HandleType.TCP)

      checkError(lib.uv_tcp_init(loop, tcp), "uv_tcp_init")
      tcp

    def spawn(program: String, args: IndexedSeq[String], exit_cb: ExitCallback = null): Int =
      val handle = mallocHandle[lib.uv_process_t](HandleType.PROCESS)
      val options = stdlib.malloc(sizeof[lib.uv_process_options_t]).asInstanceOf[lib.uv_process_options_tp]

      !handle.asInstanceOf[Ptr[lib.uv_process_options_tp]] = options

      for i <- 0 until sizeof[lib.uv_process_options_t].toInt do !(options.asInstanceOf[Ptr[Byte]] + i) = 0.toByte

      val argsArray = stdlib.malloc((args.length + 2).toUInt * sizeof[CString]).asInstanceOf[Ptr[CString]]
      val file = allocString(program)

      argsArray(0) = file

      for i <- 1 to args.length do argsArray(i) = allocString(args(i - 1))

      argsArray(args.length + 1) = null
      options._1 = exitCallback
      options._2 = file
      options._3 = argsArray
      if exit_cb != null then exitCallbacks(handle) = exit_cb
      lib.uv_spawn(loop, handle, options)

    def open(
        path: String,
        flags: Int,
        mode: Int,
        cb: FileReq => Unit,
    ): Int =
      val req = allocfs

      fileCallbacks(req) = cb
      Zone { implicit z =>
        checkError(lib.uv_fs_open(loop, req, toCString(path), flags, mode, fileCallback), "uv_fs_open")
      }

    def read(file: Int, cb: FileReq => Unit): Int =
      val req = allocfs
      val buf = Buffer(4096)

      fileCallbacks(req) = cb
      !req = buf.buf
      checkError(lib.uv_fs_read(loop, req, file, buf.buf, 1, -1, fileCallbackWithDispose), "uv_fs_read")

    def write(data: collection.IndexedSeq[Byte], offset: Int, file: Int, cb: FileReq => Unit): Int =
      val req = allocfs
      val buf = Buffer(data.length)

      buf.write(data, offset)
      fileCallbacks(req) = cb
      !req = buf.buf
      checkError(lib.uv_fs_write(loop, req, file, buf.buf, 1, -1, fileCallbackWithDispose), "uv_fs_write")

    def close(file: Int, cb: FileReq => Unit): Int =
      val req = allocfs

      fileCallbacks(req) = cb
      checkError(lib.uv_fs_close(loop, req, file, fileCallback), "uv_fs_close")

    def poll(fd: Int): Poll =
      val handle = mallocHandle[lib.uv_poll_t](HandleType.POLL.value)

      checkError(lib.uv_poll_init(loop, handle, fd), "uv_poll_init")
      handle

    def getAddrInfo(
        getaddrinfo_cb: GetAddrInfoCallback,
        node: String,
        service: String,
        family: Int,
    ): Int =
      val req = mallocReq[lib.uv_getaddrinfo_t](ReqType.GETADDRINFO)
      val hints = malloc[netdb.addrinfo]()

      Zone { implicit z =>
        lib.uv_getaddrinfo(loop, req, getaddrinfoCallback, toCString(node), toCString(service), hints)
      }
  end Loop

  private def allocfs = mallocReq[lib.uv_fs_t](ReqType.FS)

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

  implicit class Poll(val handle: lib.uv_poll_t) extends AnyVal:
    def start(events: Int, callback: PollCallback): Int =
      pollCallbacks(handle) = callback
      lib.uv_poll_start(handle, events, pollCallback)

    def stop: Int = lib.uv_poll_stop(handle)

    def dispose(): Unit =
      pollCallbacks -= handle
      free(handle)

  object Buffer:
    def apply(size: Int): Buffer =
      val buf: Buffer = stdlib.malloc(16.toUInt)
      val base = stdlib.malloc(size.toUInt)

      !buf.baseptr = base
      !buf.lenptr = size.toULong
      buf

  implicit class Buffer(val buf: lib.uv_buf_t) extends AnyVal:
    private def baseptr = (buf + (if platform == Platform.Win then 8 else 0)).asInstanceOf[Ptr[Ptr[Byte]]]

    private def lenptr = (buf + (if platform == Platform.Win then 0 else 8)).asInstanceOf[Ptr[CSize]]

    def apply(idx: Int): Byte = !(!baseptr + idx)

    def update(idx: Int, b: Int): Unit = !(!baseptr + idx) = b.toByte

    private[libuv] def alloc(size: CSize): Unit =
      !baseptr = malloc(size)
      !lenptr = size

    def size: Int = (!lenptr).toInt

    def freebase(): Unit = if !baseptr != null then free(!baseptr)

    def dispose(): Unit =
      freebase()
      free(buf)

    def read(buf: scala.collection.mutable.Buffer[Byte], len: Int): Unit =
      var i = 0

      while i < len do
        buf += apply(i)
        i += 1

    def read(len: Int): Array[Byte] =
      val arr = new Array[Byte](len)
      var i = 0

      while i < len do
        arr(i) = apply(i)
        i += 1

      arr

    def write(data: collection.IndexedSeq[Byte], offset: Int): Unit =
      val base = !baseptr
      var i = 0
      var j = offset

      while i < size do
        base(i) = data(j)
        i += 1
        j += 1

    def string(len: Int, codec: Codec = Codec.UTF8): String = new String(read(len), codec.charSet)

  type ConnectionCallback = (TCP, Int) => Unit

  private val connectionCallbacks = new mutable.LongMap[ /*lib.uv_tcp_t,*/ ConnectionCallback]

  private val connectionCallback: lib.uv_connection_cb = (tcp: lib.uv_tcp_t, status: CInt) =>
    connectionCallbacks(tcp.toLong)(tcp, checkError(status, "uv_connection_cb"))

//  val ALLOC_SIZE: CUnsignedInt = 1024.toUInt
//
//  type AllocCallback = (TCP, Int, Buffer) => Unit
//
//  private val allocCallbacks = new mutable.HashMap[lib.uv_tcp_t, AllocCallback]

  private val allocCallback: lib.uv_alloc_cb = (tcp: lib.uv_tcp_t, size: CSize, buf: lib.uv_buf_t) =>
    new Buffer(buf).alloc(size)

  type ReadCallback = (TCP, Int, Buffer) => Unit

  private val readCallbacks = new mutable.HashMap[lib.uv_tcp_t, ReadCallback]

  private val readCallback: lib.uv_read_cb = (stream: lib.uv_stream_t, size: CSSize, buf: lib.uv_buf_t) =>
    readCallbacks(stream)(stream, size.toInt, buf)
    new Buffer(buf).freebase()

  private val writeCallback: lib.uv_write_cb =
    (req: lib.uv_write_t, status: Int) =>
      val buf = new Buffer(!req)

      buf.dispose()
      free(req)

  type ShutdownCallback = TCP => Unit

  private val shutdownCallbacks = new mutable.HashMap[lib.uv_shutdown_t, ShutdownCallback]

  private val shutdownCallback: lib.uv_shutdown_cb =
    (req: lib.uv_shutdown_t, status: Int) =>
      val handle = (!req).asInstanceOf[lib.uv_tcp_t]

      shutdownCallbacks(req)(handle)
      shutdownCallbacks -= req
      free(req)

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
      val socketAddress = stackalloc[Byte](lib.libuv_sockaddr_in_size).asInstanceOf[lib.sockaddr_inp]

      checkError(lib.uv_ip4_addr(toCString(ip), port, socketAddress), "uv_ip4_addr")
      checkError(lib.uv_tcp_bind(handle, socketAddress, flags), "uv_tcp_bind")
    }

    def getSockName: String =
      val sockaddr = stackalloc[Byte](lib.libuv_sockaddr_storage_size).asInstanceOf[lib.sockaddrp]
      val namelen = stackalloc[CInt]()

      !namelen = lib.libuv_sockaddr_storage_size.toInt
      checkError(lib.uv_tcp_getsockname(handle, sockaddr, namelen), "uv_tcp_getsockname")

      val dst = stackalloc[CChar](100)

      checkError(lib.uv_ip4_name(sockaddr, dst, 100.toUInt), "uv_ip4_name")
      fromCString(dst)

    def getPeerName: String =
      val sockaddr = stackalloc[Byte](lib.libuv_sockaddr_storage_size).asInstanceOf[lib.sockaddrp]
      val namelen = stackalloc[CInt]()

      !namelen = lib.libuv_sockaddr_storage_size.toInt
      checkError(lib.uv_tcp_getpeername(handle, sockaddr, namelen), "uv_tcp_getpeername")

      val dst = stackalloc[CChar](100)

      checkError(lib.uv_ip4_name(sockaddr, dst, 100.toUInt), "uv_ip4_name")
      fromCString(dst)

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
      val req = mallocReq[lib.uv_write_t](ReqType.WRITE)
      val buffer = Buffer(data.length)

      buffer.write(data, 0)
      !req = buffer.buf
      checkError(lib.uv_write(req, handle, buffer.buf, 1.toUInt, writeCallback), "uv_write")

    def connect(data: collection.IndexedSeq[Byte]): Int =
      val req = mallocReq[lib.uv_connect_t](ReqType.CONNECT)
      val buffer = Buffer(data.length)

      buffer.write(data, 0)
      !req = buffer.buf
      checkError(lib.uv_write(req, handle, buffer.buf, 1.toUInt, writeCallback), "uv_connect")

    def shutdown(cb: ShutdownCallback): Int =
      val req = mallocReq[lib.uv_shutdown_t](ReqType.SHUTDOWN)

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
    val cstr = stdlib.malloc((bytes.length + 1).toULong)
    var c = 0

    while c < bytes.length do
      !(cstr + c) = bytes(c)
      c += 1

    !(cstr + c) = 0.toByte
    cstr
  end allocString

  implicit class FileReq(val req: lib.uv_fs_t) extends AnyVal:
    def getResult: Int = lib.uv_fs_get_result(req).toInt

    def buffer: Buffer = !req
