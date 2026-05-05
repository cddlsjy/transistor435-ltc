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
import com.google.android.material.card.MaterialCardView
import org.y20k.transistor.core.Station


/*
 * PlayerFullFragment class
 */
class PlayerFullFragment : Fragment() {

    /* Define log tag */
    private val TAG: String = PlayerFullFragment::class.java.simpleName

    /* Main class variables */
    private var listener: PlayerFullFragmentListener? = null
    private var initialStation: Station? = null
    private var initialIsPlaying: Boolean = false

    /* Views */
    private lateinit var playerCardView: MaterialCardView
    private lateinit var stationImageView: ImageView
    private lateinit var stationNameView: TextView
    private lateinit var metadataView: TextView
    private lateinit var playButtonView: ImageButton
    private lateinit var previousButtonView: ImageButton
    private lateinit var nextButtonView: ImageButton
    private lateinit var exitButtonView: ImageButton


    /* Listener interface */
    interface PlayerFullFragmentListener {
        fun onPlayButtonTapped()
        fun onPreviousButtonTapped()
        fun onNextButtonTapped()
        fun onExitFullscreen()
    }


    /* Set initial data before view is created */
    fun setInitialData(station: Station, isPlaying: Boolean) {
        initialStation = station
        initialIsPlaying = isPlaying
    }


    /* Overrides onCreateView from Fragment */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_player_full, container, false)

        playerCardView = rootView.findViewById(R.id.player_card)
        stationImageView = rootView.findViewById(R.id.station_icon)
        stationNameView = rootView.findViewById(R.id.player_station_name)
        metadataView = rootView.findViewById(R.id.player_station_metadata)
        playButtonView = rootView.findViewById(R.id.player_play_button)
        previousButtonView = rootView.findViewById(R.id.player_previous_button)
        nextButtonView = rootView.findViewById(R.id.player_next_button)
        exitButtonView = rootView.findViewById(R.id.button_fullscreen_exit)

        playButtonView.setOnClickListener { listener?.onPlayButtonTapped() }
        previousButtonView.setOnClickListener { listener?.onPreviousButtonTapped() }
        nextButtonView.setOnClickListener { listener?.onNextButtonTapped() }
        exitButtonView.setOnClickListener { listener?.onExitFullscreen() }

        rootView.isFocusableInTouchMode = true
        rootView.requestFocus()
        rootView.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        listener?.onPreviousButtonTapped()
                        return@setOnKeyListener true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        listener?.onNextButtonTapped()
                        return@setOnKeyListener true
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        listener?.onPlayButtonTapped()
                        return@setOnKeyListener true
                    }
                    KeyEvent.KEYCODE_BACK -> {
                        listener?.onExitFullscreen()
                        return@setOnKeyListener true
                    }
                }
            }
            false
        }

        // Update views with initial data if available
        initialStation?.let { station ->
            updatePlayerViews(requireContext(), station, initialIsPlaying)
        }

        return rootView
    }


    /* Overrides onAttach from Fragment */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PlayerFullFragmentListener) {
            listener = context
        }
    }


    /* Overrides onDetach from Fragment */
    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    /* Updates player views with current station */
    fun updatePlayerViews(context: Context, station: Station, isPlaying: Boolean) {
        stationNameView.text = station.name

        if (!isPlaying) {
            metadataView.text = station.name
        }

        if (isPlaying) {
            playButtonView.setImageResource(R.drawable.ic_player_stop_symbol_48dp)
        } else {
            playButtonView.setImageResource(R.drawable.ic_player_play_symbol_48dp)
        }

        try {
            if (!station.image.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(context)
                    .load(station.image)
                    .error(R.drawable.ic_default_station_image_64dp)
                    .into(stationImageView)
            } else {
                com.bumptech.glide.Glide.with(context)
                    .load(R.drawable.ic_default_station_image_64dp)
                    .into(stationImageView)
            }
            if (station.imageColor != -1) {
                stationImageView.setBackgroundColor(station.imageColor)
            }
        } catch (e: Exception) {
            com.bumptech.glide.Glide.with(context)
                .load(R.drawable.ic_default_station_image_64dp)
                .into(stationImageView)
        }
    }


    /* Updates metadata views */
    fun updateMetadata(metadata: String?) {
        if (!metadata.isNullOrEmpty()) {
            metadataView.text = metadata
            metadataView.isSelected = true
        }
    }

}