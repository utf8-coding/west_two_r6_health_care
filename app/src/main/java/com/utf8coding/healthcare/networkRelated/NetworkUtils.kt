package com.utf8coding.healthcare.networkRelated

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.utf8coding.healthcare.MyApplication
import com.utf8coding.healthcare.data.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object NetworkUtils {

    interface LoginNetListener{
        fun onSuccess()
        fun onFailure()
        fun onWrongUser()
        fun onWrongPassword()
    }
    private fun login(userName: String, passWord: String, listener: LoginNetListener, timeOut: Long){
        getGeneralAppService(timeOut).login(userName, passWord).enqueue(object : Callback<NetWorkResponse<UserData>> {
            override fun onResponse(
                call: Call<NetWorkResponse<UserData>>,
                response: Response<NetWorkResponse<UserData>>
            ) {
                val loginResponse = response.body()
                if (loginResponse != null) {
                    when (loginResponse.code) {
                        200 -> {
                            val cookie = response.headers().get("Set-Cookie")
                            MyApplication.context.getSharedPreferences("userData", Context.MODE_PRIVATE).edit()
                                .putString("userName", userName)
                                .putString("passWord", passWord)
                                .putInt("userId", loginResponse.data.id)
                                .putString("userHeadUri", loginResponse.data.headUri)
                                .apply()
                            MyApplication.context.getSharedPreferences("cookie", Context.MODE_PRIVATE).edit().putString("cookie", cookie).apply()
                            val prefCookie = MyApplication.context.getSharedPreferences("cookie", Context.MODE_PRIVATE).getString("cookie", "")
                            makeILog("login success! cookie:$cookie prefCookie: $prefCookie")
                            listener.onSuccess()
                        }
                        2007 -> {
                            makeILog("wrong user name!")
                            listener.onWrongUser()
                        }
                        2003 -> {
                            makeILog("wrong pass!")
                            listener.onWrongPassword()
                        }
                        else -> {
                            makeILog("login other code")
                        }
                    }
                } else {
                    makeILog("empty response while logging in?")
                    listener.onFailure()
                }
            }
            override fun onFailure(call: Call<NetWorkResponse<UserData>>, t: Throwable) {
                makeWLog("network failure! $t")
                listener.onFailure()
            }
        })
    }
    fun login(userName: String, passWord: String, listener: LoginNetListener){
        getGeneralAppService().login(userName, passWord).enqueue(object : Callback<NetWorkResponse<UserData>> {
            override fun onResponse(
                call: Call<NetWorkResponse<UserData>>,
                response: Response<NetWorkResponse<UserData>>
            ) {
                val loginResponse = response.body()
                if (loginResponse != null) {
                    when (loginResponse.code) {
                        200 -> {
                            val cookie = response.headers().get("Set-Cookie")
                            MyApplication.context.getSharedPreferences("userData", Context.MODE_PRIVATE).edit()
                                .putString("userName", userName)
                                .putString("passWord", passWord)
                                .putInt("userId", loginResponse.data.id)
                                .putString("userHeadUri", loginResponse.data.headUri)
                                .apply()
                            MyApplication.context.getSharedPreferences("cookie", Context.MODE_PRIVATE).edit().putString("cookie", cookie).apply()
                            val prefCookie = MyApplication.context.getSharedPreferences("cookie", Context.MODE_PRIVATE).getString("cookie", "")
                            makeILog("login success! cookie:$cookie prefCookie: $prefCookie")
                            listener.onSuccess()
                        }
                        2007 -> {
                            makeILog("wrong user name!")
                            listener.onWrongUser()
                        }
                        2003 -> {
                            makeILog("wrong pass!")
                            listener.onWrongPassword()
                        }
                        else -> {
                            makeILog("login other code")
                        }
                    }
                } else {
                    makeILog("empty response while logging in?")
                    listener.onFailure()
                }
            }
            override fun onFailure(call: Call<NetWorkResponse<UserData>>, t: Throwable) {
                makeWLog("network failure! $t")
                listener.onFailure()
            }
        })
    }


    interface SuccessFailListener {
        fun onSuccess()
        fun onFail()
    }

    //生活指数
    interface LifeIndexNetListener{
        fun onSuccess(mLifeIndexData: LifeIndexData)
        fun onFail()
    }
    fun getLifeIndex(listener: LifeIndexNetListener){
        val retrofit = Retrofit.Builder()
            .baseUrl("http://a30163f799.51vip.biz/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(generateClient())
            .build()
        val appService = retrofit.create(NetworkService::class.java)
        appService.getLifeIndex().enqueue(object : Callback<LifeIndexData> {
            override fun onResponse(call: Call<LifeIndexData>,
                                    response: Response<LifeIndexData>
            ) {
                val lifeIndexData = response.body()
                if (lifeIndexData != null){
                    makeILog("response success, login response: $lifeIndexData")
                    listener.onSuccess(lifeIndexData)
                } else {
                    makeILog("empty response! get epidemic response: $lifeIndexData")
                }
            }
            override fun onFailure(call: Call<LifeIndexData>, t: Throwable) {
                listener.onFail()
                t.printStackTrace()
                makeWLog("connection failed!!")
            }
        })
    }

    //疫情信息
    interface EpidemicNetListener{
        fun onSuccess(mEpidemicData: EpidemicData)
        fun onFail()
    }
    fun getEpidemicData(listener: EpidemicNetListener){
        val retrofit = Retrofit.Builder()
            .baseUrl("http://a30163f799.51vip.biz/")
            .client(generateClient())
            .addConverterFactory(GsonConverterFactory.create())
            .client(generateClient())
            .build()
        val appService = retrofit.create(NetworkService::class.java)
        appService.getEpidemicData().enqueue(object : Callback<EpidemicData> {
            override fun onResponse(call: Call<EpidemicData>,
                                    response: Response<EpidemicData>
            ) {
                val epidemicData = response.body()
                if (epidemicData != null){
                    makeILog("total success, epidemicData: $epidemicData")
                    listener.onSuccess(epidemicData)
                } else {
                    makeILog("empty response! epidemicData $epidemicData")
                }
            }
            override fun onFailure(call: Call<EpidemicData>, t: Throwable) {
                listener.onFail()
                t.printStackTrace()
                makeWLog("connection failed!!")
            }
        })
    }

    //获取文章的一系列
    interface GetArticleByIdListener{
        fun onSuccess(articleData: ArticleData)
        fun onFail()
    }
    fun getArticleById(essayId: Int, listener: GetArticleByIdListener){
        if (essayId.toString() != ""){
            val retrofit = Retrofit.Builder()
                .baseUrl("http://123.57.213.188:8000/")
                .client(generateClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val appService = retrofit.create(NetworkService::class.java)
            appService.getArticleById(essayId).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.Essay>> {
                override fun onResponse(call: Call<NetWorkResponse<NetWorkResponse.Essay>>,
                                        response: Response<NetWorkResponse<NetWorkResponse.Essay>>
                ) {
                    val articleData = response.body()?.data
                    if (articleData == null){
                        makeWLog("null article body?")
                    } else {
                        listener.onSuccess(articleData.essay)
                        makeILog("get articleData success")
                    }
                }

                override fun onFailure(
                    call: Call<NetWorkResponse<NetWorkResponse.Essay>>,
                    t: Throwable
                ) {
                    listener.onFail()
                    t.printStackTrace()
                    makeWLog("article list getting failed!!")
                }
            })
        } else {
            makeWLog("id is empty or null when get articles")
        }
    }
    interface ArticleListener{
        fun onSuccess(articleList: ArrayList<ArticleData>)
        fun onFail()
    }
    fun getSuggestedArticle(userId: Int, listener: ArticleListener){
       getGeneralAppService().getSuggestedArticle(userId).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.EssayList>> {
            override fun onResponse(call: Call<NetWorkResponse<NetWorkResponse.EssayList>>,
                                    response: Response<NetWorkResponse<NetWorkResponse.EssayList>>
            ) {
                val articleDataList = response.body()?.data
                if (articleDataList == null) {
                    makeWLog("null article list body?")
                } else {
                    listener.onSuccess(articleDataList.essayList)
                    makeILog("get articleData list success ${articleDataList.essayList.size}")
                }
            }

            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.EssayList>>,
                t: Throwable
            ) {
                listener.onFail()
                t.printStackTrace()
                makeWLog("suggestion article list getting failed!!")
            }
        })
    }
    fun searchArticleByKey(searchKey: String, listener: ArticleListener){
       getGeneralAppService().searchArticle(searchKey).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.EssayList>>{
            override fun onResponse(
                call: Call<NetWorkResponse<NetWorkResponse.EssayList>>,
                response: Response<NetWorkResponse<NetWorkResponse.EssayList>>
            ) {
                val body = response.body()
                if (body != null){
                    val articleList = body.data.essayList
                    listener.onSuccess(articleList)
                    makeILog("getSearched article success: $articleList")
                } else {
                    makeWLog("search article by key empty response body!")
                }
            }

            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.EssayList>>,
                t: Throwable
            ) {
                makeWLog("search article net failed!")
            }

        })
    }

    //收藏管理
    interface CollectionCheckListener{
        fun onSuccess(isCollected: Boolean)
        fun onFailure()
    }
    fun getCollection(userId: Int, listener: ArticleListener){
        fun fromCollectionListGetArticleList(collectionList: ArrayList<NetWorkResponse.Collection>, listener: ArticleListener){
            val list = ArrayList<ArticleData>()
            for (collection in collectionList){
                getArticleById(collection.articleId, object: GetArticleByIdListener{
                    override fun onSuccess(articleData: ArticleData) {
                        list.add(articleData)
                        if (list.size == collectionList.size) {
                            listener.onSuccess(list)
                        }
                    }
                    override fun onFail() {
                    }
                })
            }
        }

       getGeneralAppService().getCollection(userId).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.CollectionList>>{
            override fun onResponse(
                call: Call<NetWorkResponse<NetWorkResponse.CollectionList>>,
                response: Response<NetWorkResponse<NetWorkResponse.CollectionList>>
            ) {
                val body = response.body()
                val collectionList = body?.data?.list ?: ArrayList()
                fromCollectionListGetArticleList(collectionList, object: ArticleListener{
                    override fun onSuccess(articleList: ArrayList<ArticleData>) {
                        listener.onSuccess(articleList)
                    }
                    override fun onFail() {
                    }
                })
                makeILog("get collection success: $collectionList")
            }

            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.CollectionList>>,
                t: Throwable
            ) {
                listener.onFail()
            }

        })
    }
    //给后端减轻一点负担，进行一个类内部的回调了属于是（会导致列表无序，得加锁才能解决，但是速度会变慢。）
    fun checkCollected(userId: Int, articleId: Int, listener: CollectionCheckListener){
       getGeneralAppService().checkCollection(userId, articleId).enqueue(object : Callback<NetWorkResponse<Any>>{
            override fun onResponse(
                call: Call<NetWorkResponse<Any>>,
                response: Response<NetWorkResponse<Any>>
            ) {
                val body = response.body()
                if (body != null){
                    listener.onSuccess(body.message.toBoolean())
                    makeILog("check is collected success: ${body.data}")
                } else {
                    makeWLog("check isCollected null body!")
                }
            }
            override fun onFailure(call: Call<NetWorkResponse<Any>>, t: Throwable) {
                makeWLog("check isCollected on Failure.")
            }
        })
    }
    fun setCollection(userId: Int, articleId: Int){
       getGeneralAppService().setCollection(userId, articleId).enqueue(object : Callback<NetWorkResponse<Any>>{
            override fun onResponse(
                call: Call<NetWorkResponse<Any>>,
                response: Response<NetWorkResponse<Any>>
            ) {
                makeILog("setCollection, success")
            }

            override fun onFailure(call: Call<NetWorkResponse<Any>>, t: Throwable) {
                makeILog("setCollection, failed")
            }

        })
    }
    fun deleteCollection(userId: Int, articleId: Int){
       getGeneralAppService().deleteCollection(userId, articleId).enqueue(object : Callback<NetWorkResponse<Any>>{
            override fun onResponse(
                call: Call<NetWorkResponse<Any>>,
                response: Response<NetWorkResponse<Any>>
            ) {
                makeILog("deleteCollection, success")
            }

            override fun onFailure(call: Call<NetWorkResponse<Any>>, t: Throwable) {
                makeILog("deleteCollection, failed")
            }

        })
    }

    //搜索药物
    interface MedListListener{
        fun onSuccess(newMedList: ArrayList<MedData>)
        fun onFail()
    }
    fun searchMedByName(medName: String, listener: MedListListener){
       getGeneralAppService().searchDrugByName(medName).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.MedDataList>>{
            override fun onResponse(call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                                    response: Response<NetWorkResponse<NetWorkResponse.MedDataList>>
            ) {
                val medDataList = response.body()?.data
                if (medDataList == null){
                    makeWLog("null medData list body?")
                } else {
                    listener.onSuccess(medDataList.medList)
                    makeILog("get medData list success: ${medDataList.medList}")
                }
            }

            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                t: Throwable
            ) {
                listener.onFail()
                t.printStackTrace()
                makeWLog("suggestion medData list getting failed!!")
            }

        })
    }
    fun searchMedByType(medType: String, listener: MedListListener){
       getGeneralAppService().searchDrugByType(medType).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.MedDataList>>{
            override fun onResponse(call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                                    response: Response<NetWorkResponse<NetWorkResponse.MedDataList>>
            ) {
                val medDataList = response.body()?.data
                if (medDataList == null){
                    makeWLog("null medData list body?")
                } else {
                    listener.onSuccess(medDataList.medList)
                    makeILog("get medData list success: ${medDataList.medList}")
                }
            }

            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                t: Throwable
            ) {
                listener.onFail()
                t.printStackTrace()
                makeWLog("suggestion medData list getting failed!!")
            }

        })
    }
    fun searchMedByManufacturer(medManufacturer: String, listener: MedListListener){
       getGeneralAppService().searchDrugByMedManufacturer(medManufacturer).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.MedDataList>>{
            override fun onResponse(call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                                    response: Response<NetWorkResponse<NetWorkResponse.MedDataList>>
            ) {
                val medDataList = response.body()?.data
                if (medDataList == null){
                    makeWLog("null medData list body?")
                } else {
                    listener.onSuccess(medDataList.medList)
                    makeILog("get medData list success: ${medDataList.medList}")
                }
            }

            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                t: Throwable
            ) {
                listener.onFail()
                t.printStackTrace()
                makeWLog("suggestion medData list getting failed!!")
            }

        })
    }

    //文章的评论之获取
    interface CommentListListener{
        fun onSuccess(newCommentList: ArrayList<CommentData>)
        fun onFailure()
    }
    fun getComment(articleId: Int, listener: CommentListListener){
        getGeneralAppService().getComments(articleId).enqueue(object: Callback<NetWorkResponse<NetWorkResponse.CommentList>>{
            override fun onResponse(
                call: Call<NetWorkResponse<NetWorkResponse.CommentList>>,
                response: Response<NetWorkResponse<NetWorkResponse.CommentList>>
            ) {
                val mResponse = response
                if (mResponse.body() != null){
                    val commentList = mResponse.body()!!.data.commentList
                    listener.onSuccess(commentList)
                } else {
                    makeWLog("get comment list null body?")
                }
            }

            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.CommentList>>,
                t: Throwable
            ) {
                listener.onFailure()
                makeWLog("get comment list on net failure!")
            }
        })
    }
    fun setComment(userId: Int, articleId: Int, comment: String){
        getGeneralAppService().setComment(userId, articleId, comment).enqueue(object : Callback<NetWorkResponse<ResponseBody>>{
            override fun onResponse(
                call: Call<NetWorkResponse<ResponseBody>>,
                response: Response<NetWorkResponse<ResponseBody>>
            ) {
                makeILog("setComment success!")
            }

            override fun onFailure(call: Call<NetWorkResponse<ResponseBody>>, t: Throwable) {
                makeWLog("setComment fail!")
            }

        })
    }


    //new okhttp client that able to hold a token:
    private fun generateClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                var cookie = MyApplication.context.getSharedPreferences("cookie", Context.MODE_PRIVATE).getString("cookie", "")
                if (cookie == null){
                    cookie = ""
                    Log.e("NetworkUtils:", "null token!!")
                }
                val request: Request = chain.request()
                    .newBuilder()
                    .addHeader("Cookie", cookie)
                    .build()
                chain.proceed(request)
            }.build()
    }
    private fun generateClient(timeOut: Long): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(timeOut, TimeUnit.MILLISECONDS)
            .readTimeout(timeOut, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                var cookie = MyApplication.context.getSharedPreferences("cookie", Context.MODE_PRIVATE).getString("cookie", "")
                if (cookie == null){
                    cookie = ""
                    Log.e("NetworkUtils:", "null token!!")
                }
                val request: Request = chain.request()
                    .newBuilder()
                    .addHeader("Cookie", cookie)
                    .build()
                chain.proceed(request)
            }.build()
    }

    private fun getGeneralAppService(): NetworkService {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://123.57.213.188:8000/")
            .client(generateClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(NetworkService::class.java)
    }
    private fun getGeneralAppService(timeOut: Long): NetworkService {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://123.57.213.188:8000/")
            .client(generateClient(timeOut))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(NetworkService::class.java)
    }


    private fun onCookieFail(){

    }

    //check netWork:
    fun isNetworkConnected(listener: SuccessFailListener) {
        login("A", "123123", object: LoginNetListener{
            override fun onSuccess() {
                listener.onSuccess()
            }
            override fun onFailure() {
                listener.onFail()
            }
            override fun onWrongUser() {
            }
            override fun onWrongPassword() {
            }
        }, 500)
    }

    //tools:
    private fun makeILog(msg: String){
        Log.i("networkUtil:", msg)
    }
    private fun makeWLog(msg: String){
        Log.w("networkUtil:", msg)
    }

}