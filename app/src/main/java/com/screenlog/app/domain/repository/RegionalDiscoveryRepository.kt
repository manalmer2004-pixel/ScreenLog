package com.screenlog.app.domain.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.Title
import com.screenlog.app.domain.model.Filmmaker
import com.screenlog.app.domain.model.Region
import kotlinx.coroutines.flow.Flow

interface RegionalDiscoveryRepository {
    fun getSupportedRegions(): Flow<List<Region>>
    suspend fun getTopRatedInRegion(countryCode: String): Resource<List<Title>>
    suspend fun getCuratedLocalProductions(countryCode: String): Resource<List<Title>>
    suspend fun getEmergingFilmmakers(countryCode: String): Resource<List<Filmmaker>>
    suspend fun getLocallyProduced(): Resource<List<Title>>
    suspend fun getRegionallyProduced(): Resource<List<Title>>
}
