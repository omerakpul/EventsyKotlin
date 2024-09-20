package com.omer.eventsy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.omer.eventsy.ProfileFragment
import com.omer.eventsy.databinding.RecyclerRowBinding
import com.omer.eventsy.model.Post
import com.squareup.picasso.Picasso

class PostAdapter(private val postList : ArrayList<Post>,
                  private val isFragmentProfile: Boolean) : RecyclerView.Adapter<PostAdapter.PostHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class PostHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
         return postList.size
    }
    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        val post = postList[position]
        holder.binding.recyclerUsername.text = post.username
        holder.binding.recyclerDetails.text = post.details
        holder.binding.recyclerDate.text = post.title
        Picasso.get().load(post.downloadUrl).into(holder.binding.recyclerImageView)
        Picasso.get().load(post.profileImageUrl).into(holder.binding.recyclerProfilePicture)

        fun deletePost(documentId: String, position: Int) {
            db.collection("Posts").document(documentId)
                .delete()
                .addOnSuccessListener {
                    // Başarıyla silindi, listeden post'u çıkar
                    postList.removeAt(position)
                    notifyItemRemoved(position)
                }
                .addOnFailureListener { e ->
                    // Hata durumunda bir şeyler yapılabilir (Log veya Toast)
                    println("Error deleting post: ${e.localizedMessage}")
                }
        }
        if(isFragmentProfile){
            holder.binding.recyclerDelete.visibility = View.VISIBLE

            holder.binding.recyclerDelete.setOnClickListener {
                deletePost(post.id, position)
            }
        } else {
            holder.binding.recyclerDelete.visibility = View.GONE
        }
    }
}