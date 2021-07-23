package io.agora.flat.ui.activity.playback

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import io.agora.flat.databinding.ActivityReplayBinding
import io.agora.flat.ui.activity.play.BaseComponent

@AndroidEntryPoint
class ReplayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReplayBinding
    private var componentSet: MutableSet<BaseComponent> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSystemUI()

        componentSet.add(ReplayWhiteboardComponent(this, binding.whiteboardContainer, binding.videoListContainer))
        componentSet.add(ReplayToolComponent(this, binding.toolsContainer))
        // componentSet.add(ReplayVideoComponent(this, binding.videoListContainer))

        componentSet.forEach { lifecycle.addObserver(it) }
    }

    private fun initSystemUI() {
        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}