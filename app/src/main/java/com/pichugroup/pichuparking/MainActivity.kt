package com.pichugroup.pichuparking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import com.pichugroup.pichuparking.screens.MapViewScreen
import com.pichugroup.pichuparking.ui.theme.PichuParkingComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PichuParkingComposeTheme {
                Box {
                    MapViewScreen()
                }
            }
        }
    }
}
