package com.example.audioroomdemozego
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.audioroomdemozego.databinding.ActivityVoiceCallBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import android.Manifest


import com.zegocloud.uikit.prebuilt.liveaudioroom.ZegoUIKitPrebuiltLiveAudioRoomConfig
import com.zegocloud.uikit.prebuilt.liveaudioroom.ZegoUIKitPrebuiltLiveAudioRoomFragment
import com.zegocloud.uikit.prebuilt.liveaudioroom.ZegoUIKitPrebuiltLiveAudioRoomService
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomLayoutAlignment
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomLayoutRowConfig
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler
import java.util.HashMap



class VoiceCallActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE=1001
    private val SILENCE_TIMEOUT = 1500L
    private lateinit var roomid: String
    private lateinit var binding: ActivityVoiceCallBinding
    private var isFinishing:Boolean = false
    private var silenceHandler: Handler = Handler(Looper.getMainLooper())
    private var localSilenceRunnable: Runnable? = null
    private var remoteSilenceRunnable: Runnable? = null
    private var silenceRunnable: Runnable? = null
    private val SOUND_THRESHOLD = 0.3f
    private lateinit var roomBackgroundView:AudioRoomBackgroundView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startService(Intent(this, CallService::class.java))
        val roomID = intent.getStringExtra("roomID")
        roomid = roomID!!
        val roomName = intent.getStringExtra("roomName")

        if(savedInstanceState==null)
        addFragment()


        checkPermissions()

    }

    private fun addFragment() {
            val appID = intent.getLongExtra("appID", 0L)
            val appSign = intent.getStringExtra("appSign")
            val userID = intent.getStringExtra("userID")
            val userName = intent.getStringExtra("userName")
            val isHost = intent.getBooleanExtra("host", false)
            val roomName = intent.getStringExtra("roomName")
            val userPhoto = intent.getStringExtra("userPhoto")
        
            val config = ZegoUIKitPrebuiltLiveAudioRoomConfig.host()

            config.userAvatarUrl = userPhoto
//        Log.d("checking url","url is $userPhoto")

            config.layoutConfig.rowConfigs = listOf(
                ZegoLiveAudioRoomLayoutRowConfig(0,ZegoLiveAudioRoomLayoutAlignment.SPACE_AROUND)
            )
            val fragment = ZegoUIKitPrebuiltLiveAudioRoomFragment.newInstance(
                appID,
                appSign, userID, userName, roomid, config
            )

            config.turnOnMicrophoneWhenJoining = true



    //        config.seatConfig.backgroundImage = ContextCompat.getDrawable(this,R.drawable.circle_outline)

            fragment.setUserCountOrPropertyChangedListener { user ->
                updateMemberCountOnLeave(user.size)
//                Toast.makeText(this, "${user.size} in the room", Toast.LENGTH_SHORT).show()
            }






//        val sv = AudioRoomSeatView.inflate(this,R.layout.zego_seat,null)

            supportFragmentManager.beginTransaction().replace(R.id.fragment_container2, fragment)
                .commitNow()

        val stream = ZegoUIKitPrebuiltLiveAudioRoomService.common.events.setExpressEngineEventHandler(object :IExpressEngineEventHandler(){
//

            override fun onRemoteSoundLevelUpdate(soundLevels: HashMap<String, Float>?) {
                super.onRemoteSoundLevelUpdate(soundLevels)

                val speakingUser = soundLevels?.maxByOrNull { it.value }?.key
                if (speakingUser != null && speakingUser != intent.getStringExtra("userID")) {
                    // Fetch avatar for remote user who is speaking
                    getUserAvatar(speakingUser, userID, userPhoto)
                    cancelSilenceTimer() // Cancel silence timer as a user is speaking
                } else {
                    startSilenceTimer() // No remote users speaking, start the silence timer
                }
            }

            override fun onCapturedSoundLevelUpdate(soundLevel: Float) {
                super.onCapturedSoundLevelUpdate(soundLevel)

                if (soundLevel > SOUND_THRESHOLD) {
                    // Local user is speaking
                    val localUserPhoto = intent.getStringExtra("userPhoto")
                    if (localUserPhoto != null) {
                        updateCurrentSpeakerAvatar(localUserPhoto)
                    }
                    cancelSilenceTimer() // Cancel the silence timer
                } else {
                    // No sound from local user, start silence timer to revert to placeholder
                    startSilenceTimer()
                }
            }

//override fun onRemoteSoundLevelUpdate(soundLevels: HashMap<String, Float>?) {
//    super.onRemoteSoundLevelUpdate(soundLevels)
//
//    val speakingUser = soundLevels?.maxByOrNull { it.value }?.key
//    if (speakingUser != null && speakingUser != intent.getStringExtra("userID")) {
//        // Remote user is speaking
//        getUserAvatar(speakingUser, userID, userPhoto)
//        cancelRemoteSilenceTimer()  // Cancel remote silence timer when a user is speaking
//    } else {
//        startRemoteSilenceTimer()  // Start silence timer when no remote user is speaking
//    }
//}
//
//            override fun onCapturedSoundLevelUpdate(soundLevel: Float) {
//                super.onCapturedSoundLevelUpdate(soundLevel)
//
//                if (soundLevel > SOUND_THRESHOLD) {
//                    // Local user is speaking
//                    val localUserPhoto = intent.getStringExtra("userPhoto")
//                    if (localUserPhoto != null) {
//                        updateCurrentSpeakerAvatar(localUserPhoto)
//                    }
//                    cancelLocalSilenceTimer()  // Cancel local silence timer when the local user is speaking
//                } else {
//                    // Start local silence timer when no sound from local user
//                    startLocalSilenceTimer()
//                }
//            }



        })






           roomBackgroundView = AudioRoomBackgroundView(this)
            roomBackgroundView.setRoomID(roomid)
            roomBackgroundView.setRoomName(roomName!!)
            roomBackgroundView.setBackgroundResource(R.drawable.zego_background)
            fragment.setBackgroundView(roomBackgroundView)
            roomBackgroundView.setProfileImage(Utils.PLACEHOLDER_AVATAR_URL!!)


    //        ZegoLiveAudioRoomSeatForegroundViewProvider()


    }

    private fun cancelLocalSilenceTimer() {
        localSilenceRunnable?.let { silenceHandler.removeCallbacks(it) }
    }

    private fun startLocalSilenceTimer() {
        localSilenceRunnable?.let { silenceHandler.removeCallbacks(it) }  // Remove any previous callbacks
        localSilenceRunnable = Runnable {
            updateCurrentSpeakerAvatar(Utils.PLACEHOLDER_AVATAR_URL)
        }
        silenceHandler.postDelayed(localSilenceRunnable!!, SILENCE_TIMEOUT)
    }

    // Remote Silence Timer
    private fun cancelRemoteSilenceTimer() {
        remoteSilenceRunnable?.let { silenceHandler.removeCallbacks(it) }
    }

    private fun startRemoteSilenceTimer() {
        remoteSilenceRunnable?.let { silenceHandler.removeCallbacks(it) }  // Remove any previous callbacks
        remoteSilenceRunnable = Runnable {
            updateCurrentSpeakerAvatar(Utils.PLACEHOLDER_AVATAR_URL)
        }
        silenceHandler.postDelayed(remoteSilenceRunnable!!, SILENCE_TIMEOUT)
    }
    private fun cancelSilenceTimer() {
        silenceRunnable?.let { silenceHandler.removeCallbacks(it) }
    }
    private fun startSilenceTimer() {
        silenceRunnable?.let { silenceHandler.removeCallbacks(it) } // Remove previous callbacks
        silenceRunnable = Runnable {
            // After SILENCE_TIMEOUT, set the avatar to the placeholder
            updateCurrentSpeakerAvatar(Utils.PLACEHOLDER_AVATAR_URL)
        }
        silenceHandler.postDelayed(silenceRunnable!!, SILENCE_TIMEOUT)
    }
    private fun getUserAvatar(userid: String?, userID: String?, userPhoto: String?) {

        if (userid != null) {
            // Extract the actual Firebase user ID
            val actualUserId = userid.split("_").getOrNull(1) // Get the part after the underscore
            if (actualUserId != null) {
                if (actualUserId == intent.getStringExtra("userID")) {
                    // If it's the current user, use the pre-existing userPhoto passed via Intent
                    val currentUserPhoto = intent.getStringExtra("userPhoto")
                    if (currentUserPhoto != null) {
                        updateCurrentSpeakerAvatar(currentUserPhoto)
                    } else {
                        Log.e("VoiceCallActivity", "User photo URL is null for the current user.")
                    }
                } else {
                    // Fetch the avatar for the other speaking user from Firebase
                    val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(actualUserId)
                    databaseRef.child("photourl").get().addOnSuccessListener { snapshot ->
                        Log.d("VoiceCallActivity", "Snapshot value: ${snapshot.value}") // Log the snapshot value
                        val avatarUrl = snapshot.value as? String
                        if (avatarUrl != null) {
                            Log.d("VoiceCallActivity", "Avatar URL for user $userid: $avatarUrl")
                            updateCurrentSpeakerAvatar(avatarUrl)
                        } else {
                            Log.d("VoiceCallActivity", "Avatar not found for user: $userid")
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("VoiceCallActivity", "Failed to retrieve avatar for user: $userid. Error: ${exception.message}")
                    }
                }
            } else {
                Log.e("VoiceCallActivity", "Extracted user ID is null from composite user ID.")
            }
        } else {
            Log.e("VoiceCallActivity", "User ID is null.")
        }
    }

    private fun updateCurrentSpeakerAvatar(userAvatarUrl: String) {
            roomBackgroundView.setProfileImage(userAvatarUrl)
    }


//    override fun onStop() {
//        super.onStop()
//        isFinishing = true
//        RoomManager.currentJoinedRoom = ""
//        updateMemberCountOnLeave()
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//        if(!isFinishing) {
//
//            RoomManager.currentJoinedRoom = ""
//            updateMemberCountOnLeave()
//        }
//        isFinishing = false;
//    }
    override fun onStop() {
        super.onStop()
        roomBackgroundView.disableImageSetting()
    }

    override fun onResume() {
        super.onResume()
        roomBackgroundView.enableImageSetting()

    }

    override fun onRestart() {
        super.onRestart()
        roomBackgroundView.enableImageSetting()
    }
    override fun onDestroy() {

        super.onDestroy()
        stopService(Intent(this, CallService::class.java))
    }
//
//    override fun onPause() {
//        super.onPause()
//        updateCurrentSpeakerAvatar(Utils.PLACEHOLDER_AVATAR_URL)
//    }

    private fun updateMemberCountOnLeave(users:Int) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomid!!)
        databaseRef.child("vcMembers").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentCount: MutableData): Transaction.Result {
                val currentValue = currentCount.value as? Long ?: return Transaction.success(currentCount)
                currentCount.value = users

                return Transaction.success(currentCount)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                Toast.makeText(this@VoiceCallActivity,"Room Left",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this@VoiceCallActivity,Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_OWN_CALLS) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL,
                    Manifest.permission.MANAGE_OWN_CALLS
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions are already granted, start the CallService
            startCallService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions granted, start the CallService
                startCallService()
            } else {
                // Handle the case where permissions are denied
                Toast.makeText(this, "Permissions are required to start the service.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCallService() {
        // Start the CallService here
        startService(Intent(this, CallService::class.java))
    }

}

