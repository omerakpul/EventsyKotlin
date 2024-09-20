package com.omer.eventsy

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.omer.eventsy.databinding.FragmentSignUpBinding


class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.textViewBackToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
        binding.signUpBtn.setOnClickListener { signup(it) }

        binding.editTextPasswordAgain.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //NO ACTION NEEDED
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Compare passwords whenever the text in the confirmation field changes
                val password = binding.editTextPassword.text.toString()
                val rePassword = binding.editTextPasswordAgain.text.toString()

                if (rePassword.isNotEmpty()) {
                    if (password == rePassword) {
                        // Passwords match, clear any error
                        binding.editTextPasswordAgain.error = null
                    } else {
                        // Passwords do not match, show error
                        binding.editTextPasswordAgain.error = "Passwords do not match"
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //NO ACTION NEEDED
            }
        })
    }

    fun signup(view: View) {
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()
        val rePassword = binding.editTextPasswordAgain.text.toString()
        val username = binding.editTextUsername.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty() && rePassword.isNotEmpty() && username.isNotEmpty()) {
            if(password == rePassword) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if(task.isSuccessful) {

                        val currentUser = auth.currentUser
                        currentUser?.let {
                            val userId = it.uid
                            val userMap = hashMapOf(
                                "email" to email,
                                "username" to username
                            )
                            FirebaseFirestore.getInstance().collection("Users")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener {
                                    val action = SignUpFragmentDirections.actionSignUpFragmentToFeedFragment()
                                    Navigation.findNavController(view).navigate(action)
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(requireContext(), "Database error: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        // Kullanıcı kaydı başarısız oldu
                        Toast.makeText(requireContext(), task.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Passwords do not match!", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_LONG).show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}