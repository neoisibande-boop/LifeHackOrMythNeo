package com.example.lifehackormyth

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import com.example.lifehackormyth.databinding.ActivityWelcomeBinding
import com.example.lifehackormyth.databinding.ActivityQuizBinding
import com.example.lifehackormyth.databinding.ActivityScoreBinding
import com.example.lifehackormyth.databinding.ActivityReviewBinding
import com.example.lifehackormyth.databinding.ItemReviewBinding

/** Four XML screens in one Activity, with explicit navigation and saved state. */
class MainActivity : Activity() {
    private enum class Screen { WELCOME, QUIZ, SCORE, REVIEW }
    private var screen = Screen.WELCOME
    private val quiz = QuizEngine(QuestionBank.questions)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            val saved = savedInstanceState.getIntArray("answers")
            if (saved != null) {
                try {
                    quiz.restore(savedInstanceState.getInt("index"), saved)
                    screen = Screen.valueOf(savedInstanceState.getString("screen") ?: "WELCOME")
                    if ((screen == Screen.SCORE || screen == Screen.REVIEW) && !quiz.isComplete) screen = Screen.QUIZ
                } catch (_: IllegalArgumentException) {
                    quiz.restart()
                    screen = Screen.WELCOME
                }
            }
        }
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("screen", screen.name)
        outState.putInt("index", quiz.currentIndex)
        outState.putIntArray("answers", quiz.savedAnswers())
        super.onSaveInstanceState(outState)
    }

    private fun navigate(destination: Screen) {
        screen = destination
        render()
    }

    /** Keep content clear of status/navigation bars, including Android 15 edge-to-edge. */
    private fun show(root: View) {
        setContentView(root)
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        root.requestApplyInsets()
    }

    private fun render() {
        when (screen) {
            Screen.WELCOME -> showWelcome()
            Screen.QUIZ -> showQuiz()
            Screen.SCORE -> showScore()
            Screen.REVIEW -> showReview()
        }
    }

    private fun showWelcome() {
        val binding = ActivityWelcomeBinding.inflate(layoutInflater)
        show(binding.root)
        binding.startButton.setOnClickListener {
            quiz.restart()
            navigate(Screen.QUIZ)
        }
    }

    private fun showQuiz() {
        val binding = ActivityQuizBinding.inflate(layoutInflater)
        show(binding.root)
        binding.questionText.text = quiz.currentQuestion.statement
        binding.progressText.text = getString(R.string.question_progress, quiz.currentIndex + 1, quiz.questions.size)
        binding.progressBar.max = quiz.questions.size

        fun updateAnswer() {
            val answered = quiz.currentAnswer != null
            binding.scoreText.text = getString(R.string.score_progress, quiz.score, quiz.questions.size)
            binding.progressBar.progress = quiz.answeredCount
            binding.hackButton.isEnabled = !answered
            binding.mythButton.isEnabled = !answered
            binding.hackButton.alpha = if (answered && quiz.currentAnswer != true) 0.45f else 1f
            binding.mythButton.alpha = if (answered && quiz.currentAnswer != false) 0.45f else 1f
            binding.nextButton.isEnabled = answered
            binding.nextButton.alpha = if (answered) 1f else 0.45f
            binding.nextButton.setText(if (quiz.currentIndex == quiz.questions.lastIndex) R.string.see_score else R.string.next)
            if (answered) {
                val correct = quiz.currentAnswer == quiz.currentQuestion.isHack
                val feedback = when {
                    correct && quiz.currentQuestion.isHack -> R.string.correct_hack
                    correct -> R.string.correct_myth
                    quiz.currentQuestion.isHack -> R.string.wrong_hack
                    else -> R.string.wrong_myth
                }
                binding.feedbackText.setText(feedback)
                binding.feedbackText.setTextColor(getColor(if (correct) R.color.teal else R.color.error))
                binding.explanationText.text = quiz.currentQuestion.explanation
                binding.explanationText.visibility = View.VISIBLE
                binding.sourceText.text = getString(R.string.source_label, quiz.currentQuestion.sourceUrl)
                binding.sourceText.visibility = View.VISIBLE
            }
        }

        binding.hackButton.setOnClickListener { quiz.answer(true); updateAnswer() }
        binding.mythButton.setOnClickListener { quiz.answer(false); updateAnswer() }
        binding.nextButton.setOnClickListener {
            if (quiz.next()) showQuiz() else navigate(Screen.SCORE)
        }
        updateAnswer()
    }

    private fun showScore() {
        val binding = ActivityScoreBinding.inflate(layoutInflater)
        show(binding.root)
        binding.scoreText.text = getString(R.string.result_score, quiz.score, quiz.questions.size)
        binding.countText.text = getString(R.string.correct_count, quiz.score, quiz.questions.size)
        binding.feedbackText.text = quiz.feedback().title
        binding.descriptionText.text = quiz.feedback().message
        binding.reviewButton.setOnClickListener { navigate(Screen.REVIEW) }
        binding.restartButton.setOnClickListener { quiz.restart(); navigate(Screen.QUIZ) }
        binding.homeButton.setOnClickListener { quiz.restart(); navigate(Screen.WELCOME) }
    }

    private fun showReview() {
        val binding = ActivityReviewBinding.inflate(layoutInflater)
        show(binding.root)
        // A loop displays every statement, correct answer, and explanation.
        for ((index, question) in quiz.questions.withIndex()) {
            val item = ItemReviewBinding.inflate(layoutInflater, binding.reviewContainer, false)
            item.numberText.text = getString(R.string.card_number, index + 1)
            item.statementText.text = question.statement
            val answerName = getString(if (question.isHack) R.string.hack else R.string.myth)
            item.correctAnswerText.text = getString(R.string.correct_answer, answerName)
            val chosen = quiz.answerAt(index)
            val chosenName = getString(when (chosen) { true -> R.string.hack; false -> R.string.myth; null -> R.string.unanswered })
            val result = getString(if (chosen == question.isHack) R.string.correct else R.string.incorrect)
            item.yourAnswerText.text = getString(R.string.your_answer, chosenName, result)
            item.explanationText.text = question.explanation
            item.sourceText.text = getString(R.string.source_label, question.sourceUrl)
            binding.reviewContainer.addView(item.root)
        }
        binding.backButton.setOnClickListener { navigate(Screen.SCORE) }
    }

    @Deprecated("Legacy back navigation is deliberately enabled for this simple Activity")
    override fun onBackPressed() {
        when (screen) {
            Screen.REVIEW -> navigate(Screen.SCORE)
            Screen.SCORE -> { quiz.restart(); navigate(Screen.WELCOME) }
            Screen.QUIZ -> AlertDialog.Builder(this)
                .setTitle(R.string.quit_title).setMessage(R.string.quit_message)
                .setPositiveButton(R.string.leave) { _, _ -> quiz.restart(); navigate(Screen.WELCOME) }
                .setNegativeButton(R.string.stay, null).show()
            Screen.WELCOME -> super.onBackPressed()
        }
    }
}
