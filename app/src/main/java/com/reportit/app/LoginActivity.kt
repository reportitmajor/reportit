package com.reportit.app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.reportit.app.data.repository.FirestoreRepository
import com.reportit.app.data.session.SessionManager
import com.reportit.app.databinding.ActivityLoginBinding
import com.reportit.app.utils.LocaleHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private var isSignUpMode = false

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: android.content.res.Configuration?) {
        LocaleHelper.applyConfiguration(overrideConfiguration, this)
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    companion object {
        // Web Client ID configured in Google Cloud Console / Firebase OAuth
        private const val WEB_CLIENT_ID = "136096395261-516lf311il6jf29cuhk5aojcphb6i1l6.apps.googleusercontent.com"
    }

    // Google Sign-In Activity Launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task)
        } else {
            binding.btnGoogleSignIn.isEnabled = true
            Toast.makeText(this, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sessionManager = SessionManager(this)
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In with Web Client ID
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Check active Firebase or Local Session
        val currentFirebaseUser = auth.currentUser
        if (currentFirebaseUser != null) {
            val displayName = currentFirebaseUser.displayName ?: currentFirebaseUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Citizen"
            sessionManager.saveUserSession(
                name = displayName,
                email = currentFirebaseUser.email ?: "",
                provider = if (currentFirebaseUser.providerData.any { it.providerId == "google.com" }) SessionManager.PROVIDER_GOOGLE else SessionManager.PROVIDER_EMAIL,
                role = sessionManager.getUserRole(),
                uid = currentFirebaseUser.uid
            )
            FirestoreRepository.saveUserProfile(
                uid = currentFirebaseUser.uid,
                name = displayName,
                email = currentFirebaseUser.email ?: "",
                role = sessionManager.getUserRole()
            )
            navigateToMainActivity()
            return
        } else if (sessionManager.isLoggedIn()) {
            navigateToMainActivity()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.loginRootLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.loginRootLayout.setPadding(
                binding.loginRootLayout.paddingLeft,
                systemBars.top + 20,
                binding.loginRootLayout.paddingRight,
                systemBars.bottom + 20
            )
            insets
        }

        setupTabToggles()
        setupRoleToggle()
        setupAuthActions()
    }

    private fun setupTabToggles() {
        binding.tabSignIn.setOnClickListener {
            if (isSignUpMode) {
                setAuthMode(signUp = false)
            }
        }

        binding.tabSignUp.setOnClickListener {
            if (!isSignUpMode) {
                setAuthMode(signUp = true)
            }
        }
    }

    private fun setAuthMode(signUp: Boolean) {
        isSignUpMode = signUp

        if (signUp) {
            binding.tabSignUp.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            binding.tabSignUp.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.tabSignIn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
            binding.tabSignIn.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))

            binding.tilFullName.visibility = View.VISIBLE
            binding.tilConfirmPassword.visibility = View.VISIBLE
            binding.btnForgotPassword.visibility = View.GONE
            binding.btnSubmit.text = "Create Account"
        } else {
            binding.tabSignIn.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            binding.tabSignIn.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.tabSignUp.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
            binding.tabSignUp.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))

            binding.tilFullName.visibility = View.GONE
            binding.tilConfirmPassword.visibility = View.GONE
            binding.btnForgotPassword.visibility = View.VISIBLE
            binding.btnSubmit.text = "Sign In"
        }
    }

    private fun setupRoleToggle() {
        binding.cbOfficerRole.setOnCheckedChangeListener { _, isChecked ->
            binding.tilBadgeId.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun setupAuthActions() {
        // Submit (Sign In or Sign Up via Firebase)
        binding.btnSubmit.setOnClickListener {
            handleEmailAuthSubmit()
        }

        // Google Sign-In via Firebase
        binding.btnGoogleSignIn.setOnClickListener {
            handleGoogleSignIn()
        }

        // Guest Mode
        binding.btnGuestMode.setOnClickListener {
            sessionManager.startGuestSession()
            Toast.makeText(this, "Browsing in Guest Mode", Toast.LENGTH_SHORT).show()
            navigateToMainActivity()
        }

        // Forgot Password via Firebase
        binding.btnForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Terms and Privacy Dialog
        binding.tvTermsPrivacy.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("🔒 Privacy & Civic Policy")
                .setMessage(
                    "Report-It is dedicated to civic empowerment and privacy:\n\n" +
                    "• Camera EXIF metadata is sanitized to ensure citizen anonymity.\n" +
                    "• Location data is solely used to verify and map civic infrastructure issues.\n" +
                    "• Inappropriate or abusive submissions are quarantined automatically."
                )
                .setPositiveButton("Understood", null)
                .show()
        }
    }

    private fun handleEmailAuthSubmit() {
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString()?.trim() ?: ""
        val isOfficer = binding.cbOfficerRole.isChecked
        val role = if (isOfficer) SessionManager.ROLE_OFFICER else SessionManager.ROLE_CITIZEN

        if (isSignUpMode) {
            val fullName = binding.etFullName.text?.toString()?.trim() ?: ""
            val confirmPass = binding.etConfirmPassword.text?.toString()?.trim() ?: ""

            if (fullName.isEmpty()) {
                binding.tilFullName.error = "Please enter your name"
                return
            } else {
                binding.tilFullName.error = null
            }

            if (!isValidEmail(email)) {
                binding.tilEmail.error = "Please enter a valid email address"
                return
            } else {
                binding.tilEmail.error = null
            }

            if (password.length < 6) {
                binding.tilPassword.error = "Password must be at least 6 characters"
                return
            } else {
                binding.tilPassword.error = null
            }

            if (password != confirmPass) {
                binding.tilConfirmPassword.error = "Passwords do not match"
                return
            } else {
                binding.tilConfirmPassword.error = null
            }

            // Firebase Create User
            binding.btnSubmit.isEnabled = false
            binding.btnSubmit.text = "Creating Account..."

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    user?.updateProfile(profileUpdates)

                    sessionManager.saveUserSession(
                        name = fullName,
                        email = email,
                        provider = SessionManager.PROVIDER_EMAIL,
                        role = role,
                        uid = user?.uid ?: ""
                    )

                    FirestoreRepository.saveUserProfile(
                        uid = user?.uid ?: "",
                        name = fullName,
                        email = email,
                        role = role
                    )

                    Toast.makeText(this, "Welcome to Report-It, $fullName!", Toast.LENGTH_LONG).show()
                    navigateToMainActivity()
                }
                .addOnFailureListener { exception ->
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Create Account"
                    Toast.makeText(this, "Registration failed: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                }

        } else {
            // Sign In Mode
            if (!isValidEmail(email)) {
                binding.tilEmail.error = "Please enter a valid email address"
                return
            } else {
                binding.tilEmail.error = null
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = "Please enter your password"
                return
            } else {
                binding.tilPassword.error = null
            }

            binding.btnSubmit.isEnabled = false
            binding.btnSubmit.text = "Signing In..."

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    val displayName = user?.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

                    sessionManager.saveUserSession(
                        name = displayName,
                        email = email,
                        provider = SessionManager.PROVIDER_EMAIL,
                        role = role,
                        uid = user?.uid ?: ""
                    )

                    FirestoreRepository.saveUserProfile(
                        uid = user?.uid ?: "",
                        name = displayName,
                        email = email,
                        role = role
                    )

                    Toast.makeText(this, "Signed in successfully as $displayName", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                }
                .addOnFailureListener { exception ->
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Sign In"
                    Toast.makeText(this, "Authentication failed: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun handleGoogleSignIn() {
        binding.btnGoogleSignIn.isEnabled = false
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        val name = user?.displayName ?: account.displayName ?: "Google Citizen"
                        val email = user?.email ?: account.email ?: ""
                        val uid = user?.uid ?: ""

                        sessionManager.saveUserSession(
                            name = name,
                            email = email,
                            provider = SessionManager.PROVIDER_GOOGLE,
                            role = SessionManager.ROLE_CITIZEN,
                            uid = uid
                        )

                        FirestoreRepository.saveUserProfile(
                            uid = uid,
                            name = name,
                            email = email,
                            role = SessionManager.ROLE_CITIZEN
                        )

                        Toast.makeText(this, "Signed in with Google ($email)", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }
                    .addOnFailureListener { exception ->
                        binding.btnGoogleSignIn.isEnabled = true
                        Toast.makeText(this, "Firebase Google Auth failed: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } else {
                binding.btnGoogleSignIn.isEnabled = true
                Toast.makeText(this, "Could not retrieve Google ID Token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            binding.btnGoogleSignIn.isEnabled = true
            Toast.makeText(this, "Google Sign-In failed (${e.statusCode}): ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showForgotPasswordDialog() {
        val emailInput = com.google.android.material.textfield.TextInputEditText(this).apply {
            hint = "Enter your registered email"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(binding.etEmail.text?.toString()?.trim() ?: "")
        }

        val container = android.widget.FrameLayout(this).apply {
            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, 8, padding, 8)
            addView(emailInput)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your email address and we'll send you Firebase password reset instructions.")
            .setView(container)
            .setPositiveButton("Send Reset Link") { _, _ ->
                val email = emailInput.text?.toString()?.trim() ?: ""
                if (isValidEmail(email)) {
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Password reset instructions sent to $email", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Failed to send reset link: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun navigateToMainActivity() {
        // Associate citizen ID and email with OneSignal push user profile
        val uid = auth.currentUser?.uid ?: sessionManager.getFirebaseUid()
        val email = auth.currentUser?.email ?: sessionManager.getUserEmail()
        if (uid.isNotEmpty()) {
            com.onesignal.OneSignal.login(uid)
            if (email.isNotEmpty()) {
                com.onesignal.OneSignal.User.addEmail(email)
            }
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
