package com.example.lifehackormyth

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuizFlowTest {
    @Test fun quizScoreReviewAndRestart() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.startButton)).perform(scrollTo(), click())
            onView(withId(R.id.nextButton)).check(matches(not(isEnabled())))
            for (card in QuestionBank.questions) {
                onView(withId(if (card.isHack) R.id.hackButton else R.id.mythButton)).perform(scrollTo(), click())
                onView(withId(R.id.hackButton)).check(matches(not(isEnabled())))
                onView(withId(R.id.explanationText)).check(matches(withText(card.explanation)))
                onView(withId(R.id.nextButton)).perform(scrollTo(), click())
            }
            onView(withId(R.id.scoreText)).check(matches(withText("10 / 10")))
            onView(withId(R.id.reviewButton)).perform(scrollTo(), click())
            onView(withId(R.id.title)).check(matches(withText(R.string.review_title)))
            onView(withId(R.id.backButton)).perform(scrollTo(), click())
            onView(withId(R.id.restartButton)).perform(scrollTo(), click())
            onView(withId(R.id.scoreText)).check(matches(withText("Score: 0 / 10")))
        }
    }
    @Test fun recreationPreservesAnswerAndScore() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.startButton)).perform(scrollTo(), click())
            onView(withId(R.id.hackButton)).perform(scrollTo(), click())
            scenario.recreate()
            onView(withId(R.id.scoreText)).check(matches(withText("Score: 1 / 10")))
            onView(withId(R.id.hackButton)).check(matches(not(isEnabled())))
            onView(withId(R.id.nextButton)).check(matches(isEnabled()))
        }
    }
}
