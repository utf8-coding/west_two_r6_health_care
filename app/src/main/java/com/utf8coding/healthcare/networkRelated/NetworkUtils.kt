package com.utf8coding.healthcare.networkRelated

import android.content.Context
import android.util.Log
import com.utf8coding.healthcare.MyApplication
import com.utf8coding.healthcare.data.*
import com.utf8coding.healthcare.utils.GenerateTestContentUtils
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
    private fun testingLogin(userName: String, passWord: String, listener: LoginNetListener, timeOut: Long){
        if ((userName == "A" || userName == "testUser1" ) && passWord == "123123"){
            listener.onSuccess()
        } else if (userName != "A" && passWord == "123123") {
            listener.onWrongUser()
        } else if (userName == "A" && passWord != "123123"){
            listener.onWrongPassword()
        }
    }

    fun login(userName: String, passWord: String, listener: LoginNetListener){
        if ((userName == "A" || userName == "testUser1" ) && passWord == "123123"){
            listener.onSuccess()
            makeILog("logging success")
        } else if (userName != "A" && passWord == "123123") {
            listener.onWrongUser()
            makeILog("logging wrongUser")
        } else if (userName == "A" && passWord != "123123"){
            listener.onWrongPassword()
            makeILog("loginWrongPass")
        }
        MyApplication.context.getSharedPreferences("userData", Context.MODE_PRIVATE).edit()
            .putString("userName", "testUser1")
            .putString("passWord", "123123")
            .putInt("userId", -1)
            .putString("userHeadUri", "https://s1.ax1x.com/2022/07/03/j3zYX4.jpg")
            .apply()
    }

    //生活指数
    fun getLifeIndex(
        onSuccess: (lifeIndexData: LifeIndexData) -> Unit,
        onFailure: () -> Unit
    ){
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
                    onSuccess(lifeIndexData)
                } else {
                    makeILog("empty response! get epidemic response: $lifeIndexData")
                }
            }
            override fun onFailure(call: Call<LifeIndexData>, t: Throwable) {
                onFailure()
                t.printStackTrace()
                makeWLog("connection failed!!")
            }
        })
    }

    //疫情信息
    fun getEpidemicData(
        onSuccess: (mEpidemicData: EpidemicData) -> Unit,
        onFailure: () -> Unit
    ){
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
                    onSuccess(epidemicData)
                } else {
                    makeILog("empty response! epidemicData $epidemicData")
                }
            }
            override fun onFailure(call: Call<EpidemicData>, t: Throwable) {
                onFailure()
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
    //根据ID获取一些系列文章
    fun getArticleById(essayId: Int, listener: GetArticleByIdListener){
        if (essayId.toString() != ""){
            listener.onSuccess(GenerateTestContentUtils.generateArticle(essayId))
        } else {
            makeWLog("id is empty or null when get articles")
        }
    }
    //首页的文章
    fun getSuggestedArticle(userId: Int,
                            onSuccess: (mArticleDataList: ArrayList<ArticleData>) -> Unit,
                            onFailure: () -> Unit
                            ){
        getGeneralAppService(100).getSuggestedArticle(userId).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.EssayList>> {
            override fun onResponse(call: Call<NetWorkResponse<NetWorkResponse.EssayList>>,
                                    response: Response<NetWorkResponse<NetWorkResponse.EssayList>>
            ) {
                val articleDataList = response.body()?.data
                if (articleDataList == null) {
                    makeWLog("null article list body?")
                } else {
                    onSuccess(articleDataList.essayList)
                    makeILog("get articleData list success ${articleDataList.essayList.size}")
                }
            }
            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.EssayList>>,
                t: Throwable
            ) {
                onSuccess(GenerateTestContentUtils.generateArticleList())
                t.printStackTrace()
                makeWLog("suggestion article list getting failed!!")
            }
        })
    }

    fun searchArticleByKey(searchKey: String, onSuccess: (ArrayList<ArticleData>) -> Unit){
        getGeneralAppService(100).searchArticle(searchKey).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.EssayList>>{
            override fun onResponse(
                call: Call<NetWorkResponse<NetWorkResponse.EssayList>>,
                response: Response<NetWorkResponse<NetWorkResponse.EssayList>>
            ) {
                val body = response.body()
                if (body != null){
                    val articleList = body.data.essayList
                    onSuccess(articleList)
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
                onSuccess(GenerateTestContentUtils.generateArticleList())
            }

        })
    }

    //收藏管
    fun getCollection(userId: Int, onSuccess: (mArticleList: ArrayList<ArticleData>) -> Unit, onFailure: () -> Unit){
        //这里这样写是因为服务器只返回所收藏文章的ID列表，之后需要自行获取
        fun fromCollectionListGetArticleList(collectionList: ArrayList<NetWorkResponse.Collection>, onFeedback: (mArticleList: ArrayList<ArticleData>) -> Unit){
            val list = ArrayList<ArticleData>()
            for (collection in collectionList){
                getArticleById(collection.articleId, object: GetArticleByIdListener{
                    override fun onSuccess(articleData: ArticleData) {
                        list.add(articleData)
                        if (list.size == collectionList.size) {
                            onFeedback(list)
                        }
                    }
                    override fun onFail() {
                        onFeedback(GenerateTestContentUtils.generateArticleList())
                    }
                })
            }
        }

       getGeneralAppService(100).getCollection(userId).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.CollectionList>>{
            override fun onResponse(
                call: Call<NetWorkResponse<NetWorkResponse.CollectionList>>,
                response: Response<NetWorkResponse<NetWorkResponse.CollectionList>>
            ) {
                val body = response.body()
                val collectionList = body?.data?.list ?: ArrayList()
                fromCollectionListGetArticleList(
                    collectionList,
                    onFeedback = { onSuccess(it) }
                )
                makeILog("get collection success: $collectionList")
            }

            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.CollectionList>>,
                t: Throwable
            ) {
                onSuccess(GenerateTestContentUtils.generateArticleList())
            }

        })
    }
    //给后端减轻一点负担（会导致列表无序，得研究在过程中加锁才能解决，但是速度会变慢。）
    fun checkCollected(userId: Int, articleId: Int, onFeedBack: (isCollected: Boolean) -> Unit){
       getGeneralAppService(100).checkCollection(userId, articleId).enqueue(object : Callback<NetWorkResponse<Any>>{
            override fun onResponse(
                call: Call<NetWorkResponse<Any>>,
                response: Response<NetWorkResponse<Any>>
            ) {
                val body = response.body()
                if (body != null){
                    onFeedBack(body.message.toBoolean())
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
       getGeneralAppService(100).setCollection(userId, articleId).enqueue(object : Callback<NetWorkResponse<Any>>{
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
       getGeneralAppService(100).deleteCollection(userId, articleId).enqueue(object : Callback<NetWorkResponse<Any>>{
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
    fun searchMedByName(medName: String, onSuccess: (mMedList: ArrayList<MedData>) -> Unit, onFailure: () -> Unit){
       getGeneralAppService(100).searchDrugByName(medName).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.MedDataList>>{
            override fun onResponse(call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                                    response: Response<NetWorkResponse<NetWorkResponse.MedDataList>>
            ) {
                val medDataList = response.body()?.data
                if (medDataList == null){
                    makeWLog("null medData list body?")
                } else {
                    onSuccess(medDataList.medList)
                    makeILog("get medData list success: ${medDataList.medList}")
                }
            }
            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                t: Throwable
            ) {
                onSuccess(GenerateTestContentUtils.generateMedData())
                t.printStackTrace()
                makeWLog("suggestion medData list getting failed!!")
            }

        })
    }
    fun searchMedByType(medType: String, onSuccess: (mMedList: ArrayList<MedData>) -> Unit, onFailure: () -> Unit){
       getGeneralAppService(100).searchDrugByType(medType).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.MedDataList>>{
            override fun onResponse(call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                                    response: Response<NetWorkResponse<NetWorkResponse.MedDataList>>
            ) {
                val medDataList = response.body()?.data
                if (medDataList == null){
                    makeWLog("null medData list body?")
                } else {
                    onSuccess(medDataList.medList)
                    makeILog("get medData list success: ${medDataList.medList}")
                }
            }

            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                t: Throwable
            ) {
                onSuccess(GenerateTestContentUtils.generateMedData())
                t.printStackTrace()
                makeWLog("suggestion medData list getting failed!!")
            }

        })
    }
    fun searchMedByManufacturer(medManufacturer: String, onSuccess: (mMedList: ArrayList<MedData>) -> Unit, onFailure: () -> Unit){
       getGeneralAppService(100).searchDrugByMedManufacturer(medManufacturer).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.MedDataList>>{

            override fun onResponse(call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                                    response: Response<NetWorkResponse<NetWorkResponse.MedDataList>>
            ) {
                val medDataList = response.body()?.data
                if (medDataList == null){
                    makeWLog("null medData list body?")
                } else {
                    onSuccess(medDataList.medList)
                    makeILog("get medData list success: ${medDataList.medList}")
                }
            }

            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.MedDataList>>,
                t: Throwable
            ) {
                onSuccess(GenerateTestContentUtils.generateMedData())
                t.printStackTrace()
                makeWLog("suggestion medData list getting failed!!")
            }

        })
    }

    //文章的评论之获取
    fun getComment(articleId: Int, onSuccess: (commentList: ArrayList<CommentData>) -> Unit, onFailure: () -> Unit){
        getGeneralAppService(100).getComments(articleId).enqueue(object: Callback<NetWorkResponse<NetWorkResponse.CommentList>>{
            override fun onResponse(
                call: Call<NetWorkResponse<NetWorkResponse.CommentList>>,
                response: Response<NetWorkResponse<NetWorkResponse.CommentList>>
            ) {
                val mResponse = response
                if (mResponse.body() != null){
                    val commentList = mResponse.body()!!.data.commentList
                    onSuccess(commentList)
                } else {
                    makeWLog("get comment list null body?")
                }
            }

            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.CommentList>>,
                t: Throwable
            ) {
                onSuccess(GenerateTestContentUtils.generateCommentDataList())
                makeWLog("get comment list on net failure!")
            }
        })
    }
    fun setComment(userId: Int, articleId: Int, comment: String){
        getGeneralAppService(100).setComment(userId, articleId, comment).enqueue(object : Callback<NetWorkResponse<ResponseBody>>{
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

    //check netWork:
    fun isNetworkConnected(onSuccess: () -> Unit, onFailure: () -> Unit){
        testingLogin("A", "123123", object: LoginNetListener{
            override fun onSuccess() {
                onSuccess()
            }
            override fun onFailure() {
                onFailure()
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