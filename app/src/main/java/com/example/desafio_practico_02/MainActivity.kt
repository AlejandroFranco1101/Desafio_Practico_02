package com.example.desafio_practico_02

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var authContainer: LinearLayout
    private lateinit var sessionContainer: LinearLayout
    private lateinit var authTitleText: TextView
    private lateinit var authSubtitleText: TextView
    private lateinit var sessionEmailText: TextView
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var authActionButton: MaterialButton
    private lateinit var switchModeButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private lateinit var startQuizButton: MaterialButton
    private lateinit var categoryToggleGroup: MaterialButtonToggleGroup
    private lateinit var difficultyToggleGroup: MaterialButtonToggleGroup
    private lateinit var quizContainer: LinearLayout
    private lateinit var quizTitleText: TextView
    private lateinit var questionsContainer: LinearLayout
    private lateinit var submitQuizButton: MaterialButton
    private lateinit var resetQuizButton: MaterialButton
    private lateinit var quizBackToWelcomeButton: MaterialButton
    private lateinit var resultsContainer: LinearLayout
    private lateinit var resultsScoreText: TextView
    private lateinit var resultsCategoryText: TextView
    private lateinit var resultsFeedbackText: TextView
    private lateinit var reviewContainer: LinearLayout
    private lateinit var retryQuizButton: MaterialButton
    private lateinit var backToWelcomeButton: MaterialButton
    private var isRegisterMode = false
    private var selectedCategory = ""
    private var selectedDifficulty = ""
    private var currentQuestions = emptyList<QuizQuestion>()
    private var selectedAnswers = emptyList<Int>()
    private val answerGroups = mutableListOf<RadioGroup>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        bindViews()
        configureActions()
        updateSessionState()
    }

    private fun bindViews() {
        authContainer = findViewById(R.id.authContainer)
        sessionContainer = findViewById(R.id.sessionContainer)
        authTitleText = findViewById(R.id.authTitleText)
        authSubtitleText = findViewById(R.id.authSubtitleText)
        sessionEmailText = findViewById(R.id.sessionEmailText)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        authActionButton = findViewById(R.id.authActionButton)
        switchModeButton = findViewById(R.id.switchModeButton)
        logoutButton = findViewById(R.id.logoutButton)
        startQuizButton = findViewById(R.id.startQuizButton)
        categoryToggleGroup = findViewById(R.id.categoryToggleGroup)
        difficultyToggleGroup = findViewById(R.id.difficultyToggleGroup)
        quizContainer = findViewById(R.id.quizContainer)
        quizTitleText = findViewById(R.id.quizTitleText)
        questionsContainer = findViewById(R.id.questionsContainer)
        submitQuizButton = findViewById(R.id.submitQuizButton)
        resetQuizButton = findViewById(R.id.resetQuizButton)
        quizBackToWelcomeButton = findViewById(R.id.quizBackToWelcomeButton)
        resultsContainer = findViewById(R.id.resultsContainer)
        resultsScoreText = findViewById(R.id.resultsScoreText)
        resultsCategoryText = findViewById(R.id.resultsCategoryText)
        resultsFeedbackText = findViewById(R.id.resultsFeedbackText)
        reviewContainer = findViewById(R.id.reviewContainer)
        retryQuizButton = findViewById(R.id.retryQuizButton)
        backToWelcomeButton = findViewById(R.id.backToWelcomeButton)
    }

    private fun configureActions() {
        authActionButton.setOnClickListener {
            submitAuthForm()
        }

        switchModeButton.setOnClickListener {
            isRegisterMode = !isRegisterMode
            clearErrors()
            updateAuthMode()
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Sesion cerrada correctamente.", Toast.LENGTH_SHORT).show()
            updateSessionState()
        }

        startQuizButton.setOnClickListener {
            startSelectedQuiz()
        }

        submitQuizButton.setOnClickListener {
            submitQuiz()
        }

        resetQuizButton.setOnClickListener {
            resetQuiz()
        }

        quizBackToWelcomeButton.setOnClickListener {
            showWelcome()
        }

        retryQuizButton.setOnClickListener {
            showQuiz()
        }

        backToWelcomeButton.setOnClickListener {
            showWelcome()
        }
    }

    private fun submitAuthForm() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        if (!validateForm(email, password)) {
            return
        }

        setLoadingState(true)
        val authTask = if (isRegisterMode) {
            auth.createUserWithEmailAndPassword(email, password)
        } else {
            auth.signInWithEmailAndPassword(email, password)
        }

        authTask.addOnCompleteListener { task ->
            setLoadingState(false)
            if (task.isSuccessful) {
                val message = if (isRegisterMode) {
                    "Registro completado."
                } else {
                    "Inicio de sesion correcto."
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                updateSessionState()
            } else {
                val fallback = if (isRegisterMode) {
                    "No se pudo registrar el usuario."
                } else {
                    "Correo o contrasena incorrectos."
                }
                Toast.makeText(this, task.exception?.localizedMessage ?: fallback, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        clearErrors()
        var isValid = true

        if (email.isBlank()) {
            emailInputLayout.error = "Ingresa tu correo electronico."
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "El correo electronico no es valido."
            isValid = false
        }

        if (password.isBlank()) {
            passwordInputLayout.error = "Ingresa tu contrasena."
            isValid = false
        } else if (password.length < 6) {
            passwordInputLayout.error = "La contrasena debe tener al menos 6 caracteres."
            isValid = false
        }

        return isValid
    }

    private fun updateAuthMode() {
        if (isRegisterMode) {
            authTitleText.setText(R.string.register_title)
            authSubtitleText.setText(R.string.register_subtitle)
            authActionButton.setText(R.string.register_button)
            switchModeButton.setText(R.string.go_to_login)
        } else {
            authTitleText.setText(R.string.login_title)
            authSubtitleText.setText(R.string.login_subtitle)
            authActionButton.setText(R.string.login_button)
            switchModeButton.setText(R.string.go_to_register)
        }
    }

    private fun updateSessionState() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            authContainer.visibility = View.VISIBLE
            sessionContainer.visibility = View.GONE
            quizContainer.visibility = View.GONE
            resultsContainer.visibility = View.GONE
            updateAuthMode()
        } else {
            authContainer.visibility = View.GONE
            sessionContainer.visibility = View.VISIBLE
            quizContainer.visibility = View.GONE
            resultsContainer.visibility = View.GONE
            sessionEmailText.text = currentUser.email ?: "Usuario autenticado"
        }
    }

    private fun clearErrors() {
        emailInputLayout.error = null
        passwordInputLayout.error = null
    }

    private fun setLoadingState(isLoading: Boolean) {
        authActionButton.isEnabled = !isLoading
        switchModeButton.isEnabled = !isLoading
        emailEditText.isEnabled = !isLoading
        passwordEditText.isEnabled = !isLoading
    }

    private fun startSelectedQuiz() {
        val category = getSelectedCategory()
        val difficulty = getSelectedDifficulty()

        if (category == null) {
            Toast.makeText(this, "Selecciona un tipo de quiz.", Toast.LENGTH_SHORT).show()
            return
        }

        selectedCategory = category
        selectedDifficulty = difficulty
        currentQuestions = getQuestions(category, difficulty)
        showQuiz()
    }

    private fun getSelectedCategory(): String? {
        val selectedId = categoryToggleGroup.checkedButtonId
        return if (selectedId == View.NO_ID) null else findViewById<MaterialButton>(selectedId).text.toString()
    }

    private fun getSelectedDifficulty(): String {
        return findViewById<MaterialButton>(difficultyToggleGroup.checkedButtonId).text.toString()
    }

    private fun showQuiz() {
        sessionContainer.visibility = View.GONE
        quizContainer.visibility = View.VISIBLE
        resultsContainer.visibility = View.GONE
        quizBackToWelcomeButton.visibility = View.VISIBLE
        quizBackToWelcomeButton.isEnabled = true
        quizTitleText.text = "$selectedCategory - $selectedDifficulty"
        renderQuestions()
    }

    private fun showWelcome() {
        quizContainer.visibility = View.GONE
        resultsContainer.visibility = View.GONE
        sessionContainer.visibility = View.VISIBLE
    }

    private fun renderQuestions() {
        questionsContainer.removeAllViews()
        answerGroups.clear()

        currentQuestions.forEachIndexed { index, question ->
            val questionText = TextView(this).apply {
                text = "${index + 1}. ${question.text}"
                setTextAppearance(android.R.style.TextAppearance_Material_Medium)
                setPadding(0, 18, 0, 8)
            }
            questionsContainer.addView(questionText)

            val radioGroup = RadioGroup(this).apply {
                orientation = RadioGroup.VERTICAL
            }

            question.options.forEachIndexed { optionIndex, option ->
                val radioButton = RadioButton(this).apply {
                    id = View.generateViewId()
                    text = option
                    tag = optionIndex
                }
                radioGroup.addView(radioButton)
            }

            answerGroups.add(radioGroup)
            questionsContainer.addView(radioGroup)
        }
    }

    private fun submitQuiz() {
        val missingQuestions = answerGroups.mapIndexedNotNull { index, group ->
            if (group.checkedRadioButtonId == View.NO_ID) index + 1 else null
        }

        if (missingQuestions.isNotEmpty()) {
            Toast.makeText(
                this,
                "Falta responder: ${missingQuestions.joinToString(", ")}.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        selectedAnswers = answerGroups.map { group ->
            group.findViewById<RadioButton>(group.checkedRadioButtonId).tag as Int
        }

        val score = selectedAnswers.mapIndexed { index, selectedOption ->
            if (selectedOption == currentQuestions[index].correctAnswerIndex) 1 else 0
        }.sum()

        showResults(score)
    }

    private fun resetQuiz() {
        answerGroups.forEach { it.clearCheck() }
        Toast.makeText(this, "Quiz reiniciado.", Toast.LENGTH_SHORT).show()
    }

    private fun showResults(score: Int) {
        quizContainer.visibility = View.GONE
        sessionContainer.visibility = View.GONE
        resultsContainer.visibility = View.VISIBLE

        resultsScoreText.text = "Obtuviste $score de ${currentQuestions.size} respuestas correctas."
        resultsCategoryText.text = "Tipo de quiz: $selectedCategory ($selectedDifficulty)"
        resultsFeedbackText.text = getFeedbackMessage(score)
        renderReview()
    }

    private fun getFeedbackMessage(score: Int): String {
        return when {
            score <= 1 -> "Mejor me dedico a otra cosa."
            score in 2..3 -> "Mas o menos OK."
            score == 4 -> "Me merezco un churro."
            score == 5 && selectedDifficulty.equals("Difícil", ignoreCase = true) -> "Como pegarle a un bolo."
            else -> "Excelente resultado."
        }
    }

    private fun renderReview() {
        reviewContainer.removeAllViews()

        currentQuestions.forEachIndexed { index, question ->
            val chosenAnswer = question.options[selectedAnswers[index]]
            val correctAnswer = question.options[question.correctAnswerIndex]
            val isCorrect = selectedAnswers[index] == question.correctAnswerIndex
            val status = if (isCorrect) "Correcta" else "Incorrecta"

            val reviewText = TextView(this).apply {
                text = "${index + 1}. ${question.text}\nTu respuesta: $chosenAnswer\nRespuesta correcta: $correctAnswer\n$status"
                setPadding(0, 14, 0, 14)
                setTextAppearance(android.R.style.TextAppearance_Material_Medium)
            }

            reviewContainer.addView(reviewText)
        }
    }

    private fun getQuestions(category: String, difficulty: String): List<QuizQuestion> {
        val hard = difficulty.equals("Difícil", ignoreCase = true)

        return when (category) {
            "Cultura general" -> if (hard) {
                listOf(
                    QuizQuestion("Que pais tiene mas husos horarios oficiales?", listOf("Francia", "Rusia", "Estados Unidos"), 0),
                    QuizQuestion("En que ciudad se encuentra la sede principal de la ONU?", listOf("Ginebra", "Nueva York", "Paris"), 1),
                    QuizQuestion("Cual es el idioma con mas hablantes nativos?", listOf("Mandarin", "Ingles", "Hindi"), 0),
                    QuizQuestion("Que civilizacion construyo Machu Picchu?", listOf("Maya", "Azteca", "Inca"), 2),
                    QuizQuestion("Quien escribio Cien anos de soledad?", listOf("Gabriel Garcia Marquez", "Mario Vargas Llosa", "Julio Cortazar"), 0)
                )
            } else {
                listOf(
                    QuizQuestion("Cual es la capital de El Salvador?", listOf("San Salvador", "Santa Ana", "San Miguel"), 0),
                    QuizQuestion("Cuantos dias tiene una semana?", listOf("Cinco", "Siete", "Diez"), 1),
                    QuizQuestion("Que planeta es conocido como el planeta rojo?", listOf("Venus", "Marte", "Jupiter"), 1),
                    QuizQuestion("Que animal es famoso por tener una trompa larga?", listOf("Elefante", "Caballo", "Leon"), 0),
                    QuizQuestion("Cual es el oceano mas grande?", listOf("Atlantico", "Pacifico", "Indico"), 1)
                )
            }
            "Ciencia" -> if (hard) {
                listOf(
                    QuizQuestion("Que particula tiene carga negativa?", listOf("Electron", "Proton", "Neutron"), 0),
                    QuizQuestion("Cual es la unidad del Sistema Internacional para la fuerza?", listOf("Joule", "Newton", "Watt"), 1),
                    QuizQuestion("Que molecula almacena la informacion genetica?", listOf("ARN", "ADN", "ATP"), 1),
                    QuizQuestion("Que organelo produce energia en la celula?", listOf("Ribosoma", "Mitocondria", "Nucleo"), 1),
                    QuizQuestion("Cual es el simbolo quimico del sodio?", listOf("S", "Na", "So"), 1)
                )
            } else {
                listOf(
                    QuizQuestion("Que gas respiramos principalmente para vivir?", listOf("Oxigeno", "Helio", "Neon"), 0),
                    QuizQuestion("Cuantos estados comunes de la materia se estudian basicamente?", listOf("Tres", "Seis", "Diez"), 0),
                    QuizQuestion("Que estrella ilumina la Tierra?", listOf("La Luna", "El Sol", "Marte"), 1),
                    QuizQuestion("Que parte del cuerpo bombea sangre?", listOf("Pulmon", "Corazon", "Estomago"), 1),
                    QuizQuestion("Que instrumento mide la temperatura?", listOf("Termometro", "Regla", "Bascula"), 0)
                )
            }
            "Deportes" -> if (hard) {
                listOf(
                    QuizQuestion("En futbol, cuantos jugadores por equipo inician el partido?", listOf("9", "10", "11"), 2),
                    QuizQuestion("Que pais gano el Mundial de futbol 2014?", listOf("Alemania", "Argentina", "Brasil"), 0),
                    QuizQuestion("En tenis, como se llama el punto despues de 40 iguales?", listOf("Ventaja", "Set", "Break"), 0),
                    QuizQuestion("Cuantos aros olimpicos hay?", listOf("Cinco", "Seis", "Siete"), 0),
                    QuizQuestion("En baloncesto, cuantos puntos vale un tiro libre?", listOf("Uno", "Dos", "Tres"), 0)
                )
            } else {
                listOf(
                    QuizQuestion("Con que se juega principalmente el futbol?", listOf("Balon", "Raqueta", "Bate"), 0),
                    QuizQuestion("Que deporte usa una canasta?", listOf("Baloncesto", "Natacion", "Ciclismo"), 0),
                    QuizQuestion("Que deporte se practica en una piscina?", listOf("Tenis", "Natacion", "Beisbol"), 1),
                    QuizQuestion("En que deporte se usa una raqueta?", listOf("Tenis", "Futbol", "Boxeo"), 0),
                    QuizQuestion("Que competencia tiene medallas de oro, plata y bronce?", listOf("Juegos Olimpicos", "Liga local", "Entrenamiento"), 0)
                )
            }
            else -> if (hard) {
                listOf(
                    QuizQuestion("En que ano comenzo la Segunda Guerra Mundial?", listOf("1914", "1939", "1945"), 1),
                    QuizQuestion("Quien fue el primer presidente de Estados Unidos?", listOf("Abraham Lincoln", "George Washington", "Thomas Jefferson"), 1),
                    QuizQuestion("Que imperio construyo el Coliseo?", listOf("Romano", "Griego", "Persa"), 0),
                    QuizQuestion("En que ano llego Cristobal Colon a America?", listOf("1492", "1521", "1810"), 0),
                    QuizQuestion("Que muro cayo en 1989?", listOf("Muro de Berlin", "Muralla China", "Muro de Adriano"), 0)
                )
            } else {
                listOf(
                    QuizQuestion("Quienes declararon la independencia de Centroamerica en 1821?", listOf("Los paises centroamericanos", "Los romanos", "Los egipcios"), 0),
                    QuizQuestion("Que objeto se usaba para escribir antes de la computadora?", listOf("Maquina de escribir", "Microfono", "Camara"), 0),
                    QuizQuestion("Que civilizacion construyo piramides en Egipto?", listOf("Egipcia", "Romana", "Inca"), 0),
                    QuizQuestion("Que celebran muchos paises en su dia de independencia?", listOf("Su libertad politica", "Un eclipse", "Una receta"), 0),
                    QuizQuestion("Como se llama el estudio del pasado?", listOf("Historia", "Biologia", "Geometria"), 0)
                )
            }
        }
    }

    data class QuizQuestion(
        val text: String,
        val options: List<String>,
        val correctAnswerIndex: Int
    )
}
