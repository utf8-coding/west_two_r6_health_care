package com.utf8coding.healthcare.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.utf8coding.healthcare.R
import com.utf8coding.healthcare.networkRelated.NetworkUtils
import com.utf8coding.healthcare.utils.DensityUtils
import com.utf8coding.healthcare.view_models.LoginActivityViewModel
import com.utf8coding.healthcare.view_models.LoginActivityViewModel.Companion.CONNECTION_FAIL
import com.utf8coding.healthcare.view_models.LoginActivityViewModel.Companion.SUCCESS
import com.utf8coding.healthcare.view_models.LoginActivityViewModel.Companion.WRONG_PASSWORD
import com.utf8coding.healthcare.view_models.LoginActivityViewModel.Companion.WRONG_USERNAME
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.math.sqrt


@Suppress("TypeParameterFindViewById")
class LoginActivity : BaseActivity() {
    lateinit var viewModel: LoginActivityViewModel
    private val loginButton: Button
        get() {
        return findViewById(R.id.loginButton) as Button
        }
    private val registerButton: Button
        get() {
            return findViewById(R.id.registerButton) as Button
        }
    private val enterRegisterButton: TextView
        get() {
            return findViewById(R.id.enterRegisterButton) as TextView
        }
    private val titleTextView: TextView
        get() {
            return findViewById(R.id.titleTextView) as TextView
        }
    private val sloganTextView: TextView
        get() {
            return findViewById(R.id.sloganTextView) as TextView
        }
    private val userNameEditText: TextInputEditText
        get() {
            return findViewById(R.id.userName)
        }
    private val passWordEditText: TextInputEditText
        get() {
            return findViewById(R.id.password)
        }
    private val passWordCheckEditText: TextInputEditText
        get() {
            return findViewById(R.id.passwordCheck)
        }
    private val passWordCheckLayout: TextInputLayout
        get() {
            return findViewById(R.id.passwordCheckLayout)
        }
    private val loginBackGround: ConstraintLayout
        get() {
            return findViewById(R.id.loginBackGround)
        }
    private val backButton: ImageButton
        get() {
            return findViewById(R.id.backButton) as ImageButton
        }
    private val loadingIndicator: SpinKitView
        get(){
            return findViewById(R.id.loadingIndicator)
        }
    private val bgLeaves: ImageView
        get() {
            return findViewById(R.id.bgLeaves) as ImageView
        }
    private val bgLeaves2: ImageView
        get() {
            return findViewById(R.id.bgLeaves2) as ImageView
        }
//    private val transitionMasking: FrameLayout
//        get() {
//            return findViewById(R.id.transitionMasking) as FrameLayout
//        }
    private val passWord: String
        get() {
            return passWordEditText.text.toString()
        }
    private val userName: String
        get() {
            return userNameEditText.text.toString()
        }
    private val passWordCheck: String
        get() {
            return passWordCheckEditText.text.toString()
        }

    private var isInRegister = false
    companion object{
        const val ANIMATION_DURATION: Long = 400
    }
    private interface OnMaskingAnimEndListener{
        fun onMaskingAnimEnd();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        window.statusBarColor = Color.TRANSPARENT
        viewModel = ViewModelProvider(this).get(LoginActivityViewModel::class.java)

        window.statusBarColor = getColor(R.color.active_green)

        enterRegisterButton.setOnClickListener{
            onEnterRegisterButtonPressed()
        }
        backButton.setOnClickListener{
            onRegisterBackPressed()
        }
        loginButton.setOnClickListener {
            onLoginButtonPressed()
        }

        registerButton.setOnClickListener {
            getRegisterStatus()
        }

        passWordCheckEditText.setOnEditorActionListener { p0, _, _ ->
            viewModel.passWordCheck = p0?.text.toString()
            false
        }
    }


    private fun onLoginButtonPressed(){
        //加载图标
        if (userNameEditText.text.toString() != "" && userNameEditText.text.toString() != "" ){
            loadingIndicator.alpha = 0f
            loadingIndicator.visibility = VISIBLE
            loadingIndicator.animate().alpha(1f)
            val manager =
                applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(
                currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )

            viewModel.login (userName, passWord, object: LoginActivityViewModel.LoginListener{
                override fun onResponse(conditionCode: Int) {
                    when(conditionCode){
                        SUCCESS -> {
                            loadingIndicator.animate().alpha(0f)
                            loginBackGround.animate()
                                .alpha(0f)
                                .scaleY(1.5f)
                                .scaleX(1.5f)
                                .setInterpolator(AccelerateInterpolator())
                                .setStartDelay(80)
                                .duration = 1000
                            bgLeaves.animate()
                                .yBy(DensityUtils.dp2px(this@LoginActivity, 200f))
                                .xBy(DensityUtils.dp2px(this@LoginActivity, -100f))
                                .alpha(0f)
                                .scaleY(2.2f)
                                .scaleX(2.2f)
                                .setInterpolator(AccelerateInterpolator())
                                .setStartDelay(80)
                                .duration = 1000
                            bgLeaves2.animate()
                                .yBy(DensityUtils.dp2px(this@LoginActivity, -180f))
                                .xBy(DensityUtils.dp2px(this@LoginActivity, 90f))
                                .alpha(0f)
                                .scaleY(1.3f)
                                .scaleX(1.3f)
                                .setInterpolator(AccelerateInterpolator())
                                .setStartDelay(80)
                                .duration = 1000
                            thread {
                                sleep(450)
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                            }

//                            actionOtherVisible(true , loginButton, transitionMasking,
//                                object: OnMaskingAnimEndListener {
//                                    override fun onMaskingAnimEnd() {
//                                    }
//                                })
                        }

                        CONNECTION_FAIL -> {
                            loadingIndicator.animate().alpha(0f)
                            //除了网络错误，还有无法解析会导致调用。
                            showAlterDialog("网络错误", "检查下网络,不行的话求助开发者")
                        }

                        WRONG_PASSWORD -> {
                            loadingIndicator.animate().alpha(0f)
                            showAlterDialog("用户名或密码错误", "为你清空密码？", true)
                            passWordCheckEditText.setText("")
                        }

                        WRONG_USERNAME -> {
                            loadingIndicator.animate().alpha(0f)
                            showAlterDialog("用户名不存在", "再想想看？")}
                    }
                }
            })
        } else {
            if (userNameEditText.text.toString() == ""){
                //todo 用一个友善但是尽量简单的形式提醒进行输入：
                if (userNameEditText.text.toString() == ""){
                }
                if (passWordCheckEditText.text.toString() == ""){
                }
            }
        }
    }
    private fun onEnterRegisterButtonPressed(){
        makeILog("register pressed")
        isInRegister = true

        //animation:
        //enterRegisterButton:
        enterRegisterButton.animate()
            .alpha(0f).duration = ANIMATION_DURATION

        //loadingIndicator:
        loadingIndicator.animate()
            .yBy(DensityUtils.dp2px(this, 50f)).duration = ANIMATION_DURATION

        //loginButton, pasCheck:
        loginButton.animate()
            .alpha(0f)
            .yBy(DensityUtils.dp2px(this, 80f)).duration = ANIMATION_DURATION

        passWordCheckLayout.alpha = 0f
        passWordCheckLayout.visibility = View.VISIBLE
        passWordCheckLayout.animate().alpha(1f)
            .setListener(object: Animator.AnimatorListener{
                override fun onAnimationStart(p0: Animator?) {
                }
                override fun onAnimationEnd(p0: Animator?) {
                }
                override fun onAnimationCancel(p0: Animator?) {
                }
                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
            .setStartDelay(ANIMATION_DURATION/2).duration = ANIMATION_DURATION - ANIMATION_DURATION/2

        //registerButton, pasCheck:
        registerButton.alpha = 0f
        registerButton.visibility = VISIBLE
        registerButton.animate()
            .setListener(object: Animator.AnimatorListener{
                override fun onAnimationStart(p0: Animator?) {
                }
                override fun onAnimationEnd(p0: Animator?) {
                }
                override fun onAnimationCancel(p0: Animator?) {
                }
                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
            .alpha(1f)
            .setDuration(ANIMATION_DURATION)
            .yBy(DensityUtils.dp2px(this, 80f)).duration = ANIMATION_DURATION

        //backButton:
        backButton.visibility = VISIBLE
        backButton.animate().alpha(1f).duration = ANIMATION_DURATION
    }
    private fun onRegisterBackPressed(){
        makeILog("registerBack pressed")
        isInRegister = false

        //START - animation:
        //enterRegisterButton:
        enterRegisterButton.alpha = 0f
        enterRegisterButton.visibility = VISIBLE
        enterRegisterButton.animate()
            .alpha(1f).duration = ANIMATION_DURATION

        //loadingIndicator:
        loadingIndicator.animate()
            .yBy(DensityUtils.dp2px(this, -50f)).duration = ANIMATION_DURATION

        //loginButton, pasCheck:
        loginButton.animate()
            .alpha(1f)
            .yBy(DensityUtils.dp2px(this, -80f)).duration = ANIMATION_DURATION
        passWordCheckLayout.animate().setStartDelay(0)
            .setListener(object: Animator.AnimatorListener{
                override fun onAnimationStart(p0: Animator?) {
                }
                override fun onAnimationEnd(p0: Animator?) {
                    passWordCheckLayout.visibility = INVISIBLE
                }
                override fun onAnimationCancel(p0: Animator?) {
                }
                override fun onAnimationRepeat(p0: Animator?) {
                }
            }).alpha(0f).duration = ANIMATION_DURATION/2

        //registerButton:
        registerButton.animate()
            .alpha(0f)
            .setDuration((ANIMATION_DURATION - 0.5*ANIMATION_DURATION).toLong())
            .setListener(object: Animator.AnimatorListener{
                override fun onAnimationStart(p0: Animator?) {
                }
                override fun onAnimationEnd(p0: Animator?) {
                    registerButton.visibility = INVISIBLE
                }
                override fun onAnimationCancel(p0: Animator?) {
                }
                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
            .yBy(DensityUtils.dp2px(this, -80f)).duration = ANIMATION_DURATION

        //registerBackButton:
        backButton.animate().alpha(0f).duration = ANIMATION_DURATION
        //END - animation
    }
    private fun getRegisterStatus(){
        loadingIndicator.alpha = 0f
        loadingIndicator.visibility = VISIBLE
        loadingIndicator.animate().alpha(1f)
    }

    //animation:
    private fun actionOtherVisible(isShow: Boolean, triggerView: View, animView: View, animEndingListener: OnMaskingAnimEndListener) {

        //计算 triggerView(即搜索按钮) 的中心位置
        val tvLocation = IntArray(2)
        triggerView.getLocationInWindow(tvLocation)
        val tvX = tvLocation[0] + triggerView.width / 2
        val tvY = tvLocation[1] + triggerView.height / 2

        //计算 animView(即根布局) 的中心位置
        val avLocation = IntArray(2)
        animView.getLocationInWindow(avLocation)
        val avX = avLocation[0] + animView.width / 2
        val avY = avLocation[1] + animView.height / 2

        //计算宽高
        val rippleW = if (tvX < avX) animView.width - tvX else tvX - avLocation[0]
        val rippleH = if (tvY < avY) animView.height - tvY else tvY - avLocation[1]

        //勾股定理求斜边
        val maxRadius = sqrt((rippleW * rippleW + rippleH * rippleH).toDouble()).toFloat()
        val startRadius: Float
        val endRadius: Float

        //根据展示或隐藏设置起始与结束的半径
        if (isShow) {
            startRadius = 0f
            endRadius = maxRadius
        } else {
            startRadius = maxRadius
            endRadius = 0f
        }

        val anim = ViewAnimationUtils.createCircularReveal(animView, tvX, tvY, startRadius, endRadius)
        animView.visibility = View.VISIBLE
        anim.duration = (ANIMATION_DURATION * 1.5).toLong()
        anim.interpolator = DecelerateInterpolator()
        //监听动画结束，进行回调
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (isShow) {
                    animView.visibility = VISIBLE
                    animEndingListener.onMaskingAnimEnd()
                } else {
                    animView.visibility = INVISIBLE
                    animEndingListener.onMaskingAnimEnd()
                }
            }
        })
        anim.start()
    }

    //life cycle:
    override fun onResume() {
        userNameEditText.setText(viewModel.userName)
        passWordEditText.setText(viewModel.passWord)
        passWordCheckEditText.setText(viewModel.passWordCheck)
        super.onResume()
    }

    //overrides：
    override fun onBackPressed() {
        if (isInRegister){
            onRegisterBackPressed()
        } else {
            ActivityCollector.finishAll()
        }
    }

    //little tools:
    private fun makeToast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    private fun makeILog(msg: String){
        Log.i("Login activity:", msg)
    }
    private fun showAlterDialog(title: String, content: String, isPas: Boolean = false) {
        val alterDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        alterDialog.setTitle(title)
        alterDialog.setMessage(content)
        //显示
        if (isPas){
            alterDialog.setPositiveButton("好的",){ _,_ ->
                passWordEditText.setText("")
            }

            alterDialog.setNegativeButton("不用",){ _, _ ->}
        } else {
            alterDialog.setPositiveButton("好的",){ _, _ ->}
        }
        alterDialog.show()
    }

    //testAnything
    private fun testAnything(){
        //-------函数区域：
        fun testLogin(){
            NetworkUtils.login("A", "123123", object: NetworkUtils.LoginNetListener{
                override fun onSuccess() {
                }

                override fun onFailure() {
                }

                override fun onWrongUser() {
                }

                override fun onWrongPassword() {
                }

            })
        }
        //-------函数区域结束

        makeToast("testAnything")
    }
}