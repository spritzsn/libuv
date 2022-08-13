package io.github.edadma

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
