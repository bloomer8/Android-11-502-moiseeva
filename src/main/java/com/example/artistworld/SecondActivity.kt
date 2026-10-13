
package com.example.artistworld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.artistworld.ui.theme.ArtistWorldTheme

private val PinkBackground = Color(0xFFFFF3F6)
private val PinkAccent = Color(0xFFE88DAA)
private val DarkText = Color(0xFF392832)

class SecondActivity : ComponentActivity() {

    companion object {
        const val EXTRA_NAME = "extra_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val artistName = intent.getStringExtra(EXTRA_NAME) ?: ""

        setContent {
            ArtistWorldTheme {
                ArtistDetailsScreen(
                    artistName = artistName,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@Composable
fun ArtistDetailsScreen(
    artistName: String,
    onBackClick: () -> Unit
) {
    val artistData = when (artistName) {
        stringResource(R.string.artist_1) ->
            Pair(R.drawable.taylor_swift, R.string.bio_1)

        stringResource(R.string.artist_2) ->
            Pair(R.drawable.the_weeknd, R.string.bio_2)

        stringResource(R.string.artist_3) ->
            Pair(R.drawable.billie_eilish, R.string.bio_3)

        stringResource(R.string.artist_4) ->
            Pair(R.drawable.bruno_mars, R.string.bio_4)

        stringResource(R.string.artist_5) ->
            Pair(R.drawable.ariana_grande, R.string.bio_5)

        stringResource(R.string.artist_6) ->
            Pair(R.drawable.lady_gaga, R.string.bio_6)

        stringResource(R.string.artist_7) ->
            Pair(R.drawable.justin_bieber, R.string.bio_7)

        stringResource(R.string.artist_8) ->
            Pair(R.drawable.rihanna, R.string.bio_8)

        stringResource(R.string.artist_9) ->
            Pair(R.drawable.ed_sheeran, R.string.bio_9)

        stringResource(R.string.artist_10) ->
            Pair(R.drawable.dua_lipa, R.string.bio_10)

        else -> Pair(R.drawable.taylor_swift, R.string.bio_unknown)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.details_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = artistData.first),
                    contentDescription = artistName,
                    modifier = Modifier
                        .size(width = 220.dp, height = 240.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = artistName,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(artistData.second),
                    fontSize = 16.sp,
                    color = DarkText,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = PinkAccent
            )
        ) {
            Text(
                text = stringResource(R.string.back_button),
                color = Color.White
            )
        }
    }
}
