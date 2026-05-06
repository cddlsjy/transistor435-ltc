/*
 * PlayerFullFragment.kt
 * Implements the full screen player
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-25 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
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

class PlayerFullFragment : Fragment() {

    private val TAG: String = PlayerFullFragment::class.java.simpleName
    private var listener: PlayerFullFragmentListener? = null
    private var initialStation: Station? = null
    private var initialIsPlaying: Boolean = false

    private lateinit var textViewGeneralInfo: TextView
    private lateinit var stationIcon: ImageView
    private lateinit var playerStationName: TextView
    private lateinit var playerStationMetadata: TextView
    private lateinit var textViewTimePlayed: TextView
    private lateinit var textViewNetworkUsageInfo: TextView
    private lateinit var textViewTimeCached: TextView
    private lateinit var buttonPrev: ImageButton
    private lateinit var buttonPlay: ImageButton
    private lateinit var buttonNext: ImageButton
    private lateinit var buttonFullscreenExit: ImageButton

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
        val rootView = inflater.inflate(R.layout.fragment_player_full, container, false)

        textViewGeneralInfo = rootView.findViewById(R.id.textViewGeneralInfo)
        stationIcon = rootView.findViewById(R.id.stationIcon)
        playerStationName = rootView.findViewById(R.id.playerStationName)
        playerStationMetadata = rootView.findViewById(R.id.playerStationMetadata)
        textViewTimePlayed = rootView.findViewById(R.id.textViewTimePlayed)
        textViewNetworkUsageInfo = rootView.findViewById(R.id.textViewNetworkUsageInfo)
        textViewTimeCached = rootView.findViewById(R.id.textViewTimeCached)
        buttonPrev = rootView.findViewById(R.id.buttonPrev)
        buttonPlay = rootView.findViewById(R.id.buttonPlay)
        buttonNext = rootView.findViewById(R.id.buttonNext)
        buttonFullscreenExit = rootView.findViewById(R.id.buttonFullscreenExit)

        buttonPrev.setOnClickListener { listener?.onPreviousButtonTapped() }
        buttonPlay.setOnClickListener { listener?.onPlayButtonTapped() }
        buttonNext.setOnClickListener { listener?.onNextButtonTapped() }
        buttonFullscreenExit.setOnClickListener { listener?.onExitFullscreen() }

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
        playerStationName.text = station.name
        textViewGeneralInfo.text = station.name

        if (isPlaying) {
            buttonPlay.setImageResource(R.drawable.ic_stop_circle)
            buttonPlay.contentDescription = getString(R.string.detail_stop)
        } else {
            buttonPlay.setImageResource(R.drawable.ic_play_circle)
            buttonPlay.contentDescription = getString(R.string.detail_play)
        }

        try {
            if (!station.image.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(context)
                    .load(station.image)
                    .error(R.drawable.ic_default_station_image_64dp)
                    .into(stationIcon)
            } else {
                com.bumptech.glide.Glide.with(context)
                    .load(R.drawable.ic_default_station_image_64dp)
                    .into(stationIcon)
            }
        } catch (e: Exception) {
            com.bumptech.glide.Glide.with(context)
                .load(R.drawable.ic_default_station_image_64dp)
                .into(stationIcon)
        }
    }

    fun updateMetadata(metadata: String?) {
        if (!metadata.isNullOrEmpty()) {
            playerStationMetadata.text = metadata
            playerStationMetadata.isSelected = true
        }
    }

    fun updateTimePlayed(time: String) {
        textViewTimePlayed.text = time
    }

    fun updateNetworkUsage(info: String) {
        textViewNetworkUsageInfo.text = info
    }

    fun updateTimeCached(info: String) {
        textViewTimeCached.text = info
    }

    fun setGeneralInfo(info: String) {
        textViewGeneralInfo.text = info
    }
}