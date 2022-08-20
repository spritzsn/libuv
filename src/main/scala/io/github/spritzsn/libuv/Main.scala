package io.github.spritzsn.libuv

@main def run(): Unit =
  def opencb(req: File): Unit =
    val openres = req.getResult

    if openres < 0 then println(strError(openres))
    else
      def writecb(req: File): Unit =
        val res = req.getResult

        if res < 0 then println(strError(res))
        else if res > 0 then
          print(req.getResult)
//          defaultLoop.read(openres, readcb)
        else defaultLoop.close(openres)

      defaultLoop.write(Vector("one\ntwo\n".getBytes), openres, writecb)

  defaultLoop.open("asdf", O_WRONLY|O_CREAT, 0, opencb)
  defaultLoop.run()

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

//  val server = defaultLoop.tcp
//
//  server.bind("0.0.0.0", 3000, 0)
//
//  def connectionCallback(handle: TCP, status: Int): Unit =
//    val client = defaultLoop.tcp
//
//    handle.accept(client)
//
//    def readCallback(client: TCP, size: Int, buf: Buffer): Unit =
//      client.write(
//        s"""HTTP/1.0 200 OK\r
//           |Content-Type: text/plain\r
//           |Content-Length: 12\r
//           |\r
//           |hello world
//           |""".stripMargin.getBytes,
//      )
//      client.readStop
//      client.shutdown(_.close())
//
//    client.readStart(readCallback)
//
//  server.listen(100, connectionCallback)
//  println("listening")
//  defaultLoop.run()

//  def exitCallback(status: Int, signal: Int): Unit = println(status)
//
//  for i <- 1 to 100 do
//    defaultLoop.spawn("/home/ed/dev-sn/test/target/scala-3.1.3/test-out", Vector("3", i.toString), exitCallback)
//
//  defaultLoop.run()
