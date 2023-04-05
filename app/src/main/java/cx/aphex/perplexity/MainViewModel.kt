package cx.aphex.perplexity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.BetaOpenAI
import cx.aphex.perplexity.api.OpenAIClient
import de.l3s.boilerpipe.extractors.ArticleExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URL

@BetaOpenAI
class MainViewModel : ViewModel() {

    private val _answerChunks = MutableSharedFlow<List<String>>(replay = 0)
    val answerChunks: SharedFlow<List<String>> = _answerChunks

    private val _isFetchingAnswer = MutableStateFlow(false)
    val isFetchingAnswer: StateFlow<Boolean> = _isFetchingAnswer

    fun search(query: String) {
        viewModelScope.launch {
            _isFetchingAnswer.emit(true)
            withContext(Dispatchers.IO) {
                val urls = fetchWebPages(query)
                val sources = parseWebPages(urls)
                val prompt = buildPrompt(query, sources)
                val answer = OpenAIClient.generateAnswer(prompt)

                answer.collect { chunk ->
                    chunk.choices.firstOrNull()?.delta?.content?.let { content ->
                        _answerChunks.emit(listOf(content))
                    }
                }

                val sourcesListBuilder = StringBuilder()
                sourcesListBuilder.append("\n\nSources\n")
                sources.forEachIndexed { index, (url, text) ->
                    sourcesListBuilder.append("[${index + 1}] [${URL(url).host}]($url)\n")
                }
                _answerChunks.emit(listOf(sourcesListBuilder.toString()))
            }

            _isFetchingAnswer.emit(false)
        }
    }

    private suspend fun fetchWebPages(query: String): List<String> {
        val sourceCount = 4
        val excludeList = listOf(
            "google", "facebook", "twitter", "instagram", "youtube", "tiktok"
        )
        val httpClient = OkHttpClient()

        // GET LINKS
        val request = Request.Builder()
            .url("https://www.google.com/search?q=$query")
            .build()
        val response = httpClient.newCall(request).execute()
        val html = response.body?.string()
        val document = Jsoup.parse(html)

        val linkTags = document.select("a")
        val links = mutableSetOf<String>()

        linkTags.forEach { link ->
            val href = link.attr("href")
            if (href.startsWith("/url?q=")) {
                val cleanedHref = href.replace("/url?q=", "").split("&")[0]
                links.add(cleanedHref)
            }
        }

        val filteredLinks = links.filter { link ->
            val url = URL(link)
            val domain = url.host

            if (excludeList.any { site -> domain.contains(site) }) return@filter false
            if (!url.protocol.equals("https", true)) return@filter false

            links.indexOfFirst { otherLink -> URL(otherLink).host == domain } == links.indexOf(link)
        }

        return filteredLinks.take(sourceCount)
    }

    private suspend fun parseWebPages(urls: List<String>): List<Pair<String, String>> {
        val httpClient = OkHttpClient()
        val sources = mutableListOf<Pair<String, String>>()

        urls.forEach { url ->
            val request = Request.Builder()
                .url(url)
                .build()
            val response = httpClient.newCall(request).execute()
            val html = response.body?.string()

            html?.let {
                val sourceText = ArticleExtractor.INSTANCE.getText(it)
                val cleanedText = cleanSourceText(sourceText)
                sources.add(Pair(url, cleanedText.take(1500)))
            }
        }

        return sources
    }

    private fun cleanSourceText(text: String): String {
        // This function cleans the text by removing unnecessary spaces and newlines, and normalizing the formatting.
        return text.trim()
            .replace(Regex("(\\n){4,}"), "\n\n\n")
            .replace("\n\n", " ")
            .replace(Regex(" {3,}"), "  ")
            .replace("\t", "")
            .replace(Regex("\\n+(\\s*\\n)*"), "\n")
    }

    private fun buildPrompt(query: String, sources: List<Pair<String, String>>): String {
        val promptBuilder = StringBuilder()
        promptBuilder.append("User query: $query\n")

        sources.forEachIndexed { index, (_, text) ->
            promptBuilder.append("A${index + 1}: $text [${index + 1}]\n")
        }

        promptBuilder.append("\n\nAI:")

        return promptBuilder.toString()
    }
}