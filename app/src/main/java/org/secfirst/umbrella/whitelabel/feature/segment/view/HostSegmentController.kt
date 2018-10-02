package org.secfirst.umbrella.whitelabel.feature.segment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import kotlinx.android.synthetic.main.host_segment_view.*
import org.secfirst.umbrella.whitelabel.R
import org.secfirst.umbrella.whitelabel.UmbrellaApplication
import org.secfirst.umbrella.whitelabel.data.database.content.Module
import org.secfirst.umbrella.whitelabel.data.database.difficulty.Difficulty
import org.secfirst.umbrella.whitelabel.data.database.segment.HostSegmentTabControl
import org.secfirst.umbrella.whitelabel.data.database.segment.Segment
import org.secfirst.umbrella.whitelabel.data.database.segment.toController
import org.secfirst.umbrella.whitelabel.data.database.segment.toControllers
import org.secfirst.umbrella.whitelabel.feature.base.view.BaseController
import org.secfirst.umbrella.whitelabel.feature.segment.DaggerSegmentComponent
import org.secfirst.umbrella.whitelabel.feature.segment.interactor.SegmentBaseInteractor
import org.secfirst.umbrella.whitelabel.feature.segment.presenter.SegmentBasePresenter
import org.secfirst.umbrella.whitelabel.feature.segment.view.adapter.DifficultSpinnerAdapter
import org.secfirst.umbrella.whitelabel.feature.segment.view.adapter.HostSegmentAdapter
import javax.inject.Inject

class HostSegmentController(bundle: Bundle) : BaseController(bundle), SegmentView, HostSegmentTabControl {

    @Inject
    internal lateinit var presenter: SegmentBasePresenter<SegmentView, SegmentBaseInteractor>
    private val selectDifficulty by lazy { args.getParcelable(EXTRA_SEGMENT_BY_DIFFICULTY) as Difficulty? }
    private val selectModule by lazy { args.getParcelable(EXTRA_SEGMENT_BY_MODULE) as Module? }
    private lateinit var hostAdapter: HostSegmentAdapter

    constructor(difficulty: Difficulty) : this(Bundle().apply {
        putParcelable(EXTRA_SEGMENT_BY_DIFFICULTY, difficulty)
    })

    constructor(module: Module) : this(Bundle().apply {
        putParcelable(EXTRA_SEGMENT_BY_MODULE, module)
    })

    override fun onInject() {
        DaggerSegmentComponent.builder()
                .application(UmbrellaApplication.instance)
                .build()
                .inject(this)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.onAttach(this)

        if (selectModule != null) {
            selectModule?.let { presenter.submitLoadSegments(it) }
        } else {
            selectDifficulty?.let { presenter.submitLoadSegments(it) }
        }
        setUpToolbar()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.host_segment_view, container, false)
    }

    private fun initSpinner(segments: MutableList<Segment>) {
        val difficultAdapter = DifficultSpinnerAdapter(context, segments)
        hostSegmentSpinner?.let {
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    hostSegmentTab?.getTabAt(0)?.select()
                    refreshView(segments[position])
                }
            }
            it.adapter = difficultAdapter
        }
    }

    override fun showSegments(segments: MutableList<Segment>) {
        initSpinner(segments)
    }

    private fun refreshView(segment: Segment) {
        val controllers = mutableListOf<BaseController>()
        val selectSegment = segment.toController()
        hostAdapter = HostSegmentAdapter(this, controllers)
        controllers.add(selectSegment)
        controllers.addAll(segment.markdowns.toControllers())
        hostSegmentPager?.let {
            it.adapter = hostAdapter
            it.offscreenPageLimit = 500
            hostSegmentTab?.setupWithViewPager(it)
        }
    }

    override fun onTabHostManager(position: Int) {
        hostSegmentTab?.getTabAt(position)?.select()
    }

    companion object {
        const val EXTRA_SEGMENT_BY_MODULE = "selected_module"
        const val EXTRA_SEGMENT_BY_DIFFICULTY = "selected_difficulty"
    }

    private fun setUpToolbar() {
        hostSegmentToolbar?.let {
            mainActivity.setSupportActionBar(it)
            mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            mainActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }
}