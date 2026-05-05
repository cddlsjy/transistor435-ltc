/*
 * PlayerSmallFragment.kt
 * Implements the small player at the bottom of the screen
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import org.y20k.transistor.core.Station
import org.y20k.transistor.helpers.PreferencesHelper


/*
 * PlayerSmallFragment class
 */
class PlayerSmallFragment : Fragment() {

    /* Define log tag */
    private val TAG: String = PlayerSmallFragment::class.java.simpleName

    /* Main class variables */
    private var listener: PlayerSmallFragmentListener? = null

    /* Views */
    private lateinit var playerCardView: MaterialCardView
    private lateinit var stationImageView: ImageView
    private lateinit var stationNameView: TextView
    private lateinit var metadataView: TextView
    private lateinit var playButtonView: ImageButton
    private lateinit var bufferingIndicatorView: View


    /* Listener interface */
    interface PlayerSmallFragmentListener {
        fun onPlayButtonTapped()
        fun onPlayerTapped()
    }


    /* Overrides onCreateView from Fragment */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // inflate layout
        val rootView = inflater.inflate(R.layout.card_player, container, false)

        // get views
        playerCardView = rootView.findViewById(R.id.player_card)
        stationImageView = rootView.findViewById(R.id.station_icon)
        stationNameView = rootView.findViewById(R.id.player_station_name)
        metadataView = rootView.findViewById(R.id.player_station_metadata)
        playButtonView = rootView.findViewById(R.id.player_play_button)
        bufferingIndicatorView = rootView.findViewById(R.id.player_buffering_indicator)

        // hide the extended views by default - we only want the small player
        val playerExtendedViews = rootView.findViewById<View>(R.id.player_extended_views)
        playerExtendedViews.visibility = View.GONE

        // setup listeners
        playerCardView.setOnClickListener { listener?.onPlayerTapped() }
        stationImageView.setOnClickListener { listener?.onPlayerTapped() }
        stationNameView.setOnClickListener { listener?.onPlayerTapped() }
        playButtonView.setOnClickListener { listener?.onPlayButtonTapped() }

        return rootView
    }


    /* Overrides onAttach from Fragment */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PlayerSmallFragmentListener) {
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
        // update station name
        stationNameView.text = station.name

        // set default metadata views, when playback has stopped
        if (!isPlaying) {
            metadataView.text = station.name
        }

        // update play button icon
        if (isPlaying) {
            playButtonView.setImageResource(R.drawable.ic_player_stop_symbol_48dp)
            bufferingIndicatorView.visibility = View.GONE
        } else {
            playButtonView.setImageResource(R.drawable.ic_player_play_symbol_48dp)
        }

        // update cover
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
            // Fallback to default image on any error
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


    /* Toggle buffering indicator */
    fun showBufferingIndicator(buffering: Boolean) {
        bufferingIndicatorView.visibility = if (buffering) View.VISIBLE else View.GONE
    }

}
