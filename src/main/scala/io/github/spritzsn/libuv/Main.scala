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
    println(s"connection: $status")

    val client = defaultLoop.tcp

    handle.accept(client)

  server.listen(100, connectionCallback)
  defaultLoop.run()
