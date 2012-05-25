package scala.virtualization.lms.regexp

class TestOpt extends FileDiffSuite {
  trait Examples extends DSL {
    val aab = many(seq)(star(wildcard), c('A'), c('A'), c('B'))
  }

  trait Evaluator extends DSL with ImplOpt {
    def recompile(re: RE) = {
      val f = codegen.pack(convertREtoDFA(re))
      val fc = compile(f)
      fc
    }
  }

  trait Go extends DSL with ImplOpt {
    def go(name: String, re: RE) = {
      val fn = prefix+"opt_"+name
      withOutFile(fn) {
        val f = codegen.pack(convertREtoDFA(re))
        codegen.emitSource(f, "Match", new java.io.PrintWriter(System.out))
      }
      assertFileEqualsCheck(fn)
    }
  }

  def testEvalAAB = {
    val exs = new Examples with Evaluator
    val fc = exs.recompile(exs.aab)

    expect(true)(fc("AAB"))
  }

  def testCompileAAB = {
    val exs = new Examples with Go
    exs.go("aab", exs.aab)
  }
}
