package com.tapbi.spark.yokey.ui.main.customize.control_createtheme

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentSoundControlBinding
import com.android.inputmethod.latin.AudioAndHapticFeedbackManager
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.Sound
import com.tapbi.spark.yokey.ui.adapter.SoundAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.main.customize.CreateThemeViewModel
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.Constant.SOUND_KEY_KILLAPP
import org.greenrobot.eventbus.EventBus

class SoundControlFragment : BaseBindingFragment<FragmentSoundControlBinding, CreateThemeViewModel>() {

    private lateinit var listSound : ArrayList<Sound>
    private lateinit var soundAdapter: SoundAdapter
    val feedbackManager = AudioAndHapticFeedbackManager.getInstance()
    override fun getViewModel(): Class<CreateThemeViewModel> {
        return CreateThemeViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_sound_control

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
    }

    override fun onPermissionGranted() {
    }

    companion object {
        fun newInstance(): SoundControlFragment {
            val args = Bundle()
            val fragment = SoundControlFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSoundControlBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRclSound()
        if(savedInstanceState!=null){
            if(soundAdapter!=null){
                App.instance.soundKey = savedInstanceState.getString(SOUND_KEY_KILLAPP)!!
                soundAdapter.changeFocusSound(savedInstanceState.getString(SOUND_KEY_KILLAPP)!!)

            }
        }
    }

    private fun initRclSound() {
        listSound  = ArrayList()
        soundAdapter = SoundAdapter(listSound, requireContext(), Constant.AUDIO_DEFAULT,object : SoundAdapter.ListenerChangeSound{
            override fun changeSound(sound: String) {
                val bundle = Bundle()
                bundle.putString(Constant.DATA_CHANGE_SOUND_CUSTOMZIE,sound)
                App.instance.soundKey = sound
                EventBus.getDefault().post(MessageEvent(Constant.ACTION_CHANGE_SOUND_CUSTOMZIE, bundle))
                val handler = Handler()
                handler.postDelayed(object : Runnable{
                    override fun run() {
                        Log.d("duongcv", "changeSound1: "+ Thread.currentThread().name)
                        feedbackManager.performAudioFeedback(0)
                    }
                },100)


            }
        })
        val gridLayoutManager = GridLayoutManager(requireContext(), 5)
        binding.rclKeySound.layoutManager = gridLayoutManager
        binding.rclKeySound.adapter = soundAdapter
        viewModel.liveDataListSound.observe(viewLifecycleOwner,
            { listSound -> soundAdapter.changeListSound(listSound!!) })
        viewModel.loadListSound()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SOUND_KEY_KILLAPP, App.instance.soundKey)
        super.onSaveInstanceState(outState)
    }
}