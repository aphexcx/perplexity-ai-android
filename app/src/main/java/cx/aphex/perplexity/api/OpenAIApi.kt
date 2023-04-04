package cx.aphex.perplexity.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIApi {
    @Headers("Content-Type: application/json")
    @POST("v1/engines/text-davinci-003/turbo/completions")
    fun generateAnswer(@Body body: String): Call<OpenAIResponse>

}