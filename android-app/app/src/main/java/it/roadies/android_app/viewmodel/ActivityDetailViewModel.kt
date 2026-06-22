package it.roadies.android_app.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.repository.ActivityRepository
import javax.inject.Inject

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(private val activityRepository: ActivityRepository): ViewModel(){

}