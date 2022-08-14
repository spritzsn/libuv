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

      lib.uv_timer_init(loop, timer)
      timer

    def prepare: Prepare =
      val prepare = malloc(lib.uv_handle_size(HandleType.PREPARE.value))

      lib.uv_prepare_init(loop, prepare)
      prepare

    def check: Check =
      val check = malloc(lib.uv_handle_size(HandleType.CHECK.value))

      lib.uv_check_init(loop, check)
      check

    def idle: Idle =
      val idle = malloc(lib.uv_handle_size(HandleType.IDLE.value))

      lib.uv_idle_init(loop, idle)
      idle

  def defaultLoop: Loop = lib.uv_default_loop

  private val timerCallbacks = new mutable.HashMap[lib.uv_timer_t, Timer => Unit]

  private val timerCallback: lib.uv_timer_cb = (t: lib.uv_timer_t) => timerCallbacks(t)(t)

  implicit class Timer(val handle: lib.uv_timer_t) extends AnyVal:
    def start(callback: Timer => Unit, timeout: Long, repeat: Long = 0): Int =
      timerCallbacks(handle) = callback
      lib.uv_timer_start(handle, timerCallback, timeout, repeat)

    def stop: Int = lib.uv_timer_stop(handle)

    def dispose(): Unit = free(handle)
    // todo: add rest of timer methods

  private val prepareCallbacks = new mutable.HashMap[lib.uv_prepare_t, Prepare => Unit]

  private val prepareCallback: lib.uv_prepare_cb = (t: lib.uv_prepare_t) => prepareCallbacks(t)(t)

  implicit class Prepare(val handle: lib.uv_prepare_t) extends AnyVal:
    def start(callback: Prepare => Unit): Int =
      prepareCallbacks(handle) = callback
      lib.uv_prepare_start(handle, prepareCallback)

    def stop: Int = lib.uv_prepare_stop(handle)

    def dispose(): Unit = free(handle)

  private val checkCallbacks = new mutable.HashMap[lib.uv_check_t, Check => Unit]

  private val checkCallback: lib.uv_check_cb = (t: lib.uv_check_t) => checkCallbacks(t)(t)

  implicit class Check(val handle: lib.uv_check_t) extends AnyVal:
    def start(callback: Check => Unit): Int =
      checkCallbacks(handle) = callback
      lib.uv_check_start(handle, checkCallback)

    def stop: Int = lib.uv_check_stop(handle)

    def dispose(): Unit = free(handle)

  private val idleCallbacks = new mutable.HashMap[lib.uv_idle_t, Idle => Unit]

  private val idleCallback: lib.uv_idle_cb = (t: lib.uv_idle_t) => idleCallbacks(t)(t)

  implicit class Idle(val handle: lib.uv_idle_t) extends AnyVal:
    def start(callback: Idle => Unit): Int =
      idleCallbacks(handle) = callback
      lib.uv_idle_start(handle, idleCallback)

    def stop: Int = lib.uv_idle_stop(handle)

    def dispose(): Unit = free(handle)
