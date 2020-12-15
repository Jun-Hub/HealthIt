package io.jun.healthit.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.ViewModelProvider
import io.jun.healthit.R
import io.jun.healthit.databinding.FragmentTimerBinding
import io.jun.healthit.service.TimerService
import io.jun.healthit.viewmodel.PrefViewModel
import io.jun.healthit.viewmodel.TimerViewModel
import kotlinx.android.synthetic.main.fragment_timer.*
import kotlinx.android.synthetic.main.fragment_timer.adView

class TimerFragment : BaseFragment() {

    private lateinit var prefViewModel : PrefViewModel
    //TimerService로부터 countDown 현황을 실시간으로 보여줄 LiveData 관찰
    private lateinit var timerViewModel: TimerViewModel

    private var viewBinding: FragmentTimerBinding? = null
    private val binding get() = viewBinding!!

    private var isRunning = false
    private var isReplay = false
    private var mBound: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefViewModel = ViewModelProvider(this).get(PrefViewModel::class.java)
        timerViewModel = ViewModelProvider(this).get(TimerViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            initNumberPicker(it)
            setListener(it)
        }

        timerViewModel.run {
            getLeftTime().observe(viewLifecycleOwner, { leftTime ->
                binding.textViewCountdown.text = leftTime
            })

            getProgress().observe(viewLifecycleOwner, { progress ->
                binding.progressCountdown.beerProgress = progress
            })

            getProgressMax().observe(viewLifecycleOwner, { max ->
                binding.progressCountdown.max = max
            })

            getTimerState().observe(viewLifecycleOwner, { state ->
                updateButtons(state)

                when(state) {
                    TimerService.Companion.TimerState.Running -> {
                        isReplay = false
                        isRunning = true
                    }
                    TimerService.Companion.TimerState.Paused -> {
                        isReplay = true
                        isRunning = false
                    }
                    else -> {
                        isReplay = false
                        isRunning = false
                        binding.progressCountdown.apply {
                            max = 0
                            beerProgress = 0
                        }
                    }
                }
            })
        }
    }

    override fun checkProVersion(isProVersion: Boolean) {
        super.checkProVersion(isProVersion)
        if(isProVersion) {
            adView.visibility = View.GONE
            return
        }

        loadBannerAd(adView)
    }

    override fun onStop() {
        super.onStop()
        mBound = false
        //마지막으로 설정했던 시간을 저장
        context?.let { ctx ->
            prefViewModel.setPreviousTimerSet(
                binding.numberPickerMin.value,
                binding.numberPickerSec.value,
                ctx
            )

            if (isRunning && prefViewModel.getFloatingSettings(ctx)) {
                Intent(ctx, TimerService::class.java).let {
                    it.action = "FLOATING"
                    startForegroundService(ctx, it)
                }
            }
        }
    }

    private fun initNumberPicker(context: Context) {
        binding.apply {
            numberPickerMin.apply {
                minValue = 0
                maxValue = 15
                value = prefViewModel.getPreviousTimerSetMin(context)
            }
            numberPickerSec.apply {
                minValue = 0
                maxValue = 59
                value = prefViewModel.getPreviousTimerSetSec(context)
            }
        }
    }

    private fun updateButtons(state: TimerService.Companion.TimerState) {
        binding.apply {
            when (state) {
                TimerService.Companion.TimerState.Running -> {
                    btnStart.visibility = View.GONE
                    btnPause.visibility = View.VISIBLE
                    btnStop.visibility = View.VISIBLE
                    numberPickerLayout.visibility = View.GONE
                    textViewCountdown.visibility = View.VISIBLE
                }
                TimerService.Companion.TimerState.Stopped -> {
                    btnStart.visibility = View.VISIBLE
                    btnPause.visibility = View.GONE
                    btnStop.visibility = View.GONE
                    numberPickerLayout.visibility = View.VISIBLE
                    textViewCountdown.visibility = View.GONE
                }
                TimerService.Companion.TimerState.Paused -> {
                    btnStart.visibility = View.VISIBLE
                    btnPause.visibility = View.GONE
                    btnStop.visibility = View.VISIBLE
                    numberPickerLayout.visibility = View.GONE
                    textViewCountdown.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setListener(context: Context) {
        binding.apply {
            btnStart.setOnClickListener {
                val setTime = binding.run { numberPickerMin.value * 60 + numberPickerSec.value }

                Intent(context, TimerService::class.java).apply {
                    action = "PLAY"
                    putExtra("setTime", setTime)
                    putExtra("forReplay", isReplay)
                    putExtra("alertSetting", prefViewModel.getAlertSettings(context))
                    putExtra("ringSetting", prefViewModel.getRingSettings(context))
                }.let {
                    startForegroundService(context, it)
                }
            }
            btnPause.setOnClickListener {
                val serviceIntent = Intent(context, TimerService::class.java)
                serviceIntent.action = "PAUSE"
                startForegroundService(context, serviceIntent)
            }
            btnStop.setOnClickListener {
                val serviceIntent = Intent(context, TimerService::class.java)
                serviceIntent.action = "STOP"
                startForegroundService(context, serviceIntent)
            }
        }
    }
}