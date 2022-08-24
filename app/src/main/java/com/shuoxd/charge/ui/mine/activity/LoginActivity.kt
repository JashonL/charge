package com.shuoxd.charge.ui.mine.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import com.shuoxd.charge.R
import com.shuoxd.charge.application.MainApplication.Companion.APP_OS
import com.shuoxd.charge.base.BaseActivity
import com.shuoxd.charge.databinding.ActivityLoginBinding
import com.shuoxd.charge.ui.mine.viewmodel.LoginViewModel
import com.shuoxd.lib.service.account.User
import com.shuoxd.lib.util.MD5Util
import com.shuoxd.lib.util.ToastUtil
import com.shuoxd.lib.util.Util

class LoginActivity : BaseActivity(), View.OnClickListener {


    companion object {
        fun start(context: Context?) {
            context?.startActivity(Intent(context, LoginActivity::class.java))
        }
    }


    private lateinit var bingding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bingding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bingding.root)


        initData()

        setListener()

    }

    private fun initData() {

        viewModel.loginLiveData.observe(this) {
            dismissDialog()
            if (it.second == null) {
                val user = it.first
                loginSuccess(user)
            } else {
                ToastUtil.show(it.second)
            }

        }

    }

    private fun loginSuccess(user: User?) {
        accountService().saveUserInfo(user)
        finish()

    }


    private fun setListener() {
        bingding.btnLogin.setOnClickListener(this)
        bingding.tvRegister.setOnClickListener(this)

    }


    override fun onClick(p0: View?) {
        when {
            p0 === bingding.btnLogin -> checkInfo()
            p0 === bingding.tvRegister -> {
                RegisterActivity.start(this)
            }
        }
    }


    private fun checkInfo() {
        val userName = bingding.etUsername.text.toString().trim()
        val password = bingding.etPassword.text.toString().trim()
        when {
            TextUtils.isEmpty(userName) -> {
                ToastUtil.show(getString(R.string.m74_please_input_username))
            }
            TextUtils.isEmpty(password) -> {
                ToastUtil.show(getString(R.string.m76_password_cant_empty))
            }
            else -> {
                //校验成功
                showDialog()
                val pwd_md5 = MD5Util.md5(password)
                var version = Util.getVersion(this)
                val phoneModel = Util.getPhoneModel()
                if (version == null) version = "";
                if (pwd_md5 != null) {
                    viewModel.login(userName, pwd_md5, APP_OS, phoneModel, version)
                }
            }
        }
    }


}