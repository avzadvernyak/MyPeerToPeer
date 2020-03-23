package m.kampukter.mypeertopeer.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.mypeertopeer.R

/*
1 важное - наследник PeerConnection.Observer
2 важное - инициализация, тут она в init
3 важное - конфигурация стримов, тут она в setupMediaDevices
и последнее важное - диспозить это все ибо оно нативное и коллектор его не выгребет
 */

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        viewCamera2Button.setOnClickListener {
            startActivity(Intent(this, SecondVerActivity::class.java))
        }
    }
}