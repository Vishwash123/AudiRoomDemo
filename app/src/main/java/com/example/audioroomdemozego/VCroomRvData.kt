package com.example.audioroomdemozego

import android.net.Uri

data class VCroomRvData(val vcRoomId:String?=null,
                        val vcPhotoUri: String?=null,
                        val vcName:String="",
                        var vcMembers:Int=0,
                        var hostUserId:String="")