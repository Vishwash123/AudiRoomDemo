package com.example.audioroomdemozego

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils.TruncateAt
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import com.zegocloud.uikit.utils.Utils
import de.hdodenhof.circleimageview.CircleImageView

class AudioRoomBackgroundView : FrameLayout {


    private lateinit var roomName: TextView
    private lateinit var roomID: TextView
    private lateinit var profileImageView: CircleImageView
    private var shouldSet = true

    constructor(@NonNull context: Context) : super(context) {
        initView()
    }

    constructor(@NonNull context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(@NonNull context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    private fun initView() {
        shouldSet = true
        setPadding(
            Utils.dp2px(16f, resources.displayMetrics),
            Utils.dp2px(16f, resources.displayMetrics),
            Utils.dp2px(16f, resources.displayMetrics),
            Utils.dp2px(16f, resources.displayMetrics)
        )

        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val marginEnd = Utils.dp2px(12f, resources.displayMetrics)
            setMargins(0, 0, marginEnd, 0)
        }

        val typeFace = Typeface.createFromAsset(context.assets, "font/poppins_semibold.ttf")

        roomName = TextView(context).apply {
            maxLines = 3
            ellipsize = TruncateAt.END
            typeface = typeFace
            paint.isFakeBoldText = true
            setTextColor(Color.parseColor("#FFFFFFFF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            maxWidth = Utils.dp2px(200f, resources.displayMetrics)
        }
        linearLayout.addView(roomName, params)

        roomID = TextView(context).apply {
            maxLines = 1
            ellipsize = TruncateAt.END
            typeface = typeFace
            setSingleLine(true)
            setTextColor(Color.parseColor("#D5D4D0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            maxWidth = Utils.dp2px(120f, resources.displayMetrics)
        }
        linearLayout.addView(roomID, params)

        val linearLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val ms = Utils.dp2px(16f, resources.displayMetrics)
            val mt = Utils.dp2px(10f, resources.displayMetrics)
            setMargins(ms, mt, 0, 0)
        }

        addView(linearLayout, linearLayoutParams)

        profileImageView = CircleImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                Utils.dp2px(170f, resources.displayMetrics), // Width
                Utils.dp2px(170f, resources.displayMetrics)  // Height
            ).apply {
                gravity = Gravity.CENTER // Center the image

                topMargin = Utils.dp2px(32f, resources.displayMetrics) // Margin from top
            }
            setBackgroundResource(R.drawable.circle_outline) // Set background
            setImageResource(R.drawable.profileplaceholder) // Set the default image
            setPadding(
                Utils.dp2px(30f, resources.displayMetrics), // Left padding
                Utils.dp2px(30f, resources.displayMetrics), // Top padding
                Utils.dp2px(30f, resources.displayMetrics), // Right padding
                Utils.dp2px(30f, resources.displayMetrics)  // Bottom padding
            )
        }

        addView(profileImageView) // Add the CircleImageView to the FrameLayout
    }

    fun setRoomName(roomName: String) {
        this.roomName.text = roomName
    }

    fun setRoomID(roomID: String) {
        this.roomID.text = "ID: $roomID"
    }

    fun setProfileImage(imageUrl: String) {
        if(shouldSet) {
            Glide.with(profileImageView.context)
                .load(imageUrl) // Load image from URL
                .placeholder(R.drawable.profileplaceholder) // Placeholder image
                .error(R.drawable.profileplaceholder) // Error image
                .into(profileImageView)
        }
    }

    fun disableImageSetting(){
        profileImageView.setImageResource(R.drawable.profileplaceholder)
        shouldSet=false;
    }
    fun enableImageSetting(){
        shouldSet = true;
    }

}
