package com.example.letterly

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Letterly : AppCompatActivity() {

    // --- Pattern Mode ---
    private lateinit var boxRecyclerView: RecyclerView
    private lateinit var boxAdapter: BoxAdapter
    private val boxes = mutableListOf<Box>()
    private lateinit var patternWordsRecyclerView: RecyclerView
    private lateinit var patternWordAdapter: WordAdapter
    private val patternSuggestions = mutableListOf<Pair<String, String>>()

    // --- Dictionary Mode ---
    private lateinit var dictResultsRecyclerView: RecyclerView
    private lateinit var dictWordAdapter: WordAdapter
    private val dictResults = mutableListOf<Pair<String, String>>()

    // --- Rhyme Mode ---
    private lateinit var rhymeResultsRecyclerView: RecyclerView
    private lateinit var rhymeWordAdapter: WordAdapter
    private val rhymeResults = mutableListOf<Pair<String, String>>()

    // --- Concept Mode ---
    private lateinit var conceptResultsRecyclerView: RecyclerView
    private lateinit var conceptWordAdapter: WordAdapter
    private val conceptResults = mutableListOf<Pair<String, String>>()

    // --- Shared ---
    private lateinit var datamuseApi: DatamuseApi

    // Panels
    private lateinit var panelPattern: View
    private lateinit var panelDictionary: View
    private lateinit var panelRhyme: View
    private lateinit var panelConcept: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_letterly)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.datamuse.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        datamuseApi = retrofit.create(DatamuseApi::class.java)

        // Mode panels
        panelPattern = findViewById(R.id.panelPattern)
        panelDictionary = findViewById(R.id.panelDictionary)
        panelRhyme = findViewById(R.id.panelRhyme)
        panelConcept = findViewById(R.id.panelConcept)

        setupTabs()
        setupPatternMode()
        setupDictionaryMode()
        setupRhymeMode()
        setupConceptMode()
        setupResizableSections()
    }

    // ─────────────────────────────────────────────────────────────
    // TABS (Mode Selection)
    // ─────────────────────────────────────────────────────────────
    private fun setupTabs() {
        val tabLayout: TabLayout = findViewById(R.id.modeTabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("🔠 Pattern"))
        tabLayout.addTab(tabLayout.newTab().setText("📖 Dictionary"))
        tabLayout.addTab(tabLayout.newTab().setText("🎵 Rhyme"))
        tabLayout.addTab(tabLayout.newTab().setText("💡 Concept"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showPanel(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Show Pattern Mode by default
        showPanel(0)
    }

    private fun showPanel(index: Int) {
        panelPattern.visibility = if (index == 0) View.VISIBLE else View.GONE
        panelDictionary.visibility = if (index == 1) View.VISIBLE else View.GONE
        panelRhyme.visibility = if (index == 2) View.VISIBLE else View.GONE
        panelConcept.visibility = if (index == 3) View.VISIBLE else View.GONE
    }

    // ─────────────────────────────────────────────────────────────
    // PATTERN MODE
    // ─────────────────────────────────────────────────────────────
    private fun setupPatternMode() {
        val addBoxButton: View = findViewById(R.id.addBoxButton)
        val removeBoxButton: View = findViewById(R.id.removeBoxButton)
        val findWordsButton: View = findViewById(R.id.findWordsButton)
        boxRecyclerView = findViewById(R.id.recyclerView)
        patternWordsRecyclerView = findViewById(R.id.wordsRecyclerView)

        boxAdapter = BoxAdapter(boxes)
        boxRecyclerView.layoutManager = GridLayoutManager(this, 2)
        boxRecyclerView.adapter = boxAdapter

        patternWordAdapter = WordAdapter(patternSuggestions)
        patternWordsRecyclerView.layoutManager = LinearLayoutManager(this)
        patternWordsRecyclerView.adapter = patternWordAdapter

        addBoxButton.setOnClickListener {
            boxes.add(Box())
            boxAdapter.notifyItemInserted(boxes.size - 1)
        }

        removeBoxButton.setOnClickListener {
            if (boxes.isNotEmpty()) {
                val idx = boxes.size - 1
                boxes.removeAt(idx)
                boxAdapter.notifyItemRemoved(idx)
            }
        }

        findWordsButton.setOnClickListener { findWordsFromApi() }
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
                patternSuggestions.clear()
                patternWordAdapter.notifyDataSetChanged()
                var totalFound = 0

                for (startLetter in 'a'..'z') {
                    val pattern = "$startLetter$wildcardSuffix"
                    try {
                        val results = datamuseApi.findWords(spelledLike = pattern, max = 1000)
                        val filtered = results.filter { dw ->
                            val word = dw.word.lowercase()
                            if (word.length != wordLength) return@filter false
                            boxes.indices.all { index ->
                                val box = boxes[index]
                                val letterAtPos = word[index].toString()
                                if (box.includeLetter.isNotEmpty()) {
                                    val includes = box.includeLetter.split(",").map { it.trim().lowercase() }
                                    if (!includes.contains(letterAtPos)) return@all false
                                }
                                if (box.excludeLetters.isNotEmpty()) {
                                    val excludes = box.excludeLetters.split(",").map { it.trim().lowercase() }
                                    if (excludes.contains(letterAtPos)) return@all false
                                }
                                true
                            }
                        }
                        if (filtered.isNotEmpty()) {
                            val formatted = filtered.distinctBy { it.word }.sortedBy { it.word }
                                .map { formatResult(it) }
                            val startIdx = patternSuggestions.size
                            patternSuggestions.addAll(formatted)
                            totalFound += formatted.size
                            patternWordAdapter.notifyItemRangeInserted(startIdx, formatted.size)
                        }
                    } catch (_: Exception) {}
                }
                if (totalFound == 0) Toast.makeText(this@Letterly, "No matching words found", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@Letterly, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DICTIONARY MODE
    // ─────────────────────────────────────────────────────────────
    private fun setupDictionaryMode() {
        dictResultsRecyclerView = findViewById(R.id.dictResultsRecyclerView)
        dictWordAdapter = WordAdapter(dictResults)
        dictResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        dictResultsRecyclerView.adapter = dictWordAdapter

        val searchEditText: TextInputEditText = findViewById(R.id.dictSearchEditText)
        val searchButton: View = findViewById(R.id.dictSearchButton)

        searchButton.setOnClickListener {
            val word = searchEditText.text.toString().trim()
            if (word.isNotEmpty()) lookupWord(word)
            else Toast.makeText(this, "Please enter a word", Toast.LENGTH_SHORT).show()
        }
    }

    private fun lookupWord(word: String) {
        lifecycleScope.launch {
            try {
                val results = datamuseApi.lookupWord(word = word.lowercase(), max = 1)
                dictResults.clear()
                if (results.isNotEmpty()) {
                    dictResults.add(formatResult(results[0]))
                } else {
                    Toast.makeText(this@Letterly, "Word not found", Toast.LENGTH_SHORT).show()
                }
                dictWordAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(this@Letterly, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // RHYME MODE
    // ─────────────────────────────────────────────────────────────
    private fun setupRhymeMode() {
        rhymeResultsRecyclerView = findViewById(R.id.rhymeResultsRecyclerView)
        rhymeWordAdapter = WordAdapter(rhymeResults)
        rhymeResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        rhymeResultsRecyclerView.adapter = rhymeWordAdapter

        val searchEditText: TextInputEditText = findViewById(R.id.rhymeSearchEditText)
        val searchButton: View = findViewById(R.id.rhymeSearchButton)

        searchButton.setOnClickListener {
            val word = searchEditText.text.toString().trim()
            if (word.isNotEmpty()) findRhymes(word)
            else Toast.makeText(this, "Please enter a word", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findRhymes(word: String) {
        lifecycleScope.launch {
            try {
                val results = datamuseApi.findRhymes(word = word.lowercase(), max = 100)
                rhymeResults.clear()
                if (results.isNotEmpty()) {
                    rhymeResults.addAll(results.map { formatResult(it) })
                } else {
                    Toast.makeText(this@Letterly, "No rhymes found", Toast.LENGTH_SHORT).show()
                }
                rhymeWordAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(this@Letterly, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CONCEPT MODE
    // ─────────────────────────────────────────────────────────────
    private fun setupConceptMode() {
        conceptResultsRecyclerView = findViewById(R.id.conceptResultsRecyclerView)
        conceptWordAdapter = WordAdapter(conceptResults)
        conceptResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        conceptResultsRecyclerView.adapter = conceptWordAdapter

        val searchEditText: TextInputEditText = findViewById(R.id.conceptSearchEditText)
        val searchButton: View = findViewById(R.id.conceptSearchButton)

        searchButton.setOnClickListener {
            val concept = searchEditText.text.toString().trim()
            if (concept.isNotEmpty()) findByConcept(concept)
            else Toast.makeText(this, "Please describe the concept", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findByConcept(concept: String) {
        lifecycleScope.launch {
            try {
                val results = datamuseApi.findByMeaning(concept = concept.lowercase(), max = 100)
                conceptResults.clear()
                if (results.isNotEmpty()) {
                    conceptResults.addAll(results.map { formatResult(it) })
                } else {
                    Toast.makeText(this@Letterly, "No matching words found", Toast.LENGTH_SHORT).show()
                }
                conceptWordAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(this@Letterly, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SHARED HELPERS
    // ─────────────────────────────────────────────────────────────
    private fun formatResult(dw: DatamuseWord): Pair<String, String> {
        val rawDef = dw.definitions?.firstOrNull() ?: "No definition found"
        val definition = rawDef.replaceFirst(Regex("^[a-z]+\\t"), "")
        return dw.word.uppercase() to definition
    }

    private fun setupResizableSections() {
        val dragHandle: View = findViewById(R.id.dragHandle)
        val topCard: View = findViewById(R.id.topSectionCard)
        val bottomCard: View = findViewById(R.id.bottomSectionCard)

        dragHandle.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                val totalHeight = topCard.height + bottomCard.height
                val rawY = event.rawY
                val location = IntArray(2)
                topCard.getLocationOnScreen(location)
                val relativeY = rawY - location[1]
                val minHeight = 100
                if (relativeY > minHeight && relativeY < (totalHeight - minHeight)) {
                    val topWeight = relativeY / totalHeight.toFloat()
                    val bottomWeight = (totalHeight - relativeY) / totalHeight.toFloat()
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
}

data class Box(var includeLetter: String = "", var excludeLetters: String = "")
