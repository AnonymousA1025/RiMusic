package it.fast4x.rimusic.ui.screens.player


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults.colors
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.valentinilk.shimmer.shimmer
import it.fast4x.compose.reordering.animateItemPlacement
import it.fast4x.compose.reordering.draggedItem
import it.fast4x.compose.reordering.rememberReorderingState
import it.fast4x.compose.reordering.reorder
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.QueueType
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.transaction
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.MusicBars
import it.fast4x.rimusic.ui.components.SwipeableQueueItem
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.PlaylistsItemMenu
import it.fast4x.rimusic.ui.components.themed.QueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.items.SongItemPlaceholder
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.ui.styling.onOverlay
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.DisposableListener
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.discoverKey
import it.fast4x.rimusic.utils.downloadedStateMedia
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.queueLoopEnabledKey
import it.fast4x.rimusic.utils.queueTypeKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.reorderInQueueEnabledKey
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.shouldBePlaying
import it.fast4x.rimusic.utils.showButtonPlayerArrowKey
import it.fast4x.rimusic.utils.showButtonPlayerDiscoverKey
import it.fast4x.rimusic.utils.shuffleQueue
import it.fast4x.rimusic.utils.smoothScrollToTop
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.trackLoopEnabledKey
import it.fast4x.rimusic.utils.windows
import kotlinx.coroutines.launch
import me.knighthat.colorPalette
import me.knighthat.thumbnailShape
import me.knighthat.typography
import java.text.SimpleDateFormat
import java.util.Date


@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@androidx.media3.common.util.UnstableApi
@Composable
fun QueueModern(
    navController: NavController,
    onDismiss: () -> Unit
) {
    //val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    val windowInsets = WindowInsets.systemBars

    /*
    val horizontalBottomPaddingValues = windowInsets
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues()

     */
    //val bottomPaddingValues = windowInsets
    //    .only(WindowInsetsSides.Bottom).asPaddingValues()

    val context = LocalContext.current
    val showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, false)
    var queueType by rememberPreference(queueTypeKey, QueueType.Essential)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val binder = LocalPlayerServiceBinder.current

        binder?.player ?: return

        val player = binder.player

        var queueLoopEnabled by rememberPreference(queueLoopEnabledKey, defaultValue = false)
        var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)

        val menuState = LocalMenuState.current

        val thumbnailSizeDp = Dimensions.thumbnails.song
        val thumbnailSizePx = thumbnailSizeDp.px

        var mediaItemIndex by remember {
            mutableStateOf(if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex)
        }

        var windows by remember {
            mutableStateOf(player.currentTimeline.windows)
        }
        var windowsFiltered by remember {
            mutableStateOf(windows)
        }

        var shouldBePlaying by remember {
            mutableStateOf(binder.player.shouldBePlaying)
        }

        player.DisposableListener {
            object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    mediaItemIndex = player.currentMediaItemIndex
                    //if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
                }

                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    windows = timeline.windows
                    mediaItemIndex = player.currentMediaItemIndex
                    //if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    shouldBePlaying = binder.player.shouldBePlaying
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    shouldBePlaying = binder.player.shouldBePlaying
                }
            }
        }

        val reorderingState = rememberReorderingState(
            lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = mediaItemIndex),
            key = windows,
            onDragEnd = player::moveMediaItem,
            extraItemCount = 0
        )

        val rippleIndication = ripple(bounded = false)

        val musicBarsTransition = updateTransition(targetState = mediaItemIndex, label = "")

        var isReorderDisabled by rememberPreference(reorderInQueueEnabledKey, defaultValue = true)

        var downloadState by remember {
            mutableStateOf(Download.STATE_STOPPED)
        }

        var listMediaItems = remember {
            mutableListOf<MediaItem>()
        }
        var listMediaItemsIndex = remember {
            mutableListOf<Int>()
        }

        var selectQueueItems by remember {
            mutableStateOf(false)
        }

        /*
        var showSelectTypeClearQueue by remember {
            mutableStateOf(false)
        }

         */
        var position by remember {
            mutableIntStateOf(0)
        }

        var showConfirmDeleteAllDialog by remember {
            mutableStateOf(false)
        }

        if (showConfirmDeleteAllDialog) {
            ConfirmationDialog(
                text = "Do you really want to clean queue?",
                onDismiss = { showConfirmDeleteAllDialog = false },
                onConfirm = {
                    showConfirmDeleteAllDialog = false
                    val mediacount = binder.player.mediaItemCount - 1
                    for (i in mediacount.downTo(0)) {
                        if (i == mediaItemIndex) null else binder.player.removeMediaItem(i)
                    }
                    listMediaItems.clear()
                    listMediaItemsIndex.clear()
                }
            )
        }

        var plistName by remember {
            mutableStateOf("")
        }

        val exportLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
                if (uri == null) return@rememberLauncherForActivityResult

                context.applicationContext.contentResolver.openOutputStream(uri)
                    ?.use { outputStream ->
                        csvWriter().open(outputStream) {
                            writeRow(
                                "PlaylistBrowseId",
                                "PlaylistName",
                                "MediaId",
                                "Title",
                                "Artists",
                                "Duration",
                                "ThumbnailUrl"
                            )
                            if (listMediaItems.isEmpty()) {
                                windows.forEach {
                                    writeRow(
                                        "",
                                        plistName,
                                        it.mediaItem.mediaId,
                                        it.mediaItem.mediaMetadata.title,
                                        it.mediaItem.mediaMetadata.artist,
                                        "",
                                        it.mediaItem.mediaMetadata.artworkUri
                                    )
                                }
                            } else {
                                listMediaItems.forEach {
                                    writeRow(
                                        "",
                                        plistName,
                                        it.mediaId,
                                        it.mediaMetadata.title,
                                        it.mediaMetadata.artist,
                                        "",
                                        it.mediaMetadata.artworkUri
                                    )
                                }
                            }
                        }
                    }

            }

        var isExporting by rememberSaveable {
            mutableStateOf(false)
        }

        if (isExporting) {
            InputTextDialog(
                onDismiss = {
                    isExporting = false
                },
                title = stringResource(R.string.enter_the_playlist_name),
                value = plistName,
                placeholder = stringResource(R.string.enter_the_playlist_name),
                setValue = { text ->
                    plistName = text
                    try {
                        @SuppressLint("SimpleDateFormat")
                        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                        exportLauncher.launch(
                            "RMPlaylist_${text.take(20)}_${
                                dateFormat.format(
                                    Date()
                                )
                            }"
                        )
                    } catch (e: ActivityNotFoundException) {
                        SmartMessage(
                            context.resources.getString(R.string.info_not_find_app_create_doc),
                            type = PopupType.Warning, context = context
                        )
                    }
                }
            )
        }

        //val isSwipeToActionEnabled by rememberPreference(isSwipeToActionEnabledKey, true)
        val hapticFeedback = LocalHapticFeedback.current
        val showButtonPlayerDiscover by rememberPreference(showButtonPlayerDiscoverKey, false)
        var discoverIsEnabled by rememberPreference(discoverKey, false)
        //if (discoverIsEnabled) ApplyDiscoverToQueue()
        var searching by rememberSaveable { mutableStateOf(false) }
        var filter: String? by rememberSaveable { mutableStateOf(null) }
        val thumbnailRoundness by rememberPreference(
            thumbnailRoundnessKey,
            ThumbnailRoundness.Heavy
        )

        var filterCharSequence: CharSequence
        filterCharSequence = filter.toString()
        if (!filter.isNullOrBlank())
            windowsFiltered = windowsFiltered
                .filter {
                    it.mediaItem.mediaMetadata.title?.contains(filterCharSequence, true) ?: false ||
                            it.mediaItem.mediaMetadata.artist?.contains(filterCharSequence, true) ?: false ||
                            it.mediaItem.mediaMetadata.albumTitle?.contains(filterCharSequence, true) ?: false ||
                            it.mediaItem.mediaMetadata.albumArtist?.contains(filterCharSequence, true) ?: false
                }

        Column {
            Box(
                modifier = Modifier
                    .background(colorPalette().background1)
                    .fillMaxWidth()
            ) {
                if (searching)
                        /*        */
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                //.requiredHeight(30.dp)
                                .padding(all = 10.dp)
                                .padding(top = 30.dp)
                                .fillMaxWidth()
                        ) {
                            AnimatedVisibility(visible = searching) {
                                val focusRequester = remember { FocusRequester() }
                                val focusManager = LocalFocusManager.current
                                val keyboardController = LocalSoftwareKeyboardController.current

                                LaunchedEffect(searching) {
                                    focusRequester.requestFocus()
                                }

                                BasicTextField(
                                    value = filter ?: "",
                                    onValueChange = { filter = it },
                                    textStyle = typography().xs.semiBold,
                                    singleLine = true,
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        if (filter.isNullOrBlank()) filter = ""
                                        focusManager.clearFocus()
                                    }),
                                    cursorBrush = SolidColor(colorPalette().text),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            contentAlignment = Alignment.CenterStart,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 10.dp)
                                        ) {
                                            IconButton(
                                                onClick = {},
                                                icon = R.drawable.search,
                                                color = colorPalette().favoritesIcon,
                                                modifier = Modifier
                                                    .align(Alignment.CenterStart)
                                                    .size(16.dp)
                                            )
                                        }
                                        Box(
                                            contentAlignment = Alignment.CenterStart,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 30.dp)
                                        ) {
                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = filter?.isEmpty() ?: true,
                                                enter = fadeIn(tween(100)),
                                                exit = fadeOut(tween(100)),
                                            ) {
                                                BasicText(
                                                    text = stringResource(R.string.search),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = typography().xs.semiBold.secondary.copy(color = colorPalette().textDisabled)
                                                )
                                            }

                                            innerTextField()
                                        }
                                    },
                                    modifier = Modifier
                                        .height(30.dp)
                                        .fillMaxWidth()
                                        .background(
                                            colorPalette().background4,
                                            shape = thumbnailRoundness.shape()
                                        )
                                        .focusRequester(focusRequester)
                                        .onFocusChanged {
                                            if (!it.hasFocus) {
                                                keyboardController?.hide()
                                                if (filter?.isBlank() == true) {
                                                    filter = null
                                                    searching = false
                                                }
                                            }
                                        }
                                )
                            }
                            /*
                            else {
                                HeaderIconButton(
                                    onClick = { searching = true },
                                    icon = R.drawable.search_circle,
                                    color = colorPalette().text,
                                    iconSize = 24.dp
                                )
                            }

                             */
                        }
                        /*        */

            }
            Box(
                modifier = Modifier
                    .background(if (queueType == QueueType.Modern) Color.Transparent else colorPalette().background1)
                    .weight(1f)
            ) {

                LazyColumn(
                    state = reorderingState.lazyListState,
                    contentPadding = windowInsets
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                        .asPaddingValues(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                    //.nestedScroll(layoutState.preUpPostDownNestedScrollConnection)

                ) {

                    items(
                        items = if (searching) windowsFiltered else windows,
                        key = { it.uid.hashCode() }
                    ) { window ->

                        val currentItem by rememberUpdatedState(window)
                        val checkedState = rememberSaveable { mutableStateOf(false) }

                        //var deltaX by remember { mutableStateOf(0f) }
                        val isPlayingThisMediaItem =
                            mediaItemIndex == window.firstPeriodIndex
                        //val currentItem by rememberUpdatedState(window)
                        val isLocal by remember { derivedStateOf { window.mediaItem.isLocal } }
                        downloadState = getDownloadState(window.mediaItem.mediaId)
                        val isDownloaded =
                            if (!isLocal) downloadedStateMedia(window.mediaItem.mediaId) else true

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .draggedItem(
                                    reorderingState = reorderingState,
                                    index = window.firstPeriodIndex
                                )

                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .zIndex(5f)
                                    .align(Alignment.TopEnd)
                                    .offset(x = -15.dp)

                            ) {

                                if (!isReorderDisabled) {
                                    IconButton(
                                        icon = R.drawable.reorder,
                                        color = colorPalette().textDisabled,
                                        indication = rippleIndication,
                                        onClick = {},
                                        modifier = Modifier
                                            .reorder(
                                                reorderingState = reorderingState,
                                                index = window.firstPeriodIndex
                                            )
                                    )
                                }
                            }

                            SwipeableQueueItem(
                                mediaItem = window.mediaItem,
                                onSwipeToLeft = {
                                    player.removeMediaItem(currentItem.firstPeriodIndex)
                                    SmartMessage("${context.resources.getString(R.string.deleted)} ${currentItem.mediaItem.mediaMetadata.title}", type = PopupType.Warning, context = context)
                                },
                                onSwipeToRight = {
                                    binder.player.addNext(
                                        window.mediaItem,
                                        context
                                    )
                                }
                            ) {
                                SongItem(
                                    song = window.mediaItem,
                                    isDownloaded = isDownloaded,
                                    onDownloadClick = {
                                        binder.cache.removeResource(window.mediaItem.mediaId)
                                        if (!isLocal)
                                            manageDownload(
                                                context = context,
                                                mediaItem = window.mediaItem,
                                                downloadState = isDownloaded
                                            )
                                    },
                                    downloadState = downloadState,
                                    thumbnailSizePx = thumbnailSizePx,
                                    thumbnailSizeDp = thumbnailSizeDp,
                                    onThumbnailContent = {
                                        musicBarsTransition.AnimatedVisibility(
                                            visible = { it == window.firstPeriodIndex },
                                            enter = fadeIn(tween(800)),
                                            exit = fadeOut(tween(800)),
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .background(
                                                        color = Color.Black.copy(alpha = 0.25f),
                                                        shape = thumbnailShape()
                                                    )
                                                    .size(Dimensions.thumbnails.song)
                                            ) {
                                                if (shouldBePlaying) {
                                                    MusicBars(
                                                        color = colorPalette().onOverlay,
                                                        modifier = Modifier
                                                            .height(24.dp)
                                                    )
                                                } else {
                                                    Image(
                                                        painter = painterResource(R.drawable.play),
                                                        contentDescription = null,
                                                        colorFilter = ColorFilter.tint(colorPalette().onOverlay),
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    trailingContent = {
                                        if (selectQueueItems)
                                            Checkbox(
                                                checked = checkedState.value,
                                                onCheckedChange = {
                                                    checkedState.value = it
                                                    if (it) {
                                                        listMediaItems.add(window.mediaItem)
                                                        listMediaItemsIndex.add(window.firstPeriodIndex)
                                                    } else {
                                                        listMediaItems.remove(window.mediaItem)
                                                        listMediaItemsIndex.remove(window.firstPeriodIndex)
                                                    }
                                                },
                                                colors = colors(
                                                    checkedColor = colorPalette().accent,
                                                    uncheckedColor = colorPalette().text
                                                ),
                                                modifier = Modifier
                                                    .scale(0.7f)
                                            )
                                        else checkedState.value = false

                                        /*
                                        if (!isReorderDisabled) {
                                            IconButton(
                                                icon = R.drawable.reorder,
                                                color = colorPalette().textDisabled,
                                                indication = rippleIndication,
                                                onClick = {},
                                                modifier = Modifier
                                                    .reorder(
                                                        reorderingState = reorderingState,
                                                        index = window.firstPeriodIndex
                                                    )
                                                    .size(18.dp)
                                            )
                                        }

                                         */
                                    },
                                    modifier = Modifier
                                        .combinedClickable(
                                            onLongClick = {
                                                menuState.display {
                                                    QueuedMediaItemMenu(
                                                        navController = navController,
                                                        mediaItem = window.mediaItem,
                                                        indexInQueue = if (isPlayingThisMediaItem) null else window.firstPeriodIndex,
                                                        onDismiss = menuState::hide,
                                                        onDownload = {
                                                            manageDownload(
                                                                context = context,
                                                                mediaItem = window.mediaItem,
                                                                downloadState = isDownloaded
                                                            )
                                                        }

                                                    )
                                                }
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                            },
                                            onClick = {
                                                if (!selectQueueItems) {
                                                    if (isPlayingThisMediaItem) {
                                                        if (shouldBePlaying) {
                                                            player.pause()
                                                        } else {
                                                            player.play()
                                                        }
                                                    } else {
                                                        player.seekToDefaultPosition(window.firstPeriodIndex)
                                                        player.playWhenReady = true
                                                    }
                                                } else checkedState.value = !checkedState.value
                                            }
                                        )
                                        /*
                                        .draggedItem(
                                            reorderingState = reorderingState,
                                            index = window.firstPeriodIndex
                                        )

                                         */
                                        .animateItemPlacement(reorderingState)
                                        .background(color = if (queueType == QueueType.Modern) Color.Transparent else colorPalette().background0)

                                )
                            }
                        }
                    }

                    item {
                        if (binder.isLoadingRadio) {
                            Column(
                                modifier = Modifier
                                    .shimmer()
                            ) {
                                repeat(3) { index ->
                                    SongItemPlaceholder(
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .alpha(1f - index * 0.125f)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    item(
                        key = "footer",
                        contentType = 0
                    ) {
                        Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                    }
                }

                /*
                if(uiType == UiType.ViMusic)
                FloatingActionsContainerWithScrollToTop(
                    lazyListState = reorderingState.lazyListState,
                    iconId = R.drawable.shuffle,
                    visible = !reorderingState.isDragging,
                    windowInsets = windowInsets.only(WindowInsetsSides.Horizontal),
                    onClick = {
                        reorderingState.coroutineScope.launch {
                            reorderingState.lazyListState.smoothScrollToTop()
                        }.invokeOnCompletion {
                            player.shuffleQueue()
                        }
                    }
                )
                */

                //FloatingActionsContainerWithScrollToTop(lazyListState = reorderingState.lazyListState)


            }

            //val backgroundProgress by rememberPreference(backgroundProgressKey, BackgroundProgress.MiniPlayer)
            //val positionAndDuration by binder.player.positionAndDurationState()
            Box(
                modifier = Modifier
                    //.clip(shape)
                    .clickable(onClick = onDismiss)
                    .background(colorPalette().background1)
                    .fillMaxWidth()
                    //.padding(horizontal = 8.dp)
                    //.padding(horizontalBottomPaddingValues)
                    .height(60.dp) //bottom bar queue
                /*
                .drawBehind {
                    if (backgroundProgress == BackgroundProgress.Both || backgroundProgress == BackgroundProgress.MiniPlayer) {
                        drawRect(
                            color = colorPalette().favoritesOverlay,
                            topLeft = Offset.Zero,
                            size = Size(
                                width = positionAndDuration.first.toFloat() /
                                        positionAndDuration.second.absoluteValue * size.width,
                                height = size.maxDimension
                            )
                        )
                    }
                }
                 */
            ) {

                if (!isLandscape)
                    Box(
                        modifier = Modifier
                            .absoluteOffset(0.dp, -65.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        MiniPlayer(
                            showPlayer = {
                                //navController.navigate(NavRoutes.player.name)
                            },
                            hidePlayer = {})
                    }


                if (!showButtonPlayerArrow)
                    Image(
                        painter = painterResource(R.drawable.horizontal_bold_line_rounded),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette().text),
                        modifier = Modifier
                            .absoluteOffset(0.dp, -10.dp)
                            .align(Alignment.TopCenter)
                            .size(30.dp)
                    )


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .align(Alignment.CenterStart)

                ) {

                    BasicText(
                        text = "${binder.player.mediaItemCount} ", //+ stringResource(R.string.songs), //+ " " + stringResource(R.string.on_queue),
                        style = typography().xxs.medium,
                    )
                    Image(
                        painter = painterResource(R.drawable.musical_notes),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette().textSecondary),
                        modifier = Modifier
                            .size(12.dp)
                    )

                }


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 4.dp)
                    // .fillMaxHeight()

                ) {
                    IconButton(
                        icon = R.drawable.search_circle,
                        color = colorPalette().text,
                        onClick = {
                            searching = !searching
                            if (searching)
                                windowsFiltered = windows
                        },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(24.dp)
                    )

                    if (showButtonPlayerDiscover) {
                        IconButton(
                            icon = R.drawable.star_brilliant,
                            color = if (discoverIsEnabled) colorPalette().text else colorPalette().textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(24.dp)
                                .combinedClickable(
                                    onClick = { discoverIsEnabled = !discoverIsEnabled },
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.discoverinfo),
                                            context = context
                                        )
                                    }

                                )
                        )

                        Spacer(
                            modifier = Modifier
                                .width(12.dp)
                        )

                    }

                    IconButton(
                        icon = if (isReorderDisabled) R.drawable.locked else R.drawable.unlocked,
                        color = colorPalette().text,
                        onClick = { isReorderDisabled = !isReorderDisabled },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(24.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .width(12.dp)
                    )
                    IconButton(
                        icon = R.drawable.repeat,
                        color = if (queueLoopEnabled) colorPalette().text else colorPalette().textDisabled,
                        onClick = {
                            queueLoopEnabled = !queueLoopEnabled
                            if (queueLoopEnabled) trackLoopEnabled = false
                        },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(24.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .width(12.dp)
                    )

                    IconButton(
                        icon = R.drawable.shuffle,
                        color = colorPalette().text,
                        enabled = !reorderingState.isDragging,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(24.dp),
                        onClick = {
                            reorderingState.coroutineScope.launch {
                                reorderingState.lazyListState.smoothScrollToTop()
                            }.invokeOnCompletion {
                                player.shuffleQueue()
                            }
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .width(12.dp)
                    )
                    HeaderIconButton(
                        icon = R.drawable.ellipsis_horizontal,
                        color = if (windows.isNotEmpty() == true) colorPalette().text else colorPalette().textDisabled,
                        enabled = windows.isNotEmpty() == true,
                        modifier = Modifier
                            .padding(end = 4.dp),
                        onClick = {
                            menuState.display {
                                PlaylistsItemMenu(
                                    navController = navController,
                                    onDismiss = menuState::hide,
                                    onSelectUnselect = {
                                        selectQueueItems = !selectQueueItems
                                        if (!selectQueueItems) {
                                            listMediaItems.clear()
                                        }
                                    },
                                    /*
                                    onSelect = { selectQueueItems = true },
                                    onUncheck = {
                                        selectQueueItems = false
                                        listMediaItems.clear()
                                        listMediaItemsIndex.clear()
                                    },
                                     */
                                    onDelete = {
                                        if (listMediaItemsIndex.isNotEmpty())
                                        //showSelectTypeClearQueue = true else
                                        {
                                            val mediacount = listMediaItemsIndex.size - 1
                                            listMediaItemsIndex.sort()
                                            for (i in mediacount.downTo(0)) {
                                                //if (i == mediaItemIndex) null else
                                                binder.player.removeMediaItem(listMediaItemsIndex[i])
                                            }
                                            listMediaItemsIndex.clear()
                                            listMediaItems.clear()
                                            selectQueueItems = false
                                        } else {
                                            showConfirmDeleteAllDialog = true
                                        }
                                    },
                                    onAddToPlaylist = { playlistPreview ->
                                        position =
                                            playlistPreview.songCount.minus(1) ?: 0
                                        //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                        if (position > 0) position++ else position = 0
                                        //Log.d("mediaItem", "next initial pos ${position}")
                                        if (listMediaItems.isEmpty()) {
                                            windows.forEachIndexed { index, song ->
                                                transaction {
                                                    Database.insert(song.mediaItem)
                                                    Database.insert(
                                                        SongPlaylistMap(
                                                            songId = song.mediaItem.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + index
                                                        )
                                                    )
                                                }
                                                //Log.d("mediaItemPos", "added position ${position + index}")
                                            }
                                        } else {
                                            listMediaItems.forEachIndexed { index, song ->
                                                //Log.d("mediaItemMaxPos", position.toString())
                                                transaction {
                                                    Database.insert(song)
                                                    Database.insert(
                                                        SongPlaylistMap(
                                                            songId = song.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + index
                                                        )
                                                    )
                                                }
                                                //Log.d("mediaItemPos", "add position $position")
                                            }
                                            listMediaItems.clear()
                                            listMediaItemsIndex.clear()
                                            selectQueueItems = false
                                        }
                                    },
                                    onExport = {
                                        isExporting = true
                                    },
                                    onGoToPlaylist = {
                                        navController.navigate("${NavRoutes.localPlaylist.name}/$it")
                                    }
                                )
                            }
                        }
                    )


                    if (showButtonPlayerArrow) {
                        Spacer(
                            modifier = Modifier
                                .width(12.dp)
                        )
                        IconButton(
                            icon = R.drawable.chevron_down,
                            color = colorPalette().text,
                            onClick = onDismiss,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(24.dp)
                        )
                    }


                }
            }
        }

        FloatingActionsContainerWithScrollToTop(
            lazyListState = reorderingState.lazyListState,
            modifier = Modifier.padding(bottom = Dimensions.collapsedPlayer)
        )
    }
}
