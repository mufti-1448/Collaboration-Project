package com.colab.myfriend

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colab.myfriend.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // Variabel untuk mengakses elemen UI dari layout menggunakan View Binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menginisialisasi View Binding untuk layout activity_main
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengaktifkan fitur Edge-to-Edge untuk tampilan yang lebih modern
        enableEdgeToEdge()

        // Mengatur listener untuk mengaplikasikan insets pada tampilan utama
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            // Mendapatkan inset dari sistem (misalnya, status bar, navigasi bar)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Menetapkan padding pada tampilan utama untuk mengakomodasi sistem bar
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // Mengembalikan insets untuk diterapkan pada tampilan lainnya jika perlu
            insets
        }

        // Menangani klik pada tombol "Start" untuk berpindah ke MenuHomeActivity
        binding.btnStart.setOnClickListener {
            // Membuat Intent untuk berpindah ke MenuHomeActivity
            val destination = Intent(this, MenuHomeActivity::class.java)
            // Memulai activity baru
            startActivity(destination)
        }
    }
}
