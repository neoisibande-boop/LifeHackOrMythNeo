package com.example.lifehackormyth

import org.junit.Assert.*
import org.junit.Test

class QuizEngineTest {
    private fun quiz() = QuizEngine(listOf(
        Flashcard("True card", true, "Explanation", "https://example.org"),
        Flashcard("False card", false, "Explanation", "https://example.org")
    ))
    @Test fun startsAtFirstQuestionWithZeroScore() {
        val quiz = quiz()
        assertEquals(0, quiz.currentIndex)
        assertEquals(0, quiz.score)
        assertNull(quiz.currentAnswer)
        assertFalse(quiz.isComplete)
    }
    @Test fun correctAnswerIncreasesScore() {
        val quiz = quiz()
        assertTrue(quiz.answer(true))
        assertEquals(1, quiz.score)
    }
    @Test fun incorrectAnswerDoesNotIncreaseScore() {
        val quiz = quiz()
        assertFalse(quiz.answer(false))
        assertEquals(0, quiz.score)
    }
    @Test fun repeatedTapsCannotChangeAnswerOrInflateScore() {
        val quiz = quiz()
        quiz.answer(true); quiz.answer(true); quiz.answer(false)
        assertEquals(1, quiz.score)
        assertEquals(true, quiz.currentAnswer)
        assertEquals(1, quiz.answeredCount)
    }
    @Test(expected = IllegalStateException::class) fun cannotSkipUnansweredCard() { quiz().next() }
    @Test fun iteratesAllQuestionsAndStopsAtLastCard() {
        val quiz = quiz()
        quiz.answer(true)
        assertTrue(quiz.next())
        assertEquals(1, quiz.currentIndex)
        quiz.answer(false)
        assertFalse(quiz.next())
        assertTrue(quiz.isComplete)
        assertEquals(2, quiz.score)
    }
    @Test fun restartClearsProgressAndScore() {
        val quiz = quiz()
        quiz.answer(true); quiz.next(); quiz.answer(false); quiz.restart()
        assertEquals(0, quiz.currentIndex)
        assertEquals(0, quiz.score)
        assertArrayEquals(intArrayOf(-1, -1), quiz.savedAnswers())
    }
    @Test fun restoresAnsweredCardWithoutDoubleCounting() {
        val quiz = quiz()
        quiz.answer(true); quiz.next(); quiz.answer(false)
        val restored = quiz()
        restored.restore(quiz.currentIndex, quiz.savedAnswers())
        assertEquals(2, restored.score)
        assertEquals(false, restored.currentAnswer)
        restored.answer(false)
        assertEquals(2, restored.score)
    }
    @Test fun restoresUnansweredNextCard() {
        val quiz = quiz()
        quiz.restore(1, intArrayOf(1, -1))
        assertEquals(1, quiz.score)
        assertNull(quiz.currentAnswer)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsInvalidSavedValues() { quiz().restore(0, intArrayOf(2,-1)) }
    @Test(expected = IllegalArgumentException::class) fun rejectsSkippedSavedQuestions() { quiz().restore(1, intArrayOf(-1,-1)) }
    @Test(expected = IllegalArgumentException::class) fun rejectsFutureAnswers() { quiz().restore(0, intArrayOf(1,0)) }
    @Test(expected = IllegalArgumentException::class) fun rejectsEmptyQuiz() { QuizEngine(emptyList()) }
    @Test fun feedbackMatchesScoreBands() {
        val quiz = quiz()
        assertEquals("Stay Safe Online!", quiz.feedback().title)
        quiz.answer(true)
        assertEquals("Good instincts!", quiz.feedback().title)
        quiz.next(); quiz.answer(false)
        assertEquals("Master Hacker!", quiz.feedback().title)
    }
    @Test fun bankContainsTenBalancedCardsWithExplanationsAndSources() {
        val cards = QuestionBank.questions
        assertEquals(10, cards.size)
        assertEquals(5, cards.count { it.isHack })
        assertTrue(cards.all { it.statement.isNotBlank() && it.explanation.isNotBlank() && it.sourceUrl.startsWith("https://") })
    }
    @Test fun allCorrectAcrossFullBankScoresTen() {
        val quiz = QuizEngine(QuestionBank.questions)
        for (card in quiz.questions) { quiz.answer(card.isHack); quiz.next() }
        assertEquals(10, quiz.score)
        assertTrue(quiz.isComplete)
    }
    @Test fun allWrongAcrossFullBankScoresZero() {
        val quiz = QuizEngine(QuestionBank.questions)
        for (card in quiz.questions) { quiz.answer(!card.isHack); quiz.next() }
        assertEquals(0, quiz.score)
        assertTrue(quiz.isComplete)
    }
}
