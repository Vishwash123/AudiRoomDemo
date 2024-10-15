package com.example.audioroomdemozego

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.audioroomdemozego.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import im.zego.zegoexpress.ZegoExpressEngine
import java.util.UUID
import kotlin.random.Random

class HomeActivity : AppCompatActivity(), RoomRvAdapter.onJoinLeaveClick {

    private lateinit var audioRoomRv: RecyclerView
    private lateinit var binding: ActivityHomeBinding
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseDataBase: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorageReference: StorageReference

    private lateinit var roomPhoto: CircleImageView
    private lateinit var imageUri: Uri
    private lateinit var user: FirebaseUser
    private lateinit var roomAdapter: RoomRvAdapter
    private lateinit var roomListener:ValueEventListener

    private val PICK_IMAGE_REQUEST = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = (application as MyApplication).firebaseAuth
        firebaseDataBase = (application as MyApplication).firebaseDataBase
        firebaseStorage = (application as MyApplication).firebaseStorage
        firebaseStorageReference = (application as MyApplication).firebaseStorageReference
        user = firebaseAuth.currentUser!!
        setupUI()
        listenForRoomUpdates()



    }


    private fun setupUI() {
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        audioRoomRv = binding.RoomRv
        audioRoomRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        roomAdapter = RoomRvAdapter(this, VCdata.getList(), this)
        audioRoomRv.adapter = roomAdapter
        binding.createRoomButton.setOnClickListener{
            showDialog(user,generateRoomId())
        }

        binding.LogoutRoomButton.setOnClickListener{
            userLogOut()
        }


        binding.homeUsername.text = user.displayName
        binding.homeEmail.text = user.email
        val profileImageUrl = user.photoUrl
        if (profileImageUrl != null) {
            Toast.makeText(this,"showing photo",Toast.LENGTH_SHORT).show()
            Glide.with(this)
                .load(profileImageUrl)
                .into(binding.circleImageView2) // Assuming signUpProfilePic is your ImageView
        } else {
            // Set a default image if the profile picture is not available
            Toast.makeText(this,"phot url null cant get photo",Toast.LENGTH_SHORT).show()
            binding.circleImageView2.setImageResource(R.drawable.profileplaceholder) // Your default image
        }




        listenForRoomUpdates()


    }



    @SuppressLint("SuspiciousIndentation")
    override fun onButtonClick(adapterPosition: Int) {
        val currentRoom = VCdata.roomList[adapterPosition]
            joinRoom(currentRoom)

    }

    private fun userLogOut(){
        firebaseAuth.signOut()
        startActivity(Intent(this@HomeActivity,MainActivity::class.java))

    }


    private fun joinRoom(room: VCroomRvData) {
        val currentUserID = user.uid
        val isHost = room.hostUserId == currentUserID // Check if current user is the host

        // Update member count in Firebase
//        val databaseRef = FirebaseDatabase.getInstance().getReference("rooms").child(room.vcRoomId!!)
//        databaseRef.child("vcMembers").setValue(room.vcMembers + 1).addOnCompleteListener { task ->
//            if (task.isSuccessful) {
                RoomManager.currentJoinedRoom = room.vcRoomId!!
                // Proceed to join room
                val intent: Intent = Intent(this@HomeActivity, VoiceCallActivity::class.java)
                intent.putExtra("host", isHost)
                intent.putExtra("roomID", room.vcRoomId)
                intent.putExtra("appID", Utils.APP_ID)
                intent.putExtra("appSign", Utils.APP_SIGN)
                intent.putExtra("userID", currentUserID)
                intent.putExtra("userName", user.displayName)
//        Log.d("sending","sending intent ${user.photoUrl.toString()}")
                intent.putExtra("userPhoto",user.photoUrl.toString())
                intent.putExtra("roomName",room.vcName)

                startActivity(intent)
//            } else {
//                // Handle failure to update member count
//                Log.e("JoinRoomError", "Failed to update member count: ${task.exception?.message}")
//            }
//        }
    }



    private fun showDialog(user: FirebaseUser?, roomId: String) {
        val dialogView = layoutInflater.inflate(R.layout.create_room_dialog, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val roomNameField = dialogView.findViewById<EditText>(R.id.roomNameField)
        val negativeButton = dialogView.findViewById<CardView>(R.id.cancelRoomCreationButton)
        val positiveButton = dialogView.findViewById<CardView>(R.id.DialogCreateRoomButton)
        roomPhoto = dialogView.findViewById(R.id.roomPhotoSelect)
        roomPhoto.setOnClickListener { openImagePicker() }

        val dialog = builder.create()

        positiveButton.setOnClickListener {
            val roomId = generateRoomId()
            createRoom(roomId, roomNameField.text.toString(), imageUri, user!!.uid)
            dialog.dismiss()
        }
        negativeButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun createRoom(roomId: String, roomName: String, photoUri: Uri?, hostUserId: String) {
        if (photoUri != null) {
            val storageRef = firebaseStorageReference.child("room_photos/$roomId.jpg")

            // Log the start of the image upload
            Log.d("CreateRoom", "Starting image upload for room: $roomId")

            // Upload the image to Firebase Storage
            val uploadTask = storageRef.putFile(photoUri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        Log.e("CreateRoom", "Upload failed: ${it.message}") // Log the error message
                        throw it
                    }
                }
                // Continue with the task to get the download URL
                Log.d("CreateRoom", "Image uploaded successfully. Getting download URL.")
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the download URL
                    val downloadUri = task.result

                    // Log the download URL
                    Log.d("CreateRoom", "Download URL obtained: $downloadUri")

                    // Now create the room with the photo URL
                    saveRoomToDatabase(roomId, roomName, downloadUri.toString(), hostUserId)
                } else {
                    Log.e("RoomCreation", "Failed to upload image: ${task.exception?.message}")
                }
            }
        } else {
            // If no photo was selected, create the room without a photo URL
            Log.d("CreateRoom", "No photo URI provided. Creating room without photo.")
            saveRoomToDatabase(roomId, roomName, null, hostUserId)
        }
    }
    private fun saveRoomToDatabase(roomId: String, roomName: String, photoUrl: String?, hostUserId: String) {
        // Create a new room object
        val newRoom = VCroomRvData(
            vcRoomId = roomId,
            vcName = roomName,
            vcPhotoUri = photoUrl,
            hostUserId = hostUserId,
            vcMembers = 0
        )

        // Save the new room object to Firebase
        val databaseRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId)
        databaseRef.setValue(newRoom).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("RoomCreation", "Room successfully saved to database.")
            } else {
                Log.e("RoomCreation", "Failed to save room: ${task.exception?.message}")
            }
        }
    }


    private fun generateRoomId(): String {
        val builder = StringBuilder()

        while (builder.length < 5) {
            val nextInt: Int = Random.nextInt(10)
            if (builder.length == 0 && nextInt == 0) {
                continue
            }
            builder.append(nextInt)
        }
        return builder.toString()
    }

    private fun generateUserID(): String {
        val builder = StringBuilder()

        while (builder.length < 5) {
            val nextInt: Int = Random.nextInt(10)
            if (builder.length == 0 && nextInt == 0) {
                continue
            }
            builder.append(nextInt)
        }
        return builder.toString()
    }


    private fun openImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select an image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data!!
            roomPhoto.setImageURI(imageUri)
        }
    }

    private fun listenForRoomUpdates() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("rooms")

        roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("RoomUpdates", "Data changed, updating room list.")
                VCdata.roomList.clear() // Clear existing room list

                if (!snapshot.exists()) {
                    Log.d("RoomUpdates", "No rooms found in the snapshot.")
                } else {
                    for (roomSnapshot in snapshot.children) {
                        val room = roomSnapshot.getValue(VCroomRvData::class.java)
                        if (room != null) {
                            Log.d("RoomUpdates", "Room found: ${room.vcName} with ID: ${room.vcRoomId}")
                            VCdata.roomList.add(room)
                        } else {
                            Log.d("RoomUpdates", "Room data is null for snapshot: ${roomSnapshot.key}")
                        }
                    }
                }

                Log.d("RoomUpdates", "Total rooms in list: ${VCdata.roomList.size}")
                roomAdapter.notifyDataSetChanged() // Notify the adapter about data changes
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RoomUpdates", "Failed to read rooms: ${error.message}") // Log any errors
            }
        }

        // Attach the listener to the database reference
        databaseRef.addValueEventListener(roomListener)
    }




    interface ZegoEngineInitListener {

        fun onError(error: String)
    }
}


