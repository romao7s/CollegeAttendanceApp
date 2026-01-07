package com.example.collegeattendanceapp.retrofit

import com.example.collegeattendanceapp.dataclass.TeacherAll
import com.example.collegeattendanceapp.dataclass.VisitorData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("Visitor/add")
    suspend fun visitorAdd(
        @Header("Authorization") token: String,

        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("purpose") purpose: RequestBody,
        @Part("contact") contact: RequestBody,
        @Part("address") address: RequestBody,
        @Part("num_people") numPeople: RequestBody,
        @Part("vehicle_number") vehicleNumber: RequestBody?,

        @Part vehicleImage: MultipartBody.Part?,
        @Part idProofImage: MultipartBody.Part?,
        @Part userImage: MultipartBody.Part?,

        @Part("notes") notes: RequestBody?,
        @Part("whomToMeet") whomToMeet: RequestBody
    ): Response<VisitorData>

    @POST("Teacher/All")
    suspend fun getTeachers(
        @Header("Authorization") token: String
    ): Response<TeacherAll>

}



