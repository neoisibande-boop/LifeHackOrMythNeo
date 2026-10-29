package com.example.lifehackormyth

data class Flashcard(val statement: String, val isHack: Boolean, val explanation: String, val sourceUrl: String)
data class ScoreFeedback(val title: String, val message: String)

/** Plain Kotlin logic: no Android classes, so it can be tested on the JVM. */
class QuizEngine(val questions: List<Flashcard>) {
    init { require(questions.isNotEmpty()) { "A quiz needs at least one question." } }
    private val answers = MutableList<Boolean?>(questions.size) { null }
    var currentIndex: Int = 0
        private set
    val currentQuestion: Flashcard get() = questions[currentIndex]
    val currentAnswer: Boolean? get() = answers[currentIndex]
    val answeredCount: Int get() = answers.count { it != null }
    val score: Int get() = questions.indices.count { answers[it] == questions[it].isHack }
    val isComplete: Boolean get() = answeredCount == questions.size

    /** Only the first tap counts. Returning false also covers a wrong answer. */
    fun answer(choice: Boolean): Boolean {
        if (currentAnswer != null) return currentAnswer == currentQuestion.isHack
        answers[currentIndex] = choice
        return choice == currentQuestion.isHack
    }

    /** True means another card is available; false means the quiz has finished. */
    fun next(): Boolean {
        check(currentAnswer != null) { "Answer the current card before continuing." }
        if (currentIndex == questions.lastIndex) return false
        currentIndex++
        return true
    }

    fun answerAt(index: Int): Boolean? = answers[index]
    fun restart() {
        currentIndex = 0
        answers.fill(null)
    }

    fun savedAnswers(): IntArray = answers.map { when (it) { true -> 1; false -> 0; null -> -1 } }.toIntArray()

    /** Reject inconsistent state instead of restoring an impossible score. */
    fun restore(index: Int, saved: IntArray) {
        require(index in questions.indices && saved.size == questions.size)
        require(saved.all { it in -1..1 })
        require((0 until index).all { saved[it] != -1 })
        require(((index + 1) until questions.size).all { saved[it] == -1 })
        for (i in questions.indices) answers[i] = when (saved[i]) { 1 -> true; 0 -> false; else -> null }
        currentIndex = index
    }

    fun feedback(): ScoreFeedback {
        val percent = score * 100 / questions.size
        return when {
            percent >= 80 -> ScoreFeedback("Master Hacker!", "Great job! You can spot useful tips and avoid risky myths.")
            percent >= 50 -> ScoreFeedback("Good instincts!", "Keep practising. Review the explanations to sharpen your fact-checking skills.")
            else -> ScoreFeedback("Stay Safe Online!", "Keep practising! Check reliable sources before trying a viral tip.")
        }
    }
}
