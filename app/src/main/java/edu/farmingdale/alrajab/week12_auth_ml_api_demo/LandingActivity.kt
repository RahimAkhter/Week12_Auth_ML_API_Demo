package edu.farmingdale.alrajab.week12_auth_ml_api_demo

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.core.view.isVisible
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import edu.farmingdale.alrajab.week12_auth_ml_api_demo.databinding.ActivityLandingBinding
import java.io.IOException
import java.lang.Exception

/**
 *
 */
class LandingActivity : AppCompatActivity() {
	/**
	 * Firebase authentication handler.
	 */
	private lateinit var firebaseAuth: FirebaseAuth

	/**
	 * View binding.
	 */
	private lateinit var binding: ActivityLandingBinding

	/**
	 * Recognizer class.
	 */
	private val recognizer: TextRecognizer =
		TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

	/**
	 * Labeler class.
	 */
	private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

	/**
	 * Image uri holder.
	 */
	private var imageUri: Uri? = null

	/**
	 * Launcher to select an image.
	 */
	private val pickMedia =
		registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
			imageUri = uri

			val hasSelected = uri != null

			binding.recognize.isEnabled = hasSelected
			binding.generate.isEnabled = hasSelected
			binding.extractedText.text = null
			binding.labels.text = null

			binding.imageHolder.setImageURI(imageUri)
		}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityLandingBinding.inflate(layoutInflater)
		setContentView(binding.root)

		firebaseAuth = FirebaseAuth.getInstance()

		binding.username.text = firebaseAuth.currentUser!!.displayName

		binding.imageButton.setOnClickListener {
			pickMedia.launch(PickVisualMediaRequest(ImageOnly))
		}

		binding.recognize.setOnClickListener { extractText() }

		binding.generate.setOnClickListener { generateLabels() }

		binding.logoutBtn.setOnClickListener { logout() }

	}

	/**
	 * Convenience function to logout.
	 */
	private fun logout() {
		firebaseAuth.signOut()
		startActivity(Intent(this@LandingActivity, LoginActivity::class.java))
		finish()
	}

	/**
	 * Handler to start an operation.
	 *
	 * @param action to perform with the user selected image.
	 */
	private fun startHandler(action: (InputImage) -> Unit) {
		val uri = imageUri

		if (uri != null) {
			val image: InputImage

			try {
				image = InputImage.fromFilePath(this, uri)
			} catch (exception: IOException) {
				handleException(exception)
				return
			}

			binding.progressBar.isVisible = true
			action(image)
		}
	}

	/**
	 * Start process to extract text from the image.
	 */
	private fun extractText() {
		startHandler { image ->
			recognizer.process(image)
				.addOnCompleteListener(::onRecognizeComplete)
				.addOnFailureListener(::handleException)
		}
	}

	/**
	 * Start process to generate labels based on the image.
	 */
	private fun generateLabels() {
		startHandler { image ->
			labeler.process(image)
				.addOnCompleteListener(::onLabelComplete)
				.addOnFailureListener(::handleException)
		}
	}

	/**
	 * Handler to stop an action.
	 *
	 * @param action to call with [Task.getResult].
	 */
	private fun <T> Task<T>.completeHandler(action: (T) -> Unit) {
		if (isSuccessful) {
			action(result)
		} else {
			handleException(exception)
		}
		binding.progressBar.isVisible = false
	}

	/**
	 * Action to run when recognizing is done.
	 */
	private fun onRecognizeComplete(task: Task<Text>) = task.completeHandler {
		binding.extractedText.text = it.text
	}

	/**
	 * Action to run when labeling done.
	 */
	private fun onLabelComplete(task: Task<List<ImageLabel>>) = task.completeHandler { labels ->
		binding.labels.text = labels.joinToString { it.text }
	}

	/**
	 * Convenience function to handle exception by toasting it and printing it.
	 */
	private fun handleException(e: Exception?) {
		e?.printStackTrace()
		Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
	}
}


