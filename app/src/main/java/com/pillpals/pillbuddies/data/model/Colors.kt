package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject

open class Colors(
    var id: Int = 0,
    var color: String = ""
) : RealmObject(){}
