package com.ildango.capstone.data.repository

import com.ildango.capstone.data.model.ProductItem
import com.ildango.capstone.data.model.ProductItemList
import com.ildango.capstone.data.service.RetrofitClient
import retrofit2.Response
import java.lang.Exception
import java.util.stream.Collectors

class ProductRepository {

    private val platforms = listOf("Joongonara", "Bunjang", "Carrot Market")

    suspend fun getAllProduct(
        keyword: String,
        order: String,
        page: Int,
        platform: List<Boolean>,
        tag: List<Boolean>,
        city: String,
        state: String
    ): Result<ProductItemList> {

        val data: Response<ProductItemList>

        return try {
            val areaCity = if(tag[0]) city else null
            var areaState = if(tag[0]) state else null
            if(areaCity==areaState) areaState = null
            val mint = if(tag[1]) true else null
            val platform = getPlatform(platform)

            data = RetrofitClient.productApi.getAllProduct(
                keyword=keyword,
                order=order,
                page=page,
                mint = mint,
                platform = platform,
                city = areaCity,
                state = areaState
            )

            if (data.isSuccessful) {
                data.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Throwable(data.message()))
            } else {
                Result.failure(Throwable(data.message()))
            }
        } catch (e: Exception) {
            Result.failure(Throwable(e.message))
        }
    }

    suspend fun getProductPrice(
        keyword: String,
        order: String,
        sold: Boolean ?= false,
        mint: Boolean ?= null,
        city: String ?= null,
        state: String ?= null
    ): Result<ProductItem> {
        return try {
            val data = RetrofitClient.productApi.getAllProduct(
                keyword = keyword,
                order = order,
                sold = sold,
                page = 0,
                mint = mint,
                city = city,
                state = state
            )

            if (data.isSuccessful) {
                data.body()?.let {
                    Result.success(it.productList[0])
                } ?: Result.failure(Throwable(data.message()))
            } else {
                Result.failure(Throwable(data.message()))
            }
        } catch (e: Exception) {
            Result.failure(Throwable(e.message))
        }
    }

    private fun getPlatform(platform: List<Boolean>): String? {
        val platformArr: ArrayList<String> = ArrayList()
        var platformString: String

        if(platform[0] && platform[1] && platform[2]) {
            return null
        } else if (!(platform[0] || platform[1] || platform[2])) {
            return null
        } else {
            for (i in platforms.indices) {
                if(platform[i]) {
                    platformArr.add(platforms[i])
                }
            }
            platformString = platformArr.stream().collect(Collectors.joining(","))

            return platformString
        }
    }
}