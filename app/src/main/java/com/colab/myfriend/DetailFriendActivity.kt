package com.colab.myfriend

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colab.myfriend.databinding.ActivityDetailFriendBinding

class DetailFriendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailFriendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ambil data dari Intent
        val name = intent.getStringExtra("EXTRA_NAME")
        val school = intent.getStringExtra("EXTRA_SCHOOL")
        val bio = intent.getStringExtra("EXTRA_BIO")

        // Tampilkan data di TextView
        binding.tvName.text = name
        binding.tvSchool.text = school
        binding.tvBio.text = bio

        // Beralih ke edit friend
        binding.editButton.setOnClickListener {
            val destination = Intent(this, EditFriendActivity::class.java)
            startActivity(destination)
        }

        // Kembali ke home
        binding.backButton.setOnClickListener {
            val destination = Intent(this, MenuHomeActivity::class.java)
            startActivity(destination)
        }

        // Menangani deleteButton
        binding.deleteButton.setOnClickListener {
            setDeleteButton()
        }

        // Memanggil fungsi untuk mengatur ukuran icon
        setDrawable()
    }

    // Fungsi untuk mengatur ukuran icon
    private fun setDrawable() {
        val personDrawable = ContextCompat.getDrawable(this, R.drawable.ic_person)
        val schoolDrawable = ContextCompat.getDrawable(this, R.drawable.ic_school)
        val infoDrawable = ContextCompat.getDrawable(this, R.drawable.ic_info)

        // Set ukuran drawable baru dengan sdp
        val scaledSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._23sdp)
        personDrawable?.setBounds(0, 0, scaledSize, scaledSize)
        schoolDrawable?.setBounds(0, 0, scaledSize, scaledSize)
        infoDrawable?.setBounds(0, 0, scaledSize, scaledSize)

        // Terapkan drawable dengan ukuran baru pada TextView
        binding.tvName.setCompoundDrawablesRelative(personDrawable, null, null, null)
        binding.tvSchool.setCompoundDrawablesRelative(schoolDrawable, null, null, null)
        binding.tvBio.setCompoundDrawablesRelative(infoDrawable, null, null, null)
    }

    // Fungsi untuk deleteButton
    private fun setDeleteButton() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remove Friend")
        builder.setMessage("Are you sure you want to remove this friend?")
        builder.setPositiveButton("Remove") { dialog, which ->
            Toast.makeText(this, "Friend details removed", Toast.LENGTH_SHORT).show()
            val destination = Intent(this, MenuHomeActivity::class.java)
            startActivity(destination)
            finish() // Menutup dialog dan kembali ke home
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss() // Menutup dialog jika dibatalkan
        }
        builder.create().show()
    }
}
