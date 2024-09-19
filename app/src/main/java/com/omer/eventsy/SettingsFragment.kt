package com.omer.eventsy

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.omer.eventsy.databinding.FragmentSettingsBinding
import com.squareup.picasso.Picasso
import java.lang.Exception

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var storage : FirebaseStorage
    private lateinit var reference : StorageReference
    var selectedImage : Uri? = null
    var selectedBitmap : Bitmap? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage
        reference = storage.reference

        registerLaunchers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()

        binding.logout.setOnClickListener {
            auth.signOut()
            val action = SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }

        binding.profilePicture.setOnClickListener {
            selectPhotos(it)
        }

        binding.saveButton.setOnClickListener {
            uploadProfilePicture(it)
        }
    }

    fun selectPhotos(view: View){
        //PERMISSION CONTROL FOR ANDROID 13 (TIRAMISU) AND HIGHER
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //PERMISSION CHECK
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                //If permission is not granted and the user has previously denied permission, an explanation is given
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                        View.OnClickListener {
                            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                } else {
                    //PERMISSION IS REQUESTED
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                //IF PERMISSION ALLOWED, OPENS GALLERY
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }
        } else {
            //PERMISSION CONTROL FOR ANDROID 13 AND LOWER
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                        View.OnClickListener {
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else {
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    fun uploadProfilePicture(view: View) {

        val username = binding.usernameTxt.text.toString()
        val userId = auth.currentUser?.uid ?: return

        // Varsayılan olarak güncellenmiş bilgiler
        val updatedData = hashMapOf<String, Any>()

        // Profil resmi varsa güncelle
        if (selectedImage != null) {
            val imageReference = reference.child("profile_images").child("$userId.jpg")

            // Eski profil resmini silmeye gerek yok, direkt yeni resmi aynı isimle yükle
            imageReference.putFile(selectedImage!!).addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    updatedData["downloadUrl"] = downloadUrl

                    // Kullanıcı adını güncelle
                    if (username.isNotEmpty()) {
                        updatedData["username"] = username
                    }

                    // Güncellenmiş verileri Firestore'a kaydet
                    db.collection("Users").document(userId).update(updatedData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), "Failed to update profile: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to upload image: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to upload new image: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }

        } else {
            // Profil resmi değişmediyse, sadece kullanıcı adını güncelle
            if (username.isNotEmpty()) {
                updatedData["username"] = username
            }

            // Güncellenmiş verileri Firestore'a kaydet
            if (updatedData.isNotEmpty()) {
                db.collection("Users").document(userId).update(updatedData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Failed to update profile: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(requireContext(), "No changes to save", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                val profileImageUrl = document.getString("downloadUrl")
                val username = document.getString("username")

                // Profil resmi güncelle
                if (profileImageUrl != null) {
                    Picasso.get()
                        .load(profileImageUrl)
                        .placeholder(R.drawable.icons8_user_48) // Varsayılan resim
                        .error(R.drawable.baseline_error_outline_24) // Hata durumunda varsayılan resim
                        .into(binding.profilePicture)
                } else {
                    binding.profilePicture.setImageResource(R.drawable.icons8_user_48)
                }

                // Kullanıcı adı güncelle
                if (username != null) {
                    binding.usernameTxt.setText(username)
                }
            } else {
                Toast.makeText(requireContext(), "User document not found", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun registerLaunchers(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val intentFromResult = result.data
                if(intentFromResult != null) {
                    selectedImage = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28 ) {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedImage!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.profilePicture.setImageBitmap(selectedBitmap)
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImage)
                            binding.profilePicture.setImageBitmap(selectedBitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                //permission allowed
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                Toast.makeText(requireContext(), "Permisson needed!", Toast.LENGTH_LONG).show()
            }
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}