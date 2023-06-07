package com.example.resoluteaitask

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.resoluteaitask.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        binding.btnGoogle.setOnClickListener {
            loginWithGoogle()
        }

        binding.btnGuest.setOnClickListener {
            guestLogin()
        }

    }

    private fun guestLogin() {

        val alert = AlertDialog.Builder(this)
        alert.setTitle("Guest Login")
            .setMessage("You are exploring as a guest, this account will be valid for 30 days from now.")
            .setPositiveButton("Continue as a guest"){_,_->

                val dialog = setProgressDialog(this, "Signing you in...")
                dialog.show()

                auth.signInAnonymously().addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInAnonymously:success")
                        dialog.dismiss()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Log.w(TAG, "signInAnonymously:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            }
            .setNegativeButton("Cancel"){_,_->}
            .create()
            .show()
    }

    private fun loginWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleResult(task)
        } else {
//            Toast.makeText(this, "Result Not okay", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setProgressDialog(context: Context, message:String): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        if (completedTask.isSuccessful) {
            val account: GoogleSignInAccount? = completedTask.result
            if (account != null) {
                val dialog = setProgressDialog(this, "Signing you in...")
                dialog.show()
                updateUI(account, dialog)
            }
        } else{
            Toast.makeText(this, completedTask.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount, dialog: AlertDialog) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                Toast.makeText(this, "Sign in successful with\nUsername : ${account.displayName}\nEmail : ${account.email}", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }
}