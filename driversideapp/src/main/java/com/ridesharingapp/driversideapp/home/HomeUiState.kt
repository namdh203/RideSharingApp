package com.ridesharingapp.driversideapp.home

sealed interface HomeUiState {
    object SearchingForPassengers: HomeUiState
    data class PassengerPickUp(
        val passengerLat: Double,
        val passengerLon: Double,
        val driverLat: Double,
        val driverLon: Double,
        val destinationLat: Double,
        val destinationLon: Double,
        val destinationAddress: String,
        val passengerName: String,
        val passengerAvatar: String,
        val totalMessages: Int
    ): HomeUiState
    data class EnRoute(
        val driverLat: Double,
        val driverLon: Double,
        val destinationLat: Double,
        val destinationLon: Double,
        val destinationAddress: String,
        val passengerName: String,
        val passengerAvatar: String,
        val totalMessages: Int
    ): HomeUiState

    data class Arrived(
        val driverLat: Double,
        val driverLon: Double,
        val destinationLat: Double,
        val destinationLon: Double,
        val destinationAddress: String,
        val passengerName: String,
        val passengerAvatar: String,
        val totalMessages: Int
    ): HomeUiState

    //Signals something unexpected has happened
    object Error: HomeUiState
    object Loading: HomeUiState
}