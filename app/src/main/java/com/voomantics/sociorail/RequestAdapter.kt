package com.voomantics.sociorail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.emoji.widget.EmojiTextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class RequestAdapter(val context: Context, private val requestList: List<Request>) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {
    private lateinit var listener: OnItemClick

    interface OnItemClick {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClick) {
        this.listener = listener
    }

    open class RequestViewHolder(itemView: View, listener: OnItemClick, context: Context) : RecyclerView.ViewHolder(itemView) {
        private val image: CircleImageView = itemView.findViewById(R.id.requestImage)
        val name: EmojiTextView = itemView.findViewById(R.id.requestName)
        private val accept: FloatingActionButton = itemView.findViewById(R.id.acceptBtn)
        private val reject: FloatingActionButton = itemView.findViewById(R.id.rejectBtn)
        private lateinit var offer: Request
        private var mContext: Context = context

        fun bind(index: Request) {
            offer = index
            name.text = index.fromName
            Picasso.get().load(index.fromImageUrl).resize(50, 50).centerCrop().into(image)
        }

        init {
            itemView.setOnClickListener {
                if (itemView.id.equals(accept)) {
                    confirmResponse(itemView.id)
                } else if (itemView.id.equals(reject)) {
                    confirmResponse(itemView.id)
                } else {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position)
                    }
                }
            }
        }

        private fun sendResponse(response: String) {
            val ref = FirebaseDatabase.getInstance().getReference("Requests").child(offer.to!!).child(offer.from!!)
            val map = HashMap<String, String>()
            map["response"] = response
            ref.updateChildren(map as Map<String, Any>).addOnSuccessListener { Toast.makeText(mContext, "Response sent...", Toast.LENGTH_LONG).show() }
        }

        private fun confirmResponse(id: Int) {
            val ref = FirebaseDatabase.getInstance().getReference("Requests").child(offer.to!!).child(offer.from!!)
            ref
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (i in snapshot.children) {
                                val request = i.value as Request
                                if (request.response!! == "timed out") {
                                    accept.isEnabled = false
                                    reject.isEnabled = false
                                } else {
                                    if (id.equals(accept)) {
                                        sendResponse("Accepted")
                                    } else {
                                        sendResponse("Rejected")
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.request_row_item, parent, false)
        return RequestViewHolder(view, listener, context)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val index = requestList[position]
        holder.bind(index)
    }

    override fun getItemCount() = requestList.size
}