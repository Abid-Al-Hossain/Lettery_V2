package com.example.letterly

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Button
import android.widget.EditText
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

        val searchWordButton: Button = findViewById(R.id.searchWordButton)
        val searchWordEditText: EditText = findViewById(R.id.searchWordEditText)

        searchWordButton.setOnClickListener {
            val word = searchWordEditText.text.toString().trim()
            if (word.isNotEmpty()) {
                searchSingleWord(word)
            } else {
                Toast.makeText(this, "Please enter a word to search", Toast.LENGTH_SHORT).show()
            }
        }

        findWordsButton.setOnClickListener {
            findWordsFromApi()
        }

        setupResizableSections()
    }

    private fun setupResizableSections() {
        val dragHandle: View = findViewById(R.id.dragHandle)
        val topCard: View = findViewById(R.id.topSectionCard)
        val bottomCard: View = findViewById(R.id.bottomSectionCard)

        dragHandle.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                val parent = v.parent as View
                val totalHeight = topCard.height + bottomCard.height
                
                // Get touch position relative to the combined sections
                val rawY = event.rawY
                val location = IntArray(2)
                topCard.getLocationOnScreen(location)
                val topY = location[1]
                
                val relativeY = rawY - topY
                
                // Calculate new weights (ensure a minimum height for each)
                val minHeight = 100 // pixels
                if (relativeY > minHeight && relativeY < (totalHeight - minHeight)) {
                    val topWeight = relativeY / totalHeight.toFloat()
                    val bottomWeight = (totalHeight - relativeY) / totalHeight.toFloat()
                    
                    // Total weight should be 2.0 to maintain original layout ratio roughly
                    val totalWeight = 2.0f
                    val topParams = topCard.layoutParams as LinearLayout.LayoutParams
                    val bottomParams = bottomCard.layoutParams as LinearLayout.LayoutParams
                    
                    topParams.weight = topWeight * totalWeight
                    bottomParams.weight = bottomWeight * totalWeight
                    
                    topCard.layoutParams = topParams
                    bottomCard.layoutParams = bottomParams
                }
                true
            } else {
                v.performClick()
                true
            }
        }
    }

    private fun findWordsFromApi() {
        if (boxes.isEmpty()) {
            Toast.makeText(this, "Please add at least one box", Toast.LENGTH_SHORT).show()
            return
        }

        val wordLength = boxes.size
        val wildcardSuffix = "?".repeat(wordLength - 1)

        lifecycleScope.launch {
            try {
                wordSuggestions.clear()
                wordAdapter.notifyDataSetChanged()

                var totalFound = 0

                // Iterate through the alphabet sequentially
                for (startLetter in 'a'..'z') {
                    val pattern = "$startLetter$wildcardSuffix"
                    try {
                        val results = datamuseApi.findWords(spelledLike = pattern, max = 1000)

                        // Filter results for this letter
                        val filteredForLetter = results.filter { dw ->
                            val word = dw.word.lowercase()
                            if (word.length != wordLength) return@filter false

                            boxes.indices.all { index ->
                                val box = boxes[index]
                                val letterAtPos = word[index].toString()

                                if (box.includeLetter.isNotEmpty()) {
                                    val includeLetters = box.includeLetter.split(",").map { it.trim().lowercase() }
                                    if (!includeLetters.contains(letterAtPos)) return@all false
                                }

                                if (box.excludeLetters.isNotEmpty()) {
                                    val excludeLetters = box.excludeLetters.split(",").map { it.trim().lowercase() }
                                    if (excludeLetters.contains(letterAtPos)) return@all false
                                }

                                true
                            }
                        }

                        if (filteredForLetter.isNotEmpty()) {
                            // Format and append the new matches
                            val formattedMatches = filteredForLetter
                                .distinctBy { it.word }
                                .sortedBy { it.word }
                                .map {
                                    val rawDef = it.definitions?.firstOrNull() ?: "No definition found"
                                    val definition = rawDef.replaceFirst(Regex("^[a-z]+\\t"), "")
                                    it.word.uppercase() to definition
                                }

                            val startIdx = wordSuggestions.size
                            wordSuggestions.addAll(formattedMatches)
                            totalFound += formattedMatches.size
                            wordAdapter.notifyItemRangeInserted(startIdx, formattedMatches.size)
                        }
                    } catch (e: Exception) {
                    }
                }

                if (totalFound == 0) {
                    Toast.makeText(this@Letterly, "No matching words found", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@Letterly, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun searchSingleWord(word: String) {
        lifecycleScope.launch {
            try {
                // Search for the exact word
                val results = datamuseApi.findWords(spelledLike = word.lowercase(), max = 1)

                wordSuggestions.clear()
                if (results.isNotEmpty()) {
                    val it = results[0]
                    val rawDef = it.definitions?.firstOrNull() ?: "No definition found"
                    val definition = rawDef.replaceFirst(Regex("^[a-z]+\\t"), "")
                    wordSuggestions.add(it.word.uppercase() to definition)
                } else {
                    Toast.makeText(this@Letterly, "Word not found", Toast.LENGTH_SHORT).show()
                }
                wordAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Toast.makeText(this@Letterly, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


}

data class Box(var includeLetter: String = "", var excludeLetters: String = "")
