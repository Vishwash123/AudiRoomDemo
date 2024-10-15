package com.example.audioroomdemozego

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView




class RoomRvAdapter(val context: Context,val vclist:List<VCroomRvData>,private val listener:onJoinLeaveClick):RecyclerView.Adapter<RoomRvAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var vcIcon: CircleImageView = itemView.findViewById(R.id.VC_room_rv_item_vc_image)
        var vcName: TextView = itemView.findViewById(R.id.VC_room_rv_item_vc_name)
        var vcMembers: TextView = itemView.findViewById(R.id.VC_room_rv_item_vc_member_count)
        var vcJoinLeave:TextView = itemView.findViewById(R.id.VC_room_rv_item_join_leave)

        init{
            vcJoinLeave.setOnClickListener {
                listener.onButtonClick(adapterPosition)
            }
        }



        fun bind2(item: VCroomRvData, itemClickListener: (VCroomRvData) -> Unit) {
            itemView.setOnClickListener {
                itemClickListener(item)
            }

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        return RoomViewHolder(
            LayoutInflater.from(context).inflate(R.layout.audio_room_rv_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return vclist.size
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val currentVC = vclist[position]
        val isJoined = (RoomManager.currentJoinedRoom) == currentVC.vcRoomId

        Glide.with(context)
            .load(currentVC.vcPhotoUri)
            .placeholder(R.drawable.default_room_placeholder) // Placeholder image
            .into(holder.vcIcon)

        holder.vcName.text = currentVC.vcName
        holder.vcJoinLeave.setBackgroundResource(if (isJoined) R.drawable.red_gradient_background else R.drawable.purple_gradient_voicemail)
        holder.vcJoinLeave.setText(if(isJoined)"Leave" else "Join")
        var extra: String
        if (currentVC.vcMembers == 1) {
            extra = " member"
        } else {
            extra = " members"
        }
        holder.vcMembers.text = currentVC.vcMembers.toString() + extra + " talking"
        //bind()
        //bind2()


    }
    interface onJoinLeaveClick{
        fun onButtonClick(adapterPosition: Int)

    }


}

