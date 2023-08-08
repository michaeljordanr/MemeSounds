package com.michaeljordanr.memesounds.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.michaeljordanr.memesounds.R
import com.michaeljordanr.memesounds.presentation.components.BookmarksPage
import com.michaeljordanr.memesounds.presentation.components.SoundsPage
import kotlinx.coroutines.launch

const val AUDIO_PAGE = 0
const val BOOKMARK_PAGE = 1

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MemeSoundsScreen() {
    val viewModel: SoundListViewModel = hiltViewModel()

    val pagerState: PagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()
    val tabList =
        arrayListOf(stringResource(id = R.string.all), stringResource(id = R.string.bookmarks))

    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            MyBottomSheet(
                pagerState = pagerState,
                sheetState = bottomSheetState,
                viewModel = viewModel
            )
        },
        content = {
            Scaffold {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(it)
                ) {
                    TabRow(selectedTabIndex = pagerState.currentPage) {
                        // Add tabs for all of our pages
                        tabList.forEachIndexed { index, title ->
                            Tab(
                                text = { Text(title.uppercase()) },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        pageCount = tabList.size,
                        beyondBoundsPageCount = 2
                    ) { page: Int ->

                        when (page) {
                            0 -> {
                                SoundsPage(
                                    viewModel = viewModel,
                                    sheetState = bottomSheetState
                                )
                            }

                            1 -> {
                                BookmarksPage(
                                    viewModel = viewModel,
                                    sheetState = bottomSheetState
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MyBottomSheet(
    pagerState: PagerState,
    sheetState: ModalBottomSheetState,
    viewModel: SoundListViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FilledTonalButton(
            onClick = {
                viewModel.share()
                coroutineScope.launch {
                    sheetState.hide()
                }
            },
            modifier = Modifier.width(300.dp)
        ) {
            Text(
                text = stringResource(id = R.string.share_audio),
                fontSize = 16.sp
            )
        }
        FilledTonalButton(
            onClick = {
                if (pagerState.currentPage == AUDIO_PAGE) {
                    viewModel.bookmark()
                    coroutineScope.launch {
                        pagerState.scrollToPage(BOOKMARK_PAGE)
                    }
                } else {
                    viewModel.unbookmark()
                }
                coroutineScope.launch {
                    sheetState.hide()
                }
            },
            modifier = Modifier.width(300.dp)
        ) {
            Text(
                text = stringResource(id = if (pagerState.currentPage == AUDIO_PAGE) R.string.bookmark else R.string.unbookmark),
                fontSize = 16.sp
            )
        }
    }
}