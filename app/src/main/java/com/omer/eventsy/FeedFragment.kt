package com.omer.eventsy

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.omer.eventsy.adapter.PostAdapter
import com.omer.eventsy.model.Post
import com.omer.eventsy.databinding.FragmentFeedBinding

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
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
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root

        // BOTTOM NAVIGATIONS
        val bottomNavigationView = (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.home -> if(findNavController().currentDestination?.id != R.id.feedFragment) findNavController().navigate(R.id.feedFragment)
                R.id.profile -> if(findNavController().currentDestination?.id != R.id.profileFragment) findNavController().navigate(R.id.profileFragment)
                R.id.settings -> if(findNavController().currentDestination?.id != R.id.settingsFragment) findNavController().navigate(R.id.settingsFragment)
                R.id.post -> if(findNavController().currentDestination?.id != R.id.postFragment) findNavController().navigate(R.id.postFragment)
            }
            true
        }
    return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirestoreDatas()

        adapter = PostAdapter(postList,false,null)
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter = adapter
    }
    private fun FirestoreDatas() {

        db.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if(error!=null) {
                Log.e("FeedFragment", "Firestore query failed: ${error.localizedMessage}")
                // Toast.makeText(requireContext(),"ads",Toast.LENGTH_LONG).show()
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


                            db.collection("Users").whereEqualTo("email", email).get()
                                .addOnSuccessListener { userDocs ->
                                    val profileImageUrl = userDocs.documents[0].getString("downloadUrl")
                                    val username = userDocs.documents[0].getString("username")

                                    val post = Post(email, details, title, downloadUrl, profileImageUrl, username)
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