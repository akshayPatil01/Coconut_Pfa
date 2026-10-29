package com.patil.coconut_pfa.data.repository

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

data class DriveFileList(
    val files: List<DriveFileItem>?
)

data class DriveFileItem(
    val id: String,
    val name: String,
    val size: String?,
    val createdTime: String?,
    val modifiedTime: String?,
    val appProperties: Map<String, String>?
)

interface DriveApiService {
    @GET("drive/v3/files")
    suspend fun listFiles(
        @Header("Authorization") authHeader: String,
        @Query("spaces") spaces: String = "appDataFolder",
        @Query("fields") fields: String = "files(id,name,size,createdTime,modifiedTime,appProperties)",
        @Query("orderBy") orderBy: String = "createdTime desc"
    ): Response<DriveFileList>

    @Multipart
    @POST("upload/drive/v3/files?uploadType=multipart")
    suspend fun uploadFile(
        @Header("Authorization") authHeader: String,
        @Part metadata: MultipartBody.Part,
        @Part media: MultipartBody.Part
    ): Response<DriveFileItem>

    @DELETE("drive/v3/files/{fileId}")
    suspend fun deleteFile(
        @Header("Authorization") authHeader: String,
        @Path("fileId") fileId: String
    ): Response<Unit>

    @GET("drive/v3/files/{fileId}")
    suspend fun downloadFile(
        @Header("Authorization") authHeader: String,
        @Path("fileId") fileId: String,
        @Query("alt") alt: String = "media"
    ): Response<ResponseBody>
}
