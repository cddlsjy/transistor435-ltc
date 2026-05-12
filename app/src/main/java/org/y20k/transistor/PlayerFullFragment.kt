/*
 * PlayerFullFragment.kt
 * Implements the full screen player
 */

package org.y20k.transistor

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.y20k.transistor.core.Station
import org.y20k.transistor.helpers.PreferencesHelper

class PlayerFullFragment : Fragment() {

    private val TAG: String = PlayerFullFragment::class.java.simpleName
    private var listener: PlayerFullFragmentListener? = null
    private var initialStation: Station? = null
    private var initialIsPlaying: Boolean = false
    private var currentMetadata: String? = null

    private var stationIcon: ImageView? = null
    private var playerStationName: TextView? = null
    private var playerStationMetadata: TextView? = null
    private var textViewStationInfo: TextView? = null
    private var textViewMetadata: TextView? = null
    private var buttonPrev: ImageButton? = null
    private var buttonPlay: ImageButton? = null
    private var buttonNext: ImageButton? = null
    private var buttonFullscreenExit: ImageButton? = null

    interface PlayerFullFragmentListener {
        fun onPlayButtonTapped()
        fun onPreviousButtonTapped()
        fun onNextButtonTapped()
        fun onExitFullscreen()
        fun onOrientationChanged()
    }

    fun setInitialData(station: Station, isPlaying: Boolean, metadata: String? = null) {
        initialStation = station
        initialIsPlaying = isPlaying
        currentMetadata = metadata
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val displayMode = PreferencesHelper.loadFullScreenDisplayMode()
        val backgroundColor = PreferencesHelper.loadFullScreenBackgroundColor()
        
        val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val effectiveDisplayMode = if (displayMode == Keys.FULL_SCREEN_MODE_AUTO) {
            if (isLandscape) Keys.FULL_SCREEN_MODE_LANDSCAPE else Keys.FULL_SCREEN_MODE_DEFAULT
        } else {
            displayMode
        }
        
        val layoutId = when {
            effectiveDisplayMode == Keys.FULL_SCREEN_MODE_LANDSCAPE && backgroundColor == Keys.BACKGROUND_COLOR_DARK_BLUE -> R.layout.fragment_player_full_landscape_dark_blue
            effectiveDisplayMode == Keys.FULL_SCREEN_MODE_LANDSCAPE -> R.layout.fragment_player_full_landscape
            backgroundColor == Keys.BACKGROUND_COLOR_DARK_BLUE -> R.layout.fragment_player_full_dark_blue
            else -> R.layout.fragment_player_full
        }

        val rootView = inflater.inflate(layoutId, container, false)
        setupViews(rootView)
        setupListeners()
        setupKeyListener(rootView)

        initialStation?.let { station ->
            updatePlayerViews(requireContext(), station, initialIsPlaying)
        }

        return rootView
    }

    private fun setupViews(rootView: View) {
        stationIcon = rootView.findViewById(R.id.stationIcon)
        textViewStationInfo = rootView.findViewById(R.id.textViewStationInfo)
        textViewMetadata = rootView.findViewById(R.id.textViewMetadata)
        playerStationName = rootView.findViewById(R.id.playerStationName)
        playerStationMetadata = rootView.findViewById(R.id.playerStationMetadata)
        buttonPrev = rootView.findViewById(R.id.buttonPrev)
        buttonPlay = rootView.findViewById(R.id.buttonPlay)
        buttonNext = rootView.findViewById(R.id.buttonNext)
        buttonFullscreenExit = rootView.findViewById(R.id.buttonFullscreenExit)
    }

    private fun setupListeners() {
        buttonPrev?.setOnClickListener { listener?.onPreviousButtonTapped() }
        buttonPlay?.setOnClickListener { listener?.onPlayButtonTapped() }
        buttonNext?.setOnClickListener { listener?.onNextButtonTapped() }
        buttonFullscreenExit?.setOnClickListener { listener?.onExitFullscreen() }
    }

    private fun setupKeyListener(rootView: View) {
        rootView.isFocusableInTouchMode = true
        rootView.requestFocus()
        rootView.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> { listener?.onPreviousButtonTapped(); true }
                    KeyEvent.KEYCODE_DPAD_DOWN -> { listener?.onNextButtonTapped(); true }
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> { listener?.onPlayButtonTapped(); true }
                    KeyEvent.KEYCODE_BACK -> { listener?.onExitFullscreen(); true }
                    else -> false
                }
            } else false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PlayerFullFragmentListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentMetadata", currentMetadata)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            currentMetadata = it.getString("currentMetadata")
        }
    }

    fun updatePlayerViews(context: Context, station: Station, isPlaying: Boolean) {
        playerStationName?.text = station.name
        textViewStationInfo?.text = station.name
        
        playerStationMetadata?.text = currentMetadata ?: station.name
        textViewMetadata?.text = currentMetadata ?: station.name
        if (!currentMetadata.isNullOrEmpty()) {
            playerStationMetadata?.isSelected = true
            textViewMetadata?.isSelected = true
        }

        val backgroundColor = PreferencesHelper.loadFullScreenBackgroundColor()
        val useWhiteIcons = backgroundColor == Keys.BACKGROUND_COLOR_DARK_BLUE
        if (isPlaying) {
            buttonPlay?.setImageResource(if (useWhiteIcons) R.drawable.ic_pause_symbol_white else R.drawable.ic_pause_symbol)
            buttonPlay?.contentDescription = getString(R.string.detail_pause)
        } else {
            buttonPlay?.setImageResource(if (useWhiteIcons) R.drawable.ic_play_symbol_white else R.drawable.ic_play_symbol)
            buttonPlay?.contentDescription = getString(R.string.detail_play)
        }

        try {
            if (!station.image.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(context)
                    .load(station.image)
                    .error(R.drawable.ic_default_station_image_64dp)
                    .into(stationIcon!!)
            } else {
                com.bumptech.glide.Glide.with(context)
                    .load(R.drawable.ic_default_station_image_64dp)
                    .into(stationIcon!!)
            }
        } catch (e: Exception) {
            com.bumptech.glide.Glide.with(context)
                .load(R.drawable.ic_default_station_image_64dp)
                .into(stationIcon!!)
        }
    }

    fun updateMetadata(metadata: String?) {
        currentMetadata = metadata
        if (!metadata.isNullOrEmpty()) {
            playerStationMetadata?.text = metadata
            playerStationMetadata?.isSelected = true
            textViewMetadata?.text = metadata
            textViewMetadata?.isSelected = true
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val displayMode = PreferencesHelper.loadFullScreenDisplayMode()
        if (displayMode == Keys.FULL_SCREEN_MODE_AUTO) {
            listener?.onOrientationChanged()
        }
    }
}