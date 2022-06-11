package com.utf8coding.healthcare.view_models

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.utf8coding.healthcare.MyApplication
import com.utf8coding.healthcare.data.ArticleData
import com.utf8coding.healthcare.networkRelated.NetworkUtils

class InfoFragmentViewModel: ViewModel() {
    var articleDataList: MutableLiveData<ArrayList<ArticleData>> = MutableLiveData(ArrayList())
    fun getArticleList(): MutableLiveData<ArrayList<ArticleData>>{
        NetworkUtils.getSuggestedArticle(getUserId(), object: NetworkUtils.ArticleListener{
            override fun onSuccess(articleList: ArrayList<ArticleData>) {
                articleDataList.value = articleList
            }
            override fun onFail() {
            }
        })
        return articleDataList
    }

    fun getUserId(): Int{
        return MyApplication.context.getSharedPreferences("userData", Context.MODE_PRIVATE).getInt("userId", -1)
    }
}