package com.cccinfotech.fooddeliverypoc.component_design

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cccinfotech.fooddeliverypoc.model.banner.Banner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AutoSlidingBannerSlider(banners: List<Banner>) {
    if (banners.isEmpty()) return

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(banners) {
        var index = 0
        while (true) {
            delay(3000)
            index = (index + 1) % banners.size
            coroutineScope.launch {
                listState.animateScrollToItem(index)
            }
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp),
        contentPadding = PaddingValues(horizontal = 15.dp)
    ) {
        items(
            items = banners,
            key = { it.id }
        ) { banner ->
            Card(
                modifier = Modifier
                    .width(screenWidth - 16.dp)
                    .padding(4.dp)
                    .clickable { },
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = banner.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        }
    }
}

