package cx.aphex.perplexity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cx.aphex.perplexity.api.ApiService
import cx.aphex.perplexity.api.OpenAIResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainViewModel : ViewModel() {

    private val _answer = MutableSharedFlow<String>()
    val answer: SharedFlow<String> = _answer

    fun search(query: String) {
        viewModelScope.launch {
            val urls = fetchWebPages(query)
            val text = parseWebPages(urls)
            val prompt = buildPrompt(query, text)
            val answer = generateAnswer(prompt)
            _answer.emit(answer)
        }
    }

    private suspend fun fetchWebPages(query: String): List<String> {
        return suspendCancellableCoroutine { cont ->
            val url = "https://www.google.com/search".toHttpUrl()
                .newBuilder()
                .addQueryParameter("q", query)
                .build()

            val request = Request.Builder()
                .url(url)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
                )
                .build()

            OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
                override fun onResponse(call: okhttp3.Call, response: Response) {
                    val document = response.body?.string()?.let { Jsoup.parse(it) }
                    val elements = document?.select("div.g > div > div.rc > div.r > a[href]")

                    val urls = elements?.mapNotNull { element ->
                        element.attr("href")
                    }

                    urls?.let { cont.resume(it) }
                }

                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    cont.resumeWithException(e)
                }
            })
        }
    }

    private suspend fun parseWebPages(urls: List<String>): String {
        val stringBuilder = StringBuilder()
        for (url in urls) {
            try {
                val document = Jsoup.connect(url).get()
                val text = document.body().text()
                stringBuilder.append(text).append("\n\n")
            } catch (e: IOException) {
                // Handle exception
            }
        }
        return stringBuilder.toString()
    }

    private fun buildPrompt(query: String, text: String): String {
        return "User query: $query\n\n$text\n\nAI:"
    }

    private suspend fun generateAnswer(prompt: String): String {
        return suspendCancellableCoroutine { cont ->
            ApiService.openAIApi.generateAnswer(prompt).enqueue(object : Callback<OpenAIResponse> {
                override fun onResponse(
                    call: Call<OpenAIResponse>,
                    response: retrofit2.Response<OpenAIResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.choices?.firstOrNull()?.text?.let { answer ->
                            cont.resume(answer)
                        }
                            ?: cont.resumeWithException(RuntimeException("No answer generated. API response: ${response.body()}"))
                    } else {
                        cont.resumeWithException(RuntimeException("API call failed with status code ${response.code()} and message: ${response.message()}"))
                    }
                }

                override fun onFailure(call: Call<OpenAIResponse>, t: Throwable) {
                    cont.resumeWithException(t)
                }
            })
        }
    }

}
