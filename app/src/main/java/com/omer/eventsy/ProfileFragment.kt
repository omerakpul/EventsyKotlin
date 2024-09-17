package com.omer.eventsy

import android.os.Bundle
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
import com.omer.eventsy.adapter.PostAdapter
import com.omer.eventsy.databinding.FragmentFeedBinding
import com.omer.eventsy.databinding.FragmentProfileBinding
import com.omer.eventsy.model.Post

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    val postList : ArrayList<Post> = arrayListOf()
    private var adapter : PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
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

        adapter = PostAdapter(postList)
        binding.userPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.userPosts.adapter = adapter
    }

    private fun FirestoreDatas() {

        val currentUserEmail = auth.currentUser?.email

        db.collection("Posts").orderBy("date", Query.Direction.DESCENDING).whereEqualTo("email" , currentUserEmail).addSnapshotListener { value, error ->
            if(error!=null) {
                Toast.makeText(requireContext(),error.localizedMessage, Toast.LENGTH_LONG).show()
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

                            val post = Post(email, details, title, downloadUrl)
                            postList.add(post)
                        }
                        adapter?.notifyDataSetChanged()
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