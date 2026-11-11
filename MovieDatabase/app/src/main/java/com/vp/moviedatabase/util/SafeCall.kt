package com.vp.moviedatabase.util

import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

/**
 * Pengganti runCatching untuk coroutine: CancellationException WAJIB dilempar ulang,
 * kalau tidak, pembatalan coroutine (mis. ViewModel dibuang) ikut "tertelan".
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

/** Ubah exception teknis menjadi pesan yang ramah dibaca pengguna. */
fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> when (code()) {
        401 -> "API key tidak valid. Periksa TMDB_API_KEY di local.properties."
        404 -> "Data tidak ditemukan."
        else -> "Server bermasalah (kode ${code()})."
    }
    is IOException -> "Tidak ada koneksi internet."
    else -> message ?: "Terjadi kesalahan."
}
