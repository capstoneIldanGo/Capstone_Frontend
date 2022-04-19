package com.ildango.capstone.mywishlist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ildango.capstone.databinding.ActivityWishListBinding


class MyWishListActivity : AppCompatActivity() {

    private var _binding: ActivityWishListBinding?= null
    private val binding get() = _binding!!
    private lateinit var viewModel: MyWishListViewModel

    private val mItems = MutableLiveData<ArrayList<MyWishItem>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWishListBinding.inflate(this.layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MyWishListViewModel::class.java)

        // list view
        binding.recyclerviewWishList.layoutManager = LinearLayoutManager(this)

        val dataObserver:Observer<ArrayList<MyWishItem>> =
            Observer { livedata ->
                mItems.value = livedata
                val mAdapter = MyWishListAdapter(mItems)
                binding.recyclerviewWishList.adapter = mAdapter
            }
        viewModel.liveData.observe(this, dataObserver)

    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}