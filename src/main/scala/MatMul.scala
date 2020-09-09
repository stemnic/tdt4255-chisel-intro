// Chisel

package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class MatMul(val rowDimsA: Int, val colDimsA: Int) extends MultiIOModule {

  val io = IO(
    new Bundle {
      val dataInA     = Input(UInt(32.W))
      val dataInB     = Input(UInt(32.W))

      val dataOut     = Output(UInt(32.W))
      val outputValid = Output(Bool())
    }
  )

  val debug = IO(
    new Bundle {
      val myDebugSignal = Output(Bool())
    }
  )


  val matrixA     = Module(new Matrix(rowDimsA, colDimsA)).io
  val matrixB     = Module(new Matrix(rowDimsA, colDimsA)).io
  val dotProdCalc = Module(new DotProd(colDimsA)).io

  val state = RegInit(0.U(32.W))
  val statemachineInput: Bool = state === 0.U
  val statemachineCalc: Bool = state === 1.U

  val (cntCol, cntColW) = Counter(true.B, colDimsA)

  val cntRowBact = cntColW

  val (cntRowB, cntRowBWrap) = Counter(cntRowBact, rowDimsA)
  val cntRowAact = cntRowBWrap || (cntColW && statemachineInput)
  val (cntRowA, cntRowAWrap) = Counter(cntRowAact, rowDimsA)




  // State machine
  when (statemachineInput) {
    when (cntRowAWrap) {
      state := 1.U
    }.otherwise {
      state := state
    }
  }.otherwise {
    when (cntRowAWrap) {
      state := 0.U
    }.otherwise {
      state := state
    }
  }

  // Mapping
  matrixA.writeEnable := statemachineInput
  matrixB.writeEnable := statemachineInput

  matrixA.colIdx := cntCol
  matrixA.rowIdx := cntRowA
  matrixA.dataIn := io.dataInA

  matrixB.colIdx := cntCol
  matrixB.rowIdx := cntRowB
  matrixB.dataIn := io.dataInB

  dotProdCalc.dataInA := matrixA.dataOut
  dotProdCalc.dataInB := matrixB.dataOut

  io.dataOut := dotProdCalc.dataOut
  io.outputValid := dotProdCalc.outputValid && statemachineCalc
}
