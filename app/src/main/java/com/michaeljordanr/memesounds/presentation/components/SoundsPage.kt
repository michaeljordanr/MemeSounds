@file:OptIn(ExperimentalMaterialApi::class)

package com.michaeljordanr.memesounds.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.michaeljordanr.memesounds.R
import com.michaeljordanr.memesounds.presentation.SoundListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun SoundsPage(viewModel: SoundListViewModel, sheetState: ModalBottomSheetState) {
    val coroutineScope = rememberCoroutineScope()
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery.value,
                onValueChange = viewModel::onSearch,
                modifier = Modifier.fillMaxWidth()
                    .padding(4.dp),
                placeholder = {
                    Text(text = stringResource(id = R.string.search_hint))
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                columns = GridCells.Fixed(3),
                content = {
                    items(viewModel.soundListFilteredState) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .animateItemPlacement()
                                .combinedClickable(
                                    onClick = { viewModel.play(it.audioName) },
                                    onLongClick = {
                                        viewModel.audioSelected = it
                                        coroutineScope.launch {
                                            sheetState.show()
                                        }
                                    },
                                )
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.inversePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = it.audioDescription.uppercase(),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}