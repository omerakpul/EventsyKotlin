package com.omer.eventsy

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.omer.eventsy.adapter.PostAdapter
import com.omer.eventsy.databinding.FragmentProfileBinding
import com.omer.eventsy.model.Post
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    val postList : ArrayList<Post> = arrayListOf()
    private var adapter : PostAdapter? = null
    private lateinit var storage : FirebaseStorage
    private lateinit var reference : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage
        reference = storage.reference

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirestoreDatas()
        loadProfileData()


        adapter = PostAdapter(postList,true)
        binding.userPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.userPosts.adapter = adapter

    }

    private fun loadProfileData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                val profileImageUrl = document.getString("downloadUrl")
                val username = document.getString("username")

                // Update profile picture
                if (profileImageUrl != null) {
                    Picasso.get()
                        .load(profileImageUrl)
                        .placeholder(R.drawable.icons8_user_48) // Varsayılan resim
                        .error(R.drawable.baseline_error_outline_24) // Hata durumunda varsayılan resim
                        .into(binding.profilePicture)
                } else {
                    binding.profilePicture.setImageResource(R.drawable.icons8_user_48)
                }
                // Update username
                if (username != null) {
                    binding.username.text = username
                }
            } else {
                Toast.makeText(requireContext(), "User document not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Failed to load user data: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun FirestoreDatas() {

        val currentUserEmail = auth.currentUser?.email

        db.collection("Posts").orderBy("date", Query.Direction.DESCENDING).whereEqualTo("email" , currentUserEmail).addSnapshotListener { value, error ->
            if(error!=null) {
                Log.e("ProfileFragment", "Firestore query failed: ${error.localizedMessage}")
            } else {
                if (value != null) {
                    if (!value.isEmpty) {
                        postList.clear()
                        val documents = value.documents
                        for (document in documents) {
                            val details = document.get("details") as String
                            val email = document.get("email") as String
                            val title = document.get("title") as String
                            val downloadUrl = document.get("downloadUrl") as String

                            val documentId = document.id

                            db.collection("Users").whereEqualTo("email", email).get()
                                .addOnSuccessListener { userDocs ->
                                    val profileImageUrl = userDocs.documents[0].getString("downloadUrl")
                                    val username = userDocs.documents[0].getString("username")

                                    val post = Post(email, details, title, downloadUrl, profileImageUrl,username,documentId)
                                    postList.add(post)
                                    adapter?.notifyDataSetChanged()
                                }
                        }
                    }
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}