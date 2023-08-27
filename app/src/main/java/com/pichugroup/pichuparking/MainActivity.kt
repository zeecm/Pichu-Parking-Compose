package com.pichugroup.pichuparking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import com.pichugroup.pichuparking.screens.GoogleMapViewScreen
import com.pichugroup.pichuparking.ui.theme.PichuParkingComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PichuParkingComposeTheme {
                Column {
                    GoogleMapViewScreen()
                }
            }
        }
    }
}
