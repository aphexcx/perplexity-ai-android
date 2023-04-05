package cx.aphex.perplexity

import android.graphics.Color
import android.os.Parcel
import android.text.TextPaint
import android.text.style.BackgroundColorSpan


class MutableBackgroundColorSpan : BackgroundColorSpan {
    private var mAlpha = 255
    private var mBackgroundColor: Int

    constructor(alpha: Int, color: Int) : super(color) {
        mAlpha = alpha
        mBackgroundColor = color
    }

    constructor(src: Parcel) : super(src) {
        mBackgroundColor = src.readInt()
        mAlpha = src.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(mBackgroundColor)
        dest.writeFloat(mAlpha.toFloat())
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.bgColor = backgroundColor
    }

    /**
     * @param alpha from 0 to 255
     */
    fun setAlpha(alpha: Int) {
        mAlpha = alpha
    }

    fun setBackgroundColor(backgroundColor: Int) {
        mBackgroundColor = backgroundColor
    }

    val alpha: Float
        get() = mAlpha.toFloat()

    override fun getBackgroundColor(): Int {
        return Color.argb(
            mAlpha,
            Color.red(mBackgroundColor),
            Color.green(mBackgroundColor),
            Color.blue(mBackgroundColor)
        )
    }
}

