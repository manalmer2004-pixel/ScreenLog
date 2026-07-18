package com.screenlog.app.data.remote.tmdb.dto

import com.google.gson.annotations.SerializedName

data class TmdbCreditsDto(
    @SerializedName("cast") val cast: List<CastMemberDto>?,
    @SerializedName("crew") val crew: List<CrewMemberDto>?
) {
    data class CastMemberDto(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("character") val character: String
    )

    data class CrewMemberDto(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("job") val job: String // E.g., "Director"
    )
}
