package com.sil.morphlect.command

import com.sil.morphlect.data.StudioLayer

/**
* represents a command that applies an operation on a parameter of type `T`, returning an object of type `U`.
*/
interface Command<T, U> {
    fun execute(src: T): U
}