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
        holder.binding.recyclerUsername.text = postList[position].email
        holder.binding.recyclerDetails.text = postList[position].details
        holder.binding.recyclerDate.text = postList[position].title
        Picasso.get().load(postList[position].downloadUrl).into(holder.binding.recyclerImageView)
    }
}