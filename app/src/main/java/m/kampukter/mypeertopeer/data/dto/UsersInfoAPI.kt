package m.kampukter.mypeertopeer.data.dto

import m.kampukter.mypeertopeer.data.UserData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UsersInfoAPI {
    @GET("get_users_info.php?")
    fun getUsersData(): Call<List<UserData>>

    @POST("save_user_info.php?")
    fun saveUserInfo(
        @Query("id") id: String,
        @Query("userName") userName: String,
        @Query("tokenFCM") tokenFCM: String
    ): Call<ResponseBody>

    companion object Factory {
        private const val BASE_URL = "http://orbis.in.ua/api/"
        fun create(): UsersInfoAPI {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(UsersInfoAPI::class.java)
        }
    }
}