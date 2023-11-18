package edu.farmingdale.alrajab.week12_auth_ml_api_demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import edu.farmingdale.alrajab.week12_auth_ml_api_demo.databinding.ActivityLandingBinding

class LandingActivity : AppCompatActivity() {
	private lateinit var firebaseAuth: FirebaseAuth
	private lateinit var binding: ActivityLandingBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityLandingBinding.inflate(layoutInflater)
		setContentView(binding.root)

		binding.logoutBtn.setOnClickListener { logout() }

		firebaseAuth = FirebaseAuth.getInstance()
	}

	private fun logout() {
		firebaseAuth.signOut()
		startActivity(Intent(this@LandingActivity, LoginActivity::class.java))
		finish()
	}
}


