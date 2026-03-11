package com.example.letterly

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Letterly : AppCompatActivity() {

    // --- Mode Configurations ---
    data class ModeData(
        val title: String,
        val icon: String,
        val apiQueryKey: String?, // null for Pattern mode
        val descriptionTitle: String,
        val descriptionContent: String,
        val inputHint: String,
        val buttonText: String
    )

    private val allModes = listOf(
        ModeData("Pattern", "🔠", null, "About Pattern Mode", 
            "What it does: Finds words matching a specific letter pattern.\nHow to use: Add boxes for each letter, use include/exclude filters.\nOutput: Words of exact length matching your criteria.", 
            "", ""),
        ModeData("Dictionary", "📖", "sp", "About Dictionary Mode", 
            "What it does: Standard dictionary lookup for definitions.\nHow to use: Type an exact word.\nOutput: The word and its dictionary definition.", 
            "Enter a word", "Look Up Definition"),
        ModeData("Rhyme", "🎵", "rel_rhy", "About Rhyme Mode", 
            "What it does: Finds perfect phonetic rhymes.\nHow to use: Type the word you want to rhyme with.\nOutput: Words that sound identical in their ending.", 
            "Enter word to rhyme with", "Find Rhymes"),
        ModeData("Concept", "💡", "ml", "About Concept Mode", 
            "What it does: Finds words by their description (Reverse Dictionary).\nHow to use: Type a phrase like 'fear of heights' or 'yellow fruit'.\nOutput: Words conceptually related to your description.", 
            "Describe the concept...", "Find the Word"),
        ModeData("Synonym", "👯", "rel_syn", "About Synonym Mode", 
            "What it does: Finds words with similar meanings.\nHow to use: Type a word search for its synonyms.\nOutput: A list of words that mean roughly the same thing.", 
            "Enter word for synonyms", "Find Synonyms"),
        ModeData("Antonym", "🆚", "rel_ant", "About Antonym Mode", 
            "What it does: Finds words with opposite meanings.\nHow to use: Type a word to see its antonyms.\nOutput: Words that represent the opposite concept.", 
            "Enter word for antonyms", "Find Antonyms"),
        ModeData("Sound-Alike", "👂", "sl", "About Sound-Alike Mode", 
            "What it does: Finds words that sound similar, even if spelled differently.\nHow to use: Type words phonetically (e.g. 'jirraf').\nOutput: Words that are pronounced similarly.", 
            "Enter sounds-like word", "Search Phonetically"),
        ModeData("Homophone", "👯", "rel_hom", "About Homophone Mode", 
            "What it does: Finds words that sound exactly identical.\nHow to use: Type a word like 'flower'.\nOutput: Words like 'flour' that share the same sound.", 
            "Enter word for homophones", "Find Homophones"),
        ModeData("Trigger", "🔫", "rel_trg", "About Trigger Mode", 
            "What it does: Finds words that are conceptually associated.\nHow to use: Type a core word (e.g. 'cow').\nOutput: Words often linked to it (e.g. 'milk', 'farm').", 
            "Enter a trigger word", "Find Associations"),
        ModeData("Adjectives", "🎨", "rel_jjb", "About Adjectives Mode", 
            "What it does: Finds descriptive adjectives for a specific noun.\nHow to use: Type a noun like 'ocean'.\nOutput: Adjectives like 'deep', 'blue', or 'vast'.", 
            "Enter a noun (e.g. ocean)", "Find Adjectives"),
        ModeData("Nouns", "🏷️", "rel_jja", "About Nouns Mode", 
            "What it does: Finds nouns often described by a specific adjective.\nHow to use: Type an adjective like 'yellow'.\nOutput: Nouns like 'sun', 'taxi', or 'banana'.", 
            "Enter an adjective (e.g. yellow)", "Find Nouns"),
        ModeData("Consonant", "🎶", "rel_cns", "About Consonant Mode", 
            "What it does: Finds words with matching consonant sounds (Alliteration).\nHow to use: Type a word to match its consonant structure.\nOutput: Words that share the same 'skeleton' of consonants.", 
            "Enter word to match", "Find Consonants"),
        ModeData("Followers", "➡️", "rel_bga", "About Followers Mode", 
            "What it does: Finds words that frequently follow a specific word.\nHow to use: Type a word like 'social'.\nOutput: Common successors like 'media', 'network', 'justice'.", 
            "Enter a starting word", "Find Followers"),
        ModeData("Predecessors", "⬅️", "rel_bgb", "About Predecessors Mode", 
            "What it does: Finds words that frequently precede a specific word.\nHow to use: Type a word like 'media'.\nOutput: Common predecessors like 'social', 'multimedia', 'digital'.", 
            "Enter an ending word", "Find Predecessors")
    )

    // --- State & UI ---
    private lateinit var datamuseApi: DatamuseApi
    private var currentModeIndex = 0

    // Pattern Mode Components
    private lateinit var panelPattern: View
    private lateinit var boxRecyclerView: RecyclerView
    private lateinit var boxAdapter: BoxAdapter
    private val boxes = mutableListOf<Box>()
    private lateinit var patternWordsRecyclerView: RecyclerView
    private lateinit var patternWordAdapter: WordAdapter
    private val patternSuggestions = mutableListOf<Pair<String, String>>()

    // Generic Mode Components
    private lateinit var panelGeneric: View
    private lateinit var genericDescTitle: TextView
    private lateinit var genericDescContent: TextView
    private lateinit var genericInputLayout: TextInputLayout
    private lateinit var genericEditText: TextInputEditText
    private lateinit var genericButton: View
    private lateinit var genericRecyclerView: RecyclerView
    private lateinit var genericAdapter: WordAdapter
    private val genericResults = mutableListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_letterly)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.datamuse.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        datamuseApi = retrofit.create(DatamuseApi::class.java)

        // Initialize UI
        panelPattern = findViewById(R.id.panelPattern)
        panelGeneric = findViewById(R.id.panelGeneric)

        setupPatternModeUI()
        setupGenericModeUI()
        setupTabs()
        setupResizableSections()
    }

    private fun setupTabs() {
        val tabLayout: TabLayout = findViewById(R.id.modeTabLayout)
        allModes.forEach { mode ->
            tabLayout.addTab(tabLayout.newTab().setText("${mode.icon} ${mode.title}"))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                switchMode(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        switchMode(0) // Default to Pattern
    }

    private fun switchMode(index: Int) {
        currentModeIndex = index
        val mode = allModes[index]

        if (index == 0) {
            panelPattern.visibility = View.VISIBLE
            panelGeneric.visibility = View.GONE
        } else {
            panelPattern.visibility = View.GONE
            panelGeneric.visibility = View.VISIBLE

            // Update Generic UI
            genericDescTitle.text = mode.descriptionTitle
            genericDescContent.text = mode.descriptionContent
            genericInputLayout.hint = mode.inputHint
            (genericButton as TextView).text = mode.buttonText
            
            // Clear current list when switching
            genericResults.clear()
            genericAdapter.notifyDataSetChanged()
        }
    }

    private fun setupPatternModeUI() {
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

        findWordsButton.setOnClickListener { performPatternSearch() }
    }

    private fun setupGenericModeUI() {
        genericDescTitle = findViewById(R.id.modeDescriptionTitle)
        genericDescContent = findViewById(R.id.modeDescriptionContent)
        genericInputLayout = findViewById(R.id.genericInputLayout)
        genericEditText = findViewById(R.id.genericSearchEditText)
        genericButton = findViewById(R.id.genericSearchButton)
        genericRecyclerView = findViewById(R.id.genericResultsRecyclerView)

        genericAdapter = WordAdapter(genericResults)
        genericRecyclerView.layoutManager = LinearLayoutManager(this)
        genericRecyclerView.adapter = genericAdapter

        genericButton.setOnClickListener { performGenericSearch() }
    }

    private fun performGenericSearch() {
        val input = genericEditText.text.toString().trim()
        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter a word or phrase", Toast.LENGTH_SHORT).show()
            return
        }

        val mode = allModes[currentModeIndex]
        val queryKey = mode.apiQueryKey ?: return

        lifecycleScope.launch {
            try {
                val options = mapOf(queryKey to input.lowercase())
                val results = datamuseApi.searchWords(options = options)
                
                genericResults.clear()
                if (results.isNotEmpty()) {
                    genericResults.addAll(results.map { formatResult(it) })
                } else {
                    Toast.makeText(this@Letterly, "No results found", Toast.LENGTH_SHORT).show()
                }
                genericAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(this@Letterly, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun performPatternSearch() {
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
                            val formatted = filtered.distinctBy { it.word }.sortedBy { it.word }.map { formatResult(it) }
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
