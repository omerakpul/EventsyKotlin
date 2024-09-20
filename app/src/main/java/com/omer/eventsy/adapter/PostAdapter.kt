package com.omer.eventsy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omer.eventsy.databinding.RecyclerRowBinding
import com.omer.eventsy.model.Post
import com.squareup.picasso.Picasso

class PostAdapter(private val postList : ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>() {

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
    }
}