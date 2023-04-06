package cx.aphex.perplexity.api

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import cx.aphex.perplexity.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.seconds

@BetaOpenAI
object OpenAIClient {
    val config = OpenAIConfig(
        token = BuildConfig.OPENAI_API_KEY,
        timeout = Timeout(socket = 60.seconds),
    )

    val openAI = OpenAI(config)

    fun generateAnswer(prompt: String): Flow<ChatCompletionChunk> {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You are a helpful assistant that accurately answers the user's queries based on the given text."
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            ),
            maxTokens = 1000,
            temperature = 0.0
        )

        return openAI.chatCompletions(chatCompletionRequest)
    }
}