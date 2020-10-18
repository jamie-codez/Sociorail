package com.voomantics.sociorail

import android.os.Parcel
import android.os.Parcelable

data class Token(val from: String?, val to: String?, val token: String?, val status: String?) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    constructor() : this("", "", "", "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(from)
        parcel.writeString(to)
        parcel.writeString(token)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Token> {
        override fun createFromParcel(parcel: Parcel): Token {
            return Token(parcel)
        }

        override fun newArray(size: Int): Array<Token?> {
            return arrayOfNulls(size)
        }
    }
}