package com.voomantics.sociorail

import android.os.Parcel
import android.os.Parcelable

data class UserRowItem(val userName:String?,val uid:String?,val imageUrl:String?,val search:String?):Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }
    constructor():this("","","","")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userName)
        parcel.writeString(uid)
        parcel.writeString(imageUrl)
        parcel.writeString(search)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserRowItem> {
        override fun createFromParcel(parcel: Parcel): UserRowItem {
            return UserRowItem(parcel)
        }

        override fun newArray(size: Int): Array<UserRowItem?> {
            return arrayOfNulls(size)
        }
    }
}