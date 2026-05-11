/*
 * PlayerFullFragment.kt
 * Implements the full screen player
 */

package org.y20k.transistor

import android.content.Context
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
    }

    fun setInitialData(station: Station, isPlaying: Boolean) {
        initialStation = station
        initialIsPlaying = isPlaying
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val displayMode = PreferencesHelper.loadFullScreenDisplayMode()
        val layoutId = when (displayMode) {
            Keys.FULL_SCREEN_MODE_PORTRAIT -> R.layout.fragment_player_full_portrait
            Keys.FULL_SCREEN_MODE_LANDSCAPE -> R.layout.fragment_player_full_landscape
            else -> R.layout.fragment_player_full
        }

        val rootView = inflater.inflate(layoutId, container, false)

        applyBackgroundMode(rootView)

        stationIcon = rootView.findViewById(R.id.stationIcon)
        textViewStationInfo = rootView.findViewById(R.id.textViewStationInfo)
        textViewMetadata = rootView.findViewById(R.id.textViewMetadata)
        playerStationName = rootView.findViewById(R.id.playerStationName)
        playerStationMetadata = rootView.findViewById(R.id.playerStationMetadata)
        buttonPrev = rootView.findViewById(R.id.buttonPrev)
        buttonPlay = rootView.findViewById(R.id.buttonPlay)
        buttonNext = rootView.findViewById(R.id.buttonNext)
        buttonFullscreenExit = rootView.findViewById(R.id.buttonFullscreenExit)

        buttonPrev?.setOnClickListener { listener?.onPreviousButtonTapped() }
        buttonPlay?.setOnClickListener { listener?.onPlayButtonTapped() }
        buttonNext?.setOnClickListener { listener?.onNextButtonTapped() }
        buttonFullscreenExit?.setOnClickListener { listener?.onExitFullscreen() }

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

        initialStation?.let { station ->
            updatePlayerViews(requireContext(), station, initialIsPlaying)
        }

        return rootView
    }

    private fun applyBackgroundMode(rootView: View) {
        val backgroundMode = PreferencesHelper.loadFullScreenBackgroundMode()
        when (backgroundMode) {
            Keys.BACKGROUND_MODE_DARK_BLUE -> {
                rootView.setBackgroundColor(resources.getColor(R.color.background_dark_blue))
                updateTextColorsForDarkBackground()
            }
            else -> {
                rootView.setBackgroundResource(R.attr.colorSurface)
            }
        }
    }

    private fun updateTextColorsForDarkBackground() {
        playerStationName?.setTextColor(resources.getColor(R.color.text_on_dark_blue))
        textViewStationInfo?.setTextColor(resources.getColor(R.color.text_on_dark_blue))
        playerStationMetadata?.setTextColor(resources.getColor(R.color.text_secondary_on_dark_blue))
        textViewMetadata?.setTextColor(resources.getColor(R.color.text_secondary_on_dark_blue))
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

    fun updatePlayerViews(context: Context, station: Station, isPlaying: Boolean) {
        playerStationName?.text = station.name
        textViewStationInfo?.text = station.name

        if (isPlaying) {
            buttonPlay?.setImageResource(R.drawable.ic_pause_circle)
            buttonPlay?.contentDescription = getString(R.string.detail_pause)
        } else {
            buttonPlay?.setImageResource(R.drawable.ic_play_circle)
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
        if (!metadata.isNullOrEmpty()) {
            playerStationMetadata?.text = metadata
            playerStationMetadata?.isSelected = true
            textViewMetadata?.text = metadata
            textViewMetadata?.isSelected = true
        }
    }
}