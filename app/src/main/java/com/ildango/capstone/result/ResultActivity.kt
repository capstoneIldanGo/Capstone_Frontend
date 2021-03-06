package com.ildango.capstone.result

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ildango.capstone.data.repository.ChartRepository
import com.ildango.capstone.data.repository.ProductRepository
import com.ildango.capstone.databinding.ActivitySearchResultBinding
import com.ildango.capstone.resultdetail.ResultDetailActivity

const val type1 = "내 주변"
const val type2 = "전국"
const val type3 = "미개봉/S급"

class ResultActivity : AppCompatActivity() {

    private var _binding: ActivitySearchResultBinding? = null
    private val binding get() = _binding!!
    //resultViewModel
    private lateinit var viewModel: ResultViewModel
    private val repository = ProductRepository()
    private val viewModelFactory = ResultViewModelFactory(repository)
    //chartItemViewModel
    private lateinit var viewModel2 : ChartItemViewModel
    private val repository2 = ChartRepository()
    private val viewModelFactory2 = ChartItemViewModelFactory(repository2)

    private var keyword = ""
    private val chartTitles = arrayListOf("이주일", "일주일")
    private val priceListTags = arrayListOf(type1, type2, type3)
    private var chartPriceValues = arrayListOf<Int>()
    val chartOneWeekList = arrayListOf<Int>()
    val chartTwoWeekList = arrayListOf<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ResultViewModel::class.java)
        viewModel2 = ViewModelProvider(this, viewModelFactory2).get(ChartItemViewModel::class.java)

        keyword = intent.getStringExtra("keyword").toString()

        setChartValues()
        setLatestTransPrice()
        setPriceInfoList()
        setSearchView()
        setAlarmDialog()
    }

    private fun setChartValues(){
        viewModel2.getData(keyword)

        viewModel2.items.observe(this){
            for(i in 0..13)
                chartPriceValues.add(viewModel2.getValue(i))
            setChartAndTabs(chartPriceValues)
        }

    }

    private fun setAlarmDialog() {
        binding.btnMakeAlarm.setOnClickListener {
            AlarmDialog(keyword).show(supportFragmentManager, "AlarmDialog")
        }
    }

    private fun setLatestTransPrice() {
        viewModel.getLastSoldPrice(keyword)
        viewModel.lastSoldPrice.observe(this) {
            binding.tvLatestPrice.text = "${it}원"
        }
    }

    private fun setPriceInfoList() {
        getPriceData()
        val adapter = PriceListAdapter(priceListTags)
        binding.listviewPriceList.adapter = adapter
        setPriceObserver(adapter)
        setListListener()
    }

    private fun getPriceData() {
        val pref: SharedPreferences = getSharedPreferences("Information", MODE_PRIVATE)
        val city = pref.getString("cityInfo", "")!!
        var state = pref.getString("stateInfo", "")
        if(city == state) state = null

        viewModel.getLowestAroundMyArea(keyword, city, state)
        viewModel.getLowestOfAll(keyword)
        viewModel.getLowestOfSClass(keyword)
    }

    private fun setPriceObserver(adapter:PriceListAdapter) {
        viewModel.lowestPriceAroundArea.observe(this) {
            adapter.setPrice1(it)
        }
        viewModel.lowestPriceOfAll.observe(this) {
            adapter.setPrice2(it)
        }
        viewModel.lowestPriceOfSClass.observe(this) {
            adapter.setPrice3(it)
        }
    }

    private fun setListListener() {
        binding.listviewPriceList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val nextIntent = Intent(this, ResultDetailActivity::class.java)
                nextIntent.putExtra("keyword", keyword)
                when (position) {
                    0 -> nextIntent.putExtra("type", type1)
                    1 -> nextIntent.putExtra("type", type2)
                    2 -> nextIntent.putExtra("type", type3)
                }
                startActivity(nextIntent)
            }
    }

    private fun setSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                finish()
                val intent = Intent(this@ResultActivity, ResultActivity::class.java)
                keyword = binding.searchView.query.toString()
                intent.putExtra("keyword", keyword)
                startActivity(intent)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                // 텍스트 값 바뀔 때
                return false
            }
        })

        binding.searchView.setQuery(keyword, false)
    }

    private fun setChartAndTabs(chartPriceValues: ArrayList<Int>) {
        chartTwoWeekList.addAll(chartPriceValues)
        for(i in 7..13)
            chartOneWeekList.add(chartPriceValues.get(i))

        binding.vpChart.adapter = ChartPagerAdapter(this)
        for (title in chartTitles) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title))
        }
        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.vpChart.setCurrentItem(tab!!.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        TabLayoutMediator(binding.tabLayout, binding.vpChart) { tab, position ->
            tab.text = chartTitles[position]
        }.attach()
    }

    private inner class ChartPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {

            return when(position) {
                0-> ChartFragment(chartTwoWeekList)
                1-> ChartFragment(chartOneWeekList)
                else-> throw Exception()
            }
        }

        override fun getItemCount(): Int = 2
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}