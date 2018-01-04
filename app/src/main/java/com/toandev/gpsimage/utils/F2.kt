package com.toandev.gpsimage.utils

interface F2<in A, in B> {
    operator fun invoke(`object`: A, object1: B)
}