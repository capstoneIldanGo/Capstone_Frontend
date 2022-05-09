package com.ildango.capstone.mypages.mywishlist

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ildango.capstone.mypages.MyViewModel
import com.ildango.capstone.databinding.ActivityWishListBinding


class MyWishListActivity : AppCompatActivity() {

    private var _binding: ActivityWishListBinding?= null
    private val binding get() = _binding!!
    private lateinit var viewModel: MyWishListViewModel
    private val repository = MyWishListRepository()
    private val viewModelFactory = MyWishListViewModelFactory(repository)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWishListBinding.inflate(this.layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MyWishListViewModel::class.java)

        // list view
        binding.recyclerviewWishList.layoutManager = LinearLayoutManager(this)

        viewModel.getData()
        setObserver()
    }

    private fun setObserver() {
        viewModel.items.observe(this, Observer {
            if(it.isSuccessful) {
                Log.d("Response", "Wish ID:${it.body()?.get(0)!!.myPostId}")
                Log.d("Response", "Wish Title:${it.body()?.get(0)!!.post.title}")
            }
            else {
                Log.d("Response", "ERROR:${it.errorBody().toString()}")
            }
        })
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
