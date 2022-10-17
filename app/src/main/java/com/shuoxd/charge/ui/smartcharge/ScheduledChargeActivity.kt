package com.shuoxd.charge.ui.smartcharge

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shuoxd.charge.R
import com.shuoxd.charge.base.BaseActivity
import com.shuoxd.charge.base.BaseViewHolder
import com.shuoxd.charge.base.OnItemClickListener
import com.shuoxd.charge.databinding.ActivityScheduledChargingBinding
import com.shuoxd.charge.databinding.ItemScheduledBinding
import com.shuoxd.charge.model.charge.ScheduledModel
import com.shuoxd.charge.ui.smartcharge.viewmodel.SehduleViewModel
import com.shuoxd.charge.view.itemdecoration.DividerItemDecoration
import com.shuoxd.lib.util.gone
import com.shuoxd.lib.util.visible
import com.shuoxd.lib.view.dialog.OnTimeSetListener
import com.shuoxd.lib.view.dialog.TimePickerFragment

class ScheduledChargeActivity : BaseActivity(), View.OnClickListener {
    companion object {
        fun start(context: Context?) {
            context?.startActivity(Intent(context, ScheduledChargeActivity::class.java))
        }

    }

    private lateinit var binding: ActivityScheduledChargingBinding


    private val sehduleViewModel: SehduleViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduledChargingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        setlisteners()
        initData()
        fresh()
    }


    private fun setlisteners() {
        binding.btAdd.setOnClickListener(this)
    }


    private fun initViews() {
        binding.rvTimeList.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL,
                resources.getColor(R.color.nocolor),
                2f
            )
        )
        binding.rvTimeList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvTimeList.adapter = Adapter()


    }


    private fun fresh() {
        //请求充电桩列表
        val user = accountService().user()
        if (user != null) {
            val currentChargeModel = getCurrentChargeModel()
            sehduleViewModel.getScheduledChargingByUserId(
                user.id,
                currentChargeModel?.chargerId,
                "1"
            )
        }
    }


    private fun initData() {
        sehduleViewModel.scheduleLiveData.observe(this) {
            dismissDialog()
            if (it.first) {
                val scList = it.second?.scList
                scList?.let { it1 -> (binding.rvTimeList.adapter as Adapter).refresh(it1) }
            }
        }

    }


    inner class Adapter(var scheduledList: MutableList<ScheduledModel.Period> = mutableListOf()) :
        RecyclerView.Adapter<ScheduledViewHolder>(), OnItemClickListener,
        ScheduledViewHolder.OnItemChildClickListener {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ScheduledViewHolder {
            val holder = ScheduledViewHolder.create(parent, this, this)
            return holder;
        }

        override fun onBindViewHolder(holder: ScheduledViewHolder, position: Int) {
            holder.bindData(scheduledList[position], position)
        }

        override fun getItemCount(): Int {
            return scheduledList.size
        }

        @SuppressLint("NotifyDataSetChanged")
        fun refresh(serviceList: List<ScheduledModel.Period>) {
            this.scheduledList.clear()
            this.scheduledList.addAll(serviceList)
            notifyDataSetChanged()
        }


        @SuppressLint("NotifyDataSetChanged")
        fun addData(period: ScheduledModel.Period) {
            this.scheduledList.add(period)
            notifyDataSetChanged()
        }


        override fun onItemClick(v: View?, position: Int) {

        }


        override fun onItemLongClick(v: View?, position: Int) {

        }


        override fun onItemChildClick(v: View?, position: Int, binding: ItemScheduledBinding) {
            when {
                v === binding.ivDelete -> deleteItem(position)
                v === binding.tvEndTime -> {}
                v === binding.tvStartTime -> {}
                v === binding.tvSetTime -> {
                    TimePickerFragment.show(
                        supportFragmentManager,
                        -1,
                        -1,
                        object : OnTimeSetListener {
                            override fun onTimeSelected(hour: Int, min: Int) {
                            }

                        })

                }


            }
        }


        @SuppressLint("NotifyDataSetChanged")
        fun deleteItem(position: Int) {
            this.scheduledList.removeAt(position)
            notifyDataSetChanged()
        }


    }


    class ScheduledViewHolder(itemView: View, onItemClickListener: OnItemClickListener? = null) :
        BaseViewHolder(itemView, onItemClickListener) {

        lateinit var binding: ItemScheduledBinding

        companion object {
            fun create(
                parent: ViewGroup,
                onItemClickListener: OnItemClickListener?,
                onitemChildClick: OnItemChildClickListener
            ): ScheduledViewHolder {
                val binding = ItemScheduledBinding.inflate(LayoutInflater.from(parent.context))
                val holder = ScheduledViewHolder(binding.root, onItemClickListener)
                holder.binding = binding
                holder.binding.root.setOnLongClickListener(holder)

                holder.binding.ivDelete.setOnClickListener {
                    onitemChildClick.onItemChildClick(it, holder.bindingAdapterPosition, binding)
                }

                holder.binding.tvStartTime.setOnClickListener {
                    onitemChildClick.onItemChildClick(it, holder.bindingAdapterPosition, binding)
                }

                holder.binding.tvEndTime.setOnClickListener {
                    onitemChildClick.onItemChildClick(it, holder.bindingAdapterPosition, binding)
                }


                holder.binding.tvSetTime.setOnClickListener {
                    onitemChildClick.onItemChildClick(it, holder.bindingAdapterPosition, binding)
                }


                return holder
            }
        }


        fun bindData(period: ScheduledModel.Period, position: Int) {
            val startTimeText = period.startTimeText
            if (TextUtils.isEmpty(startTimeText)) {
                binding.llTime.gone()
                binding.tvSetTime.visible()
            } else {
                binding.tvStartTime.text = period.startTimeText
                binding.tvEndTime.text = period.endTimeText
                binding.etCurrent.setText(period.limitNum.toString())
            }


        }


        interface OnItemChildClickListener {

            fun onItemChildClick(v: View?, position: Int, binding: ItemScheduledBinding) {}

        }


    }


    override fun onClick(p0: View?) {
        when {
            p0 === binding.btAdd -> {
                addPeriod()
            }
        }

    }


    private fun addPeriod() {
        val currentChargeModel = getCurrentChargeModel()
        val user = accountService().user()
        val period: ScheduledModel.Period = ScheduledModel.Period(
            0,
            currentChargeModel?.chargerId,
            user?.id?.toInt(),
            "",
            "",
            0
        )

        (binding.rvTimeList.adapter as Adapter).addData(period)

    }


}