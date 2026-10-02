package com.example.myfirstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class SecondActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resExtra = intent.getStringExtra(EXTRA_USER_INFO)
        setContent{
            Text(
                text = resExtra ?: "",
                modifier = Modifier.padding(top = 50.dp)
            )
        }
    }

}