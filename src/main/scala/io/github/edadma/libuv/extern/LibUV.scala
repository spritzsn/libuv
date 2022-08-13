package io.github.edadma.libuv.extern

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@link("uv")
@extern
object LibUV:

  //
  // Version-checking macros and functions
  //

  def uv_version: CUnsignedInt = extern

  def uv_version_string: CString = extern

  //
  // Error handling
  //

  def uv_strerror(err: Int): CString = extern

  def uv_err_name(err: Int): CString = extern

  //
  // uv_loop_t — Event loop
  //

  type uv_loop_t = Ptr[Byte]

  def uv_loop_init(loop: uv_loop_t): CInt = extern

  def uv_default_loop: uv_loop_t = extern

  def uv_loop_size: CSize = extern

  type uv_run_mode = CInt

  def uv_run(loop: uv_loop_t, mode: uv_run_mode): CInt = extern

  def uv_update_time(loop: uv_loop_t): Unit = extern

  def uv_now(loop: uv_loop_t): CLong = extern

  //
  // uv_handle_t — Base handle
  //

  type uv_handle_t = Ptr[Byte]
  type uv_handle_type = CInt

  def uv_is_active(handle: uv_handle_t): CInt = extern

  def uv_is_closing(handle: uv_handle_t): CInt = extern

  type uv_close_cb = CFuncPtr1[uv_handle_t, Unit]

  def uv_close(handle: uv_handle_t, close_cb: uv_close_cb): Unit = extern

  def uv_handle_size(typ: uv_handle_type): CSize = extern

  def uv_fileno(handle: uv_handle_t, fd: uv_os_fd_tp): Int = extern

  def uv_handle_type_name(typ: uv_handle_type): CString = extern

  //
  // uv_req_t — Base request
  //

  type uv_req_t = Ptr[Byte]
  type uv_req_type = CInt

  def uv_cancel(req: uv_req_t): CInt = extern

  def uv_req_size(typ: uv_req_type): CSize = extern

  //
  // uv_timer_t — Timer handle
  //

  type uv_timer_t = Ptr[Byte]
  type uv_timer_cb = CFuncPtr1[uv_timer_t, Unit]

  def uv_timer_init(loop: uv_loop_t, handle: uv_timer_t): CInt = extern

  def uv_timer_start(handle: uv_timer_t, cb: uv_timer_cb, timeout: CLong, repeat: CLong): CInt = extern

  def uv_timer_stop(handle: uv_timer_t): CInt = extern

  //
  // uv_prepare_t — Prepare handle
  //

  type uv_prepare_t = Ptr[Byte]
  type uv_prepare_cb = CFuncPtr1[uv_prepare_t, Unit]

  def uv_prepare_init(loop: uv_loop_t, handle: uv_prepare_t): Int = extern

  def uv_prepare_start(handle: uv_prepare_t, cb: uv_prepare_cb): Int = extern

  def uv_prepare_stop(handle: uv_prepare_t): Unit = extern

  //

  type PipeHandle = Ptr[Byte]
  type PollHandle = Ptr[Ptr[Byte]]
  type TCPHandle = Ptr[Byte]
  type ProcessHandle = Ptr[Byte]
  type StdioContainer = CStruct2[CInt, Ptr[Byte]]
  type ProcessOptions = CStruct10[ExitCB, CString, Ptr[CString], Ptr[CString], CString, CUnsignedInt, CInt, Ptr[
    StdioContainer,
  ], CUnsignedInt, CUnsignedInt]
  type ExitCB = CFuncPtr3[ProcessHandle, CLong, CInt, Unit]
  type TTYHandle = Ptr[Byte]
  type Buffer = CStruct2[Ptr[Byte], CSize]
  type WriteReq = Ptr[Ptr[Byte]]
  type ShutdownReq = Ptr[Ptr[Byte]]
  type WorkReq = Ptr[Ptr[Byte]]
  type Connection = Ptr[Byte]
  type ConnectionCB = CFuncPtr2[TCPHandle, Int, Unit]
  type AllocCB = CFuncPtr3[TCPHandle, CSize, Ptr[Buffer], Unit]
  type ReadCB = CFuncPtr3[TCPHandle, CSSize, Ptr[Buffer], Unit]
  type WriteCB = CFuncPtr2[WriteReq, Int, Unit]
  type ShutdownCB = CFuncPtr2[ShutdownReq, Int, Unit]
  type PollCB = CFuncPtr3[PollHandle, Int, Int, Unit]
  type WorkCB = CFuncPtr1[WorkReq, Unit]
  type AfterWorkCB = CFuncPtr2[WorkReq, Int, Unit]


  type RWLock = Ptr[Byte]

  def uv_tty_init(loop: uv_loop_t, handle: TTYHandle, fd: Int, readable: Int): Int = extern

  def uv_tcp_init(loop: uv_loop_t, tcp_handle: TCPHandle): Int = extern

  def uv_tcp_bind(tcp_handle: TCPHandle, address: Ptr[Byte], flags: Int): Int = extern

  def uv_ip4_addr(address: CString, port: Int, out_addr: Ptr[Byte]): Int = extern

  def uv_ip4_name(address: Ptr[Byte], s: CString, size: Int): Int = extern

  def uv_pipe_init(loop: uv_loop_t, handle: PipeHandle, ipc: Int): Int = extern

  def uv_pipe_open(handle: PipeHandle, fd: Int): Int = extern

  def uv_pipe_bind(handle: PipeHandle, socketName: CString): Int = extern

  def uv_poll_init_socket(loop: uv_loop_t, handle: PollHandle, socket: Ptr[Byte]): Int = extern

  def uv_poll_start(handle: PollHandle, events: Int, cb: PollCB): Int = extern

  def uv_poll_stop(handle: PollHandle): Int = extern

  def uv_listen(handle: PipeHandle, backlog: Int, callback: ConnectionCB): Int = extern

  def uv_accept(server: PipeHandle, client: PipeHandle): Int = extern

  def uv_read_start(client: PipeHandle, allocCB: AllocCB, readCB: ReadCB): Int = extern

  def uv_write(writeReq: WriteReq, client: PipeHandle, bufs: Ptr[Buffer], numBufs: Int, writeCB: WriteCB): Int = extern

  def uv_read_stop(client: PipeHandle): Int = extern

  def uv_shutdown(shutdownReq: ShutdownReq, client: PipeHandle, shutdownCB: ShutdownCB): Int = extern

  def uv_guess_handle(fd: Int): Int = extern

  type FSReq = Ptr[Ptr[Byte]]
  type FSCB = CFuncPtr1[FSReq, Unit]

  def uv_fs_open(loop: uv_loop_t, req: FSReq, path: CString, flags: Int, mode: Int, cb: FSCB): Int = extern

  def uv_fs_read(loop: uv_loop_t, req: FSReq, fd: Int, bufs: Ptr[Buffer], numBufs: Int, offset: Long, fsCB: FSCB): Int =
    extern

  def uv_fs_write(loop: uv_loop_t, req: FSReq, fd: Int, bufs: Ptr[Buffer], numBufs: Int, offset: Long, fsCB: FSCB): Int =
    extern

  def uv_fs_close(loop: uv_loop_t, req: FSReq, fd: Int, fsCB: FSCB): Int = extern

  def uv_req_cleanup(req: FSReq): Unit = extern

  def uv_fs_get_result(req: FSReq): Int = extern

  def uv_fs_get_ptr(req: FSReq): Ptr[Byte] = extern

  //  def uv_queue_work(loop: uv_loop_t, req: WorkReq, work_cb: WorkCB, after_work_cb: AfterWorkCB): Int = extern
  //
  //  def uv_rwlock_init(rwlock: RWLock): Int = extern
  //
  //  def uv_rwlock_destroy(rwlock: RWLock): Unit = extern
  //
  //  def uv_rwlock_rdlock(rwlock: RWLock): Unit = extern
  //
  //  def uv_rwlock_rdunlock(rwlock: RWLock): Unit = extern
  //
  //  def uv_rwlock_wrlock(rwlock: RWLock): Unit = extern
  //
  //  def uv_rwlock_wrunlock(rwlock: RWLock): Unit = extern

  def uv_spawn(loop: uv_loop_t, handle: ProcessHandle, options: Ptr[ProcessOptions]): CInt = extern

  //
  // Miscellaneous utilities
  //

  type uv_os_fd_t = CStruct0
  type uv_os_fd_tp = Ptr[uv_os_fd_t]
