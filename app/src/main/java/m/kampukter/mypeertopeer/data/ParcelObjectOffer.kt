package m.kampukter.mypeertopeer.data

import android.os.Parcel
import android.os.Parcelable

class ParcelObjectOffer(val from: String?, val sdpOffer: String?) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(from)
        parcel.writeString(sdpOffer)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelObjectOffer> {
        override fun createFromParcel(parcel: Parcel): ParcelObjectOffer {
            return ParcelObjectOffer(parcel)
        }

        override fun newArray(size: Int): Array<ParcelObjectOffer?> {
            return arrayOfNulls(size)
        }
    }

}