package com.example.collegeattendanceapp.dataclass

import com.google.gson.annotations.SerializedName

data class VisitorData(
    @SerializedName("status"  ) var status  : Int?     = null,
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : Data?    = Data()
)

data class Data (
    @SerializedName("username"          ) var username        : String?  = null,
    @SerializedName("email"             ) var email           : String?  = null,
    @SerializedName("purpose"           ) var purpose         : String?  = null,
    @SerializedName("contact"           ) var contact         : String?  = null,
    @SerializedName("address"           ) var address         : String?  = null,
    @SerializedName("whomToMeet"        ) var whomToMeet      : String?  = null,
    @SerializedName("vehicle_number"    ) var vehicleNumber   : String?  = null,
    @SerializedName("num_people"        ) var numPeople       : Int?     = null,
    @SerializedName("id_proof_url"      ) var idProofUrl      : String?  = null,
    @SerializedName("user_image_url"    ) var userImageUrl    : String?  = null,
    @SerializedName("vehicle_image_url" ) var vehicleImageUrl : String?  = null,
    @SerializedName("checked_out"       ) var checkedOut      : Boolean? = null,
    @SerializedName("check_out_time"    ) var checkOutTime    : String?  = null,
    @SerializedName("created_by"        ) var createdBy       : String?  = null,
    @SerializedName("notes"             ) var notes           : String?  = null,
    @SerializedName("status"            ) var status          : Boolean? = null,
    @SerializedName("reason"            ) var reason          : String?  = null,
    @SerializedName("canGO"             ) var canGO           : String?  = null,
    @SerializedName("ToTeacher"         ) var ToTeacher       : Boolean? = null,
    @SerializedName("_id"               ) var Id              : String?  = null,
    @SerializedName("check_in_time"     ) var checkInTime     : String?  = null,
    @SerializedName("createdAt"         ) var createdAt       : String?  = null,
    @SerializedName("updatedAt"         ) var updatedAt       : String?  = null,
    @SerializedName("__v"               ) var _v              : Int?     = null

)
