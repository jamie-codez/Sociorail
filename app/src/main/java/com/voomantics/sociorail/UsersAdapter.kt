package com.voomantics.sociorail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class UsersAdapter(val context: Context, private val usersList: List<UserRowItem>) : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {
    private lateinit var listener:OnItemClick
    interface OnItemClick{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: OnItemClick){
        this.listener = listener
    }

    open class UsersViewHolder(itemView: View,listener: OnItemClick) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.pic_view_circle)
        val name: TextView = itemView.findViewById(R.id.user_text_view)

        fun bind(index: UserRowItem) {
            name.text = index.userName
            Picasso.get().load(index.imageUrl).resize(50,50).centerCrop().into(image)
        }
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position!=RecyclerView.NO_POSITION){
                    listener.onItemClick(position)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_row_item, parent, false)
        return UsersViewHolder(view,listener)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val index = usersList[position]
        holder.bind(index)
    }

    override fun getItemCount() = usersList.size
}