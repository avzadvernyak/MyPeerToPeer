package m.kampukter.mypeertopeer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class UserActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) supportFragmentManager.beginTransaction().add(
            android.R.id.content,
            UserFragment()
        ).commit()
    }

}
