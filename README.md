# libuv

![Maven Central](https://img.shields.io/maven-central/v/io.github.edadma/libuv_native0.5_3)
[![Last Commit](https://img.shields.io/github/last-commit/edadma/libuv)](https://github.com/edadma/libuv/commits)
![GitHub](https://img.shields.io/github/license/edadma/libuv)
![Scala Version](https://img.shields.io/badge/Scala-3.8.1-blue.svg)
![Scala Native Version](https://img.shields.io/badge/Scala_Native-0.5.10-blue.svg)

Scala Native bindings for [libuv](https://libuv.org/), the cross-platform asynchronous I/O library used by Node.js.

## Overview

This library provides idiomatic Scala wrappers around the libuv C API, giving you access to:

- **Event loop** - run modes, loop lifecycle, timing
- **TCP** - servers and clients with bind/listen/accept/connect/read/write
- **Timers** - one-shot and repeating timers
- **File I/O** - async open/read/write/close
- **Process** - spawn child processes
- **DNS** - async address resolution
- **Pipes & TTY** - named pipes and terminal handles
- **Handles** - prepare, check, idle, async, poll
- **System info** - hostname, time of day, high-resolution clock, available memory, CPU parallelism
- **Metrics** - event loop idle time

## Prerequisites

- JDK 11 or higher
- sbt 1.12+
- LLVM/Clang
- libuv development library installed on your system:

```bash
# Ubuntu/Debian
sudo apt install libuv1-dev

# macOS
brew install libuv

# Arch
sudo pacman -S libuv
```

## Usage

Add to your `build.sbt`:

```scala
libraryDependencies += "io.github.edadma" %%% "libuv" % "0.0.28"
```

### Quick example: TCP echo server

```scala
import io.github.spritzsn.libuv.*

@main def run(): Unit =
  val loop = defaultLoop
  val server = loop.tcp

  server.bind("0.0.0.0", 3000, 0)
  server.listen(128, (handle, status) =>
    val client = loop.tcp
    handle.accept(client)
    client.readStart((stream, size, buf) =>
      if size > 0 then stream.write(buf.read(size))
      else
        stream.readStop
        stream.shutdown(_.close())
    )
  )
  println("Listening on port 3000")
  loop.run()
```

### Timer

```scala
import io.github.spritzsn.libuv.*

@main def run(): Unit =
  var count = 0
  defaultLoop.timer.start(t =>
    println("tick")
    count += 1
    if count == 5 then t.stop
  , timeout = 1000, repeat = 500)
  defaultLoop.run()
```

### System info

```scala
import io.github.spritzsn.libuv.*

println(s"libuv ${versionString}")
println(s"Host: ${getHostname}")
println(s"CPUs: ${availableParallelism}")
println(s"Available memory: ${availableMemory / (1024 * 1024)} MB")
println(s"Parent PID: ${parentPid}")
```

## API Coverage

| libuv subsystem | Bindings |
|-----------------|----------|
| Event loop | `uv_loop_init`, `uv_default_loop`, `uv_run`, `uv_loop_alive`, `uv_now`, `uv_update_time` |
| TCP | `uv_tcp_init`, `uv_tcp_bind`, `uv_tcp_connect`, `uv_tcp_getsockname`, `uv_tcp_getpeername` |
| Streams | `uv_listen`, `uv_accept`, `uv_read_start`, `uv_read_stop`, `uv_write`, `uv_shutdown`, `uv_is_readable`, `uv_is_writable` |
| Timer | `uv_timer_init`, `uv_timer_start`, `uv_timer_stop` |
| File I/O | `uv_fs_open`, `uv_fs_read`, `uv_fs_write`, `uv_fs_close` |
| DNS | `uv_getaddrinfo`, `uv_freeaddrinfo` |
| Process | `uv_spawn` |
| Pipe | `uv_pipe_init`, `uv_pipe_open`, `uv_pipe_bind`, `uv_pipe_bind2`, `uv_pipe_connect2` |
| TTY | `uv_tty_init` |
| Handles | prepare, check, idle, async, poll |
| Misc | `uv_hrtime`, `uv_gettimeofday`, `uv_clock_gettime`, `uv_available_parallelism`, `uv_get_available_memory`, `uv_os_getppid`, `uv_os_gethostname` |
| Metrics | `uv_metrics_idle_time` |
| Error | `uv_strerror`, `uv_err_name` |

## Building

```bash
sbt compile
sbt test
sbt run
```

## License

ISC
