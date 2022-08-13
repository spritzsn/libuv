package io.github.edadma.libuv

@main def run(): Unit =
  println("wait")
  defaultLoop.timer.start(t => println("boom"), 1000, 500)
  println(defaultLoop.run())
