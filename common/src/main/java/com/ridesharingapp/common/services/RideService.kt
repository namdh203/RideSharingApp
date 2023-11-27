package com.ridesharingapp.common.services

import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.domain.GrabLamUser
import com.ridesharingapp.common.domain.Ride
import kotlinx.coroutines.flow.Flow

interface RideService {

    fun openRides(): Flow<ServiceResult<List<Ride>>>

    fun rideFlow(): Flow<ServiceResult<Ride?>>
    suspend fun getRideIfInProgress() : ServiceResult<String?>
    suspend fun observeRideById(rideId: String)
    suspend fun observeOpenRides()

    /**
     * If successful, it returns the id of the ride (i.e. channel) which the driver has connected
     * to.
     */
    suspend fun connectDriverToRide(ride: Ride, driver: GrabLamUser): ServiceResult<String>
    suspend fun createRide(
        passengerId: String,
        passengerName: String,
        passengerLat: Double,
        passengerLon: Double,
        passengerAvatarUrl: String,
        destinationAddress: String,
        destLat: Double,
        destLon: Double,
    ): ServiceResult<String>

    suspend fun cancelRide(): ServiceResult<Unit>
    suspend fun completeRide(ride: Ride): ServiceResult<Unit>

    suspend fun advanceRide(rideId: String, newState: String): ServiceResult<Unit>

    suspend fun updateDriverLocation(ride: Ride, lat: Double, lon: Double): ServiceResult<Unit>
    suspend fun updatePassengerLocation(ride:Ride, lat: Double, lon: Double): ServiceResult<Unit>
}