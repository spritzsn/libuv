//package io.github.spritzsn.libuv
//
//import scala.scalanative.posix.sys.socket.{AF_INET, AF_UNSPEC}
//
//@main def run(): Unit =
//  val parser = new HTTPResponseParser
//  var ex: Option[Exception] = None
//
//  def dnsCallback(status: Int, addrInfo: List[AddrInfo]): Unit =
//    println(s"dnsCallback status: $status; addrInfo $addrInfo")
//
//    val h = defaultLoop.tcp
//
//    def connectCallback(status: Int): Unit =
//      println(s"connectCallback status: $status; error: ${strError(status)}")
//      h.write("GET / HTTP/1.0\r\nHost: localhost\r\n\r\n".getBytes)
//
//      def readCallback(stream: TCP, size: Int, buf: Buffer): Unit =
//        println(s"readCallback size: $size eof $eof")
//        if size < 0 then
//          stream.readStop
//          if size != eof then println(s"error in read callback: ${errName(size)}: ${strError(size)}") // todo
//        else if size > 0 then
//          try
//            for i <- 0 until size do parser send buf(i)
//            if parser.isFinal then println(parser)
//          catch case e: Exception => ex = Some(e)
//      end readCallback
//
//      h.readStart(readCallback)
//
//    h.connect(addrInfo.head.ip, 3000, connectCallback)
//
//  defaultLoop.getAddrInfo(dnsCallback, "localhost", null, AF_INET)
//  defaultLoop.run()
//  println("done")
//  println(parser)

//  defaultLoop.spawn("sleep", Vector("3"))
//  defaultLoop.run()

//  val data = "one\ntwo\n".getBytes
//
//  def opencb(req: File): Unit =
//    val openres = req.getResult
//    var idx = 0
//    val len = data.length
//
//    if openres < 0 then println(strError(openres))
//    else
//      def writecb(req: File): Unit =
//        val res = req.getResult
//
//        if res < 0 then println(strError(res))
//        else if idx + res < len then
//          idx += res
//          defaultLoop.write(data, idx, openres, writecb)
//        else defaultLoop.close(openres)
//
//      defaultLoop.write(data, 0, openres, writecb)
//
//  defaultLoop.open("asdf", O_WRONLY | O_CREAT, S_IRUSR | S_IWUSR, opencb)
//  defaultLoop.run()

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
//    println(s"local: ${client.getSockName}")
//    println(s"remote: ${client.getPeerName}")
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
