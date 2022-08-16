package io.github.spritzsn.libuv.extern

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

  def uv_strerror(err: CInt): CString = extern

  def uv_err_name(err: CInt): CString = extern

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

  def uv_loop_alive(loop: uv_loop_t): CInt = extern

  //
  // uv_handle_t — Base handle
  //

  type uv_handle_t = Ptr[Byte]
  type uv_handle_type = CInt
  type uv_alloc_cb = CFuncPtr3[uv_handle_t, CSize, uv_buf_tp, Unit]

  def uv_is_active(handle: uv_handle_t): CInt = extern

  def uv_is_closing(handle: uv_handle_t): CInt = extern

  type uv_close_cb = CFuncPtr1[uv_handle_t, Unit]

  def uv_close(handle: uv_handle_t, close_cb: uv_close_cb): Unit = extern

  def uv_handle_size(typ: uv_handle_type): CSize = extern

  def uv_fileno(handle: uv_handle_t, fd: uv_os_fd_tp): CInt = extern

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

  def uv_prepare_init(loop: uv_loop_t, handle: uv_prepare_t): CInt = extern

  def uv_prepare_start(handle: uv_prepare_t, cb: uv_prepare_cb): CInt = extern

  def uv_prepare_stop(handle: uv_prepare_t): CInt = extern

  //
  // uv_check_t — Check handle
  //

  type uv_check_t = Ptr[Byte]
  type uv_check_cb = CFuncPtr1[uv_check_t, Unit]

  def uv_check_init(loop: uv_loop_t, handle: uv_check_t): CInt = extern

  def uv_check_start(handle: uv_check_t, cb: uv_check_cb): CInt = extern

  def uv_check_stop(handle: uv_check_t): CInt = extern

  //
  // uv_idle_t — Idle handle
  //

  type uv_idle_t = Ptr[Byte]
  type uv_idle_cb = CFuncPtr1[uv_idle_t, Unit]

  def uv_idle_init(loop: uv_loop_t, handle: uv_idle_t): CInt = extern

  def uv_idle_start(handle: uv_idle_t, cb: uv_idle_cb): CInt = extern

  def uv_idle_stop(handle: uv_idle_t): CInt = extern

  //
  // uv_async_t — Async handle
  //

  type uv_async_t = Ptr[Byte]
  type uv_async_cb = CFuncPtr1[uv_async_t, Unit]

  def uv_async_init(loop: uv_loop_t, handle: uv_async_t, cb: uv_async_cb): CInt = extern

  def uv_async_send(handle: uv_async_t): CInt = extern

  //
  // uv_process_t — Process handle
  //

  type uv_process_t = Ptr[Byte]
  type uv_process_options_t =
    CStruct10[uv_exit_cb, CString, Ptr[CString], Ptr[CString], CString, CUnsignedInt, CInt, Ptr[
      uv_stdio_container_t,
    ], CUnsignedInt, CUnsignedInt]
  type uv_process_options_tp = Ptr[uv_process_options_t]
  type uv_exit_cb = CFuncPtr3[uv_process_t, CLong, CInt, Unit]
  type uv_process_flags = CInt
  type uv_stdio_container_t = CStruct2[CInt, Ptr[Byte]]
  type uv_stdio_flags = CInt

  def uv_spawn(loop: uv_loop_t, handle: uv_process_t, options: Ptr[uv_process_options_t]): CInt = extern

  //
  // uv_stream_t — Stream handle
  //

  type uv_stream_t = Ptr[Byte]
  type uv_shutdown_t = Ptr[Ptr[Byte]]
  type uv_write_t = Ptr[Ptr[Byte]]
  type uv_connection_cb = CFuncPtr2[uv_stream_t, CInt, Unit]
  type uv_read_cb = CFuncPtr3[uv_stream_t, CSSize, uv_buf_tp, Unit]
  type uv_write_cb = CFuncPtr2[uv_write_t, Int, Unit]
  type uv_shutdown_cb = CFuncPtr2[uv_shutdown_t, Int, Unit]

  def uv_shutdown(req: uv_shutdown_t, handle: uv_stream_t, cb: uv_shutdown_cb): CInt = extern

  def uv_listen(handle: uv_stream_t, backlog: CInt, db: uv_connection_cb): CInt = extern

  def uv_accept(server: uv_stream_t, client: uv_stream_t): CInt = extern

  def uv_read_start(stream: uv_stream_t, alloc_cb: uv_alloc_cb, read_cb: uv_read_cb): CInt = extern

  def uv_write(
      req: uv_write_t,
      handle: uv_stream_t,
      bufs: uv_buf_tp,
      nbufs: CUnsignedInt,
      db: uv_write_cb,
  ): CInt = extern

  def uv_read_stop(handle: uv_stream_t): CInt = extern

  def uv_is_readable(handle: uv_stream_t): CInt = extern

  def uv_is_writable(handle: uv_stream_t): CInt = extern

  //
  // uv_tcp_t — TCP handle
  //

  type uv_tcp_t = Ptr[Byte]

  def uv_tcp_init(loop: uv_loop_t, handle: uv_tcp_t): CInt = extern

  def uv_tcp_bind(tcp_handle: uv_tcp_t, addr: sockaddr_inp, flags: CInt): CInt = extern

  //
  // Miscellaneous utilities
  //

  type uv_buf_t = CStruct2[Ptr[Byte], CSize]
  type uv_buf_tp = Ptr[uv_buf_t]
  type uv_file = CInt
  type uv_os_fd_t = CInt
  type uv_os_fd_tp = Ptr[uv_os_fd_t]
  type uv_pid_t = CInt
  type sockaddr_in = CStruct0 // netinet/in.h
  type sockaddr_inp = Ptr[sockaddr_in]

  def uv_ip4_addr(ip: CString, port: CInt, addr: sockaddr_inp): CInt = extern

  def uv_ip4_name(src: sockaddr_inp, dst: CString, size: CSize): CInt = extern

  //

  type PollHandle = Ptr[Ptr[Byte]]
  type TTYHandle = Ptr[Byte]
  type Connection = Ptr[Byte]
  type PollCB = CFuncPtr3[PollHandle, Int, Int, Unit]

  type RWLock = Ptr[Byte]

  def uv_tty_init(loop: uv_loop_t, handle: TTYHandle, fd: CInt, readable: CInt): CInt = extern

  def uv_pipe_init(loop: uv_loop_t, handle: uv_stream_t, ipc: CInt): CInt = extern

  def uv_pipe_open(handle: uv_stream_t, fd: CInt): CInt = extern

  def uv_pipe_bind(handle: uv_stream_t, socketName: CString): CInt = extern

  def uv_poll_init_socket(loop: uv_loop_t, handle: PollHandle, socket: Ptr[Byte]): CInt = extern

  def uv_poll_start(handle: PollHandle, events: CInt, cb: PollCB): CInt = extern

  def uv_poll_stop(handle: PollHandle): CInt = extern

  def uv_guess_handle(fd: CInt): CInt = extern

  type FSReq = Ptr[Ptr[Byte]]
  type FSCB = CFuncPtr1[FSReq, Unit]

  def uv_fs_open(loop: uv_loop_t, req: FSReq, path: CString, flags: CInt, mode: CInt, cb: FSCB): CInt = extern

  def uv_fs_read(
      loop: uv_loop_t,
      req: FSReq,
      fd: CInt,
      bufs: uv_buf_tp,
      numBufs: CInt,
      offset: Long,
      fsCB: FSCB,
  ): CInt =
    extern

  def uv_fs_write(
      loop: uv_loop_t,
      req: FSReq,
      fd: CInt,
      bufs: uv_buf_tp,
      numBufs: CInt,
      offset: Long,
      fsCB: FSCB,
  ): CInt =
    extern

  def uv_fs_close(loop: uv_loop_t, req: FSReq, fd: CInt, fsCB: FSCB): CInt = extern

  def uv_req_cleanup(req: FSReq): Unit = extern

  def uv_fs_get_result(req: FSReq): CInt = extern

  def uv_fs_get_ptr(req: FSReq): Ptr[Byte] = extern

  //  def uv_queue_work(loop: uv_loop_t, req: WorkReq, work_cb: WorkCB, after_work_cb: AfterWorkCB): CInt = extern
  //
  //  def uv_rwlock_init(rwlock: RWLock): CInt = extern
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

private[libuv] final val SOCKADDR_IN_SIZE: CUnsignedInt = 16.toUInt
