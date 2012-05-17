class Benchmark extends SimpleScalaBenchmark {
  def lmsMatches(re: Unit => scala.virtualization.lms.regexp.Automaton[Char,Byte])(input: String): Boolean = {
    var state = re()
    var i = 0
    val n = input.length
    while ((state.out & 2) != 2 && (i < n)) {
      state = state.next(input.charAt(i))
      i += 1
    }
    state.out % 2 == 1
  }

  def dkMatches(re: dk.brics.automaton.RunAutomaton)(input: String): Boolean = {
    re.run(input)
  }

  def dkStepMatches(re: dk.brics.automaton.RunAutomaton)(input: String): Boolean = {
    var state = re.getInitialState()
    var i = 0
    val n = input.length
    var dead = false
    while (i < n) {
      state = re.step(state, input.charAt(i))
      if (state == -1) {
        dead = true
        state = re.getInitialState()
      }
      i += 1
    }
    !dead && re.isAccept(state)
  }

  def javaMatches(re: java.util.regex.Pattern)(input: String): Boolean = {
    re.matcher(input).matches()
  }

  var lmsRegexps: Array[Unit => scala.virtualization.lms.regexp.Automaton[Char,Byte]] = _
  var dkRegexps: Array[dk.brics.automaton.RunAutomaton] = _
  var javaRegexps: Array[java.util.regex.Pattern] = _

  var inputs: Array[String] = _

  override def setUp() {
    val regexps = Array(
      (new MatchAAB, ".*AAB"),
      (new MatchAABany, ".*AAB.*"),
      (new MatchUSD, "usd [+-]?[0-9]+.[0-9][0-9]"),
      (new MatchAnything, ".*A?.*"))
    inputs = Array(
      "AAB",
      "hello AAB",
      "AAB no",
      "AAB " + (1 to 10).map(_ => "no no no"),
      "http://www.linux.com/",
      "http://www.thelinuxshow.com/main.php3",
      "usd 1234.00",
      "   usd 1234.00",
      "he said she said he said no",
      "same same same",
      "{1:" + "this is some more text - and some more and some more and even more" +
      (1 to 40).map(_ => "this is some more text and some more and some more and even more").mkString +
      "this is some more text and some more and some more and even more at the end" + "-}"
    )
    val expected = Array(
      Array(true, true, false, false, false, false, false, false, false, false, false),
      Array(true, true, true, true, false, false, false, false, false, false, false),
      Array(false, false, false, false, false, false, true, false, false, false, false),
      Array(true, true, true, true, true, true, true, true, true, true, true))
    val allRegexps = for ((lmsRe,re) <- regexps) yield {
      val dkRe = new dk.brics.automaton.RunAutomaton(new dk.brics.automaton.RegExp(re).toAutomaton(), true)
      val javaRe = java.util.regex.Pattern.compile(re, java.util.regex.Pattern.DOTALL)
      (lmsRe, dkRe, javaRe, re)
    }

    for (ri <- 0 to regexps.length-1; (lmsRe,dkRe,javaRe, re) = allRegexps(ri)) {
      for (i <- 0 to inputs.length-1; input = inputs(i)) {
        val lmsResult = lmsMatches(lmsRe)(input)
        val dkResult = dkMatches(dkRe)(input)
        val dkStepResult = dkStepMatches(dkRe)(input)
        val javaResult = javaMatches(javaRe)(input)
        val expectedResult = expected(ri)(i)
        val debug = re + ":" + input + "->" + expectedResult
        assert(lmsResult == expectedResult, debug)
        assert(dkResult == expectedResult, debug)
        assert(dkStepResult == expectedResult, debug)
        assert(javaResult == expectedResult, debug)
      }
    }

    lmsRegexps = allRegexps.map(_._1)
    dkRegexps = allRegexps.map(_._2)
    javaRegexps = allRegexps.map(_._3)
  }

  def repeatAll[T](matches: T => String => Boolean, res: Array[T])(reps: Int) = repeat(reps) {
    var count = 0
    var i = 0
    while (i < res.length) {
       val re = res(i)
       var j = 0
       while (j < inputs.length) {
         val input = inputs(j)
         if (matches(re)(input)) count += 1 else count -= 1
         j += 1
       }
       i += 1
    }
    count
  }

  def timeLMS(reps: Int) = repeatAll(lmsMatches, lmsRegexps)(reps)
  def timeDK(reps: Int) = repeatAll(dkMatches, dkRegexps)(reps)
  def timeDKStep(reps: Int) = repeatAll(dkStepMatches, dkRegexps)(reps)
  def timeJava(reps: Int) = repeatAll(javaMatches, javaRegexps)(reps)
}
