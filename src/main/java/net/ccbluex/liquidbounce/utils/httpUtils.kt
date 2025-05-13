package net.ccbluex.liquidbounce.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

object HttpUtils {

    private val client: OkHttpClient

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 可选择 BODY、HEADER、BASIC、NONE
        }

        client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // 添加日志拦截器
            .connectTimeout(15, TimeUnit.SECONDS) // 设置连接超时
            .readTimeout(15, TimeUnit.SECONDS)    // 设置读取超时
            .writeTimeout(15, TimeUnit.SECONDS)   // 设置写入超时
            .build()
    }

    /**
     * 发送 GET 请求
     *
     * @param url 请求地址
     * @param headers 请求头 (可选)
     * @return 返回结果字符串
     * @throws IOException 如果请求失败
     */
    @Throws(IOException::class)
    fun get(url: String, headers: Map<String, String> = emptyMap()): String {
        val requestBuilder = Request.Builder().url(url)

        for ((key, value) in headers) {
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            return response.body?.string() ?: throw IOException("Response body is null")
        }
    }
}
