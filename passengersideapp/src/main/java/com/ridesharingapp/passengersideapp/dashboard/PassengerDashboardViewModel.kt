package com.ridesharingapp.passengersideapp.dashboard

import android.util.Log
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.maps.model.LatLng
import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.domain.GrabLamUser
import com.ridesharingapp.common.services.RideService
import com.ridesharingapp.common.uicommon.ToastMessages
import com.ridesharingapp.common.uicommon.combineTuple
import com.ridesharingapp.common.usecases.GetUser
import com.ridesharingapp.passengersideapp.navigation.ChatKey
import com.ridesharingapp.passengersideapp.navigation.LoginKey
import com.ridesharingapp.passengersideapp.navigation.ProfileSettingsKey
import com.ridesharingapp.passengersideapp.navigation.SplashKey
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.simplestack.StateChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class PassengerDashboardViewModel(
    val backstack: Backstack,
    val getUser: GetUser,
    val rideService: RideService,
    val googleService: com.ridesharingapp.common.google.GoogleService
) : ScopedServices.Activated, CoroutineScope {

    private val canceller = Job()

    override val coroutineContext: CoroutineContext
        get() = canceller + Dispatchers.Main

    internal var toastHandler: ((ToastMessages) -> Unit)? = null

    private var _passengerModel = MutableStateFlow<GrabLamUser?>(null)
    private var _rideModel: Flow<ServiceResult<com.ridesharingapp.common.domain.Ride?>> = rideService.rideFlow()
    private val _mapIsReady = MutableStateFlow(false)

    /*
    Different UI states:
    1. User may never be null
    2. Ride may be null (If User.status is INACTIVE, then no need to try to fetch a ride)
    3. Ride may be not null, and in varying states:
        - SEARCHING_FOR_DRIVER
        - PASSENGER_PICK_UP
        - EN_ROUTE
        - ARRIVED
     */
    val uiState = combineTuple(
        _passengerModel,
        _rideModel,
        _mapIsReady
    ).map { (passenger, rideResult, isMapReady) ->
        if (rideResult is ServiceResult.Failure) {
            Log.e("PassengerDashboardViewModel", "uiState:map:rideResult is failure ${passenger?.email} $isMapReady", rideResult.exception)
            return@map PassengerDashboardUiState.Error
        }

        val ride = (rideResult as ServiceResult.Value).value

        //only publish state updates whe map is ready!
        if (passenger == null || !isMapReady) PassengerDashboardUiState.Loading
        else {
            when {
                ride == null -> PassengerDashboardUiState.RideInactive

                ride.driverId == null -> PassengerDashboardUiState.SearchingForDriver(
                    ride.passengerLatitude,
                    ride.passengerLongitude,
                    ride.destinationAddress
                )

                ride.status == com.ridesharingapp.common.domain.RideStatus.PASSENGER_PICK_UP.value
                        && ride.driverLatitude != null
                        && ride.driverLongitude != null -> PassengerDashboardUiState.PassengerPickUp(
                    passengerLat = ride.passengerLatitude,
                    passengerLon = ride.passengerLongitude,
                    driverLat = ride.driverLatitude!!,
                    driverLon = ride.driverLongitude!!,
                    destinationLat = ride.destinationLatitude,
                    destinationLon = ride.destinationLongitude,
                    destinationAddress = ride.destinationAddress,
                    driverName = ride.driverName ?: "Error",
                    driverAvatar = ride.driverAvatarUrl ?: "",
                    totalMessages = ride.totalMessages
                )

                ride.status == com.ridesharingapp.common.domain.RideStatus.EN_ROUTE.value
                        && ride.driverLatitude != null
                        && ride.driverLongitude != null -> PassengerDashboardUiState.EnRoute(
                    passengerLat = ride.passengerLatitude,
                    passengerLon = ride.passengerLongitude,
                    driverName = ride.driverName ?: "Error",
                    destinationAddress = ride.destinationAddress,
                    destinationLat = ride.destinationLatitude,
                    destinationLon = ride.destinationLongitude,
                    driverAvatar = ride.driverAvatarUrl ?: "",
                    totalMessages = ride.totalMessages,
                    driverLat = ride.driverLatitude!!,
                    driverLon = ride.driverLongitude!!
                )

                ride.status == com.ridesharingapp.common.domain.RideStatus.ARRIVED.value
                        && ride.driverLatitude != null
                        && ride.driverLongitude != null -> PassengerDashboardUiState.Arrived(
                    passengerLat = ride.passengerLatitude,
                    passengerLon = ride.passengerLongitude,
                    driverName = ride.driverName ?: "Error",
                    destinationLat = ride.destinationLatitude,
                    destinationLon = ride.destinationLongitude,
                    destinationAddress = ride.destinationAddress,
                    driverAvatar = ride.driverAvatarUrl ?: "",
                    totalMessages = ride.totalMessages
                )

                else -> {
                    Log.d("ELSE", "${passenger}, $ride")
                    PassengerDashboardUiState.Error
                }
            }
        }
    }

    private val _autoCompleteList = MutableStateFlow<List<AutoCompleteModel>>(emptyList())
    val autoCompleteList: StateFlow<List<AutoCompleteModel>> get() = _autoCompleteList

    private var passengerLatLng = LatLng()

    fun mapIsReady() {
        _mapIsReady.value = true
    }

    private fun getPassenger() = launch(Dispatchers.Main) {
        val getUser = getUser.getUser()
        when (getUser) {
            is ServiceResult.Failure -> {
                Log.e("PassengerDashboardViewModel", "getPassenger failed", getUser.exception)
                toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
                sendToLogin()
            }
            is ServiceResult.Value -> {
                if (getUser.value == null) {
                    Log.e("PassengerDashboardViewModel", "getPassenger null user")
                    sendToLogin()
                }
                else getActiveRideIfItExists(getUser.value!!)
            }
        }
    }

    private suspend fun getActiveRideIfItExists(user: GrabLamUser) {
        val result = rideService.getRideIfInProgress()

        when (result) {
            is ServiceResult.Failure -> {
                Log.e("PassengerDashboardViewModel", "getActiveRideIfItExists failed", result.exception)
                toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
                sendToLogin()
            }
            is ServiceResult.Value -> {
                //if null, no active ride exists
                if (result.value == null) _passengerModel.value = user
                else observeRideModel(result.value!!, user)
            }
        }
    }


    /**
     * The Passenger model must always be the last model which is mutated from a null state. By
     * setting the other models first, we avoid the UI rapidly switching between different states
     * in a disorganized way.
     */
    private suspend fun observeRideModel(rideId: String, user: GrabLamUser) {
        //The result of this call is handled inside the flowable assigned to _rideModel
        rideService.observeRideById(rideId)
        _passengerModel.value = user
    }

    fun handleSearchItemClick(selectedPlace: AutoCompleteModel) = launch(Dispatchers.Main) {
        val getCoordinates = googleService.getPlaceCoordinates(selectedPlace.prediction.placeId)

        when (getCoordinates) {
            is ServiceResult.Failure -> {
                Log.e("PassengerDashboardViewModel", "handleSearchItemClick:getCoordinates failed", getCoordinates.exception)
                toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
            }
            is ServiceResult.Value -> {
                if (getCoordinates.value != null &&
                    getCoordinates.value!!.place.latLng != null
                ) {
                    attemptToCreateNewRide(getCoordinates.value!!, selectedPlace.address)
                } else toastHandler?.invoke(ToastMessages.UNABLE_TO_RETRIEVE_COORDINATES)
            }
        }
    }

    private suspend fun attemptToCreateNewRide(response: FetchPlaceResponse, address: String) {
        val result = rideService.createRide(
            destLat = response.place.latLng!!.latitude,
            destLon = response.place.latLng!!.longitude,
            destinationAddress = address,
            passengerId = _passengerModel.value!!.userId,
            passengerAvatarUrl = _passengerModel.value!!.avatarPhotoUrl,
            passengerName = _passengerModel.value!!.username,
            passengerLat = passengerLatLng.lat,
            passengerLon = passengerLatLng.lng
        )

        when (result) {
            is ServiceResult.Failure -> {
                Log.w("PassengerDashboardViewModel", "attempToCreateNewRide failed", result.exception)
                toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
            }
            is ServiceResult.Value -> {
                _autoCompleteList.value = emptyList()
                observeRideModel(result.value, _passengerModel.value!!)
            }
        }
    }

    fun requestAutocompleteResults(query: String) = launch(Dispatchers.Main) {
        val autocompleteRequest = googleService.getAutocompleteResults(query)
        when (autocompleteRequest) {
            is ServiceResult.Failure -> {
                Log.e("PassengerDashboardViewModel", "requestAutocompleteResults failed", autocompleteRequest.exception)
                toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
            }
            is ServiceResult.Value -> {
                _autoCompleteList.value = autocompleteRequest.value.map { prediction ->
                    AutoCompleteModel(
                        address = prediction.getFullText(null).toString(),
                        prediction = prediction
                    )
                }
            }
        }
    }

    fun cancelRide() = launch(Dispatchers.Main) {
        val cancelRide = rideService.cancelRide()
        when (cancelRide) {
            is ServiceResult.Failure -> {
                Log.e("PassengerDashboardViewModel", "cancelRide failed", cancelRide.exception)
                toastHandler?.invoke(ToastMessages.GENERIC_ERROR)
                sendToSplash()
            }

            //State should automatically be handled by the flow
            is ServiceResult.Value -> Unit
        }
    }

    private fun sendToLogin() {
        backstack.setHistory(
            History.of(LoginKey()),
            StateChange.BACKWARD
        )
    }

    private fun sendToSplash() {
        backstack.setHistory(
            History.of(SplashKey()),
            StateChange.REPLACE
        )
    }

    override fun onServiceActive() {
        getPassenger()
    }

    override fun onServiceInactive() {
        canceller.cancel()
    }

    fun handleError() {
        sendToLogin()
    }

    fun updatePassengerLocation(latLng: LatLng) = launch(Dispatchers.Main) {
        passengerLatLng = latLng

        val currentRide = _rideModel.first()

        if (currentRide is ServiceResult.Value && currentRide.value != null) {
            val result = rideService.updatePassengerLocation(
                currentRide.value!!,
                latLng.lat,
                latLng.lng
            )

            if (result is ServiceResult.Failure) {
                Log.e("PassengerDashboardViewModel", "updatePassengerLocation", result.exception)
                toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
            }
        } else {
            Log.e("PassengerDashboardViewModel", "updatePassengerLocation:currentRide is null")
        }
    }

    fun openChat() = launch(Dispatchers.Main) {
        val currentRide = _rideModel.first()

        if (currentRide is ServiceResult.Value && currentRide.value != null) {
            backstack.setHistory(
                History.of(ChatKey(currentRide.value!!.rideId)),
                StateChange.FORWARD
            )
        }
    }

    fun goToProfile() {
        //normally we would use backStack.goTo(...), but we always want to reload the state
        //of the dashboard
        backstack.setHistory(
            History.of(ProfileSettingsKey()),
            StateChange.FORWARD
        )
    }
}