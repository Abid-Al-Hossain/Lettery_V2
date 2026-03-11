package com.example.letterly

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Letterly : AppCompatActivity() {

    private lateinit var boxRecyclerView: RecyclerView
    private lateinit var wordsRecyclerView: RecyclerView
    private lateinit var boxAdapter: BoxAdapter
    private lateinit var wordAdapter: WordAdapter
    private val boxes = mutableListOf<Box>()
    private val wordSuggestions = mutableListOf<Pair<String, String>>() // Pair of Word and Meaning

    private lateinit var datamuseApi: DatamuseApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_letterly)

        val addBoxButton: Button = findViewById(R.id.addBoxButton)
        val removeBoxButton: Button = findViewById(R.id.removeBoxButton)
        val findWordsButton: Button = findViewById(R.id.findWordsButton)
        boxRecyclerView = findViewById(R.id.recyclerView)
        wordsRecyclerView = findViewById(R.id.wordsRecyclerView)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.datamuse.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        datamuseApi = retrofit.create(DatamuseApi::class.java)

        boxAdapter = BoxAdapter(boxes)
        boxRecyclerView.layoutManager = GridLayoutManager(this,2)
        boxRecyclerView.adapter = boxAdapter

        wordAdapter = WordAdapter(wordSuggestions)
        wordsRecyclerView.layoutManager = LinearLayoutManager(this)
        wordsRecyclerView.adapter = wordAdapter

        addBoxButton.setOnClickListener {
            boxes.add(Box())
            boxAdapter.notifyItemInserted(boxes.size - 1)
        }

        removeBoxButton.setOnClickListener {
            if (boxes.isNotEmpty()) {
                val removedIndex = boxes.size - 1
                boxes.removeAt(removedIndex)
                boxAdapter.notifyItemRemoved(removedIndex)
            }
        }

        findWordsButton.setOnClickListener {
            findWordsFromApi()
        }
    }

    private fun findWordsFromApi() {
        if (boxes.isEmpty()) {
            Toast.makeText(this, "Please add at least one box", Toast.LENGTH_SHORT).show()
            return
        }

        // Build the wildcard suffix: the "?" placeholder for each box after the first
        // We'll make 26 calls (a??, b??, c?? etc.) so we get full alphabet coverage
        val wordLength = boxes.size
        val wildcardSuffix = "?".repeat(wordLength - 1) // e.g., for 3 boxes → "??"

        lifecycleScope.launch {
            try {
                // Fire 26 parallel calls, one per starting letter, and merge results
                val allResults = ('a'..'z').flatMap { startLetter ->
                    val pattern = "$startLetter$wildcardSuffix" // e.g. "a??" for 3-letter words
                    try {
                        datamuseApi.findWords(spelledLike = pattern, max = 1000)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                // Client-side filtering — identical to original local dictionary logic
                val filteredResults = allResults.filter { dw ->
                    val word = dw.word.lowercase()
                    if (word.length != wordLength) return@filter false

                    boxes.indices.all { index ->
                        val box = boxes[index]
                        val letterAtPos = word[index].toString()

                        // Mirror original: includeLetter — position must match one of the listed letters
                        if (box.includeLetter.isNotEmpty()) {
                            val includeLetters = box.includeLetter.split(",").map { it.trim().lowercase() }
                            if (!includeLetters.contains(letterAtPos)) return@all false
                        }

                        // Mirror original: excludeLetters — position must NOT match any listed letter
                        if (box.excludeLetters.isNotEmpty()) {
                            val excludeLetters = box.excludeLetters.split(",").map { it.trim().lowercase() }
                            if (excludeLetters.contains(letterAtPos)) return@all false
                        }

                        true
                    }
                }

                wordSuggestions.clear()
                wordSuggestions.addAll(
                    filteredResults
                        .distinctBy { it.word } // remove duplicates from overlapping API results
                        .sortedBy { it.word }    // alphabetical order, matching original JSON structure
                        .map {
                            val rawDef = it.definitions?.firstOrNull() ?: "No definition found"
                            val definition = rawDef.replaceFirst(Regex("^[a-z]+\\t"), "")
                            it.word.uppercase() to definition // uppercase to match original JSON keys
                        }
                )
                wordAdapter.notifyDataSetChanged()

                if (wordSuggestions.isEmpty()) {
                    Toast.makeText(this@Letterly, "No matching words found", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@Letterly, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


}

data class Box(var includeLetter: String = "", var excludeLetters: String = "")
