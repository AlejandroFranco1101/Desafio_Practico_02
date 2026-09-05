package com.example.desafio_practico_02

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
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
    private var isRegisterMode = false

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
            updateAuthMode()
        } else {
            authContainer.visibility = View.GONE
            sessionContainer.visibility = View.VISIBLE
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
}
