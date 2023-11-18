package edu.farmingdale.alrajab.week12_auth_ml_api_demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import edu.farmingdale.alrajab.week12_auth_ml_api_demo.databinding.ActivityLoginBinding
import java.lang.Exception

class LoginActivity : AppCompatActivity(), OnCompleteListener<AuthResult> {

	/**
	 * View binding.
	 */
	private lateinit var binding: ActivityLoginBinding

	/**
	 * Firebase authentication handler.
	 */
	private lateinit var firebaseAuth: FirebaseAuth

	/**
	 * Client to call for sign in events.
	 */
	private lateinit var gsoClient: GoogleSignInClient

	/**
	 * Launcher to get a google sign on.
	 */
	private val gsoSignInLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
		try {
			val account = task.getResult(ApiException::class.java)
			val idToken = account.idToken
			if (idToken != null) {
				authWithGoogle(idToken)
			}
		} catch (e: ApiException) {
			handleException(e)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityLoginBinding.inflate(layoutInflater)
		setContentView(binding.root)

		firebaseAuth = FirebaseAuth.getInstance()

		gsoClient = GoogleSignIn.getClient(
			this,
			GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build()
		)

		if (firebaseAuth.currentUser != null)
			landing()

		binding.signUpTv.setOnClickListener {
			val intent = Intent(this, SignUpActivity::class.java)
			startActivity(intent)
		}
		binding.loginBtn.setOnClickListener {
			login()
		}

		binding.googlelogin.setOnClickListener {
			googleLogin()
		}
	}

	/**
	 * Launch google sign on
	 */
	private fun googleLogin() {
		gsoSignInLauncher.launch(gsoClient.signInIntent)
	}

	/**
	 * Authenticate with firebase using the idToken
	 */
	private fun authWithGoogle(idToken: String) {
		val credential = GoogleAuthProvider.getCredential(idToken, null)
		firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, this)
	}

	/**
	 * Move to the landing activity
	 */
	private fun landing() {
		val intent = Intent(this, LandingActivity::class.java)
		startActivity(intent)
		finish()
	}

	/**
	 * Login using email and password
	 */
	private fun login() {
		val email = binding.emailET.text.toString()
		val pass = binding.passET.text.toString()

		if (email.isNotEmpty() && pass.isNotEmpty()) {
			firebaseAuth.signInWithEmailAndPassword(email, pass)
				.addOnCompleteListener(this, this)
				.addOnFailureListener(::handleException)
		} else {
			Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
		}
	}

	/**
	 * OnComplete listener for successful sign ins
	 */
	override fun onComplete(task: Task<AuthResult>) {
		if (task.isSuccessful) {
			landing()
		} else {
			handleException(task.exception)
		}
	}

	/**
	 * Convenience function to handle exceptions.
	 */
	private fun handleException(e: Exception?) {
		e?.printStackTrace()
		Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
	}
}