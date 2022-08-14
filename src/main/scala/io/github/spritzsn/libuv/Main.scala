package io.github.spritzsn.libuv

@main def run(): Unit =
//  println("wait")
//
//  var count = 0
//
//  defaultLoop.timer.start(
//    t => {
//      println("boom")
//      count += 1
//
//      if count == 5 then t.stop
//    },
//    1000,
//    500,
//  )
//  println(defaultLoop.run())

  val server = defaultLoop.tcp

  server.bind("0.0.0.0", 3000, 0)

  def connectionCallback(handle: TCP, status: Int): Unit =
    val client = defaultLoop.tcp

    handle.accept(client)

    def readCallback(client: TCP, size: Int, buf: Buffer): Unit =
      println(buf.string(size))
      client.write(
        s"""HTTP/1.0 200 OK\r
           |Content-Type: text/plain\r
           |Content-Length: 12\r
           |\r
           |hello world
           |""".stripMargin.getBytes,
      )
      client.shutdown

    client.readStart(readCallback)

  server.listen(100, connectionCallback)
  println("listening")
  defaultLoop.run()
