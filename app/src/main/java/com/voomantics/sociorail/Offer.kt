package com.voomantics.sociorail

import android.os.Parcel
import android.os.Parcelable

data class Offer(val Address: String?,
                 val category: String?,
                 val dpFromDate: String?,
                 val dpToDate: String?,
                 val establishment: String?,
                 val `file`: String?,
                 val fileSource: String?,
                 val key: String?,
                 val latitude: String?,
                 val literature: String?,
                 val location: String?,
                 val longitude: String?,
                 val nameToSearch: String?,
                 val title: String?) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }
    constructor():this("","","","","","","","","","","","","","")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Address)
        parcel.writeString(category)
        parcel.writeString(dpFromDate)
        parcel.writeString(dpToDate)
        parcel.writeString(establishment)
        parcel.writeString(file)
        parcel.writeString(fileSource)
        parcel.writeString(key)
        parcel.writeString(latitude)
        parcel.writeString(literature)
        parcel.writeString(location)
        parcel.writeString(longitude)
        parcel.writeString(nameToSearch)
        parcel.writeString(title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Offer> {
        override fun createFromParcel(parcel: Parcel): Offer {
            return Offer(parcel)
        }

        override fun newArray(size: Int): Array<Offer?> {
            return arrayOfNulls(size)
        }
    }
}