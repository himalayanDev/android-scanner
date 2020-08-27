package com.myfav.rider.ui.home.data

data class UserSelection(
    var Title: String = "", var Action: Int = 0, var isSelected: Boolean) {

    constructor(Title: String, Action: Int, isSelected: Boolean,
                Description: String,backgroundColor: Int, icon: Int, titleColor: Int, descriptionColor: Int ) : this(Title,Action,isSelected) {
    }
}
