package Ex0

import chisel3._
import chisel3.util.Counter

class DotProd(val elements: Int) extends Module {

  val io = IO(
    new Bundle {
      val dataInA     = Input(UInt(32.W))
      val dataInB     = Input(UInt(32.W))

      val dataOut     = Output(UInt(32.W))
      val outputValid = Output(Bool())
    }
  )


  /**
    * Your code here
    */

  val (counterValue, counterWrap) = Counter(true.B, elements)

  val accumulatorReg = RegInit(UInt(32.W), 0.U)
  
  val prod = io.dataInA * io.dataInB
  val result = accumulatorReg + prod

  when(counterValue === 0.U) {
    accumulatorReg := prod
  }.otherwise {
    accumulatorReg := result
  }
  io.outputValid := counterWrap
  io.dataOut := result
}
