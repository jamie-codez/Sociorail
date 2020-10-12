package com.voomantics.sociorail

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


class OfferAdapter(val context: Context, private val offerList: List<Offer>, private var listener: OnItemClick?) : RecyclerView.Adapter<OfferAdapter.OfferViewHolder>() {

    interface OnItemClick {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClick) {
        this.listener = listener
    }

    open class OfferViewHolder(itemView: View, listener: OnItemClick?) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.offer_image)
        private val title: TextView = itemView.findViewById(R.id.offer_title)
        private val description: TextView = itemView.findViewById(R.id.offer_description)
        private val dates: TextView = itemView.findViewById(R.id.offer_dates)
        private val location: TextView = itemView.findViewById(R.id.offer_location)
        private val establishment: TextView = itemView.findViewById(R.id.establishment)
        private val locality: TextView = itemView.findViewById(R.id.locality)

        fun bind(offer: Offer) {
            title.text = offer.title
            description.text = offer.literature
            val format = String.format("From: %s\nTo: %s", offer.dpFromDate, offer.dpToDate)
            dates.text = format
            location.text = offer.location
            establishment.text = String.format("Est: %s", offer.establishment)
            locality.text = String.format("Locality: \n %s", offer.location)
            val decodedString: ByteArray = Base64.decode(offer.fileSource, Base64.DEFAULT)
            val bitMap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            try {
                image.setImageBitmap(bitMap)

            }catch (e:Exception){
                e.printStackTrace()
                image.setImageResource(R.drawable.error)
            }
//            Picasso.get()
//                    .load(offer.fileSource)
//                    .placeholder(R.drawable.image)   // optional
//                    .error(R.drawable.error)      // optional
//                    .resize(300, 200)// optional
//                    .centerInside()
//                    .into(image)
        }

        init {
            itemView.setOnClickListener {
                val position: Int = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(position)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.offer_row_item, parent, false)
        return OfferViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val index = offerList[position]
        holder.bind(index)
    }

    override fun getItemCount() = offerList.size
}
