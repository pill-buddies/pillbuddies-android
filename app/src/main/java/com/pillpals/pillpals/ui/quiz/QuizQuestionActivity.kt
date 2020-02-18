package com.pillpals.pillpals.ui.quiz

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import java.util.Calendar

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getMedicationByUid
import io.realm.Realm

import java.util.*
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.helpers.DateHelper
import com.google.android.material.button.MaterialButton
import com.pillpals.pillpals.data.model.Questions
import com.pillpals.pillpals.data.model.Quizzes
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.convertByteArrayToBitmap
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getByteArrayById
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorIDByString
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getIconByID
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getIconIDByString
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getRandomIcon
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getRandomUniqueColorString
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getScheduleByUid
import com.pillpals.pillpals.helpers.QuizHelper
import com.pillpals.pillpals.helpers.calculateScheduleRecords
import io.realm.RealmObject.deleteFromRealm
import kotlinx.android.synthetic.main.delete_prompt.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import kotlin.concurrent.thread

class QuizQuestionActivity : AppCompatActivity() {

    lateinit var realm: Realm
    lateinit var quiz: Quizzes
    lateinit var icon: ImageView
    lateinit var iconBackground: CardView
    lateinit var drugName: TextView
    lateinit var questionTitle: TextView
    lateinit var questionText: TextView
    lateinit var answer1btn: Button
    lateinit var answer2btn: Button
    lateinit var answer3btn: Button
    lateinit var answer4btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)

        realm = Realm.getDefaultInstance()

        setContentView(R.layout.activity_quiz_question)

        val quizUID = intent.getStringExtra("quiz-uid")
        quiz = DatabaseHelper.getQuizByUid(quizUID)!!

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(quiz.name)

        icon = findViewById(R.id.icon)
        iconBackground = findViewById(R.id.iconBackground)
        drugName = findViewById(R.id.drugName)
        questionTitle = findViewById(R.id.questionTitle)
        questionText = findViewById(R.id.questionText)
        answer1btn = findViewById(R.id.answer1btn)
        answer2btn = findViewById(R.id.answer2btn)
        answer3btn = findViewById(R.id.answer3btn)
        answer4btn = findViewById(R.id.answer4btn)

        setPageContentsForQuestion(QuizHelper.getQuestionsAnswered(quiz))
    }

    private fun setPageContentsForQuestion(index: Int){
        val question = quiz!!.questions[index]!!
        iconBackground.setCardBackgroundColor(Color.parseColor(getColorStringByID(question.medication!!.color_id)))
        icon.setImageDrawable(
            DatabaseHelper.getCorrectIconDrawable(
                this,
                question.medication!!
            )
        )
        drugName.text = question.medication!!.name
        questionTitle.text = "Question " + (index + 1).toString()
        questionText.text = question.question

        setUpButtons(question, index)

    }

    private fun answerQuestion(question: Questions, answer: Int, index: Int) {
        realm.executeTransaction{
            question.userAnswer = answer
        }

        if(index == 9) {
            val intent = Intent(this, QuizResultsActivity::class.java)
            intent.putExtra("quiz-uid", quiz.uid)
            startActivityForResult(intent, 1)
            this.finish()
        } else {
            setPageContentsForQuestion(index + 1)
        }
    }

    private fun setUpButtons(question: Questions, index: Int) {
        answer1btn.text = question.answers[0]
        answer1btn.setOnClickListener{answerQuestion(question,0, index)}
        answer2btn.text = question.answers[1]
        answer2btn.setOnClickListener{answerQuestion(question,1, index)}
        answer3btn.text = question.answers[2]
        answer3btn.setOnClickListener{answerQuestion(question,2, index)}
        answer4btn.text = question.answers[3]
        answer4btn.setOnClickListener{answerQuestion(question,3, index)}
    }
}