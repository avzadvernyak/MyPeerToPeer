package m.kampukter.mypeertopeer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import m.kampukter.mypeertopeer.R

class UserFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val fm = getSupportFragmentManager()
        (activity as? AppCompatActivity)?.supportFragmentManager?.let { NewUserDialogFragment.create().show(it, "newUser") }
        //fragmentManager?.let { NewUserDialogFragment.create().show(it, "newUser") }
    }
}
