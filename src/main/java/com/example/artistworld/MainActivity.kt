
package com.example.artistworld

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.artistworld.ui.theme.ArtistWorldTheme

private val PinkBackground = Color(0xFFFFF3F6)
private val PinkAccent = Color(0xFFE88DAA)
private val DarkText = Color(0xFF392832)
private val SoftPink = Color(0xFFFFE0E9)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ArtistWorldTheme {
                ArtistListScreen(
                    onArtistClick = { artist ->
                        val intent = Intent(
                            this@MainActivity,
                            SecondActivity::class.java
                        )

                        intent.putExtra(
                            SecondActivity.EXTRA_NAME,
                            artist
                        )

                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistListScreen(
    onArtistClick: (String) -> Unit
) {
    val artists = listOf(
        stringResource(R.string.artist_1),
        stringResource(R.string.artist_2),
        stringResource(R.string.artist_3),
        stringResource(R.string.artist_4),
        stringResource(R.string.artist_5),
        stringResource(R.string.artist_6),
        stringResource(R.string.artist_7),
        stringResource(R.string.artist_8),
        stringResource(R.string.artist_9),
        stringResource(R.string.artist_10)
    )

    Scaffold(
        containerColor = PinkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_title),
                        color = DarkText,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PinkBackground)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.screen_subtitle),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.screen_description),
                fontSize = 14.sp,
                color = Color(0xFF8B7480)
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(artists) { index, artist ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onArtistClick(artist)
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 3.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = SoftPink,
                                        shape = CircleShape
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = PinkAccent,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 14.dp)
                            ) {
                                Text(
                                    text = artist,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkText
                                )

                                Text(
                                    text = stringResource(R.string.artist_hint),
                                    fontSize = 12.sp,
                                    color = Color(0xFF9B8791),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

