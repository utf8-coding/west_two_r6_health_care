package com.utf8coding.healthcare.view_models

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utf8coding.healthcare.MyApplication
import com.utf8coding.healthcare.data.ArticleData
import com.utf8coding.healthcare.data.CommentData
import com.utf8coding.healthcare.networkRelated.NetworkUtils

class ArticleReadingActivityViewModel: ViewModel() {
    var isCollected: MutableLiveData<Boolean> = MutableLiveData(false)
    var commentData: MutableLiveData<ArrayList<CommentData>> = MutableLiveData<ArrayList<CommentData>>(ArrayList())

    fun getCommentList(articleData: ArticleData): MutableLiveData<ArrayList<CommentData>>{
        NetworkUtils.getComment(articleData.id, object: NetworkUtils.CommentListListener{
            override fun onSuccess(newCommentList: ArrayList<CommentData>) {
                commentData.value = newCommentList
            }

            override fun onFailure() {
                Toast.makeText(MyApplication.context, "评论获取失败！", Toast.LENGTH_SHORT).show()
            }

        })
        return commentData
    }

    fun collectArticle(articleData: ArticleData){
        NetworkUtils.setCollection(getUserId(), articleData.id)
    }

    fun unCollectArticle(articleData: ArticleData){
        NetworkUtils.deleteCollection(getUserId(), articleData.id)
    }

    fun getIsCollected(articleData: ArticleData): MutableLiveData<Boolean>{
        NetworkUtils.checkCollected(getUserId(), articleData.id, object : NetworkUtils.CollectionCheckListener{
            override fun onSuccess(mIsCollected: Boolean) {
                isCollected.value = mIsCollected
            }
            override fun onFailure() {
            }
        })
        return isCollected
    }

    fun sendComment(content: String ,articleId:Int){
        NetworkUtils.setComment(getUserId(), articleId, content)
    }

    private fun getUserId(): Int{
        return MyApplication.context.getSharedPreferences("userData", Context.MODE_PRIVATE).getInt("userId", -1)
    }
}