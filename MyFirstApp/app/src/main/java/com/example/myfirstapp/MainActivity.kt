package com.example.myfirstapp

import android.R.attr.onClick
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.myfirstapp.ui.theme.MyFirstAppTheme
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

const val EXTRA_USER_INFO = "user_info"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val name = stringResource(R.string.first_and_next_name)
            val group = stringResource(R.string.group)
            val continueButton = stringResource(R.string.continue_button)
            Column(
                modifier = Modifier.padding(top = 50.dp)

            ){
                Text(name);
                Text(group);
                Button(
                    onClick = {
                        val intent = Intent(this@MainActivity, SecondActivity::class.java)
                        intent.putExtra(EXTRA_USER_INFO,name + "/" + group)
                        startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF69B4)
                    )
                ){
                    Text(continueButton)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyFirstAppTheme {
        Greeting("Android")
    }
}