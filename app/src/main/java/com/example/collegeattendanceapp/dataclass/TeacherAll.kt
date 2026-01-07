package com.example.collegeattendanceapp.dataclass

import com.google.gson.annotations.SerializedName

data class TeacherAll(
    @SerializedName("status"  ) var status  : Int?            = null,
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("data"    ) var data    : ArrayList<DataAll> = arrayListOf())

data class DataAll (

    @SerializedName("_id"        ) var Id         : String?  = null,
    @SerializedName("name"       ) var name       : String?  = null,
    @SerializedName("email"      ) var email      : String?  = null,
    @SerializedName("contact"    ) var contact    : String?  = null,
    @SerializedName("subject"    ) var subject    : String?  = null,
    @SerializedName("Department" ) var Department : String?  = null,
    @SerializedName("created_by" ) var createdBy  : String?  = null,
    @SerializedName("status"     ) var status     : Boolean? = null,
    @SerializedName("__v"        ) var _v         : Int?     = null

)
