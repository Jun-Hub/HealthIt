package io.jun.healthit.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.cleveroad.fanlayoutmanager.FanLayoutManager
import com.cleveroad.fanlayoutmanager.FanLayoutManagerSettings
import com.cleveroad.fanlayoutmanager.callbacks.FanChildDrawingOrderCallback
import io.jun.healthit.R
import io.jun.healthit.adapter.TipListAdapter
import io.jun.healthit.model.data.Tip
import io.jun.healthit.view.BillingActivity
import io.jun.healthit.view.MainActivity
import kotlinx.android.synthetic.main.fragment_routine.*

class RoutineFragment : BaseFragment(), View.OnClickListener {

    private val TAG = "RoutineFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_routine, container, false)

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)

        val fanLayoutManagerSettings = FanLayoutManagerSettings
            .newBuilder(context)
            .withFanRadius(true)
            .withAngleItemBounce(5f)
            .withViewHeightDp(180f)
            .withViewWidthDp(120f)
            .build()

        val fanLayoutManager = FanLayoutManager(requireContext(), fanLayoutManagerSettings)

        val tipAdapter = TipListAdapter(requireContext(), fanLayoutManager)
        tipAdapter.apply {
            add(Tip(getString(R.string.text_common_sense), null, R.drawable.ic_moonk, "common_sense"))
            add(Tip(getString(R.string.text_common_sense_diet), null, R.drawable.ic_hamburger, "common_sense_diet"))
            add(Tip(getString(R.string.text_full_body), "for beginner", R.drawable.ic_0, "full_body"))
            add(Tip(getString(R.string.text_2day_split), "for intermediate", R.drawable.ic_2, "2day_split"))
            add(Tip(getString(R.string.text_3day_split), "for advanced", R.drawable.ic_3, "3day_split"))
            add(Tip(getString(R.string.text_4day_split), "for advanced", R.drawable.ic_4, "4day_split"))
            add(Tip(getString(R.string.text_5day_split), "not recommended", R.drawable.ic_5, "5day_split"))
            add(Tip(getString(R.string.text_strength), "optional", R.drawable.ic_strength, "strength"))
        }

        tipAdapter.setOnItemClickListener(object : TipListAdapter.OnItemClickListener {
            override fun onItemClicked(pos: Int, view: View?) {
                fanLayoutManager.switchItem(recyclerView, pos)
            }
        })

        recyclerView.apply {
            layoutManager = fanLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = tipAdapter
            setChildDrawingOrderCallback(FanChildDrawingOrderCallback(fanLayoutManager))
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated $activity")

        test.setOnClickListener(this)

            if ((activity as MainActivity).billingProcessor.isPurchased(context?.getString(R.string.sku_inapp))) {
                adView.visibility = View.GONE
                Log.d(TAG, "inapp puchased")
            }
            else {
                Log.d(TAG, "inapp not puchased")
                adView.visibility = View.VISIBLE
                loadBannerAd(adView)
            }

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.test -> {
                Log.d(TAG, "test onClicked")
                startActivity(Intent(requireContext(), BillingActivity::class.java))
            }
        }
    }

}