package com.myfav.rider.utils

import com.myfav.rider.R
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class NavigationClient(fragmentManager: FragmentManager) {
    private var fragmentManager: FragmentManager = fragmentManager

    fun loadFragment(fragment: Fragment, label: String, addToStack: Boolean) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        if (addToStack)
            transaction.addToBackStack(label)
        transaction.commit()
    }

    private fun loadFragment(fragment: Fragment,textView:TextView, title:String, label: String,addToStack: Boolean) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        if (addToStack)
            transaction.addToBackStack(label)
        transaction.commit()
        textView.text = title
    }

}