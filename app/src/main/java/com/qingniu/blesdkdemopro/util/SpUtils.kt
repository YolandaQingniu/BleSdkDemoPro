package com.qingniu.blesdkdemopro.util

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils

/**
 * create by qzx
 * 2022/10/12: 11:55 上午
 * desc:
 */
object SpUtils {
    const val SP_KEY = "blesdkdemopro_sp_key"
    const val USER_INDEX_KEY = "user_index_key"
    const val USER_SECRET_KEY = "user_secret_key"
    const val USER_IS_VISITOR_KEY = "user_is_visitor_key"

    fun saveValue(tableKey: String, context: Context, key: String, value: Any){
        val editor = context.getSharedPreferences(tableKey, Context.MODE_PRIVATE).edit()
        if(!TextUtils.isEmpty(key) && value != null){
            if(value is String){
                editor.putString(key, value)
            } else if(value is Int){
                editor.putInt(key, value)
            } else if(value is Float){
                editor.putFloat(key, value)
            } else if(value is Long){
                editor.putLong(key, value)
            } else if(value is Boolean){
                editor.putBoolean(key, value)
            }
            editor.commit()
        }
    }

    fun getStringValue(tableKey: String, context: Context, key: String): String{
        val sp = context.getSharedPreferences(tableKey, Context.MODE_PRIVATE)
        return sp.getString(key, "") ?: ""
    }

    fun getIntValue(tableKey: String, context: Context, key: String): Int{
        val sp = context.getSharedPreferences(tableKey, Context.MODE_PRIVATE)
        return sp.getInt(key, 0)
    }

    fun getBooleanValue(tableKey: String, context: Context, key: String): Boolean{
        val sp = context.getSharedPreferences(tableKey, Context.MODE_PRIVATE)
        return sp.getBoolean(key, false)
    }

    fun cleanUserData(tableKey: String, context: Context){
        val editor = context.getSharedPreferences(tableKey, Context.MODE_PRIVATE).edit()
        editor.putInt(USER_INDEX_KEY, 0)
        editor.putInt(USER_SECRET_KEY, 0)
        editor.putBoolean(USER_IS_VISITOR_KEY, false)
        editor.commit()
    }
}