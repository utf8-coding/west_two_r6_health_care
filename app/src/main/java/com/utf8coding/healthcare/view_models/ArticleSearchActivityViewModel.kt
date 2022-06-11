package com.utf8coding.healthcare.view_models

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utf8coding.healthcare.MyApplication
import com.utf8coding.healthcare.data.ArticleData
import com.utf8coding.healthcare.networkRelated.NetworkUtils

class ArticleSearchActivityViewModel: ViewModel() {
    val articleList: MutableLiveData<ArrayList<ArticleData>> = MutableLiveData(ArrayList())

    fun getResultMedList(searchText: String) {
        NetworkUtils.searchArticleByKey(searchText, object: NetworkUtils.ArticleListener{
            override fun onSuccess(articleList: java.util.ArrayList<ArticleData>) {
                this@ArticleSearchActivityViewModel.articleList.value = articleList
            }

            override fun onFail() {
                Toast.makeText(MyApplication.context, "网络出错了，请重试", Toast.LENGTH_SHORT).show()
            }

        })
    }
}